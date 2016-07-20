package com.wordpress.chrissebesta.nyccyclemap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import static java.lang.Double.longBitsToDouble;

public class NewMapActivity extends AppCompatActivity implements NewMap.OnMapCameraChangedListener {
    private static final String MAP_FRAGMENT_TAG = "map";
    private static final String LOG_TAG = "NewMapActivity";
    private static final String STATE_MAP_POS = "mapPositionState";
    private CameraPosition mSavedCameraPosition;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_map);
        Intent intent = getIntent();
        if(intent.hasExtra("arg camera position")){
            mSavedCameraPosition = intent.getExtras().getParcelable("arg camera position");
            Log.d(LOG_TAG, "saved camera position taken from intent extra and it is = "+mSavedCameraPosition);
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        double lat = longBitsToDouble(sharedPreferences.getLong("latitude", Double.doubleToRawLongBits(40.7119042)));
        double lon = longBitsToDouble(sharedPreferences.getLong("longitude", Double.doubleToRawLongBits(-74.0066549)));
        float zoom = sharedPreferences.getFloat("zoom", (float)8);
        mSavedCameraPosition = new CameraPosition(new LatLng(lat, lon), zoom,0,0);


        if (savedInstanceState == null) {
            Log.d(LOG_TAG, "creating new map instance and saved camera position is: "+mSavedCameraPosition);
            getFragmentManager().beginTransaction()
                    .add(R.id.map_fragment_container, NewMap.newInstance(mSavedCameraPosition), MAP_FRAGMENT_TAG)
                    .commit();
        }else{
            Log.d(LOG_TAG, "creating new map with saved instance state and saved camera position is: "+mSavedCameraPosition);
            getFragmentManager().beginTransaction()
                    .add(R.id.map_fragment_container, NewMap.newInstance(mSavedCameraPosition), MAP_FRAGMENT_TAG)
                    .commit();
        }
//        getFragmentManager().beginTransaction()
//                .add(R.id.map_fragment_container, NewMap.newInstance(mSavedCameraPosition), MAP_FRAGMENT_TAG)
//                .commit();
    }

    @Override
    public void onMapChanged(CameraPosition cameraPosition) {
        Log.d(LOG_TAG, "Using the onMapChanged interface to update the saved camera position which is = "+cameraPosition);
        mSavedCameraPosition = cameraPosition;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("latitude", Double.doubleToRawLongBits(mSavedCameraPosition.target.latitude));
        editor.putLong("longitude", Double.doubleToRawLongBits(mSavedCameraPosition.target.longitude));
        editor.putFloat("zoom", mSavedCameraPosition.zoom);
        editor.commit();

    }
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        Log.d(LOG_TAG, "Saving instance state and camera position is "+mSavedCameraPosition);
        outState.putParcelable(STATE_MAP_POS, mSavedCameraPosition);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        Log.d(LOG_TAG, "Restoring instance state and camera position is "+mSavedCameraPosition);
        mSavedCameraPosition = savedInstanceState.getParcelable(STATE_MAP_POS);
    }
}


