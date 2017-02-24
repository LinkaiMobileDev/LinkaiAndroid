package com.linkai.app.modals;

import com.linkai.app.libraries.Const;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by LP1001 on 18-11-2016.
 */
public class Transfer {
    private int t_id;
    private String transfer_id="";
    private String sender_phone="";
    private String sender_account="";
    private String receiver_phone="";
    private float amount;
    private String currency="";
    private String motive="";
    private String fees_paid_by="";
    private String execution_date="";
    private String request_date="";//time stamp
    private String transfer_expiry="";//time stamp
    private int transfer_direction= Const.TRANSFER_DIRECTION.SENT.getValue();
    private int transfer_status=Const.TRANSFER_STATUS.PENDING.getValue();

//    setter methods
    public void setId(int id){
        this.t_id=id;
    }

    public void setTransferId(String transfer_id){
        this.transfer_id=transfer_id;
    }

    public void setSenderPhone(String phone){
        this.sender_phone=phone;
    }

    public void setSenderAccount(String account){
        this.sender_account=account;
    }

    public void setReceiverPhone(String phone){
        this.receiver_phone=phone;
    }

    public void setAmount(float amount){
        this.amount=amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setMotive(String motive){
        this.motive=motive;
    }

    public void setfeesPaidBy(String fees_paid_by){
        this.fees_paid_by=fees_paid_by;
    }

    public void setExecutionDate(String execution_date){
        this.execution_date=execution_date;
    }

    public void setRequestDate(String request_date){
        this.request_date=request_date;
    }

    public void setExpiry(String expiry){
        this.transfer_expiry=expiry;
    }

    public void setTransferDirection(Const.TRANSFER_DIRECTION transfer_direction){
        this.transfer_direction=transfer_direction.getValue();
    }

    public void setTransferDirection(int transfer_direction){
        this.transfer_direction=transfer_direction;
    }

    public void setTransferStatus(Const.TRANSFER_STATUS transferStatus){
        this.transfer_status=transferStatus.getValue();
    }

    public void setTransferStatus(int transferStatus){
        this.transfer_status=transferStatus;
    }

//    getter methods

    public int getId(){return this.t_id;}

    public String getTransferId(){return this.transfer_id;}

    public String getSenderPhone(){return this.sender_phone;}

    public String getSenderAccount(){return this.sender_account;}

    public String getReceiverPhone(){return this.receiver_phone;}

    public float getAmount(){return this.amount;}

    public String getCurrency(){return this.currency;}

    public String getMotive(){return this.motive;}

    public String getFeesPaidBy(){return this.fees_paid_by;}

    public String getExecutionDate(){return this.execution_date;}

    public String getRequestDate(){return this.request_date;}

    public String getExpiry(){return this.transfer_expiry;}

    public int getTransferDirection(){return this.transfer_direction;}

    public int getTransferStatus(){return this.transfer_status;}

//    other methods
    public String getJsonString(){
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("transfer_id",this.transfer_id);
            jsonObject.put("sender_phone",this.sender_phone);
            jsonObject.put("sender_account",this.sender_account);
            jsonObject.put("receiver_phone",this.receiver_phone);
            jsonObject.put("amount",this.amount);
            jsonObject.put("currency",this.currency);
            jsonObject.put("motive",this.motive);
            jsonObject.put("fees_paid_by",this.fees_paid_by);
            jsonObject.put("execution_date",this.execution_date);
            jsonObject.put("request_date",this.request_date);
            jsonObject.put("transfer_expiry",this.transfer_expiry);
            jsonObject.put("transfer_status",this.transfer_status);
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
        return jsonObject.toString();
    }

    public boolean setWithJson(String transfer_data){
        boolean ret=false;
        try {
            JSONObject jsonTransfer=new JSONObject(transfer_data);
            this.transfer_id=jsonTransfer.getString("transfer_id");
            this.sender_phone=jsonTransfer.getString("sender_phone");
            this.sender_account=jsonTransfer.getString("sender_account");
            this.receiver_phone=jsonTransfer.getString("receiver_phone");
            this.amount=Float.parseFloat(jsonTransfer.getString("amount"));
            this.currency=jsonTransfer.getString("currency");
            this.motive=jsonTransfer.getString("motive");
            this.fees_paid_by=jsonTransfer.getString("fees_paid_by");
            this.execution_date=jsonTransfer.getString("execution_date");
            this.request_date=jsonTransfer.getString("request_date");
            this.transfer_expiry=jsonTransfer.getString("transfer_expiry");
            this.transfer_status=jsonTransfer.getInt("transfer_status");
            ret=true;
        } catch (JSONException e) {
            e.printStackTrace();
            ret=false;
        }
        return ret;
    }

}
