package com.example.chatme;

import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserModel {

    String name, status, image, number, uID, online, typing, token;

    public UserModel() {
    }

    public UserModel(String name, String status, String image, String number, String uID, String online, String typing, String token) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.number = number;
        this.uID = uID;
        this.online = online;
        this.typing = typing;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTyping() {
        return typing;
    }

    public void setTyping(String typing) {
        this.typing = typing;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    @BindingAdapter("imageUrl")
    public static void loadImage(@NonNull CircleImageView view, @NonNull String image) {
        Glide.with(view.getContext()).load(image).into(view);
    }

    @BindingAdapter("imageChat")
    public static void loadImage(@NonNull ImageView view, @NonNull String image) {

        Glide.with(view.getContext()).load(image).into(view);

    }
}
