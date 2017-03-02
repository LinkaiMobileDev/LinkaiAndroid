package com.linkai.app.libraries;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.linkai.app.Utils.LinkaiUtils;
import com.linkai.app.libraries.xmpp.ReadReceipt;
import com.linkai.app.modals.ChatFriend;
import com.linkai.app.modals.ChatGroup;
import com.linkai.app.modals.ChatMessage;
import com.linkai.app.modals.ChatUser;
import com.linkai.app.modals.GroupMember;
import com.linkai.app.modals.GroupMessage;
import com.linkai.app.modals.Transfer;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.ChatStateManager;
import org.jivesoftware.smackx.delay.provider.DelayInformationProvider;
import org.jivesoftware.smackx.disco.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.disco.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.iqlast.LastActivityManager;
import org.jivesoftware.smackx.iqlast.packet.LastActivity;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;


public class MyXMPP {
    String TAG="myxmpp";

    private static Context context;
    Common common;
    DatabaseHandler db;
    private LocalBroadcastManager localBroadcastManager;
//    private static final String DOMAIN = "hpfreddy";
//    private static final String HOST = "192.168.1.100";
    private static final String DOMAIN = "1.1.1.1";
    private static final String HOST = "52.57.80.175";
    private static final int PORT = 5222;
    private String chatId ="";
    private String passWord = "";
    private String chatName="";


    public static AbstractXMPPConnection connection ;
    ChatManager chatmanager ;
    ChatStateManager chatStateManager;
//    FileTransferManager fileTransferManager;
    PingManager pingManager;
    ReconnectionManager reconnectionManager;
    ProviderManager providerManager;
    DeliveryReceiptManager deliveryReceiptManager;
    Chat newChat;
    Roster roster;
    XMPPConnectionListener connectionListener = new XMPPConnectionListener();
    GroupMessageListener groupMessageListener=new GroupMessageListener();
    GroupInvitationListener groupInvitationListener=new GroupInvitationListener();
    //GroupUserStatusListener groupUserStatusListener=new GroupUserStatusListener();
    //GroupParticipantStatusListener groupParticipantStatusListener=new GroupParticipantStatusListener();
    MultiUserChatManager multiUserChatManager;

    public static boolean IS_CONNECTED;
    private boolean isToasted;
    private boolean chat_created;
    public static boolean IS_LOGGEDIN;

//    constructor
    public MyXMPP(Context _context){
        this.context=_context;
        common=new Common(context);
        db=Const.DB;
        localBroadcastManager=LocalBroadcastManager.getInstance(context);
        ChatUser user=db.getUser();
        if(user!=null) {
            this.chatId = user.getJabberId();
            this.passWord = user.getPassword();
            this.chatName = user.getName();
        }
        //Log.d("User details", "MyXMPP: "+ this.chatId);
    }



