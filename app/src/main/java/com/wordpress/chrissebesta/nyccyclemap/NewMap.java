package com.wordpress.chrissebesta.nyccyclemap;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
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
    public static final String ARG_CAMPOS = "arg camera position";
    private GoogleMap mMap;
    private CameraPosition mSavedCameraPosition;
    private List<MyItem> mItems;
    private ClusterManager<MyItem> mClusterManager;
    private MapView mapView;
    private OnMapCameraChangedListener mCallback;
    private Context mContext;


    public NewMap() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param cameraPosition Saved camera position or requested camera position to launch map with
     * @return A new instance of fragment NewMap.
     */
    public static NewMap newInstance(CameraPosition cameraPosition) {
        Log.d(LOG_TAG, "Calling new instance");
        NewMap fragment = new NewMap();
        Bundle args = new Bundle();
        args.putParcelable(ARG_CAMPOS, cameraPosition);
        fragment.setArguments(args);
        return fragment;
    }

    public interface OnMapCameraChangedListener {
        public void onMapChanged(CameraPosition cameraPosition);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //mContext = context;
        Log.d(LOG_TAG, "Calling on attach - context");
        if (context instanceof OnMapCameraChangedListener) {
            mCallback = (OnMapCameraChangedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(LOG_TAG, "Calling on attach - activity");
        if (activity instanceof OnMapCameraChangedListener) {
            mCallback = (OnMapCameraChangedListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(LOG_TAG, "Calling on detach");
        mCallback = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "Calling on Create");
        mContext = getActivity().getApplicationContext();
        if (getArguments() != null) {
            mSavedCameraPosition = getArguments().getParcelable(ARG_CAMPOS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Calling on create view");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_map, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(mSavedCameraPosition!=null) {
            mapView = (MapView) view.findViewById(R.id.map);
            mapView.onCreate(savedInstanceState);
            mapView.onResume();
            mapView.getMapAsync(this);//when you already implement OnMapReadyCallback in your fragment
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(LOG_TAG, "Calling on map ready");
        mMap = googleMap;
        CameraPosition cp;
//        mSavedCameraPosition = ((MainActivity) getActivity()).getmSavedCameraPosition();
        if (mSavedCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mSavedCameraPosition));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.7119042, -74.0066549), 8));
        }

        Log.d(LOG_TAG, "Context is = "+mContext);
        mClusterManager = new ClusterManager<MyItem>(mContext, mMap);
        //Cluster manager with unique bike icons
        mClusterManager.setRenderer(new BikeClusterRenderer(mContext, mMap, mClusterManager));


        //mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                new DynamicallyAddMakerTask().execute(mMap.getProjection().getVisibleRegion().latLngBounds);
                Log.d(LOG_TAG, "Calling callback to update camera position in the activity");
                Log.d(LOG_TAG, "Camera position is = " + mMap.getCameraPosition());
                mCallback.onMapChanged(mMap.getCameraPosition());
                //mMap.getCameraPosition();
            }
        });
        mMap.setOnInfoWindowClickListener(this);


        readItems();

    }

    private void readItems() {
        mItems = new MyItemReader(mContext).read();
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
        Intent intent = new Intent(mContext, DetailActivity.class);
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

