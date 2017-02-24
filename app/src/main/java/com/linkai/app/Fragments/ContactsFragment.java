package com.linkai.app.Fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.linkai.app.R;
import com.linkai.app.SingleChatBoxActivity;
import com.linkai.app.adapters.FriendsListAdapter;
import com.linkai.app.libraries.Common;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.modals.ChatFriend;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {
    private final String TAG="ContactsFragment";


    private Context context;
    DatabaseHandler db;
    Common common;
    FriendsListAdapter adapter=null;
    ArrayList<ChatFriend> friends=new ArrayList<ChatFriend>();
    //private BroadcastReceiver chatBroadReceiver;

    ListView lstFriends;
    TextView txtEmptyText;
    private FloatingActionButton fab;

//    to refer the current instance
    private static  ContactsFragment cur_instance=null;

    public ContactsFragment() {
        // Required empty public constructor
        cur_instance=this;
    }

//    to get the current instance
    public static ContactsFragment  getInstance(){
        return cur_instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        context=this.getActivity().getApplicationContext();
        db= Const.DB;
        common=new Common(context);

//        setHasOptionsMenu(true);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_contacts, container, false);


        lstFriends= (ListView) view.findViewById(R.id.lstFriends);
        txtEmptyText= (TextView) view.findViewById(R.id.txtEmptyText);
        lstFriends.setEmptyView(txtEmptyText);
        lstFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ChatFriend selectedFriend= (ChatFriend) adapterView.getItemAtPosition(i);
                Intent intentChatBox = new Intent(context, SingleChatBoxActivity.class);
                intentChatBox.putExtra("chatId",selectedFriend.getJabberId());
                startActivity(intentChatBox);

            }
        });
        fab= (FloatingActionButton) view.findViewById(R.id.fab);
//        to add contact
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_INSERT);
                intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                startActivity(intent);
            }
        });
//        showAllFriends();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        showAllFriends(true,null);
    }



    public void showAllFriends(boolean reload, String searchQuery){
        if(!isAdded()){
            return;
        }
        friends.clear();
        Log.d(TAG, "showAllFriends: "+searchQuery);
        friends.addAll(db.getAllFriends(Const.FRIEND_SUBSCRIPTION_STATUS.ALL,searchQuery));
//        if(friends.size()==0){
//            return;
//        }
        if (adapter==null || reload) {
            adapter = new FriendsListAdapter(this.getActivity(), friends);
            adapter.setSearchQry(searchQuery);
            lstFriends.setAdapter(adapter);
        }
        else {
            adapter.setSearchQry(searchQuery);
            adapter.notifyDataSetChanged();
//            adapter = new FriendsListAdapter(this.getActivity(), friends);
//            lstFriends.setAdapter(adapter);
        }


    }

}
