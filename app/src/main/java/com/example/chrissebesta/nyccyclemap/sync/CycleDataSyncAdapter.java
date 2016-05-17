package com.example.chrissebesta.nyccyclemap.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import com.example.chrissebesta.nyccyclemap.R;
import com.example.chrissebesta.nyccyclemap.data.CycleContract;
import com.example.chrissebesta.nyccyclemap.data.CycleDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class CycleDataSyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String LOG_TAG = CycleDataSyncAdapter.class.getSimpleName();
    Context mContext;
    boolean mNoMoreDataToSync = false;

    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long MINUTES_PER_HOUR = 60L;
    public static final long HOURS_PER_DAY = 24L;
    public static final long SYNC_INTERVAL_IN_DAYS = 1L;
    //public static final long SYNC_INTERVAL = SYNC_INTERVAL_IN_DAYS * HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE;
    //Shortened sync interval for testing
    public static final long SYNC_INTERVAL = 10;

    public CycleDataSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync Called.");
        Log.d(LOG_TAG, "Account is: "+account+" while authority is: "+authority);
        syncDatabaseNow();
        return;
    }

    public static void setSyncFrequency(Context context){
        Log.d(LOG_TAG, "Setting sync frequency");
        ContentResolver.addPeriodicSync(getSyncAccount(context), context.getString(R.string.content_authority), Bundle.EMPTY, SYNC_INTERVAL);
    }

    public static void syncImmediately(Context context){
        Log.d(LOG_TAG, "syncImmediately Called.");

//        ContentResolver.requestSync(getSyncAccount(context),
//                context.getString(R.string.content_authority), null);
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);

    }

    private void syncDatabaseNow() {
        //Reset boolean to check for more data since a new sync was asked for
        mNoMoreDataToSync = false;
        Log.d(LOG_TAG, "in syncDatabaseNow and mNoMoreDataToSync is: " + mNoMoreDataToSync);

        //get Last unique number in current SQL database
        CycleDbHelper helper = new CycleDbHelper(mContext);
        SQLiteDatabase db = helper.getWritableDatabase();
        int lastUniqueNumberInDB = 0;

        //get most recent unique number currently stored in the database, only column necessary is date
        String[] columns = {CycleContract.CycleEntry.COLUMN_UNIQUE_KEY};
        Cursor cursor = db.query(CycleContract.CycleEntry.TABLE_NAME, columns, null, null, null, null, CycleContract.CycleEntry.COLUMN_UNIQUE_KEY + " DESC", String.valueOf(1));
        if (cursor.moveToFirst()) {
            lastUniqueNumberInDB = cursor.getInt(cursor.getColumnIndex(CycleContract.CycleEntry.COLUMN_UNIQUE_KEY));
        }

        Log.d(LOG_TAG, "Last unique key number in the database is: " + lastUniqueNumberInDB);
        //Close SQL database
        db.close();

        URL url = null;
        //Build URL with latest unique Key
        try {
            url = new URL("http://data.cityofnewyork.us/resource/qiz3-axqb.json?$where=(number_of_cyclist_killed%20%3E%200%20or%20number_of_cyclist_injured%20%3E%200)%20and%20latitude%20%3E%200%20and%20unique_key%20>%20" + lastUniqueNumberInDB+"&$order=unique_key%20ASC");
            Log.d("FETCH", "Fetching cycle data with URL: " + url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        //Right now just hacking this all together in AsyncTask, needs to eventually take place in a SyncAdapter
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String nycPublicDataResponseString = null;

        try {
            //Log.d("FETCH", "Fetching cycle data with URL: " + url);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            nycPublicDataResponseString = buffer.toString();
            Log.d("JSON", "The buffer is showing: " + nycPublicDataResponseString);
            String jsonResponseString = nycPublicDataResponseString;

            //process the returned JSON string
            try {
                getCycleDataFromJson(jsonResponseString);
            } catch (JSONException e) {
                e.printStackTrace();
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "The URL used to fetch the JSON is: " + url);
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        Log.d(LOG_TAG, "Reaching the end of the sync data method or recursive call and mNoMoreDataToSync is: " + mNoMoreDataToSync);

        //if there is still data left to sync, recursively call the method again, this will continue to be called until a JSON is returned with less than the 1000 limit
        if(!mNoMoreDataToSync){
            syncDatabaseNow();
        }

        return;
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        Log.d(LOG_TAG, "Getting sync account and returning a basic fake account");
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

        }
        return newAccount;
    }


    /**
     *
     * @param cycleDataJsonString
     * @throws JSONException
     * Method to pull JSON data from NYC Online Data and put it in to the local SQLite database
     */
    public void getCycleDataFromJson(String cycleDataJsonString) throws JSONException {
        //Strings provided by API for JSON parsing
        final String NYC_DATE = "date"; //floating time stamp
        final String NYC_time = "time"; //text
        final String NYC_BOROUGH = "borough";
        final String NYC_ZIP_CODE = "zip_code";
        final String NYC_LATITUDE = "latitude"; //number
        final String NYC_LONGITUDE = "longitude";
        final String NYC_POINT = "point"; //point
        final String NYC_ON_STREET_NAME = "on_street_name"; //text
        final String NYC_OFF_STREET_NAME = "off_street_name";
        final String NYC_CROSS_STREET_NAME = "cross_street_name";
        final String NYC_NUMBER_OF_PERSONS_INJURED = "number_of_persons_injured"; //number
        final String NYC_NUMBER_OF_PERSONS_KILLED = "number_of_persons_killed";
        final String NYC_NUMBER_OF_PEDESTRIANS_INJURED = "number_of_pedestrians_injured";
        final String NYC_NUMBER_OF_PEDESTRIANS_KILLED = "number_of_pedestrians_killed";
        final String NYC_NUMBER_OF_CYCLIST_INJURED = "number_of_cyclist_injured";
        final String NYC_NUMBER_OF_CYCLIST_KILLED = "number_of_cyclist_killed";
        final String NYC_NUMBER_OF_MOTORIST_INJURED = "number_of_motorist_injured";
        final String NYC_NUMBER_OF_MOTORIST_KILLED = "number_of_motorist_killed";
        final String NYC_CONTRIBUTING_FACTOR_VEHISCLE_1 = "contributing_factor_vehicle_1"; //text
        final String NYC_CONTRIBUTING_FACTOR_VEHISCLE_2 = "contributing_factor_vehicle_2";
        final String NYC_CONTRIBUTING_FACTOR_VEHISCLE_3 = "contributing_factor_vehicle_3";
        final String NYC_CONTRIBUTING_FACTOR_VEHISCLE_4 = "contributing_factor_vehicle_4";
        final String NYC_CONTRIBUTING_FACTOR_VEHISCLE_5 = "contributing_factor_vehicle_5";
        final String NYC_UNIQUE_KEY = "unique_key"; //number
        final String NYC_VEHICLE_TYPE_CODE_1 = "vehicle_type_code1"; //text
        final String NYC_VEHICLE_TYPE_CODE_2 = "vehicle_type_code2";
        final String NYC_VEHICLE_TYPE_CODE_3 = "vehicle_type_code3";
        final String NYC_VEHICLE_TYPE_CODE_4 = "vehicle_type_code4";
        final String NYC_VEHICLE_TYPE_CODE_5 = "vehicle_type_code5";
        final String NYC_LOCATION_ZIP = "location_zip";
        final String NYC_LOCATION_CITY = "location_city";
        final String NYC_LOCATION_ADDRESS = "location_address";
        final String NYC_LOCATION_STATE = "location_state";

        //pull data from JSON request response and put in to JSON array
        JSONArray accidentJsonArray = new JSONArray(cycleDataJsonString);
        if (accidentJsonArray.length() == 0) {
            Log.d(LOG_TAG, "THE RETURNED JSON ARRAY IS EMPTY!");
            mNoMoreDataToSync = true;
        } else {
            CycleDbHelper helper = new CycleDbHelper(mContext);
            SQLiteDatabase db = helper.getWritableDatabase();

            Log.d(LOG_TAG, "Adding " + accidentJsonArray.length() + " items to the database");
            for (int i = 0; i < accidentJsonArray.length(); i++) {
                String arrayData = accidentJsonArray.getString(i);
                ContentValues contentValues = new ContentValues();
                //Log.d(LOG_TAG, "The array data at index " + i + " is: " + arrayData);
                JSONObject accident = accidentJsonArray.getJSONObject(i);


                contentValues.put(CycleContract.CycleEntry.COLUMN_DATE, accident.getString(NYC_DATE));
                contentValues.put(CycleContract.CycleEntry.COLUMN_NUMBER_OF_CYCLIST_INJURED, accident.getString(NYC_NUMBER_OF_CYCLIST_INJURED));
                contentValues.put(CycleContract.CycleEntry.COLUMN_NUMBER_OF_CYCLIST_KILLED, accident.getString(NYC_NUMBER_OF_CYCLIST_KILLED));
                contentValues.put(CycleContract.CycleEntry.COLUMN_LATITUDE, accident.getDouble(NYC_LATITUDE));
                contentValues.put(CycleContract.CycleEntry.COLUMN_LONGITUDE, accident.getDouble(NYC_LONGITUDE));
                contentValues.put(CycleContract.CycleEntry.COLUMN_UNIQUE_KEY, accident.getString(NYC_UNIQUE_KEY));
                db.insert(CycleContract.CycleEntry.TABLE_NAME, null, contentValues);
                contentValues.clear();

            }
            if (accidentJsonArray.length() < 1000) {
                mNoMoreDataToSync = true;
            }
            //Close SQL database object
            db.close();
        }
    }
}