package com.example.chatme.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.chatme.GroupMessageModel;
import com.example.chatme.MessageModel;
import com.example.chatme.R;
import com.example.chatme.Utils.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.util.ArrayList;

public class SendMediaService extends Service {

    private NotificationCompat.Builder builder;
    private NotificationManager manager;
    private String hisID, chatID, groupId;
    private int MAX_PROGRESS;
    private final com.example.chatme.Utils.Util util = new Util();
    private ArrayList<String> images;


    public SendMediaService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        images = intent.getStringArrayListExtra("media");
        MAX_PROGRESS = images.size();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            createChannel();

        startForeground(100, getNotification().build());

        if (intent.hasExtra("type")) {

            groupId = intent.getStringExtra("groupId");
            sendGroupImage();
        } else {

            hisID = intent.getStringExtra("hisID");
            chatID = intent.getStringExtra("chatID");

            for (int a = 0; a < images.size(); a++) {

                String fileName = compressImage(images.get(a));
                uploadImage(fileName);
                builder.setProgress(MAX_PROGRESS, a + 1, false);
                manager.notify(600, builder.build());

            }
        }


        builder.setContentTitle("Sending Completed")
                .setProgress(0, 0, false);
        manager.notify(600, builder.build());
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    private NotificationCompat.Builder getNotification() {

        builder = new NotificationCompat.Builder(this, "android")
                .setContentText("Sending Media")
                .setProgress(MAX_PROGRESS, 0, false)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher_foreground);

        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(600, builder.build());
        return builder;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {

        NotificationChannel channel = new NotificationChannel("android", "Message", NotificationManager.IMPORTANCE_HIGH);
        channel.setShowBadge(true);
        channel.setLightColor(R.color.colorPrimary);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        channel.setDescription("Sending Media");
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

    }

    private String compressImage(String fileName) {

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "ChatMe/Media/");
        if (!file.exists())
            file.mkdirs();

        return SiliCompressor.with(this).compress(fileName, file, false);
    }


    private void uploadImage(String fileName) {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(chatID + "/Media/Images/" + util.getUID() + "/" + System.currentTimeMillis());
        Uri uri = Uri.fromFile(new File(fileName));
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> task = taskSnapshot.getStorage().getDownloadUrl();
                task.addOnCompleteListener(uri -> {
                    if (uri.isSuccessful()) {
                        String url = uri.getResult().toString();

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chat").child(chatID);
                        MessageModel messageModel = new MessageModel(util.getUID(), hisID, url, util.currentData(), "image");
                        databaseReference.push().setValue(messageModel);
                    }
                });
            }
        });
    }

    private void sendGroupImage() {

        for (int a = 0; a < images.size(); a++) {

            String fileName = compressImage(images.get(a));

            uploadGroupImage(fileName);
            builder.setProgress(MAX_PROGRESS, a + 1, false);
            manager.notify(600, builder.build());
        }
    }

    private void uploadGroupImage(String fileName) {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference(groupId + "/Media/Images/"
                + util.getUID() + "/" + System.currentTimeMillis());
        Uri uri = Uri.fromFile(new File(fileName));
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> image = taskSnapshot.getStorage().getDownloadUrl();
                image.addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        String url = task.getResult().toString();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Group Message")
                                .child(groupId);
                        GroupMessageModel messageModel = new GroupMessageModel("image", url,
                                String.valueOf(System.currentTimeMillis()), util.getUID());

                        reference.push().setValue(messageModel);


                    }

                });

            }
        });
    }
}
