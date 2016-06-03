package com.example.chrissebesta.nyccyclemap.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by chrissebesta on 3/30/16.
 * Contract to define the cycle incident data contained within the SQL Database
 *
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
    public static final String PATH_CYCLE = "cycle";


    public CycleContract() {
    }

    public static final class CycleEntry implements BaseColumns {

//        public static final String TABLE_NAME = "geo";
//        public static final String COLUMN_PLACE_CODE = "place_code";
//        public static final String COLUMN_ADDRESS = "place_address";
//        public static final String COLUMN_CITY_NAME = "city_name";
//        public static final String COLUMN_COUNTRY = "country_code";
//        public static final String COLUMN_COORD_LAT = "latitude_coordinate";
//        public static final String COLUMN_COORD_LONG = "longitude_coordinate";

        //Strings provided by API for JSON parsing
        public static final String TABLE_NAME = "cycle";
        public static final String COLUMN_DATE = "date"; //floating time stamp
        public static final String COLUMN_TIME = "time"; //text
        public static final String COLUMN_BOROUGH = "borough";
        public static final String COLUMN_ZIP_CODE = "zip_code";
        public static final String COLUMN_LATITUDE = "latitude"; //number
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_POINT = "point"; //point
        public static final String COLUMN_ON_STREET_NAME = "on_street_name"; //text
        public static final String COLUMN_OFF_STREET_NAME = "off_street_name";
        public static final String COLUMN_CROSS_STREET_NAME = "cross_street_name";
        public static final String COLUMN_NUMBER_OF_PERSONS_INJURED = "number_of_persons_injured"; //number
        public static final String COLUMN_NUMBER_OF_PERSONS_KILLED = "number_of_persons_killed";
        public static final String COLUMN_NUMBER_OF_PEDESTRIANS_INJURED = "number_of_pedestrians_injured";
        public static final String COLUMN_NUMBER_OF_PEDESTRIANS_KILLED = "number_of_pedestrians_killed";
        public static final String COLUMN_NUMBER_OF_CYCLIST_INJURED = "number_of_cyclist_injured";
        public static final String COLUMN_NUMBER_OF_CYCLIST_KILLED = "number_of_cyclist_killed";
        public static final String COLUMN_NUMBER_OF_MOTORIST_INJURED = "number_of_motorist_injured";
        public static final String COLUMN_NUMBER_OF_MOTORIST_KILLED = "number_of_motorist_killed";
        public static final String COLUMN_CONTRIBUTING_FACTOR_VEHICLE_1 = "contributing_factor_vehicle_1"; //text
        public static final String COLUMN_CONTRIBUTING_FACTOR_VEHICLE_2 = "contributing_factor_vehicle_2";
        public static final String COLUMN_CONTRIBUTING_FACTOR_VEHICLE_3 = "contributing_factor_vehicle_3";
        public static final String COLUMN_CONTRIBUTING_FACTOR_VEHICLE_4 = "contributing_factor_vehicle_4";
        public static final String COLUMN_CONTRIBUTING_FACTOR_VEHICLE_5 = "contributing_factor_vehicle_5";
        public static final String COLUMN_UNIQUE_KEY = "unique_key"; //number
        public static final String COLUMN_VEHICLE_TYPE_CODE_1 = "vehicle_type_code1"; //text
        public static final String COLUMN_VEHICLE_TYPE_CODE_2 = "vehicle_type_code2";
        public static final String COLUMN_VEHICLE_TYPE_CODE_3 = "vehicle_type_code3";
        public static final String COLUMN_VEHICLE_TYPE_CODE_4 = "vehicle_type_code4";
        public static final String COLUMN_VEHICLE_TYPE_CODE_5 = "vehicle_type_code5";
        public static final String COLUMN_LOCATION_ZIP = "location_zip";
        public static final String COLUMN_LOCATION_CITY = "location_city";
        public static final String COLUMN_LOCATION_ADDRESS = "location_address";
        public static final String COLUMN_LOCATION_STATE = "location_state";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CYCLE).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CYCLE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CYCLE;

        public static Uri buildCycleUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
}
