package com.linkai.app;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.linkai.app.R;

import com.linkai.app.adapters.GroupMemberListAdapter;
import com.linkai.app.adapters.SimpleFriendsListAdapter;
import com.linkai.app.libraries.Common;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.libraries.MyXMPP;
import com.linkai.app.modals.ChatFriend;
import com.linkai.app.modals.ChatGroup;
import com.linkai.app.modals.ChatUser;
import com.linkai.app.modals.GroupMember;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class GroupProfileActivity extends AppCompatActivity {
    private final String TAG="GroupProfileActivity";

    Context context;
    Common common;
    DatabaseHandler db;
    MyXMPP xmpp;
    Resources res;
    String groupId;
    ChatGroup group;

    Toolbar toolbar;
    TextView lblGroupName;
//    ImageButton btnAddMember;
    ListView lstMembers;
    ListView popupList;
    Dialog dialog;
    ProgressDialog progressDialog;

    SimpleFriendsListAdapter simpleFriendsListAdapter;
    GroupMemberListAdapter membersAdapter;
    ArrayList<ChatFriend> allFriends;
    ArrayList<GroupMember> members=new ArrayList<GroupMember>();
    AlertDialog.Builder listDialogueBuilder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile);
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent currentIntent=getIntent();

        context=this.getApplicationContext();
        common=new Common(context);
        db=Const.DB;
        xmpp=((ChatApplication)getApplication()).getMyXMPPInstance();
        res=this.getResources();

        groupId=currentIntent.getStringExtra("groupId");
        group=db.getGroup(groupId);

        this.setTitle(group.getName());

        lblGroupName= (TextView) findViewById(R.id.lblGroupName);
//        btnAddMember= (ImageButton) findViewById(R.id.btnAddMember);
        lstMembers= (ListView) findViewById(R.id.lstMembers);

        lblGroupName.setText(group.getName());
        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        popupList=new ListView(this);
        allFriends=db.getAllFriends(Const.FRIEND_SUBSCRIPTION_STATUS.SUBSCRIBED);
        listDialogueBuilder=new AlertDialog.Builder(GroupProfileActivity.this);
        simpleFriendsListAdapter=new SimpleFriendsListAdapter(this,allFriends);
        popupList.setAdapter(simpleFriendsListAdapter);
        listDialogueBuilder.setView(popupList);
        dialog=listDialogueBuilder.create();


//        showing popup list
//        btnAddMember.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialog.show();
//            }
//        });

        popupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ChatFriend selectedFriend=(ChatFriend) adapterView.getItemAtPosition(i);
//                adding selected chat friend if not already added
                GroupMember newMember=new GroupMember();
                newMember.setName(selectedFriend.getName());
                newMember.setPhone(selectedFriend.getPhone());
                newMember.setJabberId(selectedFriend.getJabberId());
                newMember.setGroupId(groupId);
                newMember.FriendObject.setName(selectedFriend.getName());
                if(!members.contains(newMember)) {

                    addMember(newMember);

                }
                else{
                    Toast.makeText(context,selectedFriend.getName()+" is already added",Toast.LENGTH_SHORT).show();
                }

//                dismiss dialog popup
                dialog.dismiss();
            }
        });


        refreshMemberList();

    }

    @Override
    protected void onResume() {
        Const.CUR_ACTIVITY= Const.APP_COMPONENTS.GroupProfileActivity;
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_profile, menu);
        boolean isAdmin=group.getOwner().equals(db.getUser().getJabberId())?true:false;
        boolean isActive=group.getStatus()==Const.GROUP_STATUS.ACTIVE.getValue()?true:false;
//        checking if user is admin to show/hide add member menu
        if(!isActive || !isAdmin){
            menu.findItem(R.id.action_add_member).setVisible(false);
        }
//        checking is user is active to show or hide exit group
        if(!isActive){
            menu.findItem(R.id.action_exit_group).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_member) {

            dialog.show();
        }
        else if(id==R.id.action_exit_group){
            AlertDialog.Builder alert=new AlertDialog.Builder(GroupProfileActivity.this);
            alert.setTitle(res.getString(R.string.group_profile_alert_title_exit_confirmation));
            alert.setMessage(res.getString(R.string.group_profile_alert_content_exit_confirmation));
            alert.setPositiveButton(res.getString(R.string.alert_btn_Yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    exitGroup();
                    dialogInterface.dismiss();
                }
            });
            alert.setNegativeButton(res.getString(R.string.alert_btn_No), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            Dialog alertDialog=alert.create();
            alertDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
//        checking if the user is the owner of the group
        if(group.getOwner().trim().equals(db.getUser().getJabberId().trim())) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            GroupMember member = (GroupMember) membersAdapter.getItem(info.position);
//        Log.d(TAG, "onCreateContextMenu: position-"+info.position+" name-"+member.getName());
            String name = member.FriendObject == null ? member.getPhone() : member.FriendObject.getName();
            menu.add(info.position, 0, 0, "Remove " + name);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);
        int position=item.getGroupId();
        GroupMember member= (GroupMember) membersAdapter.getItem(position);
//        Log.d(TAG, "onContextItemSelected:position-"+position+" membername-"+member.FriendObject.getName());
        removeMember(member);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //    to refresh added member list
    public void refreshMemberList(){
        //        getting all members
        members=db.getGroupMembers(groupId);
        membersAdapter=new GroupMemberListAdapter(this,members);
        lstMembers.setAdapter(membersAdapter);
        registerForContextMenu(lstMembers);
    }

