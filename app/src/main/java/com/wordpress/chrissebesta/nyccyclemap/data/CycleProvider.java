package com.wordpress.chrissebesta.nyccyclemap.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by chrissebesta on 3/30/16.
 * Content Provider for the Cycle database
 */
public class CycleProvider extends ContentProvider {

    private CycleDbHelper mOpenHelper;

    //Need to set tables for SQL Query Builder (?)
    private static final SQLiteQueryBuilder sCycleQueryBuilder = new SQLiteQueryBuilder();



    @Override
    public boolean onCreate() {
        mOpenHelper = new CycleDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        retCursor = mOpenHelper.getReadableDatabase().query(
                CycleContract.CycleEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
