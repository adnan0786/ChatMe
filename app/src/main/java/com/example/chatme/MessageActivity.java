package com.example.chatme;

import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.devlomi.record_view.OnRecordListener;
import com.example.chatme.Activity.UserInfo;
import com.example.chatme.Constants.AllConstants;
import com.example.chatme.Permissions.Permissions;
import com.example.chatme.Services.SendMediaService;
import com.example.chatme.Utils.Util;
import com.example.chatme.databinding.ActivityMessageBinding;
import com.example.chatme.databinding.LeftAudioItemLayoutBinding;
import com.example.chatme.databinding.LeftItemLayoutBinding;
import com.example.chatme.databinding.RightAudioItemLayoutBinding;
import com.example.chatme.databinding.RightItemLayoutBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import me.jagar.chatvoiceplayerlibrary.VoicePlayerView;

public class MessageActivity extends AppCompatActivity {

    private ActivityMessageBinding binding;
    private String hisID, hisImage, myID, chatID = null, myImage, myName, audioPath;
    private Util util;
    private DatabaseReference databaseReference;
    private FirebaseRecyclerAdapter<MessageModel, ViewHolder> firebaseRecyclerAdapter;
    private SharedPreferences sharedPreferences;
    private Permissions permissions;
    private MediaRecorder mediaRecorder;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_message, null, false);
        setContentView(binding.getRoot());
        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        myImage = sharedPreferences.getString("userImage", "");
        myName = sharedPreferences.getString("username", "");
        util = new Util();
        myID = util.getUID();
        permissions = new Permissions();


        if (getIntent().hasExtra("chatID")) {
            chatID = getIntent().getStringExtra("chatID");
            hisID = getIntent().getStringExtra("hisID");
            hisImage = getIntent().getStringExtra("hisImage");

            Log.d("message", "onCreate: hisID" + hisID + "\n myID" + myID);


            readMessages(chatID);
        } else {
            hisID = getIntent().getStringExtra("hisID");
            hisImage = getIntent().getStringExtra("hisImage");
        }


        if (chatID == null)
            checkChat(hisID);

        binding.setImage(hisImage);
        binding.setActivity(this);

        binding.btnSend.setOnClickListener(v -> {

            String message = binding.msgText.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(MessageActivity.this, "Enter Message...", Toast.LENGTH_SHORT).show();
            } else {
                sendMessage(message);
                getToken(message, hisID, myImage, chatID);
            }

            binding.msgText.setText("");
            util.hideKeyBoard(MessageActivity.this);
        });

        binding.msgText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {

                    updateTypingStatus("false");
                    binding.btnSend.setVisibility(View.GONE);
                    binding.recordButton.setVisibility(View.VISIBLE);

                } else {
                    updateTypingStatus(hisID);
                    binding.recordButton.setVisibility(View.GONE);
                    binding.btnSend.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.btnDataSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (binding.dataLayout.getVisibility() == View.INVISIBLE)
//                    showLayout();
//                else
//                    hideLayout();

                getGalleryImage();
            }
        });

        checkStatus(hisID);

