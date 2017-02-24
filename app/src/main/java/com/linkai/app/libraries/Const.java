package com.linkai.app.libraries;

import android.content.Context;

import com.linkai.app.R;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by LP1001 on 05-08-2016.
 */
//class to hold all global constants and enums
public class Const {

//    public db object
    public static DatabaseHandler DB;
//    setting static variable for application context
    public static Context CONTEXT;
//    boolean object to represent wheather the user is online or not
    public static boolean IS_ONLINE=true;
//    enum to refer currently running activity
    public static APP_COMPONENTS CUR_ACTIVITY=null;
//    enum to refer currently running fragment
    public static APP_COMPONENTS CUR_FRAGMENT=null;
//    enum to refer currently interacting member's chatID
    public static String CUR_CHATID="";
//    enum to refer currently interacting group's id
    public static  String CUR_GROUPID="";

//    urls
//    public static String BASE_URL_ROOT="http://192.168.1.100:8080/LinkaiWeb/";
    public static String BASE_URL_ROOT="http://52.57.80.175:8080/LinkaiWeb/";
//    public static String BASE_URL="http://chat.lp-its.com/";
    public static String BASE_URL=BASE_URL_ROOT+"linkai/";
//    public static String SECOND_URL="http://lp-its.com/chat/";
//    public static String SECOND_URL="http://59.90.10.142/LpChat/";
    public static String SECOND_URL="http://52.57.80.175/";
//    public static String LINKAI_BASE_URL="https://hc-stub.herokuapp.com/";
//    public static String LINKAI_BASE_URL="http://ec2-52-57-225-152.eu-central-1.compute.amazonaws.com:8071/";
    public static String LINKAI_BASE_URL="http://35.156.215.85:8071/";

//    public static String UPLOAD_FILE_URL=SECOND_URL+"upload_file.php";
    public static String UPLOAD_FILE_URL=BASE_URL_ROOT+"UploadFileServlet";
    public static String FILE_DOWNLOAD_URL=BASE_URL_ROOT+"appfiles/chatfiles/";

    public static String POST_PROFILE_URL=BASE_URL+"linkaiuser/registeruser";
    public static String SEARCH_FRIENDS_URL=BASE_URL+"linkaiuser/searchfriends";
    public static String GET_PROFILE_URL=BASE_URL+"linkaiuser/getlatestprofileupdates";
    public static String UPLOAD_PROFILE_IMAGE_URL=BASE_URL_ROOT+"UploadImageServlet";

    public static String GROUP_CREATE_URL=BASE_URL+"linkaigroup/registergroup";
    public static String GROUP_GET_URL=BASE_URL+"linkaiuser/getjoinedgroups";
    public static String GROUP_ADD_MEMBER_URL=BASE_URL+"linkaigroup/registerusertogroup";
    public static String GROUP_DELETE_MEMBER_URL=BASE_URL+"linkaigroup/removeuserfromgroup";

    public static  String LINKAI_SIGNUP_URL=LINKAI_BASE_URL+"hawala/api/users/signup";
    public static  String LINKAI_GET_CURRENCY_CODES=LINKAI_BASE_URL+"hawala/api/currencies/{value}/codes";
    public static  String LINKAI_SIGNIN_URL=LINKAI_BASE_URL+"hawala/api/users/authenticate";
    public static  String LINKAI_VARIFY_CODE_URL=LINKAI_BASE_URL+"hawala/api/users/access-code/verify";
    public static  String LINKAI_GET_ACCESS_CODE_URL=LINKAI_BASE_URL+"hawala/api/users/access-code/resend";
    public static  String LINKAI_SET_PIN=LINKAI_BASE_URL+"hawala/api/users/profile/pin/set";
    public static  String LINKAI_TRANSFER_REQUEST=LINKAI_BASE_URL+"hawala/api/transfer/mobile/send-money";
    public static  String LINKAI_TRANSFER_ACCEPTANCE_URL=LINKAI_BASE_URL+"hawala/api/transfer/mobile/execute";
    public static  String LINKAI_GET_BALANCE_URL=LINKAI_BASE_URL+"hawala/api/accounts/{value}/balance";

//    request codes
    public static int REQ_ADD_CONTACT=1001;

//    filename of shared preference file
    public final static String LINKAI_SHAREDPREFERENCE_FILE="linkaiSPfile";

//    list to keep friends whose profile needs
    public static HashSet<String> FRIENDSLIST_TO_UPDATE_PROFILE=new HashSet<>();
    public static HashSet<String> GROUPSLIST_TO_UPDATE_PROFILE=new HashSet<>();

//    list of friends who are online
    public static HashSet<String> FRIENDSLIST_ONLINE=new HashSet<>();
//    list of friends who are away
    public static HashSet<String> FRIENDSLIST_AWAY=new HashSet<>();


//    enum to hold table names
    public enum DB_TABLE{

