package com.linkai.app;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.linkai.app.R;

import com.linkai.app.design.RoundedImageView;
import com.linkai.app.libraries.Common;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.libraries.FileHandler;
import com.linkai.app.libraries.MyXMPP;
import com.linkai.app.modals.ChatFriend;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class LinkaiPinEntryActivity extends AppCompatActivity {
    private final String TAG="LinkaiPinEntryActivity";
    private Context context;
    private String chatId;
    private ChatFriend friend;
    private DatabaseHandler db;
    private FileHandler fileHandler;
    private MyXMPP xmpp;
    private Common common;
    private BroadcastReceiver chatBroadReceiver;
    LocalBroadcastManager localBroadcastManager;
    SharedPreferences prefs;
    Map<String,String> headers;

    private TextView txtName;
    private TextView txtStatus;
    private RoundedImageView imgProfileThumb;
    private Button btnContinue;
    private Toolbar toolbar;
    private EditText txtPwd;

    ProgressDialog progressDialog;

    private boolean IS_ONLINE=false;
    private boolean IS_TYPING=false;
    private final String STATUS_ONLINE="online";
    private final String STATUS_TYPING="typing...";
    private final String STATUS_OFFLINE="";

    private JSONObject json_trans_request;

    private final int SET_PIN_REQUEST=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linkai_pin_entry);
        context=this.getApplicationContext();
        db= Const.DB;
        fileHandler=new FileHandler(context);
        xmpp=((ChatApplication)getApplication()).getMyXMPPInstance();
        common=new Common(context);
        localBroadcastManager=LocalBroadcastManager.getInstance(context);
        prefs = context.getSharedPreferences(Const.LINKAI_SHAREDPREFERENCE_FILE, Context.MODE_PRIVATE);
//        actionbar
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtName= (TextView) findViewById(R.id.txtName);
        txtStatus= (TextView) findViewById(R.id.txtStatus);
        imgProfileThumb= (RoundedImageView) findViewById(R.id.imgProfileThumb);
        btnContinue= (Button) findViewById(R.id.btnContinue);
        txtPwd= (EditText) findViewById(R.id.txtPwd);

        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        chatId=getIntent().getStringExtra("chatId");
        friend=db.getFriendByJabberId(chatId);

        txtName.setText(friend.getName());
        //        setting the status of friend
        IS_ONLINE=xmpp.isOnline(chatId);
        setChatAvailabilityStatus();
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

//        checking if PIN already set
        if(!prefs.getBoolean("isPinSet",false)){
//            if not set goto set pin activity
            Intent setPinIntent=new Intent(context,LinkaiSetPinActivity.class);
            startActivityForResult(setPinIntent,SET_PIN_REQUEST);
        }

//        continue button event
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Continue();

            }
        });

    }

    @Override
    public void onResume() {
        Const.CUR_ACTIVITY= Const.APP_COMPONENTS.LinkaiPinEntryActivity;
        super.onResume();

        chatBroadReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Toast.makeText(context,"received-"+intent.getAction(),Toast.LENGTH_SHORT).show();
//                loadChatBoxMessages();
                switch (intent.getAction()){
                    case "chat.presence.changed":
//                        Log.d(TAG, "onReceive: "+intent.getStringExtra("from")+"="+chatId+"-"+intent.getStringExtra("status"));
                        if(intent.getStringExtra("from").equals(chatId)){
                            IS_ONLINE=intent.getBooleanExtra("status",false);
                            setChatAvailabilityStatus();
                        }
                        break;
                    case "chat.chatstate.changed":
                        if(intent.getStringExtra("from").equals(chatId)){
                            IS_TYPING=intent.getBooleanExtra("status",false);
                            setChatAvailabilityStatus();
                        }
                        break;
                    default:break;
                }
            }
        };
        try{
            localBroadcastManager.registerReceiver(chatBroadReceiver,new IntentFilter("chat.presence.changed"));
            localBroadcastManager.registerReceiver(chatBroadReceiver,new IntentFilter("chat.chatstate.changed"));
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
//        try {
//            context.unregisterReceiver(chatBroadReceiver);
//        }
//        catch (Exception e){
//
//        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SET_PIN_REQUEST) {
            if(resultCode == RESULT_OK){
                String pin=data.getStringExtra("pin");
                txtPwd.setText(pin);
                Continue();
                Log.d(TAG, "onActivityResult: ok pin-"+pin);
            }
            if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "onActivityResult: canceled");
                finish();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            default:return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //    function to set friend's availability status
    private void setChatAvailabilityStatus(){
        if(IS_ONLINE || IS_TYPING){
            txtStatus.setVisibility(View.VISIBLE);
            txtStatus.setText(IS_ONLINE||IS_TYPING?(IS_TYPING?STATUS_TYPING:STATUS_ONLINE):STATUS_OFFLINE);
        }
        else{
            txtStatus.setVisibility(View.GONE);
        }

    }


    private boolean Continue(){
        boolean ret=false;
        ret=validatePin();
        if(ret){
            Intent intent=new Intent(context,LinkaiAmountEntryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("chatId",chatId);

            json_trans_request=new JSONObject();
            try {
                json_trans_request.put("pin",txtPwd.getText().toString());
//                json_trans_request.put("pin",);
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
            intent.putExtra("json_trans_request",json_trans_request.toString());
            startActivity(intent);
            finish();
        }
        return ret;
    }

    private boolean validatePin(){
        boolean ret=true;
        String pwd=txtPwd.getText().toString();
        if(pwd==null || pwd.equals("")){
            txtPwd.setError("Enter PIN");
            txtPwd.requestFocus();
            ret=false;
        }
        else if(!TextUtils.isDigitsOnly(pwd) || pwd.length()!=6)
        {
            txtPwd.setError("Invalid PIN");
            txtPwd.requestFocus();
            ret=false;
        }
        else{
            ret=true;
        }
        return ret;
    }


}
