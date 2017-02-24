package com.linkai.app.modals;

/**
 * Created by LP1001 on 27-07-2016.
 */
public class GroupMember {
    private final String TAG="GroupMember";

//    primary variables
    private int grp_mem_id;
    private String jabber_id;
    private String grp_id;
    private String grp_mem_phone;
    private String grp_mem_name;
//secondary variables
    public ChatFriend FriendObject=new ChatFriend();

//    default constructor
    public GroupMember(){

    }

//    setter methods
    public void setGroupMemberId(int _grp_mem_id_id){
        this.grp_mem_id=_grp_mem_id_id;
    }

    public void setJabberId(String _jabber_id){
        this.jabber_id=_jabber_id;
    }

    public void setGroupId(String _grp_id){
        this.grp_id=_grp_id;
    }

    public void setPhone(String _phone){
        this.grp_mem_phone=_phone;
    }

    public void setName(String _name){ this.grp_mem_name=_name;}

//    getter methods
    public int getGroupMemberId(){
        return  this.grp_mem_id;
    }

    public String getJabberId(){
        return this.jabber_id;
    }

    public String getGroupId(){
        return this.grp_id;
    }

    public String getPhone(){
        return this.grp_mem_phone;
    }

    public String getName(){ return  this.grp_mem_name; }
//other methods

//    method to override equals , only check with member phone number
    @Override
    public boolean equals(Object  member){
        //Log.d(TAG, "equals: "+this.grp_mem_phone+"=="+((GroupMember) member).grp_mem_phone);
        if (member == null) return false;
        if (member == this) return true;
        if (!(member instanceof GroupMember))return false;
        return this.grp_mem_phone.equals(((GroupMember) member).grp_mem_phone);
    }

}
