package com.linkai.app.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.linkai.app.libraries.Const;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by LP1001 on 15-02-2017.
 */
public class LinkaiRequestHeader {
    private String id_token = "";
    private String authorization = "";
    private String api_key = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private String user_agent = "MOBILE";
    private String X_phone_id = "";
    private String accept_language = "En";
    private String content_type = "application/json";

    SharedPreferences prefs;

    public LinkaiRequestHeader(Context context) {
        prefs = context.getSharedPreferences(Const.LINKAI_SHAREDPREFERENCE_FILE, Context.MODE_PRIVATE);
        id_token = prefs.getString("access_token", null);
//            setting autherization header with token
        authorization = prefs.getString("token_type", "") + " " + prefs.getString("access_token", "");
//            setting imei number as user agent
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        X_phone_id = telephonyManager.getDeviceId();
//            setting default device language as accepted language
        accept_language = Locale.getDefault().getLanguage();
        Log.d("LinkaiRequestHeader", "LinkaiRequestHeader: user_agent-" + user_agent + ",accept_language-" + accept_language + ",token-" + authorization + ",X-phone-id" + X_phone_id + ",Content-Type" + content_type);
    }

    public Map<String, String> getRequestHeaders() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", this.authorization);
        headers.put("user_agent", this.user_agent);
        headers.put("X-phone-id", this.X_phone_id);
        headers.put("Accept-Language", this.accept_language);
        headers.put("Content-Type", this.content_type);
        Log.d(LinkaiUtils.TAG, "getRequestHeaders: headers-" + headers.toString());
        return headers;
    }

    public Map<String, String> getRequestHeadersWithException() throws Exception {
//            throw exception if id_token is not set or expired
        long cur_timestamp = new Date().getTime();
        long token_timestamp = prefs.getLong("token_timestamp", 0);
        long token_expiry = prefs.getLong("expires_in", 0);
        Log.d(LinkaiUtils.TAG, "getRequestHeadersWithException: curr-" + cur_timestamp + ", token-" + token_timestamp + "  exp-" + token_expiry + " diff-" + (cur_timestamp - token_timestamp));
        if (this.id_token == null || this.id_token.equals("") || (cur_timestamp - token_timestamp) > token_expiry) {
            throw new Exception("invalid id_token", new Throwable("id_token not set or expired"));
        }
        return this.getRequestHeaders();
    }

}
