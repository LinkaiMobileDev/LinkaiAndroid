package com.linkai.app.modals;

import com.linkai.app.libraries.Const;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by LP1001 on 29-06-2016.
 */
public class ChatMessage extends Message {
    public final static String TYPE_IMAGE="image";
    public final static String TYPE_VIDEO="video";
    public final static String TYPE_AUDIO="audio";
    public final static String TYPE_TEXT="text";
    public final static String TYPE_OTHERS="others";
    public final static String TYPE_NOTIFICATION="notification";
    public final static String TYPE_TRANSFER="transfer";

    public final static int STATUS_UNSENT=0;
    public final static int STATUS_PENDING=1;
    public final static int STATUS_SENT=2;
    public final static int STATUS_DELIVERED=3;
    public final static int STATUS_READ=4;
    public final static int STATUS_READ_ACK=5;

    private String xmpp_id;
    private String msg_receipt_id="";


    public ChatMessage(){
//        setting chatboxtype to single
        this.chatbox_type= Const.CHATBOX_TYPE.SINGLE.toString();
    }

//public set methods


    public void setXmppId(String _xmppid){
        this.xmpp_id=_xmppid;
    }

    public void setReceiptId(String _rec_id){
        this.msg_receipt_id=_rec_id;
    }



//    public get methods

    public String getXMPPID(){ return this.xmpp_id; }

    public String getReceiptID(){ return this.msg_receipt_id; }


//    other methods
//    to set member variables with the json string from xmpp message body part
    public boolean setWithJson(String JsonString){
        boolean ret;

        try {
            JSONObject jsonObject=new JSONObject(JsonString);
            this.msg_type=jsonObject.getString("msg_type");
            this.msg_body=jsonObject.getString("msg_body");
            this.msg_file_name=jsonObject.getString("file_name");
            this.msg_file_thumb=jsonObject.getString("file_thumb");
            ret=true;
            //Log.d("chatMessage", "setWithJson: received json"+JsonString);
        } catch (JSONException e) {
            e.printStackTrace();
            ret=false;

        }
        return  ret;
    }

//    to generate json
    public String jsonifyMessage(){
        String ret="";
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("msg_type",this.msg_type);
            jsonObject.put("msg_body",this.msg_body);
            jsonObject.put("file_name",this.msg_file_name);
            jsonObject.put("file_thumb",this.msg_file_thumb);
            jsonObject.put("msg_generate_time",this.msg_date);
            jsonObject.put("sender_phone",Const.DB.getUser().getPhone());
            ret=jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            ret="";
        }
        return ret;
    }

}
