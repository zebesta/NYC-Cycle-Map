package com.example.chrissebesta.nyccyclemap.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.LruCache;

import com.google.maps.android.ui.IconGenerator;

/**
 * Created by chrissebesta on 6/6/16.
 */
public class CachedIconGenerator extends IconGenerator {

    private final LruCache<String, Bitmap> mBitmapsCache;
    private String mText;

    public CachedIconGenerator(Context context) {
        super(context);

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;
        mBitmapsCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public Bitmap makeIcon(String text) {
        mText = text;
        return super.makeIcon(text);
    }

    @Override
    public Bitmap makeIcon() {
        if (TextUtils.isEmpty(mText)) {
            return super.makeIcon();
        } else {
            Bitmap bitmap = mBitmapsCache.get(mText);
            if (bitmap == null) {
                bitmap = super.makeIcon();
                mBitmapsCache.put(mText, bitmap);
            }
            return bitmap;
        }
    }
}
