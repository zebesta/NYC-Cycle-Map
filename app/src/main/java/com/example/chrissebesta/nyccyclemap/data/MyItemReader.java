package com.example.chrissebesta.nyccyclemap.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        String[] args = new String[]{"-73.9662333"};
        Cursor cursor = db.query(CycleContract.CycleEntry.TABLE_NAME, null, "longitude>=?", args, null, null, CycleContract.CycleEntry.COLUMN_DATE + " ASC", null);
        if (cursor.moveToFirst()) {
            do {
                //Get the LatLng of the next item to be added
                items.add(new MyItem(cursor.getDouble(cursor.getColumnIndex(CycleContract.CycleEntry.COLUMN_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(CycleContract.CycleEntry.COLUMN_LONGITUDE))));
            } while (cursor.moveToNext());

            //if using cluster manager add :
            //mClusterManager.cluster();
        }
        cursor.moveToFirst();
        String dateString = cursor.getString(cursor.getColumnIndex(CycleContract.CycleEntry.COLUMN_DATE));
        Log.d(LOG_TAG, "The raw date string is: " + dateString);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
        try {
            Date date = fmt.parse(dateString);
            Log.d(LOG_TAG, "The date's date is : "+ date);
            Log.d(LOG_TAG, "The time is: "+date.getTime());
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            Log.d(LOG_TAG, "The year is: " + cal.get(Calendar.YEAR));
            //return fmt.format(date);
        }
        catch(ParseException pe) {

           // return "Date";
        }
        cursor.close();

        return items;
    }
}
