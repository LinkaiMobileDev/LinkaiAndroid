package com.linkai.app.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.linkai.app.ChatApplication;
import com.linkai.app.libraries.Common;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.libraries.MyXMPP;
import com.linkai.app.modals.ChatMessage;
import com.linkai.app.modals.GroupMessage;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SendMessageService extends IntentService {
    private final String TAG="SendMessageService";

    public static boolean IS_ALIVE=false;
    MyXMPP xmpp;
    DatabaseHandler db;
    Common common;
    LocalBroadcastManager localBroadcastManager;
//    array to hold recipients of delayed msgs. this is for stop sending further messages to the same recipients in the same loop
    List<String> delayedMsgRecipients=new ArrayList<>();


    public SendMessageService() {
        super("SendMessageService");

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: ");
        xmpp=((ChatApplication)getApplication()).getMyXMPPInstance();

//        xmpp=new MyXMPP(this);
//        xmpp.init();
//        xmpp.connectConnection();
        db= Const.DB;
        common=new Common(this.getApplicationContext());
        localBroadcastManager=LocalBroadcastManager.getInstance(this.getApplicationContext());

        if(!IS_ALIVE){
            IS_ALIVE=true;
        }
        if (intent != null && common.isNetworkAvailable()) {
//            sending single chat messages
            sendSingleChatMessages();
//            sending group messages
            sendGroupChatMessages();
//            sending read receipt or acknwoledging read status
            acknowledgeReadMessages();
        }
    }

//    for sending single chat messages only
    private void sendSingleChatMessages(){
        int msg_count=0;
        do{
//            clear delayed msg recipient list
            delayedMsgRecipients.clear();
//            getting all messages which is not yet send
            ArrayList<ChatMessage> messages=db.getMessages(DatabaseHandler.Message_Direction.OUTGOING, DatabaseHandler.Message_Type.ALL, DatabaseHandler.Message_Status.TOSENT, DatabaseHandler.File_Status.SUCCESS);
//    adding all sent messages ,but didn't got delivery, even after the recipient is online
            messages.addAll(db.getMessages(DatabaseHandler.Message_Direction.OUTGOING, DatabaseHandler.Message_Type.ALL, DatabaseHandler.Message_Status.SENT, DatabaseHandler.File_Status.SUCCESS,Const.FRIENDSLIST_ONLINE.toArray(new String[Const.FRIENDSLIST_ONLINE.size()])));

            for (ChatMessage message:messages) {
//                    wait until if xmpp  connected and  logged in and  authorized
                int check_count=0;
                while(!xmpp.IS_CONNECTED || !xmpp.IS_LOGGEDIN){
                    check_count++;
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(check_count>10){
                        return;
                    }
                }
                //Log.d("SendMessageService", "onHandleIntent:Sending msg");
                ChatMessage retMsgObj=null;
//                checking difference b/w current time and last message sent try time and message status is pending
                long diff=Math.abs(new Date().getTime()-message.getUTCDate().getTime())/1000;
                if((diff<30 && (message.getStatus()==ChatMessage.STATUS_PENDING || message.getStatus()==ChatMessage.STATUS_SENT)) || delayedMsgRecipients.contains(message.getTo()) ){
//                    adding recipient jid to list if not in list
                    if(!delayedMsgRecipients.contains(message.getTo())) {
                        delayedMsgRecipients.add(message.getTo());
//                        Log.d(TAG, "sendSingleChatMessages: delayed"+message.getTo()+"-"+message.getBody()+"--"+delayedMsgRecipients.size());
                    }
                    continue;
                }
//                    try to send message
//                check if the receiver is online
                if(xmpp.isOnlineOrAway(message.getTo())){
                    Log.d(TAG, "sendSingleChatMessages: "+message.getBody());
                        retMsgObj = xmpp.sendMsg(message);
                        retMsgObj.setStatus(ChatMessage.STATUS_PENDING);
                        retMsgObj.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                    //                    adding recipient jid to list
                        delayedMsgRecipients.add(message.getTo());
                }
                else{
                    retMsgObj=xmpp.sendMsg(message);
                    retMsgObj.setStatus(ChatMessage.STATUS_SENT);
                }

                //Toast.makeText(this,"sent message-"+retMsgObj.getBody(),Toast.LENGTH_SHORT).show();
//                    update message
                if(retMsgObj!=null) {
                    db.updateMessage(retMsgObj);
                }
                localBroadcastManager.sendBroadcast(new Intent("chat.view.refresh"));
            }
            msg_count=db.getCountOfMessages(null,DatabaseHandler.Message_Direction.OUTGOING, DatabaseHandler.Message_Type.ALL, DatabaseHandler.Message_Status.TOSENT, DatabaseHandler.File_Status.SUCCESS);
        }while(msg_count>0 && common.isNetworkAvailable() && xmpp.IS_CONNECTED && xmpp.IS_LOGGEDIN);
    }

//    for sending group chat messages
    private void sendGroupChatMessages(){
        int msg_count=0;
        do{
            //Log.d("SendMessageService", "onHandleIntent:Sending msg count="+msg_count);
//                getting all messages which is not yet send
            ArrayList<GroupMessage> messages=db.getGroupMessages(DatabaseHandler.Message_Direction.OUTGOING, DatabaseHandler.Message_Type.ALL, DatabaseHandler.Message_Status.UNSENT, DatabaseHandler.File_Status.SUCCESS);
            for (GroupMessage message:messages) {
//                    wait until if xmpp  connected and  logged in and  authorized
                int check_count=0;
                while(!xmpp.IS_CONNECTED || !xmpp.IS_LOGGEDIN){
                    check_count++;
                    // //Log.d("in message send service", "onHandleIntent: connection and loggedin check"+check_count);
                    if(check_count>1000000){
                        //Log.d("SendMessageService", "onHandleIntent:check finished"+check_count+" "+xmpp.IS_CONNECTED+" "+xmpp.IS_LOGGEDIN);
//                            return to avoid infinit looping
                        return;
                    }
                }
                //Log.d("SendMessageService", "onHandleIntent:Sending msg");
//                    try to send message
                GroupMessage retMsgObj=xmpp.sendGroupMessage(message);
                //Toast.makeText(this,"sent message-"+retMsgObj.getBody(),Toast.LENGTH_SHORT).show();
//                    update message
                db.updateGroupMessage(retMsgObj);
                localBroadcastManager.sendBroadcast(new Intent("chat.view.refresh"));
            }
            msg_count=db.getGroupMessagesCount(null,DatabaseHandler.Message_Direction.OUTGOING, DatabaseHandler.Message_Type.ALL, DatabaseHandler.Message_Status.UNSENT, DatabaseHandler.File_Status.SUCCESS);
        }while(msg_count>0 && common.isNetworkAvailable() && xmpp.IS_CONNECTED && xmpp.IS_LOGGEDIN);
    }

//    for sending read ack back to sender
    private void acknowledgeReadMessages(){
        int msg_count=0;
        do{
            //            getting all read but not acknowledged messages
            ArrayList<ChatMessage> messages=db.getMessages(DatabaseHandler.Message_Direction.INCOMING, DatabaseHandler.Message_Type.ALL, DatabaseHandler.Message_Status.READ, DatabaseHandler.File_Status.SUCCESS);
            for (ChatMessage message:messages) {
                if(xmpp.sendReadReceipt(message)){
                    message.setStatus(ChatMessage.STATUS_READ_ACK);
                    db.updateMessage(message);
                }
            }
            msg_count=db.getCountOfMessages(null,DatabaseHandler.Message_Direction.INCOMING, DatabaseHandler.Message_Type.ALL, DatabaseHandler.Message_Status.READ, DatabaseHandler.File_Status.SUCCESS);
        }while(msg_count>0 && common.isNetworkAvailable() && xmpp.IS_CONNECTED && xmpp.IS_LOGGEDIN);
    }

    @Override
    public void onDestroy() {
        //Log.d("Send message service ", "onDestroy: ");
        if(IS_ALIVE){
            IS_ALIVE=false;
        }
    }


}
