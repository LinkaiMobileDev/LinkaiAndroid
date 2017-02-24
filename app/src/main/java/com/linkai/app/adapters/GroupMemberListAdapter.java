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
import com.linkai.app.modals.GroupMember;

import java.util.ArrayList;

/**
 * Created by LP1001 on 09-08-2016.
 */
public class GroupMemberListAdapter extends BaseAdapter {
    private final String TAG="GroupMemberListAdapter";
    ArrayList<GroupMember> itemList;
    FileHandler fileHandler;
    public Activity context;
    public LayoutInflater inflater;

    public GroupMemberListAdapter(Activity context, ArrayList<GroupMember> itemList) {
        super();

        this.context = context;
        this.itemList = itemList;
        fileHandler=new FileHandler(context.getApplicationContext());
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    static class ViewHolder {
        TextView lblFriendName;
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
            view=inflater.inflate(R.layout.listitem_groupmemberlist,null);
            holder=new ViewHolder();
            holder.lblFriendName= (TextView) view.findViewById(R.id.lblFriendName);
            holder.imgProfileThumb= (RoundedImageView) view.findViewById(R.id.imgProfileThumb);
            holder.position=i;
            view.setTag(holder);
        }
        else{
            holder= (ViewHolder) view.getTag();
        }
        GroupMember member= (GroupMember) getItem(i);
        if(member.FriendObject!=null && member.FriendObject.getProfileThumb()!=null && !member.FriendObject.getProfileThumb().equals("")){
            Bitmap thumbBitmap=fileHandler.stringToBitmap(member.FriendObject.getProfileThumb());
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
        holder.lblFriendName.setText(member.FriendObject==null?member.getPhone():member.FriendObject.getName());
//        lblFriendPhone.setText(member.getPhone());
        return view;
    }
}
