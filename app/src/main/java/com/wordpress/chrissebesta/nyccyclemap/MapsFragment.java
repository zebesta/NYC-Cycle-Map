//package com.wordpress.chrissebesta.nyccyclemap;
//
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.model.LatLng;
//
///**
// * Created by chrissebesta on 7/11/16.
// */
//public class MapsFragment extends android.app.Fragment {
//    static final LatLng HAMBURG = new LatLng(53.558, 9.927);
//    static final LatLng KIEL = new LatLng(53.551, 9.993);
//    private SupportMapFragment map;
//
//    public MapsFragment(){
//        //Constructor here!
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.map_fragment_layout, container, false);
//
////        FragmentManager fm = getFragmentManager();
////        map = fm.findFragmentById(R.id.ma)
////        Marker hamburg = map.addMarker(new MarkerOptions().position(HAMBURG)
////                .title("Hamburg"));
////        Marker kiel = map.addMarker(new MarkerOptions()
////                .position(KIEL)
////                .title("Kiel")
////                .snippet("Kiel is cool"));
////
////        // Move the camera instantly to hamburg with a zoom of 15.
////        map.moveCamera(CameraUpdateFactory.newLatLngZoom(HAMBURG, 15));
////
////        // Zoom in, animating the camera.
////        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
////
////        //...
//        return view;
//    }
//}
