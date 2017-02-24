package com.linkai.app.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.linkai.app.R;
import com.linkai.app.design.RoundedImageView;
import com.linkai.app.libraries.FileHandler;
import com.linkai.app.modals.ChatFriend;

import java.util.ArrayList;

/**
 * Created by LP1001 on 08-08-2016.
 */
public class SimpleFriendsListAdapter extends BaseAdapter {

    ArrayList<ChatFriend> itemList;

    public Activity context;
    public LayoutInflater inflater;
    FileHandler fileHandler;

    public SimpleFriendsListAdapter(Activity context, ArrayList<ChatFriend> itemList) {
        super();

        this.context = context;
        this.itemList = itemList;
        fileHandler=new FileHandler(context.getApplicationContext());
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    static class ViewHolder {
        TextView lblFriendName;
        TextView lblFriendPhone;
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
            view=inflater.inflate(R.layout.listitem_friendslist_simple,null);
            holder=new ViewHolder();
            holder.lblFriendName= (TextView) view.findViewById(R.id.lblFriendName);
            holder.lblFriendPhone= (TextView) view.findViewById(R.id.lblFriendPhone);
            holder.imgProfileThumb= (RoundedImageView) view.findViewById(R.id.imgProfileThumb);
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

        holder.lblFriendName.setText(friend.getName());
        holder.lblFriendPhone.setText(friend.getPhone());
        return view;
    }
}
