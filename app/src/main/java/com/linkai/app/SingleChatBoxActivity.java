package com.linkai.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.linkai.app.Fragments.AttachmentViewFragment;

import com.linkai.app.R;

import com.linkai.app.adapters.ChatBoxListAdapter;
import com.linkai.app.design.RoundedImageView;
import com.linkai.app.libraries.Common;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.libraries.FileHandler;
import com.linkai.app.libraries.MyXMPP;
import com.linkai.app.modals.ChatFriend;
import com.linkai.app.modals.ChatMessage;
import com.linkai.app.modals.Transfer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SingleChatBoxActivity extends AppCompatActivity {
    private final String TAG="SingleChatBoxActivity";

    //creating broadcast receiver
    private BroadcastReceiver chatBroadReceiver;
    private LocalBroadcastManager localBroadcastManager;
    private MyXMPP xmpp ;
    private Common common;
    private Context context;
    private Resources res;
    DatabaseHandler db;
    FileHandler fileHandler;
    ChatBoxListAdapter adapter=null;
    ArrayList<ChatMessage> chatMessages=new ArrayList<ChatMessage>();

    ChatFriend friend;
    private String chatId;

    ChatMessage SelectedMsg;

    RoundedImageView imgProfileThumb;
//    ImageButton btnAttach;
    ImageButton btnSendMsg;
    EditText txtMsg;
    ListView lstChatBoxMessages;
    Toolbar toolbar;
    TextView lblChatFriendName;
    TextView lblAvailabilityStatus;
    ImageButton btnCapturePhoto;
    ClickListener imgClickListener=new ClickListener();
    LinearLayout llLogoButton;


    private boolean IS_ONLINE=false;
    private boolean IS_TYPING=false;
    private final String STATUS_ONLINE="online";
    private final String STATUS_TYPING="typing...";
    private final String STATUS_OFFLINE="";



    private int RESULT_LOAD_FILE=1;
    File camPhotoFile=null;
    AttachmentListener attachmentListener=new AttachmentListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_chat_box);
        Intent intentCurrent=getIntent();
        context=this.getApplicationContext();
        res=context.getResources();
        localBroadcastManager=LocalBroadcastManager.getInstance(context);

        imgProfileThumb= (RoundedImageView) findViewById(R.id.imgProfileThumb);
//        btnAttach= (ImageButton) findViewById(R.id.btnAttach);
        btnSendMsg= (ImageButton) findViewById(R.id.btnSendMsg);
        txtMsg= (EditText) findViewById(R.id.txtMsg);
        lstChatBoxMessages= (ListView) findViewById(R.id.lstChatBox);
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        lblChatFriendName= (TextView) findViewById(R.id.lblChatFriendName);
        lblAvailabilityStatus=(TextView) findViewById(R.id.lblAvailabilityStatus);
        btnCapturePhoto= (ImageButton) findViewById(R.id.btnCapturePhoto);
        llLogoButton= (LinearLayout) findViewById(R.id.llLogoButton);

        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        chatId=intentCurrent.getStringExtra("chatId");
        Log.d("Single chat", "onCreate: chatid-"+chatId);
        db=Const.DB;

        common=new Common(context);
        fileHandler=new FileHandler(context);

        friend=db.getFriendByJabberId(chatId);
//        friend=null;
        lblChatFriendName.setText(friend.getName());
//        set member image
        if(friend.getProfileThumb()!=null && !friend.getProfileThumb().equals("")){
            Bitmap thumbBitmap=fileHandler.stringToBitmap(friend.getProfileThumb());
            if(thumbBitmap!=null){
                imgProfileThumb.setImageBitmap(thumbBitmap);

            }
            else{
                imgProfileThumb.setImageResource(R.drawable.ic_user);
            }
        }
        else{
            imgProfileThumb.setImageResource(R.drawable.ic_user);
        }


        xmpp=((ChatApplication)getApplication()).getMyXMPPInstance();

//        creating chat
        xmpp.createChat(chatId);

        //        setting the status of friend
        IS_ONLINE=xmpp.isOnline(chatId);

        setChatAvailabilityStatus();

