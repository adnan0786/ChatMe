package com.example.chatme.Interface;

import androidx.annotation.NonNull;

import com.example.chatme.UserModel;

public interface MemberItemInterface {

    void onMemberClick(@NonNull UserModel userModel, int position);
}
