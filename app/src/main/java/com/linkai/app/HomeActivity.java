package com.linkai.app;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.linkai.app.Fragments.ChatFragment;
import com.linkai.app.Fragments.ContactsFragment;
import com.linkai.app.Fragments.MyLinkaiFragment;

import com.linkai.app.R;

import com.linkai.app.Utils.SimpleJsonCallback;
import com.linkai.app.adapters.HomePagerAdapter;
import com.linkai.app.adapters.NavListAdapter;
import com.linkai.app.design.RoundedImageView;
import com.linkai.app.libraries.Common;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.Utils.LinkaiUtils;
import com.linkai.app.libraries.MyXMPP;
import com.linkai.app.modals.ChatUser;

import org.acra.ACRA;
import org.json.JSONObject;

import java.io.File;

public class HomeActivity extends AppCompatActivity {
    private final String TAG="HomeActivity";

    private MyXMPP xmpp ;
    private Context context;
    Resources res;

    protected DatabaseHandler db;
    Common common;
    ChatUser user;

    private HomePagerAdapter homePagerAdapter;
    private BroadcastReceiver chatBroadReceiver;
    private LocalBroadcastManager localBroadcastManager;

    private ViewPager mViewPager;
    private TabLayout tabLayout;

    private DrawerLayout drawerLayout;
    NavigationView navigationView;
    private RoundedImageView imgNavProfile;
    private TextView txtNavProfileName;
    private ListView lstNavList1;
    private ListView lstNavList2;
//    TabHost host;
    private String searchQry=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        to hide after testing face
        ACRA.init(this.getApplication());

        context=this.getApplicationContext();
        res=context.getResources();
        db=Const.DB;
        user=db.getUser();
        localBroadcastManager=LocalBroadcastManager.getInstance(context);
        //        checking is first login
        checkRegistrationStatus();

        setContentView(R.layout.activity_home);


        common=new Common(context);
//        create directories
        common.createAppDirectories();
//        getting instance of xmpp
        xmpp=((ChatApplication)getApplication()).getMyXMPPInstance();
        if(xmpp==null)return;
//        re initializing global xmpp object
//        ((ChatApplication)getApplication()).InitXMPPInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //toolbar.setTitle("Hawala Care");
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        homePagerAdapter=new HomePagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        tabLayout= (TabLayout) findViewById(R.id.tabs);
        //      mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setAdapter(homePagerAdapter);
        mViewPager.setCurrentItem(1);
        tabLayout.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
           @Override
           public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
           }

           @Override
           public void onPageSelected(int position) {
               setCurrentFragment(position);
           }

           @Override
           public void onPageScrollStateChanged(int state) {
           }
       });

//        setting up side menu/ navigation drawer
        drawerLayout= (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView= (NavigationView) findViewById(R.id.nav_view);
        View navHeaderView=navigationView.getHeaderView(0);
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        txtNavProfileName= (TextView) navHeaderView.findViewById(R.id.txtNavProfileName);
        imgNavProfile= (RoundedImageView) navHeaderView.findViewById(R.id.imgNavProfile);
        lstNavList1= (ListView) findViewById(R.id.lstNavList1);
//        lstNavList2= (ListView) findViewById(R.id.lstNavList2);
//        setting username in side bar
        if(user!=null) {
            if(user.getName().equals("")){
                Intent intent=new Intent(context,UserProfileActivity.class);
                startActivity(intent);
            }
            txtNavProfileName.setText(user.getName());
        }
        else{
            txtNavProfileName.setText("");
        }
//        setting first list block
        int[] iconsLst={0,0,0,R.drawable.ic_person_add_black_36dp,R.drawable.ic_twitter_black_36dp,R.drawable.ic_settings_black_36dp};
        String[] strNavList1={res.getString(R.string.home_nav_text_edit_name),res.getString(R.string.home_nav_text_new_picture),res.getString(R.string.home_nav_text_choose_from_gallery),res.getString(R.string.home_nav_text_add_contact),res.getString(R.string.home_nav_text_share_linkai),res.getString(R.string.home_nav_text_settings)};
        NavListAdapter adapterNavLst1=new NavListAdapter(this,iconsLst,strNavList1);
        lstNavList1.setAdapter(adapterNavLst1);
        lstNavList1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                    case 1:
                    case 2:
                        Intent intent=new Intent(context,UserProfileActivity.class);
                        startActivity(intent);
//                        finish();
                        break;
                    default:break;
                }
            }
        });
//        setting second list
//        NavListAdapter adptNavList2=new NavListAdapter(this,iconsLst2,strNavTexts2);
//        lstNavList2.setAdapter(adptNavList2);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                Log.d(TAG, "onNavigationItemSelected: "+item.getItemId());
                switch (item.getItemId()){
                    case R.id.nav_edit_name:
                    case R.id.nav_choose_from_gallery:
                    case R.id.nav_take_picture:
                    case R.id.nav_settings:
                        Intent intent=new Intent(context,UserProfileActivity.class);
                        startActivity(intent);
//                        finish();
                        break;
                    default:break;
                }
                return true;
            }
        });

//        call to start main service
        common.startMainService();
