package com.example.chatme;

public class ChatModel {

    String chatID, name, lastMessage, image, date, online;

    public ChatModel() {
    }

    public ChatModel(String chatID, String name, String lastMessage, String image, String date, String online) {
        this.chatID = chatID;
        this.name = name;
        this.lastMessage = lastMessage;
        this.image = image;
        this.date = date;
        this.online = online;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getChatID() {
        return chatID;
    }

    public void setChatID(String chatID) {
        this.chatID = chatID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