//Sending message- button click event
        btnSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String msg=txtMsg.getText().toString();
                if(msg.trim().equals("")){return;}
                ChatMessage msgObj=new ChatMessage();
                msgObj.setXmppId("");
                msgObj.setFrom("self");
                msgObj.setTo(chatId);
                msgObj.setBody(msg);
                msgObj.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                msgObj.setReceiptId(chatId+"-"+msgObj.getDate());

                db.addMessage(msgObj);
                txtMsg.setText("");
//                call to start background service to send messages
                common.callSendMessageService();
//                reload messaGE LIST
                loadChatBoxMessages();
            }
        });

//        event to capture image
        btnCapturePhoto.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = Environment.getExternalStorageDirectory() + File.separator + res.getString(R.string.dir_home) + File.separator+"JPEG_" + timeStamp + ".jpg";
                camPhotoFile=new File(imageFileName);
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(camPhotoFile));
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, Const.ATTACHMENT_TYPE.CAMERA.toInteger());
                }

            }
        });

//        sending notification:- textview on change event
        txtMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Log.d("textchange event", "beforeTextChanged: ");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Log.d("textchange event", "onTextChanged: ");
                if(charSequence.length()>0){
                    //                    send notification is typing
                    xmpp.sendChatStateNotification(chatId,true);
                }
                else{
                    //                    send notification is not typing
                    xmpp.sendChatStateNotification(chatId,false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Log.d("textchange event", "afterTextChanged: ");
            }
        });

        llLogoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent linkaiPinIntent=new Intent(context,LinkaiPinEntryActivity.class);
                linkaiPinIntent.putExtra("chatId",chatId);
                startActivity(linkaiPinIntent);
            }
        });