//        binding.imgGallery.setOnClickListener(view -> {
//            getGalleryImage();
//        });

        initView();


    }


    private void checkChat(final String hisID) {
        databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(myID);
        Query query = databaseReference.orderByChild("member").equalTo(hisID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String id = ds.child("member").getValue().toString();
                        if (id.equals(hisID)) {
                            chatID = ds.getKey();
                            readMessages(chatID);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void createChat(String msg) {
        databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(myID);
        chatID = databaseReference.push().getKey();
        ChatListModel chatListModel = new ChatListModel(chatID, util.currentData(), msg, hisID);
        databaseReference.child(chatID).setValue(chatListModel);

        databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(hisID);
        ChatListModel chatList = new ChatListModel(chatID, util.currentData(), msg, myID);
        databaseReference.child(chatID).setValue(chatList);

        databaseReference = FirebaseDatabase.getInstance().getReference("Chat").child(chatID);
        MessageModel messageModel = new MessageModel(myID, hisID, msg, util.currentData(), "text");
        databaseReference.push().setValue(messageModel);


    }

    private void sendMessage(String msg) {
        if (chatID == null) {
            createChat(msg);

        } else {
            String date = util.currentData();
            MessageModel messageModel = new MessageModel(myID, hisID, msg, date, "text");
            databaseReference = FirebaseDatabase.getInstance().getReference("Chat").child(chatID);
            databaseReference.push().setValue(messageModel);

            Map<String, Object> map = new HashMap<>();
            map.put("lastMessage", msg);
            map.put("date", date);
            databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(myID).child(chatID);
            databaseReference.updateChildren(map);

            databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(hisID).child(chatID);
            Map<String, Object> update = new HashMap<>();
            update.put("lastMessage", msg);
            update.put("date", date);
            databaseReference.updateChildren(map);

        }
    }

    public void userInfo() {
        Intent intent = new Intent(this, UserInfo.class);
        intent.putExtra("userID", hisID);
        startActivity(intent);
    }

    private void readMessages(String chatID) {
        Query query = FirebaseDatabase
                .getInstance().getReference().child("Chat")
                .child(chatID);
        FirebaseRecyclerOptions<MessageModel> options = new FirebaseRecyclerOptions.Builder<MessageModel>()
                .setQuery(query, MessageModel.class).build();
        query.keepSynced(true);


        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<MessageModel, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder viewHolder, int position, @NonNull MessageModel messageModel) {
                switch (getItemViewType(position)) {
                    case 0:

                        viewHolder.viewDataBinding.setVariable(BR.messageImage, myImage);
                        viewHolder.viewDataBinding.setVariable(BR.message, messageModel);
                        break;

                    case 100:
                        viewHolder.viewDataBinding.setVariable(BR.messageImage, myImage);
                        viewHolder.voicePlayerView.setAudio(messageModel.message);
                        break;
                    case 1:
                        viewHolder.viewDataBinding.setVariable(BR.messageImage, hisImage);
                        viewHolder.viewDataBinding.setVariable(BR.message, messageModel);
                        break;
                    case 200:
                        viewHolder.viewDataBinding.setVariable(BR.messageImage, hisImage);
                        viewHolder.voicePlayerView.setAudio(messageModel.message);

                        break;
                }


            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                ViewDataBinding viewDataBinding = null;
                switch (viewType) {
                    case 0:
                        viewDataBinding = RightItemLayoutBinding.inflate(
                                LayoutInflater.from(getBaseContext()), parent, false);
                        break;
                    case 100:
                        viewDataBinding = RightAudioItemLayoutBinding.inflate(
                                LayoutInflater.from(parent.getContext()), parent, false);
                        break;
                    case 1:
                        viewDataBinding = LeftItemLayoutBinding.inflate(
                                LayoutInflater.from(getBaseContext()), parent, false);
                        break;
                    case 200:

                        viewDataBinding = LeftAudioItemLayoutBinding.inflate(
                                LayoutInflater.from(parent.getContext()), parent, false);
                        break;
                }
                return new ViewHolder(viewDataBinding);

            }

            @Override
            public int getItemViewType(int position) {
                MessageModel messageModel = getItem(position);
                if (myID.equals(messageModel.getSender())) {

                    if (messageModel.getType().equals("recording"))
                        return 100;
                    else return 0;

                } else {
                    if (messageModel.getType().equals("recording"))
                        return 200;
                    else return 1;
                }
            }
        };


        binding.recyclerViewMessage.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewMessage.setHasFixedSize(false);
        binding.recyclerViewMessage.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ViewDataBinding viewDataBinding;
        private VoicePlayerView voicePlayerView;

        public ViewHolder(@NonNull ViewDataBinding viewDataBinding) {
            super(viewDataBinding.getRoot());
            this.viewDataBinding = viewDataBinding;

            if (viewDataBinding instanceof RightAudioItemLayoutBinding) {
                voicePlayerView = ((RightAudioItemLayoutBinding) viewDataBinding).voicePlayerView;

            }

            if (viewDataBinding instanceof LeftAudioItemLayoutBinding) {
                voicePlayerView = ((LeftAudioItemLayoutBinding) viewDataBinding).voicePlayerView;
            }

        }
    }

    private void checkStatus(String hisID) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(hisID);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String online = Objects.requireNonNull(dataSnapshot.child("online").getValue()).toString();
                    String typing = Objects.requireNonNull(dataSnapshot.child("typing").getValue()).toString();
                    binding.setStatus(online);
                    if (typing.equals(myID)) {
                        binding.typingStatus.setVisibility(View.VISIBLE);
                        binding.typingStatus.playAnimation();
                    } else {
                        binding.typingStatus.cancelAnimation();
                        binding.typingStatus.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        util.updateOnlineStatus("online");
        super.onResume();
    }

    @Override
    protected void onPause() {
        util.updateOnlineStatus(String.valueOf(System.currentTimeMillis()));
        updateTypingStatus("false");
        super.onPause();
    }

    private void updateTypingStatus(String status) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(myID);
        Map<String, Object> map = new HashMap<>();
        map.put("typing", status);
        databaseReference.updateChildren(map);
    }

    private void getToken(String message, String hisID, String myImage, String chatID) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(hisID);
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String token = snapshot.child("token").getValue().toString();


                JSONObject to = new JSONObject();
                JSONObject data = new JSONObject();
                try {
                    data.put("title", myName);
                    data.put("message", message);
                    data.put("hisID", myID);
                    data.put("hisImage", myImage);
                    data.put("chatID", chatID);


                    to.put("to", token);
                    to.put("data", data);

                    sendNotification(to);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification(JSONObject to) {

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, AllConstants.NOTIFICATION_URL, to, response -> {
            Log.d("notification", "sendNotification: " + response);
        }, error -> {
            Log.d("notification", "sendNotification: " + error);
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> map = new HashMap<>();
                map.put("Authorization", "key=" + AllConstants.SERVER_KEY);
                map.put("Content-Type", "application/json");
                return map;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        request.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }

    private void showLayout() {
        RelativeLayout view = binding.dataLayout;
        float radius = Math.max(view.getWidth(), view.getHeight());
        Animator animator = ViewAnimationUtils.createCircularReveal(view, view.getLeft(), view.getTop(), 0, radius * 2);
        animator.setDuration(800);
        view.setVisibility(View.VISIBLE);
        animator.start();

    }

    private void hideLayout() {

        RelativeLayout view = binding.dataLayout;
        float radius = Math.max(view.getWidth(), view.getHeight());
        Animator animator = ViewAnimationUtils.createCircularReveal(view, view.getLeft(), view.getTop(), radius * 2, 0);
        animator.setDuration(800);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    @Override
    public void onBackPressed() {

        if (binding.dataLayout.getVisibility() == View.VISIBLE)
            hideLayout();
        else
            super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 300) {
            if (data != null) {
                ArrayList<String> selectedImages = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);

                if (chatID == null)
                    Toast.makeText(this, "Send simple message first", Toast.LENGTH_SHORT).show();
                else {

                    Intent intent = new Intent(MessageActivity.this, SendMediaService.class);
                    intent.putExtra("hisID", hisID);
                    intent.putExtra("chatID", chatID);
                    intent.putStringArrayListExtra("media", selectedImages);

                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
                        startForegroundService(intent);
                    else startService(intent);
                }

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        switch (requestCode) {
            case PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getGalleryImage();
                } else {
                    Toast.makeText(this, "Approve permissions to open Pix ImagePicker", Toast.LENGTH_LONG).show();
                }

                break;

            case AllConstants.RECORDING_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (this.permissions.isStorageOk(MessageActivity.this))
                        binding.recordButton.setListenForRecord(true);
                    else this.permissions.requestStorage(MessageActivity.this);

                } else
                    Toast.makeText(this, "Recording permission denied", Toast.LENGTH_SHORT).show();
                break;
            case AllConstants.STORAGE_REQUEST_CODE:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    binding.recordButton.setListenForRecord(true);
                else
                    Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
                break;


        }
    }

    private void getGalleryImage() {

        Options options = Options.init()
                .setRequestCode(300)                                           //Request code for activity results
                .setCount(5)                                                   //Number of images to restict selection count
                .setFrontfacing(false)                                         //Front Facing camera on start
                .setExcludeVideos(true)                                       //Option to exclude videos
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)     //Orientaion
                .setPath("/ChatMe/Media");                                       //Custom Path For media Storage


        Pix.start(this, options);
    }

    private void initView() {

        binding.recordButton.setRecordView(binding.recordView);
        binding.recordButton.setListenForRecord(false);

        binding.recordButton.setOnClickListener(view -> {

            if (permissions.isRecordingOk(MessageActivity.this))
                if (permissions.isStorageOk(MessageActivity.this))
                    binding.recordButton.setListenForRecord(true);
                else permissions.requestStorage(MessageActivity.this);
            else permissions.requestRecording(MessageActivity.this);
        });

        binding.recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                //Start Recording..
                Log.d("RecordView", "onStart");

                setUpRecording();

                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                binding.messageLayout.setVisibility(View.GONE);
                binding.recordView.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancel() {
                //On Swipe To Cancel
                Log.d("RecordView", "onCancel");

                mediaRecorder.reset();
                mediaRecorder.release();
                File file = new File(audioPath);
                if (file.exists())
                    file.delete();

                binding.recordView.setVisibility(View.GONE);
                binding.messageLayout.setVisibility(View.VISIBLE);


            }

            @Override
            public void onFinish(long recordTime) {
                //Stop Recording..
                Log.d("RecordView", "onFinish");

                try {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                binding.recordView.setVisibility(View.GONE);
                binding.messageLayout.setVisibility(View.VISIBLE);

                sendRecodingMessage(audioPath);


            }

            @Override
            public void onLessThanSecond() {
                //When the record time is less than One Second
                Log.d("RecordView", "onLessThanSecond");

                mediaRecorder.reset();
                mediaRecorder.release();

                File file = new File(audioPath);
                if (file.exists())
                    file.delete();



                binding.recordView.setVisibility(View.GONE);
                binding.recyclerViewMessage.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setUpRecording() {

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "ChatMe/Media/Recording");

        if (!file.exists())
            file.mkdirs();
        audioPath = file.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".3gp";

        mediaRecorder.setOutputFile(audioPath);
    }

    private void sendRecodingMessage(String audioPath) {
        if (chatID == null)
            Toast.makeText(this, "Send simple message first", Toast.LENGTH_SHORT).show();
        else {


            StorageReference storageReference = FirebaseStorage.getInstance().getReference(chatID + "/Media/Recording/" + myID + "/" + System.currentTimeMillis());
            Uri audioFile = Uri.fromFile(new File(audioPath));
            storageReference.putFile(audioFile).addOnSuccessListener(success -> {
                Task<Uri> audioUrl = success.getStorage().getDownloadUrl();

                audioUrl.addOnCompleteListener(path -> {
                    if (path.isSuccessful()) {

                        String url = path.getResult().toString();
                        if (chatID == null) {
                            createChat(url);
                        } else {
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chat").child(chatID);
                            MessageModel messageModel = new MessageModel(myID, hisID, url, util.currentData(), "recording");
                            databaseReference.push().setValue(messageModel);
                        }
                    }
                });
            });
        }
    }


}
