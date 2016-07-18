package com.wordpress.chrissebesta.nyccyclemap;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;
import com.wordpress.chrissebesta.nyccyclemap.data.BikeClusterRenderer;
import com.wordpress.chrissebesta.nyccyclemap.data.MyItem;
import com.wordpress.chrissebesta.nyccyclemap.data.MyItemReader;

import java.util.List;

public class NewMap extends android.app.Fragment implements
        OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener {


    private static final String LOG_TAG = "NewMap";
    private GoogleMap mMap;
    private CameraPosition mSavedCameraPosition;
    private List<MyItem> mItems;
    private ClusterManager<MyItem> mClusterManager;
    private MapView mapView;


    public NewMap() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_map, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);//when you already implement OnMapReadyCallback in your fragment
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mSavedCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mSavedCameraPosition));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.7119042, -74.0066549), 8));
        }

        mClusterManager = new ClusterManager<MyItem>(getActivity().getBaseContext(), mMap);

        //Cluster manager with unique bike icons
        mClusterManager.setRenderer(new BikeClusterRenderer(getActivity().getBaseContext(), mMap, mClusterManager));


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

    private void readItems() {
        mItems = new MyItemReader(getActivity().getBaseContext()).read();
        mClusterManager.addItems(mItems);

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String snippetString = marker.getSnippet();
        String intValueString = snippetString.replaceAll("[^0-9]", "");
        int intValue = Integer.parseInt(intValueString);
        Log.d(LOG_TAG, "The intValueString is: " + intValueString + " and the inValue is: " + intValue + " and the snippet string is: " + snippetString);
//        Toast.makeText(this, "Info window clicked, ID = " + intValueString,
//                Toast.LENGTH_SHORT).show();
        Log.d(LOG_TAG, "Info window has been clicked!");
        Intent intent = new Intent(getActivity().getBaseContext(), DetailActivity.class);
        intent.putExtra(getString(R.string.unique_id_extra_key), intValue);
        startActivity(intent);

    }


    private class DynamicallyAddMakerTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] params) {
            mClusterManager.clearItems();
            LatLngBounds bounds = (LatLngBounds) params[0];
            Log.d(LOG_TAG, "In do in background for dynamically adding markers and lat lng bounds are: " + bounds);
            for (int i = 0; i < mItems.size(); i++) {
                if (bounds.contains(mItems.get(i).getPosition())) {
                    mClusterManager.addItem(mItems.get(i));
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            mClusterManager.cluster();
        }
    }
}

