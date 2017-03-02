package com.linkai.app.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.linkai.app.R;
import com.linkai.app.libraries.Common;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.libraries.FileHandler;
import com.linkai.app.modals.ChatMessage;
import com.linkai.app.modals.GroupMessage;

import java.io.File;
import java.util.ArrayList;


public class UploadFileService extends IntentService {
    private final String TAG="UploadFileService";

    public static boolean IS_ALIVE=false;
    DatabaseHandler db;
    Common common;
    LocalBroadcastManager localBroadcastManager;
    FileHandler fileHandler;
    String fileRoot;
    public UploadFileService() {
        super("UploadFileService");

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: ");
        db= Const.DB;
        common=new Common(this.getApplicationContext());
        fileHandler=new FileHandler(this.getApplicationContext());
        localBroadcastManager=LocalBroadcastManager.getInstance(this.getApplicationContext());
        fileRoot=Environment.getExternalStorageDirectory() + File.separator +this.getResources().getString(R.string.dir_sent)+File.separator;
        if(!IS_ALIVE){
            IS_ALIVE=true;
        }
        if (intent != null && common.isNetworkAvailable()) {
            localBroadcastManager.sendBroadcast(new Intent("chat.view.refresh"));
//            uploading single chat files
            uploadSingleChatFiles();
//            uploading groupChat files
            uploadGroupChatFiles();
        }
    }

//    to upload files transfered in single chat
    public void uploadSingleChatFiles(){
        int msg_count=0;
        do{
//                getting all messages which is not yet send
            ArrayList<ChatMessage> messages=db.getMessages(DatabaseHandler.Message_Direction.OUTGOING, DatabaseHandler.Message_Type.ALL_FILES, DatabaseHandler.Message_Status.UNSENT, DatabaseHandler.File_Status.PENDING);
            for (ChatMessage message:messages) {
                Log.d(TAG, "onHandleIntent: msgid-"+message.getID()+" filename-"+message.getFileStatus());
                String filePath=fileRoot+message.getFileName();
                int response_code= fileHandler.uploadFile(filePath);
                Log.d(TAG, "uploadSingleChatFiles: "+response_code);
                if(response_code==200){
                    message.setFileStatus(1);
                    db.updateMessage(message);
                    //            send broadcast to refresh chat box if file uploaded successfully
                    localBroadcastManager.sendBroadcast(new Intent("chat.view.refresh"));
//                        call send message_service
                    common.callSendMessageService();
                }

            }
            msg_count=db.getCountOfMessages(null,DatabaseHandler.Message_Direction.OUTGOING, DatabaseHandler.Message_Type.ALL_FILES, DatabaseHandler.Message_Status.UNSENT, DatabaseHandler.File_Status.PENDING);
            Log.d(TAG, "onHandleIntent: count"+msg_count);
        }while(msg_count>0 && common.isNetworkAvailable() );
    }

//    to upload files transfered in group chat
public void uploadGroupChatFiles(){
    int msg_count=0;
    do{
//                getting all messages which is not yet send
        ArrayList<GroupMessage> messages=db.getGroupMessages(DatabaseHandler.Message_Direction.OUTGOING, DatabaseHandler.Message_Type.ALL_FILES, DatabaseHandler.Message_Status.UNSENT, DatabaseHandler.File_Status.PENDING);
        for (GroupMessage message:messages) {
            Log.d("Uploadfile service", "onHandleIntent: msgid-"+message.getID()+" filename-"+message.getFileStatus());
//                    wait until if xmpp  connected and  logged in and  authorized
            String filePath=fileRoot+message.getFileName();
            int response_code= fileHandler.uploadFile(filePath);
            if(response_code==200){
                message.setFileStatus(1);
                db.updateGroupMessage(message);
                //            send broadcast to refresh chat box if file uploaded successfully
                localBroadcastManager.sendBroadcast(new Intent("chat.view.refresh"));
//                        call send message_service
                common.callSendMessageService();
            }

        }
        msg_count=db.getGroupMessagesCount(null,DatabaseHandler.Message_Direction.OUTGOING, DatabaseHandler.Message_Type.ALL_FILES, DatabaseHandler.Message_Status.UNSENT, DatabaseHandler.File_Status.PENDING);
        Log.d("Uploadfile service", "onHandleIntent: count"+msg_count);
    }while(msg_count>0 && common.isNetworkAvailable() );
}

    @Override
    public void onDestroy() {
        if(IS_ALIVE){
            IS_ALIVE=false;
        }
    }


}
