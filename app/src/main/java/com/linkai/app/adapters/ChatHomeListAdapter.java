package com.linkai.app.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linkai.app.R;
import com.linkai.app.design.RoundedImageView;
import com.linkai.app.libraries.Common;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.libraries.FileHandler;
import com.linkai.app.modals.Chat;
import com.linkai.app.modals.ChatMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by LP1001 on 08-07-2016.
 */
public class ChatHomeListAdapter extends BaseAdapter {
    private final String TAG="ChatHomeListAdapter";

    ArrayList<Chat> itemList;
    public Activity context;
    public LayoutInflater inflater;
    private DatabaseHandler db;
    private FileHandler fileHandler;
    Common common;
    String searchQry=null;


    public ChatHomeListAdapter(Activity context, ArrayList<Chat> itemList) {
        super();
        this.context = context;
        this.itemList = itemList;
        db=Const.DB;
        fileHandler=new FileHandler(context.getApplicationContext());
        common=new Common(context.getApplicationContext());
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //    view holder class
    static class ViewHolder {
        TextView lblFriendName;
        TextView lblLastMessage;
        TextView lblLastMessageDate;
        ImageView imgMsgStatus;
        TextView lblUnreadMsgCount;
        LinearLayout llMsgCountContainer;
        RoundedImageView imgProfileThumb;
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if(view==null || ((ViewHolder)view.getTag()).position != i){
            view=inflater.inflate(R.layout.listitem_chathome,null);
            holder=new ViewHolder();
            holder.lblFriendName= (TextView) view.findViewById(R.id.lblChatName);
            holder.lblLastMessage= (TextView) view.findViewById(R.id.lblLastMessage);
            holder.lblLastMessageDate=(TextView) view.findViewById(R.id.lblLastMessageDate);
            holder.imgMsgStatus= (ImageView) view.findViewById(R.id.imgMsgStatus);
            holder.lblUnreadMsgCount=(TextView) view.findViewById(R.id.lblUnreadMsgCount);
            holder.llMsgCountContainer= (LinearLayout) view.findViewById(R.id.llMsgCountContainer);
            holder.imgProfileThumb= (RoundedImageView) view.findViewById(R.id.imgProfileThumb);
            holder.position=i;
            view.setTag(holder);
        }
        else{
            holder= (ViewHolder) view.getTag();
        }
        holder.lblFriendName.requestFocus();
        holder.lblFriendName.setSelected(true);
        Chat chat= (Chat) getItem(i);
        if(chat.getProfileThumb()!=null && !chat.getProfileThumb().equals("")){
            Log.d(TAG, "getView: "+chat.getChatName()+" "+chat.getProfileThumb());
            Bitmap thumbBitmap=fileHandler.stringToBitmap(chat.getProfileThumb());
            if(thumbBitmap!=null){
                holder.imgProfileThumb.setImageBitmap(thumbBitmap);
            }
            else{
                holder.imgProfileThumb.setImageResource(R.drawable.ic_user);
            }
        }
//        if chat type is group/event and evnt has no profile pic, show default event icon
        else if(chat.getChatboxType().equals(Const.CHATBOX_TYPE.GROUP.toString())){
            Const.EVENT_TYPE event_type= Const.EVENT_TYPE.getEventType(chat.getEventType());
            Log.d(TAG, "getView: "+event_type);
            if(event_type!=null && event_type.getImageResourceId(200)>0){
                holder.imgProfileThumb.setImageResource(event_type.getImageResourceId(200));
            }
            else{
                holder.imgProfileThumb.setImageResource(R.drawable.event_icon_100);
            }
        }
        else{
            holder.imgProfileThumb.setImageResource(R.drawable.ic_user);
        }

        //Log.d("ChatHomeListadapter", "getView: ");
        String message="";
        String msg_date = "";
        String senderName="";
        if(chat.LastMessage!=null) {
//        date formating
            SimpleDateFormat dateOnlyFormat = new SimpleDateFormat("d/MM/yy");
            try {
                Date dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(chat.LastMessage.getDate());
//            checking if msg date is current date
                Date curDateOnly = dateOnlyFormat.parse(dateOnlyFormat.format(new Date()));
                Date msgDateOnly = dateOnlyFormat.parse(dateOnlyFormat.format(dt));
                int dayDiff = (int) ((curDateOnly.getTime() - msgDateOnly.getTime()) / (1000 * 60 * 60 * 24));
//            Log.d("ChatHomeListAdapter", "getView: day diff-"+dayDiff);
                if (dayDiff == 0) {
                    msg_date = new SimpleDateFormat("h:mm a").format(dt);
                } else if (dayDiff == 1) {
                    msg_date = "YESTERDAY";
                } else {
                    msg_date = dateOnlyFormat.format(dt);
                }
            } catch (ParseException e) {
                // e.printStackTrace();
                msg_date = chat.LastMessage.getDate();
            }
            //        setting message status icon
            if (chat.LastMessage.getFrom().equals("self")) {
                holder.imgMsgStatus.setVisibility(View.VISIBLE);
                if (chat.LastMessage.getStatus() == ChatMessage.STATUS_UNSENT || chat.LastMessage.getStatus() == ChatMessage.STATUS_PENDING) {
                    holder.imgMsgStatus.setImageResource(R.drawable.ic_access_time_black_36dp);
                } else if (chat.LastMessage.getStatus() == ChatMessage.STATUS_SENT) {
                    holder.imgMsgStatus.setImageResource(R.drawable.ic_done_black_36dp);
                } else if (chat.LastMessage.getStatus() == ChatMessage.STATUS_DELIVERED) {
                    holder.imgMsgStatus.setImageResource(R.drawable.ic_done_all_black_36dp);
                } else if (chat.LastMessage.getStatus() == ChatMessage.STATUS_READ || chat.LastMessage.getStatus() == ChatMessage.STATUS_READ_ACK) {
                    holder.imgMsgStatus.setImageResource(R.drawable.ic_done_all_black_36dp);
                    holder.imgMsgStatus.setColorFilter(ContextCompat.getColor(context,R.color.blue1));
                }
            } else {
                holder.imgMsgStatus.setVisibility(View.GONE);

//            try to get sender name from friends list if group chat

                if(chat.getChatboxType().equals(Const.CHATBOX_TYPE.GROUP.toString())) {
                    try {
//                if sender is in friends list
                        senderName = db.getFriendByJabberId(chat.LastMessage.getFrom()).getName();
                    } catch (Exception e) {
//                if sender not is in friends list, exception will be raised
                        senderName = db.getGroupMember(chat.getChatId(),chat.LastMessage.getFrom()).getPhone();
                    }
                    try {
                        senderName = senderName.length() > 15 ? senderName.substring(0, 15) + ".." : senderName;
                        senderName += " :";
                    }catch (Exception e){
                        e.printStackTrace();
                        senderName="";
                    }

                }

            }
            //        message formatting
            message = chat.LastMessage.getBody().length() > (35-senderName.length()) ? chat.LastMessage.getBody().substring(0, (35-senderName.length())) + "..." : chat.LastMessage.getBody();
            message=senderName+message;
            if (!chat.LastMessage.getType().equals(ChatMessage.TYPE_TEXT)) {
                if(chat.LastMessage.getType().equals(ChatMessage.TYPE_TRANSFER)){
                    message = senderName + " Linkai Transfer";
                }
                else {
                    message = senderName + chat.LastMessage.getType() + " file";
                }
            }
//        get count of unread messages
            int cnt_unread = 0;
            if(chat.getChatboxType().equals(Const.CHATBOX_TYPE.SINGLE.toString())) {
                cnt_unread=db.getCountOfMessages(chat.getChatId(), DatabaseHandler.Message_Direction.INCOMING, DatabaseHandler.Message_Type.ALL, DatabaseHandler.Message_Status.UNREAD, DatabaseHandler.File_Status.ALL);
            }
            else if(chat.getChatboxType().equals(Const.CHATBOX_TYPE.GROUP.toString())) {
                cnt_unread=db.getGroupMessagesCount(chat.getChatId(), DatabaseHandler.Message_Direction.INCOMING, DatabaseHandler.Message_Type.ALL, DatabaseHandler.Message_Status.UNREAD, DatabaseHandler.File_Status.ALL);
            }
//        Log.d("Chathomelistadapter", "getView: unread count-"+cnt_unread);
            if (cnt_unread > 0) {
                holder.llMsgCountContainer.setVisibility(View.VISIBLE);
                holder.lblUnreadMsgCount.setText(String.valueOf(cnt_unread));
            } else {
                holder.llMsgCountContainer.setVisibility(View.GONE);
            }
//            Log.d(TAG, "getView: count-"+cnt_unread);
        }
        else{
            holder.llMsgCountContainer.setVisibility(View.GONE);
            holder.imgMsgStatus.setVisibility(View.GONE);
        }

//
        holder.lblFriendName.setText(common.getHighlightedText(chat.getChatName(),searchQry));
        holder.lblLastMessage.setText(message);
        holder.lblLastMessageDate.setText(msg_date);

//        add friend to friends list to update profile
        if(chat.getChatboxType().equals(Const.CHATBOX_TYPE.SINGLE.toString())){
//                    add chat friend to update_profile list
            Const.FRIENDSLIST_TO_UPDATE_PROFILE.add(chat.getChatId());

        }
        else{
            Const.GROUPSLIST_TO_UPDATE_PROFILE.add(chat.getChatId());
        }
        //                    call sync profile service
        new Common(context).callProfileSyncService();
        return view;
    }

    public void setSearchQry(String _searchQry){
        this.searchQry=_searchQry;
    }
}
