package com.linkai.app;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v4.content.LocalBroadcastManager;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.linkai.app.R;

import com.linkai.app.Utils.LinkaiRequestHeader;
import com.linkai.app.Utils.SimpleJsonCallback;
import com.linkai.app.design.RoundedImageView;
import com.linkai.app.libraries.Common;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.libraries.FileHandler;
import com.linkai.app.Utils.LinkaiUtils;
import com.linkai.app.libraries.MyXMPP;
import com.linkai.app.modals.ChatFriend;
import com.linkai.app.modals.ChatMessage;
import com.linkai.app.modals.ChatUser;
import com.linkai.app.modals.Transfer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class LinkaiTransferTimeActivity extends AppCompatActivity {

    private final String TAG="LinkaiTransferTime";

    private Context context;
    private String chatId;
    private ChatFriend friend;
    private DatabaseHandler db;
    private FileHandler fileHandler;
    private MyXMPP xmpp;
    ChatUser user;
    private Common common;
    Resources res;

    private TextView txtName;
    private TextView txtStatus;
    private RoundedImageView imgProfileThumb;
    private Toolbar toolbar;
    private RadioGroup radioGp=null;
    private Button btnContinue;
    private BroadcastReceiver chatBroadReceiver;
    LocalBroadcastManager localBroadcastManager;

    ProgressDialog progressDialog;

    private boolean IS_ONLINE=false;
    private boolean IS_TYPING=false;
    private final String STATUS_ONLINE="online";
    private final String STATUS_TYPING="typing...";
    private final String STATUS_OFFLINE="";

    JSONObject json_trans_request;
    Map<String,String> headers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linkai_transfer_time);
        context=this.getApplicationContext();
        db= Const.DB;
        fileHandler=new FileHandler(context);
        xmpp=((ChatApplication)getApplication()).getMyXMPPInstance();
        common=new Common(context);
        user=db.getUser();
        res=context.getResources();
        localBroadcastManager=LocalBroadcastManager.getInstance(context);
