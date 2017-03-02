package com.linkai.app;

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
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

import java.util.ArrayList;

public class LinkaiAmountEntryActivity extends AppCompatActivity {
    private final String TAG="LinkaiAmountEntry";

    private Context context;
    private String chatId;
    private ChatFriend friend;
    private DatabaseHandler db;
    private FileHandler fileHandler;
    private MyXMPP xmpp;
    private Common common;
    private BroadcastReceiver chatBroadReceiver;
    private LocalBroadcastManager localBroadcastManager;

    private EditText txtAmount;
    private Spinner spinnerCurrency;

    private TextView txtName;
    private TextView txtStatus;
    private RoundedImageView imgProfileThumb;
    private Toolbar toolbar;
    private Button btnContinue;

    String strTemp="";

    private boolean IS_ONLINE=false;
    private boolean IS_TYPING=false;
    private final String STATUS_ONLINE="online";
    private final String STATUS_TYPING="typing...";
    private final String STATUS_OFFLINE="";

    JSONObject json_trans_request;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linkai_amount_entry);
        context=this.getApplicationContext();
        db= Const.DB;
        fileHandler=new FileHandler(context);
        xmpp=((ChatApplication)getApplication()).getMyXMPPInstance();
        common=new Common(context);
        localBroadcastManager=LocalBroadcastManager.getInstance(context);
//        actionbar
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtAmount=(EditText) findViewById(R.id.txtAmount);
        spinnerCurrency=(Spinner)findViewById(R.id.spinnerCurrency);

        txtName= (TextView) findViewById(R.id.txtName);
        txtStatus= (TextView) findViewById(R.id.txtStatus);
        imgProfileThumb= (RoundedImageView) findViewById(R.id.imgProfileThumb);

        chatId=getIntent().getStringExtra("chatId");
        try {
            json_trans_request=new JSONObject(getIntent().getStringExtra("json_trans_request"));
        } catch (JSONException e) {
            e.printStackTrace();
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

        btnContinue=(Button)findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Continue();
            }
        });
//        setting value in currency spinner
        SharedPreferences prefs = context.getSharedPreferences(Const.LINKAI_SHAREDPREFERENCE_FILE, Context.MODE_PRIVATE);
        ArrayList<String> currencyList=new ArrayList<String>();
        currencyList.add(prefs.getString("currency","IQD"));
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(LinkaiAmountEntryActivity.this,android.R.layout.simple_spinner_dropdown_item, new ArrayList<String>().add(prefs.getString("currency","IQD")));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,R.layout.app_spinner,currencyList);
        adapter.setDropDownViewResource(R.layout.app_spinner);
        spinnerCurrency.setAdapter(adapter);

    }

    @Override
    public void onResume() {
        Const.CUR_ACTIVITY= Const.APP_COMPONENTS.LinkaiAmountEntryActivity;
        super.onResume();

        chatBroadReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Toast.makeText(context,"received-"+intent.getAction(),Toast.LENGTH_SHORT).show();
//                loadChatBoxMessages();
                switch (intent.getAction()){
                    case "chat.presence.changed":
                        //Log.d(TAG, "onReceive: "+intent.getStringExtra("from")+"="+chatId+"-"+intent.getStringExtra("status"));
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
        String amount=txtAmount.getText().toString();
        String currency=spinnerCurrency.getSelectedItem().toString();
        if(amount==null || amount.equals("")){
            txtAmount.setError("Enter amount");
            txtAmount.requestFocus();
            return;
        }
        try {
            json_trans_request.put("amount",amount);
            json_trans_request.put("currency",currency);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Intent linkaiFeesPaidActivity = new Intent(context, LinkaiFeesPaidByActivity.class);
        linkaiFeesPaidActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        linkaiFeesPaidActivity.putExtra("chatId",chatId);
        linkaiFeesPaidActivity.putExtra("json_trans_request",json_trans_request.toString());
        context.startActivity(linkaiFeesPaidActivity);
//        finish();
    }
}
