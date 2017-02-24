package com.linkai.app;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ListView;

import com.linkai.app.R;

import com.linkai.app.adapters.MyLinkodsListAdapter;
import com.linkai.app.libraries.Const;

public class LinkaiMyLinkodsActivity extends AppCompatActivity {

    Context context;

    private Toolbar toolbar;
    private ListView lstMyLinkods;

    MyLinkodsListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linkai_my_linkods);
        context=this.getApplicationContext();
        //        actionbar
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lstMyLinkods= (ListView) findViewById(R.id.lstMyLinkods);

//        call method to load linkods
        loadLinkods();
    }

    @Override
    protected void onResume() {
        Const.CUR_ACTIVITY= Const.APP_COMPONENTS.LinkaiMyLinkodsActivity;
        super.onResume();
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

    public void loadLinkods(){
        adapter=new MyLinkodsListAdapter(this);
        lstMyLinkods.setAdapter(adapter);
    }
}
