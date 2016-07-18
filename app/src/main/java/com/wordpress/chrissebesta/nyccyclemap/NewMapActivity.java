package com.wordpress.chrissebesta.nyccyclemap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.model.CameraPosition;

public class NewMapActivity extends AppCompatActivity implements NewMap.OnMapCameraChangedListener {
    private static final String MAP_FRAGMENT_TAG = "map";
    CameraPosition mSavedCameraPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_map);
        Intent intent = getIntent();
        if(intent.hasExtra("arg camera position")){
            mSavedCameraPosition = intent.getExtras().getParcelable("arg camera position");
        }


        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.map_fragment_container, NewMap.newInstance(mSavedCameraPosition), MAP_FRAGMENT_TAG)
                    .commit();
        }
        getFragmentManager().beginTransaction()
                .add(R.id.map_fragment_container, NewMap.newInstance(mSavedCameraPosition), MAP_FRAGMENT_TAG)
                .commit();
    }

    @Override
    public void onMapChanged(CameraPosition cameraPosition) {
        mSavedCameraPosition = cameraPosition;
    }
}


