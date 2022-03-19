package com.example.chatme.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.airbnb.lottie.L;
import com.bumptech.glide.Glide;
import com.devlomi.record_view.OnRecordListener;
import com.example.chatme.Adapter.GroupMessageAdapter;
import com.example.chatme.Constants.AllConstants;
import com.example.chatme.GroupMessageModel;
import com.example.chatme.GroupModel;
import com.example.chatme.MessageActivity;
import com.example.chatme.Model.GroupLastMessageModel;
import com.example.chatme.Permissions.Permissions;
import com.example.chatme.R;
import com.example.chatme.Services.SendMediaService;
import com.example.chatme.UserModel;
import com.example.chatme.Utils.Util;
import com.example.chatme.databinding.ActivityGroupMessageBinding;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GroupMessageActivity extends AppCompatActivity {

    private ActivityGroupMessageBinding binding;
    private Util util;
    private String myId, audioPath;
    private GroupModel currentModel;
    private boolean isAdmin;
    private DatabaseReference databaseReference;
    private ArrayList<GroupMessageModel> groupMessageModels;
    private GroupMessageAdapter messageAdapter;
    private Permissions appPermissions;
    private MediaRecorder mediaRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.groupToolbar);
        util = new Util();
        myId = util.getUID();
        groupMessageModels = new ArrayList<>();
        appPermissions = new Permissions();

        binding.groupMessageActivity.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new GroupMessageAdapter(groupMessageModels, this, myId);
        binding.groupMessageActivity.setAdapter(messageAdapter);


        binding.backLayout.setOnClickListener(view -> {
            onBackPressed();
        });

        if (getIntent().hasExtra("groupModel")) {
            currentModel = getIntent().getParcelableExtra("groupModel");
            binding.txtGroupName.setText(currentModel.name);
            Glide.with(this).load(currentModel.image).into(binding.groupImage);
            readMessages();
        }


        binding.btnGroupSend.setOnClickListener(view -> {
            String message = binding.edtGroupMessage.getText().toString().trim();

            if (message.isEmpty()) {
                Toast.makeText(this, "Enter message...", Toast.LENGTH_SHORT).show();
            } else {
                sendMessage(message);

                binding.edtGroupMessage.setText("");
                util.hideKeyBoard(this);
            }
        });

        binding.btnGroupDataSend.setOnClickListener(view -> getGalleryImage());


        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().trim().length() == 0) {
                    binding.btnGroupSend.setVisibility(View.GONE);
                    binding.groupRecordButton.setVisibility(View.VISIBLE);
                } else {
                    binding.groupRecordButton.setVisibility(View.GONE);
                    binding.btnGroupSend.setVisibility(View.VISIBLE);
                }
            }
        };

        binding.edtGroupMessage.addTextChangedListener(textWatcher);

        initRecordingView();

    }

    private void sendMessage(String message) {

        String date = String.valueOf(System.currentTimeMillis());

        GroupMessageModel groupMessageModel = new GroupMessageModel("text", message, date, myId);
        databaseReference = FirebaseDatabase.getInstance().getReference("Group Message").child(currentModel.id);
        databaseReference.push().setValue(groupMessageModel);

        GroupLastMessageModel lastMessageModel = new GroupLastMessageModel();
        lastMessageModel.date = date;
        lastMessageModel.message = message;
        lastMessageModel.senderId = myId;

        databaseReference = FirebaseDatabase.getInstance().getReference("Group Detail").child(currentModel.id)
                .child("lastMessageModel");
        databaseReference.setValue(lastMessageModel);


    }


    private void readMessages() {

        Query query = FirebaseDatabase.getInstance().getReference("Group Message")
                .child(currentModel.id);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    groupMessageModels.clear();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        GroupMessageModel messageModel = ds.getValue(GroupMessageModel.class);

                        if (!messageModel.getSenderId().equals(myId)) {
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                                    .child(messageModel.getSenderId());
                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snap) {
                                    if (snap.exists()) {
                                        UserModel userModel = snap.getValue(UserModel.class);
                                        messageModel.setName(userModel.getName());
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }


                        groupMessageModels.add(messageModel);
                    }
                    messageAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getGalleryImage() {

        Options options = Options.init()
                .setRequestCode(300)                                           //Request code for activity results
                .setCount(5)                                                   //Number of images to restict selection count
                .setFrontfacing(false)                                         //Front Facing camera on start
                .setExcludeVideos(true)                                       //Option to exclude videos
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)     //Orientaion
                .setPath("/ChatMe/Media/Group");                                       //Custom Path For media Storage


        Pix.start(this, options);
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
                    if (appPermissions.isStorageOk(this)) {
                        binding.groupRecordButton.setListenForRecord(true);
                    } else {
                        appPermissions.requestStorage(this);
                    }
                } else {
                    Toast.makeText(this, "Recording permission denied", Toast.LENGTH_SHORT).show();
                }
                break;

            case AllConstants.STORAGE_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    binding.groupRecordButton.setListenForRecord(true);
                } else {
                    Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
                }


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == 300) {
            if (data != null) {
                ArrayList<String> selectedImages = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
                Toast.makeText(this, "Sending Images", Toast.LENGTH_SHORT).show();


                Intent intent = new Intent(GroupMessageActivity.this, SendMediaService.class);
                intent.putExtra("groupId", currentModel.id);
                intent.putExtra("type", "group");
                intent.putStringArrayListExtra("media", selectedImages);

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
                    startForegroundService(intent);
                else startService(intent);
            }

        } else if (requestCode == 90 && resultCode == Activity.RESULT_OK) {
            currentModel = data.getParcelableExtra("groupModel");
            binding.txtGroupName.setText(currentModel.name);
            Glide.with(this).load(currentModel.image).into(binding.groupImage);


        } else if (requestCode == 101 && resultCode == RESULT_OK) {

            currentModel = data.getParcelableExtra("groupModel");
        }
    }

    private void initRecordingView() {

        binding.groupRecordButton.setRecordView(binding.groupRecordView);
        binding.groupRecordButton.setListenForRecord(false);

        binding.groupRecordButton.setOnClickListener(view -> {

            if (appPermissions.isRecordingOk(this)) {
                if (appPermissions.isStorageOk(this)) {

                    binding.groupRecordButton.setListenForRecord(true);

                } else {
                    appPermissions.requestStorage(this);
                }
            } else {
                appPermissions.requestRecording(this);
            }

        });

        binding.groupRecordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                Log.d("TAG", "onStart: ");
                setupRecoding();

                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                binding.groupMessageLayout.setVisibility(View.GONE);
                binding.groupRecordView.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancel() {

                Log.d("TAG", "onCancel: ");

                mediaRecorder.reset();
                mediaRecorder.release();
                File file = new File(audioPath);

                if (file.exists())
                    file.delete();

                binding.groupRecordView.setVisibility(View.GONE);
                binding.groupMessageLayout.setVisibility(View.VISIBLE);

            }

            @Override
            public void onFinish(long recordTime) {
                Log.d("TAG", "onFinish: ");

                try {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                binding.groupRecordView.setVisibility(View.GONE);
                binding.groupMessageLayout.setVisibility(View.VISIBLE);

                sendRecordingMessage(audioPath);

            }

            @Override
            public void onLessThanSecond() {

                Log.d("TAG", "onLessThanSecond: ");

                mediaRecorder.reset();
                mediaRecorder.release();
                File file = new File(audioPath);
                if (file.exists())
                    file.delete();
                binding.groupRecordView.setVisibility(View.GONE);
                binding.groupMessageLayout.setVisibility(View.VISIBLE);

            }
        });
    }

    private void sendRecordingMessage(String audioPath) {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(currentModel.id + "/Media" +
                "/Recording/" + myId + "/" + System.currentTimeMillis());

        Uri audioFile = Uri.fromFile(new File(audioPath));
        storageReference.putFile(audioFile).addOnSuccessListener(taskSnapshot -> {

            Task<Uri> audio = taskSnapshot.getStorage().getDownloadUrl();
            audio.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String url = task.getResult().toString();
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group Message")
                            .child(currentModel.id);
                    GroupMessageModel messageModel = new GroupMessageModel("recording", url,
                            String.valueOf(System.currentTimeMillis()), myId);
                    reference.push().setValue(messageModel);
                }

            });

        });
    }

    private void setupRecoding() {

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "ChatMe/Media/Group/Recording");

        if (!file.exists())
            file.mkdirs();

        audioPath = file.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".3gp";

        mediaRecorder.setOutputFile(audioPath);

    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.group_message_menu, menu);
        menu.findItem(R.id.btnAddMember).setVisible(currentModel.isAdmin);
        menu.findItem(R.id.btnDeleteGroup).setVisible(currentModel.isAdmin);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.btnAddMember:
                Intent member = new Intent(this, AddMemberActivity.class);
                member.putExtra("groupModel", currentModel);
                startActivityForResult(member, 101);

                break;
            case R.id.btnGroupInfo:
                Intent intent = new Intent(this, GroupInfoActivity.class);
                intent.putExtra("groupModel", currentModel);
                startActivityForResult(intent, 90);
                break;
            case R.id.btnExitGroup:
                exitGroup();
                break;
            case R.id.btnDeleteGroup:
                deleteGroup();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteGroup() {

        new AlertDialog.Builder(this)
                .setTitle("Delete Group")
                .setMessage("Are you sure to delete the group?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group Detail")
                                .child(currentModel.id);
                        reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    DatabaseReference database = FirebaseDatabase.getInstance().getReference("Group Message")
                                            .child(currentModel.id);
                                    database.removeValue();
                                    Toast.makeText(GroupMessageActivity.this, "Group Deleted", Toast.LENGTH_SHORT).show();
                                    onBackPressed();
                                } else {
                                    Toast.makeText(GroupMessageActivity.this, "Error : " + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create().show();


    }

    private void exitGroup() {

        new AlertDialog.Builder(this)
                .setTitle("Leave Group")
                .setMessage("Are you sure to leave the group?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group Detail")
                                .child(currentModel.id)
                                .child("Members").child(FirebaseAuth.getInstance().getUid());
                        reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(GroupMessageActivity.this, "Group Leaved", Toast.LENGTH_SHORT).show();
                                    onBackPressed();
                                } else {
                                    Toast.makeText(GroupMessageActivity.this, "Error : " + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create().show();
    }
}