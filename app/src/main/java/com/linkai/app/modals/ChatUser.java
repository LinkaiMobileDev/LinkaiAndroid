package com.linkai.app.modals;

/**
 * Created by LP1001 on 30-06-2016.
 */
public class ChatUser {
    private final String TAG="ChatUser";


    private String jabber_id="";
    private String phone="";
    private String name="";
    private  String password="";
    private String linkai_password="";
    private String linkai_account="";
    private int status=0;
    private String email="";
    private String address="";

    public ChatUser(){

    }
//primary getter setter methods
    public void setJabberId(String _jabber_id){
        this.jabber_id=_jabber_id;
    }

    public void setPhone(String _phone){
        this.phone=_phone;
        if(this.phone!=null){
            this.phone=this.phone.replace("-","").replace(" ","");
        }
    }

    public void setName(String _name){
        this.name=_name;
    }

    public void setPassword(String _password){
        this.password=_password;
        if(this.password!=null){
            this.password=this.password.replace("-","").replace(" ","");
        }
    }

    public void setLinkaiPassword(String _password){
        this.linkai_password=_password;
    }

    public void setLinkaiAccount(String _account){
        this.linkai_account=_account;
    }

    public void setStatus(int _status){
        this.status=_status;
    }

    public void setEmail(String _email){
        this.email=_email;
    }

    public void setAddress(String _addr){
        this.address=_addr;
    }


    public String getJabberId(){
        return this.jabber_id;
    }

    public String getPhone(){
        return this.phone;
    }

    public String getName(){
        return this.name;
    }

    public String getPassword(){
        return this.password;
    }

    public String getLinkaiPassword(){
        return this.linkai_password;
    }

    public String getLinakiAccount(){return this.linkai_account;}

    public int getStatus(){ return this.status; }

    public String getEmail() {
        return this.email;
    }

    public String getAddress() {
        return this.address;
    }
}
