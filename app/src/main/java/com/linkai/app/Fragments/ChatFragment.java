package com.linkai.app.Fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.linkai.app.GroupChatBoxActivity;
import com.linkai.app.LinkaiCreateEventStep1Activity;
import com.linkai.app.R;
import com.linkai.app.SingleChatBoxActivity;
import com.linkai.app.adapters.ChatHomeListAdapter;
import com.linkai.app.libraries.Common;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.modals.Chat;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {
    private final String TAG="ChatFragment";

    ListView lstChats;
    TextView txtEmptyText;
    private FloatingActionButton fab;
    ChatHomeListAdapter chatHomeListAdapter=null;
    ArrayList<Chat> chats=new ArrayList<Chat>();
    Chat selectedChat;

    Common common;
    Context context;
    DatabaseHandler db;

//    current instance
    private static ChatFragment cur_instance;

    public ChatFragment() {
        // Required empty public constructor
        cur_instance=this;
    }

//    to get current instance
    public static ChatFragment getInstatnce(){
        return  cur_instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
//        setRetainInstance(true);

        context=this.getActivity().getApplicationContext();
        db=Const.DB;
        common=new Common(context);
//        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_chat, container, false);
        lstChats= (ListView) view.findViewById(R.id.lstChats);
        txtEmptyText= (TextView) view.findViewById(R.id.txtEmptyText);
        lstChats.setEmptyView(txtEmptyText);
        lstChats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Chat selectedChat= (Chat) adapterView.getItemAtPosition(i);
//                Log.d(TAG, "onItemClick: "+selectedChat.getChatId()+" ");
                if(selectedChat.getChatboxType().equals(Const.CHATBOX_TYPE.SINGLE.toString())) {
                    Intent intentChatBox = new Intent(context, SingleChatBoxActivity.class);
                    intentChatBox.putExtra("chatId", selectedChat.getChatId());
                    startActivity(intentChatBox);
                }
                else if(selectedChat.getChatboxType().equals(Const.CHATBOX_TYPE.GROUP.toString())) {
                    Intent intent=new Intent(context, GroupChatBoxActivity.class);
                    intent.putExtra("groupId",selectedChat.getChatId());
                    startActivity(intent);
                }
            }
        });

        //        setting up floating action button
        fab= (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent=new Intent(context,NewGroup.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
                Intent intent=new Intent(context,LinkaiCreateEventStep1Activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        showAllChats(null);
        return view;
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info= (AdapterView.AdapterContextMenuInfo) menuInfo;
        selectedChat= (Chat) chatHomeListAdapter.getItem(info.position);
        if(selectedChat.LastMessage!=null && !selectedChat.LastMessage.getBody().trim().equals("")){
            menu.add(info.position,0,0,R.string.text_delete);
        }

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getItemId()==0){
//            Log.d(TAG, "onContextItemSelected: "+selectedChat.getChatboxType()+"-"+selectedChat.getChatId()+"-"+selectedChat.getChatName());
            if(selectedChat.getChatboxType().equals(Const.CHATBOX_TYPE.SINGLE.toString())){
//                Log.d(TAG, "onContextItemSelected: "+selectedChat.getChatId()+"-"+selectedChat.getChatName());
                db.deleteAllMessages(selectedChat.getChatId());
            }
            else{
                db.deleteAllGroupMessages(selectedChat.getChatId());
            }
            showAllChats(null);
        }
        return super.onContextItemSelected(item);
    }

    public void showAllChats(String searchQuery){
        if(!isAdded()){
//            Log.d(TAG, "showAllChats: notAdded");
            return;
        }
        chats.clear();
        chats.addAll(db.getChats(searchQuery));
//        Log.d(TAG, "showAllChats: size-"+chats.size());
        if(chatHomeListAdapter==null) {
            chatHomeListAdapter = new ChatHomeListAdapter(this.getActivity(), chats);
            chatHomeListAdapter.setSearchQry(searchQuery);
            lstChats.setAdapter(chatHomeListAdapter);
            registerForContextMenu(lstChats);
//            Log.d(TAG, "showAllChats: null");
        }
        else{
            chatHomeListAdapter.setSearchQry(searchQuery);
            chatHomeListAdapter.notifyDataSetChanged();
//            Log.d(TAG, "showAllChats: not null");
        }

    }

}
