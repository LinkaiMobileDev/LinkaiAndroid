package com.linkai.app.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.linkai.app.R;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.libraries.FileHandler;
import com.linkai.app.modals.ChatMessage;
import com.linkai.app.modals.GroupMessage;
import com.linkai.app.services.UploadFileService;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by LP1001 on 04-08-2016.
 */
public class GroupchatBoxListAdapter extends BaseAdapter {
    private final String TAG="GroupchatBoxListAdapter";

    ArrayList<GroupMessage> itemList;

    public Activity context;
    public LayoutInflater inflater;
    FileHandler fileHandler;
    DatabaseHandler db;
    private ImageClickListener imageClickListener=null;

    public GroupchatBoxListAdapter(Activity context, ArrayList<GroupMessage> itemList,ImageClickListener _imageClickListener) {
        super();

        this.context = context;
        this.itemList = itemList;
//        init filehandler
        fileHandler=new FileHandler(context.getApplicationContext());
//        init db
        db=Const.DB;
//        init imageclick listener
        this.imageClickListener=_imageClickListener;

        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

//    view holder class
    static class ViewHolder {
    LinearLayout llMessageContainer;
    TextView lblChatBoxListMessage;
    TextView lblChatBoxMessageTime;
    ImageView imgFileThumb;
    ImageView imgMsgStatus;
    FrameLayout flImgContainer;
    ProgressBar pgImageProgress;
    TextView lblSender;
    LinearLayout llPlayIndicator;
    LinearLayout llDownloadIndicator;
        int position;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int i) {
        return itemList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public boolean isEnabled(int position) {
        GroupMessage msgObj= (GroupMessage) getItem(position);
        if(msgObj.getType().equals(ChatMessage.TYPE_NOTIFICATION)){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if(view==null || ((ViewHolder)view.getTag()).position != i){
            view=inflater.inflate(R.layout.listitem_chatbox,null);
            holder=new ViewHolder();
            holder.llMessageContainer= (LinearLayout) view.findViewById(R.id.llMessageContainer);
            holder.lblChatBoxListMessage= (TextView) view.findViewById(R.id.lblChatBoxListMessage);
            holder.lblChatBoxMessageTime= (TextView) view.findViewById(R.id.lblChatBoxMessageTime);
            holder.imgFileThumb= (ImageView) view.findViewById(R.id.imgFileThumb);
            holder.imgMsgStatus= (ImageView) view.findViewById(R.id.imgMsgStatus);
            holder.flImgContainer= (FrameLayout) view.findViewById(R.id.flOuterImgThumb);
            holder.pgImageProgress= (ProgressBar) view.findViewById(R.id.prImageProgress);
            holder.lblSender=(TextView) view.findViewById(R.id.lblSender);
            holder.llPlayIndicator= (LinearLayout) view.findViewById(R.id.llPlayIndicator);
            holder.llDownloadIndicator= (LinearLayout) view.findViewById(R.id.llDownloadIndicator);

//            setting default hiiden items
            holder.flImgContainer.setVisibility(View.GONE);
            holder.llPlayIndicator.setVisibility(View.GONE);
            holder.llDownloadIndicator.setVisibility(View.GONE);
            holder.pgImageProgress.setVisibility(View.GONE);
            holder.position=i;
            view.setTag(holder);
//            Log.d(TAG, "getView: convert view -"+i);
        }
        else{
            holder= (ViewHolder) view.getTag();
        }
        GroupMessage msgObj= (GroupMessage) getItem(i);



        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.llMessageContainer.getLayoutParams();
        Log.d(TAG, "getView: "+msgObj.getType());
//        checking if message is a notification
        if(msgObj.getType().equals(ChatMessage.TYPE_NOTIFICATION)){
            Log.d(TAG, "getView: in notification "+msgObj.getPacketId());
            String msg="";
            try {
                JSONObject notifyMsgBody=new JSONObject(msgObj.getBody());
                String memberName;
                if(notifyMsgBody.getString("member_id").equals(db.getUser().getJabberId())){
                    memberName="you";
                }
                else {
                    try {
//                if member is in friends list
                        memberName = db.getFriendByJabberId(notifyMsgBody.getString("member_id")).getName();
                    } catch (Exception e) {
//                if member not is in friends list, exception will be raised
                        memberName = db.getGroupMember(msgObj.getGroupId(), notifyMsgBody.getString("member_id")).getPhone();
                    }
                }
                if(notifyMsgBody.getString("notify_type").equals(Const.NOTIFY_MESSAGE_TYPE.ADD_MEMBER.toString())){
                    msg="Added "+memberName;
                }
                else if(notifyMsgBody.getString("notify_type").equals(Const.NOTIFY_MESSAGE_TYPE.REMOVE_MEMBER.toString())){
                    msg="Removed "+memberName;
                }
                else if(notifyMsgBody.getString("notify_type").equals(Const.NOTIFY_MESSAGE_TYPE.LEFT_GROUP.toString())){

                    msg=memberName+" left";
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
//            setting text
//            lblChatBoxListMessage.setBackgroundResource(R.drawable.notification_background);
            holder.lblChatBoxListMessage.setText(msg);
            holder.llMessageContainer.setGravity(Gravity.CENTER);
//            hiding status and time
            holder.imgMsgStatus.setVisibility(View.GONE);
            holder.lblChatBoxMessageTime.setVisibility(View.GONE);
//            resetting params
            params.setMargins(0, 0, 0, 0);
//            removing background
            holder.llMessageContainer.setBackground(null);
            holder.lblSender.setVisibility(View.GONE);
        }
//        if(false){}
        else {
//            Remove background from message label
//            lblChatBoxListMessage.setBackgroundResource(0);
//            showing time label
            holder.lblChatBoxMessageTime.setVisibility(View.VISIBLE);
            if (msgObj.getFrom().equals("self")) {
                holder.lblSender.setVisibility(View.GONE);
//            showing message status using icon
                holder.imgMsgStatus.setVisibility(View.VISIBLE);
                Log.d(TAG, "getView: status-"+msgObj.getStatus());
                if (msgObj.getStatus() == ChatMessage.STATUS_UNSENT || msgObj.getStatus() == ChatMessage.STATUS_PENDING) {
                    holder.imgMsgStatus.setImageResource(R.drawable.ic_access_time_black_36dp);
                } else if (msgObj.getStatus() == ChatMessage.STATUS_SENT) {
                    holder.imgMsgStatus.setImageResource(R.drawable.ic_done_black_36dp);
                } else if (msgObj.getStatus() == ChatMessage.STATUS_DELIVERED) {
                    holder.imgMsgStatus.setImageResource(R.drawable.ic_done_all_black_36dp);
                } else if (msgObj.getStatus() == ChatMessage.STATUS_READ || msgObj.getStatus() == ChatMessage.STATUS_READ_ACK) {
                    holder.imgMsgStatus.setImageResource(R.drawable.ic_done_all_black_36dp);
                }
//            setting outgoing background style
                holder.llMessageContainer.setBackgroundResource(R.drawable.bubble_outgoing);
                params.setMargins(150, 0, 0, 0);
            } else {
//            hiding status icon
                holder.imgMsgStatus.setVisibility(View.GONE);
//            showing sender
                String senderName;
//            try to get sender name from friends list
                try {
//                if sender is in friends list
                    senderName = db.getFriendByJabberId(msgObj.getFrom()).getName();
                } catch (Exception e) {
//                if sender not is in friends list, exception will be raised
                    senderName = db.getGroupMember(msgObj.getGroupId(),msgObj.getFrom()).getPhone();
                }
                holder.lblSender.setText(senderName);
                holder.lblSender.setVisibility(View.VISIBLE);

                holder.llMessageContainer.setBackgroundResource(R.drawable.bubble_incoming);
                params.setMargins(0, 0, 150, 0);
            }
            holder.llMessageContainer.setLayoutParams(params);

            String msg_date = "";
            try {
                Date dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(msgObj.getDate());
                msg_date = new SimpleDateFormat("d-MMM-yy h:mm a").format(dt);
            } catch (ParseException e) {
                // e.printStackTrace();
                msg_date = msgObj.getDate();
            }
            holder.lblChatBoxMessageTime.setText(msg_date);
//        checking msg body
            if (!msgObj.getBody().trim().equals("")) {
                holder.lblChatBoxListMessage.setText(msgObj.getBody());
                holder.lblChatBoxListMessage.setVisibility(View.VISIBLE);
            } else {
                holder.lblChatBoxListMessage.setVisibility(View.GONE);
            }
//        checking filetype and showing thumb
            if (msgObj.getType().equals(ChatMessage.TYPE_IMAGE) || msgObj.getType().equals(ChatMessage.TYPE_VIDEO)) {
                holder.flImgContainer.setVisibility(View.VISIBLE);
                //imgFileThumb.setVisibility(View.VISIBLE);
//            set bitmap
                Bitmap img;
                if (msgObj.getFileStatus() == 0) {
//                if not downloaded/uploaded
                    img = fileHandler.stringToBitmap(msgObj.getThumb());
                } else {
                    //                if downloaded/uploaded successfully
                    String imgPath = fileHandler.getFilePath(msgObj);
                    if(msgObj.getType().equals(ChatMessage.TYPE_IMAGE)) {
                        img = fileHandler.getBitmap(imgPath);
                    }
                    else if(msgObj.getType().equals(ChatMessage.TYPE_VIDEO)){
                        img = fileHandler.stringToBitmap(fileHandler.getThumbOfVideo(imgPath,false));
//                    show play indicator
                        holder.llPlayIndicator.setVisibility(View.VISIBLE);
                    }
                    else{
                        img=null;
                    }
//                Showing thumb if img is null. ie, file is deleted or cannot decode
                    if (img == null) {
                        img = fileHandler.stringToBitmap(msgObj.getThumb());
                    }
                }
                if (img == null) {
                    //imgFileThumb.setVisibility(View.GONE);
                    holder.flImgContainer.setVisibility(View.GONE);
                } else {
                    holder.imgFileThumb.setImageBitmap(img);
                }
//            img.recycle();
            }
            else if(msgObj.getType().equals(ChatMessage.TYPE_AUDIO)){
                holder.flImgContainer.setVisibility(View.VISIBLE);
                holder.imgFileThumb.setImageResource(R.drawable.file_music_gray);
            }
            else {
                //imgFileThumb.setVisibility(View.GONE);
                holder.flImgContainer.setVisibility(View.GONE);
            }
//            to show progressbar and indicators
            if(msgObj.getFileStatus()==0 && msgObj.getFrom().equals("self") && UploadFileService.IS_ALIVE) {
                holder.pgImageProgress.setVisibility(View.VISIBLE);
            }
            else if(msgObj.getFileStatus()==0 && !msgObj.getFrom().equals("self")){
                holder.llDownloadIndicator.setVisibility(View.VISIBLE);
            }
            else{
                holder.pgImageProgress.setVisibility(View.GONE);
                holder.llDownloadIndicator.setVisibility(View.GONE);
            }
//setting tag thumb image for using in click event
            holder.flImgContainer.setTag(i);
//        click event for thumb image
            holder.flImgContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imageClickListener.onImageClick((Integer) view.getTag(), view);
                }
            });

        }
        return view;
    }

    public interface ImageClickListener{
        public abstract void onImageClick(int position,View view);
    }
}
