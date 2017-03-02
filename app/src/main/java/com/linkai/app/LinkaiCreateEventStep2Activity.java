package com.linkai.app;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.linkai.app.R;

import com.linkai.app.adapters.SimpleFriendsListAdapter;
import com.linkai.app.design.RoundedImageView;
import com.linkai.app.libraries.Common;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.libraries.FileHandler;
import com.linkai.app.libraries.MyXMPP;
import com.linkai.app.modals.ChatFriend;
import com.linkai.app.modals.ChatGroup;
import com.linkai.app.modals.ChatUser;
import com.linkai.app.modals.GroupMember;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class LinkaiCreateEventStep2Activity extends AppCompatActivity {
    private static final String TAG="Create event-2";

    private Context context;
    private FileHandler fileHandler;
    private Common common;
    private DatabaseHandler db;
    LayoutInflater mInflater;
    Resources res;

    private Toolbar toolbar;
    Button btnAddParticipants;
    Button btnContinue;
    LinearLayout lstEventParticipants;
    ListView popupList;
    TextView lblBenficiaryName;
    TextView lblBenficiaryPhone;
    TextView lblTarget;
    TextView lblEventName;
    ImageView imgEventIcon;

    SimpleFriendsListAdapter popupAdapter;
    AlertDialog.Builder listDialogueBuilder;
    ArrayList<ChatFriend> allFriends;
    ArrayList<GroupMember> participants=new ArrayList<GroupMember>();
    ChatGroup chatGroup=null;
    ProgressDialog progressDialog;
    MyXMPP xmpp;
    ChatUser user;

    String profile_temp_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linkai_create_event_step2);
        context=this.getApplicationContext();
        db= Const.DB;
        fileHandler=new FileHandler(context);
        common=new Common(context);
        xmpp=((ChatApplication)getApplication()).getMyXMPPInstance();
        user=db.getUser();
        res=context.getResources();
        //        actionbar
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mInflater= LayoutInflater.from(context);

        btnContinue= (Button) findViewById(R.id.btnContinue);
        btnAddParticipants= (Button) findViewById(R.id.btnAddParticipants);
        lstEventParticipants= (LinearLayout) findViewById(R.id.lstEventParticipants);
        popupList=new ListView(this);
        lblBenficiaryName= (TextView) findViewById(R.id.lblBenficiaryName);
        lblBenficiaryPhone= (TextView) findViewById(R.id.lblBenficiaryPhone);
        lblTarget= (TextView) findViewById(R.id.lblTarget);
        lblEventName= (TextView) findViewById(R.id.lblEventName);
        imgEventIcon= (ImageView) findViewById(R.id.imgEventIcon);

        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        listDialogueBuilder=new AlertDialog.Builder(LinkaiCreateEventStep2Activity.this);
        allFriends=db.getAllFriends(Const.FRIEND_SUBSCRIPTION_STATUS.SUBSCRIBED);
        popupAdapter=new SimpleFriendsListAdapter(this,allFriends);

        chatGroup= getIntent().getParcelableExtra("group_object");
        profile_temp_name=getIntent().getStringExtra("profile_temp_name");
//        Log.d(TAG, "onCreate: "+chatGroup.getName()+"-"+chatGroup.getBeneficiaryName()+"-"+chatGroup.getTargetAmount());
        popupList.setAdapter(popupAdapter);
        listDialogueBuilder.setView(popupList);
        final Dialog dialog=listDialogueBuilder.create();

        btnAddParticipants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: "+participants.size());
                if(participants.size()<20){
                    dialog.show();
                }
                else{
                    Toast.makeText(context,"Reached maximum participants limit",Toast.LENGTH_SHORT).show();
                }

            }
        });

        //        add member when selectinga n item from friends list
        popupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ChatFriend selectedFriend=(ChatFriend) adapterView.getItemAtPosition(i);
                GroupMember newMember=new GroupMember();
                newMember.setName(selectedFriend.getName());
                newMember.setPhone(selectedFriend.getPhone());
                newMember.setJabberId(selectedFriend.getJabberId());
                newMember.FriendObject=selectedFriend;
//                adding selected chat friend if not already added
                if(!participants.contains(newMember)) {

                    participants.add(newMember);
                    //refresh added members
                    refreshMemberList();
                }
                else{
                    Toast.makeText(context,selectedFriend.getName()+" is already added",Toast.LENGTH_SHORT).show();
                }

//                dismiss dialog popup
                dialog.dismiss();
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNewGroup();
            }
        });

        lblEventName.setText(chatGroup.getName());
        lblBenficiaryName.setText(chatGroup.getBeneficiaryName());
        lblBenficiaryPhone.setText(chatGroup.getBeneficiaryPhone());
