package com.example.chatme.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.chatme.Activity.EditName;
import com.example.chatme.Constants.AllConstants;
import com.example.chatme.Permissions.Permissions;
import com.example.chatme.R;
import com.example.chatme.UserModel;
import com.example.chatme.Utils.Util;
import com.example.chatme.ViewModel.ProfileViewModel;
import com.example.chatme.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel profileViewModel;
    private Uri imageUri;
    private Util util;
    private Permissions permissions;
    private AlertDialog alertDialog;
    private UserModel user;
    private SharedPreferences.Editor sharedPreferences;


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        util = new Util();
        sharedPreferences = getContext().getSharedPreferences("UserData", Context.MODE_PRIVATE).edit();
        permissions = new Permissions();
        profileViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication()).create(ProfileViewModel.class);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Profile");

        Observer<UserModel> observer = new Observer<UserModel>() {
            @Override
            public void onChanged(UserModel userModel) {

                binding.setUserModel(userModel);
                user = userModel;
                String name = userModel.getName();
                if (name.contains(" ")) {
                    String[] split = name.split(" ");
                    binding.txtProfileFName.setText(split[0]);
                    binding.txtProfileLName.setText(split[1]);
                } else {
                    binding.txtProfileFName.setText(name);
                    binding.txtProfileLName.setText("");
                }

            }
        };
        profileViewModel.getUser().observe(getViewLifecycleOwner(), observer);
        binding.imgPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissions.isStorageOk(getContext()))
                    pickImage();
                else permissions.requestStorage(getActivity());
            }
        });

        binding.imgEditStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                View view1 = LayoutInflater.from(getContext()).inflate(R.layout.dialog_layout, null);
                builder.setView(view1);

                final EditText edtStatus = view1.findViewById(R.id.edtUserStatus);
                Button btnEditStatus = view1.findViewById(R.id.btnEditStatus);

                btnEditStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String status = edtStatus.getText().toString().trim();
                        if (!status.isEmpty()) {
                            profileViewModel.editStatus(status);
                            alertDialog.dismiss();

                        }
                    }
                });

                alertDialog = builder.create();
                alertDialog.show();

            }
        });

        binding.cardName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!user.getName().isEmpty()) {
                    Intent intent = new Intent(getContext(), EditName.class);
                    intent.putExtra("name", user.getName());
                    startActivityForResult(intent, AllConstants.USERNAME_CODE);
                } else {
                    Intent intent = new Intent(getContext(), EditName.class);
                    startActivityForResult(intent, AllConstants.USERNAME_CODE);
                }
            }
        });


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                if (data != null) {
                    CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    if (resultCode == Activity.RESULT_OK) {
                        imageUri = result.getUri();
                        uploadImage(imageUri);
                    } else {
                        Log.d("image", "onActivityResult: " + result.getError());
                    }
                }
                break;
            case AllConstants.USERNAME_CODE:

                if (data != null) {

                    String name = data.getStringExtra("name");
                    profileViewModel.edtUsername(name);
                    sharedPreferences.putString("username", name).apply();

                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case AllConstants.STORAGE_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    pickImage();
                else
                    Toast.makeText(getContext(), "Storage Permission rejected.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void pickImage() {
        CropImage.activity()
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(getContext(), ProfileFragment.this);
    }

    private void uploadImage(Uri imageUri) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(util.getUID()).child(AllConstants.IMAGE_PATH);
        storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                task.addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        String uri = task.getResult().toString();
                        profileViewModel.editImage(uri);
                        sharedPreferences.putString("userImage", uri).apply();

                    }
                });
            }
        });
    }
}