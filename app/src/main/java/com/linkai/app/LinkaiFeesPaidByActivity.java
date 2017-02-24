package com.linkai.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
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

public class LinkaiFeesPaidByActivity extends AppCompatActivity {
    private final String TAG="LinkaiFeesPaid";

    private Context context;
    private String chatId;
    private ChatFriend friend;
    private DatabaseHandler db;
    private FileHandler fileHandler;
    private MyXMPP xmpp;
    private Common common;
    private BroadcastReceiver chatBroadReceiver;
    Resources res;

    private RadioGroup radioGp=null;

    private TextView txtName;
    private TextView txtStatus;
    private RoundedImageView imgProfileThumb;
    private Toolbar toolbar;

    private boolean IS_ONLINE=false;
    private boolean IS_TYPING=false;
    private final String STATUS_ONLINE="online";
    private final String STATUS_TYPING="typing...";
    private final String STATUS_OFFLINE="";

    private JSONObject json_trans_request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linkai_fees_paid_by);
        context=this.getApplicationContext();
        db= Const.DB;
        fileHandler=new FileHandler(context);
        xmpp=((ChatApplication)getApplication()).getMyXMPPInstance();
        common=new Common(context);
        res=context.getResources();
//        actionbar
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        radioGp = (RadioGroup)findViewById(R.id.radioGp);

        txtName= (TextView) findViewById(R.id.txtName);
        txtStatus= (TextView) findViewById(R.id.txtStatus);
        imgProfileThumb= (RoundedImageView) findViewById(R.id.imgProfileThumb);

        chatId=getIntent().getStringExtra("chatId");
        try {
            json_trans_request=new JSONObject(getIntent().getStringExtra("json_trans_request"));
        } catch (JSONException e) {
            e.printStackTrace();
            onBackPressed();
        }
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

        final Button btnContinue=(Button)findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Continue();
            }
        });


    }

    @Override
    public void onResume() {
        Const.CUR_ACTIVITY= Const.APP_COMPONENTS.LinkaiFeesPaidByActivity;
        super.onResume();

        chatBroadReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Toast.makeText(context,"received-"+intent.getAction(),Toast.LENGTH_SHORT).show();
//                loadChatBoxMessages();
                switch (intent.getAction()){
                    case "chat.presence.changed":
                        Log.d(TAG, "onReceive: "+intent.getStringExtra("from")+"="+chatId+"-"+intent.getStringExtra("status"));
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
            this.registerReceiver(chatBroadReceiver,new IntentFilter("chat.presence.changed"));
            this.registerReceiver(chatBroadReceiver,new IntentFilter("chat.chatstate.changed"));
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

    private void Continue(){
        int rdCheckedId=radioGp.getCheckedRadioButtonId();
        if(rdCheckedId==-1){
            AlertDialog alertBuilder=new AlertDialog.Builder(LinkaiFeesPaidByActivity.this).create();
            alertBuilder.setTitle(res.getString(R.string.fees_paidby_alert_title_choose_payment_option));
            alertBuilder.setMessage(res.getString(R.string.fees_paidby_alert_content_choose_payment_option));
            alertBuilder.setButton(AlertDialog.BUTTON_NEUTRAL, res.getString(R.string.alert_btn_ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertBuilder.show();
            return;
        }
        String fees_paid_by=rdCheckedId==R.id.first?"SENDER":(rdCheckedId==R.id.second)?"RECEIVER":"SHARED";
        try {
            json_trans_request.put("fees",fees_paid_by);
        } catch (JSONException e) {
            e.printStackTrace();
            onBackPressed();
        }
        Intent linkaiTransferTimeAct = new Intent(context, LinkaiTransferTimeActivity.class);
        linkaiTransferTimeAct.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        linkaiTransferTimeAct.putExtra("chatId",chatId);
        linkaiTransferTimeAct.putExtra("json_trans_request",json_trans_request.toString());
        context.startActivity(linkaiTransferTimeAct);
//        finish();
    }

}
