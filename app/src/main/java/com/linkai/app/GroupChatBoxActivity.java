package com.linkai.app;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
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
import com.linkai.app.adapters.GroupchatBoxListAdapter;
import com.linkai.app.design.RoundedImageView;
import com.linkai.app.libraries.Common;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.libraries.FileHandler;
import com.linkai.app.libraries.MyXMPP;
import com.linkai.app.modals.ChatGroup;
import com.linkai.app.modals.ChatMessage;
import com.linkai.app.modals.GroupMessage;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GroupChatBoxActivity extends AppCompatActivity {
    private final String TAG="GroupChatBoxActivity";

    private BroadcastReceiver chatBroadReceiver;
    private MyXMPP xmpp ;
    private Common common;
    private Context context;
    Resources res;
    DatabaseHandler db;
    FileHandler fileHandler;
    GroupchatBoxListAdapter groupchatBoxListAdapter=null;
    ArrayList<GroupMessage> messages=new ArrayList<GroupMessage>();

    ChatGroup group;
    private String groupId;
    GroupMessage SelectedMsg;

//    ImageButton btnAttach;
    ImageButton btnSendMsg;
    EditText txtMsg;
    ListView lstChatBoxMessages;
    Toolbar toolbar;
    TextView lblGroupName;
    TextView lblEventBalance;
    LinearLayout llGroupNamecontainer;
    ImageButton btnCapturePhoto;
    RoundedImageView imgProfileThumb;
    ImgClickListener imgClickListener=new ImgClickListener();
    AttachmentListener attachmentListener=new AttachmentListener();

    private int RESULT_LOAD_FILE=1;
    File camPhotoFile=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_box);
        Intent intentCurrent=getIntent();

//        btnAttach= (ImageButton) findViewById(R.id.btnAttach);
        imgProfileThumb= (RoundedImageView) findViewById(R.id.imgProfileThumb);
        btnSendMsg= (ImageButton) findViewById(R.id.btnSendMsg);
        txtMsg= (EditText) findViewById(R.id.txtMsg);
        lstChatBoxMessages= (ListView) findViewById(R.id.lstChatBox);
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        lblGroupName= (TextView) findViewById(R.id.lblGroupName);
        lblEventBalance=(TextView) findViewById(R.id.lblEventBalance);
        llGroupNamecontainer= (LinearLayout) findViewById(R.id.llGroupNamecontainer);
        btnCapturePhoto= (ImageButton) findViewById(R.id.btnCapturePhoto);

        context=this.getApplicationContext();
        res=context.getResources();
        xmpp=((ChatApplication)getApplication()).getMyXMPPInstance();
        db=Const.DB;
        common=new Common(context);
        fileHandler=new FileHandler(context);



        groupId=intentCurrent.getStringExtra("groupId");
        group=db.getGroup(groupId.trim());

        this.setSupportActionBar(toolbar);
        lblGroupName.setText(group.getName());
        lblGroupName.requestFocus();
        lblEventBalance.setText(" "+group.getBalance()+" "+group.getTargetCurrency());

        Log.d(TAG, "onCreate: "+group.getBalance()+" "+group.getTargetCurrency());
        //        set member image
        if(group.getProfileThumb()!=null && !group.getProfileThumb().equals("")){
            Bitmap thumbBitmap=fileHandler.stringToBitmap(group.getProfileThumb());
            if(thumbBitmap!=null){
                imgProfileThumb.setImageBitmap(thumbBitmap);

            }
            else{
                imgProfileThumb.setImageResource(R.drawable.event_icon_100);
            }
        }
        else{
            Const.EVENT_TYPE event_type= Const.EVENT_TYPE.getEventType(group.getEventType());
            if(event_type!=null && event_type.getImageResourceId(200)>0){
                imgProfileThumb.setImageResource(event_type.getImageResourceId(200));
            }
            else{
                imgProfileThumb.setImageResource(R.drawable.event_icon_100);
            }
        }

        //Sending message- button click event
        btnSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                checking if left from group
                Log.d(TAG, "onClick: "+group.getStatus()+"= "+Const.GROUP_STATUS.INACTIVE.getValue());
                if(group.getStatus()==Const.GROUP_STATUS.INACTIVE.getValue()){
                    AlertDialog.Builder alert=new AlertDialog.Builder(view.getContext());
                    alert.setTitle(res.getString(R.string.group_chatbox_alert_title_message_sending_denied));
                    alert.setMessage(res.getString(R.string.group_chatbox_alert_content_message_sending_denied));
                    alert.setPositiveButton(res.getString(R.string.alert_btn_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    Dialog alertDialog=alert.create();
                    alertDialog.show();
                    return;
                }

                String msg=txtMsg.getText().toString();
//                return if message is empty
                if(msg.trim().equals("")){
                    return;
                }
                GroupMessage message=new GroupMessage();
                message.setBody(msg);
                message.setGroupId(groupId);
                message.setFrom("self");
                message.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                message.setStatus(0);
//                saving msg obj in db
                db.addGroupMessage(message);
//                calling service to send message
                common.callSendMessageService();

                txtMsg.setText("");
//                txtMsg.setFocusable(false);
//                reloading messages
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

                }
                else{
                    //                    send notification is not typing

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Log.d("textchange event", "afterTextChanged: ");
            }
        });

        //btnattach listener to attach file


