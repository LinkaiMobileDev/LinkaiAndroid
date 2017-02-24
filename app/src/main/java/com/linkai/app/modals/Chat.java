package com.linkai.app.modals;

/**
 * Created by LP1001 on 22-08-2016.
 */
//class to represent both single and group messages
public class Chat {

    private String chatId;
    private String chatName;
    private String profileThumb="";
    private int unread_msg_count=0;
    private String chatbox_type;
    private int event_type;// to hold event types. only used for groupchat/event

    public Message LastMessage=new Message();

//    constructor
    public Chat(){}

//    setter method
    public void setChatId(String _id){
        this.chatId=_id;
    }

    public void setChatName(String _name){
        this.chatName=_name;
    }

    public void setProfileThumb(String _profileThumb){
        this.profileThumb=_profileThumb;
    }

    public void setUnreadMessageCount(int _count){
        this.unread_msg_count=_count;
    }

    public void setChatboxType(String _chatboxType){
        this.chatbox_type=_chatboxType;
    }

    public void setEventType(int type){
        this.event_type=type;
    }

//    getter methods
    public String getChatId(){
        return this.chatId;
    }

    public String getChatName(){
        return this.chatName;
    }

    public String getProfileThumb(){
        return this.profileThumb;
    }

    public int getUnreadMessageCount(){
        return this.unread_msg_count;
    }

    public String getChatboxType(){
        return this.chatbox_type;
    }

    public int getEventType(){
        return this.event_type;
    }
}
