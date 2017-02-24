package com.linkai.app.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.linkai.app.R;

/**
 * Created by LP1001 on 13-10-2016.
 */
public class MyLinkodsListAdapter extends BaseAdapter {

    Activity context;
    public LayoutInflater inflater;

    String[] names=new String[]{"Sandra laght","Omar Bro","Dady"};
    String[] dates=new String[]{"24/09/2016 17:20","24/09/2016 17:05","24/09/2016 16:31"};
    String[] linkods=new String[]{"456513123123","123213123123","678913123123"};
    String[] amounts=new String[]{"2510 IQD","1200 IQD","5200 IQD"};
    String[] expiryTime=new String[]{"3 Hours","15 Hours","2 Days"};

    public MyLinkodsListAdapter(Activity context){
        super();
        this.context=context;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    //    view holder class
    static class ViewHolder {
        TextView txtName;
        TextView txtDate;
        TextView txtLinkod;
        TextView txtAmount;
        TextView txtExpiryTime;
        int position;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if(view==null || ((ViewHolder)view.getTag()).position != i){
            view=inflater.inflate(R.layout.listitem_mylinkodslist,null);
            holder=new ViewHolder();
            holder.txtName= (TextView) view.findViewById(R.id.txtName);
            holder.txtDate= (TextView) view.findViewById(R.id.txtDate);
            holder.txtLinkod= (TextView) view.findViewById(R.id.txtLinkod);
            holder.txtAmount= (TextView) view.findViewById(R.id.txtAmount);
            holder.txtExpiryTime= (TextView) view.findViewById(R.id.txtExpiryTime);
            holder.position=i;
            view.setTag(holder);
        }
        else{
            holder= (ViewHolder) view.getTag();
        }
        holder.txtName.setText(names[i]);
        holder.txtDate.setText(dates[i]);
        holder.txtLinkod.setText(linkods[i]);
        holder.txtAmount.setText(amounts[i]);
        holder.txtExpiryTime.setText(expiryTime[i]);
        return view;
    }
}
