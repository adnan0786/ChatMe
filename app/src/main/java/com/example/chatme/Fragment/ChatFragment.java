package com.example.chatme.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatme.ChatListModel;
import com.example.chatme.ChatModel;
import com.example.chatme.Constants.AllConstants;
import com.example.chatme.MessageActivity;
import com.example.chatme.R;
import com.example.chatme.Utils.Util;
import com.example.chatme.databinding.ChatItemLayoutBinding;
import com.example.chatme.databinding.FragmentChatBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;


public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    private Util util;
    private FirebaseRecyclerAdapter<ChatListModel, ViewHolder> firebaseRecyclerAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        binding = FragmentChatBinding.bind(view);
        util = new Util();

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Chats");

        readChat();

        return binding.getRoot();
    }

    private void readChat() {
        Query query = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(util.getUID());

        FirebaseRecyclerOptions<ChatListModel> options = new FirebaseRecyclerOptions.Builder<ChatListModel>()
                .setQuery(query, ChatListModel.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ChatListModel, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i, @NonNull final ChatListModel chatModel) {

                String userID = chatModel.getMember();
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userID);
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Date time = null;
                            String name = dataSnapshot.child("name").getValue().toString();
                            String image = dataSnapshot.child("image").getValue().toString();
                            String online = dataSnapshot.child("online").getValue().toString();
                            Calendar calendar = Calendar.getInstance();
                            try {
                                time = Util.sdf().parse(chatModel.getDate());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            assert time != null;
                            calendar.setTime(time);
                            String date = Util.getTimeAgo(calendar.getTimeInMillis());
                            ChatModel chat = new ChatModel(chatModel.getChatListID(), name, chatModel.getLastMessage()
                                    , image, date, online);
                            viewHolder.binding.setChatModel(chat);

                            viewHolder.itemView.setOnClickListener(view -> {
                                Intent intent = new Intent(getContext(), MessageActivity.class);
                                intent.putExtra("hisID", userID);
                                intent.putExtra("hisImage", image);
                                intent.putExtra("chatID", chatModel.getChatListID());
                                startActivity(intent);
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                ChatItemLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(requireContext()),
                        R.layout.chat_item_layout, parent, false);
                return new ViewHolder(binding);
            }
        };

        binding.recyclerViewChat.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewChat.setHasFixedSize(false);
        binding.recyclerViewChat.setAdapter(firebaseRecyclerAdapter);


    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ChatItemLayoutBinding binding;

        public ViewHolder(@NonNull ChatItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @Override
    public void onResume() {
        firebaseRecyclerAdapter.startListening();
        super.onResume();
    }

    @Override
    public void onPause() {
        firebaseRecyclerAdapter.stopListening();
        super.onPause();
    }

}