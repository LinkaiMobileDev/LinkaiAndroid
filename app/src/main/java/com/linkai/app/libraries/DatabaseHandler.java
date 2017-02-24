package com.linkai.app.libraries;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.linkai.app.modals.Chat;
import com.linkai.app.modals.ChatFriend;
import com.linkai.app.modals.ChatGroup;
import com.linkai.app.modals.ChatMessage;
import com.linkai.app.modals.ChatUser;
import com.linkai.app.modals.GroupMember;
import com.linkai.app.modals.GroupMessage;
import com.linkai.app.modals.Transfer;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;


public class DatabaseHandler extends SQLiteOpenHelper {
    private final String TAG="DB";

    private static final String DATABASE_NAME="linkaiChat";
    private static final int DATABASE_VERSION=1;

    public enum Message_Direction{INCOMING,OUTGOING,BOTH}
    public enum Message_Type{TEXTS,ALL_FILES,IMAGES,AUDIOS,VIDEOS,OTHERS,NOTIFICATION,TRANSFER,ALL}
    public enum Message_Status{UNSENT,PENDING,SENT,DELIVERED,READ,READ_ACK,UNREAD,ALL,TOSENT}
    public enum File_Status{SUCCESS,PENDING,ALL}


    SQLiteDatabase db=null;

