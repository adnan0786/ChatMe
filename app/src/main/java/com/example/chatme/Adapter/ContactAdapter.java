package com.example.chatme.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatme.Activity.UserInfo;
import com.example.chatme.MessageActivity;
import com.example.chatme.R;
import com.example.chatme.UserModel;
import com.example.chatme.databinding.ContactItemLayoutBinding;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> implements Filterable {

    private Context context;
    private ArrayList<UserModel> arrayList, filterArrayList;


    public ContactAdapter(Context context, ArrayList<UserModel> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        filterArrayList = new ArrayList<>();
        filterArrayList.addAll(arrayList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        ContactItemLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context),
                R.layout.contact_item_layout, parent, false);


        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final UserModel userModel = arrayList.get(position);
        holder.layoutBinding.setUserModel(userModel);

        holder.layoutBinding.imgContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserInfo.class);
                intent.putExtra("userID", userModel.getuID());
                context.startActivity(intent);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("hisID", userModel.getuID());
                intent.putExtra("hisImage", userModel.getImage());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    @Override
    public Filter getFilter() {
        return contactFilter;
    }

    private Filter contactFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<UserModel> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0)
                filteredList.addAll(filterArrayList);
            else {
                String filter = constraint.toString().toLowerCase().trim();
                for (UserModel userModel : filterArrayList) {
                    if (userModel.getName().toLowerCase().contains(filter))
                        filteredList.add(userModel);
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredList;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            arrayList.clear();
            arrayList.addAll((Collection<? extends UserModel>) results.values);
            notifyDataSetChanged();

        }
    };


    public class ViewHolder extends RecyclerView.ViewHolder {
        ContactItemLayoutBinding layoutBinding;

        public ViewHolder(@NotNull ContactItemLayoutBinding layoutBinding) {
            super(layoutBinding.getRoot());
            this.layoutBinding = layoutBinding;
        }
    }
}