//        actionbar
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtName= (TextView) findViewById(R.id.txtName);
        txtStatus= (TextView) findViewById(R.id.txtStatus);
        imgProfileThumb= (RoundedImageView) findViewById(R.id.imgProfileThumb);
        radioGp= (RadioGroup) findViewById(R.id.radioGp);
        btnContinue= (Button) findViewById(R.id.btnContinue);

        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        chatId=getIntent().getStringExtra("chatId");
        friend=db.getFriendByJabberId(chatId);
        try {
            json_trans_request=new JSONObject(getIntent().getStringExtra("json_trans_request"));
            json_trans_request.put("receiver_phone_number",friend.getPhone());
            json_trans_request.put("motive","");
            json_trans_request.put("sender_account",user.getLinakiAccount());
            Log.d(TAG, "onCreate: json-data "+json_trans_request.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            onBackPressed();
        }

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

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeTransfer();
            }
        });
    }

    @Override
    protected void onResume() {
        Const.CUR_ACTIVITY= Const.APP_COMPONENTS.LinkaiTransferTimeActivity;
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

    private void executeTransfer(){
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
            int rdCheckedId = radioGp.getCheckedRadioButtonId();
            if (rdCheckedId == -1) {
                AlertDialog alertBuilder = new AlertDialog.Builder(LinkaiTransferTimeActivity.this).create();
                alertBuilder.setTitle(res.getString(R.string.transfer_time_alert_title_choose_time));
                alertBuilder.setMessage(res.getString(R.string.transfer_time_alert_content_choose_time));
                alertBuilder.setButton(AlertDialog.BUTTON_NEUTRAL, res.getString(R.string.alert_btn_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertBuilder.show();
                return;
            } else if (rdCheckedId == R.id.second) {
                json_trans_request.put("execution_date",dateFormat.format(new Date()).replace("-","/"));
            }
            else if (rdCheckedId == R.id.third) {
                json_trans_request.put("execution_date",dateFormat.format(new Date()).replace("-","/"));
            }
            else{
                json_trans_request.put("execution_date","");
            }
            String url=Const.LINKAI_TRANSFER_REQUEST;
            try {
                headers=new LinkaiRequestHeader(context).getRequestHeadersWithException();
            } catch (Exception e) {
                e.printStackTrace();
                LinkaiUtils linkaiUtils=new LinkaiUtils(context);
                showProgress(true,"Signing in..");
                linkaiUtils.signIn(new SimpleJsonCallback() {
                    @Override
                    public void onSuccess(JSONObject jsonObject) {
                        showProgress(false,null);
                        executeTransfer();
                    }

                    @Override
                    public void onError(JSONObject jsonObject) {
                        showProgress(false,null);
                        AlertDialog alertBuilder=new AlertDialog.Builder(LinkaiTransferTimeActivity.this).create();
                        alertBuilder.setTitle(res.getString(R.string.alert_title_signin_failed));
                        alertBuilder.setMessage(res.getString(R.string.alert_content_signin_failed));
                        alertBuilder.setButton(AlertDialog.BUTTON_NEUTRAL, res.getString(R.string.alert_btn_ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertBuilder.show();
                    }
                });
                return;

            }
            showProgress(true,"Transferring...");
            Log.d(TAG, "executeTransfer: request-"+json_trans_request.toString());
            JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST, url, json_trans_request, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    showProgress(false,null);
                    Log.d(TAG, "onResponse: "+response);
//                    {"pin":"123456","amount":"500","currency":"IQD","fees":"SENDER","receiver_phone_number":"+919567090305","motive":"","sender_account":"3","execution_date":"11\/18\/2016"}
//                    {"transfer_id":43}
//                    creating transfer object
                    Transfer transfer=new Transfer();
                    try {
                        transfer.setTransferId(response.getString("transfer_id"));
                        transfer.setSenderPhone(user.getPhone());
                        transfer.setSenderAccount(user.getLinakiAccount());
                        transfer.setReceiverPhone(json_trans_request.getString("receiver_phone_number"));
                        transfer.setAmount(Float.parseFloat(json_trans_request.getString("amount")));
                        transfer.setCurrency(json_trans_request.getString("currency"));
                        transfer.setfeesPaidBy(json_trans_request.getString("fees"));
                        transfer.setMotive(json_trans_request.getString("motive"));
                        transfer.setExecutionDate(json_trans_request.getString("execution_date"));
                        transfer.setRequestDate(String.valueOf(new Date().getTime()));
                        transfer.setTransferDirection(Const.TRANSFER_DIRECTION.SENT);
                        transfer.setTransferStatus(Const.TRANSFER_STATUS.PENDING);
                        if(db.addTransfer(transfer)>0){
                            ChatMessage msgObj=new ChatMessage();
                            msgObj.setXmppId("");
                            msgObj.setFrom("self");
                            msgObj.setTo(chatId);
                            msgObj.setBody(transfer.getJsonString());
                            msgObj.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                            msgObj.setReceiptId(chatId+"-"+msgObj.getDate());
                            msgObj.setType(ChatMessage.TYPE_TRANSFER);
                            msgObj.setStatus(ChatMessage.STATUS_UNSENT);
                            if(db.addMessage(msgObj)>0){
//                                call send message service
                                common.callSendMessageService();
//                                view message box
                                Intent intent=new Intent(context,SingleChatBoxActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("chatId",chatId);
                                context.startActivity(intent);
                                finish();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    showProgress(false,null);
                    error.printStackTrace();
                    String errorbody="";
                    String error_message="Could not complete the process. Try again.";
                    try {
                        errorbody=new String(error.networkResponse.data,"UTF-8");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "onErrorResponse: error body-"+errorbody);
                    try {
                        JSONObject jsonError=new JSONArray(errorbody).getJSONObject(0);
                        if(jsonError.getString("error_code").equals("ERR_UNAVAILABLE_AMOUNT")){
                            error_message="You don't have sufficient balance.";
                        }
                        else if(jsonError.getString("error_code").equals("ERR_INVALID")){
                            JSONObject jsonFields=jsonError.getJSONObject("fields");
                            error_message=jsonFields.getString(jsonFields.names().getString(0));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    AlertDialog alertBuilder=new AlertDialog.Builder(LinkaiTransferTimeActivity.this).create();
                    alertBuilder.setTitle("Failed to transfer");
                    alertBuilder.setMessage(error_message);
                    alertBuilder.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertBuilder.show();
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    return headers;
                }
            };
            RequestQueue requestQueue= Volley.newRequestQueue(context);
            requestQueue.add(request);
        }catch (Exception e){

        }
    }

    private void showProgress(final boolean show,String message) {

        if(show) {
            progressDialog.setMessage(message);
            progressDialog.setIndeterminate(true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setProgressStyle(0);
            progressDialog.show();
        }
        else{
            progressDialog.dismiss();
        }

    }

}
