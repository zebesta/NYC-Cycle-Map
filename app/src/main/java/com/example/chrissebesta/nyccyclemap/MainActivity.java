package com.example.chrissebesta.nyccyclemap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appyvet.rangebar.RangeBar;
import com.example.chrissebesta.nyccyclemap.data.CycleContract;
import com.example.chrissebesta.nyccyclemap.data.CycleDbHelper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    public final String LOG_TAG = MainActivity.class.getSimpleName();
    //The starting year of the data in the NYC Open Data library
    public final int STARTING_YEAR_OF_DATA = 2012;
    public final int endingYearOfData = Calendar.getInstance().get(Calendar.YEAR);
    //Views that need to be accessible outside of onCreate
    ProgressBar mProgressBar;
    TextView mLoadingText;
    RangeBar mMaterialRangeBar;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button refreshButton = (Button) findViewById(R.id.refreshbutton);
        Button clearSqlDb = (Button) findViewById(R.id.clearSQL);
        Button mapDatabase = (Button) findViewById(R.id.mapDatabase);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar = progressBar;
        final TextView loadingText = (TextView) findViewById(R.id.loadingTextView);
        mLoadingText = loadingText;
        final TextView yearMappingTextView = (TextView) findViewById(R.id.yearMappingTextView);
        final RangeBar materialRangeBar = (RangeBar) findViewById(R.id.materialRangeBarWithDates);
        mMaterialRangeBar = materialRangeBar;
        final CheckedTextView injuredCheckedTextView = (CheckedTextView) findViewById(R.id.injuredCheckedTextView);
        final CheckedTextView killedCheckedTextView = (CheckedTextView) findViewById(R.id.killedCheckedView);

        //update the injured/killed checkedTextViews based on what was previously set in the shared preferences, default to true
        SharedPreferences sharedPreferences =getSharedPreferences(getString(R.string.sharedpreference), Context.MODE_PRIVATE);
        boolean injured = sharedPreferences.getBoolean(getString(R.string.injuredcyclists), true);
        boolean killed = sharedPreferences.getBoolean(getString(R.string.killedcyclists), true);
        injuredCheckedTextView.setChecked(injured);
        killedCheckedTextView.setChecked(killed);
        //set text view to indicate which years are going to be mapped by user
        final int startDate = sharedPreferences.getInt(getString(R.string.mindate), STARTING_YEAR_OF_DATA);
        final int endDate = sharedPreferences.getInt(getString(R.string.maxdate), endingYearOfData);
        final String[] textForYearsToBeMapped = {"Mapping data for years: " + startDate + " - " + endDate};
        yearMappingTextView.setText(textForYearsToBeMapped[0]);


//        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt(getString(R.string.mindate), 0);
//        editor.putInt(getString(R.string.maxdate), 2020);
//        editor.commit();

        //final FetchCycleDataTask fetch = new FetchCycleDataTask();
//        final FetchCycleDataTask fetch = new FetchCycleDataTask();
//        fetch.mContext = getBaseContext();
        materialRangeBar.setPinRadius(30);
        materialRangeBar.setTickHeight(4);
        materialRangeBar.setTickEnd(endingYearOfData);
        materialRangeBar.setTickStart(STARTING_YEAR_OF_DATA);
        materialRangeBar.setTickInterval(1);
        materialRangeBar.setBarWeight(8);
        try {
            URL urlToUse = new URL("http://data.cityofnewyork.us/resource/qiz3-axqb.json?$where=number_of_cyclist_killed%20%3E%200%20and%20latitude%20%3E%200%20and%20date%20between%20%27"+materialRangeBar.getLeftPinValue()+"-01-01T10:00:00%27%20and%20%27"+materialRangeBar.getRightPinValue()+"-12-31T23:59:00%27");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        //Build URL based on range bar values here, can modify later
        materialRangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {
                Log.d("RANGEBAR", "Range bar is now set to look between " + leftPinValue + " and " + rightPinValue);

                //update shared preferences for Query when user maps new data
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.sharedpreference), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(getString(R.string.mindate), Integer.parseInt(leftPinValue));
                editor.putInt(getString(R.string.maxdate), Integer.parseInt(rightPinValue));
                textForYearsToBeMapped[0] = "Mapping data for years: "+leftPinValue +" - "+rightPinValue;
                yearMappingTextView.setText(textForYearsToBeMapped[0]);
                editor.commit();
            }
        });

        injuredCheckedTextView.setOnClickListener(new CheckedTextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                //update check box and shared preferences for Query when user maps new data
                injuredCheckedTextView.setChecked(!injuredCheckedTextView.isChecked());
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.sharedpreference), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.injuredcyclists), injuredCheckedTextView.isChecked());
                editor.commit();
            }
        });
        killedCheckedTextView.setOnClickListener(new CheckedTextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                //update check box and shared preferences for Query when user maps new data
                killedCheckedTextView.setChecked(!killedCheckedTextView.isChecked());
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.sharedpreference), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.killedcyclists), killedCheckedTextView.isChecked());
                editor.commit();
            }
        });

        assert refreshButton != null;
        refreshButton.setOnClickListener(new View.OnClickListener() {
            //TODO need to check the existing SQL database for the latest date currently stored and then when the database URL is built, add this date constraint
            //Goal is to allow users to only have the large update once, and any updates conducted later just pull new data that has been added to NYC Open maps


            @Override
            public void onClick(View v) {
                CycleDbHelper helper = new CycleDbHelper(getBaseContext());
                SQLiteDatabase db = helper.getWritableDatabase();
                //TODO will want to modify this to no longer delete DB
                db.delete(CycleContract.CycleEntry.TABLE_NAME, null, null);
                Log.d(LOG_TAG, "Clearing Database");

                //fetch all the data from starting year to the current year
                Log.d("FETCH", "Fetching data between "+STARTING_YEAR_OF_DATA + " and " +endingYearOfData);
                //TODO need to change this to only pull from the years that are not currently in the DB
                for (int i = STARTING_YEAR_OF_DATA; i<=endingYearOfData;i++){
                    fetchCycleData(i);
                }

                //fetch Json data from NYC Data
//                final FetchCycleDataTask fetch = new FetchCycleDataTask();
//                //TODO THIS IS PROBABLY A HORRIBLE WAY TO MESS WITH THE UI FROM ASYNC TASK! Look in to this
//                //Pass UI effecting variable to the Asyc task
//                fetch.mContext = getBaseContext();
//                fetch.mProgressBar = progressBar;
//                fetch.mTextView = loadingText;
//                try {
//                    fetch.mUrlCycleData = new URL("http://data.cityofnewyork.us/resource/qiz3-axqb.json?$where=number_of_cyclist_killed%20%3E%200%20and%20latitude%20%3E%200%20and%20date%20between%20%27"+materialRangeBar.getLeftPinValue()+"-01-01T10:00:00%27%20and%20%27"+materialRangeBar.getRightPinValue()+"-12-31T23:59:00%27");
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    fetch.mUrlCycleData = new URL("");
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                }
//                fetch.execute();
//                assert progressBar != null;
//                progressBar.setVisibility(View.VISIBLE);
//                loadingText.setVisibility(View.VISIBLE);
//
//                if (fetch.getStatus() == AsyncTask.Status.RUNNING) {
//                    // My AsyncTask is currently doing work in doInBackground()
//                    Log.d(LOG_TAG, "Still working on Async task");
//                }
//                if (fetch.getStatus() == AsyncTask.Status.FINISHED) {
//                    // My AsyncTask is done and onPostExecute was called
//                    Log.d(LOG_TAG, "Finished the async task and now making load image invisible");
//                    progressBar.setVisibility(View.INVISIBLE);
//                    loadingText.setVisibility(View.INVISIBLE);
//                }


//                CycleDbHelper helper = new CycleDbHelper(getBaseContext());
//                SQLiteDatabase db = helper.getReadableDatabase();
//                Log.d("BUILDTABLE", helper.getTableAsString(db, CycleContract.CycleEntry.TABLE_NAME));
            }
        });


        assert clearSqlDb != null;
        clearSqlDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CycleDbHelper helper = new CycleDbHelper(getBaseContext());
                SQLiteDatabase db = helper.getWritableDatabase();
                db.delete(CycleContract.CycleEntry.TABLE_NAME, null, null);
                Log.d(LOG_TAG, "Clearing Database");
            }
        });

        assert mapDatabase != null;
        mapDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
            }
        });

