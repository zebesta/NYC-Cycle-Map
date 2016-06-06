package com.example.chrissebesta.nyccyclemap.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.content.ContextCompat;
import android.view.ViewGroup;

import com.example.chrissebesta.nyccyclemap.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.android.ui.SquareTextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by chrissebesta on 6/6/16.
 */
public class SimpleClusterRenderer extends DefaultClusterRenderer<MyItem> {
    private static final int CLUSTER_PADDING = 12;
    private static final int ITEM_PADDING = 7;

    private final Bitmap mIconItemGreen;
    private final IconGenerator mIconClusterGenerator;
    private final float mDensity;

    public SimpleClusterRenderer(Context context, GoogleMap map, ClusterManager<MyItem> clusterManager) {
        super(context, map, clusterManager);

        mDensity = context.getResources().getDisplayMetrics().density;

        mIconClusterGenerator = new CachedIconGenerator(context);
        mIconClusterGenerator.setContentView(makeSquareTextView(context, CLUSTER_PADDING));
        mIconClusterGenerator.setTextAppearance(com.google.maps.android.R.style.ClusterIcon_TextAppearance);

        IconGenerator iconItemGenerator = new IconGenerator(context);
        iconItemGenerator.setContentView(makeSquareTextView(context, ITEM_PADDING));
        iconItemGenerator.setBackground(makeClusterBackground(ContextCompat.getColor(context, R.color.colorPrimary)));
        mIconItemGreen = iconItemGenerator.makeIcon();
    }

    @Override
    protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
//        if (item.killed) {
//            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.blackwhitebike));
////            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
////                    BitmapFactory.decodeResource(resources, R.drawable.blackwhitebike)));
//        }else {
////            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
////                    BitmapFactory.decodeResource(resources, R.drawable.redwhitebike)));
//            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.redwhitebike));
//
//        }
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

        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(mIconItemGreen));
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<MyItem> cluster, MarkerOptions markerOptions) {
        int clusterSize = getBucket(cluster);

        mIconClusterGenerator.setBackground(makeClusterBackground(getColor(clusterSize)));
        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(mIconClusterGenerator.makeIcon(getClusterText(clusterSize)));
        markerOptions.icon(descriptor);
    }

//    @Override
//    protected boolean shouldRenderAsCluster(Cluster<MyItem> cluster) {
//        // Always render clusters.
//        return cluster.getSize() > 1;
//    }

    private int getColor(int clusterSize) {
        float size = Math.min((float) clusterSize, 300.0F);
        float hue = (300.0F - size) * (300.0F - size) / 90000.0F * 220.0F;
        return Color.HSVToColor(new float[]{hue, 1.0F, 0.6F});
    }

    private LayerDrawable makeClusterBackground(int color) {
        ShapeDrawable mColoredCircleBackground = new ShapeDrawable(new OvalShape());
        mColoredCircleBackground.getPaint().setColor(color);
        ShapeDrawable outline = new ShapeDrawable(new OvalShape());
        outline.getPaint().setColor(0x80ffffff);
        LayerDrawable background = new LayerDrawable(new Drawable[]{outline, mColoredCircleBackground});
        int strokeWidth = (int) (mDensity * 3.0F);
        background.setLayerInset(1, strokeWidth, strokeWidth, strokeWidth, strokeWidth);
        return background;
    }

    private SquareTextView makeSquareTextView(Context context, int padding) {
        SquareTextView squareTextView = new SquareTextView(context);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        squareTextView.setLayoutParams(layoutParams);
        squareTextView.setId(R.id.text);
        int paddingDpi = (int) (padding * mDensity);
        squareTextView.setPadding(paddingDpi, paddingDpi, paddingDpi, paddingDpi);
        return squareTextView;
    }
}
