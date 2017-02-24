package com.linkai.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.linkai.app.R;

import com.linkai.app.design.RoundedImageView;
import com.linkai.app.libraries.Common;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.libraries.FileHandler;
import com.linkai.app.modals.ChatGroup;
import com.linkai.app.modals.GroupMember;

import java.io.File;
import java.util.ArrayList;

public class LinkaiCreateEventStep3Activity extends AppCompatActivity {
    private static final String TAG="Create event-3";

    private Context context;
    private FileHandler fileHandler;
    private Common common;
    private DatabaseHandler db;
    LayoutInflater mInflater;

    private Toolbar toolbar;
    Button btnContinue;
    LinearLayout lstEventParticipants;
    ListView popupList;
    TextView lblBenficiaryName;
    TextView lblBenficiaryPhone;
    TextView lblTarget;
    TextView lblEventName;
    TextView txtEventBalance;
    ImageView imgEventIcon;

    ChatGroup chatGroup=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linkai_create_event_step3);
        context=this.getApplicationContext();
        db= Const.DB;
        fileHandler=new FileHandler(context);
        common=new Common(context);
        //        actionbar
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mInflater= LayoutInflater.from(context);

        btnContinue= (Button) findViewById(R.id.btnContinue);
        lstEventParticipants= (LinearLayout) findViewById(R.id.lstEventParticipants);
        lblBenficiaryName= (TextView) findViewById(R.id.lblBenficiaryName);
        lblBenficiaryPhone= (TextView) findViewById(R.id.lblBenficiaryPhone);
        lblTarget= (TextView) findViewById(R.id.lblTarget);
        lblEventName= (TextView) findViewById(R.id.lblEventName);
        imgEventIcon= (ImageView) findViewById(R.id.imgEventIcon);
        txtEventBalance= (TextView) findViewById(R.id.txtEventBalance);

        chatGroup= getIntent().getParcelableExtra("group_object");

        lblEventName.setText(chatGroup.getName());
        txtEventBalance.setText(" "+chatGroup.getBalance()+" "+chatGroup.getTargetCurrency());
        lblBenficiaryName.setText(chatGroup.getBeneficiaryName());
        lblBenficiaryPhone.setText(chatGroup.getBeneficiaryPhone());

//        Log.d(TAG, "onCreate targetAmount: "+chatGroup.getTargetAmount());
        String target=String.format("%.2f",chatGroup.getTargetAmount())+" "+chatGroup.getTargetCurrency();
        lblTarget.setText(target);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent grpChatIntent=new Intent(context,GroupChatBoxActivity.class);
                grpChatIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                grpChatIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                grpChatIntent.putExtra("groupId",chatGroup.getGroupId());
                startActivity(grpChatIntent);
                finish();
            }
        });

        showGroupProfileImage();
        listParticipants();
    }

    @Override
    protected void onResume() {
        Const.CUR_ACTIVITY= Const.APP_COMPONENTS.LinkaiCreateEventStep3Activity;
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
        //super.onBackPressed();
        Intent homeIntent=new Intent(context,HomeActivity.class);
//        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
        finish();
    }

    private void showGroupProfileImage(){
        File imgFile=new File(Environment.getExternalStorageDirectory() + File.separator + context.getResources().getString(R.string.dir_home) + File.separator +"event"+chatGroup.getGroupId()+".jpg");
        if(imgFile.exists()){

                Bitmap bmImg = BitmapFactory.decodeFile(imgFile.getPath());
                imgEventIcon.setImageBitmap(bmImg);
                imgEventIcon.setColorFilter(null);
                return;

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

    private void listParticipants(){
        ArrayList<GroupMember> participants=db.getGroupMembers(chatGroup.getGroupId());
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

}
