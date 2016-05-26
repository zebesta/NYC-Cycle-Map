package com.example.chrissebesta.nyccyclemap;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.appyvet.rangebar.RangeBar;
import com.example.chrissebesta.nyccyclemap.data.CycleContract;
import com.example.chrissebesta.nyccyclemap.data.CycleDbHelper;
import com.example.chrissebesta.nyccyclemap.sync.CycleDataSyncAdapter;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    // Constants
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.example.android.datasync.provider";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "nyccyclemap.example.com";
    // The account name
    public static final String ACCOUNT = "dummyaccount";
    // Sync interval constants
    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long MINUTES_PER_HOUR = 60L;
    public static final long HOURS_PER_DAY = 24L;
    public static final long SYNC_INTERVAL_IN_DAYS = 1L;
    //public static final long SYNC_INTERVAL = SYNC_INTERVAL_IN_DAYS * HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE;
    //Shortened sync interval for testing
    public static final long SYNC_INTERVAL = 100L;

    // A content resolver and account for accessing the provider
    ContentResolver mResolver;
    Account mAccount;

    public final String LOG_TAG = MainActivity.class.getSimpleName();
    //The starting year of the data in the NYC Open Data library
    public final int STARTING_YEAR_OF_DATA = 2012;
    public final int endingYearOfData = Calendar.getInstance().get(Calendar.YEAR);
    public final int endingMonthOfData = Calendar.getInstance().get(Calendar.MONTH) + 1;
    //Views that need to be accessible outside of onCreate
    ProgressBar mProgressBar;
    TextView mLoadingText;
    //Button mInitialButton;
    //RangeBar mMaterialRangeBar;

    //Shared preferences
    SharedPreferences sharedPreferences;
    //shared preference mListener declaured to avoid garbage collection
    private SharedPreferences.OnSharedPreferenceChangeListener mListener;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.action_settings:
