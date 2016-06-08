package com.wordpress.chrissebesta.nyccyclemap.data;

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
    static final String DATABASE_NAME = "cycle.db";

    public CycleDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_CYCLE_TABLE = "CREATE TABLE " + CycleContract.CycleEntry.TABLE_NAME + " (" +
                CycleContract.CycleEntry._ID + " INTEGER PRIMARY KEY," +
                CycleContract.CycleEntry.COLUMN_DATE + " TEXT NOT NULL, " +
                CycleContract.CycleEntry.COLUMN_NUMBER_OF_CYCLIST_INJURED + " REAL NOT NULL, " +
                CycleContract.CycleEntry.COLUMN_NUMBER_OF_CYCLIST_KILLED + " REAL NOT NULL, " +
                //CycleContract.CycleEntry.COLUMN_TIME + " TEXT NOT NULL, "+
                //CycleContract.CycleEntry.COLUMN_BOROUGH + " TEXT NOT NULL, " +
                //CycleContract.CycleEntry.COLUMN_ZIP_CODE + " TEXT NOT NULL, " +
                CycleContract.CycleEntry.COLUMN_LATITUDE + " REAL NOT NULL, " +
                CycleContract.CycleEntry.COLUMN_LONGITUDE + " REAL NOT NULL, " +
                //UNIQUE Key needs to be unique so that the same value is not added more than once.
                CycleContract.CycleEntry.COLUMN_UNIQUE_KEY + " REAL NOT NULL UNIQUE" + ");";

//              CycleContract.CycleEntry.COLUMN_LONGITUDE + " REAL NOT NULL, " +
//              CycleContract.CycleEntry.COLUMN_UNIQUE_KEY + " REAL NOT NULL"+");"; //, " +

        //CycleContract.CycleEntry.COLUMN_CONTRIBUTING_FACTOR_VEHICLE_1 + " TEXT NOT NULL, " +
        //CycleContract.CycleEntry.COLUMN_NUMBER_OF_CYCLIST_INJURED + " REAL NOT NULL, " +
        //CycleContract.CycleEntry.COLUMN_NUMBER_OF_CYCLIST_KILLED + " REAL NOT NULL"
        //+");";

        sqLiteDatabase.execSQL(SQL_CREATE_CYCLE_TABLE);
//        Log.d("SQL STRING IS: ", SQL_CREATE_CYCLE_TABLE);

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
//        Log.d(LOG_TAG, "getTableAsString called");
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

    /**
     * Returned values contained within the cursor and a human readable string
     * @param cursor
     * @return
     */
    public String cursorToString(Cursor cursor) {
        String cursorString = "";
        if (cursor.moveToFirst()) {
            String[] columnNames = cursor.getColumnNames();
            for (String name : columnNames)
                cursorString += String.format("%s ][ ", name);
            cursorString += "\n";
            do {
                for (String name : columnNames) {
                    cursorString += String.format("%s ][ ",
                            cursor.getString(cursor.getColumnIndex(name)));
                }
                cursorString += "\n";
            } while (cursor.moveToNext());
        }
        return cursorString;
    }
}
