package com.example.chatme.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatme.Constants.AllConstants;
import com.example.chatme.Permissions.Permissions;
import com.example.chatme.R;
import com.example.chatme.databinding.FragmentGetGroupDetailBinding;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class GetGroupDetailFragment extends Fragment {

    private FragmentGetGroupDetailBinding binding;
    private Permissions appPermission;
    private Uri imageUri;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGetGroupDetailBinding.inflate(inflater, container, false);
        appPermission = new Permissions();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Group Detail");
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        binding.btnDone.setOnClickListener(done -> {

            String groupName = binding.edtGroupName.getText().toString().trim();
            if (groupName.isEmpty()) {
                binding.edtGroupName.setError("Filed is required");
                binding.edtGroupName.requestFocus();
            } else {
                if (imageUri == null) {
                    Toast.makeText(requireContext(), "Select Group Image", Toast.LENGTH_SHORT).show();
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("GroupName", groupName);
                    bundle.putString("GroupImage", imageUri.toString());
                    GroupMemberFragment memberFragment = new GroupMemberFragment();
                    memberFragment.setArguments(bundle);
                    getFragmentManager().beginTransaction().replace(R.id.groupContainer, memberFragment)
                            .commit();

                }
            }
        });

        binding.imgPickImage.setOnClickListener(pick -> {
            if (appPermission.isStorageOk(requireContext())) {
                pickImage();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, AllConstants.STORAGE_REQUEST_CODE);
            }
        });
    }


    private void pickImage() {
        CropImage.activity()
                .setCropShape(CropImageView.CropShape.OVAL)
                .start(requireContext(), this);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK) {
                imageUri = result.getUri();
                Glide.with(requireContext()).load(imageUri).into(binding.imgGroup);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AllConstants.STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            } else {
                Toast.makeText(requireContext(), "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}