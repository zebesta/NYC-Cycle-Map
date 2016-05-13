package com.example.chrissebesta.nyccyclemap.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class CycleDataSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static CycleDataSyncAdapter sCycleSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("CycleData", "onCreate - CycleData");
        synchronized (sSyncAdapterLock) {
            if (sCycleSyncAdapter == null) {
                sCycleSyncAdapter = new CycleDataSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sCycleSyncAdapter.getSyncAdapterBinder();
    }
}