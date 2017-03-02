package com.linkai.app.libraries;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.linkai.app.ChatApplication;
import com.linkai.app.GroupChatBoxActivity;
import com.linkai.app.R;
import com.linkai.app.SingleChatBoxActivity;
import com.linkai.app.modals.ChatMessage;
import com.linkai.app.modals.GroupMember;
import com.linkai.app.modals.GroupMessage;
import com.linkai.app.modals.Transfer;
import com.linkai.app.services.DownloadFileService;
import com.linkai.app.services.MainService;
import com.linkai.app.services.SendMessageService;
import com.linkai.app.services.SyncContactsService;
import com.linkai.app.services.SyncProfileService;
import com.linkai.app.services.SyncGroupsService;
import com.linkai.app.services.UploadFileService;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Locale;
import java.util.Set;

/**
 * Created by LP1001 on 04-07-2016.
 */
public class Common {
    private final String TAG="common";

    Context context;
    DatabaseHandler db;
    AudioManager audioManager;


    public Common(Context _context){
        this.context=_context;
        db=Const.DB;
//        init audiomanager
        audioManager =(AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//        create dirs
        createAppDirectories();
//        Log.d(TAG, "Common: cur_act "+Const.CUR_ACTIVITY);

    }

//    function to init startup services
    public void initServices(){
        if(!db.isUserExist()){
            return;
        }
        callSendMessageService();
        callUploadFileService();
        callSyncContactsService();
        callSyncGroupsService();
//        callProfileSyncService();
    }

//to start main service
    public void startMainService(){
        if(!db.isUserExist()){
            return;
        }
        if(!isMyServiceRunning(MainService.class)) {
            Intent mainserviceIntent=new Intent(context, MainService.class);
            context.startService(mainserviceIntent);
        }

    }

//    to stop main service
    public void stopMainService(){
        if(!isMyServiceRunning(MainService.class)) {
            Intent mainserviceIntent=new Intent(context, MainService.class);
            context.stopService(mainserviceIntent);
        }
    }
//    to start sendmessage background service
    public void callSendMessageService(){
        if((!SendMessageService.IS_ALIVE) && isNetworkAvailable()){
            Intent serviceIntent=new Intent(context,SendMessageService.class);
            context.startService(serviceIntent);
        }
    }

    //    to start sendmessage background service
    public void callUploadFileService(){
        if((!UploadFileService.IS_ALIVE) && isNetworkAvailable()){
            Intent serviceIntent=new Intent(context,UploadFileService.class);
            context.startService(serviceIntent);
        }
    }

    //    to call download file service
    public void callDownloadFileService(String msg_id,Const.CHATBOX_TYPE chatbox_type){
        if((!DownloadFileService.IS_ALIVE) && isNetworkAvailable()){
            Intent serviceIntent=new Intent(context,DownloadFileService.class).putExtra("msg_id",msg_id).putExtra("chatbox_type",chatbox_type);
            context.startService(serviceIntent);
        }
    }

    //    to call sync contacts service
    public void callSyncContactsService(){
        if((!SyncContactsService.IS_ALIVE) && isNetworkAvailable()){
            Intent serviceIntent=new Intent(context,SyncContactsService.class);
            context.startService(serviceIntent);
        }
    }

    //    to call join groups service
    public void callSyncGroupsService(){
        Log.d(TAG, "callSyncGroupsService: ");
        if((!SyncGroupsService.IS_ALIVE) && isNetworkAvailable()){
            Intent serviceIntent=new Intent(context,SyncGroupsService.class);
            context.startService(serviceIntent);
        }
    }

    //    to call friends profile sync service
    public void callProfileSyncService(){
        if((!SyncProfileService.IS_ALIVE) && isNetworkAvailable()){
            Intent serviceIntent=new Intent(context,SyncProfileService.class);
            context.startService(serviceIntent);
        }
    }

//    method to detect if a specified service is running
    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

//    to check if the device have network access
    public boolean isNetworkAvailable() {
    ConnectivityManager connectivityManager
            = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
}

    //    to show notification
    private void showNotification(String title,String message,Intent resultIntent,String notification_tag){
//        checking if app needs to show notification
//        avoiding notification if app is running in foreground and viewing task is chats in home
//        Log.d(TAG, "showNotification: cur_act "+Const.CUR_ACTIVITY+"-"+Const.CUR_FRAGMENT+"-"+(!isRunningInBackground()));
//        if((!isRunningInBackground()) && Const.CUR_ACTIVITY==Const.APP_COMPONENTS.HomeActivity && Const.CUR_FRAGMENT== Const.APP_COMPONENTS.ChatFragment){
//            Log.d(TAG, "showNotification: in cnd");
//            return;
//        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle(title);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setContentText(message);
        mBuilder.setSmallIcon(R.drawable.ic_stat_linkai_notification);
        mBuilder.setColor(context.getResources().getColor(R.color.colorPrimary));
        mBuilder.setAutoCancel(true);
        mBuilder.setPriority(Notification.PRIORITY_HIGH);

//        Intent parentIntent=new Intent(context,HomeActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(SingleChatBoxActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        // PendingIntent resultPendingIntent = PendingIntent.getActivities(context, 1,new Intent[] {resultIntent}, PendingIntent.FLAG_ONE_SHOT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

// notificationID allows you to update the notification later on.
        mNotificationManager.notify(notification_tag,0, mBuilder.build());
    }

    public void clearNotification(String tag){
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(tag,0);
    }

//    to send message notification
    public void showSingleChatNotification(String msgid){
        ChatMessage msg=db.getMessageById(msgid);
//        checking if app needs to show notification or not
//        conditions to avoid notification
//        1. app running foreground and viewing message sender's chatbox
//        2. app running in foreground and message read
        if((!isRunningInBackground()) && (
                (Const.CUR_ACTIVITY==Const.APP_COMPONENTS.SingleChatBoxActivity && Const.CUR_CHATID.equals(msg.getFrom()))
                    || msg.getStatus()==ChatMessage.STATUS_READ
            )){
            return;
        }

        String senderName;
        String msg_body="";
        try {
            senderName = db.getFriendByJabberId(msg.getFrom().trim()).getName();
        }
        catch (Exception e){
            senderName="";
        }
//        defining intent to show when clicking notification
        Intent resultIntent = new Intent(context, SingleChatBoxActivity.class).putExtra("chatId",msg.getFrom());
        if(msg.getType().equals(ChatMessage.TYPE_TEXT)){
            msg_body=msg.getBody();
        }
        else if(msg.getType().equals(ChatMessage.TYPE_IMAGE)){
            msg_body="Send an image";
        }
        else if(msg.getType().equals(ChatMessage.TYPE_VIDEO)){
            msg_body="Send a video";
        }
        else if(msg.getType().equals(ChatMessage.TYPE_AUDIO)){
            msg_body="Send an audio";
        }
        else if(msg.getType().equals(ChatMessage.TYPE_OTHERS)){
            msg_body="Send a file";
        }
        else if(msg.getType().equals(ChatMessage.TYPE_TRANSFER)){
            JSONObject transferJson= null;
            Transfer transfer;
            try {
                transferJson = new JSONObject(msg.getBody());
                transfer=db.getTransfer(transferJson.getString("transfer_id"));
                if(transfer.getTransferStatus()==Const.TRANSFER_STATUS.PENDING.getValue()){
                    msg_body="Linkai you. "+transfer.getAmount()+" "+transfer.getCurrency();
                }
                else if(transfer.getTransferStatus()==Const.TRANSFER_STATUS.ACCEPTED.getValue()){
                    msg_body="Accepted your linkai of "+transfer.getAmount()+" "+transfer.getCurrency();
                }
                else if(transfer.getTransferStatus()==Const.TRANSFER_STATUS.REJECTED.getValue()){
                    msg_body="Rejected your linkai of "+transfer.getAmount()+" "+transfer.getCurrency();
                }
                else{
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }
        else{
            return;
        }
//        show notification
        showNotification(senderName,msg_body,resultIntent,msg.getFrom());
    }

    public void showGroupChatNotification(String msgid){

        GroupMessage msg=db.getGroupMessageById(msgid);
//        checking if app needs to show notification or not
//        conditions to avoid notification
//        1. app running foreground and viewing message group's chatbox
//        2. app running in foreground and message read

        if((!isRunningInBackground()) && (
                (Const.CUR_ACTIVITY==Const.APP_COMPONENTS.GroupChatBoxActivity && Const.CUR_GROUPID.equals(msg.getGroupId()))
                        || msg.getStatus()==ChatMessage.STATUS_READ
        )){
            return;
        }
        String groupId=msg.getGroupId();
        GroupMember member=db.getGroupMember(groupId,msg.getFrom());
//        getting sender name
        String groupName=db.getGroup(groupId).getName();
        String senderName;
        try {
//                if sender is in friends list
            senderName = db.getFriendByJabberId(msg.getFrom()).getName();
        } catch (Exception e) {
//                if sender not is in friends list, exception will be raised
            senderName = member.getPhone();
        }
//        defining intent to show when clicking notification
        Intent resultIntent = new Intent(context, GroupChatBoxActivity.class).putExtra("groupId",groupId);
//        appending sender name with message body
        String msg_body=senderName+":";
        if(msg.getType().equals(ChatMessage.TYPE_TEXT)){
            msg_body+=msg.getBody();
        }
        else if(msg.getType().equals(ChatMessage.TYPE_IMAGE)){
            msg_body+="send an image";
        }
        else if(msg.getType().equals(ChatMessage.TYPE_VIDEO)){
            msg_body+="send a video";
        }
        else if(msg.getType().equals(ChatMessage.TYPE_AUDIO)){
            msg_body+="send an audio";
        }
        else if(msg.getType().equals(ChatMessage.TYPE_OTHERS)){
            msg_body+="send a file";
        }
        else{
            return;
        }
        //        creating notification id


//        show notification
        showNotification(groupName,msg_body,resultIntent,groupId);
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public void createAppDirectories(){
        if(!isExternalStorageReadable() || !isExternalStorageWritable()){
            return;
        }
        Resources res = context.getResources();
        File f;
        f = new File(Environment.getExternalStorageDirectory(),res.getString(R.string.dir_home));
        if (!f.exists()) {
            f.mkdirs();
        }
        f = new File(Environment.getExternalStorageDirectory(),res.getString(R.string.dir_sent));
        if (!f.exists()) {
            f.mkdirs();
        }
        f = new File(Environment.getExternalStorageDirectory(),res.getString(R.string.dir_images));
        if (!f.exists()) {
            f.mkdirs();
        }
        f = new File(Environment.getExternalStorageDirectory(),res.getString(R.string.dir_audio));
        if (!f.exists()) {
            f.mkdirs();
        }
        f = new File(Environment.getExternalStorageDirectory(),res.getString(R.string.dir_video));
        if (!f.exists()) {
            f.mkdirs();
        }
        f = new File(Environment.getExternalStorageDirectory(),res.getString(R.string.dir_others));
        if (!f.exists()) {
            f.mkdirs();
        }
        f = new File(Environment.getExternalStorageDirectory(),res.getString(R.string.dir_temp));
        if (!f.exists()) {
            f.mkdirs();
        }
    }


    public void playMessageTone(){
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playMessageToneLow(){
        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,250);
    }

    public void getCountrywithCodes(){
        Set<String> set = PhoneNumberUtil.getInstance().getSupportedRegions();

        String[] arr = set.toArray(new String[set.size()]);

        for (int i = 0; i < set.size(); i++) {
            Locale locale = new Locale("en", arr[i]);
            Log.d("common","country-" + arr[i] + "  "+ locale.getDisplayCountry()+" "+PhoneNumberUtil.getInstance().getCountryCodeForRegion(arr[i].trim()));
        }

    }

//    to highlight substrings in a text
    public Spannable getHighlightedText(String text,String searchQry){
        Spannable highlightedTExt=new SpannableString(text);
        //Log.d(TAG, "getHighlightedText: ser "+searchQry);
        if(searchQry==null || searchQry.equals("") || !text.toLowerCase().contains(searchQry.toLowerCase()))return highlightedTExt;
        int spanStart=text.toLowerCase().indexOf(searchQry.toLowerCase());
        int searchLen=searchQry.length();
        while(spanStart!=-1){
           // Log.d(TAG, "getHighlightedText: las "+spanStart);
            int spanEnd=spanStart+searchLen;
            highlightedTExt.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context,R.color.colorPrimaryLight)), spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanStart=text.toLowerCase().indexOf(searchQry.toLowerCase(),spanEnd);
        }
        return highlightedTExt;
    }

//    to check if app is in background
    public boolean isRunningInBackground(){
        return (ChatApplication.ACTIVITY_RESUME_COUNT <= ChatApplication.ACTIVITY_PAUSE_COUNT);
    }


}