//        Log.d(TAG, "onCreate targetAmount: "+chatGroup.getTargetAmount());
        String target=String.format("%.2f",chatGroup.getTargetAmount())+" "+chatGroup.getTargetCurrency();
        lblTarget.setText(target);
        refreshGroupImage();

    }

    @Override
    protected void onResume() {
        Const.CUR_ACTIVITY= Const.APP_COMPONENTS.LinkaiCreateEventStep2Activity;
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            default:return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //    to refresh added member list
    public void refreshMemberList(){
//        GroupMemberListAdapter membersAdapter=new GroupMemberListAdapter(this,participants);
//        lstEventParticipants.setAdapter(membersAdapter);
        lstEventParticipants.removeAllViews();
        int index=0;
        for (GroupMember participant:participants) {
            View itemview=mInflater.inflate(R.layout.listitem_groupmemberlist,null);
            TextView lblFriendName = (TextView) itemview.findViewById(R.id.lblFriendName);
            RoundedImageView imgProfileThumb = (RoundedImageView) itemview.findViewById(R.id.imgProfileThumb);
            //Log.d(TAG, "refreshMemberList: "+participant.FriendObject.getPhone());
            if(participant.FriendObject!=null && participant.FriendObject.getProfileThumb()!=null && !participant.FriendObject.getProfileThumb().equals("")){
                Bitmap thumbBitmap=fileHandler.stringToBitmap(participant.FriendObject.getProfileThumb());
                if(thumbBitmap!=null){
                    imgProfileThumb.setImageBitmap(thumbBitmap);

                }
                else{
                    imgProfileThumb.setImageResource(R.drawable.ic_user);
                }
            }
            else{
                imgProfileThumb.setImageResource(R.drawable.ic_user);
            }
            lblFriendName.setText(participant.FriendObject==null?participant.getPhone():participant.FriendObject.getName());
            lstEventParticipants.addView(itemview,index);
            index++;
        }
    }


    //    to create new group
    public void saveNewGroup(){
        if(!validate()){
            return;
        }
//        getting json object params
        final JSONObject jsonRequest=createJsonParams();
        Log.d(TAG, "saveNewGroup: "+jsonRequest);
//        getting url
        String url= Const.GROUP_CREATE_URL;
//        showing progress bar
        showProgress(true,"Saving event. Please wait..");
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.POST, url, jsonRequest, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject responseJson) {
                Log.d(TAG, "onResponse: "+responseJson.toString());
//                adding group to db
                try {
//                    check if success or not
                    if(!responseJson.getBoolean("status"))
                    {
                        showProgress(false,null);
                        AlertDialog alert=new AlertDialog.Builder(LinkaiCreateEventStep2Activity.this).create();
                        alert.setTitle(res.getString(R.string.alert_title_error));
                        alert.setMessage(res.getString(R.string.alert_content_error_try));
                        alert.show();
                        return;
                    }
                    Log.d(TAG, "onResponse: response-"+responseJson.toString());
                    //ChatGroup newGroup=new ChatGroup();
                    chatGroup.setGroupId(responseJson.getString("groupId"));
                    //newGroup.setName(jsonRequest.getJSONObject("jsonobj").getString("groupName"));
                    //newGroup.setOwner(user.getJabberId());
//                    try to add group
                    if(db.addGroup(chatGroup)){
//                        Add members if group added succesfully
                        for (GroupMember addedFriend:participants) {
                            GroupMember member=new GroupMember();
                            member.setGroupId(responseJson.getString("groupId"));
                            member.setPhone(addedFriend.getPhone());
                            member.setJabberId(addedFriend.getJabberId());
                            member.setJabberId(addedFriend.getJabberId());
                            db.addGroupMember(member);
                        }
//                        copy image fro temp to main
                        moveProfileImage();
//                        join to the new group
                        xmpp.createOrJoinGroup(chatGroup);
//                      move to next activity
                        Intent createEvent3=new Intent(context,LinkaiCreateEventStep3Activity.class);
                        createEvent3.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        createEvent3.putExtra("group_object",chatGroup);
                        startActivity(createEvent3);
//                        finish this activity after succeffully creating group
                        finish();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                hiding progress bar
                showProgress(false,null);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                showProgress(false,null);
                error.printStackTrace();
                AlertDialog alert=new AlertDialog.Builder(LinkaiCreateEventStep2Activity.this).create();
                alert.setTitle(res.getString(R.string.alert_title_error));
                alert.setMessage(res.getString(R.string.alert_content_error_try));
                alert.show();
                error.printStackTrace();
                //hiding progress bar

            }
        });
//        setting default retry policy
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(20),
                0,  // maxNumRetries = 0 means no retry
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//        getting request que
        RequestQueue requestQueue= Volley.newRequestQueue(context);
        requestQueue.add(jsonObjectRequest);
    }

    public JSONObject createJsonParams(){
//        returning json obj
        JSONObject retObj=new JSONObject();
        //        creating json object
        JSONObject jsonObject=new JSONObject();
//        get username and password
        user=db.getUser();
        try {
            //        adding user details to json
            jsonObject.put("userPhone",user.getPhone());
            jsonObject.put("userJabberId",user.getJabberId());
            jsonObject.put("userPassword",user.getPassword());
            jsonObject.put("groupName",chatGroup.getName());
            jsonObject.put("groupType",chatGroup.getType());
            jsonObject.put("eventType",chatGroup.getEventType());
            jsonObject.put("eventTargetAmount",chatGroup.getTargetAmount());
            jsonObject.put("eventTargetCurrency",chatGroup.getTargetCurrency());
            jsonObject.put("eventBeneficiaryName",chatGroup.getBeneficiaryName());
            jsonObject.put("eventBeneficiaryPhone",chatGroup.getBeneficiaryPhone());
            //profile_temp_name=profile_temp_name.equals("")?JSONObject.NULL:profile_temp_name;
            jsonObject.put("tempfilename",profile_temp_name);
            JSONArray memJsonArr=new JSONArray();
//            adding members to json array
            for (GroupMember member:participants) {
                JSONObject memJson=new JSONObject();
                memJson.put("name",member.getName());
                memJson.put("phone",member.getPhone());
                memJson.put("jabberid",member.getJabberId());
                memJsonArr.put(memJson);
            }
//            adding creator/owner itself to the member list
            JSONObject memJson=new JSONObject();
            memJson.put("name",user.getName());
            memJson.put("phone",user.getPhone());
            memJson.put("jabberid",user.getJabberId());
            memJsonArr.put(memJson);

//            addding memberarr to json
            jsonObject.put("members",memJsonArr);

//            pushing json obj to returning obj
            retObj.put("jsonobj",jsonObject);
            Log.d(TAG, "createJsonParams: json-"+retObj.toString());
            return retObj;

        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
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

    private void refreshGroupImage(){
        Log.d(TAG, "refreshGroupImage: "+profile_temp_name);
        if(profile_temp_name!=null && !profile_temp_name.equals("")){
            String img= Environment.getExternalStorageDirectory() + File.separator + context.getResources().getString(R.string.dir_temp) + File.separator +profile_temp_name;
            Log.d(TAG, "refreshGroupImage: "+img);
            File imgFile=new File(img);
            if(imgFile.exists()){
                Log.d(TAG, "refreshGroupImage: exists");
                Bitmap bmImg = BitmapFactory.decodeFile(img);
                imgEventIcon.setImageBitmap(bmImg);
                imgEventIcon.setColorFilter(null);
                return;
            }
        }
        else{
            Const.EVENT_TYPE event_type= Const.EVENT_TYPE.getEventType(chatGroup.getEventType());
            if(event_type!=null && event_type.getImageResourceId(200)>0){
                imgEventIcon.setImageResource(event_type.getImageResourceId(200));
                return;
            }
        }
        imgEventIcon.setImageResource(R.drawable.event_icon_200);
    }

    private void moveProfileImage(){
        if(profile_temp_name==null || profile_temp_name.equals("")){
            return;
        }
        String tempFile=Environment.getExternalStorageDirectory() + File.separator + context.getResources().getString(R.string.dir_temp) + File.separator +profile_temp_name;
        String destFile=Environment.getExternalStorageDirectory() + File.separator + context.getResources().getString(R.string.dir_home) + File.separator+ "event" +chatGroup.getGroupId()+".jpg";
        fileHandler.copyFile(new File(tempFile),new File(destFile));
    }

    public boolean validate(){
        if(participants.size()<=0){
            AlertDialog alertDialogue = new AlertDialog.Builder(LinkaiCreateEventStep2Activity.this).create();
            alertDialogue.setTitle(res.getString(R.string.create_event_alert_title_empty_participants));
            alertDialogue.setMessage(res.getString(R.string.create_event_alert_content_empty_participants));
            alertDialogue.setButton(AlertDialog.BUTTON_NEUTRAL, res.getString(R.string.alert_btn_ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialogue.show();
            return false;
        }
        return true;
    }

}
