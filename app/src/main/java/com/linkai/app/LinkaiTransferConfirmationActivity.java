package com.linkai.app;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
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
import com.linkai.app.modals.Transfer;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

public class LinkaiTransferConfirmationActivity extends AppCompatActivity {
    private final String TAG="TransferConfirmation";

    private BroadcastReceiver chatBroadReceiver;
    private MyXMPP xmpp ;
    private Common common;
    private Context context;
    FileHandler fileHandler;
    DatabaseHandler db;
    Resources res;

    ChatFriend friend;
    ChatMessage message;
    Transfer transfer;
    private String chatId;

    RoundedImageView imgProfileThumb;
    Toolbar toolbar;
    TextView lblChatFriendName;
    TextView lblAvailabilityStatus;
    Button btnContinue;
    EditText txtPin;
    TextView txtHead1;
    TextView txtAmount;
    TextView txtServiceFees;

    ProgressDialog progressDialog;
    Map<String,String> headers;
    SharedPreferences prefs;

    private boolean IS_ONLINE=false;
    private boolean IS_TYPING=false;
    private final String STATUS_ONLINE="online";
    private final String STATUS_TYPING="typing...";
    private final String STATUS_OFFLINE="";

    private final int SET_PIN_REQUEST=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linkai_transfer_confirmation);
        Intent currentIntent=getIntent();
        context=this.getApplicationContext();
        db= Const.DB;
        common=new Common(context);
        fileHandler=new FileHandler(context);
        xmpp=((ChatApplication)getApplication()).getMyXMPPInstance();
        prefs = context.getSharedPreferences(Const.LINKAI_SHAREDPREFERENCE_FILE, Context.MODE_PRIVATE);
        res=context.getResources();

        chatId=currentIntent.getStringExtra("chatId");
        friend=db.getFriendByJabberId(chatId);
        String msgid=String.valueOf(currentIntent.getIntExtra("msgId",0));
        message=db.getMessageById(msgid);
        JSONObject transferJson= null;
        try {
            transferJson = new JSONObject(message.getBody());
            transfer=db.getTransfer(transferJson.getString("transfer_id"));
        } catch (JSONException e) {
            finish();
            e.printStackTrace();
        }

        toolbar= (Toolbar) findViewById(R.id.toolbar);
        lblChatFriendName= (TextView) findViewById(R.id.lblChatFriendName);
        lblAvailabilityStatus=(TextView) findViewById(R.id.lblAvailabilityStatus);
        imgProfileThumb= (RoundedImageView) findViewById(R.id.imgProfileThumb);
        txtPin= (EditText) findViewById(R.id.txtPin);
        btnContinue= (Button) findViewById(R.id.btnContinue);
        txtHead1= (TextView) findViewById(R.id.txtHead1);
        txtAmount= (TextView) findViewById(R.id.txtAmount);
        txtServiceFees= (TextView) findViewById(R.id.txtServiceFees);

        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

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

//        setting linkai details
        txtHead1.setText(friend.getName()+" linkai you.");
        txtAmount.setText(""+transfer.getAmount()+" "+transfer.getCurrency());
        txtServiceFees.setText("Service fees : 1000 IQD");
        //        setting the status of friend
        IS_ONLINE=xmpp.isOnline(chatId);
        setChatAvailabilityStatus();

        //        checking if PIN already set
        if(!prefs.getBoolean("isPinSet",false)){
//            if not set goto set pin activity
            Intent setPinIntent=new Intent(context,LinkaiSetPinActivity.class);
            startActivityForResult(setPinIntent,SET_PIN_REQUEST);
        }

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validatePin()) {
                    Continue();
//                    onTransferExecute();
                }
            }
        });

    }

    @Override
    public void onResume() {
        Const.CUR_ACTIVITY= Const.APP_COMPONENTS.LinkaiTransferConfirmationActivity;
        super.onResume();

        chatBroadReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SET_PIN_REQUEST) {
            if(resultCode == RESULT_OK){
                String pin=data.getStringExtra("pin");
                txtPin.setText(pin);
//                Continue();
                Log.d(TAG, "onActivityResult: ok pin-"+pin);
            }
            if (resultCode == RESULT_CANCELED) {
                Log.d(TAG, "onActivityResult: canceled");
                finish();
            }
        }
    }

    public void Continue(){
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("transfer_id",Integer.parseInt(transfer.getTransferId()));
            jsonObject.put("pin",Integer.parseInt(txtPin.getText().toString()));
            jsonObject.put("status",1);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            headers=new LinkaiRequestHeader(context).getRequestHeadersWithException();
            Log.d(TAG, "Continue: headers-"+headers);
        } catch (Exception e) {
            e.printStackTrace();
            LinkaiUtils linkaiUtils=new LinkaiUtils(context);
            showProgress(true,"Signing in..");
            linkaiUtils.signIn(new SimpleJsonCallback() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    showProgress(false,null);
                    Continue();
                }

                @Override
                public void onError(JSONObject jsonObject) {
                    showProgress(false,null);
                    AlertDialog alertBuilder=new AlertDialog.Builder(context).create();
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
        Log.d(TAG, "Continue: request-"+jsonObject.toString());
        showProgress(true,"Executing transfer..");
        JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST, Const.LINKAI_TRANSFER_ACCEPTANCE_URL, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
//                call to update balance
                new LinkaiUtils(context).getBalance();
//                hide progress bar
                showProgress(false,null);
//                execute transfer success
                onTransferExecute();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showProgress(false,null);
                error.printStackTrace();
                String errorbody="";
                try {
                    errorbody=new String(error.networkResponse.data,"UTF-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "onErrorResponse: error body-"+errorbody);
                AlertDialog alertBuilder=new AlertDialog.Builder(LinkaiTransferConfirmationActivity.this).create();
                alertBuilder.setTitle(res.getString(R.string.transfer_confirm_alert_title_trans_exec_failed));
                alertBuilder.setMessage(res.getString(R.string.transfer_confirm_alert_content_trans_exec_failed));
                alertBuilder.setButton(AlertDialog.BUTTON_NEUTRAL, res.getString(R.string.alert_btn_ok),
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

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                if (response.data == null || response.data.length == 0) {
                    return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
                } else {
                    return super.parseNetworkResponse(response);
                }
            }
        };

        RequestQueue requestQueue= Volley.newRequestQueue(context);
        requestQueue.add(request);
    }

    public void onTransferExecute(){
        transfer.setTransferStatus(Const.TRANSFER_STATUS.ACCEPTED);
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
                finish();
            }
            else{

            }
        }
        else{

        }
    }


    private boolean validatePin(){
        boolean ret=true;
        String pwd=txtPin.getText().toString();
        if(pwd==null || pwd.equals("")){
            txtPin.setError("Enter PIN");
            txtPin.requestFocus();
            ret=false;
        }
        else if(!TextUtils.isDigitsOnly(pwd) || pwd.length()!=6)
        {
            txtPin.setError("Invalid PIN");
            txtPin.requestFocus();
            ret=false;
        }
        else{
            ret=true;
        }
        return ret;
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
