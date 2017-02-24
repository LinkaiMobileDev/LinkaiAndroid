package com.linkai.app.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
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
import com.linkai.app.modals.ChatFriend;
import com.linkai.app.modals.ChatMessage;
import com.linkai.app.modals.Transfer;
import com.linkai.app.services.UploadFileService;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by LP1001 on 29-06-2016.
 */
public class ChatBoxListAdapter extends BaseAdapter {
    private final String TAG="ChatBoxListAdapter";
    ArrayList<ChatMessage> itemList;

    public Activity context;
    public LayoutInflater inflater;
    FileHandler fileHandler;
    DatabaseHandler db;
    SharedPreferences pref;
    private ClickListener clickListener=null;
    int TYPE_CHAT=0,TYPE_TRANSFER=1;




    public ChatBoxListAdapter(Activity context, ArrayList<ChatMessage> itemList,ClickListener _clickListener) {
        super();

        this.context = context;
        this.itemList = itemList;

//        init filehandler
        fileHandler=new FileHandler(context.getApplicationContext());
        db= Const.DB;
        pref=context.getSharedPreferences(Const.LINKAI_SHAREDPREFERENCE_FILE, Context.MODE_PRIVATE);
//        init imageclick listener
        this.clickListener=_clickListener;

        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //    view holder class
    static class ChatViewHolder {
        LinearLayout llMessageContainer;
        TextView lblChatBoxListMessage;
        TextView lblChatBoxMessageTime;
        ImageView imgFileThumb;
        ImageView imgMsgStatus;
        FrameLayout flImgContainer;
        ProgressBar pgImageProgress;
        LinearLayout llPlayIndicator;
        LinearLayout llDownloadIndicator;
        int position;
    }

    static class TransferViewHolder{
        TextView txtHead1;
        TextView txtLine1;
        TextView txtLine2;
        TextView txtSmallLine1;
        LinearLayout llAcceptBtnContainer;
        LinearLayout llBtnAcceptNo;
        LinearLayout llBtnAcceptYes;
        TextView lblChatBoxMessageTime;
        ImageView imgMsgStatus;
//
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
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msgObj= (ChatMessage) getItem(position);
//        Log.d(TAG, "getItemViewType: type "+msgObj.getType()+"="+ChatMessage.TYPE_TRANSFER+" ?"+msgObj.getType().equals(ChatMessage.TYPE_TRANSFER));
        return msgObj.getType().equals(ChatMessage.TYPE_TRANSFER)?TYPE_TRANSFER:TYPE_CHAT;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
//        Log.d(TAG, "getView: view type="+getItemViewType(i));
        view=getItemViewType(i)==TYPE_CHAT?setChatView(i,view,viewGroup):setTransferView(i,view,viewGroup);

        return view;
    }

    private View setChatView(int i, View view, ViewGroup viewGroup){
        ChatViewHolder holder;
        if(view==null || ((ChatViewHolder)view.getTag()).position != i){
            view=inflater.inflate(R.layout.listitem_chatbox,null);
            holder=new ChatViewHolder();
            holder.llMessageContainer= (LinearLayout) view.findViewById(R.id.llMessageContainer);
            holder.lblChatBoxListMessage= (TextView) view.findViewById(R.id.lblChatBoxListMessage);
            holder.lblChatBoxMessageTime= (TextView) view.findViewById(R.id.lblChatBoxMessageTime);
            holder.imgFileThumb= (ImageView) view.findViewById(R.id.imgFileThumb);
            holder.imgMsgStatus= (ImageView) view.findViewById(R.id.imgMsgStatus);
            holder.flImgContainer= (FrameLayout) view.findViewById(R.id.flOuterImgThumb);
            holder.pgImageProgress= (ProgressBar) view.findViewById(R.id.prImageProgress);
            holder.llPlayIndicator= (LinearLayout) view.findViewById(R.id.llPlayIndicator);
            holder.llDownloadIndicator= (LinearLayout) view.findViewById(R.id.llDownloadIndicator);
//            setting default hiiden items
            holder.flImgContainer.setVisibility(View.GONE);
            holder.llPlayIndicator.setVisibility(View.GONE);
            holder.llDownloadIndicator.setVisibility(View.GONE);
            holder.pgImageProgress.setVisibility(View.GONE);
            holder.position=i;
            view.setTag(holder);
        }
        else{
            holder= (ChatViewHolder) view.getTag();
        }
        ChatMessage msgObj= (ChatMessage) getItem(i);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.llMessageContainer.getLayoutParams();

        if(msgObj.getFrom().equals("self")){
//            llMessageContainer.setBackgroundColor(Color.argb(-8,255,255,255));
            holder.llMessageContainer.setBackgroundResource(R.drawable.bubble_outgoing);
            params.setMargins(150, 0, 0, 0);
        }
        else{
//            llMessageContainer.setBackgroundColor(Color.argb(-8,198,232,254));
//            llMessageContainer.setBackgroundColor(Color.argb(-8,200,230,250));
            holder.llMessageContainer.setBackgroundResource(R.drawable.bubble_incoming);
            params.setMargins(0, 0, 150, 0);
        }
        holder.llMessageContainer.setLayoutParams(params);

        SimpleDateFormat dateFormat = new SimpleDateFormat("d-M-yy HH:mm");
        String msg_date="";
        try {
            Date dt= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(msgObj.getDate());
            msg_date= new SimpleDateFormat("d-MMM-yy h:mm a").format(dt);
        } catch (ParseException e) {
            // e.printStackTrace();
            msg_date=msgObj.getDate();
        }

        holder.lblChatBoxMessageTime.setText(msg_date);
//        checking msg body
        if(!msgObj.getBody().trim().equals("")) {
            holder.lblChatBoxListMessage.setText(msgObj.getBody());
            holder.lblChatBoxListMessage.setVisibility(View.VISIBLE);
        }
        else{
            holder.lblChatBoxListMessage.setVisibility(View.GONE);
        }
//        checking filetype and showing thumb
        if(msgObj.getType().equals(ChatMessage.TYPE_IMAGE) || msgObj.getType().equals(ChatMessage.TYPE_VIDEO)){
            holder.flImgContainer.setVisibility(View.VISIBLE);
//            set bitmap
            Bitmap img;
            if(msgObj.getFileStatus()==0) {
//                if not downloaded/uploaded
                img = fileHandler.stringToBitmap(msgObj.getThumb());
            }
            else{
                //                if downloaded/uploaded successfully
                String imgPath=fileHandler.getFilePath(msgObj);
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
//                get thumb if fetching image from directory failed
                if(img==null ){
                    img = fileHandler.stringToBitmap(msgObj.getThumb());
                }

            }

//            img = fileHandler.stringToBitmap(msgObj.getThumb());

            if(img==null){
                //imgFileThumb.setVisibility(View.GONE);
                holder.flImgContainer.setVisibility(View.GONE);
            }
            else{
                holder.imgFileThumb.setImageBitmap(img);
            }
//            img.recycle();
        }
        else if(msgObj.getType().equals(ChatMessage.TYPE_AUDIO)){
            holder.flImgContainer.setVisibility(View.VISIBLE);
            holder.imgFileThumb.setImageResource(R.drawable.file_music_gray);
        }
        else{
            //imgFileThumb.setVisibility(View.GONE);
            holder.flImgContainer.setVisibility(View.GONE);
        }
        //            to show progressbar and indicators
//        Log.d(TAG, "getView: "+msgObj.getFileStatus()+" "+msgObj.getFrom());
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
                clickListener.onFileClick((Integer) view.getTag(),view);
            }
        });