//clear notifications of this chat
        common.clearNotification(chatId);

    }

    @Override
    public void onStart() {
        super.onStart();
        loadChatBoxMessages();



    }

    @Override
    public void onResume() {
        super.onResume();
        Const.CUR_ACTIVITY= Const.APP_COMPONENTS.SingleChatBoxActivity;
        Const.CUR_CHATID=chatId;
        loadChatBoxMessages();

        chatBroadReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()){
                    case "chat.message.received":
                        if(intent.getStringExtra("from").trim().equals(chatId)){
                            common.playMessageToneLow();
                            loadChatBoxMessages();
                            common.clearNotification(chatId);
                        }
                        break;
                    case "chat.presence.changed":
//                        Log.d(TAG, "onReceive: "+intent.getStringExtra("from")+"="+chatId+"-"+intent.getStringExtra("status"));
                        if(intent.getStringExtra("from").equals(chatId)){
                            IS_ONLINE=intent.getBooleanExtra("status",false);
                            setChatAvailabilityStatus();
                        }
                        break;
                    case "chat.chatstate.changed":
//
                        if(intent.getStringExtra("from").equals(chatId)){
                            IS_TYPING=intent.getBooleanExtra("status",false);
                            setChatAvailabilityStatus();
                        }
                        break;
                    case "chat.view.refresh":
                    case "linkai.view.refresh":
//                        reload chatbox content
                        loadChatBoxMessages();
                        break;
                    default:break;
                }
            }
        };
        try{
            localBroadcastManager.registerReceiver(chatBroadReceiver,new IntentFilter("chat.message.received"));
            localBroadcastManager.registerReceiver(chatBroadReceiver,new IntentFilter("chat.presence.changed"));
            localBroadcastManager.registerReceiver(chatBroadReceiver,new IntentFilter("chat.chatstate.changed"));
            localBroadcastManager.registerReceiver(chatBroadReceiver,new IntentFilter("chat.view.refresh"));
            localBroadcastManager.registerReceiver(chatBroadReceiver,new IntentFilter("linkai.view.refresh"));
        }catch (Exception e){
            e.printStackTrace();
        }
       // this.registerReceiver(chatBroadReceiver,new IntentFilter("chat.roster.received"));

    }

    @Override
    public void onPause() {
        super.onPause();

//        UNREGISTER BROADCAST
        try {
            localBroadcastManager.unregisterReceiver(chatBroadReceiver);
        }
        catch (Exception e){

        }

        //        send notification istyping false
        xmpp.sendChatStateNotification(chatId,false);
    }

    @Override
    public void onStop() {
        super.onStop();
        //        UNREGISTER BROADCAST
        try {
            context.unregisterReceiver(chatBroadReceiver);
        }
        catch (Exception e){

        }

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //        back to home page and clear history
        Intent homeIntent=new Intent(context,HomeActivity.class);
//        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_single_chat_box,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_attachment){
//            Intent browse_file_intent=new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            startActivityForResult(browse_file_intent,RESULT_LOAD_FILE);
            AttachmentViewFragment attachmentViewFragment=new AttachmentViewFragment();
            attachmentViewFragment.show(getSupportFragmentManager(),attachmentViewFragment.getTag());
            attachmentViewFragment.setAttachmentListener(attachmentListener);
        }
        else if(item.getItemId()==R.id.action_linkai){
            Intent linkaiPinIntent=new Intent(context,LinkaiPinEntryActivity.class);
            linkaiPinIntent.putExtra("chatId",chatId);
            startActivity(linkaiPinIntent);
        }
        else if(item.getItemId()==R.id.action_clear_chat){
            db.deleteAllMessages(chatId);
            loadChatBoxMessages();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info= (AdapterView.AdapterContextMenuInfo) menuInfo;
        SelectedMsg= (ChatMessage) adapter.getItem(info.position);
        if(SelectedMsg.getType().equals(ChatMessage.TYPE_TEXT)) {
            menu.add(info.position, 0, 0, R.string.text_copy);
        }
        menu.add(info.position,1,0, R.string.text_delete);

        //Log.d(TAG, "onCreateContextMenu: position-"+info.position+" name-"+member.getName());

        //txtMsg.setText(clipboard.getText());
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.d("oncontextselected", ""+item.getItemId());
        switch(item.getItemId()) {
            case 0:
                try{
                    //Toast.makeText(this, "Copy selected 1", Toast.LENGTH_SHORT).show();
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    clipboard.setText(SelectedMsg.getBody().toString());
                }
                catch(Exception ex){
                    Log.d("Exception", "onContextItemSelected: "+ex.getMessage());
                }
                break;
            case 1:
                //Toast.makeText(this, "Delete selected 2", Toast.LENGTH_SHORT).show();
                try{
                    db.deleteMessage(SelectedMsg);
                    loadChatBoxMessages();

                }
                catch (Exception ex){
                    Log.d("Exception", "onContextItemSelected: "+ex.getMessage());
                }
                break;
            default:

        }

        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Const.ATTACHMENT_TYPE.CAMERA.toInteger() && resultCode == RESULT_OK) {
            if(camPhotoFile!=null){
                String path=camPhotoFile.getPath();
                attachmentListener.imageSelected(path);
                camPhotoFile.delete();
                camPhotoFile=null;
            }
        }
        else{
            super.onActivityResult(requestCode,resultCode,data);
        }

    }

    public void loadChatBoxMessages(){
        //        Listing All messages of the current chatBox
        chatMessages.clear();
        chatMessages.addAll(db.getMessagesFromChat(chatId));

        if(adapter==null) {
            adapter = new ChatBoxListAdapter(this, chatMessages, imgClickListener);
            lstChatBoxMessages.setAdapter(adapter);
            registerForContextMenu(lstChatBoxMessages);
//            Log.d(TAG, "loadChatBoxMessages: null");
        }
        else{
            adapter.notifyDataSetChanged();
        }

        // update all incoming msg_status to read
        if(!common.isRunningInBackground()){
            db.updateAllMessageStatus(chatId, DatabaseHandler.Message_Direction.INCOMING, DatabaseHandler.Message_Status.READ, DatabaseHandler.Message_Status.UNREAD);
        }

        //        check if last incoming message's status is read
        if(chatMessages.size()>0){
            Log.d(TAG, "loadChatBoxMessages: "+chatMessages.get(chatMessages.size()-1).getFrom()+"-"+chatMessages.get(chatMessages.size()-1).getStatus());
            if(!chatMessages.get(chatMessages.size()-1).getFrom().equals("self") && chatMessages.get(chatMessages.size()-1).getStatus()==ChatMessage.STATUS_READ){
//                call send message service
                Log.d(TAG, "loadChatBoxMessages: SendMessageService");
                common.callSendMessageService();
            }
        }
    }


