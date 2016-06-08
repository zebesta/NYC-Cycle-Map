package com.wordpress.chrissebesta.nyccyclemap.data;

import android.content.Context;
import android.util.Log;

import com.wordpress.chrissebesta.nyccyclemap.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chrissebesta on 4/27/16.
 * Custom bike cluster renderer to allow for unique icons while using the default cluster renderer to handlt the clustering
 * <p/>
 * TODO Correct the white box appearing issue that was introduced by Google on the Google play services library
 * More details about the white box issue can be found here: https://code.google.com/p/gmaps-api-issues/issues/detail?id=9765
 */
public class BikeClusterRenderer extends DefaultClusterRenderer<MyItem> {
    public final String LOG_TAG = BikeClusterRenderer.class.getSimpleName();
    private GoogleMap mMap;

    //private Context mContext;
    public BikeClusterRenderer(Context context, GoogleMap map, ClusterManager<MyItem> clusterManager) {
        super(context, map, clusterManager);
        mMap = map;
        //mContext = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
        // Change the icon to be a Bike
        //final Resources resources = mContext.getResources();
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        //If item is within bounds, allow it to be shown, otherwise set it to invisble
        if (bounds.contains(item.getPosition())) {
            markerOptions.visible(true);
            if (item.killed) {
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.blackwhitebike));
//            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
//                    BitmapFactory.decodeResource(resources, R.drawable.blackwhitebike)));
            } else {
//            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
//                    BitmapFactory.decodeResource(resources, R.drawable.redwhitebike)));
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.redwhitebike));

            }
            String inputDateStr = item.date;
            //Convert date format, this is done here to prevent doing it for every marker, only done on click instead
            DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
            DateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy");
            Date date = null;
            try {
                date = inputFormat.parse(inputDateStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String outputDateStr = outputFormat.format(date);
            markerOptions.title(outputDateStr);
            markerOptions.snippet("Click for more info.....\nID: " + String.valueOf(item.uniqueId));
        } else {
            //set Item to be invisible if its outside of the visible range on the Map
            markerOptions.visible(false);
//            Log.d(LOG_TAG, "Setting item to invisible at " + markerOptions.getPosition());
        }
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<MyItem> cluster, MarkerOptions markerOptions) {
        super.onBeforeClusterRendered(cluster, markerOptions);

        //set Cluster to be invisible if its outside of the visible range on the Map
        LatLngBounds bounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        if (bounds.contains(cluster.getPosition())) {
            markerOptions.visible(true);
        } else {
//            Log.d(LOG_TAG, "Setting cluster to invisible at " + markerOptions.getPosition());
            markerOptions.visible(false);
        }
    }
    //Temporary fix for the white box issue, is very memory intensive and creates its own issues
//
//    @Override
//    protected void onBeforeClusterRendered(Cluster<MyItem> cluster, MarkerOptions markerOptions) {
//        super.onBeforeClusterRendered(cluster, markerOptions);
//        final Resources resources = mContext.getResources();
//        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
//                BitmapFactory.decodeResource(resources, R.drawable.redwhitebikeplus)));
//    }
    //    @Override
//    protected boolean shouldRenderAsCluster(Cluster cluster) {
//        //return super.shouldRenderAsCluster(cluster);
//        //temporarily removing clusters to prevent white box issue related to google play services version
//        return false;
//    }
}