//        setting message status icon
        if(msgObj.getFrom().equals("self")) {
            holder.imgMsgStatus.setVisibility(View.VISIBLE);
            if (msgObj.getStatus() == ChatMessage.STATUS_UNSENT || msgObj.getStatus() ==ChatMessage.STATUS_PENDING) {
                holder.imgMsgStatus.setImageResource(R.drawable.ic_access_time_black_36dp);
            } else if (msgObj.getStatus() == ChatMessage.STATUS_SENT) {
                holder.imgMsgStatus.setImageResource(R.drawable.ic_done_black_36dp);
            } else if (msgObj.getStatus() == ChatMessage.STATUS_DELIVERED) {
                holder.imgMsgStatus.setImageResource(R.drawable.ic_done_all_black_36dp);
            }else if (msgObj.getStatus() == ChatMessage.STATUS_READ || msgObj.getStatus() == ChatMessage.STATUS_READ_ACK) {
                holder.imgMsgStatus.setImageResource(R.drawable.ic_done_all_black_36dp);
                holder.imgMsgStatus.setColorFilter(ContextCompat.getColor(context,R.color.blue1));
            }
        }
        else{
            holder.imgMsgStatus.setVisibility(View.GONE);
        }
//        Log.d("Chatboxlistadapter", "getView: status - "+msgObj.getStatus()+" from-"+msgObj.getFrom());
        return view;
    }

    private View setTransferView(int i, View view, ViewGroup viewGroup){

//        Log.d(TAG, "setTransferView: ");
        TransferViewHolder holder;
        if(view==null || ((TransferViewHolder)view.getTag()).position != i){
            view=inflater.inflate(R.layout.listitem_chat_transfer,null);
            holder=new TransferViewHolder();
            holder.llAcceptBtnContainer= (LinearLayout) view.findViewById(R.id.llAcceptBtnContainer);
            holder.llBtnAcceptNo= (LinearLayout) view.findViewById(R.id.llBtnAcceptNo);
            holder.llBtnAcceptYes= (LinearLayout) view.findViewById(R.id.llBtnAcceptYes);
            holder.txtHead1= (TextView) view.findViewById(R.id.txtHead1);
            holder.txtLine1= (TextView) view.findViewById(R.id.txtLine1);
            holder.txtLine2= (TextView) view.findViewById(R.id.txtLine2);
            holder.txtSmallLine1= (TextView) view.findViewById(R.id.txtSmallLine1);
            holder.lblChatBoxMessageTime= (TextView) view.findViewById(R.id.lblChatBoxMessageTime);
            holder.imgMsgStatus= (ImageView) view.findViewById(R.id.imgMsgStatus);
//            default hideden views
            holder.llAcceptBtnContainer.setVisibility(View.GONE);
            holder.txtLine1.setVisibility(View.GONE);
            holder.txtLine2.setVisibility(View.GONE);
//            holder.txtSmallLine1.setVisibility(View.GONE);
//            holder.txtHead1.setVisibility(View.GONE);

            holder.position=i;
            view.setTag(holder);
        }
        else{
            holder= (TransferViewHolder) view.getTag();
        }
        ChatMessage msgObj= (ChatMessage) getItem(i);
        Transfer transfer;
        try {
            JSONObject transferJson=new JSONObject(msgObj.getBody());
            transfer=db.getTransfer(transferJson.getString("transfer_id"));
            String head1="",line1="",line2="",smallLine="";
//            outgoing transfer
            if(msgObj.getFrom().equals("self")){
                ChatFriend receiver=db.getFriendByPhone(transfer.getReceiverPhone());
                String receiver_name=receiver!=null?receiver.getName():transfer.getReceiverPhone();
                if(transfer.getTransferStatus()==Const.TRANSFER_STATUS.PENDING.getValue()){
                    line1="Waiting...\n"+receiver_name+"\n to accept your linkai\n"+transfer.getAmount()+" "+transfer.getCurrency();
                }
                else if(transfer.getTransferStatus()==Const.TRANSFER_STATUS.ACCEPTED.getValue()){
                    line1=receiver_name+"\n accepted your linkai of\n"+transfer.getAmount()+" "+transfer.getCurrency();
                }
                else if(transfer.getTransferStatus()==Const.TRANSFER_STATUS.REJECTED.getValue()){
                    line1=receiver_name+"\n rejected your linkai of\n"+transfer.getAmount()+" "+transfer.getCurrency();
                }
                else if(transfer.getTransferStatus()==Const.TRANSFER_STATUS.EXPIRED.getValue()){

                }
            }
            else{
                ChatFriend sender=db.getFriendByPhone(transfer.getSenderPhone());
                String sender_name=sender!=null?sender.getName():transfer.getSenderPhone();
                holder.llAcceptBtnContainer.setVisibility(View.GONE);
                if(transfer.getTransferStatus()==Const.TRANSFER_STATUS.PENDING.getValue()){
                    head1=sender_name+" linkai you.";
                    line1=""+transfer.getAmount()+" "+transfer.getCurrency();
                    line2="Do you accept?";
                    smallLine="The request will be expired in 50s.";
                    holder.llAcceptBtnContainer.setVisibility(View.VISIBLE);
                }
                else if(transfer.getTransferStatus()==Const.TRANSFER_STATUS.ACCEPTED.getValue()){
                    line1="You have accepted\n"+sender_name+" linkai\n"+transfer.getAmount()+" "+transfer.getCurrency()+"\n\n Your balance is " +
                            "\n"+pref.getString("current_balance","0.0")+" "+pref.getString("currency","");
                }
                else if(transfer.getTransferStatus()==Const.TRANSFER_STATUS.REJECTED.getValue()){
                    line1="You have rejected\n"+sender_name+" linkai\n"+transfer.getAmount()+" "+transfer.getCurrency();
                }
                else if(transfer.getTransferStatus()==Const.TRANSFER_STATUS.EXPIRED.getValue()){

                }
            }

//            setting view content and visibility
            if(!head1.trim().equals("")){
                holder.txtHead1.setVisibility(View.VISIBLE);
                holder.txtHead1.setText(head1);
            }
            else{
                holder.txtHead1.setText("");
            }
            if(!line1.trim().equals("")){
                holder.txtLine1.setVisibility(View.VISIBLE);
                holder.txtLine1.setText(line1);
            }
            else{
                holder.txtLine1.setText("");
            }
            if(!line2.trim().equals("")){
                holder.txtLine2.setVisibility(View.VISIBLE);
                holder.txtLine2.setText(line2);
            }
            else{
                holder.txtLine2.setText("");
            }
            if(!smallLine.trim().equals("")){
                holder.txtSmallLine1.setVisibility(View.VISIBLE);
                holder.txtSmallLine1.setText(smallLine);
            }
            else{
                holder.txtSmallLine1.setText("");
            }

//            setting msg date
            String msg_date="";
            try {
                Date dt= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(msgObj.getDate());
                msg_date= new SimpleDateFormat("d-MMM-yy h:mm a").format(dt);
            } catch (ParseException e) {
                // e.printStackTrace();
                msg_date=msgObj.getDate();
            }
            holder.lblChatBoxMessageTime.setText(msg_date);

//            setting msg status icon
            //        setting message status icon
            if(msgObj.getFrom().equals("self")) {
                holder.imgMsgStatus.setVisibility(View.VISIBLE);
                if (msgObj.getStatus() == ChatMessage.STATUS_UNSENT || msgObj.getStatus() ==ChatMessage.STATUS_PENDING) {
                    holder.imgMsgStatus.setImageResource(R.drawable.ic_access_time_black_36dp);
                } else if (msgObj.getStatus() == ChatMessage.STATUS_SENT) {
                    holder.imgMsgStatus.setImageResource(R.drawable.ic_done_black_36dp);
                } else if (msgObj.getStatus() == ChatMessage.STATUS_DELIVERED) {
                    holder.imgMsgStatus.setImageResource(R.drawable.ic_done_all_black_36dp);
                }else if (msgObj.getStatus() == ChatMessage.STATUS_READ || msgObj.getStatus() == ChatMessage.STATUS_READ_ACK) {

                    holder.imgMsgStatus.setImageResource(R.drawable.ic_done_all_black_36dp);
                    holder.imgMsgStatus.setColorFilter(ContextCompat.getColor(context,R.color.blue1));
                }
            }
            else{
                holder.imgMsgStatus.setVisibility(View.GONE);
            }

//            click listeners
            holder.llBtnAcceptNo.setTag(i);
            holder.llBtnAcceptYes.setTag(i);
            holder.llBtnAcceptNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onTransferAcceptClick((Integer) v.getTag(),false);
                }
            });
            holder.llBtnAcceptYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onTransferAcceptClick((Integer) v.getTag(),true);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    public interface ClickListener{

        public abstract void onFileClick(int position,View view);

        public abstract  void onTransferAcceptClick(int position,boolean isAccepted);

    }


}
