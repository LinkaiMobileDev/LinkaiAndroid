package com.linkai.app.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.linkai.app.ChatApplication;
import com.linkai.app.Utils.LinkaiUtils;
import com.linkai.app.libraries.Common;
import com.linkai.app.libraries.MyXMPP;

public class NetworkStateReceiver extends BroadcastReceiver {

    MyXMPP xmpp;
    public NetworkStateReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
//        Toast.makeText(context,"network changed",Toast.LENGTH_LONG).show();
        try {
//        xmpp object
            xmpp = ((ChatApplication) context.getApplicationContext()).getMyXMPPInstance();
            Common common = new Common(context);
//            Log.d("app", "Network connectivity change");
            if (intent.getExtras() != null) {
                NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
//                    Log.i("app", "Network " + ni.getTypeName() + " connected");
//                connect to xmpp server
                    xmpp.connectConnection();
//                init services
                    common.initServices();
//                    start main service
                    common.startMainService();
//                    call function to get balance
                    new LinkaiUtils(context).getBalance();

                }
                else{
//                    stop main service
//                    common.stopMainService();
                }
            }
            if (intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
//                Log.d("app", "There's no network connectivity");
            }
        }
        catch (Exception e){

        }

    }
}
