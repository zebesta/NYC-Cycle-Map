package com.example.chrissebesta.nyccyclemap.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chrissebesta.nyccyclemap.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by chrissebesta on 4/26/16.
 * Reads all the Item from the SQL database so that they can be properly handled by the Clustering capability in google maps
 */
public class MyItemReader {
    private static final String LOG_TAG = MyItemReader.class.getSimpleName();
    Context mContext;

    public MyItemReader(Context context) {
        mContext = context;
    }

    public List<MyItem> read() {
        List<MyItem> items = new ArrayList<MyItem>();
//        String json = new Scanner(inputStream).useDelimiter(REGEX_INPUT_BOUNDARY_BEGINNING).next();
//        JSONArray array = new JSONArray(json);
//        for (int i = 0; i < array.length(); i++) {
//            JSONObject object = array.getJSONObject(i);
//            double lat = object.getDouble("lat");
//            double lng = object.getDouble("lng");
//            items.add(new MyItem(lat, lng));
//        }
        CycleDbHelper helper = new CycleDbHelper(mContext);
        final SQLiteDatabase db = helper.getWritableDatabase();
        //Cursor cursor = db.rawQuery("SELECT * FROM " + CycleContract.CycleEntry.TABLE_NAME, null);
        //40.7096637,-73.9662333
        int startYear = 2015;


        //get min and max date from shared Preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        int minDateYear = sharedPreferences.getInt(mContext.getString(R.string.mindateyear), 0); //default min year is 0
        int maxDateYear = sharedPreferences.getInt(mContext.getString(R.string.maxdateyear), 3000);//default max year is 3000
        int minDateMonth = sharedPreferences.getInt(mContext.getString(R.string.mindatemonth), 0); //default min year is 0
        int maxDateMonth = sharedPreferences.getInt(mContext.getString(R.string.maxdatemonth), 12);
        //first day in selected start month
        GregorianCalendar startDate = new GregorianCalendar(minDateYear, minDateMonth - 1, 1);
        //first day in month after selected end month
        GregorianCalendar endDate = new GregorianCalendar(maxDateYear, maxDateMonth, 1);
        //Get comparably formated date so that the database can be queried with the right strings
        String startDateString = new SimpleDateFormat("yyyy-MM-dd").format(startDate.getTime());
        String endDateString = new SimpleDateFormat("yyyy-MM-dd").format(endDate.getTime());
        Log.d(LOG_TAG, "starting date is: " + startDate.getTime() + " Formated as: " + startDateString);
        Log.d(LOG_TAG, "ending date is: " + endDate.getTime() + " Formated as: " + endDateString);
        boolean injured = sharedPreferences.getBoolean(mContext.getString(R.string.injuredcyclists), true);
        boolean killed = sharedPreferences.getBoolean(mContext.getString(R.string.killedcyclists), true);
        Log.d(LOG_TAG, "In Item Reader Shared preference are showing dates between: " + minDateYear + " and " + maxDateYear + " and injured is " + injured + " while killed is " + killed);
        String injuredArgs;
        String killedArgs;
        //increment max date by one year and look for dates that are lower than it (effectively, looks at everything less than year 2013 if max date is 2012, catches all dates in 2012
        maxDateYear++;
        //set args to properly include or exclude injured or killed cyclists
        if (injured) {
            injuredArgs = "0";
        } else injuredArgs = "100";
        if (killed) {
            killedArgs = "0";
        } else killedArgs = "100";

        //set args for SQL Query
        //String[] args = new String[]{String.valueOf(minDateYear), String.valueOf(maxDateYear), injuredArgs, killedArgs};

        //New month included date query arguments:
        String[] args = new String[]{startDateString, endDateString, injuredArgs, killedArgs};
        Log.d(LOG_TAG, "InjuredArgs is: " + injuredArgs + " and killedArgs is: " + killedArgs);


        Cursor cursor = db.query(CycleContract.CycleEntry.TABLE_NAME, null, CycleContract.CycleEntry.COLUMN_DATE + ">=? AND " + CycleContract.CycleEntry.COLUMN_DATE + " <? AND (" + CycleContract.CycleEntry.COLUMN_NUMBER_OF_CYCLIST_INJURED + ">? OR " + CycleContract.CycleEntry.COLUMN_NUMBER_OF_CYCLIST_KILLED + ">?)", args, null, null, CycleContract.CycleEntry.COLUMN_DATE + " DESC", null);
        if (cursor.getCount() > 0) {
            Toast.makeText(mContext, "Mapping " + cursor.getCount() + " data points!", Toast.LENGTH_SHORT).show();
        } else {
            //No items to show, test if database is completely empty
            Cursor totalDbCursor = db.query(CycleContract.CycleEntry.TABLE_NAME, null, null, null, null, null, null, null);
            if (totalDbCursor.getCount() > 0) {
                Toast toast = Toast.makeText(mContext, "No items to show on map\nUpdate map settings!", Toast.LENGTH_LONG);
                TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                if (v != null) v.setGravity(Gravity.CENTER);
                toast.show();
            } else {
                Toast toast = Toast.makeText(mContext, "Database is empty!\nUpdate data from the settings menu!", Toast.LENGTH_LONG);
                TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
                if (v != null) v.setGravity(Gravity.CENTER);
                toast.show();
            }
            totalDbCursor.close();

        }
//        Toast t = Toast.makeText(mContext, "Mapping "+cursor.getCount() + " data points!", Toast.LENGTH_SHORT);
//        t.setGravity(Gravity.FILL_HORIZONTAL, t.getXOffset(), t.getYOffset());
//        t.show();
        Log.d(LOG_TAG, "The query statement is: " + CycleContract.CycleEntry.COLUMN_DATE + ">=? AND " + CycleContract.CycleEntry.COLUMN_DATE + "<? AND (" + CycleContract.CycleEntry.COLUMN_NUMBER_OF_CYCLIST_INJURED + ">? OR " + CycleContract.CycleEntry.COLUMN_NUMBER_OF_CYCLIST_KILLED + ">?)");
        if (cursor.moveToFirst()) {
            do {
                //Get the LatLng of the next item to be added
                boolean killedBoolean;
                int killedInt = cursor.getInt(cursor.getColumnIndex(CycleContract.CycleEntry.COLUMN_NUMBER_OF_CYCLIST_KILLED));
                if (killedInt > 0) {
                    killedBoolean = true;
                } else killedBoolean = false;
                items.add(new MyItem(cursor.getDouble(cursor.getColumnIndex(CycleContract.CycleEntry.COLUMN_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(CycleContract.CycleEntry.COLUMN_LONGITUDE)),
                        cursor.getString(cursor.getColumnIndex(CycleContract.CycleEntry.COLUMN_DATE)),
                        killedBoolean,
                        cursor.getInt(cursor.getColumnIndex(CycleContract.CycleEntry.COLUMN_UNIQUE_KEY))));
            } while (cursor.moveToNext());

            //if using cluster manager add :
            //mClusterManager.cluster();
        }

        //close cursor and SQL database
        cursor.close();
        db.close();

        return items;
    }
}
