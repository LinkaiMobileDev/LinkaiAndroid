package com.linkai.app;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.linkai.app.libraries.Const;

import java.util.ArrayList;

public class AppAgreement extends AppCompatActivity {

    Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_app_agreement);
        res=this.getApplicationContext().getResources();
        TextView txtTerms = (TextView)findViewById(R.id.txtTerms);
        String linkTextTerms ="<a href='#'>"+res.getString(R.string.content_TnC_link)+"</a>";
        txtTerms.setText(Html.fromHtml(linkTextTerms));
        txtTerms.setMovementMethod(LinkMovementMethod.getInstance());
        txtTerms.getLinksClickable();
        txtTerms.setFocusable(true);

        Button btnContinue = (Button) findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });


//        asking for contacts permission
        askContactsPermission();

    }

//    function to ask contact permission
    public void askContactsPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {

            ArrayList<String> permissionList=new ArrayList<String>();
            if(checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){

                permissionList.add(Manifest.permission.READ_CONTACTS);

            }
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if(checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){

                permissionList.add(Manifest.permission.CALL_PHONE);
            }

            if(permissionList.size()>0) {
                ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), 100);
            }

            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        boolean isAllGranted=true;
        if (requestCode == 100) {
//            Toast.makeText(this,"result -"+grantResults[0]+" code -"+requestCode,Toast.LENGTH_SHORT).show();
            for(int i=0;i<permissions.length;i++){
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted

                } else {
                    isAllGranted=false;
                }
            }
            if (!isAllGranted) {
                AlertDialog.Builder alertBuilder=new AlertDialog.Builder(this)
                        .setTitle(res.getString(R.string.app_agreement_alert_title_grant_permission))
                        .setMessage(res.getString(R.string.app_agreement_alert_content_grant_permission))
                        .setPositiveButton(res.getString(R.string.alert_btn_Yes),new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                askContactsPermission();
                            }
                        })
                        .setNegativeButton(res.getString(R.string.alert_btn_No), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                finish();
                                System.exit(0);
                            }
                        });
                alertBuilder.create().show();
            }
        }
    }

    @Override
    protected void onResume() {
        Const.CUR_ACTIVITY= Const.APP_COMPONENTS.AppAgreement;
        super.onResume();
    }
}
