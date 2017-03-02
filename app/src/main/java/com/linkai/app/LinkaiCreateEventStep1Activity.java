package com.linkai.app;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.linkai.app.R;

import com.linkai.app.adapters.SimpleFriendsListAdapter;
import com.linkai.app.adapters.SpinnerWithImageAdapter;
import com.linkai.app.libraries.Common;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.libraries.FileHandler;
import com.linkai.app.modals.ChatFriend;
import com.linkai.app.modals.ChatGroup;

import java.io.File;
import java.util.ArrayList;

public class LinkaiCreateEventStep1Activity extends AppCompatActivity {
    private static final String TAG="Create event-1";

    private Context context;
    private FileHandler fileHandler;
    private Common common;
    private DatabaseHandler db;

    private Toolbar toolbar;
    private Spinner spinnerCurrency;
    private Spinner spinnerEventCategory;
    Button btnContinue;
    Button btnAddBeneficiary;
    ListView popupList;
    TextView lblBenficiaryName;
    TextView lblBenficiaryPhone;
    FloatingActionButton fabCamera;
    ImageView imgEventIcon;
    EditText txtEventName;
    EditText txtAmount;

    SimpleFriendsListAdapter popupAdapter;
    AlertDialog.Builder listDialogueBuilder;
    ArrayList<ChatFriend> allFriends;
    File camPhotoFile=null;
    ChatGroup chatGroup=null;
    ChatFriend beneficiary=null;
    ProgressDialog progressDialog;


    private final int REQUEST_IMAGE_FROM_GALLARY=114;
    String profile_temp_name=null;
    String temp_filename=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linkai_create_event_step1);
        context=this.getApplicationContext();
        db= Const.DB;
        fileHandler=new FileHandler(context);
        common=new Common(context);
        chatGroup=new ChatGroup();
        //        actionbar
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        spinnerCurrency=(Spinner)findViewById(R.id.spinnerCurrency);
        spinnerEventCategory= (Spinner) findViewById(R.id.spinnerEventCategory);
        btnContinue= (Button) findViewById(R.id.btnContinue);
        btnAddBeneficiary= (Button) findViewById(R.id.btnAddBeneficiary);
        lblBenficiaryName= (TextView) findViewById(R.id.lblBenficiaryName);
        lblBenficiaryPhone= (TextView) findViewById(R.id.lblBenficiaryPhone);
        fabCamera=(FloatingActionButton) findViewById(R.id.fabCamera);
        imgEventIcon= (ImageView) findViewById(R.id.imgEventIcon);
        txtEventName= (EditText) findViewById(R.id.txtEventName);
        txtAmount= (EditText) findViewById(R.id.txtAmount);
        popupList=new ListView(this);

        listDialogueBuilder=new AlertDialog.Builder(LinkaiCreateEventStep1Activity.this);
        allFriends=db.getAllFriends(Const.FRIEND_SUBSCRIPTION_STATUS.SUBSCRIBED);
        popupAdapter=new SimpleFriendsListAdapter(this,allFriends);
        popupList.setAdapter(popupAdapter);
        listDialogueBuilder.setView(popupList);
        final Dialog dialog=listDialogueBuilder.create();

        btnAddBeneficiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        popupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                beneficiary=(ChatFriend) adapterView.getItemAtPosition(i);
                lblBenficiaryName.setText(beneficiary.getName());
                lblBenficiaryPhone.setText(beneficiary.getPhone());
//                dismiss dialog popup
                dialog.dismiss();
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Continue();
            }
        });

        imgEventIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browse_file_intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(browse_file_intent, REQUEST_IMAGE_FROM_GALLARY);
                ///UploadFile();
            }
        });

        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    profile_temp_name = "temp_"+fileHandler.createRandomFileName()+".jpg";
                    String imageFile = Environment.getExternalStorageDirectory() + File.separator + context.getResources().getString(R.string.dir_temp) + File.separator +profile_temp_name;
                    camPhotoFile=new File(imageFile);
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(camPhotoFile));
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, Const.ATTACHMENT_TYPE.CAMERA.toInteger());
                    }

                } catch (Exception ex) {

                }
            }
        });

        //        setting value in currency spinner
        loadcurrencySpinner();
//        setting value in event type spinner
        loadEventTypespinner();
