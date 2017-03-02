package com.linkai.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.linkai.app.R;

import com.linkai.app.adapters.LinkaiReedemHistoryAdapter;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;

public class LinkaiRedeemActivity extends AppCompatActivity {
    Context context;
    SharedPreferences prefs;
    DatabaseHandler db;
    private BroadcastReceiver broadcastReceiver;
    private LocalBroadcastManager localBroadcastManager;

    private Toolbar toolbar;
    private ListView lstHistory;
    private Button btnContinue;
    private TextView txtViewAmount;
    private TextView txtViewCurrency;
    private TextView lblBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linkai_redeem);
        context=this.getApplicationContext();
        db= Const.DB;
        localBroadcastManager=LocalBroadcastManager.getInstance(context);
        prefs = context.getSharedPreferences(Const.LINKAI_SHAREDPREFERENCE_FILE, Context.MODE_PRIVATE);
        //        actionbar
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        lstHistory=(ListView) findViewById(R.id.lstHistory);

        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loadHistory();


        btnContinue= (Button) findViewById(R.id.btnContinue);
        txtViewAmount= (TextView) findViewById(R.id.txtViewAmount);
        txtViewCurrency= (TextView) findViewById(R.id.txtViewCurrency);
        lblBalance= (TextView) findViewById(R.id.lblBalance);

        //        setting balance
        setView();

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtViewAmount.setText("5600");
                txtViewCurrency.setText("IQD");
            }
        });


    }

    @Override
    protected void onResume() {
        Const.CUR_ACTIVITY= Const.APP_COMPONENTS.LinkaiRedeemActivity;
        super.onResume();
        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()){

                    case "linkai.view.refresh":setView();
                        break;
                    default:break;
                }
            }
        };
        try{
            localBroadcastManager.registerReceiver(broadcastReceiver,new IntentFilter("linkai.view.refresh"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

//        UNREGISTER BROADCAST
        try {
            localBroadcastManager.unregisterReceiver(broadcastReceiver);
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
        Log.d("", "onBackPressed: true");
        super.onBackPressed();
    }

    public void setView(){
        //        setting balance
        lblBalance.setText(prefs.getString("current_balance","0.0")+" "+prefs.getString("currency",""));
    }

    private void  loadHistory()
    {
        try{



           LinkaiReedemHistoryAdapter adapter = new LinkaiReedemHistoryAdapter(this);
            lstHistory.setAdapter(adapter);
            registerForContextMenu(lstHistory);

        }
        catch (Exception ex){

        }
    }

}