//                //TODO make a settings activity to set the sync frequency and anything else that makes sense
//                Toast.makeText(MainActivity.this, "selected settings!", Toast.LENGTH_SHORT).show();
//                return true;
            //Clear the database from the settings menu
            case R.id.clear_database_settings:
                CycleDbHelper helper = new CycleDbHelper(getBaseContext());
                SQLiteDatabase db = helper.getWritableDatabase();
                db.delete(CycleContract.CycleEntry.TABLE_NAME, null, null);
                Log.d(LOG_TAG, "Clearing Database");

                //close SQL Database
                db.close();
                return true;
            case R.id.update_settings:
                //update the NYC cycle data dababase
                Log.d(LOG_TAG, "Syncing immediately");
                //Toast.makeText(MainActivity.this, "Updating data in background!", Toast.LENGTH_SHORT).show();
                CycleDataSyncAdapter.syncImmediately(getApplicationContext());
                return true;
            case R.id.about_settings:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //dummy account for sync adapter
        mAccount = CreateSyncAccount(this);
        setContentView(R.layout.activity_main);
        //set icon
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.toolbar_space);
        //get UI elements
        //Button refreshButton = (Button) findViewById(R.id.refreshbutton);
        //Button initialDataButton = (Button) findViewById(R.id.initialDataButton);
        //mInitialButton = initialDataButton;
        //Button clearSqlDb = (Button) findViewById(R.id.clearSQL);
        Button mapDatabase = (Button) findViewById(R.id.mapDatabase);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar = progressBar;
        final TextView loadingText = (TextView) findViewById(R.id.loadingTextView);
        final TextView startDatTextView = (TextView) findViewById(R.id.startDateTextView);
        final TextView endDateTextView = (TextView) findViewById(R.id.endDateTextView);
        mLoadingText = loadingText;
        //final TextView yearMappingTextView = (TextView) findViewById(R.id.yearMappingTextView);
        TextView startYearTextView = (TextView) findViewById(R.id.startYearTextView);
        if (startYearTextView != null) {
            startYearTextView.setText(String.valueOf(STARTING_YEAR_OF_DATA));
        }
        TextView endYearTextView = (TextView) findViewById(R.id.endYearTextView);
        if (endYearTextView != null) {
            endYearTextView.setText(String.valueOf(endingYearOfData));
        }
        final RangeBar materialRangeBar = (RangeBar) findViewById(R.id.materialRangeBarWithDates);
        //mMaterialRangeBar = materialRangeBar;
        final CheckedTextView injuredCheckedTextView = (CheckedTextView) findViewById(R.id.injuredCheckedTextView);
        final CheckedTextView killedCheckedTextView = (CheckedTextView) findViewById(R.id.killedCheckedView);

        //update the injured/killed checkedTextViews based on what was previously set in the shared preferences, default to true
        //sharedPreferences = getSharedPreferences(getString(R.string.sharedpreference), Context.MODE_PRIVATE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //If sync is already in progress when on create is called (unlikely but possible to force) show loading views and ensure syncing is happening
        //this will trigger if app was killed while it was loading cycle data
        Boolean showSyncing = sharedPreferences.getBoolean(getString(R.string.syncing), false);
        if (showSyncing) {
            CycleDataSyncAdapter.syncImmediately(getApplicationContext());
            mProgressBar.setVisibility(View.VISIBLE);
            mLoadingText.setVisibility(View.VISIBLE);
        }
        //set shared preference on change mListener
        mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                                  String key) {
                // your stuff here
                Log.d(LOG_TAG, "THERE WAS A CHANGE TO SHARED PREFERENCES: " + key);
                if (key == getString(R.string.syncing)) {
                    //Update the view to show the user whether new data is being loaded in the background or not
                    Boolean showSyncing = sharedPreferences.getBoolean(getString(R.string.syncing), false);
                    if (showSyncing) {
                        mProgressBar.setVisibility(View.VISIBLE);
                        mLoadingText.setVisibility(View.VISIBLE);
                    } else {
                        mProgressBar.setVisibility(View.GONE);
                        mLoadingText.setVisibility(View.GONE);
                    }
                }
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(mListener);
        boolean injured = sharedPreferences.getBoolean(getString(R.string.injuredcyclists), true);
        boolean killed = sharedPreferences.getBoolean(getString(R.string.killedcyclists), true);
        boolean previouslyStarted = sharedPreferences.getBoolean(getString(R.string.pref_previously_started), false);
        if (!previouslyStarted) {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putBoolean(getString(R.string.pref_previously_started), Boolean.TRUE);
            edit.apply();
            firstRun();
        }
        assert injuredCheckedTextView != null;
        injuredCheckedTextView.setChecked(injured);
        assert killedCheckedTextView != null;
        killedCheckedTextView.setChecked(killed);

//        //If intial database has already been loaded, remove the load initial database button from the view
//        boolean showInitialDatabase = sharedPreferences.getBoolean(getString(R.string.showinitialbutton), true);
//        if(!showInitialDatabase){
//            initialDataButton.setVisibility(View.GONE);
//        }
        //set text view to indicate which years and months are going to be mapped by user
        final int startingYear = sharedPreferences.getInt(getString(R.string.mindateyear), STARTING_YEAR_OF_DATA);
        final int endingYear = sharedPreferences.getInt(getString(R.string.maxdateyear), endingYearOfData);
        final int startingMonth = sharedPreferences.getInt(getString(R.string.mindatemonth), 1);
        final int endingMonth = sharedPreferences.getInt(getString(R.string.maxdatemonth), endingMonthOfData);
        final String startingMonthString = new DateFormatSymbols().getMonths()[startingMonth - 1];
        final String endingMonthString = new DateFormatSymbols().getMonths()[endingMonth - 1];
        //final String[] textForYearsToBeMapped = {"" + startingMonthString + " " + startingYear + " - " + endingMonthString + " " + endingYear};
//        assert yearMappingTextView != null;
//        yearMappingTextView.setText(textForYearsToBeMapped[0]);
        startDatTextView.setText("" + startingMonthString + " " + startingYear);
        endDateTextView.setText("" + endingMonthString + " " + endingYear);


//        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt(getString(R.string.mindate), 0);
//        editor.putInt(getString(R.string.maxdate), 2020);
//        editor.commit();

        //final FetchCycleDataTask fetch = new FetchCycleDataTask();
//        final FetchCycleDataTask fetch = new FetchCycleDataTask();
//        fetch.mContext = getBaseContext();
        //materialRangeBar.setPinRadius(30);
        //materialRangeBar.setTickHeight(4);
        final float tickEnd = (STARTING_YEAR_OF_DATA + (endingYearOfData - STARTING_YEAR_OF_DATA) * 12f + endingMonthOfData);
        Log.d(LOG_TAG, "Tick end will equal" + tickEnd);
        assert materialRangeBar != null;
        materialRangeBar.setTickEnd(tickEnd);
        materialRangeBar.setTickStart(STARTING_YEAR_OF_DATA);
        //materialRangeBar.setTickInterval(1);
        materialRangeBar.setBarWeight(8);
        materialRangeBar.setPinRadius(0);
        materialRangeBar.setDrawTicks(false);
        materialRangeBar.setTemporaryPins(false);
        //set range bar values from preferences after checking they are within range
        float leftPinValueFromPref = STARTING_YEAR_OF_DATA + (startingYear - STARTING_YEAR_OF_DATA) * 12f + startingMonth;
        float rightPinValueFromPref = STARTING_YEAR_OF_DATA + (endingYearOfData - STARTING_YEAR_OF_DATA) * 12f + endingMonth;
        if(leftPinValueFromPref>=materialRangeBar.getTickStart() && rightPinValueFromPref <= materialRangeBar.getTickEnd()) {
            materialRangeBar.setRangePinsByValue(leftPinValueFromPref, rightPinValueFromPref);
            materialRangeBar.refreshDrawableState();
        }else{
            Log.d(LOG_TAG, "Preferences were out of the range bars allowable range! Trying to set to " + leftPinValueFromPref + " and "+rightPinValueFromPref + " while the range is between "+ materialRangeBar.getTickStart()+ " and "+ materialRangeBar.getTickEnd());
        }
        //Listen to user settings for the range bar here
        materialRangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue) {

                //update shared preferences for Query when user maps new data
                //SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.sharedpreference), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                //hack to fix error in material range bar where value can be set to greater than its max or min when dragged funny
                int rightPinValueInt = Integer.parseInt(rightPinValue);
                if (rightPinValueInt > tickEnd) {
                    //Log.d(LOG_TAG, "Out of bounds on right pin, rangebar is being set to left pin of " + Float.parseFloat(materialRangeBar.getLeftPinValue()) + " and a right pin of " + (materialRangeBar.getTickEnd()));
                    rightPinValueInt = (int) tickEnd;
                    materialRangeBar.setRangePinsByValue(Float.parseFloat(materialRangeBar.getLeftPinValue()), materialRangeBar.getTickEnd());
                }
                int leftPinValueInt = Integer.parseInt(leftPinValue);
                if (leftPinValueInt < STARTING_YEAR_OF_DATA) {
                    //Log.d(LOG_TAG, "Out of bounds on left pin, rangebar is being set to left pin of " + Float.parseFloat(materialRangeBar.getLeftPinValue()) + " and a right pin of " + (materialRangeBar.getTickEnd()));
                    leftPinValueInt = STARTING_YEAR_OF_DATA;
                    materialRangeBar.setRangePinsByValue(materialRangeBar.getTickStart(), Float.parseFloat(materialRangeBar.getRightPinValue()));
                }

                int startingMonth = (leftPinValueInt - STARTING_YEAR_OF_DATA) % 12 + 1;
                int endingMonth = (rightPinValueInt - STARTING_YEAR_OF_DATA) % 12 + 1;
                int startingYear = (leftPinValueInt - STARTING_YEAR_OF_DATA) / 12 + STARTING_YEAR_OF_DATA;
                int endingYear = (rightPinValueInt - STARTING_YEAR_OF_DATA) / 12 + STARTING_YEAR_OF_DATA;
                //Log.d("RANGEBAR", "starting month and ending month are: " + startingMonth +"and" +endingMonth);
                //Log.d("RANGEBAR", "starting year and ending year are: " + startingYear +"and" +endingYear);
                editor.putInt(getString(R.string.mindateyear), startingYear);
                editor.putInt(getString(R.string.maxdateyear), endingYear);
                editor.putInt(getString(R.string.mindatemonth), startingMonth);
                editor.putInt(getString(R.string.maxdatemonth), endingMonth);
                //Log.d("RANGEBAR", "Range bar is now set to look between " + leftPinValue + " and " + rightPinValue);
                String startingMonthString = new DateFormatSymbols().getMonths()[startingMonth - 1];
                String endingMonthString = new DateFormatSymbols().getMonths()[endingMonth - 1];
                //textForYearsToBeMapped[0] = "Mapping data between " + startingMonth + "-" + startingYear + " and " + endingMonth + "-" + endingYear;
                //textForYearsToBeMapped[0] = "" + startingMonthString + " " + startingYear + " - " + endingMonthString + " " + endingYear;


                //yearMappingTextView.setText(textForYearsToBeMapped[0]);
                startDatTextView.setText("" + startingMonthString + " " + startingYear);
                endDateTextView.setText("" + endingMonthString + " " + endingYear);
                //editor.commit();
                //handle preference update in the background so it is less obtrusive to UI thread
                editor.apply();
            }
        });

        injuredCheckedTextView.setOnClickListener(new CheckedTextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                //update check box and shared preferences for Query when user maps new data
                injuredCheckedTextView.setChecked(!injuredCheckedTextView.isChecked());
                //SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.sharedpreference), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.injuredcyclists), injuredCheckedTextView.isChecked());
                editor.apply();
            }
        });
        killedCheckedTextView.setOnClickListener(new CheckedTextView.OnClickListener() {
            @Override
            public void onClick(View v) {
                //update check box and shared preferences for Query when user maps new data
                killedCheckedTextView.setChecked(!killedCheckedTextView.isChecked());
                //SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.sharedpreference), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.killedcyclists), killedCheckedTextView.isChecked());
                editor.apply();
            }
        });

