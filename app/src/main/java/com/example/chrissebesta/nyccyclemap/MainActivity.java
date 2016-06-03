package com.example.chrissebesta.nyccyclemap;

import android.accounts.Account;
import android.accounts.AccountManager;
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
    public final String LOG_TAG = MainActivity.class.getSimpleName();
    // Constants
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.example.android.datasync.provider";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "nyccyclemap.example.com";
    // The account name
    public static final String ACCOUNT = "dummyaccount";

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
        Account account = CreateSyncAccount(this);
        setContentView(R.layout.activity_main);
        //set icon
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.toolbar_space);

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
            firstRun();
        }

        //Set up check boxes for injured and killed cyclists
        final CheckedTextView injuredCheckedTextView = (CheckedTextView) findViewById(R.id.injuredCheckedTextView);
        assert injuredCheckedTextView != null;
        injuredCheckedTextView.setChecked(injured);
        final CheckedTextView killedCheckedTextView = (CheckedTextView) findViewById(R.id.killedCheckedView);
        assert killedCheckedTextView != null;
        killedCheckedTextView.setChecked(killed);


        //set text view to indicate which years and months are going to be mapped by user
        final int startingYear = sharedPreferences.getInt(getString(R.string.mindateyear), STARTING_YEAR_OF_DATA);
        final int endingYear = sharedPreferences.getInt(getString(R.string.maxdateyear), endingYearOfData);
        final int startingMonth = sharedPreferences.getInt(getString(R.string.mindatemonth), 1);
        final int endingMonth = sharedPreferences.getInt(getString(R.string.maxdatemonth), endingMonthOfData);
        final String startingMonthString = new DateFormatSymbols().getMonths()[startingMonth - 1];
        final String endingMonthString = new DateFormatSymbols().getMonths()[endingMonth - 1];

        startDatTextView.setText("" + startingMonthString + " " + startingYear);
        endDateTextView.setText("" + endingMonthString + " " + endingYear);

        //Set up range bar
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
        materialRangeBar.setRangePinsByValue(calculateStartPosition(startingYear, startingMonth), calculateEndPosition(endingYear, endingMonth));


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

        assert mapDatabase != null;
        mapDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show user that map is loading while datapoints are being populated on the UI thread (can be time consuming for larger sets)
                Toast.makeText(MainActivity.this, "Loading map....", Toast.LENGTH_SHORT).show();
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
        //Fetch data using the AsyncTask since the SyncAdapter is taking to long to start on the first run.
        FetchCycleDataTask fetch = new FetchCycleDataTask(getApplicationContext());
        fetch.execute();
    }


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

    /**
     * Method to calculate the start position for the range bar
     * @param minDateYear
     * @param minDateMonth
     * @return
     */
    private float calculateStartPosition(int minDateYear, int minDateMonth) {
        float startPosition;
        startPosition = (float) (STARTING_YEAR_OF_DATA + (minDateYear - STARTING_YEAR_OF_DATA) * 12 + minDateMonth-1);
        return startPosition;

    }

    /**
     * Method to calculate the end position for the range bar
     * @param maxDateYear
     * @param maxDateMonth
     * @return
     */
    private float calculateEndPosition(int maxDateYear, int maxDateMonth) {
        float endPosition;
        endPosition = (float) (STARTING_YEAR_OF_DATA + (maxDateYear - STARTING_YEAR_OF_DATA) * 12 + maxDateMonth-1);
        return endPosition;
    }
}