        TBL_USER("tbl_user"),
        TBL_FRIENDS("tbl_friends_list"),
        TBL_MESSAGES("tbl_messages"),
        TBL_GROUPS("tbl_group_master"),
        TBL_GROUP_MEMBERS("tbl_group_members"),
        TBL_GROUP_MESSAGES("tbl_group_messages");
        private String tbl_name;
        DB_TABLE(String name){this.tbl_name=name;}

        public String toString(){return this.tbl_name;}
    }
//    enum for chatbox type
    public enum CHATBOX_TYPE {
        SINGLE("single"),
        GROUP("group");
        private String value;

        CHATBOX_TYPE(String val){
            this.value=val;
        }

        public String toString() {
            return this.value;
        }
    }
//enum for sub types of notify message with its values
    public enum NOTIFY_MESSAGE_TYPE{
        REMOVE_MEMBER("remove_member"),
        ADD_MEMBER("add_member"),
        LEFT_GROUP("left_group");

        private String value;

    NOTIFY_MESSAGE_TYPE(String val){
            this.value=val;
        }
        public String toString() {
            return this.value;
        }
    }

//    enum for message show status
    public enum MESSAGE_SHOW_STATUS{
        SHOW("Y"),HIDE("N");
        private String value;
        MESSAGE_SHOW_STATUS(String val){
            this.value=val;
        }
        public String toString() {
            return this.value;
        }
    }

    //enum for group status
    public enum GROUP_STATUS{
        INACTIVE(0),
        ACTIVE(1);

        private int value;

        GROUP_STATUS(int val){
            this.value=val;
        }

        public int getValue() {
            return this.value;
        }
    }

    //enum for group type //chat or event
    public enum GROUP_TYPE{
        CHAT(0),
        EVENT(1);

        private int value;
        GROUP_TYPE(int val){this.value=val;}
        public int getValue() {
            return this.value;
        }
    }

//    enum for event types
    public enum EVENT_TYPE{
        OTHERS(R.string.event_others,0, 0,0,0),
        BIRTHDAY(R.string.event_birthday,1, R.drawable.birthday_icon,R.drawable.birthday_icon_100,R.drawable.birthday_icon_200),
        WEDDING(R.string.event_wedding,2, R.drawable.wedding_icon,R.drawable.wedding_icon_100,R.drawable.wedding_icon_200),
        SELFA(R.string.event_selfa,3, R.drawable.selfa,R.drawable.selfa_100,R.drawable.selfa_200);

        int string_id;
        int value;
        int res_id;
        int res_id_100;
        int res_id_200;
        private static Map<Integer,EVENT_TYPE> event_type_map=new HashMap<Integer,EVENT_TYPE>();

        EVENT_TYPE(int _string_id,int val,int r_id,int r_id_100,int r_id_200){
            this.string_id=_string_id;
            this.value=val;
            this.res_id=r_id;
            this.res_id_100=r_id_100;
            this.res_id_200=r_id_200;

        }

        static {
            for(EVENT_TYPE event_type:EVENT_TYPE.values()){
                event_type_map.put(event_type.getValue(),event_type);
            }
        }

