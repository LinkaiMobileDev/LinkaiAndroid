package com.linkai.app.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.linkai.app.R;

/**
 * Created by LP1001 on 27-09-2016.
 */
public class NavListAdapter extends BaseAdapter {

    private int[] icons={0,0,0};
    private String[] texts;
    public LayoutInflater inflater;
    public NavListAdapter(Activity context, int[] _icons, String[] _texts){
        if(_icons!=null){
            this.icons=_icons;
        }
        this.texts=_texts;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return 6;
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
        if(view==null){
            view=inflater.inflate(R.layout.listitem_navlist,null);
        }
        ImageView imgMenuIcon= (ImageView) view.findViewById(R.id.imgMenuIcon);
        TextView txtMenutext= (TextView) view.findViewById(R.id.txtMenutext);
        View viewDivider=view.findViewById(R.id.viewDivider);
//        determining to show or hide divider
        viewDivider.setVisibility(View.GONE);
        if(i==2){
            viewDivider.setVisibility(View.VISIBLE);
        }
        if(icons[i]!=0){
            imgMenuIcon.setImageResource(icons[i]);
        }
        else{
            imgMenuIcon.setVisibility(View.GONE);
        }
//        Log.d("nav menu", "getView: "+texts[i]);
        txtMenutext.setText(texts[i]);
        return view;
    }
}