//        //TODO move this update database idea to a floating settings menu or action bar menu button or something similar
//        //TODO make this be some kind of default action when application is started for the first time or the SQL database is empty
//        assert refreshButton != null;
//        refreshButton.setOnClickListener(new View.OnClickListener() {
//            //TODO need to check the existing SQL database for the latest date currently stored and then when the database URL is built, add this date constraint
//            //Goal is to allow users to only have the large update once, and any updates conducted later just pull new data that has been added to NYC Open maps
//            @Override
//            public void onClick(View v) {
//                fetchUpdatedCycleData();
//            }
//        });

//        assert initialDataButton != null;
//        initialDataButton.setOnClickListener(new View.OnClickListener() {
//            //TODO need to check the existing SQL database for the latest date currently stored and then when the database URL is built, add this date constraint
//            //Goal is to allow users to only have the large update once, and any updates conducted later just pull new data that has been added to NYC Open maps
//
//
//            @Override
//            public void onClick(View v) {
////                CycleDbHelper helper = new CycleDbHelper(getBaseContext());
////                SQLiteDatabase db = helper.getWritableDatabase();
////                //TODO will want to modify this to no longer delete DB
////                //db.delete(CycleContract.CycleEntry.TABLE_NAME, null, null);
////                String lastDateInDB = "2000-01-01T00:00:00";
//
//
//                //fetch all the data from starting year to the current year
//                //Log.d("FETCH", "Fetching data between "+STARTING_YEAR_OF_DATA + " and " +endingYearOfData);
//                //TODO need to change this to only pull from the years that are not currently in the DB - USE UNIQUE NUMBERS TO SORT INSETAD
//                for (int i = STARTING_YEAR_OF_DATA; i <= endingYearOfData; i++) {
//                    fetchInitialCycleData(i);
//                }
//
//                //Log.d(LOG_TAG, "Last date in the database is: "+lastDateInDB);
//                //fetchUpdatedCycleData(STARTING_YEAR_OF_DATA, lastDateInDB);
//
//            }
//        });


