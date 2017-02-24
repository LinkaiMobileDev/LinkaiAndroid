package com.linkai.app.services;

import android.app.IntentService;
import android.content.Intent;

import com.linkai.app.libraries.Common;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.libraries.FileHandler;
import com.linkai.app.modals.ChatMessage;
import com.linkai.app.modals.GroupMessage;

public class DownloadFileService extends IntentService {

    public static boolean IS_ALIVE=false;
    Common common;
    FileHandler fileHandler;
    DatabaseHandler db;

    public DownloadFileService() {
        super("DownloadFileService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        common=new Common(this.getApplicationContext());
        fileHandler=new FileHandler(this.getApplicationContext());
        db=Const.DB;
        if(!IS_ALIVE){
            IS_ALIVE=true;
        }
        if (intent != null) {
            Const.CHATBOX_TYPE chatbox_type= (Const.CHATBOX_TYPE) intent.getSerializableExtra("chatbox_type");
            if(chatbox_type== Const.CHATBOX_TYPE.SINGLE){
                ChatMessage msg=db.getMessageById(intent.getStringExtra("msg_id"));
                if(fileHandler.downloadFile(msg)) {
//            send broadcast to refresh chat box if file downloaded successfully
                    this.sendBroadcast(new Intent("chat.view.refresh"));
                }
                else{
                    this.sendBroadcast(new Intent("chat.view.refresh"));
                }
            }
            else if(chatbox_type== Const.CHATBOX_TYPE.GROUP){
                GroupMessage msg=db.getGroupMessageById(intent.getStringExtra("msg_id"));
                if(fileHandler.downloadFile(msg)) {
//            send broadcast to refresh chat box if file downloaded successfully
                    this.sendBroadcast(new Intent("chat.view.refresh"));
                }
                else{
                    this.sendBroadcast(new Intent("chat.view.refresh"));
                }
            }

        }
    }

    @Override
    public void onDestroy() {
        if(IS_ALIVE){
            IS_ALIVE=false;
        }
    }


}
