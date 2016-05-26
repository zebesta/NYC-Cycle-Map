package com.example.chrissebesta.nyccyclemap;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chrissebesta.nyccyclemap.details.Detail;
import com.example.chrissebesta.nyccyclemap.details.DetailAdapter;

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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class DetailActivity extends AppCompatActivity {
    public final String LOG_TAG = DetailActivity.class.getSimpleName();
    //TextView textView;
    ProgressBar progressBar;
    ListView detailsListView;
    ArrayList<Detail> details = new ArrayList<Detail>();
    DetailAdapter detailAdapter;


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
//        textView = (TextView) findViewById(R.id.detail_activity_text_view);
//        if (textView != null) {
//            textView.setText("" + uniqueId);
//        }
        progressBar = (ProgressBar) findViewById(R.id.detail_activity_progressbar);
        detailAdapter = new DetailAdapter(this, details);
        detailsListView = (ListView) findViewById(R.id.detail_activity_list_view);
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

            //textView.setText(s);
            progressBar.setVisibility(View.GONE);
            //textView.setVisibility(View.VISIBLE);
            detailsListView.setAdapter(detailAdapter);
            Log.d(LOG_TAG, "The size of the details array is: " + details.size());
            Log.d(LOG_TAG, "The size of the list view count is: "+detailsListView.getAdapter().getCount());

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
                            //ignore location value to display to user, this is repeated info
                            if(!key.contains("location")) {
                                //If it is a date, format it
                                if(key.contains("date")){
                                    String inputDateStr = (String) value;
                                    //Convert date format, this is done here to prevent doing it for every marker, only done on click instead
                                    DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
                                    DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy");
                                    Date date = null;
                                    try {
                                        date = inputFormat.parse(inputDateStr);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    String outputDateStr = outputFormat.format(date);
                                    value = outputDateStr;

                                }
                                //replace underscored with spaces to make keys readable
                                key = key.replace("_", " ");
                                result = result + key + ": " + value + "\n";
                                details.add(new Detail(key, (String) value));
                            }
                            Log.d(LOG_TAG, "The key is: "+key+ " and the value is: "+value);
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
