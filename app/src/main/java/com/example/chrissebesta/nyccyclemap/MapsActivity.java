package com.example.chrissebesta.nyccyclemap;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.example.chrissebesta.nyccyclemap.data.CycleContract;
import com.example.chrissebesta.nyccyclemap.data.CycleDbHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    public final String LOG_TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //TODO: Will want to limit markers to only show incidents within  a boundary from the current focused location. SQL query should reflect this
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        //Create cursor to cycle through cycle injury databse data
        CycleDbHelper helper = new CycleDbHelper(getBaseContext());
        final SQLiteDatabase db = helper.getWritableDatabase();

        //Coordinates near Manhattan to Brooklyn Bridges: 40.7111704,-73.9936095
        //LatLng nycBridges = new LatLng(40.7111704,-73.9936095);
        //Coordinates where Google Maps places the New York label
        LatLng nycGoogleLabel = new LatLng(40.7119042, -74.0066549);

        //TODO START CODE BLOCK this code needs to be implemented when the user moved the current position of the camera:
        //This will likely require a different callback routine and should not be placed here, OnMapReady is not giving response needed
        //Currently just focuses on 0,0 because this is done before markers are added and camera is updated
        //get lat and longitude of current camera position
        mMap.moveCamera(CameraUpdateFactory.newLatLng(nycGoogleLabel));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));


//        CameraPosition cameraPosition = mMap.getCameraPosition();
//        LatLng latLngCamera = cameraPosition.target;
//        double cpLatitude = latLngCamera.latitude;
//        double cpLongitude = latLngCamera.longitude;
//        Log.d(LOG_TAG, "Current lat/lng for map's camera position is: "+latLngCamera.toString());
//
//        LatLngBounds curScreen = mMap.getProjection()
//                .getVisibleRegion().latLngBounds;

        //TODO END CODE BLOCK

        //Set on Camera change listener and only load the markers that are within the visible area
        //TODO based on performance might need to set a max zoom level based on number of values in the cursor
        //Can also use clustering to reduce load on google maps, but need to determine if simply accessing so many points in the problem
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                //get new visible map bounds
                LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
                LatLng northeast = bounds.northeast;
                String boundLat = String.valueOf(northeast.latitude);
                String boundLong = String.valueOf(northeast.longitude);
                LatLng southwest = bounds.southwest;
                String boundLat2 = String.valueOf(southwest.latitude);
                String boundLong2 = String.valueOf(southwest.longitude);
                Log.d(LOG_TAG, "The northeast location is: " + northeast + " and the southwest location is: " + southwest);


                //clear old markers from map
                mMap.clear();
                // or if your a using cluster manager:
                //mClusterManager.clearItems();
                //String[] fields = new String[]{"name", "latitude", "longitude"};
                String[] args = new String[]{boundLat, boundLong, boundLat2, boundLong2,};

                //Cursor cursor = db.query("markers", fields, "latitude<=? AND longitude<=? AND latitude>=? AND longitude>=?");
                //query the SQL DB for only the items that are in the visible field
                Cursor cursor = db.query(CycleContract.CycleEntry.TABLE_NAME, null, "latitude<=? AND longitude<=? AND latitude>=? AND longitude>=?", args, null, null, null, null);
                Log.d(LOG_TAG, "Queried the database and received this many samples: "+ cursor.getCount());

                //populate the map with the markers
                if (cursor.moveToFirst()) {
                    do {
                        //Get the LatLng of the next item to be added
                        LatLng latLng = new LatLng(cursor.getDouble(cursor.getColumnIndex(CycleContract.CycleEntry.COLUMN_LATITUDE)), cursor.getDouble(cursor.getColumnIndex(CycleContract.CycleEntry.COLUMN_LONGITUDE)));
                        mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_directions_bike_white_24dp))
                                        //.title(boroughName)
                                .position(latLng));
                        //TODO implement cluster manager here

                    } while (cursor.moveToNext());

                    //if using cluster manager add :
                    //mClusterManager.cluster();
                }
                cursor.close();
            }


        });


//        //TODO update this to only query the database for a limited lat and long region around current maps focus to limit work here
//        Cursor cursor = db.rawQuery("SELECT * FROM " + CycleContract.CycleEntry.TABLE_NAME, null);
//        cursor.moveToFirst();
//
//        //Add locations for each point in the
//        for (int i = 0; i < cursor.getCount(); i++) {
//            //String boroughName = cursor.getString(cursor.getColumnIndex(CycleContract.CycleEntry.COLUMN_BOROUGH));
//            //String contributingFactor = cursor.getString(cursor.getColumnIndex(CycleContract.CycleEntry.COLUMN_CONTRIBUTING_FACTOR_VEHICLE_1));
//            LatLng latLng = new LatLng(cursor.getDouble(cursor.getColumnIndex(CycleContract.CycleEntry.COLUMN_LATITUDE)), cursor.getDouble(cursor.getColumnIndex(CycleContract.CycleEntry.COLUMN_LONGITUDE)));
//            Log.d(LOG_TAG, "Adding point to map at: " + latLng.toString());
//            mMap.addMarker(new MarkerOptions()
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_directions_bike_white_24dp))
//                            //.title(boroughName)
//                    .position(latLng));
//            //continue cycling through the SQL Cursor
//            cursor.moveToNext();
//
//            //Move camera to added pin, ideally would not do this in the repeated block and instead only call once for last pin added
//            //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        }
        //mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
//        cursor.close();

    }
}
