package com.example.chrissebesta.nyccyclemap.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.example.chrissebesta.nyccyclemap.R;

import java.util.ArrayList;
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
        //TODO adjust SQL request here to search only the events that the user has opted for
        //Cursor cursor = db.rawQuery("SELECT * FROM " + CycleContract.CycleEntry.TABLE_NAME, null);
        //40.7096637,-73.9662333
        int startYear = 2015;


        //get min and max date from shared Preferences
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(mContext.getString(R.string.sharedpreference), Context.MODE_PRIVATE);
        int minDate = sharedPreferences.getInt(mContext.getString(R.string.mindate), 0); //default min year is 0
        int maxDate = sharedPreferences.getInt(mContext.getString(R.string.maxdate), 3000);//default max year is 3000
        boolean injured = sharedPreferences.getBoolean(mContext.getString(R.string.injuredcyclists), true);
        boolean killed = sharedPreferences.getBoolean(mContext.getString(R.string.killedcyclists), true);
        Log.d(LOG_TAG, "In Item Reader Shared preference are showing dates between: " + minDate + " and " + maxDate + " and injured is " + injured + " while killed is " + killed);
        String injuredArgs;
        String killedArgs;
        //increment max date by one year and look for dates that are lower than it (effectively, looks at everything less than year 2013 if max date is 2012, catches all dates in 2012
        maxDate++;
        //set args to properly include or exclude injured or killed cyclists
        if (injured) {
            injuredArgs = "0";
        } else injuredArgs = "100";
        if (killed) {
            killedArgs = "0";
        } else killedArgs = "100";

        //set args for SQL Query
        String[] args = new String[]{String.valueOf(minDate), String.valueOf(maxDate), injuredArgs, killedArgs};
        Log.d(LOG_TAG, "InjuredArgs is: " + injuredArgs + " and killedArgs is: " + killedArgs);


        Cursor cursor = db.query(CycleContract.CycleEntry.TABLE_NAME, null, CycleContract.CycleEntry.COLUMN_DATE+">=? AND "+ CycleContract.CycleEntry.COLUMN_DATE+" <? AND (" + CycleContract.CycleEntry.COLUMN_NUMBER_OF_CYCLIST_INJURED + ">? OR " + CycleContract.CycleEntry.COLUMN_NUMBER_OF_CYCLIST_KILLED + ">?)", args, null, null, CycleContract.CycleEntry.COLUMN_DATE + " DESC", null);
        Log.d(LOG_TAG, "The query statement is: " + CycleContract.CycleEntry.COLUMN_DATE+">=? AND "+CycleContract.CycleEntry.COLUMN_DATE+"<? AND (" + CycleContract.CycleEntry.COLUMN_NUMBER_OF_CYCLIST_INJURED + ">? OR " + CycleContract.CycleEntry.COLUMN_NUMBER_OF_CYCLIST_KILLED + ">?)");
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
                        killedBoolean));
            } while (cursor.moveToNext());

            //if using cluster manager add :
            //mClusterManager.cluster();
        }else{
            Toast.makeText(mContext, "No items to show on map!", Toast.LENGTH_LONG).show();
        }
//        if (cursor.moveToFirst()){
//            String dateString = cursor.getString(cursor.getColumnIndex(CycleContract.CycleEntry.COLUMN_DATE));
//            Log.d(LOG_TAG, "The raw date string is: " + dateString);
//            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
//            try {
//                Date date = fmt.parse(dateString);
//                Log.d(LOG_TAG, "The date's date is : " + date);
//                Log.d(LOG_TAG, "The time is: " + date.getTime());
//                Calendar cal = Calendar.getInstance();
//                cal.setTime(date);
//                Log.d(LOG_TAG, "The year is: " + cal.get(Calendar.YEAR));
//                //return fmt.format(date);
//            } catch (ParseException pe) {
//
//                // return "Date";
//            }
//        }
        cursor.close();

        return items;
    }
}
