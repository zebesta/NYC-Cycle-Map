//package com.wordpress.chrissebesta.nyccyclemap;
//
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.RelativeLayout;
//
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;
//
///**
// * Created by chrissebesta on 7/11/16.
// */
//public class MapsFragment extends Fragment {
//
//    private static View view;
//    /**
//     * Note that this may be null if the Google Play services APK is not
//     * available.
//     */
//
//    private static GoogleMap mMap;
//    private static Double latitude, longitude;
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        if (container == null) {
//            return null;
//        }
//        view = (RelativeLayout) inflater.inflate(R.layout.map_fragment_layout, container, false);
//        // Passing harcoded values for latitude & longitude. Please change as per your need. This is just used to drop a Marker on the Map
//        latitude = 26.78;
//        longitude = 72.56;
//
//        setUpMapIfNeeded(); // For setting up the MapFragment
//
//        return view;
//    }
//
//    /***** Sets up the map if it is possible to do so *****/
//    public static void setUpMapIfNeeded() {
//        // Do a null check to confirm that we have not already instantiated the map.
//        if (mMap == null) {
//            // Try to obtain the map from the SupportMapFragment.
//            mMap = ((SupportMapFragment) MainActivity.fragmentManager
//                    .findFragmentById(R.id.location_map)).getMap();
//            // Check if we were successful in obtaining the map.
//            if (mMap != null)
//                setUpMap();
//        }
//    }
//
//    /**
//     * This is where we can add markers or lines, add listeners or move the
//     * camera.
//     * <p>
//     * This should only be called once and when we are sure that {@link #mMap}
//     * is not null.
//     */
//    private static void setUpMap() {
//        // For showing a move to my loction button
//        mMap.setMyLocationEnabled(true);
//        // For dropping a marker at a point on the Map
//        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("My Home").snippet("Home Address"));
//        // For zooming automatically to the Dropped PIN Location
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,
//                longitude), 12.0f));
//    }
//
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        // TODO Auto-generated method stub
//        if (mMap != null)
//            setUpMap();
//
//        if (mMap == null) {
//            // Try to obtain the map from the SupportMapFragment.
//            mMap = ((SupportMapFragment) MainActivity.fragmentManager
//                    .findFragmentById(R.id.location_map)).getMap(); // getMap is deprecated
//            // Check if we were successful in obtaining the map.
//            if (mMap != null)
//                setUpMap();
//        }
//    }
//
//    /**** The mapfragment's id must be removed from the FragmentManager
//     **** or else if the same it is passed on the next time then
//     **** app will crash ****/
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        if (mMap != null) {
//            MainActivity.fragmentManager.beginTransaction()
//                    .remove(MainActivity.fragmentManager.findFragmentById(R.id.location_map)).commit();
//            mMap = null;
//        }
//    }
//}