//        btnAttach.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent browse_file_intent=new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(browse_file_intent,RESULT_LOAD_FILE);
//            }
//        });

//        to see group profile
        llGroupNamecontainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(group.getStatus()==Const.GROUP_STATUS.INACTIVE.getValue()){
                    return;
                }
                Intent profileIntent=new Intent(context,GroupProfileActivity.class);
                profileIntent.putExtra("groupId",groupId);
                startActivity(profileIntent);
            }
        });

//clear notigfications of this group
        common.clearNotification(groupId);

    }

    @Override
    public void onStart() {
        super.onStart();
//        load messages
        loadChatBoxMessages();


    }

    @Override
    public void onResume() {
        Const.CUR_ACTIVITY= Const.APP_COMPONENTS.GroupChatBoxActivity;
        Const.CUR_GROUPID=groupId;
        super.onResume();

//        reloading group details
        group=db.getGroup(groupId.trim());

        chatBroadReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                Toast.makeText(context,"received-"+intent.getAction(),Toast.LENGTH_SHORT).show();
//                loadChatBoxMessages();
                switch (intent.getAction()){
                    case "group.message.received":
                        if(intent.getStringExtra("groupId").trim().equals(groupId)) {
                            common.playMessageToneLow();
                            common.clearNotification(groupId);
                            loadChatBoxMessages();
                        }
                        break;
                    case "group.notification.received":
//                        Log.d(TAG, "onReceive: received notification broadcast");
                        loadChatBoxMessages();
                        break;
                    case "chat.view.refresh":
//                        reload chatbox content
                            group=db.getGroup(groupId);
                            loadChatBoxMessages();
                        break;
                    default:break;
                }
            }
        };

        try {
            this.registerReceiver(chatBroadReceiver, new IntentFilter("group.message.received"));
            this.registerReceiver(chatBroadReceiver, new IntentFilter("group.notification.received"));
            this.registerReceiver(chatBroadReceiver, new IntentFilter("chat.view.refresh"));
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
            context.unregisterReceiver(chatBroadReceiver);
        }
        catch (Exception e){

        }
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
        getMenuInflater().inflate(R.menu.menu_chat_box,menu);
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
        else if(item.getItemId()==R.id.action_clear_chat){
//            delete all group messages
            db.deleteAllGroupMessages(groupId);
//            set group status date with current date time.  to avoid receiving cleared messages
            group.setStatusDate(new Date());
//            update group
            db.updateGroup(group);
//            reload messages
            loadChatBoxMessages();
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info= (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.add(info.position,0,0,"Copy");
        menu.add(info.position,1,0, "Delete");
        SelectedMsg= (GroupMessage) groupchatBoxListAdapter.getItem(info.position);

        //Log.d(TAG, "onCreateContextMenu: position-"+info.position+" name-"+member.getName());

        //txtMsg.setText(clipboard.getText());
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.d("oncontextselected", "" + item.getItemId());
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
                    db.deleteGroupMessage(SelectedMsg);
                    loadChatBoxMessages();

                }
                catch (Exception ex){
                    Log.d("Exception", "onContextItemSelected: " + ex.getMessage());
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

//    to load all chat history
    public void loadChatBoxMessages(){
        messages.clear();
        messages.addAll(db.getMessagesFromGroup(groupId));
        if(groupchatBoxListAdapter==null) {
            groupchatBoxListAdapter = new GroupchatBoxListAdapter(this, messages, imgClickListener);
            lstChatBoxMessages.setAdapter(groupchatBoxListAdapter);
            registerForContextMenu(lstChatBoxMessages);
        }
        else{
            groupchatBoxListAdapter.notifyDataSetChanged();
        }
        // update all incoming msg_status to read
        db.updateGroupMessageStatus(groupId, DatabaseHandler.Message_Direction.INCOMING, DatabaseHandler.Message_Status.READ);
    }

    //    click listener for chat list image()
    private class ImgClickListener implements GroupchatBoxListAdapter.ImageClickListener{
        @Override
        public void onImageClick(int position,View view) {
//            get msg obj at position
            GroupMessage msg= (GroupMessage) groupchatBoxListAdapter.getItem(position);
            //Toast.makeText(context,msg.getFileName()+"-"+msg.getID(),Toast.LENGTH_SHORT).show();
            if(!(msg.getFrom().equals("self")) && msg.getFileStatus()!=1) {

                common.callDownloadFileService("" + msg.getID(), Const.CHATBOX_TYPE.GROUP);
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
    }

    private class AttachmentListener implements AttachmentViewFragment.AttachmentSelectedListener {
        @Override
        public void imageSelected(String path) {
            GroupMessage msg=new GroupMessage();
            msg.setFrom("self");
            msg.setGroupId(groupId);
            msg.setBody("");
            msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
            msg.setStatus(0);
            msg.setType(ChatMessage.TYPE_IMAGE);
            msg.setFileStatus(0);
//        generate file name
            String filename=groupId.replace("+","")+"_"+fileHandler.generateFileName("jpg");
//        set msg obj filename
            msg.setFileName(filename);
//        resize and copy file to the app folder with specific name
            String outpath=Environment.getExternalStorageDirectory() + File.separator + res.getString(R.string.dir_sent) + File.separator +filename;
            if( !fileHandler.copyAndResizeImage(path,outpath) ){
                return ;
            }
//        get thumb image
            String thumb=fileHandler.getThumbImage(filename);
//        msg obj set thumb
            msg.setThumb(thumb);
//        save message
            db.addGroupMessage(msg);

            //        start upload file service
            common.callUploadFileService();
            //                refresing listview
            loadChatBoxMessages();

        }

        @Override
        public void videoSelected(String path) {
            Log.d(TAG, "videoSelected: "+path);
            File videoFile=new File(path);
            if (!videoFile.exists() || videoFile.length()>(10*1024*1024)){
                Toast.makeText(context,"File of size greater than 10 MB is not allowed to transfer",Toast.LENGTH_SHORT).show();
                return;
            }
            GroupMessage msg=new GroupMessage();
            msg.setFrom("self");
            msg.setGroupId(groupId);
            msg.setBody("");
            msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
            msg.setStatus(0);
            msg.setType(ChatMessage.TYPE_VIDEO);
            msg.setFileStatus(0);
            //        generate file name
            String filename=groupId.replace("+","")+"_"+fileHandler.generateFileName(path.substring(path.lastIndexOf('.')+1));
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
//        msg obj set thumb
            msg.setThumb(thumb);
//        save message
            db.addGroupMessage(msg);

            //        start upload file service
            common.callUploadFileService();
            //                refresing listview
            loadChatBoxMessages();
        }

        @Override
        public void audioSelected(String path) {
            File audioFile=new File(path);
            if (!audioFile.exists() || audioFile.length()>(10*1024*1024)){
                Toast.makeText(context,"File of size greater than 10 MB is not allowed to transfer",Toast.LENGTH_SHORT).show();
                return;
            }
            GroupMessage msg=new GroupMessage();
            msg.setFrom("self");
            msg.setGroupId(groupId);
            msg.setBody("");
            msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
            msg.setStatus(0);
            msg.setType(ChatMessage.TYPE_AUDIO);
            msg.setFileStatus(0);
            //        generate file name
            String filename=groupId.replace("+","")+"_"+fileHandler.generateFileName(path.substring(path.lastIndexOf('.')+1));
//        set msg obj filename
            msg.setFileName(filename);
//      copying file to sent
            File dst=new File(Environment.getExternalStorageDirectory() + File.separator + res.getString(R.string.dir_sent) + File.separator +filename);
            if(!fileHandler.copyFile(audioFile,dst)){
                return;
            }
//        save message
            db.addGroupMessage(msg);
            //        start upload file service
            common.callUploadFileService();
            //                refresing listview
            loadChatBoxMessages();
        }
    }
}
