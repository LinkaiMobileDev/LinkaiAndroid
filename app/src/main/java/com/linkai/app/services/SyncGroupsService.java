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
import com.linkai.app.modals.ChatGroup;

import java.util.ArrayList;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class SyncGroupsService extends IntentService {


    final String TAG="SyncGroupsService";

    public static boolean IS_ALIVE=false;
    Context context;
    MyXMPP xmpp;
    DatabaseHandler db;
    Common common;


    public SyncGroupsService() {
        super("SyncGroupsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: start");
        context=this.getApplicationContext();
        xmpp=((ChatApplication)getApplication()).getMyXMPPInstance();
        db=Const.DB;
        common=new Common(this.getApplicationContext());


        if(!IS_ALIVE){
            IS_ALIVE=true;
        }
        if (intent != null) {
//sync groups with server
            ChatGroup.syncGroupsWithServer(context);
//            send refresh broadcast
            context.sendBroadcast(new Intent("chat.view.refresh"));
//          join groups
            joinGroups();
        }
    }

//    function to join all groups
    public void joinGroups(){
        int check_count=0;
        while(!xmpp.IS_CONNECTED || !xmpp.IS_LOGGEDIN){
            try {
                check_count++;
                Thread.sleep(5000);
                if(check_count>10){
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //            getting all groups
        ArrayList<ChatGroup> groups=db.getGroups(Const.GROUP_STATUS.ACTIVE);
//            loop through all groups and join each
        for (ChatGroup group:groups ) {
            Log.d(TAG, "onHandleIntent: groupname -"+group.getName());
            xmpp.createOrJoinGroup(group);
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
