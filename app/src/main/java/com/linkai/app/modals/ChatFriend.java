package com.linkai.app.modals;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChatFriend {
    private static final String TAG="ChatFriend";
//primary variables
    private String jabber_id="";
    private int friend_id=0;
    private String name=null;
    private String phone=null;
    private int subscription_status=Const.FRIEND_SUBSCRIPTION_STATUS.SUBSCRIBED.getValue();
    private int block_status=0;
    private String profile_thumb="";
    private String profile_updated_date="0";

//secondary
//   public object to refer last chatMessage
    public ChatMessage LastMessage=new ChatMessage();
    private static  Context context;

//    constructor
    public ChatFriend(){

    }

//setter methods
    public void setId(int _friend_id){
        this.friend_id=_friend_id;
    }

    public void setJabberId(String _jabber_id){
        this.jabber_id=_jabber_id;
    }


    public void setName(String _name){
        this.name=_name.replace("'","''");
    }


    public void setPhone(String _phone){
//        checking phone number format
        _phone=_phone.replace("-", "").replace(" ", "");
//        if(_phone.length()==10){
//            _phone="+91"+_phone;
//        }
        this.phone=_phone;
    }

    public void setProfileThumb(String _profile_thumb){
        this.profile_thumb=_profile_thumb;
    }

    public void setProfileUpdatedDate(String _date){
        this.profile_updated_date=_date;
    }

    public void setSubscriptionStatus(int _sub_status){
        this.subscription_status=_sub_status;
    }

//    getter methods


    public int getId(){
        return this.friend_id;
    }

    public String getJabberId(){
        return this.jabber_id;
    }

    public String getName(){
        return this.name;
    }

    public String getPhone(){
        return this.phone;
    }

    public int getSubscriptionStatus(){
        return this.subscription_status;
    }

    public void setBlockStatus(int _block_status){
        this.block_status=_block_status;
    }

    public int getBlockStatus(){
        return this.block_status;
    }

    public String getProfileThumb(){
        return this.profile_thumb;
    }

    public String getProfileUpdatedDate(){
        return this.profile_updated_date;
    }

//    overiding methods
    @Override
    public boolean equals(Object o) {
//        Log.d(TAG, "equals: ");
//        Log.d(TAG, "equals: "+((ChatFriend) o).getPhone()+"="+this.getPhone()+"");
        ChatFriend chatFriend= (ChatFriend) o;
        if(this.getJabberId().equals(chatFriend.getJabberId())){
//            Log.d(TAG, "equals: "+((ChatFriend) o).getPhone()+"="+this.getPhone()+"  true");
            return true;
        }
        else{
//            Log.d(TAG, "equals: "+((ChatFriend) o).getPhone()+"="+this.getPhone()+"  true");
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash=12345;
        hash=10*hash+this.getId();
        return hash;
    }

    //static method for refreshing contacts
    public static void refreshFriendList(final Context _context){

//        Log.d(TAG, "refreshFriendList: init");

        context=_context;


        DatabaseHandler db=Const.DB;

        String user_phone=db.getUser().getPhone();
        String user_phone_countrycode;
        //                creating phonenumber util
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try {
            // phone must begin with '+'
            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(user_phone, "");
            user_phone_countrycode ="+"+ numberProto.getCountryCode();
        } catch (Exception e) {
            return ;
        }

        Cursor contacts= context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        ArrayList<ChatFriend> friends=new ArrayList<ChatFriend>();
        while(contacts.moveToNext()){

            String phone_number=contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).trim();
//                    removing leading zeros
            phone_number=trimLeadingZeros(phone_number);
            //phone_number.replaceFirst("^0+(?!$)", "");
//                    replacing space,-,(,)
            phone_number=phone_number.replace("-", "").replace(" ", "").replace("(","").replace(")","");
            if(!(phone_number.contains("+"))){
                phone_number=user_phone_countrycode+phone_number;
            }
//            Log.d(TAG, "refreshFriendList: "+phone_number);
//                    trying to get number proto
            try {
                Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phone_number, "");
//                        checking if phone number is valid, if so adding friend
                if(phoneUtil.isValidNumber(numberProto)){
                    ChatFriend friend=new ChatFriend();
                    friend.setName(contacts.getString(contacts.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                    friend.setPhone(phone_number);
                    friend.setJabberId(phone_number);
                    friend.setSubscriptionStatus(Const.FRIEND_SUBSCRIPTION_STATUS.UNSUBSCRIBED.getValue());
                    friends.add(friend);
                }
            } catch (Exception e) {
            }

        }
        db.addFriends(friends);
//               search registered friends
        try {
//            ((ChatApplication) _context).getMyXMPPInstance().searchFriendsAccount();
        }catch(Exception e){

        }

    }

//    to trim leading zeros from alphanumeric string
    private static String trimLeadingZeros(String source){
        String out;
        int length = source.length();

        if (length < 2)
            return source;

        int i;
        for (i = 0; i < length-1; i++)
        {
            char c = source.charAt(i);
            if (c != '0')
                break;
        }

        if (i == 0)
            out= source;
        else{
            out=source.substring(i);
            if(i==2){
//                i==2 means 1st 2 chars was idd code(00). prefixing + after trimming 00
                out="+"+out;
            }
        }

        return out;
    }

//    static method to search user in server
    public static void searchFriendsInServer(final Context _context){
//        if(true)return;
        context=_context;
        //final MyXMPP xmpp= ((ChatApplication) context.getApplicationContext()).getMyXMPPInstance();
        final DatabaseHandler db=Const.DB;
        final LocalBroadcastManager localBroadcastManager=LocalBroadcastManager.getInstance(context);
        ArrayList<ChatFriend> friends=db.getAllFriends(Const.FRIEND_SUBSCRIPTION_STATUS.UNSUBSCRIBED);
        JSONObject jsonObject;
        JSONArray jsonArray=new JSONArray();
        for (ChatFriend friend :friends) {
            jsonObject=new JSONObject();
            try {
                jsonObject.put("phone",friend.getPhone());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(jsonObject);
        }
//        sending to server
        try {
            Resources res=context.getResources();
            String url = Const.SEARCH_FRIENDS_URL;

            JSONObject jsonRequest=new JSONObject();
            jsonRequest.put("contacts",jsonArray);
            Log.d(TAG, "searchFriendsInServer: "+jsonRequest.toString());
            JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(JsonObjectRequest.Method.POST, url, jsonRequest, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, "onResponse: "+response);
                    //                    get response json
                    JSONObject responseJson= null;
                    try {
//                        responseJson = new JSONObject(response.getString("d"));
//                        if(!responseJson.getBoolean("status"))
//                        {
//                            return;
//                        }
//                        JSONArray friends_array=responseJson.getJSONArray("friends");
                        JSONArray friends_array= response.getJSONArray("Contacts");
                        for(int i=0;i<friends_array.length();i++){
                            JSONObject friend_obj=friends_array.getJSONObject(i);
                            ChatFriend friend=db.getFriendByPhone(friend_obj.getString("JabberId"));
                            if(friend!=null){
                                friend.setSubscriptionStatus(Const.FRIEND_SUBSCRIPTION_STATUS.SUBSCRIBED.getValue());
                                friend.setJabberId(friend_obj.getString("JabberId"));
                                db.updateFriend(friend);
//                        send subscription request
                                //xmpp.sendSubscriptionRequest(friend.getJabberId(),friend.getName());
                            }
                        }
                        localBroadcastManager.sendBroadcast(new Intent("chat.roster.received"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    check if success or not

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "onErrorResponse: "+error.getMessage());
                }
            });
            //        getting request que
            RequestQueue requestQueue= Volley.newRequestQueue(context);
            requestQueue.add(jsonObjectRequest);

        }catch (Exception e){
            e.printStackTrace();
        }
    }



}
