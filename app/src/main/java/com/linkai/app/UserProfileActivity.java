package com.linkai.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.linkai.app.R;

import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.libraries.FileHandler;
import com.linkai.app.modals.ChatUser;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserProfileActivity extends AppCompatActivity {
    String TAG="SettingsActivity";
    protected DatabaseHandler db;
    protected FileHandler fileHandler;
    public Context context;
    ImageView imgView;
    Button btnContinue;
    EditText txtName;
    EditText txtPhone;
    EditText txtEmail;
    EditText txtAddress;

    ChatUser objChatUsr;
    ProgressDialog progressDialog;
    File camPhotoFile=null;
    FloatingActionButton fabCamera;

    private final int REQUEST_IMAGE_FROM_GALLARY=123;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate:1");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle("");
        Log.d(TAG, "onCreate:2");

        context=getApplicationContext();
        db= Const.DB;
        fileHandler=new FileHandler(context);
        objChatUsr=db.getUser();
        imgView=(ImageView)findViewById(R.id.imgUser);
        progressDialog=new ProgressDialog(this);
        refreshProfileImage();
        Log.d(TAG, "onCreate:3");
        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browse_file_intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(browse_file_intent, REQUEST_IMAGE_FROM_GALLARY);
                ///UploadFile();
            }
        });
        txtName = (EditText) findViewById(R.id.txtName);
        txtPhone= (EditText) findViewById(R.id.txtPhone);
        txtEmail= (EditText) findViewById(R.id.txtEmail);
        txtAddress= (EditText) findViewById(R.id.txtAddress);

        txtName.setText(objChatUsr.getName());
        txtPhone.setText(objChatUsr.getPhone());
        txtEmail.setText(objChatUsr.getEmail());
        txtAddress.setText(objChatUsr.getAddress());

        fabCamera=(FloatingActionButton) findViewById(R.id.fabCamera);


        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String imageFile = Environment.getExternalStorageDirectory() + File.separator + context.getResources().getString(R.string.dir_temp) + File.separator +db.getUser().getJabberId()+".jpg";
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

        btnContinue=(Button)findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProfile();

            }
        });

        Log.d(TAG, "onCreate:4");
    }

    @Override
    protected void onResume() {
        Const.CUR_ACTIVITY= Const.APP_COMPONENTS.UserProfileActivity;
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Toast.makeText(context, "image selected", Toast.LENGTH_SHORT).show();
        //Log.d("SingleChat", "onActivityResult: ");
        try {

            if (requestCode == Const.ATTACHMENT_TYPE.CAMERA.toInteger() && resultCode == RESULT_OK) {
                if(camPhotoFile!=null){
                    String imageFile=Environment.getExternalStorageDirectory() + File.separator + context.getResources().getString(R.string.dir_temp) + File.separator +db.getUser().getJabberId()+".jpg";
                    upLoadImage(imageFile, imageFile);
                    camPhotoFile=null;
                }
            }
            else if (requestCode==REQUEST_IMAGE_FROM_GALLARY && resultCode == RESULT_OK && null != data){
                // Get the Image from data

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
                String imgOut=Environment.getExternalStorageDirectory() + File.separator + context.getResources().getString(R.string.dir_temp) + File.separator +db.getUser().getJabberId()+".jpg";
                upLoadImage(imgPath,imgOut);
//

            } else {
                //Toast.makeText(this, "You haven't picked Image",Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG) .show();
            //Log.d("SinglrChat", "onActivityResult: "+e.getMessage());
        }

    }

    @Override
    public void onBackPressed() {
        if(db.getUser().getName().trim().equals("")){
            txtName.setError("Enter Your Name");
            txtName.requestFocus();
            return;
        }
        else {
            super.onBackPressed();
        }
    }

    private void saveProfile(){
        try {
//validating name
            if(txtName.getText().toString().trim().equals("")){
                txtName.setError("Enter Your Name");
                txtName.requestFocus();
                return;
            }
//            validating email address
            if(!txtEmail.getText().toString().trim().equals("") && !Patterns.EMAIL_ADDRESS.matcher(txtEmail.getText().toString().trim()).matches()){
                txtEmail.setError("Invalid email");
                txtEmail.requestFocus();
                return;
            }
            objChatUsr.setName(txtName.getText().toString().trim());
            objChatUsr.setEmail(txtEmail.getText().toString().trim());
            objChatUsr.setAddress(txtAddress.getText().toString().trim());
            db.updateUser(objChatUsr);
            refreshProfileImage();
            Log.d("update username", "updated");

            Intent homeIntent = new Intent(context, HomeActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(homeIntent);
            finish();

        } catch (Exception ex) {

        }
    }

    public  boolean upLoadImage(String imgPath,String outImg){
        try{
            //                copy and resize profile image
            final String outImgPath=outImg;
            if(!fileHandler.copyAndResizeImage(imgPath,outImgPath)){
                return false;
            }
            //refreshing image after copying
            refreshProfileImage();
            //upload to server
            AsyncTask<Void,Void,Boolean> uploadThread=new AsyncTask<Void, Void, Boolean>(){

                @Override
                protected void onPreExecute() {
                    showProgress(true,"Uploading profile image...");
                    super.onPreExecute();
                }

                @Override
                protected Boolean doInBackground(Void... voids) {
                    return fileHandler.uploadProfileImage(outImgPath,0)==200;
                }

                @Override
                protected void onPostExecute(Boolean aBoolean) {
                    showProgress(false,null);
                    if(aBoolean){
                        moveProfileImage();
                    }else{
                        Toast.makeText(context,"Failed to update profile. Try again.",Toast.LENGTH_SHORT).show();
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

    public  void  refreshProfileImage(){
        try {
            String userImgPath = Environment.getExternalStorageDirectory() + File.separator + context.getResources().getString(R.string.dir_home) + File.separator + ((ChatUser) db.getUser()).getJabberId() + ".jpg";
            Log.d("userImgPath", "" + userImgPath);
            File file = new File(userImgPath);
            if (file.exists()){
                Bitmap bmImg = BitmapFactory.decodeFile(userImgPath);
                imgView.setImageBitmap(bmImg);
                imgView.setColorFilter(null);
            }
            else{
                imgView.setImageResource(R.drawable.ic_account_black_48dp);
                imgView.setColorFilter(ContextCompat.getColor(context,R.color.colorUserIcon));
            }
        }
        catch (Exception ex)
        {
        }
    }

    private void moveProfileImage(){

        String tempFile=Environment.getExternalStorageDirectory() + File.separator + context.getResources().getString(R.string.dir_temp) + File.separator + db.getUser().getJabberId() + ".jpg";
        String destFile=Environment.getExternalStorageDirectory() + File.separator + context.getResources().getString(R.string.dir_home) + File.separator + db.getUser().getJabberId() + ".jpg";
        if(fileHandler.copyFile(new File(tempFile),new File(destFile))){
            refreshProfileImage();
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

}
