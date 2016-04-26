package com.example.chrissebesta.nyccyclemap;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

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

/**
 * Created by chrissebesta on 4/21/16.
 */
public class FetchCycleDataTask extends AsyncTask<String, Void, Void> {
    public final String LOG_TAG = FetchCycleDataTask.class.getSimpleName();
    public String jsonResponseString;
    public Context mContext;

    @Override
    protected Void doInBackground(String... params) {
        Log.d(LOG_TAG, "In the do in background phase");

        //Right now just hacking this all together in AsyncTask, needs to eventually take place in a SyncAdapter
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String nycPublicDataResponseString = null;

        // Construct the URL for the OpenWeatherMap query
        // Possible parameters are avaiable at OWM's forecast API page, at
        // http://openweathermap.org/API#forecast
//        final String FORECAST_BASE_URL =
//                "https://data.cityofnewyork.us/resource/qiz3-axqb.json?";
//        final String QUERY_PARAM = "q";
//        final String FORMAT_PARAM = "mode";
//        final String UNITS_PARAM = "units";
//        final String DAYS_PARAM = "cnt";
//        final String APP_ID = "APPID";
//        final String APP_KEY = "99c1da3dd06bf5bda6d3d333273554c8";
//
//        Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
//                .appendQueryParameter(QUERY_PARAM, locationQuery + ",USA")
//                .appendQueryParameter(FORMAT_PARAM, format)
//                .appendQueryParameter(UNITS_PARAM, units)
//                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
//                .appendQueryParameter(APP_ID, APP_KEY)
//                .build();

        URL url = null;
        try {
            //url = new URL("https://data.cityofnewyork.us/resource/qiz3-axqb.json");
            //TODO get query information from user and append to URL properly, allow user to select injuries, deaths, or both, and number of victims
            //TODO OR: use a SQL database to store everything with at least one cyclist injured or killed and then query based on user topics to limit number of internet calls


            url = new URL("https://data.cityofnewyork.us/resource/qiz3-axqb.json?$where=number_of_cyclist_injured%20%3E%200%20AND%20latitude%20%3E%2040&$limit=5000");
            //https://data.cityofnewyork.us/resource/qiz3-axqb.json?$where=number_of_cyclist_injured%20%3E%200%20AND%20latitude%20%3E%2040&$limit=100
            //increased limit since it was defaulting to a limit of 1000
            //url = new URL("https://data.cityofnewyork.us/resource/qiz3-axqb.json?$where=number_of_cyclist_killed%20%3E%200%20AND%20latitude%20%3E%2040&$limit=5000");


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
            jsonResponseString = nycPublicDataResponseString;


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
        // This will only happen if there was an error getting or parsing the forecast.
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.d(LOG_TAG, "In the post execute phase");

        try {
            getCycleDataFromJson(jsonResponseString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        super.onPostExecute(aVoid);
    }

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
        CycleDbHelper helper = new CycleDbHelper(mContext);
        SQLiteDatabase db = helper.getWritableDatabase();


        for (int i = 0; i < accidentJsonArray.length(); i++) {
            String arrayData = accidentJsonArray.getString(i);
            ContentValues contentValues = new ContentValues();
            //Log.d(LOG_TAG, "The array data at index " + i + " is: " + arrayData);
            JSONObject accident = accidentJsonArray.getJSONObject(i);
//            String contributingFactor1 = accident.getString(NYC_CONTRIBUTING_FACTOR_VEHISCLE_1);
//            Log.d(LOG_TAG, "The contibuting factor for event "+i+" is: "+contributingFactor1);

            //String latitude = accident.getString(NYC_LATITUDE);
            //String longitude = accident.getString(NYC_LONGITUDE);
            //Log.d(LOG_TAG, "The lat long data at index " + i + " is: " + latitude + ", " + longitude);


            //ContentValues contentValues = new ContentValues();
            contentValues.put(CycleContract.CycleEntry.COLUMN_LATITUDE, accident.getDouble(NYC_LATITUDE));
            contentValues.put(CycleContract.CycleEntry.COLUMN_LONGITUDE, accident.getDouble(NYC_LONGITUDE));
            //contentValues.put(CycleContract.CycleEntry.COLUMN_UNIQUE_KEY, accident.getString(NYC_UNIQUE_KEY));
            //contentValues.put(CycleContract.CycleEntry.COLUMN_BOROUGH, accident.getString(NYC_BOROUGH));
            //Log.d("CONTENTVALUES", contentValues.toString());
            db.insert(CycleContract.CycleEntry.TABLE_NAME, null, contentValues);
            contentValues.clear();
        }
        Log.d("BUILDTABLE", helper.getTableAsString(db, CycleContract.CycleEntry.TABLE_NAME));


    }
}
