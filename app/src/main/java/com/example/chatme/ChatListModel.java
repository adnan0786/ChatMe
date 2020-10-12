package com.example.chatme;

public class ChatListModel {
    String chatListID,date,lastMessage,member;

    public ChatListModel() {
    }

    public ChatListModel(String chatListID, String date, String lastMessage, String member) {
        this.chatListID = chatListID;
        this.date = date;
        this.lastMessage = lastMessage;
        this.member = member;
    }

    public String getChatListID() {
        return chatListID;
    }

    public void setChatListID(String chatListID) {
        this.chatListID = chatListID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getMember() {
        return member;
    }

    public void setMember(String member) {
        this.member = member;
    }
}