    //Initialize
    public void init() {
//        set packet reply timeout
        SmackConfiguration.setDefaultPacketReplyTimeout(10000);

        XMPPTCPConnectionConfiguration  config = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword(chatId, passWord)
                .setServiceName(DOMAIN)
                .setHost(HOST)
                .setPort(PORT)

                .setDebuggerEnabled(true)
                //.setSendPresence(false)
//                .setSocketFactory(SSLSocketFactory.getDefault())
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .build();
        connection = new XMPPTCPConnection(config);
        connection.addConnectionListener(connectionListener);

        chatStateManager=ChatStateManager.getInstance(connection);
        chatmanager=ChatManager.getInstanceFor(connection);
        multiUserChatManager=MultiUserChatManager.getInstanceFor(connection);
        providerManager=new ProviderManager();
        configureProviderManager();

        //receiving chat
        chatmanager.addChatListener(new ChatManagerListener() {
            @Override
            public void chatCreated(Chat chat, boolean createdLocally) {
                chat.addMessageListener(new ChatMessageListener() {
                    @Override
                    public void processMessage(Chat chat, Message message) {
                        boolean add_message=true;
                        long id=-1;
                        String m_from=message.getFrom().substring(0,message.getFrom().indexOf('@'));
                        Log.d(TAG, "processMessage: "+m_from);
                        if(message.getBody()!=null) {
//                            if message received
                            if (!(message.getBody().trim().equals(""))) {
                                if(db.getFriendByJabberId(m_from)==null && !m_from.contains("admin")){
//                                 if unknown add to friendlist wit phone as name
                                    try {
                                        ChatFriend unknownFriend=new ChatFriend();
                                        unknownFriend.setJabberId(m_from);
                                        String unKnownfriendPhone=new JSONObject(message.getBody()).getString("sender_phone");
                                        unknownFriend.setName(unKnownfriendPhone);
                                        unknownFriend.setPhone(unKnownfriendPhone);
                                        unknownFriend.setSubscriptionStatus(Const.FRIEND_SUBSCRIPTION_STATUS.SUBSCRIBED.getValue());
                                        db.addOrUpdateFriend(unknownFriend);
//                                        send subscription request
                                        sendSubscriptionRequest(m_from,unKnownfriendPhone);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }


                                ChatMessage msgObj = new ChatMessage();
                                msgObj.setXmppId(message.getThread());
                                msgObj.setFrom(m_from);
                                msgObj.setTo("self");
                                msgObj.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                                msgObj.setStatus(1);
                                msgObj.setReceiptId(message.getStanzaId());
//                                trying to set with json body,(parse body and set fields)
                                if(!msgObj.setWithJson(message.getBody())){
//                                    if failed inserting string without parsing
                                    msgObj.setBody(message.getBody());
                                }
//                                for transfer type message
                                if(msgObj.getType().equals(ChatMessage.TYPE_TRANSFER)){
                                    Log.d(TAG, "processMessage: in transfer");
                                    Transfer transfer=new Transfer();
//                                    trying to set transfer object with json message
                                    if(!transfer.setWithJson(msgObj.getBody())) {
                                        Log.d(TAG, "processMessage: set json error");
                                        return;
                                    }
//                                    checking if transfer type pending..ie. new transfer request
                                    if(transfer.getTransferStatus()==Const.TRANSFER_STATUS.PENDING.getValue()){
                                        transfer.setTransferDirection(Const.TRANSFER_DIRECTION.RECEIVE);
                                        if(db.addTransfer(transfer)<=0){
                                            add_message=false;
                                        }
                                    }
//                                    checking if  transfer type not pending..ie. request to update transfer status
                                    else{
//                                        transfer object to hold transfer details from db
                                        Transfer transfer_update=db.getTransfer(transfer.getTransferId());
                                        if(transfer_update!=null){
//                                            change status to new status
                                            transfer_update.setTransferStatus(transfer.getTransferStatus());
//                                            update transfer
                                            db.updateTransfer(transfer_update);
//                                            set message show status to hide
                                            msgObj.setShowStatus(Const.MESSAGE_SHOW_STATUS.HIDE);
//                                            call to update balance
                                            new LinkaiUtils(context).getBalance();
                                            add_message=true;
                                        }
                                        else{
                                            add_message=false;
                                        }

                                    }

                                }
//                                handling notification message
                                else if(msgObj.getType().equals(ChatMessage.TYPE_NOTIFICATION)){
                                    Log.d(TAG, "processMessage: notification0-"+msgObj.getBody());
                                    ChatGroup.updateOnNotification(msgObj.getBody());
                                    add_message=false;
                                }

                                if(add_message)id=db.addMessage(msgObj);
                                Log.d(TAG, "processMessage: id-"+id);
                                if(id!=-1) {
                                    localBroadcastManager.sendBroadcast(new Intent("chat.message.received").putExtra("id",""+id).putExtra("from",m_from));
                                }
                                else{
                                    localBroadcastManager.sendBroadcast(new Intent("chat.view.refresh"));
                                }
                            }

                        }
//                        checking if message is a read receipt
                        else if(message.hasExtension(ReadReceipt.NAMESPACE)){
                            ReadReceipt readReceipt= (ReadReceipt) message.getExtension(ReadReceipt.NAMESPACE);
                            Log.d(TAG, "processMessage: "+readReceipt.getReceiptId());
                            ChatMessage msg=db.getMessageByReceiptId(readReceipt.getReceiptId());
                            msg.setStatus(ChatMessage.STATUS_READ);
                            db.updateMessage(msg);
                            localBroadcastManager.sendBroadcast(new Intent("chat.view.refresh"));
                        }
                        else{
                            //                            if chat state received or message read status received
                            String msg_xml = message.toXML().toString();
                            if (msg_xml.contains(ChatState.composing.toString()))
                            {
                                //                sending broadcast with presence status
                                localBroadcastManager.sendBroadcast(new Intent("chat.chatstate.changed").putExtra("from", m_from).putExtra("status", true));

                            }
                            else if (msg_xml.contains(ChatState.paused.toString()))
                            {
                                //                sending broadcast with presence status
                                localBroadcastManager.sendBroadcast(new Intent("chat.chatstate.changed").putExtra("from", m_from).putExtra("status", false));

                            }


                        }

                    }
                });
            }
        });

//End chat listener
        roster=Roster.getInstanceFor(connection);
//        setting subscription mode to accept all
        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.accept_all);
        roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
//        roster listener
        roster.addRosterListener(new RosterListener() {
            @Override

            public void entriesAdded(Collection<String> addresses) {
//                Log.d("Roster", "entriesAdded: ");
                for (String adrs:
                     addresses) {
                    ChatFriend friend=db.getFriendByJabberId(adrs.substring(0,adrs.indexOf('@')));
                    if(friend!=null && friend.getSubscriptionStatus()!=Const.FRIEND_SUBSCRIPTION_STATUS.SUBSCRIBED.getValue()) {
                        friend.setSubscriptionStatus(Const.FRIEND_SUBSCRIPTION_STATUS.SUBSCRIBED.getValue());
                        db.updateFriend(friend);
                    }

                }
//                getRoster();
//                common.callSyncContactsService();
            }

            @Override
            public void entriesUpdated(Collection<String> addresses) {
//                Log.d("Roster", "entriesUpdated: ");
//                getRoster();
            }

            @Override
            public void entriesDeleted(Collection<String> addresses) {
//                Log.d("Roster", "entriesDeleted: ");
//                getRoster();
            }

            @Override
            public void presenceChanged(Presence presence) {
                //Log.d("Roster", "presenceChanged: "+presence.getFrom()+"-"+presence.isAvailable()+"-"+presence.getMode());
                String m_from=presence.getFrom().substring(0,presence.getFrom().indexOf('@')).trim();
//                ChatFriend friend=db.getFriendByJabberId(m_from);
                boolean status;
//                checking if online
                if(presence.isAvailable() && (presence.getMode()== Presence.Mode.available || presence.getMode()==Presence.Mode.chat)) {
                    status=true;
                    Const.FRIENDSLIST_ONLINE.add(m_from);
//                    call send message service
                    common.callSendMessageService();
//                    Log.d("Roster", "presenceChanged: online- "+presence.getFrom()+"-"+presence.isAvailable()+"-"+presence.getMode());
                }
                else{
                    status=false;
                    Const.FRIENDSLIST_ONLINE.remove(m_from);
                }
//                Log.d(TAG, "presenceChanged1: size"+Const.FRIENDSLIST_ONLINE.size());
//                for (String frnd:Const.FRIENDSLIST_ONLINE
//                     ) {
//                    Log.d(TAG, "presenceChanged1: "+frnd);
//                }
//                sending broadcast with presence status
                localBroadcastManager.sendBroadcast(new Intent("chat.presence.changed").putExtra("from", m_from).putExtra("status", status));
            }
        });
//end roaster listener

//        Ping management
        pingManager=PingManager.getInstanceFor(connection);
        XmppPingFailedListener xmppPingFailedListener=new XmppPingFailedListener();
        pingManager.setPingInterval(60);
        pingManager.registerPingFailedListener(xmppPingFailedListener);

//reconncectionmanager
        reconnectionManager=ReconnectionManager.getInstanceFor(connection);
        reconnectionManager.enableAutomaticReconnection();
        reconnectionManager.setFixedDelay(5);

//        delivery receipt status management
        deliveryReceiptManager=DeliveryReceiptManager.getInstanceFor(connection);
        deliveryReceiptManager.addReceiptReceivedListener(new ReceiptReceivedListener() {
            @Override
            public void onReceiptReceived(String fromJid, String toJid, String receiptId, Stanza receipt) {
//                Log.d("myxmpp", "onReceiptReceived message delivery  recept : from: " + fromJid + " to: " + toJid + " deliveryReceiptId: " + receiptId + " stanza: " + receipt);
//                getting msg object
//                sleep 1 sec b4 updating
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ChatMessage msg=db.getMessageByReceiptId(receiptId.trim());
//                set msg status to 2
                msg.setStatus(ChatMessage.STATUS_DELIVERED);

//                update message in db
                db.updateMessage(msg);
                //Log.d("myxmpp", "onReceiptReceived: messsage details "+msg.getTo()+" "+msg.getStatus()+" "+msg.getID()+" "+msg.getReceiptID()+ "="+receiptId);
                localBroadcastManager.sendBroadcast(new Intent("chat.view.refresh"));
            }
        });

//        add group invitation listener
        multiUserChatManager.addInvitationListener(groupInvitationListener);
    }

