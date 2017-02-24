package com.linkai.app;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
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
import com.linkai.app.libraries.Common;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.Utils.LinkaiUtils;
import com.linkai.app.modals.ChatUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MobileVerificationActivity extends AppCompatActivity {
    private final String TAG="MobileVerifyActivity";
    Context context;
    Common common;
    DatabaseHandler db;
    private BroadcastReceiver broadcastReceiver;
    Resources res;

    ProgressDialog progressDialog;

    Button btnValidate;
    TextView txtViewResendText;
    Button btnResend;
    EditText txtValidationCode;
    TextView txtTimer;

    private String phone;
    private String country;
    private String country_code;
    private String linkai_password;

    ChatUser user;
    Map<String,String> headers;

    long min=0,sec=0;
    boolean isTimerRunning=false;
    boolean isVarified=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate:" + phone);
        context=getApplicationContext();
        db = Const.DB;
        phone=(String) getIntent().getSerializableExtra("phone");
        country=(String) getIntent().getSerializableExtra("country");
        country_code=(String) getIntent().getSerializableExtra("country_code");
        linkai_password=(String) getIntent().getSerializableExtra("password");
        user=db.getUser();
        Log.d(TAG, "onCreate:" + phone);
        res=context.getResources();

        setContentView(R.layout.activity_mobile_verification);

        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        txtValidationCode= (EditText) findViewById(R.id.txtValidationCode);
        btnValidate=(Button)findViewById(R.id.btnValidate);
        txtViewResendText=(TextView)findViewById(R.id.txtViewResendText);
        btnResend=(Button)findViewById(R.id.btnResend);
        txtTimer= (TextView) findViewById(R.id.txtTimer);

        btnValidate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                try {
//                    btnValidate.setEnabled(false);
                    if(isVarified){
                        signUp();
                    }
                    else{
                        varifyCode();
                    }
//                    signUp();
                }catch(Exception ex){

                }
            }
        });

        btnResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reSendCode();
            }
        });

        startCountDown();
        askSMSPermission();

    }

    @Override
    protected void onResume() {
        Const.CUR_ACTIVITY= Const.APP_COMPONENTS.MobileVarificationActivity;
        super.onResume();
        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
                    // Retrieves a map of extended data from the intent.
                    final Bundle bundle = intent.getExtras();

                    try {

                        if (bundle != null && isTimerRunning) {

                            final Object[] pdusObj = (Object[]) bundle.get("pdus");

                            for (int i = 0; i < pdusObj.length; i++) {

                                SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                                String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                                String senderNum = phoneNumber;
                                String message = currentMessage.getDisplayMessageBody();

//                                Log.d("SmsReceiver", "senderNum: "+ senderNum + "; message: " + message);
//                                Toast.makeText(context,"senderNum: "+ senderNum + "; message: " + message,Toast.LENGTH_SHORT).show();
//                                check if message is  linkai varification message
                                if(message.toLowerCase().contains("linkai")){
                                    Pattern p = Pattern.compile("[0-9]+");
                                    Matcher m = p.matcher(message);
                                    while (m.find()) {
                                        if(m.group().length()==6){
                                            txtValidationCode.setText(m.group());
                                            varifyCode();
                                            break;
                                        }
                                        // append n to list
                                    }
                                }
                            } // end for loop
                        } // bundle is null

                    } catch (Exception e) {
                        Log.e("SmsReceiver", "Exception smsReceiver" +e);
                    }
                }
            }
        };

        try {
            context.registerReceiver(broadcastReceiver,new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
        }
        catch (Exception e){
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            context.unregisterReceiver(broadcastReceiver);
        }
        catch (Exception e){

        }
    }

    //    varify access code
    private void varifyCode(){
        String code=txtValidationCode.getText().toString();
        if(code==null || code.equals("")){
            return;
        }
        JSONObject jsonParameter=new JSONObject();
        try {

            Log.d(TAG, "password"+linkai_password +"  phone"+phone);
            jsonParameter.put("phone_number",phone);
            jsonParameter.put("access_code",code);
            jsonParameter.put("password",linkai_password);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        String url= Const.LINKAI_VARIFY_CODE_URL;

        headers=new LinkaiRequestHeader(context).getRequestHeaders();

        showProgress(true,"Validating code");
        Log.d(TAG, "varifyCode: request-"+jsonParameter.toString());
        JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST,url , jsonParameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                showProgress(false,null);
                Log.d(TAG, "onResponse: "+response.toString());
                if(new LinkaiUtils(context).setAccessToken(response))
                {
                    try {
                        JSONObject account=response.getJSONObject("user").getJSONArray("accounts").getJSONObject(0);
                        user.setLinkaiAccount(account.getString("id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                    isVarified=true;
                    signUp();
                }

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
                AlertDialog alertBuilder=new AlertDialog.Builder(MobileVerificationActivity.this).create();
                alertBuilder.setTitle(res.getString(R.string.mobile_verification_alert_title_code_validation_failed));
                alertBuilder.setMessage(res.getString(R.string.mobile_verification_alert_content_code_validation_failed));
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
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                if (response.data == null || response.data.length == 0) {
                    return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
                } else {
                    return super.parseNetworkResponse(response);
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }
        };
        RequestQueue requestQueue=Volley.newRequestQueue(context);
        requestQueue.add(request);
    }
//to signup in xmpp server
    private void signUp(){
        JSONObject jsonObject=new JSONObject();
        JSONObject jsonRequest=new JSONObject();
        try {
            jsonObject.put("Phone",user.getPhone());
            jsonObject.put("Country",country);
            jsonObject.put("CountryCode",country_code);
            jsonObject.put("Id","");
            jsonObject.put("JabberId","");
            jsonObject.put("Name","");
            jsonObject.put("ProfileImage","");
            jsonObject.put("ImageUpdatedTime","");
            jsonObject.put("StatusMessage","");
            jsonObject.put("Operation","1");
//            adding json to request
            jsonRequest.put("jsonobj",jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        Log.d(TAG, "signUp: request"+jsonObject.toString());
        String url= Const.POST_PROFILE_URL;
//        call to show progress
        showProgress(true,"Signing in...");
        JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST, url, jsonRequest, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject responseJson) {
                Log.d(TAG, "onResponse: "+responseJson);

                //        call to dismiss progress
                showProgress(false,null);
                try {
                    //                    check if success or not
                    if(!responseJson.getBoolean("status"))
                    {
                        AlertDialog alertBuilder=new AlertDialog.Builder(MobileVerificationActivity.this).create();
                        alertBuilder.setTitle(res.getString(R.string.mobile_verification_alert_title_signup_failed));
                        alertBuilder.setMessage(res.getString(R.string.mobile_verification_alert_content_signup_failed));
                        alertBuilder.setButton(AlertDialog.BUTTON_NEUTRAL, res.getString(R.string.alert_btn_ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertBuilder.show();
                        return;
                    }
                    user.setJabberId(responseJson.getString("jabberid"));
                    user.setPassword(responseJson.getString("password"));
                    Log.d(TAG, "onResponse: user jid-"+user.getJabberId()+", pwd-"+user.getPassword()+" ,"+user.getLinakiAccount());
                    user.setStatus(Const.USER_STATUS.VARIFIED.getValue());
                    if(db.updateUser(user)){
                        Intent profileIntent = new Intent(context, UserProfileActivity.class);
                        profileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(profileIntent);
                        finish();
                    }else{
                        AlertDialog alertBuilder=new AlertDialog.Builder(MobileVerificationActivity.this).create();
                        alertBuilder.setTitle(res.getString(R.string.mobile_verification_alert_title_signup_failed));
                        alertBuilder.setMessage(res.getString(R.string.mobile_verification_alert_content_signup_failed));
                        alertBuilder.setButton(AlertDialog.BUTTON_NEUTRAL, res.getString(R.string.alert_btn_ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertBuilder.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                //        call to dismiss progress
                showProgress(false,null);
                AlertDialog alertBuilder=new AlertDialog.Builder(MobileVerificationActivity.this).create();
                alertBuilder.setTitle(res.getString(R.string.mobile_verification_alert_title_signup_failed));
                alertBuilder.setMessage(res.getString(R.string.mobile_verification_alert_content_signup_failed));
                alertBuilder.setButton(AlertDialog.BUTTON_NEUTRAL, res.getString(R.string.alert_btn_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertBuilder.show();
            }
        });
        //        setting default retry policy
        request.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(20),
                0,  // maxNumRetries = 0 means no retry
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue= Volley.newRequestQueue(context);
        requestQueue.add(request);
    }


    private void reSendCode(){
        String url= Const.LINKAI_GET_ACCESS_CODE_URL;
        JSONObject jsonParameter=new JSONObject();
        try {

            Log.d(TAG, "password"+linkai_password +"  phone"+phone);
            jsonParameter.put("phone_number",phone);
            jsonParameter.put("password",linkai_password);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        headers=new LinkaiRequestHeader(context).getRequestHeaders();
        showProgress(true,"Resending code..");
        JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST,url , jsonParameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
//                Log.d(TAG, "onResponse: response-"+response.toString());
                showProgress(false,null);
                disableResend();
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
                AlertDialog alertBuilder=new AlertDialog.Builder(MobileVerificationActivity.this).create();
                alertBuilder.setTitle(res.getString(R.string.mobile_verification_alert_title_resending_failed));
                alertBuilder.setMessage(res.getString(R.string.mobile_verification_alert_content_resending_failed));
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
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                if (response.data == null || response.data.length == 0) {
                    return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
                } else {
                    return super.parseNetworkResponse(response);
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                return headers;
            }
        };
        RequestQueue requestQueue=Volley.newRequestQueue(context);
        requestQueue.add(request);
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

//    start counting down the access code expiry
    private void startCountDown(){
        txtTimer.setVisibility(View.VISIBLE);
        isTimerRunning=true;
        new CountDownTimer(120*1000,1000){
            @Override
            public void onTick(long millisUntilFinished) {

                min=TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                sec=(millisUntilFinished/1000)-(min*60);
                String count= min+":"+sec;
                txtTimer.setText(count);
            }
            @Override
            public void onFinish() {
                isTimerRunning=false;
                txtTimer.setVisibility(View.GONE);
                enableResendOption();
            }
        }.start();
    }

//    show/enable resend option after timeout disable 'validate' button
    private void enableResendOption(){
        if(isVarified){
//            return if varified
            return;
        }
        txtViewResendText.setVisibility(View.VISIBLE);
        btnResend.setVisibility(View.VISIBLE);

        btnValidate.setEnabled(false);
        btnValidate.setBackgroundResource(R.drawable.app_button_disabled);
    }

//    to disable/hide resend option and enable 'validate' button and start countdown again
    private void disableResend(){
        txtViewResendText.setVisibility(View.GONE);
        btnResend.setVisibility(View.GONE);

        btnValidate.setEnabled(true);
        btnValidate.setBackgroundResource(R.drawable.app_button);

        startCountDown();
    }

  public void askSMSPermission(){
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
          if(checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED){
              ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, 100);
          }
      }
  }



}
