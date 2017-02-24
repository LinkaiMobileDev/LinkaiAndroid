package com.linkai.app.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.linkai.app.libraries.Common;
import com.linkai.app.services.MainService;

/**
 * Created by LP1001 on 06-10-2016.
 */
public class AppStartReceiver extends BroadcastReceiver {
    private final String TAG="AppStartReceiver";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "onReceive: "+intent.getAction());
//        Toast.makeText(context,"AppStartReceiver",Toast.LENGTH_LONG).show();
        final Common common=new Common(context);
//        thread to start service if it is not running
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(6000);
                    Log.d(TAG, "run: start running"+common.isMyServiceRunning(MainService.class));
                    common.startMainService();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