    // Disconnect Function
    public void disconnectConnection(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                connection.disconnect();
            }
        }).start();
    }

    public void connectConnection()
    {
        Log.d(TAG, "connectConnection: 1");
        AsyncTask<Void, Void, Boolean> connectionThread = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... arg0) {

                // Create a connection
//                //Log.d("connectconnection", "doInBackground: b4");
                try {
//                    //Log.d("connectconnection", "doInBackground: in try");
                    connection.connect();
//                    //Log.d("connectconnection", "doInBackground: b4login-"+connection.isConnected());
                    chatmanager=ChatManager.getInstanceFor(connection);
                    login();
                    IS_CONNECTED = connection.isConnected();
//                  search new friends accounts  after login
                    //searchFriendsAccount();
//                    //Log.d("connectconnection", "doInBackground: connected-"+connected);

                } catch (IOException e) {
                    Log.d("connectconnection", "doInBackground: error-io--"+e.getMessage());
//                    //Log.d("connectconnection", ""+e.toString()+"\n"+e.getLocalizedMessage());
                } catch (SmackException e) {
                    Log.d("connectconnection", "doInBackground: error-smack--"+e.getMessage());
                } catch (XMPPException e) {
                    Log.d("connectconnection", "doInBackground: errror-xmpp--"+e.getMessage());
                }

                return null;
            }
        };
        connectionThread.execute();

    }

