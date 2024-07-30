package com.example.chatify;

import com.google.firebase.firestore.ServerTimestamp;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

public class ChatMessage {

    private  String messageText;
    private  String senderId;
    private  String receiverId;
    @ServerTimestamp
    private  Date sending_time;
    public   Boolean isSentByUser;
    private String messageId;

    public ChatMessage(String messageText,String senderId,String receiverId,Date sending_time,String messageId)
    {
        this.messageText   = messageText;
        this.senderId      = senderId;
        this.receiverId    = receiverId;
        this.sending_time  = sending_time;
        this.messageId     = messageId;
    }
    public ChatMessage()
    {

    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public Date getSending_time() {
        return sending_time;
    }

    public void setSending_time(Date sending_time) {
        this.sending_time = sending_time;
    }

    public Boolean sendMessage()
    {
        return true;
    }
    public Boolean getMessage()
    {
        return true;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getTimeString() {
      //  long timestamp = Long.parseLong(this.sending_time);
        // Use Java Date or other libraries for formatting
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        //return dateFormat.format(new Date(timestamp));
        return "";
    }

}
