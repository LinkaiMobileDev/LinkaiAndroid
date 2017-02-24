package com.linkai.app.modals;

import com.linkai.app.libraries.Const;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by LP1001 on 22-08-2016.
 */
public class Message {
    private final String TAG="MessageObj";

//    primary variables
    protected int msg_id;
    protected String msg_from;
    protected String msg_to;
    protected String msg_body;
    protected Date msg_date= new Date();
    protected int msg_status=0;// 0-not sent , 1-sent, 2-delivered, 3-read
    protected String msg_type=ChatMessage.TYPE_TEXT;
    protected String msg_file_name="";
    protected String msg_file_thumb="";
    protected int msg_file_status=0;
    protected String msg_show_status;

//    constructor
    public Message(){
        this.msg_show_status=Const.MESSAGE_SHOW_STATUS.SHOW.toString();
    }

//    secondary variables
    protected String chatbox_type;

    //public set methods
    public void setId(int _id){
        this.msg_id=_id;
    }

    public void setFrom(String _from){
        this.msg_from=_from;
    }

    public void setTo(String _to){
        this.msg_to=_to;
    }

    public void setBody(String _body){
        this.msg_body=_body;
    }

    public void setDate(String _date){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            this.msg_date= dateFormat.parse(_date);
        } catch (Exception e) {
            // e.printStackTrace();
            this.msg_date=new Date();
        }
    }

    public void setStatus(int _status){
        this.msg_status=_status;
    }

    public void setType(String _msg_type){
        this.msg_type=_msg_type;
    }

    public void setFileName(String _msg_file_name){
        this.msg_file_name=_msg_file_name;
    }

    public  void setThumb(String _msg_file_thumb){
        this.msg_file_thumb=_msg_file_thumb;
    }

    public void setFileStatus(int _msg_file_status){
        this.msg_file_status=_msg_file_status;
    }

    public void setShowStatus(Const.MESSAGE_SHOW_STATUS message_show_status){
        this.msg_show_status=message_show_status.toString();
    }

    public void setShowStatus(String message_show_status){
        this.msg_show_status=message_show_status;
    }

    //    public get methods
    public int getID(){ return this.msg_id; }

    public String getFrom(){ return this.msg_from; }

    public String getTo(){ return this.msg_to; }

    public String getBody(){ return this.msg_body; }

    public String getDate(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(this.msg_date);
    }

    public Date getUTCDate(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String utcDateString=dateFormat.format(this.msg_date);
        Date utcDate;
        try {
            utcDate=dateFormat.parse(utcDateString);
        } catch (ParseException e) {
            utcDate=new Date(this.msg_date.getTime()-(24*60*60*1000));
            e.printStackTrace();
        }
        return utcDate;
//        return dateFormat.format(this.grp_msg_date);
    }

    public int getStatus(){ return this.msg_status; }

    public String getType(){
        return this.msg_type;
    }

    public String getFileName(){
        return this.msg_file_name;
    }

    public  String getThumb(){
        return this.msg_file_thumb;
    }

    public int getFileStatus(){
        return this.msg_file_status;
    }
    public String getShowStatus(){
        return this.msg_show_status;
    }


}
