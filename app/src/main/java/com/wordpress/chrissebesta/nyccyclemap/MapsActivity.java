package com.wordpress.chrissebesta.nyccyclemap;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;
import com.wordpress.chrissebesta.nyccyclemap.data.MyItem;
import com.wordpress.chrissebesta.nyccyclemap.data.MyItemReader;
import com.wordpress.chrissebesta.nyccyclemap.data.SimpleClusterRenderer;

import java.util.List;

public class MapsActivity extends FragmentActivity implements
        GoogleMap.OnInfoWindowClickListener,
        OnMapReadyCallback {
    private ClusterManager<MyItem> mClusterManager;
    public final String LOG_TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;
    private CameraPosition mSavedCameraPosition;
    private List<MyItem> mItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mSavedCameraPosition = savedInstanceState.getParcelable(getString(R.string.cameraposition));
        }
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(getString(R.string.cameraposition), mMap.getCameraPosition());
        super.onSaveInstanceState(outState);
    }

    private void readItems() {
        mItems = new MyItemReader(getBaseContext()).read();
        mClusterManager.addItems(mItems);
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
        mMap = googleMap;
        if (mSavedCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mSavedCameraPosition));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.7119042, -74.0066549), 8));
        }

        mClusterManager = new ClusterManager<MyItem>(this, mMap);

        try {
            int v = getPackageManager().getPackageInfo("com.google.android.gms", 0).versionCode;
            String version = getPackageManager().getPackageInfo("com.google.android.gms", 0).versionName;
            Log.d(LOG_TAG, "Google play services version is: " + v + " and the name is: " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //Cluster manager, select between generated icone or bike icons by changing this
        //Generated icons avoid the white box issue
        mClusterManager.setRenderer(new SimpleClusterRenderer(this, mMap, mClusterManager));

        //mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                new DynamicallyAddMakerTask().execute(mMap.getProjection().getVisibleRegion().latLngBounds);
            }
        });
        mMap.setOnInfoWindowClickListener(this);


        readItems();

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String snippetString = marker.getSnippet();
        String intValueString = snippetString.replaceAll("[^0-9]", "");
        int intValue = Integer.parseInt(intValueString);
//        Log.d(LOG_TAG, "The intValueString is: " + intValueString + " and the inValue is: " + intValue + " and the snippet string is: " + snippetString);
//        Toast.makeText(this, "Info window clicked, ID = " + intValueString,
//                Toast.LENGTH_SHORT).show();
//        Log.d(LOG_TAG, "Info window has been clicked!");
        Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
        intent.putExtra(getString(R.string.unique_id_extra_key), intValue);
        startActivity(intent);
    }

    /**
     * An async task to populate the cluster managers Items list with
     * only the markers that are in the cameras viewable bounds
     */
    private class DynamicallyAddMakerTask extends AsyncTask {
        /**
         * empy the cluster manager items list and populate it with the new markers
         * @param params
         * @return
         */
        @Override
        protected Object doInBackground(Object[] params) {
            mClusterManager.clearItems();
            LatLngBounds bounds = (LatLngBounds) params[0];
//            Log.d(LOG_TAG, "In do in background for dynamically adding markers and lat lng bounds are: "+bounds);
            for (int i = 0; i<mItems.size(); i++) {
                if (bounds.contains(mItems.get(i).getPosition())) {
                    mClusterManager.addItem(mItems.get(i));
                }
            }
            return null;
        }

        /**
         * Updates the cluster manager by asking it to render the new markers
         * @param o
         */
        @Override
        protected void onPostExecute(Object o) {
            mClusterManager.cluster();
        }
    }
}
