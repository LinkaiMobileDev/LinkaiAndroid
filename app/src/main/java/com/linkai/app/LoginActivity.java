package com.linkai.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.linkai.app.R;

import com.linkai.app.Utils.LinkaiRequestHeader;
import com.linkai.app.libraries.Common;
import com.linkai.app.libraries.Const;
import com.linkai.app.libraries.DatabaseHandler;
import com.linkai.app.modals.ChatUser;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity  {
    private final String TAG="LoginActivity";
    Context context;
    Activity activity;
    BroadcastReceiver broadcastReceiver;
    Resources res;
   // Toolbar toolbar;
    Common common;
    DatabaseHandler db;
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };


    // UI references.
    private EditText txtPhone;
    private EditText txtUserName;
    private ProgressDialog progressDialog;
    private AutoCompleteTextView autoCompleteTextViewCountry;
    private Button btnLogin;
    private Spinner spinnerCurrency;
    private String _username;
    private String _phone;
    private String _phoneForAlert;
    private String _countryCode="";
    private String _countryCode_alpha="";
    private String currency_code;
    private String guesscountryISOCode;
    private String linkaiVerificationPassword;
    ChatUser user;
    HashMap<String,String> map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context=this.getApplicationContext();
        activity=this;
        res=context.getResources();
        common=new Common(context);
        db=Const.DB;
        //toolbar= (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        // Set up the login form.
        txtUserName = (EditText) findViewById(R.id.txtUserName);
        txtPhone = (EditText) findViewById(R.id.txtPhone);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        autoCompleteTextViewCountry = (AutoCompleteTextView)findViewById(R.id.autoCompleteTextViewCountry);
        spinnerCurrency= (Spinner) findViewById(R.id.spinnerCurrency);
        try
        {
            TelephonyManager teleMgr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            if (teleMgr != null){
                guesscountryISOCode = teleMgr.getSimCountryIso();
                Log.d("guesscountryISOCode", guesscountryISOCode);

            }

        }
        catch (Exception ex)
        {}
        btnLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
              attemptLogin();

            }
        });

        autoCompleteTextViewCountry.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                autoCompleteTextViewCountry.showDropDown();
                return false;
            }
        });

        autoCompleteTextViewCountry.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getCurrencies();
            }
        });

        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);



        loadCountries();

//        guessCountry();
//        clearing sharedpreference
        getSharedPreferences(Const.LINKAI_SHAREDPREFERENCE_FILE, Context.MODE_PRIVATE).edit().clear().commit();
