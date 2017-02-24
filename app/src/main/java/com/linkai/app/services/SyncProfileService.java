package com.linkai.app.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.modals.ChatFriend;
import com.linkai.app.modals.ChatGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;


public class SyncProfileService extends IntentService {
    final String TAG="SyncProfileService";

    public static boolean IS_ALIVE=false;
    Context context;
    DatabaseHandler db=Const.DB;


    public SyncProfileService() {
        super("SyncProfileService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        context=this.getApplicationContext();
        if(!IS_ALIVE){
            IS_ALIVE=true;
        }
        if (intent != null) {
            //Log.d(TAG, "onHandleIntent: "+Const.FRIENDSLIST_TO_UPDATE_PROFILE.size());
            while (Const.FRIENDSLIST_TO_UPDATE_PROFILE.size()>0 || Const.GROUPSLIST_TO_UPDATE_PROFILE.size()>0) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                JSONObject jsonRequest=new JSONObject();
                try{
                    jsonRequest.put("users",getContactsJson());
                    jsonRequest.put("groups",getGroupsJson());
                    Log.d(TAG, "onHandleIntent: request"+jsonRequest);
                    JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.POST, Const.GET_PROFILE_URL, jsonRequest, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "onResponse: syncFriendsProfile response-"+response.toString());
                            try {
                                updateContactsProfile(response.getJSONArray("users"));
                                updateGroupsProfile(response.getJSONArray("groups"));
//                            send broadcast
                                context.sendBroadcast(new Intent().setAction("chat.view.refresh"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();

                        }
                    });
                    //        getting request que
                    RequestQueue requestQueue= Volley.newRequestQueue(context);
                    requestQueue.add(jsonObjectRequest);
                }catch (Exception e){
                    e.printStackTrace();
                }
               // syncFriendsProfile(context,frnds_up_profile);
            }
        }
    }

    private JSONArray getContactsJson(){
        JSONArray jsonArray=new JSONArray();
        try {
            HashSet<String> friends = Const.FRIENDSLIST_TO_UPDATE_PROFILE;
            for (String friendid : friends) {
                ChatFriend friend=db.getFriendByJabberId(friendid);
                if(friend==null){
                    continue;
                }
                JSONObject jsonObject;
                jsonObject = new JSONObject();
                jsonObject.put("jabberid", friend.getJabberId());
                jsonObject.put("time", friend.getProfileUpdatedDate());
                jsonArray.put(jsonObject);
            }
            Const.FRIENDSLIST_TO_UPDATE_PROFILE.clear();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return  jsonArray;
    }

    private JSONArray getGroupsJson(){
        JSONArray jsonArray=new JSONArray();
        try {
            HashSet<String> groups = Const.GROUPSLIST_TO_UPDATE_PROFILE;
            for (String groupid : groups) {
                ChatGroup group=db.getGroup(groupid);
                if(group==null){
                    continue;
                }
                JSONObject jsonObject;
                jsonObject = new JSONObject();
                jsonObject.put("groupid", group.getGroupId());
                jsonObject.put("time", group.getProfileUpdatedDate());
                jsonArray.put(jsonObject);
            }
            Const.GROUPSLIST_TO_UPDATE_PROFILE.clear();
        }catch (Exception e){
            e.printStackTrace();
        }
        return  jsonArray;
    }

    private boolean updateContactsProfile(JSONArray profileArray){
        try {
            for (int i = 0; i < profileArray.length(); i++) {
                JSONObject profObj = profileArray.getJSONObject(i);
                ChatFriend friend = db.getFriendByJabberId(profObj.getString("JabberId"));
                if (friend != null) {
                    friend.setProfileThumb(profObj.getString("ProfileImage"));
                    friend.setProfileUpdatedDate(profObj.getString("ImageUpdatedTime"));
                    db.updateFriend(friend);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean updateGroupsProfile(JSONArray profileArray){
        try {
            for (int i = 0; i < profileArray.length(); i++) {
                JSONObject profObj = profileArray.getJSONObject(i);
                ChatGroup group = db.getGroup(profObj.getString("GroupId"));
                if (group != null) {
                    group.setProfileThumb(profObj.getString("ProfileImage"));
                    group.setProfileUpdatedDate(profObj.getString("ImageUpdatedTime"));
                    db.updateGroup(group);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        //Log.d("Send message service ", "onDestroy: ");
        if(IS_ALIVE){
            IS_ALIVE=false;
        }
    }

}
