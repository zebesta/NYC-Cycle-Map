/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wordpress.chrissebesta.nyccyclemap.data;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
/**
 * Created by chrissebesta on 4/26/16.
 * Custom Item as required for the clustering in google maps API 2.0
 * Also adds a date that is used for the title of the rendered Markers
 */

public class MyItem implements ClusterItem {
    private final LatLng mPosition;
    public final String date;
    public final boolean killed;
    public final int uniqueId;

    public MyItem(double lat, double lng, String date, boolean killed, int uniqueId) {
        mPosition = new LatLng(lat, lng);
        this.date = date;
        this.killed = killed;
        this.uniqueId = uniqueId;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
}