//    To set our presence
    public void setPresence(boolean isOnline){
        Presence presence;
        if(isOnline) {
            presence = new Presence(Presence.Type.available, "online", 10, Presence.Mode.available);
            Const.IS_ONLINE=true;
        }
        else{
            presence = new Presence(Presence.Type.available, "away", 10, Presence.Mode.away);
            Const.IS_ONLINE=false;
        }
        try {
            connection.sendStanza(presence);
        } catch (SmackException.NotConnectedException e) {
            Const.IS_ONLINE=false;
            e.printStackTrace();
        }
    }


//    create chat
    public void createChat(String chatFriendId){
        //Log.d("myxmpp", "createChat: ");
        if(newChat!=null){
            newChat.close();
        }
//        check if jid is complete
        if(!chatFriendId.contains("@"+DOMAIN)){
            chatFriendId+="@"+DOMAIN;
        }
        //Log.d("myxmpp", "sendMsg: createChat"+chatFriendId);
        newChat = chatmanager.createChat(chatFriendId);
        //Log.d("myxmpp", "sendMsg: createChat "+newChat.getParticipant());
    }

// to send message
    public ChatMessage sendMsg(ChatMessage msgObj)  {
        //Log.d("sendmsg", "sendMsg: in");
        if (connection.isConnected()== true) {
//            check if chat is created correctly, else recreating
            if(newChat==null || !(newChat.getParticipant().equals(msgObj.getTo()+"@"+DOMAIN))){

                createChat(msgObj.getTo()+"@"+DOMAIN);

            }

            try {
//                generating json formatted string to send
                String formatted_message=msgObj.jsonifyMessage().trim();
                //Log.d("sendmsg", "sendMsg: messge-"+formatted_message);
                if(formatted_message.equals("")){
                    return msgObj;
                }
//                creating message obj
                Message message=new Message(newChat.getParticipant());
//                setting receipt id as stanza id
                message.setStanzaId(msgObj.getReceiptID());
//                add message body
                message.addBody(null,formatted_message);
//                add message delivery request
                String deliveryrequestId= DeliveryReceiptRequest.addTo(message);
//                Log.d("myxmpp", "sendMsg: message receipt id-"+deliveryrequestId);
//                sending message
                newChat.sendMessage(message);
//                reinitializing msg object with status 1 and new xmppid
//                Log.d("myxmpp", "sendMsg: message:  id-"+message.getStanzaId());
                msgObj.setXmppId(message.getStanzaId());
                msgObj.setStatus(ChatMessage.STATUS_SENT);
//                msgObj.setReceiptId(deliveryrequestId);
            } catch (SmackException.NotConnectedException e) {
                //Log.d("sendmsg", "sendMsg: error");
                e.printStackTrace();

            }
        }

        return msgObj;

    }