    public DatabaseHandler(Context context) {
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
//        context.openOrCreateDatabase(DATABASE_NAME, context.MODE_PRIVATE, null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table tbl_user (jabber_id TEXT,phone text,name text,password text,linkai_password text,linkai_account text,status DEFAULT "+Const.USER_STATUS.NOT_VARIFIED.getValue()+",email text,address text)");
        db.execSQL("create table tbl_friends_list (fri_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,jabber_id TEXT, name text,phone text UNIQUE ,subscription_status INTEGER DEFAULT "+Const.FRIEND_SUBSCRIPTION_STATUS.UNSUBSCRIBED.getValue()+",block_status INTEGER DEFAULT 0" +
                ",profile_thumb TEXT,profile_updated_date TEXT DEFAULT '0')");
        db.execSQL("create table tbl_messages (msg_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,xmpp_id TEXT,msg_receipt_id TEXT UNIQUE,msg_from TEXT, msg_to TEXT, msg_body TEXT, msg_date DATETIME DEFAULT CURRENT_TIMESTAMP ," +
                "msg_status INTEGER,msg_type TEXT DEFAULT 'text',msg_file_name text,msg_file_thumb text,msg_file_status INTEGER,msg_show_status TEXT DEFAULT '"+Const.MESSAGE_SHOW_STATUS.SHOW.toString()+"' )");
        //        for groups
        db.execSQL("create table tbl_group_master (grp_id TEXT PRIMARY KEY, grp_name TEXT,grp_owner TEXT" +
                ",grp_status INTEGER,grp_status_date DATETIME,grp_type INTEGER ,event_type TEXT,event_target_amount FLOAT " +
                ",event_balance FLOAT,event_target_currency TEXT,event_beneficiary_name TEXT,event_beneficiary_phone TEXT" +
                ",profile_thumb TEXT,profile_updated_date TEXT DEFAULT '0')");
        db.execSQL("create table tbl_group_members (grp_mem_id INTEGER PRIMARY KEY AUTOINCREMENT,jabber_id TEXT, grp_id TEXT,grp_mem_phone TEXT,grp_mem_name TEXT,CONSTRAINT mem_grp_unique UNIQUE (grp_id, grp_mem_phone))");
        db.execSQL("create table tbl_group_messages (grp_msg_id INTEGER PRIMARY KEY AUTOINCREMENT,grp_id TEXT,packet_id TEXT UNIQUE, grp_msg_from TEXT,grp_msg_body TEXT,grp_msg_date DATETIME DEFAULT CURRENT_TIMESTAMP,grp_msg_status INTEGER,grp_msg_type TEXT DEFAULT 'text',grp_msg_file_name text,grp_msg_file_thumb text,grp_msg_file_status INTEGER)");
//        for transfer
        db.execSQL("create table tbl_transfer(t_id INTEGER PRIMARY KEY AUTOINCREMENT,transfer_id TEXT UNIQUE" +
                ",sender_phone TEXT, sender_account TEXT, receiver_phone TEXT, amount FLOAT, currency TEXT" +
                ", motive TEXT,fees_paid_by TEXT, execution_date TEXT, request_date TEXT, transfer_expiry TEXT" +
                ",transfer_direction INTEGER, transfer_status INTEGER)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS tbl_friends_list");
        db.execSQL("DROP TABLE IF EXISTS tbl_messages");
        db.execSQL("DROP TABLE IF EXISTS tbl_user");
        db.execSQL("DROP TABLE IF EXISTS tbl_group_master");
        db.execSQL("DROP TABLE IF EXISTS tbl_group_members");
        db.execSQL("DROP TABLE IF EXISTS tbl_group_messages");
        db.execSQL("DROP TABLE IF EXISTS tbl_transfer");
        // Create tables again
        onCreate(db);
    }

//    to get number of rows of a table
    public int getCount(Const.DB_TABLE db_table ){
        if(db==null){
            db=this.getWritableDatabase();
        }
        int count =0;
        Cursor cursor=db.rawQuery("select count(*) from "+db_table.toString(),null);
        if (cursor.moveToFirst()){
            count=cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

//    functions manage user
    public boolean addUser(ChatUser user){
//        Log.d(TAG, "addUser: "+user.getPhone()+""+user.getName());
        boolean ret;
        ContentValues values = new ContentValues();
        values.put("jabber_id", user.getJabberId());
        values.put("phone", user.getPhone());
        values.put("name", user.getName());
        values.put("password", user.getPassword());
        values.put("linkai_password", user.getLinkaiPassword());
        values.put("linkai_account", user.getLinakiAccount());
        values.put("status", user.getStatus());
        values.put("email", user.getEmail());
        values.put("address", user.getAddress());
        if(db==null){
            db=this.getWritableDatabase();
        }
        try {
//            adding user
            db.insert("tbl_user", null, values);
            ret=true;
        }
        catch (Exception e){
            ret=false;
        }
        
        return ret;
    }

    public boolean updateUser(ChatUser user){
        try {
            if(db==null){
                db=this.getWritableDatabase();
            }
            String qry="update tbl_user set name='"+user.getName()+"'" +
                    ",jabber_id='"+user.getJabberId()+"'" +
                    ",password='"+user.getPassword()+"'" +
                    ",linkai_account='"+user.getLinakiAccount()+"'" +
                    ",status='"+user.getStatus()+"'" +
                    ",email='"+user.getEmail()+"'" +
                    ",address='"+user.getAddress()+"' ";
            db.execSQL(qry);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public ChatUser getUser(){
        ChatUser user;
        if(db==null){
            db=this.getWritableDatabase();
        }
        Cursor cursor = db.rawQuery("select jabber_id,phone,name,password,status,linkai_password,linkai_account" +
                ",email,address from tbl_user", null);
        if(cursor.moveToFirst()){
            user=new ChatUser();
            user.setJabberId(cursor.getString(0));
            user.setPhone(cursor.getString(1));
            user.setName(cursor.getString(2));
            user.setPassword(cursor.getString(3));
            user.setStatus(cursor.getInt(4));
            user.setLinkaiPassword(cursor.getString(5));
            user.setLinkaiAccount(cursor.getString(6));
            user.setEmail(cursor.getString(7));
            user.setAddress(cursor.getString(8));
//            Log.d(TAG, "getUser: "+user.getPassword());
        }
        else{
            user=null;
        }
        cursor.close();
        return user;
    }

    public boolean deleteUser(){
        if(db==null){
            db=this.getWritableDatabase();
        }
        try {
            db.execSQL("delete from tbl_user");
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public boolean isUserExist(){
        boolean ret=false;
        if(db==null){
            db=this.getWritableDatabase();
        }
        Cursor cursor = db.rawQuery("select count(*),status from tbl_user", null);
        if(cursor.moveToFirst()){
            if(cursor.getInt(0)>0){
                if(cursor.getInt(1)==Const.USER_STATUS.VARIFIED.getValue()) {
                    ret = true;
                }
            }
        }
        return  ret;
    }

    public void addOrUpdateFriend(ChatFriend friend){
        ContentValues values;
        if(db==null){
            db=this.getWritableDatabase();
        }
        values=new ContentValues();
        values.put("name",friend.getName());
//        try to update friends name if friend already exist
        int update_count=db.update("tbl_friends_list",values,"phone=?",new String[]{friend.getPhone()});
        if(update_count>0){
//            return if updated
            return;
        }
//        if friend not already exists ie. count=0 try to add
        values.put("jabber_id",friend.getJabberId());
        values.put("phone",friend.getPhone());
        values.put("subscription_status",friend.getSubscriptionStatus());
        values.put("block_status",friend.getBlockStatus());
        values.put("profile_thumb",friend.getProfileThumb());
        values.put("profile_updated_date",friend.getProfileUpdatedDate());
        try {
            db.insertOrThrow("tbl_friends_list", null, values);
        }
        catch(Exception e){
        }
    }

    public void addFriend(ChatFriend friend){
        ContentValues values;
        if(db==null){
            db=this.getWritableDatabase();
        }
        values=new ContentValues();
        values.put("name",friend.getName());
        values.put("jabber_id",friend.getJabberId());
        values.put("phone",friend.getPhone());
        values.put("subscription_status",friend.getSubscriptionStatus());
        values.put("block_status",friend.getBlockStatus());
        values.put("profile_thumb",friend.getProfileThumb());
        values.put("profile_updated_date",friend.getProfileUpdatedDate());
        try {
            db.insertOrThrow("tbl_friends_list", null, values);
        }
        catch(Exception e){
        }
    }

//    functions to deal with tbl_friends_list
    public void addFriends(ArrayList<ChatFriend> friends){

        if(getCount(Const.DB_TABLE.TBL_FRIENDS)>0) {
//            if friends table have some data, add or update
            for (ChatFriend friend : friends) {
                addOrUpdateFriend(friend);
            }
        }
        else{
//            if friends table have no data, just add
            for (ChatFriend friend : friends) {
                addFriend(friend);
            }
        }
    }

//    update friend
    public void updateFriend(ChatFriend  friend){

//        Log.d(TAG, "updateFriend: worked"+friend.getProfileThumb());
        if(db==null){
            db=this.getWritableDatabase();
        }
        db.execSQL("update tbl_friends_list set " +
                " jabber_id='"+friend.getJabberId()+"',name='"+friend.getName()+"',subscription_status='"+friend.getSubscriptionStatus()+"'" +
                ",block_status='"+friend.getBlockStatus()+"',profile_thumb='"+friend.getProfileThumb()+"'" +
                ",profile_updated_date='"+friend.getProfileUpdatedDate()+"'" +
                " where  phone='"+friend.getPhone()+"' ");
//        db.update("tbl_friends_list",values,"phone",new String[]{friend.getPhone()});

    }

    public boolean clearFriendList(){
        if(db==null){
            db=this.getWritableDatabase();
        }
        try {
            db.execSQL("delete from tbl_friends_list");
            return true;
        }catch (Exception e){
            return false;
        }
    }

//    get friend
    public ChatFriend getFriendByPhone(String phone){
        //        profile_thumb ,profile_updated_date
        ChatFriend friend=new ChatFriend();
        if(db==null){
            db=this.getWritableDatabase();
        }
        Cursor cur=db.rawQuery("select fri_id,jabber_id,name,phone,subscription_status,block_status" +
                ",profile_thumb ,profile_updated_date" +
                " from tbl_friends_list where phone='"+phone+"'",null);
        if(cur.moveToFirst()){
            friend.setId(cur.getInt(0));
            friend.setJabberId(cur.getString(1));
            friend.setName(cur.getString(2));
            friend.setPhone(cur.getString(3));
            friend.setSubscriptionStatus(cur.getInt(4));
            friend.setBlockStatus(cur.getInt(5));
            friend.setProfileThumb(cur.getString(6));
            friend.setProfileUpdatedDate(cur.getString(7));
        }
        else{
            return  null;
        }
        cur.close();
        return  friend;
    }

    public ChatFriend getFriendByJabberId(String jabberid){
        //        profile_thumb ,profile_updated_date
        ChatFriend friend=new ChatFriend();
        if(db==null){
            db=this.getWritableDatabase();
        }
        Cursor cur=db.rawQuery("select fri_id,jabber_id,name,phone,subscription_status,block_status" +
                ",profile_thumb ,profile_updated_date" +
                " from tbl_friends_list where jabber_id='"+jabberid+"'",null);
        if(cur.moveToFirst()){
            friend.setId(cur.getInt(0));
            friend.setJabberId(cur.getString(1));
            friend.setName(cur.getString(2));
            friend.setPhone(cur.getString(3));
            friend.setSubscriptionStatus(cur.getInt(4));
            friend.setBlockStatus(cur.getInt(5));
            friend.setProfileThumb(cur.getString(6));
            friend.setProfileUpdatedDate(cur.getString(7));
        }
        else{
            return  null;
        }
        cur.close();
        return  friend;
    }
//methods to get all friends: 2 overriding methods
//    overriding method with 2 args
    public ArrayList<ChatFriend> getAllFriends(Const.FRIEND_SUBSCRIPTION_STATUS friend_subscription_status,String searchValue){
        ArrayList<ChatFriend> friends=new ArrayList<ChatFriend>();
        if(db==null){
            db=this.getWritableDatabase();
        }
        String condition="where 1=1";
        if(friend_subscription_status== Const.FRIEND_SUBSCRIPTION_STATUS.ALL){
            condition+="";
        }
        else{
            condition+=" and subscription_status='"+friend_subscription_status.getValue()+"' ";
        }

        if(searchValue!=null && !searchValue.equals("")){
            condition+=" and (name like '%"+searchValue+"%' or phone like '%"+searchValue+"%') ";
        }

        Cursor cursor = db.rawQuery("select fri_id,jabber_id,name,phone,subscription_status,block_status" +
                ",profile_thumb ,profile_updated_date" +
                " from tbl_friends_list "+condition+" order by subscription_status desc,name asc", null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ChatFriend friend = new ChatFriend();
                friend.setId(cursor.getInt(0));
                friend.setJabberId(cursor.getString(1));
                friend.setName(cursor.getString(2));
                friend.setPhone(cursor.getString(3));
                friend.setSubscriptionStatus(cursor.getInt(4));
                friend.setBlockStatus(cursor.getInt(5));
                friend.setProfileThumb(cursor.getString(6));
                friend.setProfileUpdatedDate(cursor.getString(7));
                // Adding contact to list
                friends.add(friend);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return friends;
    }
// overriding method with only 1 arg
    public ArrayList<ChatFriend> getAllFriends(Const.FRIEND_SUBSCRIPTION_STATUS friend_subscription_status){
        return  getAllFriends(friend_subscription_status,null);
    }

    //    functions to deal with tbl_messages
    public long addMessage(ChatMessage message){
        long ret;
        try {
            if(db==null){
                db=this.getWritableDatabase();
            }
            ContentValues values = new ContentValues();

            values.put("xmpp_id", message.getXMPPID());
            values.put("msg_receipt_id", message.getReceiptID());

            values.put("msg_from", message.getFrom());
            values.put("msg_to", message.getTo());
            values.put("msg_body", message.getBody());
            values.put("msg_date", message.getDate());
            values.put("msg_status", message.getStatus());

            values.put("msg_type", message.getType());
            values.put("msg_file_name", message.getFileName());
            values.put("msg_file_thumb", message.getThumb());
            values.put("msg_file_status", message.getFileStatus());
            if(message.getShowStatus()!=null) {
                values.put("msg_show_status", message.getShowStatus());
            }

//            Log.d(TAG, "addMessage: value status-"+values.getAsString("msg_show_status"));
            //Log.d("Adding msg", "addMessage: "+message.getBody()+"-"+message.getFrom()+"-"+message.getTo()+"-"+message.getXMPPID()+"-"+message.getDate()+"-"+message.getStatus()+"-");
            ret = db.insertOrThrow("tbl_messages", null, values);
//            Log.d(TAG, "addMessage: return"+ret);

        }catch (Exception e){
//            Log.d(TAG, "addMessage: exception-"+e.getMessage());
            e.printStackTrace();
            ret= -1;
        }
//        Log.d(TAG, "addMessage: "+ret);
        return ret;
    }


//    to update message
    public void updateMessage(ChatMessage message){
        if(db==null){
            db=this.getWritableDatabase();
        }
        String qry="update tbl_messages set" +
                " xmpp_id='"+message.getXMPPID()+"'" +
                " ,msg_status='"+message.getStatus()+"'" +
                " ,msg_file_status='"+message.getFileStatus()+"'" +
                " ,msg_receipt_id='"+message.getReceiptID()+"'" +
                " ,msg_date='"+message.getDate()+"'" +
                " ,msg_show_status='"+message.getShowStatus()+"'" +
                " where msg_id='"+message.getID()+"'";
        db.execSQL(qry);


    }

//    update message status
    public void updateAllMessageStatus(String chatid,Message_Direction message_direction,Message_Status message_status,Message_Status condition_status){
        if(db==null){
            db=this.getWritableDatabase();
        }
        String condition="where 1=1";
        int status;
        if(message_status==Message_Status.SENT){
            status=ChatMessage.STATUS_SENT;
        }
        else if(message_status==Message_Status.DELIVERED){
            status=ChatMessage.STATUS_DELIVERED;
        }
        else if(message_status==Message_Status.READ){
            status=ChatMessage.STATUS_READ;
        }
        else if(message_status==Message_Status.READ_ACK){
            status=ChatMessage.STATUS_READ_ACK;
        }
        else if(message_status==Message_Status.UNSENT){
            status=ChatMessage.STATUS_UNSENT;
        }
        else if(message_status==Message_Status.PENDING){
            status=ChatMessage.STATUS_PENDING;
        }
        else{
            status=ChatMessage.STATUS_UNSENT;
        }
//
        if(message_direction==Message_Direction.INCOMING){
            condition+=" and msg_from='"+chatid+"'";
        }
        else if(message_direction==Message_Direction.OUTGOING){
            condition+=" and msg_to='"+chatid+"'";
        }
        else{
            condition+=" and ( msg_to='"+chatid+"' or msg_from='"+chatid+"' )";
        }

//        checking condition status
        if(condition_status==Message_Status.SENT){
            condition+=" and msg_status='"+ChatMessage.STATUS_SENT+"'";
        }
        else if(condition_status==Message_Status.DELIVERED){
            condition+=" and msg_status='"+ChatMessage.STATUS_DELIVERED+"'";
        }
        else if(condition_status==Message_Status.READ){
            condition+=" and msg_status='"+ChatMessage.STATUS_READ+"'";
        }
        else if(condition_status==Message_Status.READ_ACK){
            condition+=" and msg_status='"+ChatMessage.STATUS_READ_ACK+"'";
        }
        else if(condition_status==Message_Status.UNREAD){
            condition+=" and msg_status<>'"+ChatMessage.STATUS_READ_ACK+"'";
        }
        else if(condition_status==Message_Status.UNSENT){
            condition+=" and msg_status='"+ChatMessage.STATUS_UNSENT+"'";
        }
        else if(condition_status==Message_Status.PENDING){
            condition+=" and msg_status='"+ChatMessage.STATUS_PENDING+"'";
        }
        else{
            condition+=" and msg_status<>'"+status+"'";
        }


//
        String qry="update tbl_messages set msg_status='"+status+"' "+condition;
//        Log.d("db", "updateAllMessageStatus: qry-"+qry);
        db.execSQL(qry);
    }
//overridden function without a condition_status
    public void updateAllMessageStatus(String chatid,Message_Direction message_direction,Message_Status message_status){
        updateAllMessageStatus(chatid,message_direction,message_status,null);
    }
    //to Delete message
    public void deleteMessage(ChatMessage message){
        if(db==null){
            db=this.getWritableDatabase();
        }
        String qry="delete from tbl_messages where msg_id='"+message.getID()+"'";
        db.execSQL(qry);

    }

//    to delete all messages from a chat
    public void deleteAllMessages(String chatId){
        if(db==null){
            db=this.getWritableDatabase();
        }
        String qry="delete from tbl_messages where ( msg_to='"+chatId+"' or msg_from='"+chatId+"' )";
        db.execSQL(qry);
    }

//    to get a single message of a particular id
    public ChatMessage getMessageById(String id){
        ChatMessage msg=new ChatMessage();
        if(db==null){
            db=this.getWritableDatabase();
        }
        Cursor cursor = db.rawQuery("select msg_id ,xmpp_id ,msg_from , msg_to , msg_body , msg_date  ,msg_status," +
                "msg_type ,msg_file_name ,msg_file_thumb ,msg_file_status,msg_receipt_id,msg_show_status from tbl_messages where msg_id='"+id+"' ", null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {

                msg.setId(cursor.getInt(0));
                msg.setXmppId(cursor.getString(1));
                msg.setFrom(cursor.getString(2));
                msg.setTo(cursor.getString(3));
                msg.setBody(cursor.getString(4));
                msg.setDate(cursor.getString(5));
                msg.setStatus(cursor.getInt(6));
                msg.setType(cursor.getString(7));
                msg.setFileName(cursor.getString(8));
                msg.setThumb(cursor.getString(9));
                msg.setFileStatus(cursor.getInt(10));
                msg.setReceiptId(cursor.getString(11));
                msg.setShowStatus(cursor.getString(12));


        }
        else{
            msg=null;
        }
        cursor.close();
        return msg;
    }

    //    to get a single message of a particular receipt id
    public ChatMessage getMessageByReceiptId(String receiptId){
        ChatMessage msg=new ChatMessage();
        if(db==null){
            db=this.getWritableDatabase();
        }
        Cursor cursor = db.rawQuery("select msg_id ,xmpp_id ,msg_from , msg_to , msg_body , msg_date  ,msg_status," +
                "msg_type ,msg_file_name ,msg_file_thumb ,msg_file_status,msg_receipt_id,msg_show_status from tbl_messages where msg_receipt_id='"+receiptId+"' ", null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {

            msg.setId(cursor.getInt(0));
            msg.setXmppId(cursor.getString(1));
            msg.setFrom(cursor.getString(2));
            msg.setTo(cursor.getString(3));
            msg.setBody(cursor.getString(4));
            msg.setDate(cursor.getString(5));
            msg.setStatus(cursor.getInt(6));
            msg.setType(cursor.getString(7));
            msg.setFileName(cursor.getString(8));
            msg.setThumb(cursor.getString(9));
            msg.setFileStatus(cursor.getInt(10));
            msg.setReceiptId(cursor.getString(11));
            msg.setShowStatus(cursor.getString(12));

        }
        else{
            msg=null;
        }
        cursor.close();
        return msg;
    }

//    to get all messages of a particular chat
    public ArrayList<ChatMessage> getMessagesFromChat(String ChatId){
        ArrayList<ChatMessage> messages=new ArrayList<ChatMessage>();
        if(db==null){
            db=this.getWritableDatabase();
        }
//        //Log.d("Getting msgs", "getMessagesFromChat: chatid-"+ChatId);
        Cursor cursor = db.rawQuery("select msg_id ,xmpp_id ,msg_from , msg_to , msg_body , msg_date  ,msg_status," +
                "msg_type ,msg_file_name ,msg_file_thumb ,msg_file_status,msg_receipt_id,msg_show_status from tbl_messages where msg_show_status<>'"+Const.MESSAGE_SHOW_STATUS.HIDE.toString()+"' and ( msg_from='"+ChatId+"' or msg_to='"+ChatId+"' ) ", null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ChatMessage msg=new ChatMessage();
                msg.setId(cursor.getInt(0));
                msg.setXmppId(cursor.getString(1));
                msg.setFrom(cursor.getString(2));
                msg.setTo(cursor.getString(3));
                msg.setBody(cursor.getString(4));
                msg.setDate(cursor.getString(5));
                msg.setStatus(cursor.getInt(6));
                msg.setType(cursor.getString(7));
                msg.setFileName(cursor.getString(8));
                msg.setThumb(cursor.getString(9));
                msg.setFileStatus(cursor.getInt(10));
                msg.setReceiptId(cursor.getString(11));
                msg.setShowStatus(cursor.getString(12));
//                Log.d(TAG, "getMessagesFromChat: status-"+cursor.getString(12)+" id-"+cursor.getInt(0));
                // Adding contact to list
                messages.add(msg);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return messages;
    }

//    to get all messages
    public ArrayList<ChatMessage> getMessages(Message_Direction msg_direction,Message_Type type,Message_Status msg_status,File_Status file_status,String[] chatPartners){
        ArrayList<ChatMessage> messages=new ArrayList<ChatMessage>();
        if(db==null){
            db=this.getWritableDatabase();
        }
        String whereCondition="where 1=1";
//        checking message status condition
        if(msg_status==Message_Status.SENT){
            whereCondition+=" and msg_status='"+ChatMessage.STATUS_SENT+"'";
        }
        else if(msg_status==Message_Status.DELIVERED){
            whereCondition+=" and msg_status='"+ChatMessage.STATUS_DELIVERED+"'";
        }
        else if(msg_status==Message_Status.READ){
            whereCondition+=" and msg_status='"+ChatMessage.STATUS_READ+"'";
        }
        else if(msg_status==Message_Status.READ_ACK){
            whereCondition+=" and msg_status='"+ChatMessage.STATUS_READ_ACK+"'";
        }
        else if(msg_status==Message_Status.UNREAD){
            whereCondition+=" and msg_status<>'"+ChatMessage.STATUS_READ+"'";
        }
        else if(msg_status==Message_Status.UNSENT){
            whereCondition+=" and msg_status='"+ChatMessage.STATUS_UNSENT+"'";
        }
        else if(msg_status==Message_Status.PENDING){
            whereCondition+=" and msg_status='"+ChatMessage.STATUS_PENDING+"'";
        }
        else if(msg_status==Message_Status.TOSENT){
            whereCondition+=" and (msg_status='"+ChatMessage.STATUS_UNSENT+"' or msg_status='"+ChatMessage.STATUS_PENDING+"')";
        }
        else{

        }

//        checking message type condition
        if(type==Message_Type.ALL_FILES){
//            whereCondition+=" and msg_type<>'"+ChatMessage.TYPE_TEXT+"' and msg_type<>'"+ChatMessage.TYPE_NOTIFICATION+"'";
            whereCondition+=" and ( msg_type='"+ChatMessage.TYPE_IMAGE+"' or msg_type='"+ChatMessage.TYPE_AUDIO+"' or msg_type='"+ChatMessage.TYPE_VIDEO+"' or msg_type='"+ChatMessage.TYPE_OTHERS+"' )";
        }
        else if(type==Message_Type.TEXTS){
            whereCondition+=" and msg_type='"+ChatMessage.TYPE_TEXT+"'";
        }
        else if(type==Message_Type.IMAGES){
            whereCondition+=" and msg_type='"+ChatMessage.TYPE_IMAGE+"'";
        }
        else if(type==Message_Type.AUDIOS){
            whereCondition+=" and msg_type='"+ChatMessage.TYPE_AUDIO+"'";
        }
        else if(type==Message_Type.VIDEOS){
            whereCondition+=" and msg_type='"+ChatMessage.TYPE_VIDEO+"'";
        }
        else if(type==Message_Type.OTHERS){
            whereCondition+=" and msg_type='"+ChatMessage.TYPE_OTHERS+"'";
        }
        else if(type==Message_Type.NOTIFICATION){
            whereCondition+=" and msg_type='"+ChatMessage.TYPE_NOTIFICATION+"'";
        }
        else{

        }


//        checking message direction
        if(msg_direction==Message_Direction.INCOMING){
            whereCondition+=" and msg_to='self' ";
            if(chatPartners!=null && chatPartners.length>0){
                whereCondition+= " and msg_from in ('"+ StringUtils.join(chatPartners,"','")+"')";
            }

        }
        else if(msg_direction==Message_Direction.OUTGOING){
            whereCondition+=" and msg_from='self' ";
            if(chatPartners!=null && chatPartners.length>0){
                whereCondition+= "  and msg_to in ('"+ StringUtils.join(chatPartners,"','")+"')";
            }
        }
        else{

        }

//        checking file status condition only if the message type is not text

        if(file_status==File_Status.SUCCESS){

            whereCondition+=" and (msg_file_status='1' or msg_type='"+ChatMessage.TYPE_TEXT+"' or msg_type='"+ChatMessage.TYPE_TRANSFER+"')";
        }
        else if(file_status==File_Status.PENDING){
            whereCondition+=" and (msg_file_status='0' or msg_type='"+ChatMessage.TYPE_TEXT+"' or msg_type='"+ChatMessage.TYPE_TRANSFER+"')";
        }
        else{
        }

//        Log.d(TAG, "getMessages: "+whereCondition);

        Cursor cursor = db.rawQuery("select msg_id ,xmpp_id ,msg_from , msg_to , msg_body , msg_date  ,msg_status," +
                "msg_type ,msg_file_name ,msg_file_thumb ,msg_file_status,msg_receipt_id,msg_show_status from tbl_messages "+whereCondition, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ChatMessage msg=new ChatMessage();
                msg.setId(cursor.getInt(0));
                msg.setXmppId(cursor.getString(1));
                msg.setFrom(cursor.getString(2));
                msg.setTo(cursor.getString(3));
                msg.setBody(cursor.getString(4));
                msg.setDate(cursor.getString(5));
                msg.setStatus(cursor.getInt(6));
                msg.setType(cursor.getString(7));
                msg.setFileName(cursor.getString(8));
                msg.setThumb(cursor.getString(9));
                msg.setFileStatus(cursor.getInt(10));
                msg.setReceiptId(cursor.getString(11));
                msg.setShowStatus(cursor.getString(12));
                // Adding contact to list
                messages.add(msg);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return  messages;
    }
// overiding method without recipient list
    public ArrayList<ChatMessage> getMessages(Message_Direction msg_direction,Message_Type type,Message_Status msg_status,File_Status file_status){
        return getMessages(msg_direction,type,msg_status,file_status,null);
    }
//to get count of messages
    public int getCountOfMessages(String chat_id,Message_Direction msg_direction,Message_Type type,Message_Status msg_status,File_Status file_status){

        String whereCondition="where 1=1";
//        checking chat id( from or to) condition
        if(chat_id!=null && !chat_id.trim().equals("")){
            //        checking message direction
            if(msg_direction==Message_Direction.INCOMING){
                whereCondition+=" and msg_from='"+chat_id+"'";
            }
            else if(msg_direction==Message_Direction.OUTGOING){
                whereCondition+=" and msg_to='"+chat_id+"'";
            }
            else{

            }
        }
        else{

//        checking message direction
            if(msg_direction==Message_Direction.INCOMING){
                whereCondition+=" and msg_to='self' and msg_from=''";
            }
            else if(msg_direction==Message_Direction.OUTGOING){
                whereCondition+=" and msg_from='self'";
            }
            else{

            }
        }
        //        checking message status condition
        if(msg_status==Message_Status.SENT){
            whereCondition+=" and msg_status='"+ChatMessage.STATUS_SENT+"'";
        }
        else if(msg_status==Message_Status.DELIVERED){
            whereCondition+=" and msg_status='"+ChatMessage.STATUS_DELIVERED+"'";
        }
        else if(msg_status==Message_Status.READ){
            whereCondition+=" and msg_status='"+ChatMessage.STATUS_READ+"'";
        }
        else if(msg_status==Message_Status.READ_ACK){
            whereCondition+=" and msg_status='"+ChatMessage.STATUS_READ_ACK+"'";
        }
        else if(msg_status==Message_Status.UNREAD){
            whereCondition+=" and msg_status<>'"+ChatMessage.STATUS_READ+"' and msg_status<>'"+ChatMessage.STATUS_READ_ACK+"'";
        }
        else if(msg_status==Message_Status.UNSENT){
            whereCondition+=" and msg_status='"+ChatMessage.STATUS_UNSENT+"'";
        }
        else if(msg_status==Message_Status.PENDING){
            whereCondition+=" and msg_status='"+ChatMessage.STATUS_PENDING+"'";
        }
        else if(msg_status==Message_Status.TOSENT){
            whereCondition+=" and (msg_status='"+ChatMessage.STATUS_UNSENT+"' or msg_status='"+ChatMessage.STATUS_PENDING+"')";
        }
        else{

        }

//        checking message type condition
        if(type==Message_Type.ALL_FILES){
            whereCondition+=" and ( msg_type='"+ChatMessage.TYPE_IMAGE+"' or msg_type='"+ChatMessage.TYPE_AUDIO+"' or msg_type='"+ChatMessage.TYPE_VIDEO+"' or msg_type='"+ChatMessage.TYPE_OTHERS+"' )";
        }
        else if(type==Message_Type.TEXTS){
            whereCondition+=" and msg_type='"+ChatMessage.TYPE_TEXT+"'";
        }
        else if(type==Message_Type.IMAGES){
            whereCondition+=" and msg_type='"+ChatMessage.TYPE_IMAGE+"'";
        }
        else if(type==Message_Type.AUDIOS){
            whereCondition+=" and msg_type='"+ChatMessage.TYPE_AUDIO+"'";
        }
        else if(type==Message_Type.VIDEOS){
            whereCondition+=" and msg_type='"+ChatMessage.TYPE_VIDEO+"'";
        }
        else if(type==Message_Type.OTHERS){
            whereCondition+=" and msg_type='"+ChatMessage.TYPE_OTHERS+"'";
        }
        else if(type==Message_Type.NOTIFICATION){
            whereCondition+=" and msg_type='"+ChatMessage.TYPE_NOTIFICATION+"'";
        }
        else{

        }


//        checking file status condition only if the message type is not text

        if(file_status==File_Status.SUCCESS){

            whereCondition+=" and (msg_file_status='1' or msg_type='"+ChatMessage.TYPE_TEXT+"')";
        }
        else if(file_status==File_Status.PENDING){
            whereCondition+=" and (msg_file_status='0' or msg_type='"+ChatMessage.TYPE_TEXT+"')";
        }
        else{

        }

        //Log.d("db", "getCountOfMessages: condition-"+whereCondition);

        int count=0;
        try {
            if(db==null){
                db=this.getWritableDatabase();
            }


            Cursor cursor = db.rawQuery("select count(*) from tbl_messages " + whereCondition, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        catch (Exception e){
            //Log.d("DBHandler", "getCountOfSendMessages: Exception");
        }
        return  count;

    }

    //    to get all friends who had a chat
    public ArrayList<ChatFriend> getAllChattingFriends(){
        ArrayList<ChatFriend> friends=new ArrayList<ChatFriend>();
        if(db==null){
            db=this.getWritableDatabase();
        }
        String qry="select fri_id,jabber_id,name,phone,subscription_status,block_status" +
                ",profile_thumb ,profile_updated_date" +
                ",msg_body,msg_date,msg_type,msg_status,msg_from from tbl_friends_list  " +
                "inner join tbl_messages  on (tbl_friends_list.phone=tbl_messages.msg_from or tbl_friends_list.phone=tbl_messages.msg_to) group by tbl_friends_list.phone order by msg_date desc";
        Cursor cursor = db.rawQuery(qry, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {

            do {
                //Log.d("DBHandler", "getAllChattingFriends: "+cursor.getInt(0));
                ChatFriend friend = new ChatFriend();
                friend.setId(cursor.getInt(0));
                friend.setJabberId(cursor.getString(1));
                friend.setName(cursor.getString(2));
                friend.setPhone(cursor.getString(3));
                friend.setSubscriptionStatus(cursor.getInt(4));
                friend.setBlockStatus(cursor.getInt(5));
                friend.setProfileThumb(cursor.getString(6));
                friend.setProfileUpdatedDate(cursor.getString(7));
                friend.LastMessage.setBody(cursor.getString(8));
                friend.LastMessage.setDate(cursor.getString(9));
                friend.LastMessage.setType(cursor.getString(10));
                friend.LastMessage.setStatus(cursor.getInt(11));
                friend.LastMessage.setFrom(cursor.getString(12));
                // Adding contact to list
                friends.add(friend);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return friends;
    }

    //    to get all friends who had a chat
    public ArrayList<Chat> getChats(String searchQuery){
        ArrayList<Chat> chats=new ArrayList<Chat>();
        if(db==null){
            db=this.getWritableDatabase();
        }
        if(searchQuery==null)searchQuery="";
        String qry="select * from (" +
                " select jabber_id as col0,name as col1,profile_thumb as col2,msg_body as col3,msg_date as col4," +
                " msg_type as col5, msg_status as col6,msg_from  as col7,'"+ Const.CHATBOX_TYPE.SINGLE.toString()+"' as col8,0 as col9" +
                " from tbl_friends_list inner join  tbl_messages " +
                " on (tbl_friends_list.jabber_id=tbl_messages.msg_from or tbl_friends_list.jabber_id=tbl_messages.msg_to) " +
                " where tbl_messages.msg_show_status<>'"+Const.MESSAGE_SHOW_STATUS.HIDE.toString()+"'" +
                " and name like '%"+searchQuery+"%' group by tbl_friends_list.phone " +
                " UNION" +
                " select tbl_group_master.grp_id as col0,grp_name as col1,profile_thumb as col2,grp_msg_body as col3,grp_msg_date as col4," +
                " grp_msg_type as col5, grp_msg_status as col6,grp_msg_from col7,'"+ Const.CHATBOX_TYPE.GROUP.toString()+"' as col8, event_type as col9 " +
                " from tbl_group_master inner join tbl_group_messages on (tbl_group_master.grp_id=tbl_group_messages.grp_id)" +
                " where tbl_group_messages.grp_msg_type<>'"+ChatMessage.TYPE_NOTIFICATION+"'" +
                " and grp_name like '%"+searchQuery+"%' group by tbl_group_master.grp_id " +
                " UNION " +
                " select tbl_group_master.grp_id as col0,grp_name as col1,profile_thumb as col2,null as col3,null as col4," +
                " null as col5,null as col6,null as col7,'"+ Const.CHATBOX_TYPE.GROUP.toString()+"' as col8 , event_type as col9" +
                " from tbl_group_master where tbl_group_master.grp_id not in " +
                " (select grp_id from tbl_group_messages where grp_msg_type<>'"+ChatMessage.TYPE_NOTIFICATION+"') " +
                " and grp_name like '%"+searchQuery+"%'" +
                " )" +
                " order by col4 desc";
        Cursor cursor = db.rawQuery(qry, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {

            do {
//                Log.d("DBHandler", "getChat : "+cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(3)+" "+cursor.getString(4)+" "+cursor.getString(5)+" "+cursor.getString(6)+" "+cursor.getString(7)+" "+" "+cursor.getString(8)+" "+" "+cursor.getString(9)+" ");
                Chat chat = new Chat();
                chat.setChatId(cursor.getString(0));
                chat.setChatName(cursor.getString(1));
                chat.setProfileThumb(cursor.getString(2));
                chat.LastMessage.setBody(cursor.getString(3));
                chat.LastMessage.setDate(cursor.getString(4));
                chat.LastMessage.setType(cursor.getString(5));
                chat.LastMessage.setStatus(cursor.getInt(6));
                chat.LastMessage.setFrom(cursor.getString(7));
                if(chat.LastMessage.getFrom()==null || chat.LastMessage.getBody()==null || chat.LastMessage.getBody()==null){
                    chat.LastMessage=null;
                }
                chat.setChatboxType(cursor.getString(8));
                chat.setEventType(cursor.getInt(9));
                // Adding contact to list
                chats.add(chat);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return chats;
    }

//Groups
//  add  group
    public boolean addGroup(ChatGroup group){
        boolean ret=false;
        try {
            if(db==null){
                db=this.getWritableDatabase();
            }
            ContentValues values = new ContentValues();
            values.put("grp_id", group.getGroupId());
            values.put("grp_name", group.getName());
            values.put("grp_owner", group.getOwner());
            values.put("grp_status", group.getStatus());
            values.put("grp_status_date", group.getStatusDateString());
            values.put("grp_type", group.getType());
            values.put("event_type", group.getEventType());
            values.put("event_target_amount", group.getTargetAmount());
            values.put("event_balance", group.getBalance());
            values.put("event_target_currency", group.getTargetCurrency());
            values.put("event_beneficiary_name", group.getBeneficiaryName());
            values.put("event_beneficiary_phone", group.getBeneficiaryPhone());
            values.put("profile_thumb", group.getProfileThumb());
            values.put("profile_updated_date", group.getProfileUpdatedDate());
            db.insertOrThrow("tbl_group_master", null, values);
            ret=true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }

    public boolean updateGroup(ChatGroup group){
        try {
            if(db==null){
                db=this.getWritableDatabase();
            }
//            Log.d(TAG, "updateGroup: ");
            ContentValues values = new ContentValues();
            values.put("grp_name", group.getName());
            values.put("grp_owner", group.getOwner());
            values.put("grp_status", group.getStatus());
            values.put("grp_status_date", group.getStatusDateString());
            values.put("grp_type", group.getType());
            values.put("event_type", group.getEventType());
            values.put("event_target_amount", group.getTargetAmount());
            values.put("event_balance", group.getBalance());
            values.put("event_target_currency", group.getTargetCurrency());
            values.put("event_beneficiary_name", group.getBeneficiaryName());
            values.put("event_beneficiary_phone", group.getBeneficiaryPhone());
            values.put("profile_thumb", group.getProfileThumb());
            values.put("profile_updated_date", group.getProfileUpdatedDate());
            int count=db.update(Const.DB_TABLE.TBL_GROUPS.toString(),values,"grp_id=?",new String[]{group.getGroupId()});
            if(count>0){
                return true;
            }
            else{
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

//    get group of a specified id
    public ChatGroup getGroup(String groupId){
        ChatGroup group=new ChatGroup();
        try {
            if(db==null){
                db=this.getWritableDatabase();
            }
            //grp_id , grp_name ,grp_owner ,grp_status ,grp_type  ,event_type ,event_target_amount ,
            // event_target_currency ,event_beneficiary_name ,event_beneficiary_phone
            String qry="select grp_id,grp_name,grp_owner,grp_status,grp_type ,event_type ,event_target_amount " +
                    ",event_target_currency ,event_beneficiary_name ,event_beneficiary_phone" +
                    ",profile_thumb,profile_updated_date,grp_status_date,event_balance from tbl_group_master where grp_id='"+groupId+"'";
//            Log.d(TAG, "getGroup: query-"+qry);
            Cursor cursor=db.rawQuery(qry,null);
//            Log.d(TAG, "getGroup: len="+cursor.getCount());
            if(cursor.moveToFirst()){
//                Log.d(TAG, "getGroup: "+cursor.getString(0)+" "+cursor.getString(1)+" "+cursor.getString(2));
                group.setGroupId(cursor.getString(0));
                group.setName(cursor.getString(1));
                group.setOwner(cursor.getString(2));
                group.setStatus(cursor.getInt(3));
                group.setType(cursor.getInt(4));
                group.setEventType(cursor.getInt(5));
                group.setTargetAmount(cursor.getFloat(6));
                group.setTargetCurrency(cursor.getString(7));
                group.setBeneficiaryName(cursor.getString(8));
                group.setBeneficiaryPhone(cursor.getString(9));
                group.setProfileThumb(cursor.getString(10));
                group.setProfileUpdatedDate(cursor.getString(11));
                group.setStatusDate(cursor.getString(12));
                group.setBalance(cursor.getFloat(13));// event_balance
//                Log.d(TAG, "getGroup: thumb-"+group.getProfileThumb());
            }else{
                return null;
            }
            cursor.close();

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return  group;
    }

    //    get all groups with chat
    public ArrayList<ChatGroup> getGroups(Const.GROUP_STATUS group_status){

        ArrayList<ChatGroup> groups=new ArrayList<ChatGroup>();
        if(db==null){
            db=this.getWritableDatabase();
        }
        String qry="select grp_id,grp_name,grp_owner,grp_status,grp_type ,event_type ,event_target_amount "
                 +",event_target_currency ,event_beneficiary_name ,event_beneficiary_phone" +
                ",profile_thumb,profile_updated_date,grp_status_date,event_balance" +
                " from tbl_group_master where grp_status='"+group_status.getValue()+"'" ;
        Cursor cursor=db.rawQuery(qry,null);
//        Log.d(TAG, "getGroups: count-"+cursor.getCount());
        if(cursor.moveToFirst()){
            do{
                ChatGroup group=new ChatGroup();
                group.setGroupId(cursor.getString(0));
                group.setName(cursor.getString(1));
                group.setOwner(cursor.getString(2));
                group.setStatus(cursor.getInt(3));
                group.setType(cursor.getInt(4));
                group.setEventType(cursor.getInt(5));
                group.setTargetAmount(cursor.getFloat(6));
                group.setTargetCurrency(cursor.getString(7));
                group.setBeneficiaryName(cursor.getString(8));
                group.setBeneficiaryPhone(cursor.getString(9));
                group.setProfileThumb(cursor.getString(10));
                group.setProfileUpdatedDate(cursor.getString(11));
                group.setStatusDate(cursor.getString(12));
                group.setBalance(cursor.getFloat(13));// event_balance
                groups.add(group);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return groups;
    }

//    get all groups with chat
    public ArrayList<ChatGroup> getGroupsWithChat(){

        ArrayList<ChatGroup> groups=new ArrayList<ChatGroup>();
        if(db==null){
            db=this.getWritableDatabase();
        }
        String qry="select tbl_group_master.grp_id,grp_name,grp_owner,grp_status,grp_msg_from,grp_msg_body,grp_msg_date,grp_msg_status,grp_msg_type" +
                " from tbl_group_master inner join tbl_group_messages on (tbl_group_master.grp_id=tbl_group_messages.grp_id)" +
                " where tbl_group_messages.grp_msg_type<>'"+ChatMessage.TYPE_NOTIFICATION+"'" +
                " group by tbl_group_master.grp_id " +
                " UNION select tbl_group_master.grp_id,grp_name,grp_owner,grp_status,null as grp_msg_from,null as grp_msg_body," +
                " null as grp_msg_date,null as grp_msg_status,null as grp_msg_type " +
                " from tbl_group_master where tbl_group_master.grp_id not in " +
                " (select grp_id from tbl_group_messages where grp_msg_type<>'"+ChatMessage.TYPE_NOTIFICATION+"') " +
                " order by tbl_group_messages.grp_msg_date desc";
        Cursor cursor=db.rawQuery(qry,null);
//        Log.d(TAG, "getGroups: count-"+cursor.getCount());
        if(cursor.moveToFirst()){
            do{
                ChatGroup group=new ChatGroup();
                group.setGroupId(cursor.getString(0));
                group.setName(cursor.getString(1));
                group.setOwner(cursor.getString(2));
                group.setStatus(cursor.getInt(3));
//                Log.d("db", "getGroups: "+cursor.getString(3)+" "+cursor.getString(4)+" "+cursor.getString(5)+" ");
                group.LastMessage.setFrom(cursor.getString(4));
                group.LastMessage.setBody(cursor.getString(5));
                group.LastMessage.setDate(cursor.getString(6));
                group.LastMessage.setStatus(cursor.getInt(7));
                group.LastMessage.setType(cursor.getString(8));
//                checking if message is null
                if(group.LastMessage.getFrom()==null || group.LastMessage.getBody()==null || group.LastMessage.getBody()==null){
                    group.LastMessage=null;
                }
//                add group to groups
                groups.add(group);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return groups;
    }

//    add group members
    public boolean addGroupMember(GroupMember member){
//        tbl_group_members (grp_mem_id INTEGER PRIMARY KEY AUTOINCREMENT, grp_id TEXT,grp_mem_phone TEXT
        try {
            if(db==null){
                db=this.getWritableDatabase();
            }
            ContentValues values = new ContentValues();
            values.put("grp_id", member.getGroupId());
            values.put("jabber_id", member.getJabberId());
            values.put("grp_mem_phone", member.getPhone());
            values.put("grp_mem_name", member.getName());
            db.insertOrThrow("tbl_group_members", null, values);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean removeGroupMember(GroupMember member){
        try{
            if(db==null){
                db=this.getWritableDatabase();
            }
            String qry="delete from tbl_group_members where grp_id='"+member.getGroupId()+"' and jabber_id='"+member.getJabberId()+"'";
            db.execSQL(qry);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

//    to remove all members
    public boolean removeAllMembers(String groupId){
        try{
            if(db==null){
                db=this.getWritableDatabase();
            }
            String qry="delete from tbl_group_members where grp_id='"+groupId+"'";
//            Log.d(TAG, "removeAllMembers: "+qry);
            db.execSQL(qry);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

//    to get all group members by groupId
    public ArrayList<GroupMember> getGroupMembers(String groupId){
//        tbl_friends_list (fri_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,name text,phone
        ArrayList<GroupMember> members=new ArrayList<GroupMember>();
        try {
            if(db==null){
                db=this.getWritableDatabase();
            }
            String qry="select grp_mem_id,tbl_group_members.jabber_id,grp_id,grp_mem_phone,grp_mem_name" +
                    ",tbl_friends_list.name,tbl_friends_list.profile_thumb " +
                    " from tbl_group_members left join tbl_friends_list" +
                    " on (tbl_group_members.jabber_id=tbl_friends_list.jabber_id)  where grp_id='"+groupId+"'";
            Cursor cursor=db.rawQuery(qry,null);
            if(cursor.moveToFirst()){
                do{
                    GroupMember member=new GroupMember();
                    member.setGroupMemberId(cursor.getInt(0));
                    member.setJabberId(cursor.getString(1));
                    member.setGroupId(cursor.getString(2));
                    member.setPhone(cursor.getString(3));
                    member.setName(cursor.getString(4));
//                    Log.d(TAG, "getGroupMembers: name-"+cursor.getString(4));
                    if(cursor.getString(5)==null){
                        member.FriendObject=null;
                    }
                    else{
                        member.FriendObject.setName(cursor.getString(5));
                        member.FriendObject.setProfileThumb(cursor.getString(6));
                    }
                    members.add(member);
                }while(cursor.moveToNext());
            }
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return  members;
    }

    //    to get a group members by groupId
    public GroupMember getGroupMember(String groupId,String memJabberId){

        try {
            if(db==null){
                db=this.getWritableDatabase();
            }
            String qry="select grp_mem_id,tbl_group_members.jabber_id,grp_id,grp_mem_phone,grp_mem_name" +
                    ",tbl_friends_list.name,tbl_friends_list.profile_thumb " +
                    " from tbl_group_members left join tbl_friends_list" +
                    " on (tbl_group_members.jabber_id=tbl_friends_list.jabber_id)  where grp_id='"+groupId+"' and tbl_group_members.jabber_id='"+memJabberId+"'";
            Cursor cursor=db.rawQuery(qry,null);
            GroupMember member=new GroupMember();
            if(cursor.moveToFirst()){

                    member.setGroupMemberId(cursor.getInt(0));
                    member.setJabberId(cursor.getString(1));
                    member.setGroupId(cursor.getString(2));
                    member.setPhone(cursor.getString(3));
                    member.setName(cursor.getString(4));
//                    Log.d(TAG, "getGroupMembers: name-"+cursor.getString(4));
                    if(cursor.getString(5)==null){
                        member.FriendObject=null;
                    }
                    else{
                        member.FriendObject.setName(cursor.getString(5));
                        member.FriendObject.setProfileThumb(cursor.getString(6));
                    }
            }
            cursor.close();
            return member;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

//    to add a new group message
    public long addGroupMessage(GroupMessage message){

        long ret=-1;
        try{
            if(db==null){
                db=this.getWritableDatabase();
            }
            ContentValues values = new ContentValues();
            values.put("grp_id",message.getGroupId());
            values.put("packet_id",message.getPacketId());

            values.put("grp_msg_from",message.getFrom());
            values.put("grp_msg_body",message.getBody());
            values.put("grp_msg_date",message.getDate());
            values.put("grp_msg_status",message.getStatus());

            values.put("grp_msg_type",message.getType());
            values.put("grp_msg_file_name",message.getFileName());
            values.put("grp_msg_file_thumb",message.getThumb());
            values.put("grp_msg_file_status",message.getFileStatus());

            //Log.d("Adding msg", "addMessage: "+message.getBody()+"-"+message.getFrom()+"-"+message.getTo()+"-"+message.getXMPPID()+"-"+message.getDate()+"-"+message.getStatus()+"-");
            ret=db.insertOrThrow("tbl_group_messages",null,values);

        }catch (Exception e){
            e.printStackTrace();
            ret=-1;
        }
        return ret;
    }

    //    to update group message
    public void updateGroupMessage(GroupMessage message){
        if(db==null){
            db=this.getWritableDatabase();
        }
        String qry="update tbl_group_messages set" +
                " grp_msg_status='"+message.getStatus()+"'" +
                " ,grp_msg_file_status='"+message.getFileStatus()+"'" +
                " where grp_msg_id='"+message.getID()+"'";
        db.execSQL(qry);


    }

    //    update group message status
    public void updateGroupMessageStatus(String groupId,Message_Direction message_direction,Message_Status message_status){
        if(db==null){
            db=this.getWritableDatabase();
        }
        String condition="where 1=1";
        int status;
        if(message_status==Message_Status.SENT){
            status=ChatMessage.STATUS_SENT;
        }
        else if(message_status==Message_Status.DELIVERED){
            status=ChatMessage.STATUS_DELIVERED;
        }
        else if(message_status==Message_Status.READ){
            status=ChatMessage.STATUS_READ;
        }
        else if(message_status==Message_Status.READ_ACK){
            status=ChatMessage.STATUS_READ_ACK;
        }
        else if(message_status==Message_Status.UNSENT){
            status=ChatMessage.STATUS_UNSENT;
        }
        else if(message_status==Message_Status.PENDING){
            status=ChatMessage.STATUS_PENDING;
        }
        else{
            status=ChatMessage.STATUS_UNSENT;
        }
//
        if(message_direction==Message_Direction.INCOMING){
            condition+=" and grp_id='"+groupId+"' and grp_msg_from<>'self'";
        }
        else if(message_direction==Message_Direction.OUTGOING){
            condition+=" and grp_id='"+groupId+"' and grp_msg_from='self'";

        }
        else{
            condition+=" and grp_id='"+groupId+"'";
        }



        condition+=" and grp_msg_status<>"+status;

//
        String qry="update tbl_group_messages set grp_msg_status='"+status+"' "+condition;
//        Log.d("db", "updateAllMessageStatus: qry-"+qry);
        db.execSQL(qry);
    }

    //to Delete group message
    public boolean deleteGroupMessage(GroupMessage message){
        try {
            if (db == null) {
                db = this.getWritableDatabase();
            }
            String qry = "delete from tbl_group_messages where grp_msg_id='" + message.getID() + "'";
            db.execSQL(qry);
            return  true;
        }catch (Exception e){
            e.printStackTrace();
            return  false;
        }
    }

//    to delete all group messages
    public boolean deleteAllGroupMessages(String grp_id){
        try {
            if (db == null) {
                db = this.getWritableDatabase();
            }
            String qry = "delete from tbl_group_messages where grp_id='"+grp_id+"'";
            db.execSQL(qry);
            return  true;
        }catch (Exception e){
            e.printStackTrace();
            return  false;
        }
    }

    //    to get a single group message of a particular id
    public GroupMessage getGroupMessageById(String id){
        GroupMessage msg=new GroupMessage();
        if(db==null){
            db=this.getWritableDatabase();
        }
        Cursor cursor = db.rawQuery("select grp_msg_id ,grp_id ,packet_id, grp_msg_from ,grp_msg_body , grp_msg_date " +
                ",grp_msg_status ,grp_msg_type ,grp_msg_file_name , grp_msg_file_thumb ,grp_msg_file_status  " +
                "from tbl_group_messages where grp_msg_id='"+id+"' ", null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {

            msg.setId(cursor.getInt(0));
            msg.setGroupId(cursor.getString(1));
            msg.setPacketId(cursor.getString(2));
            msg.setFrom(cursor.getString(3));
            msg.setBody(cursor.getString(4));
            msg.setDate(cursor.getString(5));
            msg.setStatus(cursor.getInt(6));
            msg.setType(cursor.getString(7));
            msg.setFileName(cursor.getString(8));
            msg.setThumb(cursor.getString(9));
            msg.setFileStatus(cursor.getInt(10));

        }
        cursor.close();
        return msg;
    }

    //    to get a single group message of a particular packet id
    public GroupMessage getGroupMessageByPacketId(String packetid){
        GroupMessage msg=new GroupMessage();
        if(db==null){
            db=this.getWritableDatabase();
        }
        Cursor cursor = db.rawQuery("select grp_msg_id ,grp_id ,packet_id, grp_msg_from ,grp_msg_body , grp_msg_date " +
                ",grp_msg_status ,grp_msg_type ,grp_msg_file_name , grp_msg_file_thumb ,grp_msg_file_status  " +
                "from tbl_group_messages where packet_id='"+packetid+"' ", null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {

            msg.setId(cursor.getInt(0));
            msg.setGroupId(cursor.getString(1));
            msg.setPacketId(cursor.getString(2));
            msg.setFrom(cursor.getString(3));
            msg.setBody(cursor.getString(4));
            msg.setDate(cursor.getString(5));
            msg.setStatus(cursor.getInt(6));
            msg.setType(cursor.getString(7));
            msg.setFileName(cursor.getString(8));
            msg.setThumb(cursor.getString(9));
            msg.setFileStatus(cursor.getInt(10));

        }
        cursor.close();
        return msg;
    }

    public GroupMessage getLastGroupMessage(String groupId,Message_Direction message_direction){
        String whereCondition="grp_id='"+groupId+"' ";
        if(message_direction==Message_Direction.INCOMING){
            whereCondition+=" and grp_msg_from<>'self'";
        }
        else if(message_direction==Message_Direction.OUTGOING){
            whereCondition+=" and grp_msg_from='self'";
        }
        else{

        }

        GroupMessage msg=new GroupMessage();
        if(db==null){
            db=this.getWritableDatabase();
        }
        Cursor cursor = db.rawQuery("select grp_msg_id ,grp_id ,packet_id, grp_msg_from ,grp_msg_body , grp_msg_date " +
                ",grp_msg_status ,grp_msg_type ,grp_msg_file_name , grp_msg_file_thumb ,grp_msg_file_status  " +
                "from tbl_group_messages where "+whereCondition+" order by grp_msg_id desc limit 1 ", null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {

            msg.setId(cursor.getInt(0));
            msg.setGroupId(cursor.getString(1));
            msg.setPacketId(cursor.getString(2));
            msg.setFrom(cursor.getString(3));
            msg.setBody(cursor.getString(4));
            msg.setDate(cursor.getString(5));
            msg.setStatus(cursor.getInt(6));
            msg.setType(cursor.getString(7));
            msg.setFileName(cursor.getString(8));
            msg.setThumb(cursor.getString(9));
            msg.setFileStatus(cursor.getInt(10));

        }
        else{
            return null;
        }
        cursor.close();
        return msg;
    }

    //    to get all messages of a particular group
    public ArrayList<GroupMessage> getMessagesFromGroup(String groupId){
        ArrayList<GroupMessage> messages=new ArrayList<GroupMessage>();
        if(db==null){
            db=this.getWritableDatabase();
        }
//        //Log.d("Getting msgs", "getMessagesFromChat: chatid-"+ChatId);
        Cursor cursor = db.rawQuery("select grp_msg_id ,grp_id ,packet_id, grp_msg_from ,grp_msg_body , grp_msg_date " +
                ",grp_msg_status ,grp_msg_type ,grp_msg_file_name , grp_msg_file_thumb ,grp_msg_file_status  " +
                "from tbl_group_messages where grp_id='"+groupId+"' ", null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                GroupMessage msg=new GroupMessage();
                msg.setId(cursor.getInt(0));
                msg.setGroupId(cursor.getString(1));
                msg.setPacketId(cursor.getString(2));
                msg.setFrom(cursor.getString(3));
                msg.setBody(cursor.getString(4));
                msg.setDate(cursor.getString(5));
                msg.setStatus(cursor.getInt(6));
                msg.setType(cursor.getString(7));
                msg.setFileName(cursor.getString(8));
                msg.setThumb(cursor.getString(9));
                msg.setFileStatus(cursor.getInt(10));
                // Adding contact to list
                messages.add(msg);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return messages;
    }

    //    to get all group messages
    public ArrayList<GroupMessage> getGroupMessages(Message_Direction msg_direction,Message_Type type,Message_Status msg_status,File_Status file_status){
        ArrayList<GroupMessage> messages=new ArrayList<GroupMessage>();
        if(db==null){
            db=this.getWritableDatabase();
        }
        String whereCondition="where 1=1";
//        checking message status condition
        if(msg_status==Message_Status.SENT){
            whereCondition+=" and grp_msg_status='"+ChatMessage.STATUS_SENT+"'";
        }
        else if(msg_status==Message_Status.DELIVERED){
            whereCondition+=" and grp_msg_status='"+ChatMessage.STATUS_DELIVERED+"'";
        }
        else if(msg_status==Message_Status.READ){
            whereCondition+=" and grp_msg_status='"+ChatMessage.STATUS_READ+"'";
        }
        else if(msg_status==Message_Status.READ_ACK){
            whereCondition+=" and grp_msg_status='"+ChatMessage.STATUS_READ_ACK+"'";
        }
        else if(msg_status==Message_Status.UNREAD){
            whereCondition+=" and grp_msg_status<>'"+ChatMessage.STATUS_READ+"'";
        }
        else if(msg_status==Message_Status.UNSENT){
            whereCondition+=" and grp_msg_status='"+ChatMessage.STATUS_UNSENT+"'";
        }
        else if(msg_status==Message_Status.PENDING){
            whereCondition+=" and grp_msg_status='"+ChatMessage.STATUS_PENDING+"'";
        }
        else if(msg_status==Message_Status.TOSENT){
            whereCondition+=" and (grp_msg_status='"+ChatMessage.STATUS_PENDING+"' or grp_msg_status='"+ChatMessage.STATUS_UNSENT+"')";
        }
        else{

        }

//        checking message type condition
        if(type==Message_Type.ALL_FILES){
//            whereCondition+=" and grp_msg_type<>'"+ChatMessage.TYPE_TEXT+"' and grp_msg_type<>'"+ChatMessage.TYPE_NOTIFICATION+"'";
            whereCondition+=" and ( grp_msg_type='"+ChatMessage.TYPE_IMAGE+"' or grp_msg_type='"+ChatMessage.TYPE_AUDIO+"' or grp_msg_type='"+ChatMessage.TYPE_VIDEO+"' or grp_msg_type='"+ChatMessage.TYPE_OTHERS+"' )";
        }
        else if(type==Message_Type.TEXTS){
            whereCondition+=" and grp_msg_type='"+ChatMessage.TYPE_TEXT+"'";
        }
        else if(type==Message_Type.IMAGES){
            whereCondition+=" and grp_msg_type='"+ChatMessage.TYPE_IMAGE+"'";
        }
        else if(type==Message_Type.AUDIOS){
            whereCondition+=" and grp_msg_type='"+ChatMessage.TYPE_AUDIO+"'";
        }
        else if(type==Message_Type.VIDEOS){
            whereCondition+=" and grp_msg_type='"+ChatMessage.TYPE_VIDEO+"'";
        }
        else if(type==Message_Type.OTHERS){
            whereCondition+=" and grp_msg_type='"+ChatMessage.TYPE_OTHERS+"'";
        }
        else if(type==Message_Type.NOTIFICATION){
            whereCondition+=" and grp_msg_type='"+ChatMessage.TYPE_NOTIFICATION+"'";
        }
        else{

        }

//        checking message direction
        if(msg_direction==Message_Direction.INCOMING){
            whereCondition+=" and grp_msg_from<>'self'";
        }
        else if(msg_direction==Message_Direction.OUTGOING){
            whereCondition+=" and grp_msg_from='self'";
        }
        else{

        }

//        checking file status condition only if the message type is not text

        if(file_status==File_Status.SUCCESS){

            whereCondition+=" and (grp_msg_file_status='1' or grp_msg_type='"+ChatMessage.TYPE_TEXT+"' or grp_msg_type='"+ChatMessage.TYPE_NOTIFICATION+"')";
        }
        else if(file_status==File_Status.PENDING){
            whereCondition+=" and (grp_msg_file_status='0' or grp_msg_type='"+ChatMessage.TYPE_TEXT+"' or grp_msg_type='"+ChatMessage.TYPE_NOTIFICATION+"')";
        }
        else{

        }


        Cursor cursor = db.rawQuery("select grp_msg_id ,grp_id ,packet_id, grp_msg_from ,grp_msg_body , grp_msg_date " +
                                ",grp_msg_status ,grp_msg_type ,grp_msg_file_name , grp_msg_file_thumb ,grp_msg_file_status  " +
                                "from tbl_group_messages "+whereCondition, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                GroupMessage msg=new GroupMessage();
                msg.setId(cursor.getInt(0));
                msg.setGroupId(cursor.getString(1));
                msg.setPacketId(cursor.getString(2));
                msg.setFrom(cursor.getString(3));
                msg.setBody(cursor.getString(4));
                msg.setDate(cursor.getString(5));
                msg.setStatus(cursor.getInt(6));
                msg.setType(cursor.getString(7));
                msg.setFileName(cursor.getString(8));
                msg.setThumb(cursor.getString(9));
                msg.setFileStatus(cursor.getInt(10));
                // Adding contact to list
                messages.add(msg);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return  messages;
    }

//    get count of group messages
    public int getGroupMessagesCount(String groupId,Message_Direction msg_direction,Message_Type type,Message_Status msg_status,File_Status file_status){

        String whereCondition="where 1=1";
//        checking chat id( from or to) condition
        if(groupId!=null && !groupId.trim().equals("")){
            //        checking message direction
            whereCondition+=" and grp_id='"+groupId+"'";

        }
        else{

        }
        //        checking message status condition
        if(msg_status==Message_Status.SENT){
            whereCondition+=" and grp_msg_status='"+ChatMessage.STATUS_SENT+"'";
        }
        else if(msg_status==Message_Status.DELIVERED){
            whereCondition+=" and grp_msg_status='"+ChatMessage.STATUS_DELIVERED+"'";
        }
        else if(msg_status==Message_Status.READ){
            whereCondition+=" and grp_msg_status='"+ChatMessage.STATUS_READ+"'";
        }
        else if(msg_status==Message_Status.READ_ACK){
            whereCondition+=" and grp_msg_status='"+ChatMessage.STATUS_READ_ACK+"'";
        }
        else if(msg_status==Message_Status.UNREAD){
            whereCondition+=" and grp_msg_status<>'"+ChatMessage.STATUS_READ+"' and grp_msg_status<>'"+ChatMessage.STATUS_READ_ACK+"'";
        }
        else if(msg_status==Message_Status.UNSENT){
            whereCondition+=" and grp_msg_status='"+ChatMessage.STATUS_UNSENT+"'";
        }
        else if(msg_status==Message_Status.PENDING){
            whereCondition+=" and grp_msg_status='"+ChatMessage.STATUS_PENDING+"'";
        }
        else if(msg_status==Message_Status.TOSENT){
            whereCondition+=" and (grp_msg_status='"+ChatMessage.STATUS_PENDING+"' or grp_msg_status='"+ChatMessage.STATUS_UNSENT+"')";
        }
        else{

        }

//        checking message type condition
        if(type==Message_Type.ALL_FILES){
//            whereCondition+=" and grp_msg_type<>'"+ChatMessage.TYPE_TEXT+"' and grp_msg_type<>'"+ChatMessage.TYPE_NOTIFICATION+"'";
            whereCondition+=" and ( grp_msg_type='"+ChatMessage.TYPE_IMAGE+"' or grp_msg_type='"+ChatMessage.TYPE_AUDIO+"' or grp_msg_type='"+ChatMessage.TYPE_VIDEO+"' or grp_msg_type='"+ChatMessage.TYPE_OTHERS+"' )";
        }
        else if(type==Message_Type.TEXTS){
            whereCondition+=" and grp_msg_type='"+ChatMessage.TYPE_TEXT+"'";
        }
        else if(type==Message_Type.IMAGES){
            whereCondition+=" and grp_msg_type='"+ChatMessage.TYPE_IMAGE+"'";
        }
        else if(type==Message_Type.AUDIOS){
            whereCondition+=" and grp_msg_type='"+ChatMessage.TYPE_AUDIO+"'";
        }
        else if(type==Message_Type.VIDEOS){
            whereCondition+=" and grp_msg_type='"+ChatMessage.TYPE_VIDEO+"'";
        }
        else if(type==Message_Type.OTHERS){
            whereCondition+=" and grp_msg_type='"+ChatMessage.TYPE_OTHERS+"'";
        }
        else if(type==Message_Type.NOTIFICATION){
            whereCondition+=" and grp_msg_type='"+ChatMessage.TYPE_NOTIFICATION+"'";
        }
        else{

        }

//        checking message direction
        if(msg_direction==Message_Direction.INCOMING){
            whereCondition+=" and grp_msg_from<>'self'";
        }
        else if(msg_direction==Message_Direction.OUTGOING){
            whereCondition+=" and grp_msg_from='self'";
        }
        else{

        }

//        checking file status condition only if the message type is not text

        if(file_status==File_Status.SUCCESS){

            whereCondition+=" and (grp_msg_file_status='1' or grp_msg_type='"+ChatMessage.TYPE_TEXT+"' or grp_msg_type='"+ChatMessage.TYPE_NOTIFICATION+"')";
        }
        else if(file_status==File_Status.PENDING){
            whereCondition+=" and (grp_msg_file_status='0' or grp_msg_type='"+ChatMessage.TYPE_TEXT+"' or grp_msg_type='"+ChatMessage.TYPE_NOTIFICATION+"')";
        }
        else{

        }

        //Log.d("db", "getCountOfMessages: condition-"+whereCondition);

        int count=0;
        try {
            if(db==null){
                db=this.getWritableDatabase();
            }


            Cursor cursor = db.rawQuery("select count(*) from tbl_group_messages " + whereCondition, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        catch (Exception e){
            //Log.d("DBHandler", "getCountOfSendMessages: Exception");
        }
        return  count;

    }

//    Transfer table operations
    public long addTransfer(Transfer transfer){
        long ret=-1;
        try{
            if(db==null){
                db=this.getWritableDatabase();
            }
            ContentValues values=new ContentValues();
            values.put("transfer_id",transfer.getTransferId());
            values.put("sender_phone",transfer.getSenderPhone());
            values.put("sender_account",transfer.getSenderAccount());
            values.put("receiver_phone",transfer.getReceiverPhone());
            values.put("amount",transfer.getAmount());
            values.put("currency",transfer.getCurrency());
            values.put("motive",transfer.getMotive());
            values.put("fees_paid_by",transfer.getFeesPaidBy());
            values.put("execution_date",transfer.getExecutionDate());
            values.put("request_date",transfer.getRequestDate());
            values.put("transfer_expiry",transfer.getExpiry());
            values.put("transfer_direction",transfer.getTransferDirection());
            values.put("transfer_status",transfer.getTransferStatus());
            ret=db.insertOrThrow("tbl_transfer",null,values);
        }catch (Exception e){
            e.printStackTrace();
            ret=-1;
        }
        return ret;
    }

    public int updateTransfer(Transfer transfer){
        int updated_count=0;
        try{
            if(db==null){
                db=this.getWritableDatabase();
            }
            ContentValues values=new ContentValues();
            values.put("transfer_id",transfer.getTransferId());
            values.put("sender_phone",transfer.getSenderPhone());
            values.put("sender_account",transfer.getSenderAccount());
            values.put("receiver_phone",transfer.getReceiverPhone());
            values.put("amount",transfer.getAmount());
            values.put("currency",transfer.getCurrency());
            values.put("motive",transfer.getMotive());
            values.put("fees_paid_by",transfer.getFeesPaidBy());
            values.put("execution_date",transfer.getExecutionDate());
            values.put("request_date",transfer.getRequestDate());
            values.put("transfer_expiry",transfer.getExpiry());
            values.put("transfer_direction",transfer.getTransferDirection());
            values.put("transfer_status",transfer.getTransferStatus());
            updated_count=db.update("tbl_transfer",values,"t_id=?",new String[]{String.valueOf(transfer.getId())});
        }catch (Exception e){
            e.printStackTrace();
        }
        return updated_count;
    }

    public Transfer getTransfer(String tranfer_id){
        Transfer transfer=null;
        try {
            if(db==null){
                db=this.getWritableDatabase();
            }
            String qry="select t_id,transfer_id,sender_phone, sender_account, receiver_phone , amount , currency " +
                    ", motive ,fees_paid_by , execution_date , request_date , transfer_expiry ,transfer_direction " +
                    ", transfer_status from tbl_transfer where transfer_id='"+tranfer_id+"'";
            Cursor cursor=db.rawQuery(qry,null);
            if(cursor.moveToFirst()){
                do{
                    transfer=new Transfer();
                    transfer.setId(cursor.getInt(0));
                    transfer.setTransferId(cursor.getString(1));
                    transfer.setSenderPhone(cursor.getString(2));
                    transfer.setSenderAccount(cursor.getString(3));
                    transfer.setReceiverPhone(cursor.getString(4));
                    transfer.setAmount(cursor.getFloat(5));
                    transfer.setCurrency(cursor.getString(6));
                    transfer.setMotive(cursor.getString(7));
                    transfer.setfeesPaidBy(cursor.getString(8));
                    transfer.setExecutionDate(cursor.getString(9));
                    transfer.setRequestDate(cursor.getString(10));
                    transfer.setExpiry(cursor.getString(11));
                    transfer.setTransferDirection(cursor.getInt(12));
                    transfer.setTransferStatus(cursor.getInt(13));
                }while (cursor.moveToNext());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return transfer;
    }

}
