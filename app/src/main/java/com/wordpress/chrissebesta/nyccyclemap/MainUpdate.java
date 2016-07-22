package com.wordpress.chrissebesta.nyccyclemap;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.wordpress.chrissebesta.nyccyclemap.data.CycleContract;
import com.wordpress.chrissebesta.nyccyclemap.data.CycleDbHelper;
import com.wordpress.chrissebesta.nyccyclemap.sync.CycleDataSyncAdapter;

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

public class MainUpdate extends AppCompatActivity {

    private static final String LOG_TAG = MainUpdate.class.getSimpleName();
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_update);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean previouslyStarted = mSharedPreferences.getBoolean(getString(R.string.pref_previously_started), false);
        if (!previouslyStarted) {
            //First run detected, update the database!
            Log.d(LOG_TAG, "First run detected, updating database");
            FetchCycleDataTask fetch = new FetchCycleDataTask(this);
            fetch.execute();
        }else{
            //Not the first run, jump to Main Activity to show map
            Log.d(LOG_TAG, "not the first run, starting up app");
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
    public class FetchCycleDataTask extends AsyncTask<Void, Void, Void> {
        public final String LOG_TAG = FetchCycleDataTask.class.getSimpleName();
        private Context mContext;
        private boolean mNoMoreDataToSync = false;

        public FetchCycleDataTask(Context c) {
            mContext = c;
        }


        @Override
        protected Void doInBackground(Void... params) {
            mNoMoreDataToSync = false;
            Log.d(LOG_TAG, "in doInBackground and mNoMoreDataToSync is: " + mNoMoreDataToSync);

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
                url = new URL("http://data.cityofnewyork.us/resource/qiz3-axqb.json?$where=(number_of_cyclist_killed%20%3E%200%20or%20number_of_cyclist_injured%20%3E%200)%20and%20latitude%20%3E%200%20and%20unique_key%20>%20" + lastUniqueNumberInDB + "&$order=unique_key%20ASC"+"&$limit=1000"+"&$$app_token="+mContext.getString(R.string.app_token));
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
                    return null;
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
                    return null;
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
            if (!mNoMoreDataToSync) {
                FetchCycleDataTask fetchRecursive = new FetchCycleDataTask(mContext);
                //noinspection ResourceType
                fetchRecursive.execute();
                //syncDatabase();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            SharedPreferences.Editor edit = mSharedPreferences.edit();
            edit.putBoolean(mContext.getString(R.string.syncing), Boolean.TRUE);
            edit.apply();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //Log.d(LOG_TAG, "In the post execute phase and the boolean flag for last thread is set to: " + mLastThreadBoolean);
            if (mNoMoreDataToSync) {
                Toast.makeText(mContext, "Your database is up to date!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, MainActivity.class);
                startActivity(intent);
            }
            super.onPostExecute(aVoid);
        }

        /**
         * Pulls cycle data from Json and puts it in to the database
         *
         * @param cycleDataJsonString //JSON returned from NYC Open Data
         * @throws JSONException Method to pull JSON data from NYC Online Data and put it in to the local SQLite database
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
                //Set syncing shared preference to false
                Log.d(LOG_TAG, "Setting shared prefs sync to false");
                SharedPreferences.Editor edit = mSharedPreferences.edit();
                edit.putBoolean(mContext.getString(R.string.syncing), Boolean.FALSE);
                edit.putBoolean(mContext.getString(R.string.pref_previously_started), Boolean.TRUE);
                edit.apply();
                //setting automatic sync frequency up:
                ContentResolver.setSyncAutomatically(CycleDataSyncAdapter.getSyncAccount(mContext), mContext.getString(R.string.content_authority), true);
                CycleDataSyncAdapter.setSyncFrequency(mContext);
            } else {
                CycleDbHelper helper = new CycleDbHelper(mContext);
                SQLiteDatabase db = helper.getWritableDatabase();

                Log.d(LOG_TAG, "Adding " + accidentJsonArray.length() + " items to the database");
                //Use manually controlled database transaction to reduce total number of writes by grouping them together
                db.beginTransaction();
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
                db.setTransactionSuccessful();
                db.endTransaction();
                if (accidentJsonArray.length() < 1000) {
                    mNoMoreDataToSync = true;
                    //Set syncing shared preference to false
                    Log.d(LOG_TAG, "Setting shared prefs sync to false");
                    SharedPreferences.Editor edit = mSharedPreferences.edit();
                    edit.putBoolean(mContext.getString(R.string.syncing), Boolean.FALSE);
                    edit.putBoolean(mContext.getString(R.string.pref_previously_started), Boolean.TRUE);
                    edit.apply();
                    //setting automatic sync frequency up since initial sync is done
                    ContentResolver.setSyncAutomatically(CycleDataSyncAdapter.getSyncAccount(mContext), mContext.getString(R.string.content_authority), true);
                    CycleDataSyncAdapter.setSyncFrequency(mContext);
                }
                db.close();
            }
        }
    }
}