//        assert clearSqlDb != null;
//        clearSqlDb.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                CycleDbHelper helper = new CycleDbHelper(getBaseContext());
//                SQLiteDatabase db = helper.getWritableDatabase();
//                db.delete(CycleContract.CycleEntry.TABLE_NAME, null, null);
//                Log.d(LOG_TAG, "Clearing Database");
//
//                //close SQL Database
//                db.close();
//            }
//        });

        assert mapDatabase != null;
        mapDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show user that map is loading while datapoints are being populated on the UI thread (can be time consuming for larger sets)
                Toast.makeText(MainActivity.this, "Loading map....", Toast.LENGTH_SHORT).show();
//                Toast t = Toast.makeText(MainActivity.this, "Loading map...", Toast.LENGTH_SHORT);
//                t.setGravity(Gravity.FILL_HORIZONTAL, t.getXOffset(), t.getYOffset());
//                t.show();
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Run this method only the first time the app is opened (detected by shared preferences)
     * This method syncs the database for the first time and sets the sync frequency for the sync adapter to update the database
     */
    private void firstRun() {
        Log.d(LOG_TAG, "First run detecting, setting up sync and sync parameters");
        //Toast.makeText(MainActivity.this, "Syncing data in background", Toast.LENGTH_LONG).show();
        CycleDataSyncAdapter.syncImmediately(getApplicationContext());
        //Set up automated syncing by allowing it and set the sync frequency here
        ContentResolver.setSyncAutomatically(CycleDataSyncAdapter.getSyncAccount(getApplicationContext()), getApplicationContext().getString(R.string.content_authority), true);
        CycleDataSyncAdapter.setSyncFrequency(getApplicationContext());

    }

    //TODO should actually sort by Unique Number here so that even older queries are properly pulled
