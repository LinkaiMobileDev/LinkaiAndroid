package com.linkai.app.Fragments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linkai.app.LinkaiGenerateLinkodActivity;
import com.linkai.app.LinkaiRedeemActivity;
import com.linkai.app.R;
import com.linkai.app.design.LineGraph;
import com.linkai.app.libraries.Const;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyLinkaiFragment extends Fragment {
    private final String TAG="MyLinkaiFragment";

    Context context;
    SharedPreferences pref;

    private ImageView imgBtnPeriodFrom;
    private ImageView imgBtnPeriodTo;
    private LinearLayout llGenerate;
    private LinearLayout llRedeem;
    private TextView lblLinkaiBalance;
    private TextView lblLinkaiCurrency;
    private LineGraph lineGraph;

    private final int DATE_PICKER_ID=1111;

    //    current instance
    public static MyLinkaiFragment cur_instance;


    public MyLinkaiFragment() {
        // Required empty public constructor
        cur_instance=this;
    }

    //    to get current instance
    public static MyLinkaiFragment getInstatnce(){
        return  cur_instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this.getActivity().getApplicationContext();
        pref=context.getSharedPreferences(Const.LINKAI_SHAREDPREFERENCE_FILE, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_mylinkai, container, false);
        imgBtnPeriodFrom= (ImageView) view.findViewById(R.id.imgBtnPeriodFrom);
        imgBtnPeriodTo= (ImageView) view.findViewById(R.id.imgBtnPeriodTo);
        llGenerate= (LinearLayout) view.findViewById(R.id.llGenerate);
        llRedeem= (LinearLayout) view.findViewById(R.id.llRedeem);
        lblLinkaiBalance= (TextView) view.findViewById(R.id.lblLinkaiBalance);
        lblLinkaiCurrency= (TextView) view.findViewById(R.id.lblLinkaiCurrency);
        lineGraph= (LineGraph) view.findViewById(R.id.lineGraph);


        llGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent generatelinkodIntent=new Intent(context, LinkaiGenerateLinkodActivity.class);
                generatelinkodIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(generatelinkodIntent);
            }
        });

        llRedeem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("redeem", "onClick: ");
                Intent redeemLinkodIntent=new Intent(context, LinkaiRedeemActivity.class);
                redeemLinkodIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(redeemLinkodIntent);
            }
        });

        imgBtnPeriodFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });
        imgBtnPeriodTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });
//        to delete
        String[] months=new String[]{"JAN","FEB","MAR","APR","MAY","JUN","JUL"};
        float[] values=new float[]{100,300,200,600,100,400,100};
        lineGraph.setAttrAndValues(months,values);
//
        refreshMyLinkai();
        return view;
    }

    @Override
    public void onResume() {

        super.onResume();
        refreshMyLinkai();
    }


    public void refreshMyLinkai(){
        //        setting balance
//        Log.d(TAG, "refreshMyLinkai: "+pref.getString("current_balance","0.0"));
        lblLinkaiBalance.setText(pref.getString("current_balance","0.0"));
        lblLinkaiCurrency.setText(pref.getString("currency",""));
    }



}
