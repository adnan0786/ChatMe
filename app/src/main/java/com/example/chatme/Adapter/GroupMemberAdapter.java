package com.example.chatme.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatme.Interface.MemberItemInterface;
import com.example.chatme.UserModel;
import com.example.chatme.databinding.MemberItemLayoutBinding;

import java.util.ArrayList;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {
    private ArrayList<UserModel> arrayList;
    private MemberItemInterface memberItemInterface;

    public GroupMemberAdapter(@NonNull MemberItemInterface memberItemInterface) {
        this.memberItemInterface = memberItemInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MemberItemLayoutBinding binding = MemberItemLayoutBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (arrayList != null) {
            UserModel userModel = arrayList.get(position);
            holder.binding.setUserModel(userModel);

            holder.itemView.setOnClickListener(view -> {
                memberItemInterface.onMemberClick(userModel, position);
            });

        }

    }

    @Override
    public int getItemCount() {
        if (arrayList != null)
            return arrayList.size();
        else
            return 0;
    }

    public void setArrayList(@NonNull ArrayList<UserModel> arrayList) {
        this.arrayList = arrayList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private MemberItemLayoutBinding binding;

        public ViewHolder(@NonNull MemberItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
