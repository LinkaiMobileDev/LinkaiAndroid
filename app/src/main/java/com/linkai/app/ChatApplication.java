package com.linkai.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.libraries.MyXMPP;

import org.acra.annotation.ReportsCrashes;

/**
 * Created by LP1001 on 13-07-2016.
 */
//to hide after testing
@ReportsCrashes(formUri = "http://59.90.10.142/LpChat/crash_report.php")
public class ChatApplication extends Application {
    private final String TAG="ChatApplication";

    public static int ACTIVITY_START_COUNT=0;
    public static int ACTIVITY_RESUME_COUNT=0;
    public static int ACTIVITY_PAUSE_COUNT=0;
    public static int ACTIVITY_STOP_COUNT=0;

    MyXMPP xmpp=null;
    public ChatApplication(){

    }

    public void onCreate() {
        super.onCreate();
//init public db
        Const.DB=new DatabaseHandler(this);
//        set static context variable
        Const.CONTEXT=this.getApplicationContext();
//        register activity lifecycle caLLBACK
        registerActivityLifecycleCallbacks(new ChatActivityLifecyleListener());
    }

//    @Override
//    public void onTerminate() {
//        super.onTerminate();
//        Log.d(TAG, "onTerminate: ");
//    }



    public void InitXMPPInstance(){
        if(!Const.DB.isUserExist()){
            xmpp=null;
            return;
        }
        if(xmpp==null) {
            xmpp = new MyXMPP(this);
            xmpp.init();
            xmpp.connectConnection();
        }
//        Log.d(TAG, "reInitXMPPInstance: re initializing");
    }

//    get xmpp object
    public MyXMPP getMyXMPPInstance(){
        InitXMPPInstance();
//        Log.d(TAG, "getMyXMPPInstance: ");
        return this.xmpp;
    }

//    function to set user off line if app is in background
    public void goOfflineIfAppInBg(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
//                    check activity callback counts
                    if(ACTIVITY_RESUME_COUNT<=ACTIVITY_PAUSE_COUNT){
                        if(xmpp!=null){
                            xmpp.setPresence(false);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

//    activity life cycle receiver
    private class ChatActivityLifecyleListener implements ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
            Log.d(TAG, "onActivityCreated: ");
        }

        @Override
        public void onActivityStarted(Activity activity) {
            ACTIVITY_START_COUNT++;
            Log.d(TAG, "onActivityStarted: "+ACTIVITY_START_COUNT);
        }

        @Override
        public void onActivityResumed(Activity activity) {
            ACTIVITY_RESUME_COUNT++;
            Log.d(TAG, "onActivityResumed: "+ACTIVITY_RESUME_COUNT);
//            make user online if currently offline
//            if(!Const.IS_ONLINE){
//                xmpp.setPresence(true);
//            }
        }

        @Override
        public void onActivityPaused(Activity activity) {
            ACTIVITY_PAUSE_COUNT++;
            Log.d(TAG, "onActivityPaused: "+ACTIVITY_PAUSE_COUNT);
//            check if app in bg
            goOfflineIfAppInBg();
        }

        @Override
        public void onActivityStopped(Activity activity) {
            ACTIVITY_STOP_COUNT++;
            Log.d(TAG, "onActivityStopped: "+ACTIVITY_STOP_COUNT);
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
            Log.d(TAG, "onActivitySaveInstanceState: ");
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            Log.d(TAG, "onActivityDestroyed: ");
//            ACTIVITY_START_COUNT=0;
//            ACTIVITY_RESUME_COUNT=0;
//            ACTIVITY_PAUSE_COUNT=0;
//            ACTIVITY_STOP_COUNT=0;
        }
    }
}