//    function to set friend's availability status
    private void setChatAvailabilityStatus(){
        if(IS_ONLINE || IS_TYPING){
            lblAvailabilityStatus.setVisibility(View.VISIBLE);
            lblAvailabilityStatus.setText(IS_ONLINE||IS_TYPING?(IS_TYPING?STATUS_TYPING:STATUS_ONLINE):STATUS_OFFLINE);
        }
        else{
            lblAvailabilityStatus.setVisibility(View.GONE);
        }

    }

    //    click listener for chat list image()
    private class ClickListener implements ChatBoxListAdapter.ClickListener{
        @Override
        public void onFileClick(int position,View view) {
//            get msg obj at position
            ChatMessage msg= (ChatMessage) adapter.getItem(position);
            //Toast.makeText(context,msg.getFileName()+"-"+msg.getID(),Toast.LENGTH_SHORT).show();
            if(!(msg.getFrom().equals("self")) && msg.getFileStatus()!=1) {

                common.callDownloadFileService("" + msg.getID(), Const.CHATBOX_TYPE.SINGLE);
//               show progressbar
                if(common.isNetworkAvailable()) {
                    view.findViewById(R.id.prImageProgress).setVisibility(View.VISIBLE);
                }
                else{
                    Toast.makeText(context,"Turn on internet",Toast.LENGTH_SHORT).show();
                }
            }
            else{
                fileHandler.openFile(fileHandler.getFilePath(msg));
            }
        }

        @Override
        public void onTransferAcceptClick(int position, boolean isAccepted) {
            ChatMessage msg= (ChatMessage) adapter.getItem(position);
//            move to confirmation page if accepted
            if(isAccepted){
                Intent intent=new Intent(context,LinkaiTransferConfirmationActivity.class);
                intent.putExtra("chatId",chatId);
                intent.putExtra("msgId",msg.getID());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return;
            }
            Transfer transfer;
            JSONObject transferJson= null;
            try {
                transferJson = new JSONObject(msg.getBody());
                transfer=db.getTransfer(transferJson.getString("transfer_id"));
//                    transfer rejected
                    transfer.setTransferStatus(Const.TRANSFER_STATUS.REJECTED);

//                try to update transfer
                if(db.updateTransfer(transfer)>0){
//                    send message to transfer partner by updating status
                    ChatMessage msgObj=new ChatMessage();
                    msgObj.setXmppId("");
                    msgObj.setFrom("self");
                    msgObj.setTo(chatId);
                    msgObj.setBody(transfer.getJsonString());
                    msgObj.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                    msgObj.setReceiptId(chatId+"-"+msgObj.getDate());
                    msgObj.setType(ChatMessage.TYPE_TRANSFER);
                    msgObj.setStatus(ChatMessage.STATUS_UNSENT);
                    msgObj.setShowStatus(Const.MESSAGE_SHOW_STATUS.HIDE);
                    int lastid= (int) db.addMessage(msgObj);
                    if(lastid>0){

//                                call send message service
                        common.callSendMessageService();
                        loadChatBoxMessages();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


//listener for attachment selected
    private class AttachmentListener implements AttachmentViewFragment.AttachmentSelectedListener {
        @Override
        public void imageSelected(String path) {
            Log.d(TAG, "imageSelected: "+path);
            //                sending file
            ChatMessage msg = new ChatMessage();
            msg.setXmppId("");
            msg.setFrom("self");
            msg.setTo(chatId);
            msg.setBody("");
            msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
            msg.setType(ChatMessage.TYPE_IMAGE);
            msg.setFileStatus(0);
            msg.setReceiptId(chatId+"-"+msg.getDate());
//            Log.d(TAG, "imageSelected: chatid "+chatId);
//        generate file name
            String filename=chatId.replace("+","")+"_"+fileHandler.generateFileName("jpg");
//        set msg obj filename
            msg.setFileName(filename);
//        resize and copy file to the app folder with specific name
            String outpath=Environment.getExternalStorageDirectory() + File.separator + res.getString(R.string.dir_sent) + File.separator +filename;
            if( !fileHandler.copyAndResizeImage(path,outpath) ){
                return;
            }
//        get thumb image
            String thumb=fileHandler.getThumbImage(filename);
//        msg obj set thumb
            msg.setThumb(thumb);
//        save message
            db.addMessage(msg);
            //        start upload file service
            common.callUploadFileService();
//                refresing listview
            loadChatBoxMessages();

        }

//    public void cameraCaptured(String path){
//        ChatMessage msg = new ChatMessage();
//        msg.setXmppId("");
//        msg.setFrom("self");
//        msg.setTo(chatId);
//        msg.setBody("");
//        msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
//        msg.setType(ChatMessage.TYPE_IMAGE);
//        msg.setFileStatus(0);
////        generate file name
//        String filename=chatId.replace("+","")+"_"+fileHandler.generateFileName("jpg");
////        set msg obj filename
//        msg.setFileName(filename);
////        resize and copy file to the app folder with specific name
//        if( !fileHandler.copyandResizeImage(path,filename) ){
//            return;
//        }
////        get thumb image
//        String thumb=fileHandler.getThumbImage(filename);
////        msg obj set thumb
//        msg.setThumb(thumb);
////        save message
//        db.addMessage(msg);
//        //        start upload file service
//        common.callUploadFileService();
////                refresing listview
//        loadChatBoxMessages();
//    }

    @Override
    public void videoSelected(String path) {
        Log.d(TAG, "videoSelected: "+path);
        File videoFile=new File(path);
        if (!videoFile.exists() || videoFile.length()>(10*1024*1024)){
            Toast.makeText(context,"File of size greater than 10 MB is not allowed to transfer",Toast.LENGTH_SHORT).show();
            return;
        }
//        creating message object
        ChatMessage msg = new ChatMessage();
        msg.setXmppId("");
        msg.setFrom("self");
        msg.setTo(chatId);
        msg.setBody("");
        msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        msg.setType(ChatMessage.TYPE_VIDEO);
        msg.setFileStatus(0);
        msg.setReceiptId(chatId+"-"+msg.getDate());
        //        generate file name
        String filename=chatId.replace("+","")+"_"+fileHandler.generateFileName(path.substring(path.lastIndexOf('.')+1));
//        set msg obj filename
        msg.setFileName(filename);
//      copying file to sent
        File dst=new File(Environment.getExternalStorageDirectory() + File.separator + res.getString(R.string.dir_sent) + File.separator +filename);
        if(!fileHandler.copyFile(videoFile,dst)){
            return;
        }
//        create thumb of video
        String thumb=fileHandler.getThumbOfVideo(dst.getPath(),false);
        Log.d(TAG, "videoSelected:path-"+dst.getPath()+" thumb-"+thumb);
        msg.setThumb(thumb);
//        add message to db
        db.addMessage(msg);
        //        start upload file service
        common.callUploadFileService();
        //                refresing listview
        loadChatBoxMessages();

    }

    @Override
    public void audioSelected(String path) {
        Log.d(TAG, "audioSelected: "+path);
        File audioFile=new File(path);
        if (!audioFile.exists() || audioFile.length()>(10*1024*1024)){
            Toast.makeText(context,"File of size greater than 10 MB is not allowed to transfer",Toast.LENGTH_SHORT).show();
            return;
        }
        //        creating message object
        ChatMessage msg = new ChatMessage();
        msg.setXmppId("");
        msg.setFrom("self");
        msg.setTo(chatId);
        msg.setBody("");
        msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        msg.setType(ChatMessage.TYPE_AUDIO);
        msg.setFileStatus(0);
        msg.setReceiptId(chatId+"-"+msg.getDate());
        //        generate file name
        String filename=chatId.replace("+","")+"_"+fileHandler.generateFileName(path.substring(path.lastIndexOf('.')+1));
//        set msg obj filename
        msg.setFileName(filename);
        //      copying file to sent
        File dst=new File(Environment.getExternalStorageDirectory() + File.separator + res.getString(R.string.dir_sent) + File.separator +filename);
        if(!fileHandler.copyFile(audioFile,dst)){
            return;
        }
        //        add message to db
        db.addMessage(msg);
        //        start upload file service
        common.callUploadFileService();
        //                refreshing listview
        loadChatBoxMessages();
    }
}
}