//    function to send read stanza test
    public boolean sendReadReceipt(ChatMessage chatMessage){
        Chat chat=chatmanager.createChat(chatMessage.getFrom()+"@"+DOMAIN);
        Message message=new Message(chat.getParticipant());
        ReadReceipt readReceipt=new ReadReceipt(chatMessage.getReceiptID());
        message.addExtension(readReceipt);
        try {
            chat.sendMessage(message);
            return true;
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            return false;
        }
    }


    public void login() {
        //Log.d("logging in", "login:in ");
        if(!connection.isConnected()){
            //Log.d("logging in", "login:not connected ");
            return;
        }
        try {
            connection.login(chatId, passWord);
//            getting roster after successfull loged in
//            getRoster();
            //call to start background services after successfull login
//            common.initServices();
            //Log.i("LOGIN", "Yey! We're connected to the Xmpp server!");
            //Log.d("logging in", "login:success ");

        } catch (XMPPException | SmackException | IOException e) {
            e.printStackTrace();
            //Log.d("logging in", "login:exeption-xmpp,smacc,io ");
            //Log.d("logging in", "login: "+e.getMessage());
        } catch (Exception e) {
            //Log.d("logging in", "login:exeption");
        }

    }


//    edited by hari
    public void getRoster(){
        //        getting roster
        //        accepting all requests
        //Log.d("Roster", "RosterCheck: ");
        if (!roster.isLoaded()) {
            try {
                //Log.d("Roster", "Reloading: ");
                roster.reloadAndWait();
            } catch (SmackException.NotLoggedInException e) {
                //Log.d("Roster", "RosterReloading:NotLoggedInException "+e.getMessage());
            } catch (SmackException.NotConnectedException e) {
                //Log.d("Roster", "RosterReloading:NotConnectedException "+e.getMessage());
            } catch (InterruptedException e) {
                //Log.d("Roster", "RosterReloading:InterruptedException "+e.getMessage());
            }
        }

        Collection<RosterEntry> entries = roster.getEntries();

//        //Log.d("Roster", "RosterEntry: " + rosterJson);
        for (RosterEntry entry : entries) {
            Log.d("Roster", "RosterEntries: " + entry.getUser()+"-"+entry.getName());
            ChatFriend friend=db.getFriendByJabberId(entry.getUser().substring(0,entry.getUser().indexOf('@')));
            if(friend!=null) {
                friend.setSubscriptionStatus(Const.FRIEND_SUBSCRIPTION_STATUS.SUBSCRIBED.getValue());
                db.updateFriend(friend);
            }
        }
//        send broadcast after getting roaster
        //Log.d("Roster", "Sending Broadcast: " );
        localBroadcastManager.sendBroadcast(new Intent("chat.roster.received"));

    }

    //    register in server
    public boolean register(ChatUser user){
        this.chatId = user.getJabberId();
        this.passWord = user.getPassword();
        String name=user.getName();
        XMPPTCPConnectionConfiguration  config = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword(chatId, passWord)
                .setServiceName(DOMAIN)
                .setHost(HOST)
                .setPort(PORT)

                .setDebuggerEnabled(true)
                //.setSendPresence(false)
//                .setSocketFactory(SSLSocketFactory.getDefault())
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .build();
        connection = new XMPPTCPConnection(config);

        try {
            connection.connect();
            if(!connection.isConnected()){
                //Log.d("Register", "login:not connected ");
                return false;
            }
            try {
                AccountManager accountManager=AccountManager.getInstance(connection);
                accountManager.createAccount(chatId,passWord);
//                try login  if account created successfully
                connection.login(chatId,chatId);
                //Log.i("LOGIN", "Yey! We're connected to the Xmpp server!");
                //Log.d("Register", "login:success ");
//                return true;
                return db.addUser(user);


            } catch (XMPPException | SmackException e ) {

                //Log.d("Register", "login:exeption-xmpp,smacc,io- trying to login ");
                //Log.d("Register", "login: "+e.getMessage());
//               try login if account creation failed, to check if accountis already created earlier
                connection.login(chatId, passWord);
//                return true;
                return db.addUser(user);
            } catch (Exception e) {
                //Log.d("Register", "login:exeption");
                return false;
            }
        } catch (SmackException e) {
            Log.d("Register", "login:smackexeption-"+e.getMessage());
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            Log.d("Register", "login:ioexeption");
            e.printStackTrace();
            return false;
        } catch (XMPPException e) {
            Log.d("Register", "login:xmppexeption");
            e.printStackTrace();
            return false;
        }

    }

//    to request subscription for all unsubscribed
    public void requestSubscription(){

        Log.d(TAG, "requestSubscription: ");
        ArrayList<ChatFriend> friends=db.getAllFriends(Const.FRIEND_SUBSCRIPTION_STATUS.SUBSCRIBED);
        for (ChatFriend friend :friends) {
            sendSubscriptionRequest(friend.getJabberId(),friend.getName());
        }
    }
