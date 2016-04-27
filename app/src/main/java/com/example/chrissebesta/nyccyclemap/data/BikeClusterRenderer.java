package com.example.chrissebesta.nyccyclemap.data;

import android.content.Context;

import com.example.chrissebesta.nyccyclemap.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by chrissebesta on 4/27/16.
 * Custom bike cluster renderer to allow for unique icons while using the default cluster renderer to handlt the clustering
 */
public class BikeClusterRenderer extends DefaultClusterRenderer<MyItem> {
    public BikeClusterRenderer(Context context, GoogleMap map, ClusterManager<MyItem> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
        // Change the icon to be a Bike
        //TODO adjust the icon based on if person was injured or killed
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.redwhitebike));
    }
}