//    private void fetchUpdatedCycleData() {
//        //get Last unique number in current SQL database
//        CycleDbHelper helper = new CycleDbHelper(getBaseContext());
//        SQLiteDatabase db = helper.getWritableDatabase();
//        int lastUniqueNumberInDB = 0;
//
//        //get most recent unique number currently stored in the database, only column necessary is date
//        String[] columns = {CycleContract.CycleEntry.COLUMN_UNIQUE_KEY};
//        Cursor cursor = db.query(CycleContract.CycleEntry.TABLE_NAME, columns, null, null, null, null, CycleContract.CycleEntry.COLUMN_UNIQUE_KEY + " DESC", String.valueOf(1));
//        if (cursor.moveToFirst()) {
//            lastUniqueNumberInDB = cursor.getInt(cursor.getColumnIndex(CycleContract.CycleEntry.COLUMN_UNIQUE_KEY));
//        }
//
//        Log.d(LOG_TAG, "Last unique key number in the database is: " + lastUniqueNumberInDB);
//        //Close SQL database
//        db.close();
//
//        final FetchCycleDataTask fetch = new FetchCycleDataTask();
//        //TODO THIS IS PROBABLY A HORRIBLE WAY TO MESS WITH THE UI FROM ASYNC TASK! Look in to this
//        //Pass UI effecting variable to the Asyc task
//        fetch.mContext = getBaseContext();
//        fetch.mProgressBar = mProgressBar;
//        fetch.mTextView = mLoadingText;
//        try {
//            //URL for both injured and killed cyclists
//            //fetch.mUrlCycleData = new URL("http://data.cityofnewyork.us/resource/qiz3-axqb.json?$where=(number_of_cyclist_killed%20%3E%200%20or%20number_of_cyclist_injured%20%3E%200)%20and%20latitude%20%3E%200%20and%20date%20between%20%27"+year+"-01-01T10:00:00%27%20and%20%27"+(year+1)+"-01-01T10:00:00%27");
//            //fetch.mUrlCycleData = new URL("http://data.cityofnewyork.us/resource/qiz3-axqb.json?$where=(number_of_cyclist_killed%20%3E%200%20or%20number_of_cyclist_injured%20%3E%200)%20and%20latitude%20%3E%200%20and%20date%20between%20%27"+lastUniqueNumberInDB+"%27%20and%20%27"+(endingYearOfData+1)+"-01-01T10:00:00%27");
//            fetch.mUrlCycleData = new URL("http://data.cityofnewyork.us/resource/qiz3-axqb.json?$where=(number_of_cyclist_killed%20%3E%200%20or%20number_of_cyclist_injured%20%3E%200)%20and%20latitude%20%3E%200%20and%20unique_key%20>%20" + lastUniqueNumberInDB + "&$order=unique_key%20ASC");
//            Log.d(LOG_TAG, "The URL being used now is: " + fetch.mUrlCycleData);
//            //URL for only killed cyclists (useful for testing as it is much much faster)
//            //fetch.mUrlCycleData = new URL("http://data.cityofnewyork.us/resource/qiz3-axqb.json?$where=number_of_cyclist_killed%20%3E%200%20and%20latitude%20%3E%200%20and%20date%20between%20%27"+year+"-01-01T10:00:00%27%20and%20%27"+(year+1)+"-01-01T10:00:00%27");
//
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//
//        fetch.execute();
//        Log.d("FETCH", "Fetching cycle data with unique key greater than: " + lastUniqueNumberInDB);
//        assert mProgressBar != null;
//
//        //TODO Use shared preferences to determine to show or not show the loading objects (is this possible the way I want?)
//        //SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.sharedpreference), Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean(getString(R.string.showloading), true);
//        editor.commit();
//        mProgressBar.setVisibility(View.VISIBLE);
//        mLoadingText.setVisibility(View.VISIBLE);
//
//
//    }


    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
        }
        return newAccount;
    }
}