//    to send subscription request to user
    public boolean sendSubscriptionRequest(String userId,String NickName){
        Log.d(TAG, "sendSubscriptionRequest: ");
        boolean ret=false;
        Presence subscribe = new Presence(Presence.Type.subscribe);
        userId+="@"+DOMAIN;
        //Log.d("Roster", "AddRoster: userid-"+userId);
        subscribe.setTo(userId);
        try {
//            connection.sendPacket(subscribe);
            connection.sendStanza(subscribe);
            Log.d(TAG, "sendSubscriptionRequest: sending-"+userId+" name-"+NickName);
            ret=true;
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            Log.d(TAG, "sendSubscriptionRequest: "+e.getMessage());
            ret=false;
        }
        return  ret;
    }

//    to check presence of a chatid(friend) ,is online
    public boolean isOnline(String chatFriendId){
        boolean status=false;
        Presence presence=roster.getPresence(chatFriendId+"@"+DOMAIN);
        Log.d("Roster", "presenceChanged ONLINE: "+presence.getMode()+"-"+presence.isAvailable());
        if(presence.isAvailable() && (presence.getMode()== Presence.Mode.available || presence.getMode()==Presence.Mode.chat)) {
            status=true;
                    Log.d("Roster", "presenceChanged ONLINE: online- "+presence.getFrom()+"-"+presence.isAvailable()+"-"+presence.getMode());
        }
        return  status;
    }

    //    to check presence of a chatid(friend) ,is online or away
    public boolean isOnlineOrAway(String chatFriendId){
        boolean status=false;
        Presence presence=roster.getPresence(chatFriendId+"@"+DOMAIN);
        Log.d("Roster", "presenceChanged AWAY: "+presence.getMode()+"-"+presence.isAvailable());
        if(presence.isAvailable() && (presence.getMode()== Presence.Mode.available || presence.getMode()==Presence.Mode.chat || presence.getMode()==Presence.Mode.away)) {
            status=true;
            Log.d("Roster", "presenceChanged AWAY: online- "+presence.getFrom()+"-"+presence.isAvailable()+"-"+presence.getMode());
        }
        return  status;
    }