//        refreshing img
        refreshGroupImage();
    }

    @Override
    protected void onResume() {
        Const.CUR_ACTIVITY= Const.APP_COMPONENTS.LinkaiCreateEventStep1Activity;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: "+requestCode+" "+resultCode);
        try {
            if (requestCode == Const.ATTACHMENT_TYPE.CAMERA.toInteger() && resultCode == RESULT_OK) {
                if(camPhotoFile!=null){
                   // Log.d(TAG, "onActivityResult: "+camPhotoFile.exists());
                    if(!camPhotoFile.exists()){
                        profile_temp_name="";
                    }
                    camPhotoFile=null;
                }
                else{
                    profile_temp_name="";
                }
            }
            else if (requestCode==REQUEST_IMAGE_FROM_GALLARY && resultCode == RESULT_OK && null != data){
                Uri selectedImage = data.getData();

//              fileHandler.uploadFile(selectedImage);
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();
                String imgPath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
                Log.d("imgPath", "onActivityResult: "+imgPath);
                cursor.close();
                profile_temp_name = "temp_"+fileHandler.createRandomFileName()+".jpg";
                String imgOut=Environment.getExternalStorageDirectory() + File.separator + context.getResources().getString(R.string.dir_temp) + File.separator +profile_temp_name;
                fileHandler.copyFile(new File(imgPath),new File(imgOut));
            } else {
                //profile_temp_name="";
            }
            Log.d(TAG, "onActivityResult: "+profile_temp_name);
            //        refreshing img
            if(profile_temp_name!=null && !profile_temp_name.equals("") && !profile_temp_name.equals(temp_filename)){
                String filePath=Environment.getExternalStorageDirectory() + File.separator + context.getResources().getString(R.string.dir_temp) + File.separator +profile_temp_name;
                upLoadImage(filePath);
                //refreshGroupImage();
            }
            else{
//                restore group image if profile_nmae is not satisfying the above condition
                restoreGroupImage();
            }
            Log.d(TAG, "onActivityResult: "+profile_temp_name);

        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG) .show();
            //Log.d("SinglrChat", "onActivityResult: "+e.getMessage());
            profile_temp_name="";
        }

    }

    public  boolean upLoadImage(final String imgPath){
        try{
            //                copy and resize profile image
            Log.d(TAG, "upLoadImage: "+imgPath);
            if(!fileHandler.copyAndResizeImage(imgPath,imgPath)){
                // restore group image if error in copying
                restoreGroupImage();
                return false;
            }
            //upload to server
            AsyncTask<Void,Void,Boolean> uploadThread=new AsyncTask<Void, Void, Boolean>(){

                @Override
                protected void onPreExecute() {
                    showProgress(true,"Uploading Group image...");
                    super.onPreExecute();
                }

                @Override
                protected Boolean doInBackground(Void... voids) {
                    if(fileHandler.uploadProfileImage(imgPath,1)==200){
                        return true;
                    }
                    else{
                        return false;
                    }
                }

                @Override
                protected void onPostExecute(Boolean aBoolean) {
                    showProgress(false,null);
                    if(aBoolean) {
//                        refresh image
                        refreshGroupImage();
//                        set temp filename with newly uploaded filename
                        temp_filename=profile_temp_name;
                    }
                    else{
//                        restore group image if uploading failed
                        restoreGroupImage();
                    }
                    super.onPostExecute(aBoolean);
                }
            };
            uploadThread.execute();
            return true;

        }
        catch (Exception ex){
            ex.printStackTrace();
            Log.d(TAG, "upLoadImage: Error"+ex.getMessage());
            return  false;
        }
    }

    private void Continue(){
        if(validate()) {
            chatGroup.setOwner(db.getUser().getJabberId());
            chatGroup.setName(txtEventName.getText().toString().trim());
            chatGroup.setTargetAmount(Double.parseDouble(txtAmount.getText().toString().trim()));
            chatGroup.setTargetCurrency(spinnerCurrency.getSelectedItem().toString());
            chatGroup.setBeneficiaryName(beneficiary.getName());
            chatGroup.setBeneficiaryPhone(beneficiary.getPhone());
            chatGroup.setType(Const.GROUP_TYPE.EVENT);
            chatGroup.setEventType((int)spinnerEventCategory.getSelectedItemId());
            Log.d(TAG, "event target: "+chatGroup.getTargetAmount());
            //if(true)return;
            //Log.d(TAG, "Continue: id "+spinnerEventCategory.getSelectedItemId());
            Intent step2Intent = new Intent(context, LinkaiCreateEventStep2Activity.class);
            step2Intent.putExtra("group_object",chatGroup);
            step2Intent.putExtra("profile_temp_name",profile_temp_name);
            startActivity(step2Intent);
        }

    }


    public void loadcurrencySpinner(){
        //        setting value in currency spinner
        SharedPreferences prefs = context.getSharedPreferences(Const.LINKAI_SHAREDPREFERENCE_FILE, Context.MODE_PRIVATE);
        ArrayList<String> currencyList=new ArrayList<String>();
        currencyList.add(prefs.getString("currency","IQD"));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,R.layout.app_spinner,currencyList);
        adapter.setDropDownViewResource(R.layout.app_spinner);
        spinnerCurrency.setAdapter(adapter);
    }

    public void loadEventTypespinner(){
        String[] names=Const.EVENT_TYPE.toNameArray();
        Integer[] images=Const.EVENT_TYPE.toResourceArray(100);
        int[] values= Const.EVENT_TYPE.toValueArray();
        SpinnerWithImageAdapter adapter=new SpinnerWithImageAdapter(context,images,names,values);
        spinnerEventCategory.setAdapter(adapter);
    }

    private boolean validate(){
        if(txtEventName.getText().toString().trim().equals("")){
            txtEventName.setError("Enter event name");
            txtEventName.setFocusable(true);
            return false;
        }
        else if(txtAmount.getText().toString().trim().equals("")){
            txtAmount.setError("Enter target amount");
            txtAmount.setFocusable(true);
            return false;
        }
        else if(beneficiary==null){
            lblBenficiaryName.setError("Choose beneficiary");
            lblBenficiaryName.setFocusable(true);
            return false;
        }
        return true;
    }

    private void refreshGroupImage(){
        if(profile_temp_name!=null && !profile_temp_name.equals("")){
            String img=Environment.getExternalStorageDirectory() + File.separator + context.getResources().getString(R.string.dir_temp) + File.separator +profile_temp_name;
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
        imgEventIcon.setImageResource(R.drawable.event_icon_200);
    }

//    to restore previously uploaded profile image if new uploading failed
    private void restoreGroupImage(){
        profile_temp_name=temp_filename;
        refreshGroupImage();
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

}
