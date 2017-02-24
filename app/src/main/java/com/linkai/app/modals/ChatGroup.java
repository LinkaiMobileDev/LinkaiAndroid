package com.linkai.app.modals;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * Created by LP1001 on 27-07-2016.
 */
public class ChatGroup implements Parcelable{
    final static String TAG="ChatGroup";

//    primary varibles
    private String grp_id;
    private String grp_name;
    private String grp_owner;
    private int grp_status= Const.GROUP_STATUS.ACTIVE.getValue();
    private Date grp_status_date=new Date();
    private int grp_type=Const.GROUP_TYPE.EVENT.getValue();
    private int event_type=Const.EVENT_TYPE.OTHERS.getValue();
    private double event_target_amount=0;
    private double event_balance=0;
    private String event_target_currency="";
    private String event_beneficiary_name="";
    private String event_beneficiary_phone="";
    private String profile_thumb="";
    private String profile_updated_date="0";
//    default constructor
    public ChatGroup(){

    }
    //   public object to refer last group Message
    public GroupMessage LastMessage=new GroupMessage();
    //    setter methods
    public void setGroupId(String _grp_id){
        this.grp_id=_grp_id;
    }

    public void setName(String _grp_name){
        this.grp_name=_grp_name;
    }

    public void setOwner(String _grp_owner){
        this.grp_owner=_grp_owner;
    }

    public void setStatus(int _grp_status){
        this.grp_status=_grp_status;
    }
    public void setStatus(Const.GROUP_STATUS _grp_status){
        this.grp_status=_grp_status.getValue();
    }

    public void setStatusDate(Date dt){
        this.grp_status_date=dt;
    }
    public void setStatusDate(String dt){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            this.grp_status_date= dateFormat.parse(dt);
        } catch (Exception e) {
            // e.printStackTrace();
            this.grp_status_date=new Date();
        }
    }

    public void setType(int type){
        this.grp_type=type;
    }
    public void setType(Const.GROUP_TYPE type){
        this.grp_type=type.getValue();
    }

    public void setEventType(int type){
        this.event_type=type;
    }
    public void setEventType(Const.EVENT_TYPE type){
        this.event_type=type.getValue();
    }

    public void setTargetAmount(double amount){
        this.event_target_amount=amount;
    }

    public void setBalance(double balance){
        this.event_balance=balance;
    }

    public void setTargetCurrency(String currency){
        this.event_target_currency=currency;
    }

    public void setBeneficiaryName(String name){
        this.event_beneficiary_name=name;
    }

    public void setBeneficiaryPhone(String phone){
        this.event_beneficiary_phone=phone;
    }

    public void setProfileThumb(String _profile_thumb){
        this.profile_thumb=_profile_thumb;
    }

    public void setProfileUpdatedDate(String _date){
        this.profile_updated_date=_date;
    }

//    getter methods
    public String getGroupId(){
        return this.grp_id;
    }

    public String getName(){
        return this.grp_name;
    }

    public String getOwner(){
        return this.grp_owner;
    }

    public  int getStatus(){ return this.grp_status; }

    public Date getStatusDate(){
        return this.grp_status_date;
    }
    public Date getStatusDateUTC(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String utcDateString=dateFormat.format(this.grp_status_date);
        Date utcDate;
        try {
            utcDate=dateFormat.parse(utcDateString);
        } catch (ParseException e) {
            utcDate=new Date(this.grp_status_date.getTime()-(24*60*60*1000));
            e.printStackTrace();
        }
        return utcDate;
    }
    public String getStatusDateString(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(this.grp_status_date);
    }

    public int getType(){
        return this.grp_type;
    }

    public int getEventType(){
        return this.event_type;
    }

    public double getTargetAmount(){
        return this.event_target_amount;
    }

    public double getBalance(){return  this.event_balance; }

    public String getTargetCurrency(){
        return this.event_target_currency;
    }

    public String getBeneficiaryName(){
        return this.event_beneficiary_name;
    }

    public String getBeneficiaryPhone(){
        return this.event_beneficiary_phone;
    }

    public String getProfileThumb(){
        return this.profile_thumb;
    }

    public String getProfileUpdatedDate(){
        return this.profile_updated_date;
    }

