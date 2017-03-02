package com.linkai.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
import com.linkai.app.libraries.Const;
import com.linkai.app.Utils.LinkaiUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class LinkaiSetPinActivity extends AppCompatActivity {
    private final String TAG="LinkaiSetPinActivity";

    private Button btnContinue;
    private Toolbar toolbar;
    private EditText txtPwd;
    private EditText txtConfirmPwd;

    Context context;
    SharedPreferences prefs;
    Map<String,String> headers;
    ProgressDialog progressDialog;
    Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linkai_set_pin);
        context=this.getApplicationContext();
        prefs = context.getSharedPreferences(Const.LINKAI_SHAREDPREFERENCE_FILE, Context.MODE_PRIVATE);
        res=context.getResources();
//        actionbar
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnContinue= (Button) findViewById(R.id.btnContinue);
        txtPwd= (EditText) findViewById(R.id.txtPwd);
        txtConfirmPwd= (EditText)findViewById(R.id.txtConfirmPwd);

        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setPin();
            }
        });

    }

    @Override
    protected void onResume() {
        Const.CUR_ACTIVITY= Const.APP_COMPONENTS.LinkaiSetPinActivity;
        super.onResume();
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

    //    for setting pin
    private boolean setPin(){
        boolean ret;
        ret=(validatePin()&& validateConfirmPin());
        if(ret){
            String pin=txtPwd.getText().toString();
            JSONObject jsonRequest=new JSONObject();
            try {
                jsonRequest.put("pin",pin);
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
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
                        setPin();
                    }

                    @Override
                    public void onError(JSONObject jsonObject) {
                        showProgress(false,null);
                        AlertDialog alertBuilder=new AlertDialog.Builder(LinkaiSetPinActivity.this).create();
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
                return false;
            }
            showProgress(true,"Setting PIN..");
            Log.d(TAG, "setPin: "+jsonRequest);
            String url=Const.LINKAI_SET_PIN;
            JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST, url,jsonRequest , new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
//                    Log.d(TAG, "onResponse: "+response);
                    showProgress(false,null);
                    prefs.edit().putBoolean("isPinSet",true).commit();
                    returnResult(true);
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
                    AlertDialog alertBuilder=new AlertDialog.Builder(LinkaiSetPinActivity.this).create();
                    alertBuilder.setTitle(res.getString(R.string.set_pin_alert_title_failed_to_set_pin));
                    alertBuilder.setMessage(res.getString(R.string.set_pin_alert_content_failed_to_set_pin));
                    alertBuilder.setButton(AlertDialog.BUTTON_NEUTRAL, res.getString(R.string.alert_btn_ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertBuilder.show();
//                    to delete
//                    Continue();
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
            RequestQueue requestQueue= Volley.newRequestQueue(context);
            requestQueue.add(request);
        }
        return ret;
    }

    private void returnResult(boolean successStatus){
        Intent resultIntent=new Intent();
        if(successStatus){
            resultIntent.putExtra("pin",txtPwd.getText().toString());
            setResult(RESULT_OK,resultIntent);
        }
        else{
            setResult(RESULT_CANCELED,resultIntent);
        }
        finish();
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

    private boolean validateConfirmPin(){
        boolean ret=true;
        String cpin=txtConfirmPwd.getText().toString();
        String pin=txtPwd.getText().toString();
        if(!cpin.equals(pin)){
            txtConfirmPwd.setError("PIN values not matching");
            txtConfirmPwd.requestFocus();
            ret=false;
        }
        return ret;
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
