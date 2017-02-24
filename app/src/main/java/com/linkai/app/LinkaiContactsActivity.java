package com.linkai.app;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.linkai.app.R;

import com.linkai.app.adapters.LinkaiContactListAdapter;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.modals.ChatFriend;

import java.util.ArrayList;

public class LinkaiContactsActivity extends AppCompatActivity {

    Context context;
    DatabaseHandler db;

    private Toolbar toolbar;
    private ListView lstLinkaiContacts;
    private TextView txtEmptyText;
    private EditText txtSearchContacts;

    LinkaiContactListAdapter listAdapter=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linkai_contacts);
        context=this.getApplicationContext();
        db= Const.DB;
        //        actionbar
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lstLinkaiContacts= (ListView) findViewById(R.id.lstLinkaiContacts);
        txtEmptyText= (TextView)findViewById(R.id.txtEmptyText);
        txtSearchContacts= (EditText) findViewById(R.id.txtSearchContacts);

        lstLinkaiContacts.setEmptyView(txtEmptyText);
//        search textbox value enter listener
        txtSearchContacts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                refreshContacts();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

//        list selected listener
        lstLinkaiContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    ChatFriend contact = (ChatFriend) adapterView.getItemAtPosition(i);
                    Intent intent=new Intent(context,LinkaiPinEntryActivity.class);
                    intent.putExtra("chatId",contact.getJabberId());
                    startActivity(intent);
                }catch (Exception e){

                }
            }
        });
//        refresh contacts
        refreshContacts();
    }

    @Override
    protected void onResume() {
        Const.CUR_ACTIVITY= Const.APP_COMPONENTS.LinkaiContactsActivity;
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:onBackPressed();
                break;
            default:break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    protected void refreshContacts(){
        ArrayList<ChatFriend> friends=db.getAllFriends(Const.FRIEND_SUBSCRIPTION_STATUS.SUBSCRIBED,txtSearchContacts.getText().toString());
        listAdapter=new LinkaiContactListAdapter(this,friends);
        lstLinkaiContacts.setAdapter(listAdapter);
    }
}
