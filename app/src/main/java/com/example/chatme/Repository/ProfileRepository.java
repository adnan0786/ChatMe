package com.example.chatme.Repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.chatme.UserModel;
import com.example.chatme.Utils.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileRepository {


    private DatabaseReference databaseReference;
    private Util util = new Util();
    private MutableLiveData<UserModel> liveData;
    private static ProfileRepository profileRepository;

    public static ProfileRepository getInstance() {
        return profileRepository = new ProfileRepository();
    }

    public LiveData<UserModel> getUser() {
        if (liveData == null) {
            liveData = new MutableLiveData<>();
            databaseReference = FirebaseDatabase.getInstance().getReference("Users");
            databaseReference.child(util.getUID()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        UserModel userModel = dataSnapshot.getValue(UserModel.class);
                        liveData.setValue(userModel);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        return liveData;

    }

    public void editImage(final String uri) {
        final UserModel userModel = liveData.getValue();

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(util.getUID());
        Map<String, Object> map = new HashMap<>();
        map.put("image", uri);
        databaseReference.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    userModel.setImage(uri);
                    liveData.setValue(userModel);
                    Log.d("image", "onComplete: Image updated");
                } else Log.d("image", "onComplete: " + task.getException());
            }
        });
    }

    public void editStatus(final String status) {
        final UserModel userModel = liveData.getValue();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(util.getUID());
        Map<String, Object> map = new HashMap<>();
        map.put("status", status);
        databaseReference.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    userModel.setStatus(status);
                    liveData.setValue(userModel);
                    Log.d("status", "onComplete: Status Updated");
                } else Log.d("status", "onComplete: " + task.getException());
            }
        });
    }

    public void editUsername(final String name) {
        final UserModel userModel = liveData.getValue();

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(util.getUID());
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        databaseReference.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    userModel.setName(name);
                    liveData.setValue(userModel);
                    Log.d("name", "onComplete: Name Updated");
                } else Log.d("name", "onComplete:" + task.getException());
            }
        });
    }
}
