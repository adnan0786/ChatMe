package com.example.chatme.Adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatme.Activity.GroupMessageActivity;
import com.example.chatme.GroupMessageModel;
import com.example.chatme.GroupModel;
import com.example.chatme.Model.GroupMemberModel;
import com.example.chatme.databinding.GroupChatItemLayoutBinding;

import java.util.ArrayList;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.ViewHolder> {

    private ArrayList<GroupModel> groupModels;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        GroupChatItemLayoutBinding binding = GroupChatItemLayoutBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (groupModels != null) {
            GroupModel groupModel = groupModels.get(position);
            holder.binding.setGroupModel(groupModel);

            holder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), GroupMessageActivity.class);
                intent.putExtra("groupModel", groupModel);
                view.getContext().startActivity(intent);
            });

        }

    }

    @Override
    public int getItemCount() {
        if (groupModels != null)
            return groupModels.size();
        else
            return 0;
    }

    public void setGroupModels(@NonNull ArrayList<GroupModel> groupModels) {
        this.groupModels = groupModels;
        notifyDataSetChanged();

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private GroupChatItemLayoutBinding binding;

        public ViewHolder(@NonNull GroupChatItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
