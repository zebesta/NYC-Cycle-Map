package com.example.chrissebesta.nyccyclemap;

import android.os.AsyncTask;
import android.util.Log;

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

    @Override
    protected Void doInBackground(String... params) {
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
            url = new URL("https://data.cityofnewyork.us/resource/qiz3-axqb.json");

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



        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "The URL used to fetch the JSON is: " + url);
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
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
}