//    to check the last seen
    public String getLastSeen(String chatFriendId){
        String lastSeen="";
        try {
            LastActivity lastActivity=LastActivityManager.getInstanceFor(connection).getLastActivity(chatFriendId+"@"+DOMAIN);
            Log.d(TAG, "getLastSeen: "+lastActivity);
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        return lastSeen;
    }

//    function to send composing/typing notification to a chat
    public void sendChatStateNotification(String chatFriendId,boolean is_typing){
        //            check if chat is created correctly, else recreating
        if(newChat==null || !(newChat.getParticipant().equals(chatFriendId+"@"+DOMAIN))){
            //Log.d("myxmpp", "sendMsg: createChat");
            createChat(chatFriendId);
        }
        ChatState state=is_typing?ChatState.composing:ChatState.paused;
        try {
            chatStateManager.setCurrentState(state,newChat);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    public void configureProviderManager() {
        providerManager.addIQProvider("query",

                "http://jabber.org/protocol/bytestreams",

                new BytestreamsProvider());



        providerManager.addIQProvider("query",

                "http://jabber.org/protocol/disco#items",

                new DiscoverItemsProvider());



        providerManager.addIQProvider("query",

                "http://jabber.org/protocol/disco#info",

                new DiscoverInfoProvider());
        providerManager.addExtensionProvider("x","jabber:x:delay",
                new DelayInformationProvider());
        providerManager.addExtensionProvider(ReadReceipt.ELEMENT,ReadReceipt.NAMESPACE,
               new ReadReceipt.ReadReceiptProvider());
    }

//    methods for groups
 public boolean createOrJoinGroup(ChatGroup group){
    String groupId=group.getGroupId();
//     //        join try group in both cases
     return joinGroup(groupId);

}

    public boolean joinGroup(String groupId){
        ChatGroup group=db.getGroup(groupId);
        if(group==null)return false;
        boolean ret=false;
        MultiUserChat muc=multiUserChatManager.getMultiUserChat(groupId+"@conference."+MyXMPP.DOMAIN);
        try {
//            getting last message
            GroupMessage lastMessage=db.getLastGroupMessage(groupId, DatabaseHandler.Message_Direction.BOTH);
            DiscussionHistory history=new DiscussionHistory();
            long grpStatusTime=group.getStatusDateUTC().getTime();
            long lastMessageTime=-1;
            if(lastMessage!=null){
                lastMessageTime=lastMessage.getUTCDate().getTime();
            }
            else{
            }
            int historyFromTime=(int) (Math.abs(new Date().getTime()-Math.max(grpStatusTime,lastMessageTime))/1000)+10;
            history.setSeconds(historyFromTime);

//            history.
            muc.join(chatId,null,history,SmackConfiguration.getDefaultPacketReplyTimeout());
            //        add message listener
            muc.addMessageListener(groupMessageListener);
//            add user status listener
           // muc.addUserStatusListener(groupUserStatusListener);
//            add participant listener
            //muc.addParticipantStatusListener(groupParticipantStatusListener);
            ret=true;
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public boolean leaveGroup(String groupId){
        boolean ret=false;
        MultiUserChat muc=multiUserChatManager.getMultiUserChat(groupId+"@conference."+MyXMPP.DOMAIN);
        try {
            muc.leave();
            ret=true;
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();

        }
        return ret;
    }

//    method to invire group members
    public boolean inviteGroupMembers(String groupId){

        MultiUserChat muc=multiUserChatManager.getMultiUserChat(groupId+"@conference."+MyXMPP.DOMAIN);
        ArrayList<GroupMember> members=db.getGroupMembers(groupId);
        for (GroupMember member:members) {
            try {
                muc.invite(member.getJabberId()+"@"+DOMAIN,"");
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
                return false;
            }
        }
        return  true;
    }
//    invite a single group member
    public boolean inviteGroupMember(GroupMember member){
        MultiUserChat muc=multiUserChatManager.getMultiUserChat(member.getGroupId()+"@conference."+MyXMPP.DOMAIN);
        try {
            muc.invite(member.getJabberId()+"@"+DOMAIN,"");
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

//    ban a user in muc
    public boolean banGroupMember(GroupMember member){
        MultiUserChat muc=multiUserChatManager.getMultiUserChat(member.getGroupId()+"@conference."+MyXMPP.DOMAIN);
        try {
            muc.banUser(member.getJabberId()+"@"+DOMAIN,"Removed");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


//    method to send message to group
    public GroupMessage sendGroupMessage(GroupMessage message){
// rejoining before sending message to avoid condition if user left unconditionally
        joinGroup(message.getGroupId());
//        creating muc instance
        MultiUserChat muc=multiUserChatManager.getMultiUserChat(message.getGroupId()+"@conference."+MyXMPP.DOMAIN);
        try {
//            Log.d(TAG, "sendGroupMessage: sending");
            String formatted_message=message.jsonifyMessage().trim();
//            sending message
            muc.sendMessage(formatted_message);
//            updating status
            message.setStatus(ChatMessage.STATUS_SENT);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
//            Log.d(TAG, "sendGroupMessage: NotConnectedException-"+e.getMessage());

        }catch (Exception e){
            e.printStackTrace();
//            Log.d(TAG, "sendGroupMessage: Exception-"+e.getMessage());
        }

        return message;
    }

    //    end methods for groups


//    inner classes
//Connection Listener to check connection state
    private class XMPPConnectionListener implements ConnectionListener {

    @Override
    public void connected(final XMPPConnection connection) {

        //Log.d("XMPPConnectionListener", "Connected!");
        IS_CONNECTED = true;
        if (!connection.isAuthenticated()) {

            login();
        }
        else{
            IS_LOGGEDIN=true;
//            call to start background services after connected
            common.callSendMessageService();
            common.callSyncGroupsService();
        }
    }

    @Override
    public void connectionClosed() {
        if (isToasted)

            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub


                }
            });
        Log.d("XMPPConnectionListener", "ConnectionCLosed!");
        IS_CONNECTED = false;
        chat_created = false;
        IS_LOGGEDIN = false;

    }

    @Override
    public void connectionClosedOnError(Exception arg0) {
        if (isToasted)

            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {

                }
            });
        Log.d("XMPPConnectionListener", "ConnectionClosedOn Error!");
        IS_CONNECTED = false;

        chat_created = false;
        IS_LOGGEDIN = false;
    }

    @Override
    public void reconnectingIn(int arg0) {

        //Log.d("XMPPConnectionListener", "Reconnectingin " + arg0);

        //IS_LOGGEDIN = false;
    }

    @Override
    public void reconnectionFailed(Exception arg0) {
        if (isToasted)

            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {



                }
            });
        //Log.d("XMPPConnectionListener", "ReconnectionFailed!");
        //IS_CONNECTED = false;

        chat_created = false;
        //IS_LOGGEDIN = false;
    }

    @Override
    public void reconnectionSuccessful() {
        if (isToasted)

            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub

                }
            });
        //Log.d("XMPPConnectionListener", "ReconnectionSuccessful");
        IS_CONNECTED = true;

        chat_created = false;
        if (!connection.isAuthenticated()) {

            login();
        }
        else{
            IS_LOGGEDIN=true;
            //call to start background services after reconnected
            common.callSendMessageService();
        }
    }

    @Override
    public void authenticated(XMPPConnection arg0, boolean arg1) {
        //Log.d("XMPPConnectionListener", "Authenticated!");
        IS_CONNECTED = true;
        IS_LOGGEDIN = true;
        requestSubscription();
        chat_created = false;
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    //call to start background services after authenticated
                    common.callSendMessageService();
                    common.callSyncGroupsService();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }).start();
        if (isToasted)

            new Handler(Looper.getMainLooper()).post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub



                }
            });
    }
}

    //    Ping failed listener
    private  class XmppPingFailedListener implements PingFailedListener {

        @Override
        public void pingFailed() {
//            reconnecting
            connectConnection();
        }
    }

