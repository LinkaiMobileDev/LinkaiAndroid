package com.linkai.app.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.linkai.app.ChatApplication;
import com.linkai.app.libraries.Common;
import com.linkai.app.libraries.MyXMPP;

public class MainService extends Service {
    private final String TAG="MainService";
    private MyXMPP xmpp;
    private Context context;
    private Common common;
    public MainService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        xmpp=((ChatApplication)getApplication()).getMyXMPPInstance();
        context=this.getApplicationContext();
        common=new Common(context);
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
//        Log.d(TAG, "onStartCommand: cur_act "+ Const.CUR_ACTIVITY);
//        checking if application is in background. if so user needs to go offline
        Log.d(TAG, "onStartCommand: "+ChatApplication.ACTIVITY_RESUME_COUNT+"-"+ChatApplication.ACTIVITY_PAUSE_COUNT);
        try {
            if (common.isRunningInBackground()) {
                xmpp.setPresence(false);
            } else {
                xmpp.setPresence(true);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
//        calling thread for checking whether app is running, xmpp is connected or not, xmpp is loggedin or not etc.
        mainThread();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw null;
    }



    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "onTaskRemoved: ");

        this.sendBroadcast(new Intent("chat.start.service"));
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        this.sendBroadcast(new Intent("chat.start.service"));
        super.onDestroy();
    }

//    method for checking if app running in foreground or background. if in background make user presence online
    private void mainThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Thread.sleep(10000);
                       boolean status;
//                        making offline or online
                        //Log.d(TAG, "onStartCommand: "+ChatApplication.ACTIVITY_RESUME_COUNT+"-"+ChatApplication.ACTIVITY_PAUSE_COUNT);
                        if(ChatApplication.ACTIVITY_RESUME_COUNT<=ChatApplication.ACTIVITY_PAUSE_COUNT ){
                            status=false;
                        }
                        else{
                            status=true;
                        }
                        if(MyXMPP.IS_CONNECTED && MyXMPP.IS_LOGGEDIN){
                            xmpp.setPresence(status);
                        }
                        else if(!MyXMPP.IS_CONNECTED ){
//                            to reconnect if not logged in
                            xmpp.connectConnection();
                            Log.d(TAG, "run: not connected");
                        }
                        else if(!MyXMPP.IS_LOGGEDIN ){
//                            to reconnect if not logged in
                            xmpp.login();
                            Log.d(TAG, "run: not loggedin");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

}