//        set presence to online
        xmpp.setPresence(true);

        //      call to signin in linkai server
        new LinkaiUtils(context).signIn(new SimpleJsonCallback() {
            @Override
            public void onSuccess(JSONObject jsonObject) {

            }

            @Override
            public void onError(JSONObject jsonObject) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
// search view listner
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchQry=newText;
                Log.d(TAG, "onQueryTextChange: "+newText);
                refreshChats();
                refreshContacts();

                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//        Log.d(TAG, "onOptionsItemSelected: "+item.getItemId());
//        noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            common.callSyncContactsService();
            common.callSyncGroupsService();
        }
        else if(id==R.id.action_new_group){
//            Log.d("HomeActivity", "onOptionsItemSelected: "+id);
            Intent intent=new Intent(context,LinkaiCreateEventStep1Activity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
        else if(id==R.id.action_linkai){
            Intent intent=new Intent(context,LinkaiContactsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        //call to start background services
        common.initServices();
//        set current activity
        Const.CUR_ACTIVITY= Const.APP_COMPONENTS.HomeActivity;
        setCurrentFragment(mViewPager.getCurrentItem());
//        Log.d(TAG, "onResume: cur_act "+Const.CUR_ACTIVITY);
        super.onResume();
        //      setting user image
        try {
            String userImgPath = Environment.getExternalStorageDirectory() + File.separator + context.getResources().getString(R.string.dir_home) + File.separator + ((ChatUser) db.getUser()).getJabberId() + ".jpg";

            File file = new File(userImgPath);
            if (file.exists()) {
                Bitmap bmImg = BitmapFactory.decodeFile(userImgPath);
                imgNavProfile.setImageBitmap(bmImg);
            }
            else{
                imgNavProfile.setImageResource(R.drawable.ic_user);
            }
        }
        catch (Exception ex){
            imgNavProfile.setImageResource(R.drawable.ic_user);
        }
//        checking is first login
        checkRegistrationStatus();
//        refreshing chats on resuming
        refreshChats();


        chatBroadReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                Toast.makeText(context,"rec-"+intent.getAction(),Toast.LENGTH_SHORT).show();
                switch (intent.getAction()){
                    case "chat.roster.received": refreshContacts();
                        break;
                    case "chat.message.received":refreshChats();
//                        Log.d(TAG, "onResume: cur_act 1"+Const.CUR_ACTIVITY);
                        break;
                    case "group.message.received":refreshChats();
                        break;
                    case "linkai.view.refresh":refreshMyLinkai();
                        break;
                    case "chat.view.refresh":
                        refreshChats();
                        refreshContacts();
                        refreshMyLinkai();
//                        Log.d(TAG, "onResume: cur_act 2"+Const.CUR_ACTIVITY);
                        break;
                    default:break;
                }
            }
        };

        //Register BROADCAST
        try {
            localBroadcastManager.registerReceiver(chatBroadReceiver,new IntentFilter("chat.message.received"));
            localBroadcastManager.registerReceiver(chatBroadReceiver,new IntentFilter("chat.roster.received"));
            localBroadcastManager.registerReceiver(chatBroadReceiver,new IntentFilter("chat.view.refresh"));
            localBroadcastManager.registerReceiver(chatBroadReceiver,new IntentFilter("group.message.received"));
            localBroadcastManager.registerReceiver(chatBroadReceiver,new IntentFilter("linkai.view.refresh"));
        }
        catch (Exception e){
        }
    }

    @Override
    public void onPause() {
        super.onPause();

//        UNREGISTER BROADCAST
        try {
            localBroadcastManager.unregisterReceiver(chatBroadReceiver);
        }
        catch (Exception e){

        }
    }

    @Override
    public void onStop() {
        super.onStop();

//        UNREGISTER BROADCAST
        try {
            localBroadcastManager.unregisterReceiver(chatBroadReceiver);
        }
        catch (Exception e){

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }

    public void refreshContacts(){
        if(ContactsFragment.getInstance()!=null){
            if(searchQry==null || searchQry.equals("")) {
                ContactsFragment.getInstance().showAllFriends(false, searchQry);
            }
            else{
                ContactsFragment.getInstance().showAllFriends(true, searchQry);
            }
        }
    }

    public void refreshChats(){
        if(ChatFragment.getInstatnce()!=null){
            ChatFragment.getInstatnce().showAllChats(searchQry);
        }
    }

    public void refreshMyLinkai(){
        if(MyLinkaiFragment.getInstatnce()!=null){
            MyLinkaiFragment.getInstatnce().refreshMyLinkai();
        }
    }

    //    check if user is registered
    private boolean checkRegistrationStatus(){
//        //Toast.makeText(this.getApplicationContext(),user.getName()+"--"+user.getPassword(),Toast.LENGTH_SHORT).show();
        if(!db.isUserExist()){

            this.startActivity(new Intent(this.getApplicationContext(),AppAgreement.class));
            finish();
            return false;
        }
        return true;
    }

//    to set current fragment constant
    private void setCurrentFragment(int viewpager_position){
//        Log.d(TAG, "setCurrentFragment: "+viewpager_position);
        switch (viewpager_position){
            case 0:Const.CUR_FRAGMENT= Const.APP_COMPONENTS.ContactsFragment;break;
            case 1:Const.CUR_FRAGMENT= Const.APP_COMPONENTS.ChatFragment;break;
            case 2:Const.CUR_FRAGMENT= Const.APP_COMPONENTS.MyLinkaiFragment;break;
            default:break;
        }
    }

//    to restart service periodically
//    private void startAlarmForMainService(){
//        Intent ll24 = new Intent(context, AppStartReceiver.class);
//        PendingIntent recurringLl24 = PendingIntent.getBroadcast(context, 0, ll24, PendingIntent.FLAG_CANCEL_CURRENT);
//        AlarmManager alarms = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        alarms.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 3000, recurringLl24);
//
//    }


}
