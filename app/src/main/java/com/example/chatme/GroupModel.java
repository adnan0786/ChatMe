package com.example.chatme;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.chatme.Model.GroupLastMessageModel;
import com.example.chatme.Model.GroupMemberModel;

import java.util.List;

public class GroupModel implements Parcelable {

    public String id, adminId, adminName, createdAt, image, name;
    public List<GroupMemberModel> members;
    public GroupLastMessageModel lastMessageModel;
    public boolean isAdmin;

    public GroupModel() {
    }

    protected GroupModel(Parcel in) {
        id = in.readString();
        adminId = in.readString();
        adminName = in.readString();
        createdAt = in.readString();
        image = in.readString();
        name = in.readString();
        members = in.createTypedArrayList(GroupMemberModel.CREATOR);
        lastMessageModel = in.readParcelable(GroupLastMessageModel.class.getClassLoader());
        isAdmin = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(adminId);
        dest.writeString(adminName);
        dest.writeString(createdAt);
        dest.writeString(image);
        dest.writeString(name);
        dest.writeTypedList(members);
        dest.writeParcelable(lastMessageModel, flags);
        dest.writeByte((byte) (isAdmin ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GroupModel> CREATOR = new Creator<GroupModel>() {
        @Override
        public GroupModel createFromParcel(Parcel in) {
            return new GroupModel(in);
        }

        @Override
        public GroupModel[] newArray(int size) {
            return new GroupModel[size];
        }
    };
}
