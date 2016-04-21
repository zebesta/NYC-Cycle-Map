package com.example.chrissebesta.nyccyclemap.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by chrissebesta on 3/30/16.
 */
public class CycleContract {
    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.example.android.travology.app";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sunshine.app/weather/ is a valid path for
    // looking at weather data. content://com.example.android.sunshine.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_GEO = "geo";


    public CycleContract(){}

    public static final class GeoEntry implements BaseColumns {

        public static final String TABLE_NAME = "geo";
        public static final String COLUMN_PLACE_CODE = "place_code";
        public static final String COLUMN_ADDRESS = "place_address";
        public static final String COLUMN_CITY_NAME = "city_name";
        public static final String COLUMN_COUNTRY = "country_code";
        public static final String COLUMN_COORD_LAT = "latitude_coordinate";
        public static final String COLUMN_COORD_LONG = "longitude_coordinate";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_GEO).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GEO;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GEO;

        public static Uri buildGeoUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
}
