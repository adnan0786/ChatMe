package com.example.chatme.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.databinding.library.baseAdapters.BR;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatme.GroupMessageModel;
import com.example.chatme.Model.GroupMemberModel;
import com.example.chatme.databinding.GroupLeftAudioItemLayoutBinding;
import com.example.chatme.databinding.GroupLeftItemLayoutBinding;
import com.example.chatme.databinding.GroupRightAudioItemLayoutBinding;
import com.example.chatme.databinding.GroupRightItemLayoutBinding;
import com.example.chatme.databinding.RightItemLayoutBinding;

import java.util.ArrayList;

import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;

public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.ViewHolder> {

    private ArrayList<GroupMessageModel> messageModels;
    private Context context;
    private String myId;

    public GroupMessageAdapter(ArrayList<GroupMessageModel> memberModels, Context context, String myId) {
        this.messageModels = memberModels;
        this.context = context;
        this.myId = myId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ViewDataBinding viewDataBinding = null;
        switch (viewType) {
            case 0:
                viewDataBinding = GroupRightItemLayoutBinding.inflate(
                        LayoutInflater.from(context), parent, false);
                break;

            case 100:
                viewDataBinding = GroupRightAudioItemLayoutBinding.inflate(
                        LayoutInflater.from(context), parent, false);
                break;
            case 1:
                viewDataBinding = GroupLeftItemLayoutBinding.inflate(
                        LayoutInflater.from(context), parent, false);
                break;

            case 200:
                viewDataBinding = GroupLeftAudioItemLayoutBinding.inflate(
                        LayoutInflater.from(context), parent, false);
                break;
        }
        return new ViewHolder(viewDataBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        GroupMessageModel groupMessageModel = messageModels.get(position);

        switch (getItemViewType(position)) {
            case 0:
            case 1:
                holder.viewDataBinding.setVariable(BR.groupMessage, groupMessageModel);
                break;

            case 100:
            case 200:
                holder.voicePlayerView.setAudio(groupMessageModel.getMessage());

        }
        

    }

    @Override
    public int getItemCount() {

        return messageModels.size();
    }

    @Override
    public int getItemViewType(int position) {
        GroupMessageModel messageModel = messageModels.get(position);
        if (myId.equals(messageModel.getSenderId())) {

            if (messageModel.getType().equals("recording"))
                return 100;
            else
                return 0;
        } else {

            if (messageModel.getType().equals("recording"))
                return 200;
            else
                return 1;
        }


    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ViewDataBinding viewDataBinding;
        private VoicePlayerView voicePlayerView;

        public ViewHolder(@NonNull ViewDataBinding viewDataBinding) {
            super(viewDataBinding.getRoot());
            this.viewDataBinding = viewDataBinding;

            if (viewDataBinding instanceof GroupRightAudioItemLayoutBinding) {

                voicePlayerView = ((GroupRightAudioItemLayoutBinding) viewDataBinding).voicePlayerView;
            }

            if (viewDataBinding instanceof GroupLeftAudioItemLayoutBinding) {
                voicePlayerView = ((GroupLeftAudioItemLayoutBinding) viewDataBinding).voicePlayerView;
            }
        }
    }
}
