package com.linkai.app.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.modals.ChatUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Map;

/**
 * Created by LP1001 on 20-10-2016.
 */
public class LinkaiUtils {
    final static String TAG="LinkaiUtils";

    Context context;
    DatabaseHandler db;
    LocalBroadcastManager localBroadcastManager;

    private Map<String,String> headers;

    public LinkaiUtils(Context _context){
        this.context=_context;
        this.db= Const.DB;
        localBroadcastManager=LocalBroadcastManager.getInstance(context);
    }

//    method for signing in linkai account
    public boolean signIn(final SimpleJsonCallback callBack) {
        ChatUser user = db.getUser();
        JSONObject jsonParameter = new JSONObject();
        try {
            jsonParameter.put("phone_number", user.getPhone());
            jsonParameter.put("password", user.getLinkaiPassword());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String url = Const.LINKAI_SIGNIN_URL;
        RequestQueue requestQueue = Volley.newRequestQueue(context);
//            run signin process asynchronous
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonParameter, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                setAccessToken(response);
//                    calling get balance after each signing in
                getBalance();
                callBack.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                JSONObject errorObj=new JSONObject();
                try {
                    errorObj.put("error",error.networkResponse.data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                callBack.onError(errorObj);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                LinkaiRequestHeader linkaiRequestHeader = new LinkaiRequestHeader(context);
                return linkaiRequestHeader.getRequestHeaders();
            }
        };
        requestQueue.add(request);
        return true;
    }
//to set access tocken after successfull signin
    public boolean setAccessToken(JSONObject response){
        boolean ret=false;
        Log.d(TAG, "setAccessToken: "+response);
        SharedPreferences.Editor editor=context.getSharedPreferences(Const.LINKAI_SHAREDPREFERENCE_FILE,Context.MODE_PRIVATE).edit();
        try {
            editor.putString("access_token",response.getString("access_token"));
            editor.putString("token_type",response.getString("token_type"));
            editor.putLong("expires_in",Long.valueOf(response.getString("expires_in")));
//            editor.putLong("expires_in",10000);
            editor.putLong("token_timestamp",new Date().getTime());
            editor.commit();
            ret=true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }

//    to get balance
    public boolean getBalance(){
        boolean ret=true;
        String url=Const.LINKAI_GET_BALANCE_URL.replace("{value}",db.getUser().getLinakiAccount());
        try {
            headers=new LinkaiRequestHeader(context).getRequestHeadersWithException();
        } catch (Exception e) {
            e.printStackTrace();
            signIn(new SimpleJsonCallback() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    getBalance();
                }

                @Override
                public void onError(JSONObject jsonObject) {

                }
            });
            return false;
        }
        JsonObjectRequest request=new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "get balance onResponse: "+response);
                setBalanceDetails(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                String errorbody="";
                try {
                    errorbody=new String(error.networkResponse.data,"UTF-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "get balance onErrorResponse: error body-"+errorbody);
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
        RequestQueue requestQueue=Volley.newRequestQueue(context);
        requestQueue.add(request);
        return ret;
    }

    public boolean setBalanceDetails(JSONObject balanceJson){
//        Log.d(TAG, "setBalanceDetails: "+balanceJson);
        SharedPreferences.Editor editor=context.getSharedPreferences(Const.LINKAI_SHAREDPREFERENCE_FILE,Context.MODE_PRIVATE).edit();
        try {
            editor.putString("current_balance",balanceJson.getString("current_balance"));
            editor.putString("available_amount",balanceJson.getString("available_amount"));
            editor.putString("blocked_amount",balanceJson.getString("blocked_amount"));
            editor.putString("currency",balanceJson.getString("currency"));
            editor.commit();
//            broadcast message to refresh linkai views
            localBroadcastManager.sendBroadcast(new Intent("linkai.view.refresh"));
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


}
