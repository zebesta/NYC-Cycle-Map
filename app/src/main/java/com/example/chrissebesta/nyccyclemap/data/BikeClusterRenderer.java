package com.example.chrissebesta.nyccyclemap.data;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;

import com.example.chrissebesta.nyccyclemap.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * Created by chrissebesta on 4/27/16.
 * Custom bike cluster renderer to allow for unique icons while using the default cluster renderer to handlt the clustering
 */
public class BikeClusterRenderer extends DefaultClusterRenderer<MyItem> {
    private Context mContext;
    public BikeClusterRenderer(Context context, GoogleMap map, ClusterManager<MyItem> clusterManager) {
        super(context, map, clusterManager);
        mContext = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
        // Change the icon to be a Bike
        final Resources resources = mContext.getResources();
        if (item.killed) {
            //markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.blackwhitebike));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
                    BitmapFactory.decodeResource(resources, R.drawable.blackwhitebike)));
        }else markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.redwhitebike)));
        markerOptions.title(item.date);
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<MyItem> cluster, MarkerOptions markerOptions) {
        super.onBeforeClusterRendered(cluster, markerOptions);
        final Resources resources = mContext.getResources();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.redwhitebikeplus)));
    }
    //    @Override
//    protected boolean shouldRenderAsCluster(Cluster cluster) {
//        //return super.shouldRenderAsCluster(cluster);
//        //temporarily removing clusters to prevent
//        return false;
//    }
}
