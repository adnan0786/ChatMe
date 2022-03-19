package com.example.chatme.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.chatme.Activity.DashBoard;
import com.example.chatme.Constants.AllConstants;
import com.example.chatme.Permissions.Permissions;
import com.example.chatme.databinding.FragmentUserDataBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;


public class UserData extends Fragment {


    private FragmentUserDataBinding binding;
    private String storagePath, name, status;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Uri imageUri;
    private Permissions permissions;
    private SharedPreferences sharedPreferences;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentUserDataBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();
        storagePath = firebaseAuth.getUid() + AllConstants.IMAGE_PATH;
        permissions = new Permissions();
        sharedPreferences = getContext().getSharedPreferences("UserData", Context.MODE_PRIVATE);


        binding.imgPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (permissions.isStorageOk(getContext()))
                    pickImage();
                else
                    permissions.requestStorage(getActivity());
            }
        });


        binding.btnDataDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkStatus() & checkImage() & checkName())
                    uploadData();
            }
        });


        return view;
    }

    private void uploadData() {

        Toast.makeText(getContext(), "Uploading", Toast.LENGTH_SHORT).show();
        storageReference.child(storagePath).putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                task.addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        String url = task.getResult().toString();
                        Map<String, Object> map = new HashMap<>();
                        map.put("name", name);
                        map.put("status", status);
                        map.put("image", url);
                        databaseReference.child(firebaseAuth.getUid()).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("userImage", url).apply();
                                    editor.putString("username", name).apply();
                                    Intent intent = new Intent(getContext(), DashBoard.class);
                                    startActivity(intent);
                                    getActivity().finish();
                                } else
                                    Toast.makeText(getContext(), "Fail to upload", Toast.LENGTH_SHORT).show();
                            }
                        });


                    }
                });
            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AllConstants.STORAGE_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    pickImage();
                else
                    Toast.makeText(getContext(), "permission denied", Toast.LENGTH_SHORT).show();

                break;
        }
    }

    private void pickImage() {

        CropImage.activity()
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(getContext(), this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (data != null) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    imageUri = result.getUri();
                    binding.imgUser.setImageURI(imageUri);
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            }
        }
    }

    private boolean checkName() {
        name = binding.edtUserName.getText().toString().trim();
        if (name.isEmpty()) {
            binding.edtUserName.setError("Filed is required");
            return false;
        } else {
            binding.edtUserName.setError(null);
            return true;
        }
    }

    private boolean checkStatus() {
        status = binding.edtUserStatus.getText().toString();
        if (status.isEmpty()) {
            binding.edtUserStatus.setError("Filed is required");
            return false;
        } else {
            binding.edtUserStatus.setError(null);
            return true;
        }
    }

    private boolean checkImage() {
        if (imageUri == null) {
            Toast.makeText(getContext(), "Image is required", Toast.LENGTH_SHORT).show();
            return false;
        } else return true;
    }
}