        public static EVENT_TYPE getEventType(int val){
            return event_type_map.get(val);
        }

        public int getValue(){
            return this.value;
        }

        public String toString(){
            return CONTEXT.getResources().getString(this.string_id);
        }

        public int getImageResourceId(int pixels){
            if(pixels==100){
                return this.res_id_100;
            }
            else if(pixels==200){
                return this.res_id_200;
            }
            else{
                return this.res_id;
            }

        }


        public static int[] toValueArray(){
            return new int[]{BIRTHDAY.getValue(),WEDDING.getValue(),SELFA.getValue(),OTHERS.getValue()};
        }

        public static String[] toNameArray(){
            return new String[]{BIRTHDAY.toString(),WEDDING.toString(),SELFA.toString(),OTHERS.toString()};
        }

        public static Integer[] toResourceArray(int pixels){
            return new Integer[]{BIRTHDAY.getImageResourceId(pixels),WEDDING.getImageResourceId(pixels),SELFA.getImageResourceId(pixels),OTHERS.getImageResourceId(pixels)};
        }

    }


//    enum for user status
    public enum USER_STATUS{
        NOT_VARIFIED(0),
        VARIFIED(1);

        private int value;

        USER_STATUS(int val){
            this.value=val;
        }

        public int getValue() {
            return this.value;
        }
    }

//    enum for attachment type
    public enum ATTACHMENT_TYPE{
        IMAGE(1,"image"),
        VIDEO(2,"video"),
        AUDIO(3,"audio"),
        CAMERA(4,"image");

        private int int_val;
        private String str_val;

        ATTACHMENT_TYPE(int _int_val,String _str_val){
            this.int_val=_int_val;
            this.str_val=_str_val;
        }

        public String toString(){ return this.str_val; }

        public int toInteger(){ return this.int_val; }
    }

//    enum to determine friend's subscription status
    public enum FRIEND_SUBSCRIPTION_STATUS{
        UNSUBSCRIBED(0),
        SUBSCRIBED(1),
        ALL(-1);

        private int value;

        FRIEND_SUBSCRIPTION_STATUS(int val){
            this.value=val;
        }

        public int getValue() {
            return this.value;
        }
    }

//    enum to determine transfer direction
    public enum TRANSFER_DIRECTION{
        SENT(0),RECEIVE(1);
        private int value;
        TRANSFER_DIRECTION(int val){ this.value=val;}
        public int getValue(){return this.value;}
    }

//    enum to determine linkai transfer status
    public enum TRANSFER_STATUS{
        PENDING(0),ACCEPTED(1),REJECTED(2),EXPIRED(3);
        private int value;
        TRANSFER_STATUS(int val){ this.value=val;}
        public int getValue(){return this.value;}
    }

//    enum for app components(activities,fragments etc)
    public enum APP_COMPONENTS{
        AppAgreement(1),
        GroupChatBoxActivity(2),
        GroupProfileActivity(3),
        HomeActivity(4),
        LinkaiAmountEntryActivity(5),
        LinkaiContactsActivity(6),
        LinkaiCreateEventStep1Activity(7),
        LinkaiCreateEventStep2Activity(8),
        LinkaiCreateEventStep3Activity(9),
        LinkaiFeesPaidByActivity(10),
        LinkaiGenerateLinkodActivity(11),
        LinkaiMyLinkodsActivity(12),
        LinkaiPinEntryActivity(13),
        LinkaiRedeemActivity(14),
        LinkaiSetPinActivity(15),
        LinkaiTransferConfirmationActivity(16),
        LinkaiTransferTimeActivity(17),
        LoginActivity(18),
        MobileVarificationActivity(19),
        SingleChatBoxActivity(20),
        UserProfileActivity(21),
        AttachmentViewFragment(22),
        ChatFragment(23),
        ContactsFragment(24),
        MyLinkaiFragment(25);

        private int code;
        APP_COMPONENTS(int _code){
            code=_code;
        }
    }
}
