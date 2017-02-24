package com.linkai.app.Utils;

import org.json.JSONObject;

/**
 * Created by LP1001 on 15-02-2017.
 */
public interface SimpleJsonCallback {
    void onSuccess(JSONObject jsonObject);
    void onError(JSONObject jsonObject);
}
