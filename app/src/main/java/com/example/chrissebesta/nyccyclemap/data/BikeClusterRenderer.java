package com.example.chrissebesta.nyccyclemap.data;

import android.content.Context;

import com.example.chrissebesta.nyccyclemap.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chrissebesta on 4/27/16.
 * Custom bike cluster renderer to allow for unique icons while using the default cluster renderer to handlt the clustering
 */
public class BikeClusterRenderer extends DefaultClusterRenderer<MyItem> {
    //private Context mContext;
    public BikeClusterRenderer(Context context, GoogleMap map, ClusterManager<MyItem> clusterManager) {
        super(context, map, clusterManager);
        //mContext = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
        // Change the icon to be a Bike
        //final Resources resources = mContext.getResources();
        if (item.killed) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.blackwhitebike));
//            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
//                    BitmapFactory.decodeResource(resources, R.drawable.blackwhitebike)));
        }else {
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
        markerOptions.snippet("Click for more info.....\nID: "+String.valueOf(item.uniqueId));
    }
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
