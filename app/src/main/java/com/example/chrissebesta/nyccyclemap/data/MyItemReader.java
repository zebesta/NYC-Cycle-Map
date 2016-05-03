package com.example.chrissebesta.nyccyclemap.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chrissebesta on 4/26/16.
 * Reads all the Item from the SQL database so that they can be properly handled by the Clustering capability in google maps
 */
public class MyItemReader {
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
        Cursor cursor = db.query(CycleContract.CycleEntry.TABLE_NAME, null, "longitude>=?", args, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                //Get the LatLng of the next item to be added
                items.add(new MyItem(cursor.getDouble(cursor.getColumnIndex(CycleContract.CycleEntry.COLUMN_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(CycleContract.CycleEntry.COLUMN_LONGITUDE))));
            } while (cursor.moveToNext());

            //if using cluster manager add :
            //mClusterManager.cluster();
        }
        cursor.close();

        return items;
    }
}
