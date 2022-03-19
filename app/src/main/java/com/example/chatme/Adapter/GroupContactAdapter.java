package com.example.chatme.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatme.Interface.ContactItemInterface;
import com.example.chatme.UserModel;
import com.example.chatme.databinding.ContactItemLayoutBinding;

import java.util.ArrayList;

public class GroupContactAdapter extends RecyclerView.Adapter<GroupContactAdapter.ViewHolder> {
    private ArrayList<UserModel> arrayList;
    private ContactItemInterface contactItemInterface;

    public GroupContactAdapter(ContactItemInterface contactItemInterface) {
        this.contactItemInterface = contactItemInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ContactItemLayoutBinding binding = ContactItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (arrayList != null) {
            UserModel userModel = arrayList.get(position);
            holder.binding.setUserModel(userModel);
            holder.binding.imgContact.setVisibility(View.GONE);

            holder.itemView.setOnClickListener(view -> {
                contactItemInterface.onContactClick(userModel, position, false);
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

    public void setArrayList(ArrayList<UserModel> arrayList) {
        this.arrayList = arrayList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ContactItemLayoutBinding binding;

        public ViewHolder(@NonNull ContactItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
