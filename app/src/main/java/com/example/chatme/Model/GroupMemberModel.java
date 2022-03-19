package com.example.chatme.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class GroupMemberModel implements Parcelable {

    public String id, role, token;


    public GroupMemberModel() {
    }

    protected GroupMemberModel(Parcel in) {
        id = in.readString();
        role = in.readString();
        token = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(role);
        dest.writeString(token);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GroupMemberModel> CREATOR = new Creator<GroupMemberModel>() {
        @Override
        public GroupMemberModel createFromParcel(Parcel in) {
            return new GroupMemberModel(in);
        }

        @Override
        public GroupMemberModel[] newArray(int size) {
            return new GroupMemberModel[size];
        }
    };
}
