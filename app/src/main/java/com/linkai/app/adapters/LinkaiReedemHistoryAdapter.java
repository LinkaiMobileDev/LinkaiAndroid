package com.linkai.app.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.linkai.app.R;
import com.linkai.app.libraries.FileHandler;

/**
 * Created by Finez1 on 13-10-2016.
 */
public class LinkaiReedemHistoryAdapter extends BaseAdapter {

   // ArrayList<ChatFriend> itemList;
    public Activity context;
    public LayoutInflater inflater;
    FileHandler fileHandler;

    public LinkaiReedemHistoryAdapter(Activity context){
        super();
        this.context = context;
        //this.itemList = itemList;
        fileHandler=new FileHandler(context.getApplicationContext());
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    static class ViewHolder {
        TextView lblFriendName;
        TextView lblTime;
        TextView lblLinkod;
        TextView lblAmount;
        View viewSectionDivider;
        int position;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if(view==null || ((ViewHolder)view.getTag()).position != i){
            view=inflater.inflate(R.layout.listitem_linkai_reedem_history,null);
            holder=new ViewHolder();
            holder.lblFriendName= (TextView) view.findViewById(R.id.lblFriendName);
            holder.lblTime= (TextView) view.findViewById(R.id.lblTime);
            holder.lblLinkod= (TextView) view.findViewById(R.id.lblLinkod);
            holder.lblAmount= (TextView) view.findViewById(R.id.lblAmount);
            holder.viewSectionDivider=(View) view.findViewById(R.id.viewSectionDivider);
            holder.position=i;
            view.setTag(holder);
        }
        else{
            holder= (ViewHolder) view.getTag();
        }

        holder.lblFriendName.setText("Sandra Laght");
        holder.lblTime.setText("24/09/2016 17:20");
        holder.lblLinkod.setText("12753716253712");
        holder.lblAmount.setText("2510 IQD");

        //ChatFriend friend= (ChatFriend) getItem(i);

        //section handling block
//        if(i==0 || !friend.getName().substring(0,1).equals(((ChatFriend) getItem(i-1)).getName().substring(0,1))){
//            holder.lblFriendName.setText(friend.getName().substring(0,1));
//            holder.lblTime.setVisibility(View.VISIBLE);
//            if(i!=0){
//                holder.viewSectionDivider.setVisibility(View.VISIBLE);
//            }
//            else{
//                holder.viewSectionDivider.setVisibility(View.GONE);
//            }
//        }
//        else{
//            holder.viewSectionDivider.setVisibility(View.GONE);
//        }
        return view;
    }


}
