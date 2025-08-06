package model;

import java.util.Date;

public class Message {
    private int id;
    private String message;
    private int senderId;
    private int receiverId;
    private int groupId;
    private String type;
    private Date date;
    public Message(int id, String message, int senderId, int receiverId, int groupId, String type,Date date) {
        this.id = id;
        this.message = message;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.groupId = groupId;
        this.type = type;
        this.date = date;
    }
    public Message(){}

    public Message(int senderId, int receiverId, String message, String type) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.type = type;
    }

    public Message(int groupId, String message, int senderId, String type) {
        this.groupId = groupId;
        this.message = message;
        this.senderId = senderId;
        this.type = type;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
