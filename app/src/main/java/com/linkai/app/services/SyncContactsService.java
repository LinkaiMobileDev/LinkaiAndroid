package com.linkai.app.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.linkai.app.ChatApplication;
import com.linkai.app.libraries.Common;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.libraries.MyXMPP;
import com.linkai.app.modals.ChatFriend;

public class SyncContactsService extends IntentService {
    private String TAG="SyncContactsService";

    public static boolean IS_ALIVE=false;
    MyXMPP xmpp;
    DatabaseHandler db;
    Common common;
    Context context;

    public SyncContactsService() {
        super("SyncContactsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: ");
        xmpp=((ChatApplication)getApplication()).getMyXMPPInstance();
        db= Const.DB;
        common=new Common(this.getApplicationContext());
        context=this.getApplicationContext();
        if(!IS_ALIVE){
            IS_ALIVE=true;
        }
        if (intent != null) {
            try {
                ChatFriend.searchFriendsInServer(context);
                ChatFriend.refreshFriendList(context);
                ChatFriend.searchFriendsInServer(context);
//                wait until xmpp is connected to request subscription
                int check_count=0;
                while(xmpp==null || !xmpp.IS_CONNECTED || !xmpp.IS_LOGGEDIN){
                    check_count++;
                    try {
//                        Log.d(TAG, "waiting: ");
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(check_count>20){
                        return;
                    }
                }
                xmpp.requestSubscription();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }



    @Override
    public void onDestroy() {
        //Log.d("Send message service ", "onDestroy: ");
        if(IS_ALIVE){
            IS_ALIVE=false;
        }
    }

}