//load currency
        loadCurrencies(null);
    }

    @Override
    protected void onResume() {
        Const.CUR_ACTIVITY= Const.APP_COMPONENTS.LoginActivity;
        super.onResume();
        broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")){
                    getCurrencies();
                }
            }
        };
        context.registerReceiver(broadcastReceiver,new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        context.unregisterReceiver(broadcastReceiver);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        Log.d(TAG, "attemptLogin: ");

        View focusView = null;
        boolean cancel = false;

        // Reset errors.
        txtUserName.setError(null);
        txtPhone.setError(null);

        // Store values at the time of the login attempt.
        _username = txtUserName.getText().toString();
        _countryCode=map.get(autoCompleteTextViewCountry.getText().toString());
        currency_code= spinnerCurrency.getSelectedItem()==null?null:spinnerCurrency.getSelectedItem().toString();

        if(txtPhone.getText().toString()!=null){
            _phone=trimLeadingZeros(txtPhone.getText().toString().replace("-", "").replace(" ", "").replace("(","").replace(")",""));
//            Log.d(TAG, "phone after trim: "+_phone);
        }
        _phoneForAlert="(+"+_countryCode+")"+_phone;
        _phone ="+"+_countryCode+_phone;

//        Log.d("attemptLogin", "attemptLogin: " + _phone);

        user=new ChatUser();
        user.setJabberId("");
        user.setPhone(_phone);
        user.setName("");

//checking internet connection
        if(!common.isNetworkAvailable()){
            AlertDialog alertBuilder=new AlertDialog.Builder(LoginActivity.this).create();
            alertBuilder.setTitle(res.getString(R.string.login_alert_title_internet_notification));
            alertBuilder.setMessage(res.getString(R.string.login_alert_content_internet_notification));
            alertBuilder.setButton(AlertDialog.BUTTON_NEUTRAL, res.getString(R.string.alert_btn_ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertBuilder.show();
            return;
        }
//validating country code
        if(_countryCode==null || _countryCode.equals("")){
            autoCompleteTextViewCountry.setError("Invalid Country Name");
            focusView=autoCompleteTextViewCountry;
            cancel=true;
        }

//        validating currency
        if(currency_code==null || currency_code.equals("SELECT") || currency_code.equals("")){
            ((TextView)spinnerCurrency.getSelectedView()).setError("Choose currency");
            focusView=spinnerCurrency;
            cancel=true;
        }

        // Check for a valid phone, if the user entered one.
        else if (!TextUtils.isEmpty(_phone) && !isPhoneValid(_phone)){
            txtPhone.setError("Invalid Phone Number");
            focusView = txtPhone;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            AlertDialog.Builder builder= new AlertDialog.Builder(LoginActivity.this);
                    //.setIcon(android.R.drawable.ic_dialog_alert)
                    builder.setTitle(res.getString(R.string.login_alert_title_confirmation));
                    builder.setMessage(Html.fromHtml("<font color='#808080' size='12'>"+_phoneForAlert +"<br/>"+res.getString(R.string.login_alert_content1_confirmation)+"<br/><br/>"+res.getString(R.string.login_alert_content2_confirmation)+"</font>"));

                    builder.setPositiveButton(res.getString(R.string.login_alert_btn_confirm), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Log.d("Register", "attemptLogin: b4 calling userlogin task");
                            linkaiSignUp();
//                            getCurrencies();
                        }
                    })
                    .setNegativeButton(res.getString(R.string.login_alert_btn_edit), null);
                    //.show();
            AlertDialog alert = builder.create();
            alert.show();
            Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
            nbutton.setTextColor(Color.GRAY);
            Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
            pbutton.setTextColor(ContextCompat.getColor(context,R.color.colorPrimary));

        }
    }
    //    to trim leading zeros from alphanumeric string
    private static String trimLeadingZeros(String source){
        int length = source.length();

        if (length < 2)
            return source;

        int i;
        for (i = 0; i < length-1; i++)
        {
            char c = source.charAt(i);
            if (c != '0')
                break;
        }

        if (i == 0)
            return source;

        return source.substring(i);
    }

    private boolean isPhoneValid(String phone){
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneProto;
        try {
            phoneProto = phoneUtil.parse(phone, "");
//            _countryCode_alpha=phoneUtil.getRegionCodeForNumber(phoneProto);
//            Log.d(TAG, "isPhoneValid: "+_countryCode_alpha);
        } catch (NumberParseException e) {
            return false;
        }
        //TODO: Replace this with your own logic
        return phoneUtil.isValidNumber(phoneProto);
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

    public void linkaiSignUp(){
//        save currency in shared preference
        SharedPreferences.Editor editor=context.getSharedPreferences(Const.LINKAI_SHAREDPREFERENCE_FILE,Context.MODE_PRIVATE).edit();
        editor.putString("currency",currency_code);
        editor.commit();

//        json object for request
        JSONObject parameters = null;
        try {
            parameters = new JSONObject();
            parameters.put("full_name","");
            parameters.put("phone_number",_phone);
            parameters.put("pin","1234");
            parameters.put("country_code",_countryCode_alpha);
            parameters.put("currency_code",currency_code);
            parameters.put("agreed_to_terms_and_conditions",true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "linkaiSignUp: "+parameters);
        String url= Const.LINKAI_SIGNUP_URL;
//        call to show progress
        showProgress(true,"Please wait..");
        JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "onResponse: "+response);

                //        call to dismiss progress
                showProgress(false,null);
                try {
                    linkaiVerificationPassword=response.getString("password");
                    user.setLinkaiPassword(response.getString("password"));
                    //        delete user if exist
                    db.deleteUser();
            //        add user
                    db.addUser(user);
            //        clear existing friendslist
                    db.clearFriendList();
            //        call service add friends
                    common.callSyncContactsService();
////                    signin async
//                    LinkaiUtils linkaiUtils=new LinkaiUtils(context);
//                    linkaiUtils.signIn(true);

                    Intent verActivityIntent=new Intent(context,MobileVerificationActivity.class);
                    verActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    verActivityIntent.putExtra("phone", _phone);
                    verActivityIntent.putExtra("country",autoCompleteTextViewCountry.getText().toString());
                    verActivityIntent.putExtra("country_code",_countryCode);
                    verActivityIntent.putExtra("password",linkaiVerificationPassword);
                    context.startActivity(verActivityIntent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                String errorbody="";
                try {
                    errorbody=new String(error.networkResponse.data,"UTF-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "onErrorResponse: error body-"+errorbody);
                //        call to dismiss progress
                showProgress(false,null);
                AlertDialog alertBuilder=new AlertDialog.Builder(LoginActivity.this).create();
                alertBuilder.setTitle(res.getString(R.string.login_alert_alert_title_signup_failed));
                alertBuilder.setMessage(res.getString(R.string.login_alert_alert_content_signup_failed));
                alertBuilder.setButton(AlertDialog.BUTTON_NEUTRAL, res.getString(R.string.alert_btn_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertBuilder.show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                LinkaiRequestHeader linkaiRequestHeader=new LinkaiRequestHeader(context);
                return  linkaiRequestHeader.getRequestHeaders();
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(context);
        requestQueue.add(request);
    }


    private void guessCountry(){
        try {
            for (Map.Entry<String, String> e : map.entrySet()) {
                Log.d("get mapvalue", e.getValue() +"guess"+PhoneNumberUtil.getInstance().getCountryCodeForRegion(guesscountryISOCode.toUpperCase()));

                if (e.getValue().trim().equals((""+PhoneNumberUtil.getInstance().getCountryCodeForRegion(guesscountryISOCode.toUpperCase())).trim()))
                {
                    Log.d("gussed region", e.getKey());
                    autoCompleteTextViewCountry.setText(e.getKey());
                    getCurrencies();
                    //add to my result list
                }
            }
        }
        catch (Exception ex){
            Log.d("Error in Guess Country", ""+ex.getMessage());
        }
    }

    private  void loadCountries(){

        final ArrayList<String> countries = new ArrayList<String>();
        final ArrayList<String> codes = new ArrayList<String>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,android.R.layout.simple_spinner_dropdown_item, countries);
        autoCompleteTextViewCountry.setAdapter(adapter);
        autoCompleteTextViewCountry.setThreshold(1);

        AsyncTask asyncTask=new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                Set<String> set = PhoneNumberUtil.getInstance().getSupportedRegions();

                String[] arr = set.toArray(new String[set.size()]);

                //add at l
                map=new HashMap<String,String>();


                for (int i = 0; i < set.size(); i++){
                    Locale locale = new Locale("en", arr[i]);
                    countries.add(locale.getDisplayCountry()+"(+"+PhoneNumberUtil.getInstance().getCountryCodeForRegion(arr[i].trim())+")");
                    codes.add("" + PhoneNumberUtil.getInstance().getCountryCodeForRegion(arr[i].trim()));
                    //Log.d("Countrycode ISO", "LoadCountry: " + arr[i].trim());
                    map.put(locale.getDisplayCountry()+"(+"+PhoneNumberUtil.getInstance().getCountryCodeForRegion(arr[i].trim())+")",""+PhoneNumberUtil.getInstance().getCountryCodeForRegion(arr[i].trim()));

                }
                Collections.sort(countries, String.CASE_INSENSITIVE_ORDER);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                adapter.notifyDataSetChanged();
                guessCountry();
            }
        };
        asyncTask.execute();

    }

    private void getCurrencies(){

        if(!common.isNetworkAvailable()){
            AlertDialog alertBuilder=new AlertDialog.Builder(LoginActivity.this).create();
            alertBuilder.setTitle(res.getString(R.string.login_alert_title_internet_notification));
            alertBuilder.setMessage(res.getString(R.string.login_alert_content_internet_notification));
            alertBuilder.setButton(AlertDialog.BUTTON_NEUTRAL, res.getString(R.string.alert_btn_ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertBuilder.show();
            return;
        }
        PhoneNumberUtil phoneNumberUtil=PhoneNumberUtil.getInstance();
        _countryCode=map.get(autoCompleteTextViewCountry.getText().toString());
        if(_countryCode==null || _countryCode.equals("")){
            return;
        }
        _countryCode_alpha=phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(_countryCode.replace("+","")));
        Log.d(TAG, "getCurrencies: "+_countryCode_alpha);

        String url=Const.LINKAI_GET_CURRENCY_CODES.replace("{value}",_countryCode_alpha);
        showProgress(true,"Loading currencies..");
        StringRequest request=new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                showProgress(false,null);
                Log.d(TAG, "onResponse: "+response.toString());
                try {
                    JSONArray jsonArray=new JSONArray(response);
                    Log.d(TAG, "onResponse: array "+jsonArray);
                    loadCurrencies(jsonArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                    loadCurrencies(null);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                showProgress(false,null);
                loadCurrencies(null);
                String errorbody="";
                try {
                    errorbody=new String(error.networkResponse.data,"UTF-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "onErrorResponse: error body-"+errorbody);
                error.printStackTrace();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                LinkaiRequestHeader requestHeader=new LinkaiRequestHeader(context);
                return requestHeader.getRequestHeaders();
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(10),
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue=Volley.newRequestQueue(context);
        requestQueue.add(request);
    }

    private void loadCurrencies(JSONArray jsonArray){
        try {
            Log.d(TAG, "loadCurrencies: "+jsonArray);
            int len;
            String[] currencies;
            if(jsonArray==null){
                jsonArray=new JSONArray();
                currencies= new String[1];
                currencies[0]="SELECT";
            }
            else{
                currencies= new String[jsonArray.length()];
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                currencies[i] = jsonArray.getString(i);
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, R.layout.app_spinner, currencies);
            arrayAdapter.setDropDownViewResource(R.layout.app_spinner);
//                    arrayAdapter.addAll(currencies);
            spinnerCurrency.setAdapter(arrayAdapter);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