//        if(fetch.getStatus() == AsyncTask.Status.RUNNING){
//            // My AsyncTask is currently doing work in doInBackground()
//            Log.d(LOG_TAG, "Still working on Async task");
//
//        }
//        if(fetch.getStatus() == AsyncTask.Status.FINISHED){
//            // My AsyncTask is done and onPostExecute was called
//            Log.d(LOG_TAG, "Finished the async task and now making load image invisible");
//            progressBar.setVisibility(View.INVISIBLE);
//            loadingText.setVisibility(View.INVISIBLE);
//        }
    }

    private void fetchCycleData(int year){
        final FetchCycleDataTask fetch = new FetchCycleDataTask();
        //TODO THIS IS PROBABLY A HORRIBLE WAY TO MESS WITH THE UI FROM ASYNC TASK! Look in to this
        //Pass UI effecting variable to the Asyc task
        fetch.mContext = getBaseContext();
        fetch.mProgressBar = mProgressBar;
        fetch.mTextView = mLoadingText;
        try {
            //URL for both injured and killed cyclists
            fetch.mUrlCycleData = new URL("http://data.cityofnewyork.us/resource/qiz3-axqb.json?$where=(number_of_cyclist_killed%20%3E%200%20or%20number_of_cyclist_injured%20%3E%200)%20and%20latitude%20%3E%200%20and%20date%20between%20%27"+year+"-01-01T10:00:00%27%20and%20%27"+(year+1)+"-01-01T10:00:00%27");
            //URL for only killed cyclists (useful for testing as it is much much faster)
            //fetch.mUrlCycleData = new URL("http://data.cityofnewyork.us/resource/qiz3-axqb.json?$where=number_of_cyclist_killed%20%3E%200%20and%20latitude%20%3E%200%20and%20date%20between%20%27"+year+"-01-01T10:00:00%27%20and%20%27"+(year+1)+"-01-01T10:00:00%27");

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        //set the last threat flag to true if this is the last fetch task to be run
        if(year == endingYearOfData){
            fetch.lastThread = true;
        }
//        try {
//            fetch.mUrlCycleData = new URL("");
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
        fetch.execute();
        Log.d("FETCH", "Fetching cycle data between "+year + " and "+(year+1));
        assert mProgressBar != null;
        mProgressBar.setVisibility(View.VISIBLE);
        mLoadingText.setVisibility(View.VISIBLE);

//        if (fetch.getStatus() == AsyncTask.Status.RUNNING) {
//            // My AsyncTask is currently doing work in doInBackground()
//            Log.d(LOG_TAG, "Still working on Async task");
//        }
//        if (fetch.getStatus() == AsyncTask.Status.FINISHED) {
//            // My AsyncTask is done and onPostExecute was called
//            Log.d(LOG_TAG, "Finished the async task and now making load image invisible");
//            mProgressBar.setVisibility(View.INVISIBLE);
//            mLoadingText.setVisibility(View.INVISIBLE);
//        }
    }
}