//    to add new member
    public boolean addMember(final GroupMember newMember){

//      creating  request json
        JSONObject jsonRequest=new JSONObject();
        ChatUser user=db.getUser();
        try {
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("userPhone",user.getPhone());
            jsonObject.put("userJabberId",user.getJabberId());
            jsonObject.put("userPassword",user.getPassword());
            jsonObject.put("memberName",newMember.getName());
            jsonObject.put("memberPhone",newMember.getPhone());
            jsonObject.put("memberJabberId",newMember.getJabberId());
            jsonObject.put("groupId",groupId);
            jsonObject.put("action",Const.NOTIFY_MESSAGE_TYPE.ADD_MEMBER.toString());
//            adding object to request object
            jsonRequest.put("jsonobj",jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        Log.d(TAG, "addMember: request-"+jsonRequest);
//        if(true) {
//            return false;
//        }
//get url
        String url=Const.GROUP_ADD_MEMBER_URL;
        showProgress(true,"Adding member");
        final JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.POST, url, jsonRequest, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject responseJson) {
                showProgress(false,null);
                Log.d(TAG, "onResponse: "+responseJson.toString());
                try {
                    if(!responseJson.getBoolean("status"))
                    {
                        return;
                    }
                    db.addGroupMember(newMember);
//                    invite new member
//                    xmpp.inviteGroupMember(newMember);
//                    refersh list after successfully added
                    refreshMemberList();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                    check if success or not

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: add error");
                error.printStackTrace();
                showProgress(false,null);
                Toast.makeText(context,"Network Error. Try again",Toast.LENGTH_SHORT).show();
            }
        });
        //        getting request que
        RequestQueue requestQueue= Volley.newRequestQueue(context);
        requestQueue.add(jsonObjectRequest);
        return true;
    }

    public boolean removeMember(final GroupMember member){
//      creating  request json
        JSONObject jsonRequest=new JSONObject();
        ChatUser user=db.getUser();
        try {
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("userPhone",user.getPhone());
            jsonObject.put("userJabberId",user.getJabberId());
            jsonObject.put("userPassword",user.getPassword());
            jsonObject.put("memberPhone",member.getPhone());
            jsonObject.put("memberJabberId",member.getJabberId());
            jsonObject.put("groupId",groupId);
            jsonObject.put("action",Const.NOTIFY_MESSAGE_TYPE.REMOVE_MEMBER.toString());
//            adding object to request object
            jsonRequest.put("jsonobj",jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        Log.d(TAG, "removeMember: request-"+jsonRequest);
        String name=member.FriendObject==null?member.getPhone():member.FriendObject.getName();
//get url
        String url=Const.GROUP_DELETE_MEMBER_URL;
        showProgress(true,"Removing "+name);
        final JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.POST, url, jsonRequest, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject responseJson) {
                showProgress(false,null);
                Log.d(TAG, "onResponse: response-"+responseJson);
                try {
                    if(!responseJson.getBoolean("status"))
                    {
                        return;
                    }
                    db.removeGroupMember(member);
//                    refersh list after successfully added
                    refreshMemberList();
                } catch (JSONException e) {
                    e.printStackTrace();
                    showProgress(false,null);
                }
//                    check if success or not

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                showProgress(false,null);
                Toast.makeText(context,"Network Error. Try again",Toast.LENGTH_SHORT).show();
            }
        });
        //        getting request que
        RequestQueue requestQueue= Volley.newRequestQueue(context);
        requestQueue.add(jsonObjectRequest);
        return true;
    }

    public boolean exitGroup(){
        //      creating  request json
        JSONObject jsonRequest=new JSONObject();
        final ChatUser user=db.getUser();
        try {
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("userPhone",user.getPhone());
            jsonObject.put("userJabberId",user.getJabberId());
            jsonObject.put("userPassword",user.getPassword());
            jsonObject.put("memberPhone",user.getPhone());
            jsonObject.put("memberJabberId",user.getJabberId());
            jsonObject.put("groupId",groupId);
            jsonObject.put("action",Const.NOTIFY_MESSAGE_TYPE.LEFT_GROUP.toString());
//            adding object to request object
            jsonRequest.put("jsonobj",jsonObject);


        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        Log.d(TAG, "exitGroup: request-"+jsonRequest);
        //String name=member.FriendObject==null?member.getPhone():member.FriendObject.getName();
//get url
        String url=Const.GROUP_DELETE_MEMBER_URL;
        showProgress(true,"Exiting from "+group.getName());
        final JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.POST, url, jsonRequest, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject responseJson) {
                Log.d(TAG, "onResponse: response-"+responseJson);
                try {
                    if(!responseJson.getBoolean("status"))
                    {
                        return;
                    }
//                    update group
                    group.setStatus(Const.GROUP_STATUS.INACTIVE);
                    group.setStatusDate(new Date());
                    db.updateGroup(group);
//                    leave group
                    xmpp.leaveGroup(groupId);
//                    refresh activity
                    refreshActivity();
//                    refersh list after successfully added
                    refreshMemberList();
                    showProgress(false,null);
                } catch (JSONException e) {
                    e.printStackTrace();
                    showProgress(false,null);
                }
//                    check if success or not

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                showProgress(false,null);
                Toast.makeText(context,"Network Error. Try again",Toast.LENGTH_SHORT).show();

            }
        });
        //        getting request que
        RequestQueue requestQueue= Volley.newRequestQueue(context);
        requestQueue.add(jsonObjectRequest);
        return true;
    }

    private void showProgress(final boolean show,String message) {

        if(show) {
            progressDialog.setMessage(message);
            progressDialog.setIndeterminate(true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setProgressStyle(0);
            progressDialog.show();
        }
        else{
            progressDialog.dismiss();
        }

    }

    private void refreshActivity(){
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }


}
