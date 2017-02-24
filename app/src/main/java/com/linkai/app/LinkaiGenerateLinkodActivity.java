package com.linkai.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.linkai.app.R;

import com.linkai.app.libraries.Const;

public class LinkaiGenerateLinkodActivity extends AppCompatActivity {

    Context context;
    SharedPreferences prefs;
    private BroadcastReceiver broadcastReceiver;

    private Toolbar toolbar;
    private Spinner spinnerCurrency;
    private Button btnGetLinkod;
    private EditText txtAmount;
    private TextView lblLinkod;
    private TextView lblBalance;
    private LinearLayout llShare;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linkai_generate_linkod);
        context=this.getApplicationContext();
        prefs = context.getSharedPreferences(Const.LINKAI_SHAREDPREFERENCE_FILE, Context.MODE_PRIVATE);
        //        actionbar
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        spinnerCurrency=(Spinner)findViewById(R.id.spinnerCurrency);
        btnGetLinkod= (Button) findViewById(R.id.btnGetLinkod);
        txtAmount= (EditText) findViewById(R.id.txtAmount);
        lblLinkod= (TextView) findViewById(R.id.lblLinkod);
        lblBalance= (TextView) findViewById(R.id.lblBalance);
        llShare= (LinearLayout) findViewById(R.id.llShare);

//        setting balance
        setView();

        btnGetLinkod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amount=txtAmount.getText().toString();
                if(amount.trim().equals("")){
                    txtAmount.setError("Enter amount");
                    txtAmount.requestFocus();
                    return;
                }
                AlertDialog.Builder builder= new AlertDialog.Builder(LinkaiGenerateLinkodActivity.this);
                //.setIcon(android.R.drawable.ic_dialog_alert)
                builder.setTitle("Confirmation");
                builder.setMessage(Html.fromHtml("<font color='#808080' size='12'>"+amount+" IQD <br/> click OK if the amount is coorect</font>"));

                builder.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        lblLinkod.setText("2345345345345");
                    }
                })
                        .setNegativeButton("EDIT", null);
                //.show();
                AlertDialog alert = builder.create();
                alert.show();
                Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                nbutton.setTextColor(Color.GRAY);
                Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                pbutton.setTextColor(ContextCompat.getColor(context,R.color.colorPrimary));
            }
        });

        llShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!lblLinkod.getText().toString().trim().equals("")){
                    Intent shareIntent=new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Linkod");
                    shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, lblLinkod.getText().toString());
                    startActivity(shareIntent);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        Const.CUR_ACTIVITY= Const.APP_COMPONENTS.LinkaiGenerateLinkodActivity;
        super.onResume();
        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Toast.makeText(context,"received-"+intent.getAction(),Toast.LENGTH_SHORT).show();
//                loadChatBoxMessages();
                switch (intent.getAction()){

                    case "linkai.view.refresh":setView();
                        break;
                    default:break;
                }
            }
        };
        try{
            context.registerReceiver(broadcastReceiver,new IntentFilter("linkai.view.refresh"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

//        UNREGISTER BROADCAST
        try {
            context.unregisterReceiver(broadcastReceiver);
        }
        catch (Exception e){

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:onBackPressed();
                return  true;
            default:return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void setView(){
        //        setting balance
        lblBalance.setText(prefs.getString("current_balance","0.0")+" "+prefs.getString("currency",""));
    }

    public void showMyLinkods(View view){
        Intent myLinkodsIntent=new Intent(LinkaiGenerateLinkodActivity.this,LinkaiMyLinkodsActivity.class);
        startActivity(myLinkodsIntent);
    }

}
