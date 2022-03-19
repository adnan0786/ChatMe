package com.example.chatme;

public class GroupMessageModel {

    String type, message, date, senderId,name;

    public GroupMessageModel() {
    }

    public GroupMessageModel(String type, String message, String date, String senderId) {
        this.type = type;
        this.message = message;
        this.date = date;
        this.senderId = senderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
}
