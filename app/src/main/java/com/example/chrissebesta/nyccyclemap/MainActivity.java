package com.example.chrissebesta.nyccyclemap;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chrissebesta.nyccyclemap.data.CycleContract;
import com.example.chrissebesta.nyccyclemap.data.CycleDbHelper;

public class MainActivity extends AppCompatActivity {
    public final String LOG_TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button refreshButton = (Button) findViewById(R.id.refreshbutton);
        Button brooklynButton = (Button) findViewById(R.id.brooklynButton);
        Button clearSqlDb = (Button) findViewById(R.id.clearSQL);
        Button mapDatabase = (Button) findViewById(R.id.mapDatabase);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        final TextView loadingText = (TextView) findViewById(R.id.loadingTextView);
        //final FetchCycleDataTask fetch = new FetchCycleDataTask();
//        final FetchCycleDataTask fetch = new FetchCycleDataTask();
//        fetch.mContext = getBaseContext();

        assert refreshButton != null;
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //fetch Json data from NYC Data
                final FetchCycleDataTask fetch = new FetchCycleDataTask();
                //TODO THIS IS PROBABLY A HORRIBLE WAY TO MESS WITH THE UI FROM ASYNC TASK! Look in to this
                //Pass UI effecting variable to the Asyc task
                fetch.mContext = getBaseContext();
                fetch.mProgressBar = progressBar;
                fetch.mTextView = loadingText;
                fetch.execute();
                assert progressBar != null;
                progressBar.setVisibility(View.VISIBLE);
                loadingText.setVisibility(View.VISIBLE);

                if (fetch.getStatus() == AsyncTask.Status.RUNNING) {
                    // My AsyncTask is currently doing work in doInBackground()
                    Log.d(LOG_TAG, "Still working on Async task");
                }
                if (fetch.getStatus() == AsyncTask.Status.FINISHED) {
                    // My AsyncTask is done and onPostExecute was called
                    Log.d(LOG_TAG, "Finished the async task and now making load image invisible");
                    progressBar.setVisibility(View.INVISIBLE);
                    loadingText.setVisibility(View.INVISIBLE);
                }


//                CycleDbHelper helper = new CycleDbHelper(getBaseContext());
//                SQLiteDatabase db = helper.getReadableDatabase();
//                Log.d("BUILDTABLE", helper.getTableAsString(db, CycleContract.CycleEntry.TABLE_NAME));
            }
        });

        assert brooklynButton != null;
        brooklynButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CycleDbHelper helper = new CycleDbHelper(getBaseContext());
                SQLiteDatabase db = helper.getReadableDatabase();
                Log.d(LOG_TAG, "Querying db for Brooklyn results");
                Cursor cursor = db.rawQuery("SELECT * FROM " + CycleContract.CycleEntry.TABLE_NAME + " WHERE " + CycleContract.CycleEntry.COLUMN_BOROUGH + "='BROOKLYN'", null);
                String cursorString = helper.cursorToString(cursor);
                Log.d(LOG_TAG, cursorString);
                cursor.close();
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
}
