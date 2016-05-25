package com.example.chrissebesta.nyccyclemap;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

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
import java.util.Iterator;

public class DetailActivity extends AppCompatActivity {
    public final String LOG_TAG = DetailActivity.class.getSimpleName();
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get unique ID from intent
        int uniqueId = getIntent().getIntExtra(getString(R.string.unique_id_extra_key), 0);
        Log.d(LOG_TAG, "The unique ID is: "+uniqueId);
        textView = (TextView) findViewById(R.id.detail_activity_text_view);
        if (textView != null) {
            textView.setText("" + uniqueId);
        }
        FetchDetailsData fetch = new FetchDetailsData();
        new FetchDetailsData().execute(new Integer(uniqueId));
    }



    public class FetchDetailsData extends AsyncTask<Integer, Void, String> {
        public final String LOG_TAG = this.getClass().getSimpleName();
        TextView detailsTextView;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
            String returnString=null;
            Log.d(LOG_TAG, "In the do in background phase");
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String nycPublicDataResponseString = null;
            URL url = null;
            int uniqueId = 0;
            uniqueId = params[0].intValue();
            try {
                url = new URL("http://data.cityofnewyork.us/resource/qiz3-axqb.json?$where=unique_key=" + uniqueId);
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
                try {
                    returnString = getCycleDataFromJson(nycPublicDataResponseString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return returnString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            textView.setText(s);
        }

        public String getCycleDataFromJson(String cycleDataJsonString) throws JSONException {
            String result = "";

            //pull data from JSON request response and put in to JSON array
            JSONArray accidentJsonArray = new JSONArray(cycleDataJsonString);
            if (accidentJsonArray.length() == 0) {
                Log.d(LOG_TAG, "THE RETURNED JSON ARRAY IS EMPTY!");
            } else {
                for (int i = 0; i < accidentJsonArray.length(); i++) {
                    JSONObject accident = accidentJsonArray.getJSONObject(i);
                    Iterator<String> iter = accident.keys();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        try {
                            Object value = accident.get(key);
                            Log.d(LOG_TAG, "The key is: "+key+ " and the value is: "+value);
                            result = result + key+": "+value+"\n";
                        } catch (JSONException e) {
                            // Something went wrong!
                        }
                    }
                }
            }

            return result;
        }
    }

}
