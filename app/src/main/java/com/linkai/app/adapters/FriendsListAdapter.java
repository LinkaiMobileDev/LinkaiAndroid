package com.linkai.app.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.linkai.app.R;
import com.linkai.app.design.RoundedImageView;
import com.linkai.app.libraries.Common;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.FileHandler;
import com.linkai.app.modals.ChatFriend;

import java.util.ArrayList;

/**
 * Created by LP1001 on 04-07-2016.
 */
public class FriendsListAdapter extends BaseAdapter {
    private final String TAG="FriendsListAdapter";

    ArrayList<ChatFriend> itemList;

    public Activity context;
    public LayoutInflater inflater;
    FileHandler fileHandler;
    Common common;
    String searchQry=null;

    public FriendsListAdapter(Activity context, ArrayList<ChatFriend> itemList) {
        super();

        this.context = context;
        this.itemList = itemList;
        fileHandler=new FileHandler(context.getApplicationContext());
        common=new Common(context.getApplicationContext());

        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    static class ViewHolder {
        TextView lblFriendName;
        TextView lblFriendPhone;
        RoundedImageView imgProfileThumb;
        ImageView imgLstLogo;
        Button btnInvite;
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
            view=inflater.inflate(R.layout.listitem_friendslist,null);
            holder=new ViewHolder();
            holder.lblFriendName= (TextView) view.findViewById(R.id.lblFriendName);
            holder.lblFriendPhone= (TextView) view.findViewById(R.id.lblFriendPhone);
            holder.imgProfileThumb= (RoundedImageView) view.findViewById(R.id.imgProfileThumb);
            holder.imgLstLogo= (ImageView) view.findViewById(R.id.imgLstLogo);
            holder.btnInvite= (Button) view.findViewById(R.id.btnInvite);
            holder.position=i;
            view.setTag(holder);
        }
        else{
            holder= (ViewHolder) view.getTag();
        }
        ChatFriend friend= (ChatFriend) getItem(i);
        //Log.d("FriendsListAdapter", "getView: ");

        if(friend.getProfileThumb()!=null && !friend.getProfileThumb().equals("")){
            Bitmap thumbBitmap=fileHandler.stringToBitmap(friend.getProfileThumb());
            if(thumbBitmap!=null){
                holder.imgProfileThumb.setImageBitmap(thumbBitmap);
            }
            else{
                holder.imgProfileThumb.setImageResource(R.drawable.ic_user);
            }
        }
        else{
            holder.imgProfileThumb.setImageResource(R.drawable.ic_user);
        }
//        checking if friend is subscribed// to determine wheather to show logo or invite button
        if(friend.getSubscriptionStatus()== Const.FRIEND_SUBSCRIPTION_STATUS.SUBSCRIBED.getValue()){
            holder.imgLstLogo.setVisibility(View.VISIBLE);
            holder.btnInvite.setVisibility(View.GONE);

            //                    add chat friend to update_profile list
            Const.FRIENDSLIST_TO_UPDATE_PROFILE.add(friend.getJabberId());
            //                    call sync profile service
            new Common(context).callProfileSyncService();
        }
        else {
            holder.imgLstLogo.setVisibility(View.GONE);
            holder.btnInvite.setVisibility(View.VISIBLE);
        }
//        holder.lblFriendName.setText(friend.getName());
//        holder.lblFriendPhone.setText(friend.getPhone());
        holder.lblFriendName.setText(common.getHighlightedText(friend.getName(),searchQry), TextView.BufferType.SPANNABLE);
        holder.lblFriendPhone.setText(common.getHighlightedText(friend.getPhone(),searchQry), TextView.BufferType.SPANNABLE);
//        view listeners
        holder.btnInvite.setTag(holder.position);
        holder.btnInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ChatFriend sms_receiver = (ChatFriend) getItem((Integer) v.getTag());
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + sms_receiver.getPhone()));
                    intent.putExtra("sms_body", "Let’s chat and share on Linkai! Get the free app http://linkai.com");
                    context.startActivity(intent);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        return view;
    }

    public void setSearchQry(String _searchQry){
        this.searchQry=_searchQry;
    }

}