//    other methods
//    method to sync with groups in server
    public static boolean syncGroupsWithServer(Context context){
        DatabaseHandler db=Const.DB;
        Resources res=context.getResources();
        String url=Const.GROUP_GET_URL;
        ChatUser user=db.getUser();
        Log.d(TAG, "syncGroupsWithServer: "+user.getJabberId()+"--"+user.getPassword()+"--"+user.getPhone());
        if(user==null || user.getPhone()==null || user.getPassword()==null || user.getPhone().equals("") || user.getPassword().equals("") ){
            return false;
        }
        JSONObject reqJson=new JSONObject();
        try {
//            reqJson.put("phone",user.getPhone());
            reqJson.put("jabberid",user.getJabberId());
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        Log.d(TAG, "syncGroupsWithServer: request-"+reqJson.toString());
        RequestFuture<JSONObject> future=RequestFuture.newFuture();
        JsonObjectRequest request= new JsonObjectRequest(Request.Method.POST,url,reqJson,future,future);
        //        getting request que
        RequestQueue requestQueue= Volley.newRequestQueue(context);
        requestQueue.add(request);

        try {

//            JSONObject response=new JSONObject(new  JSONObject(future.get(30, TimeUnit.SECONDS).getString("d")).getString("root"));

            JSONObject response=future.get(30, TimeUnit.SECONDS);
            Log.d(TAG, "syncGroupsWithServer: response-"+response);
            JSONArray grpArray=response.getJSONArray("groups");
            Log.d(TAG, "syncGroupsWithServer: len-"+grpArray.length());
//            loop through array
            for(int i=0;i<grpArray.length();i++){
                JSONObject grpObj=grpArray.getJSONObject(i);
                // adding new group

                String grp_id=grpObj.getString("GroupId");
                ChatGroup newGroup=db.getGroup(grp_id);
                boolean addGroup=false;
                if(newGroup==null){
                    newGroup=new ChatGroup();
                    addGroup=true;
                }
                newGroup.setGroupId(grpObj.getString("GroupId"));
                newGroup.setName(grpObj.getString("Name"));
                newGroup.setOwner(grpObj.getString("Owner"));
                newGroup.setType(grpObj.getInt("isevent"));
                newGroup.setEventType(grpObj.getInt("event_type"));
                newGroup.setTargetCurrency(grpObj.getString("event_target_currency"));
                newGroup.setTargetAmount(grpObj.getDouble("event_target_amount"));
                newGroup.setBeneficiaryPhone(grpObj.getString("event_beneficiary_jid"));
//                check if group was inactive. if so make it active and update status date
                if(newGroup.getStatus()==Const.GROUP_STATUS.INACTIVE.getValue()){
                    newGroup.setStatus(Const.GROUP_STATUS.ACTIVE);
                    newGroup.setStatusDate(new Date());
                }
//                boolean flag to check wheather to add members or not
                boolean addMembers=false;
//trying to add group
                if(addGroup){
                    if(db.addGroup(newGroup)){
                        addMembers=true;
                        Log.d(TAG, "syncGroupsWithServer:add-true");
                    }
                }
                else{
//                    update group
                    if(db.updateGroup(newGroup)){
                        addMembers=true;
                    }

                }
//                adding members
                if(addMembers){
                    JSONArray memberArr=grpObj.getJSONArray("Members");
//                    remove all members
                    db.removeAllMembers(grp_id);
//                    add members
                    for(int k=0;k<memberArr.length();k++){
                        JSONObject memObj=memberArr.getJSONObject(k);
                        GroupMember member=new GroupMember();
                        member.setGroupId(grp_id);
                        member.setPhone(memObj.getString("Phone"));
                        member.setJabberId(memObj.getString("Mem_JabberId"));
                        db.addGroupMember(member);
                    }
                }
            }

        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            Log.d(TAG, "syncGroupsWithServer: exception="+e.getMessage());
            e.printStackTrace();
        }catch (Exception e){
            Log.d(TAG, "syncGroupsWithServer: exception="+e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

//    method to update group on receiving individual group notification and save that notification as a group message
    public static void updateOnNotification(String note_body){
        try{
//            parse notification
            JSONObject jsonNotification=new JSONObject(note_body);
            String group_id=jsonNotification.getString("group_id");
//            create group
            ChatGroup group=Const.DB.getGroup(group_id);
//            create notification message
            GroupMessage message=new GroupMessage();
            message.setGroupId(group_id);
            message.setBody(note_body);
            message.setFrom("self");
            message.setDate(DateFormat.getDateTimeInstance().format(new Date()));
            message.setStatus(ChatMessage.STATUS_DELIVERED);
            message.setType(ChatMessage.TYPE_NOTIFICATION);
//            check notifytype
            if(jsonNotification.getString("notify_type").equals(Const.NOTIFY_MESSAGE_TYPE.ADD_MEMBER.toString())){
                group.setStatus(Const.GROUP_STATUS.ACTIVE.getValue());
                //            set message packetid as groupid+'NA'
                message.setPacketId(group_id+"NA");
            }
            else{
                group.setStatus(Const.GROUP_STATUS.INACTIVE.getValue());
                //            set message packetid as groupid+'NR'
                message.setPacketId(group_id+"NR");
            }

            group.setStatusDate(new Date());
            if(Const.DB.updateGroup(group)){
//                delete old notification message if exists. only one notification message is maintained
                Const.DB.deleteGroupMessage(Const.DB.getGroupMessageByPacketId(message.getPacketId()));
//                add notification message
                Const.DB.addGroupMessage(message);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

//    overidden methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
//        Log.d(TAG, "writeToParcel targetAmount:"+String.valueOf(event_target_amount)+"="+event_target_amount);
        dest.writeStringArray(new String[]{
                String.valueOf(grp_id),
                String.valueOf(grp_name),
                String.valueOf(grp_owner),
                String.valueOf(grp_status),
                String.valueOf(grp_type),
                String.valueOf(event_type),
                String.format(new Locale("en"),"%.2f",event_target_amount),
                String.valueOf(event_target_currency),
                String.valueOf(event_beneficiary_name),
                String.valueOf(event_beneficiary_phone)
        });
        //String.format("%.2f",event_target_amount)//String.valueOf(event_target_amount)
        Log.d(TAG, "writeToParcel: event target: "+String.format(new Locale("en"),"%.2f",event_target_amount));
    }
//constructor initialized with parcel
    protected ChatGroup(Parcel in) {
        String[] data=new String[10];
        in.readStringArray(data);
        Log.d(TAG, "ChatGroup: "+StringUtils.join(data,","));
        grp_id = data[0];
        grp_name = data[1];
        grp_owner = data[2];
        grp_status = Integer.parseInt(data[3]);
        grp_type =  Integer.parseInt(data[4]);
        event_type = Integer.parseInt(data[5]);
        Log.d(TAG, "ChatGroup: event target: getfromparcel: "+data[6]);
        event_target_amount = Double.parseDouble(data[6]);
        event_target_currency = data[7];
        event_beneficiary_name = data[8];
        event_beneficiary_phone = data[9];
//        Log.d(TAG, "ChatGroup targetAmount: "+data[6]+"="+event_target_amount);
    }

    public static final Creator<ChatGroup> CREATOR = new Creator<ChatGroup>() {
        @Override
        public ChatGroup createFromParcel(Parcel in) {
            return new ChatGroup(in);
        }

        @Override
        public ChatGroup[] newArray(int size) {
            return new ChatGroup[size];
        }
    };
}
