package com.example.chrissebesta.nyccyclemap.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by chrissebesta on 3/30/16.
 */
public class CycleDbHelper extends SQLiteOpenHelper {
    public final String LOG_TAG = this.getClass().getSimpleName();

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "geo.db";

    public CycleDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_CYCLE_TABLE = "CREATE TABLE " + CycleContract.CycleEntry.TABLE_NAME + " (" +
                CycleContract.CycleEntry._ID + " INTEGER PRIMARY KEY," +
                CycleContract.CycleEntry.COLUMN_DATE + " REAL NOT NULL, "+
                CycleContract.CycleEntry.COLUMN_TIME + " TEXT NOT NULL, "+
                CycleContract.CycleEntry.COLUMN_BOROUGH + " TEXT NOT NULL, " +
                CycleContract.CycleEntry.COLUMN_ZIP_CODE + " TEXT NOT NULL, " +
                CycleContract.CycleEntry.COLUMN_LATITUDE + " REAL NOT NULL, " +
                CycleContract.CycleEntry.COLUMN_LONGITUDE + " REAL NOT NULL, " +
                CycleContract.CycleEntry.COLUMN_UNIQUE_KEY + " REAL NOT NULL, " +
                CycleContract.CycleEntry.COLUMN_CONTRIBUTING_FACTOR_VEHICLE_1 + " TEXT NOT NULL, " +
                CycleContract.CycleEntry.COLUMN_NUMBER_OF_CYCLIST_INJURED + " REAL NOT NULL, " +
                CycleContract.CycleEntry.COLUMN_NUMBER_OF_CYCLIST_KILLED + " REAL NOT NULL, " +
                CycleContract.CycleEntry.COLUMN_LATITUDE + " REAL NOT NULL, " +
                CycleContract.CycleEntry.COLUMN_LONGITUDE + " REAL NOT NULL"
                +");";

        //Strings provided by API for JSON parsing
//        public static final String TABLE_NAME = "cycle";
//        public static final String COLUMN_DATE = "date"; //floating time stamp
//        public static final String COLUMN_TIME = "time"; //text
//        public static final String COLUMN_BOROUGH = "borough";
//        public static final String COLUMN_ZIP_CODE = "zip_code";
//        public static final String COLUMN_LATITUDE = "latitude"; //number
//        public static final String COLUMN_LONGITUDE = "longitude";
//        public static final String COLUMN_POINT = "point"; //point
//        public static final String COLUMN_ON_STREET_NAME = "on_street_name"; //text
//        public static final String COLUMN_OFF_STREET_NAME = "off_street_name";
//        public static final String COLUMN_CROSS_STREET_NAME = "cross_street_name";
//        public static final String COLUMN_NUMBER_OF_PERSONS_INJURED = "number_of_persons_injured"; //number
//        public static final String COLUMN_NUMBER_OF_PERSONS_KILLED = "number_of_persons_killed";
//        public static final String COLUMN_NUMBER_OF_PEDESTRIANS_INJURED = "number_of_pedestrians_injured";
//        public static final String COLUMN_NUMBER_OF_PEDESTRIANS_KILLED = "number_of_pedestrians_killed";
//        public static final String COLUMN_NUMBER_OF_CYCLIST_INJURED = "number_of_cyclist_injured";
//        public static final String COLUMN_NUMBER_OF_CYCLIST_KILLED = "number_of_cyclist_killed";
//        public static final String COLUMN_NUMBER_OF_MOTORIST_INJURED = "number_of_motorist_injured";
//        public static final String COLUMN_NUMBER_OF_MOTORIST_KILLED = "number_of_motorist_killed";
//        public static final String COLUMN_CONTRIBUTING_FACTOR_VEHICLE_1 = "contributing_factor_vehicle_1"; //text
//        public static final String COLUMN_CONTRIBUTING_FACTOR_VEHICLE_2 = "contributing_factor_vehicle_2";
//        public static final String COLUMN_CONTRIBUTING_FACTOR_VEHICLE_3 = "contributing_factor_vehicle_3";
//        public static final String COLUMN_CONTRIBUTING_FACTOR_VEHICLE_4 = "contributing_factor_vehicle_4";
//        public static final String COLUMN_CONTRIBUTING_FACTOR_VEHICLE_5 = "contributing_factor_vehicle_5";
//        public static final String COLUMN_UNIQUE_KEY = "unique_key"; //number
//        public static final String COLUMN_VEHICLE_TYPE_CODE_1 = "vehicle_type_code1"; //text
//        public static final String COLUMN_VEHICLE_TYPE_CODE_2 = "vehicle_type_code2";
//        public static final String COLUMN_VEHICLE_TYPE_CODE_3 = "vehicle_type_code3";
//        public static final String COLUMN_VEHICLE_TYPE_CODE_4 = "vehicle_type_code4";
//        public static final String COLUMN_VEHICLE_TYPE_CODE_5 = "vehicle_type_code5";
//        public static final String COLUMN_LOCATION_ZIP = "location_zip";
//        public static final String COLUMN_LOCATION_CITY = "location_city";
//        public static final String COLUMN_LOCATION_ADDRESS = "location_address";
//        public static final String COLUMN_LOCATION_STATE = "location_state";


//                CycleContract.CycleEntry.COLUMN_PLACE_CODE + " TEXT NOT NULL, " +
//                CycleContract.CycleEntry.COLUMN_ADDRESS + " TEXT NOT NULL, " +
//                CycleContract.CycleEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, " +
//                CycleContract.CycleEntry.COLUMN_COUNTRY + " TEXT NOT NULL, " +
//                CycleContract.CycleEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " +
//                CycleContract.CycleEntry.COLUMN_COORD_LONG + " REAL NOT NULL"



        sqLiteDatabase.execSQL(SQL_CREATE_CYCLE_TABLE);
        Log.d("SQL STRING IS: ", SQL_CREATE_CYCLE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CycleContract.CycleEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    /**
     * Helper function that parses a given table into a string
     * and returns it for easy printing. The string consists of
     * the table name and then each row is iterated through with
     * column_name: value pairs printed out.
     *
     * @param db        the database to get the table from
     * @param tableName the the name of the table to parse
     * @return the table tableName as a string
     */
    public String getTableAsString(SQLiteDatabase db, String tableName) {
        Log.d(LOG_TAG, "getTableAsString called");
        String tableString = String.format("Table %s:\n", tableName);
        Cursor allRows = db.rawQuery("SELECT * FROM " + tableName, null);
        if (allRows.moveToFirst()) {
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name : columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
        }

        return tableString;
    }
}
