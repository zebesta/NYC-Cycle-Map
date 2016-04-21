//package com.example.chrissebesta.nyccyclemap.sync;
//
//import android.app.Service;
//import android.content.Intent;
//import android.os.IBinder;
//import android.util.Log;
//
//public class CycleDataSyncService extends Service {
//    private static final Object sSyncAdapterLock = new Object();
//    private static CycleDataSyncAdapter sSunshineSyncAdapter = null;
//
//    @Override
//    public void onCreate() {
//        Log.d("CycleData", "onCreate - CycleData");
//        synchronized (sSyncAdapterLock) {
//            if (sSunshineSyncAdapter == null) {
//                sSunshineSyncAdapter = new CycleDataSyncAdapter(getApplicationContext(), true);
//            }
//        }
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return sSunshineSyncAdapter.getSyncAdapterBinder();
//    }
//}