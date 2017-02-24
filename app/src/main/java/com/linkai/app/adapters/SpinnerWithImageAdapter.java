package com.linkai.app.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.linkai.app.R;

/**
 * Created by LP1001 on 06-12-2016.
 */
public class SpinnerWithImageAdapter extends BaseAdapter {
    private final String TAG="SpinnerImageAdapter";

    Context context;
    Integer[] images;
    String[] names;
    int[] item_ids;
    LayoutInflater mInflater;

    public SpinnerWithImageAdapter(Context _context,Integer[] _images,String[] _names,int[] _item_ids){
        this.context=_context;
        this.images=_images;
        this.names=_names;
        this.item_ids=_item_ids;
        mInflater=LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return names.length;
    }

    @Override
    public Object getItem(int position) {
        return names[position];
    }

    @Override
    public long getItemId(int position) {
        return item_ids[position];
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView( position,  convertView,  parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView( position,  convertView,  parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent){
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.app_spinner_with_image,
                    null);
        }
        TextView txtValue=(TextView) convertView.findViewById(R.id.txtValue);
        ImageView imgIcon=(ImageView) convertView.findViewById(R.id.imgIcon);

        txtValue.setText(names[position]);
        Log.d(TAG, "getCustomView: "+images.length+" "+images[position]);
        if(position<images.length && images[position]!=null && images[position]!=0){
            Log.d(TAG, "getCustomView: ");
            imgIcon.setBackgroundResource(images[position]);
        }


        return convertView;
    }

}
