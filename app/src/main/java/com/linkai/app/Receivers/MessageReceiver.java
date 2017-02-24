package com.linkai.app.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.linkai.app.libraries.Common;

/**
 * Created by LP1001 on 05-10-2016.
 */

public class MessageReceiver extends BroadcastReceiver {
    private final String TAG="MessageReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Common common=new Common(context);
        switch (intent.getAction()){
            case "chat.message.received":
                common.showSingleChatNotification(intent.getStringExtra("id").trim());
                break;
            case "group.message.received":
                common.showGroupChatNotification(intent.getStringExtra("id"));
                break;

            default:break;
        }
//        Log.d(TAG, "onReceive: cue_act "+ Const.CUR_ACTIVITY);
    }
}