//    listener for group chat or multi user chat
    private class GroupMessageListener implements MessageListener{

        @Override
        public void processMessage(Message message) {
            Log.d(TAG, "processMessage: from-"+message.getFrom()+"--body-"+message.getBody());
            boolean add=true;
            long msg_id=-1;
            String groupId=message.getFrom().substring(0,message.getFrom().indexOf('@'));
            String sender=message.getFrom().substring(message.getFrom().lastIndexOf('/')+1);
            if(message.getBody()!=null && !sender.equals(chatId)) {
                GroupMessage msgObj = new GroupMessage();
                msgObj.setGroupId(groupId);
                msgObj.setFrom(sender);
                msgObj.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                msgObj.setStatus(1);
                if(!msgObj.setWithJson(message.getBody())){
//                      if failed inserting string without parsing
                    msgObj.setBody(message.getBody());
                }
                if(msgObj.getType().equals(ChatMessage.TYPE_NOTIFICATION)){
//                    process notification message
                    try {
                        JSONObject notifyMsgBody=new JSONObject(msgObj.getBody());
                        GroupMember member=new GroupMember();
                        member.setJabberId(notifyMsgBody.getString("member_id"));
                        member.setPhone(notifyMsgBody.getString("member_phone"));
                        member.setName("");
                        member.setGroupId(groupId);

                        if(notifyMsgBody.getString("notify_type").equals(Const.NOTIFY_MESSAGE_TYPE.ADD_MEMBER.toString())){
                            Log.d(TAG, "processMessage: processing notification add-"+msgObj.getBody());
                           db.addGroupMember(member);
                            Log.d(TAG, "processMessage: add-"+add);
                        }
                        else if(notifyMsgBody.getString("notify_type").equals(Const.NOTIFY_MESSAGE_TYPE.REMOVE_MEMBER.toString())
                                || notifyMsgBody.getString("notify_type").equals(Const.NOTIFY_MESSAGE_TYPE.LEFT_GROUP.toString())){
                            Log.d(TAG, "processMessage: processing notification remove-"+msgObj.getBody());
                            add=db.removeGroupMember(member);
                        }
                        else{
                            add=false;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        add=false;
                        add=false;
                    }
                }
                if(add) {
                    msg_id = db.addGroupMessage(msgObj);
                }
                Log.d(TAG, "processMessage joinGroup msg: "+ChatMessage.TYPE_NOTIFICATION+"="+msgObj.getType()+" "+msg_id);
//                Log.d(TAG, "processMessage: ret-"+ret);

//               broadcasting notification
                if(msg_id!=-1 && msgObj.getType().equals(ChatMessage.TYPE_NOTIFICATION)) {
//                    Log.d(TAG, "processMessage: sending notification broadcast");
                    localBroadcastManager.sendBroadcast(new Intent("group.notification.received"));
                }
                else if(msg_id!=-1){
                    localBroadcastManager.sendBroadcast(new Intent("group.message.received").putExtra("id", ""+msg_id).putExtra("groupId",groupId));
                }
            }
        }
    }

//    listener for group invitation
    private class GroupInvitationListener implements InvitationListener{

        @Override
        public void invitationReceived(XMPPConnection conn, MultiUserChat room, String inviter, String reason, String password, Message message) {
//            sync group details from server
            Log.d(TAG, "invitationReceived: "+inviter);
            common.callSyncGroupsService();
        }
    }


}
