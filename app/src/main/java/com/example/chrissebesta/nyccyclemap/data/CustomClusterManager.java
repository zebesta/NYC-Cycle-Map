package com.example.chrissebesta.nyccyclemap.data;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.maps.android.clustering.ClusterManager;

import java.util.Collection;
import java.util.List;

/**
 * Created by chrissebesta on 6/6/16.
 * A custom cluster manager that updates the items list when the camera is updated,
 * this will only show items that are within the maps bounds
 */
public class CustomClusterManager<M> extends ClusterManager<MyItem> {
    private static final String LOG_TAG = CustomClusterManager.class.getSimpleName();
    private Context mContext;
    private GoogleMap mMap;
    public CustomClusterManager(Context context, GoogleMap map) {
        super(context, map);
        mContext = context;
        mMap = map;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        //empty the existing item list and repopulate based on current map bounds
        //this also requires a re-calculation for the clustering
        this.clearItems();
        List<MyItem> items = new MyItemReader(mContext, mMap).read();
        this.addItems(items);
        this.cluster();
    }

    @Override
    public void addItem(MyItem myItem) {
        super.addItem(myItem);
    }

    @Override
    public void addItems(Collection<MyItem> items) {
        super.addItems(items);
    }
}
