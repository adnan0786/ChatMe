package com.example.chatme.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatme.Interface.ContactItemInterface;
import com.example.chatme.UserModel;
import com.example.chatme.databinding.SelectedContactItemLayoutBinding;

import java.util.ArrayList;

public class SelectedContactAdapter extends RecyclerView.Adapter<SelectedContactAdapter.ViewHolder> {

    private ArrayList<UserModel> userModels;
    private ContactItemInterface contactItemInterface;
    private Context context;

    public SelectedContactAdapter(ContactItemInterface contactItemInterface, Context context) {
        this.contactItemInterface = contactItemInterface;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SelectedContactItemLayoutBinding binding = SelectedContactItemLayoutBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (userModels != null) {
            UserModel userModel = userModels.get(position);
            Glide.with(context).load(userModel.getImage()).into(holder.binding.imgSelectedContact);
            holder.itemView.setOnClickListener(view -> {
                contactItemInterface.onContactClick(userModel, position, true);
            });
        }

    }

    @Override
    public int getItemCount() {
        if (userModels != null)
            return userModels.size();
        else
            return 0;
    }

    public void setUserModels(ArrayList<UserModel> userModels) {
        this.userModels = userModels;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private SelectedContactItemLayoutBinding binding;

        public ViewHolder(@NonNull SelectedContactItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
