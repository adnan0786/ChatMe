package com.example.chatme.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatme.Adapter.GroupMemberAdapter;
import com.example.chatme.Constants.AllConstants;
import com.example.chatme.GroupModel;
import com.example.chatme.Interface.MemberItemInterface;
import com.example.chatme.Permissions.Permissions;
import com.example.chatme.R;
import com.example.chatme.UserModel;
import com.example.chatme.databinding.ActivityGroupInfoBinding;
import com.example.chatme.databinding.AdminDialogLayoutBinding;
import com.example.chatme.databinding.DialogLayoutBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupInfoActivity extends AppCompatActivity implements MemberItemInterface {

    private ActivityGroupInfoBinding binding;
    private GroupModel currentGroup;
    ArrayList<UserModel> arrayList;
    AlertDialog alertDialog;
    Permissions permissions;
    Uri imageUri;
    private GroupMemberAdapter memberAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.infoToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        memberAdapter = new GroupMemberAdapter(this);

        binding.memberRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.memberRecyclerView.setNestedScrollingEnabled(false);
        binding.memberRecyclerView.setAdapter(memberAdapter);

        permissions = new Permissions();
        currentGroup = getIntent().getParcelableExtra("groupModel");
        Toast.makeText(this, "" + currentGroup.members.size(), Toast.LENGTH_SHORT).show();

        Glide.with(this).load(currentGroup.image).into(binding.expandedImage);
        binding.collapsingToolbar.setTitle(currentGroup.name);
//        binding.txtTotalMember.setText(currentGroup.members.size()+"Members");

        arrayList = new ArrayList<>();

        for (int i = 0; i < currentGroup.members.size(); i++) {


            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                    .child(currentGroup.members.get(i).id);
            int finalI = i;
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        UserModel userModel = snapshot.getValue(UserModel.class);
                        userModel.setTyping(currentGroup.members.get(finalI).role);
                        arrayList.add(userModel);
                    }

                    if (finalI == currentGroup.members.size() - 1) {
                        memberAdapter.setArrayList(arrayList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


        if (currentGroup.isAdmin) {
            binding.cardDeleteGroup.setVisibility(View.VISIBLE);
        } else {
            binding.cardDeleteGroup.setVisibility(View.GONE);
        }

        binding.expandedImage.setOnClickListener(view -> {
            if (permissions.isStorageOk(this)) {
                pickImage();
            } else {
                permissions.requestStorage(this);
            }
        });

        binding.btnEditGroupName.setOnClickListener(view -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view1 = LayoutInflater.from(this).inflate(R.layout.dialog_layout, null);
            builder.setView(view1);
            EditText edtName = view1.findViewById(R.id.edtUserStatus);
            edtName.setHint("Group Name");
            Button btnDone = view1.findViewById(R.id.btnEditStatus);

            btnDone.setOnClickListener(done -> {
                String groupName = edtName.getText().toString().trim();
                if (!groupName.isEmpty()) {

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group Detail")
                            .child(currentGroup.id);
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", groupName);
                    reference.updateChildren(map);
                    currentGroup.name = groupName;
                    binding.collapsingToolbar.setTitle(currentGroup.name);
                    alertDialog.dismiss();
                }
            });

            alertDialog = builder.create();
            alertDialog.show();

        });


        binding.cardExitGroup.setOnClickListener(view -> {
            exitGroup();
        });

        binding.cardDeleteGroup.setOnClickListener(view -> {
            deleteGroup();
        });


    }

    private void deleteGroup() {

        new AlertDialog.Builder(this)
                .setTitle("Delete Group")
                .setMessage("Are you sure to delete the group?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group Detail")
                                .child(currentGroup.id);
                        reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    DatabaseReference database = FirebaseDatabase.getInstance().getReference("Group Message")
                                            .child(currentGroup.id);
                                    database.removeValue();
                                    Toast.makeText(GroupInfoActivity.this, "Group Deleted", Toast.LENGTH_SHORT).show();
                                    onBackPressed();
                                } else {
                                    Toast.makeText(GroupInfoActivity.this, "Error : " + task.getException(), Toast.LENGTH_SHORT).show();
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
                                .child(currentGroup.id)
                                .child("Members").child(FirebaseAuth.getInstance().getUid());
                        reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(GroupInfoActivity.this, "Group Leaved", Toast.LENGTH_SHORT).show();
                                    onBackPressed();
                                } else {
                                    Toast.makeText(GroupInfoActivity.this, "Error : " + task.getException(), Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent();
        intent.putExtra("groupModel", currentGroup);
        setResult(RESULT_OK, intent);
        finish();

        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == AllConstants.STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            } else {
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pickImage() {

        CropImage.activity()
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                updateGroupImage();
            }
        }
    }

    private void updateGroupImage() {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        storageReference.child(currentGroup.id + AllConstants.GROUP_IMAGE).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(GroupInfoActivity.this, "Uploading Image.....", Toast.LENGTH_SHORT).show();
                Task<Uri> image = taskSnapshot.getStorage().getDownloadUrl();
                image.addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            String url = task.getResult().toString();
                            currentGroup.image = url;
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group Detail")
                                    .child(currentGroup.id);
                            Map<String, Object> map = new HashMap<>();
                            map.put("image", url);
                            reference.updateChildren(map);
                            Glide.with(GroupInfoActivity.this).load(url).into(binding.expandedImage);
                            Toast.makeText(GroupInfoActivity.this, "Image Updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("TAG", "onComplete: " + task.getException());
                            Toast.makeText(GroupInfoActivity.this, "Error : " + task.getException(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        });
    }

    @Override
    public void onMemberClick(@NonNull UserModel userModel, int position) {

        if (!userModel.getuID().equals(FirebaseAuth.getInstance().getUid())) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            AdminDialogLayoutBinding layoutBinding = AdminDialogLayoutBinding.inflate(LayoutInflater.from(this));

            builder.setView(layoutBinding.getRoot());

            if (!currentGroup.isAdmin) {

                layoutBinding.txtAdmin.setVisibility(View.GONE);
                layoutBinding.txtRAdmin.setVisibility(View.GONE);
                layoutBinding.txtRemove.setVisibility(View.GONE);
            }

            if (userModel.getTyping().equals("admin")) {
                layoutBinding.txtAdmin.setVisibility(View.GONE);
            } else {
                layoutBinding.txtRAdmin.setVisibility(View.GONE);
            }


            layoutBinding.txtInfo.setOnClickListener(view -> {
                Intent intent = new Intent(GroupInfoActivity.this, UserInfo.class);
                intent.putExtra("userID", userModel.getuID());
                alertDialog.dismiss();
                startActivity(intent);
            });

            layoutBinding.txtAdmin.setOnClickListener(view -> {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group Detail")
                        .child(currentGroup.id).child("Members").child(userModel.getuID());

                Map<String, Object> map = new HashMap<>();
                map.put("role", "admin");
                reference.updateChildren(map);
                alertDialog.dismiss();
                arrayList.get(position).setTyping("admin");
                currentGroup.members.get(position).role = "admin";
                memberAdapter.setArrayList(arrayList);
            });

            layoutBinding.txtRAdmin.setOnClickListener(view -> {

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group Detail")
                        .child(currentGroup.id).child("Members").child(userModel.getuID());

                Map<String, Object> map = new HashMap<>();
                map.put("role", "member");
                reference.updateChildren(map);
                alertDialog.dismiss();
                arrayList.get(position).setTyping("member");
                currentGroup.members.get(position).role = "member";
                memberAdapter.setArrayList(arrayList);

            });


            layoutBinding.txtRemove.setOnClickListener(view -> {

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group Detail")
                        .child(currentGroup.id).child("Members").child(userModel.getuID());


                reference.removeValue();

                alertDialog.dismiss();
                arrayList.remove(position);
                currentGroup.members.remove(position);
                memberAdapter.setArrayList(arrayList);

            });

            alertDialog = builder.create();
            alertDialog.show();
        }

    }
}