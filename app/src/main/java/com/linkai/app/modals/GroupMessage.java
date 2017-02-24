package com.linkai.app.modals;

import com.linkai.app.libraries.Const;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Created by LP1001 on 27-07-2016.
 */
public class GroupMessage extends Message{

//    primary variables
    private String packet_id= UUID.randomUUID().toString();

//    default constructor
    public GroupMessage(){
        //        setting chatboxtype to group
        this.chatbox_type= Const.CHATBOX_TYPE.GROUP.toString();
    }

// setter methods

    public void setGroupId(String _grp_id){
        this.msg_to=_grp_id;
    }

    public void setPacketId(String _packet_id){
        this.packet_id=_packet_id;
    }


//    public getter methods

    public String getGroupId(){ return this.msg_to; }

    public String getPacketId(){ return this.packet_id; }


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
            this.packet_id=jsonObject.getString("packet_id");
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
            jsonObject.put("packet_id",this.packet_id);
            jsonObject.put("msg_generate_time",this.msg_date);
            ret=jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            ret="";
        }
        return ret;
    }



}
