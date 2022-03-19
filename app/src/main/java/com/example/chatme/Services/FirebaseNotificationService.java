package com.example.chatme.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.chatme.Constants.AllConstants;
import com.example.chatme.MessageActivity;
import com.example.chatme.R;
import com.example.chatme.Utils.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FirebaseNotificationService extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0) {
            Map<String, String> map = remoteMessage.getData();

            String type = map.get("type");
            if (type != null && type.equals("group")) {
                Log.d("TAG", "onMessageReceived: Group Notification");

                String title = map.get("title");
                String message = map.get("message");
                String groupId = map.get("groupId");

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
                    createGroupOreoNotification(title, message, groupId);
                else
                    createGroupNormalNotification(title, message, groupId);

            } else {

                Log.d("TAG", "onMessageReceived: Chat Notification");

                String title = map.get("title");
                String message = map.get("message");
                String hisID = map.get("hisID");
                String hisImage = map.get("hisImage");
                String chatID = map.get("chatID");

                Log.d("TAG", "onMessageReceived: chatID is " + chatID + "\n hisID" + hisID);

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
                    createOreoNotification(title, message, hisID, hisImage, chatID);
                else
                    createNormalNotification(title, message, hisID, hisImage, chatID);
            }
        } else Log.d("TAG", "onMessageReceived: no data ");


        super.onMessageReceived(remoteMessage);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        updateToken(s);
        super.onNewToken(s);
    }

    private void updateToken(String token) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseAuth.getUid());
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            databaseReference.updateChildren(map);
        }

    }


    private void createNormalNotification(String title, String message, String hisID, String hisImage, String chatID) {

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, AllConstants.CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null))
                .setSound(uri);

        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra("chatID", chatID);
        intent.putExtra("hisID", hisID);
        intent.putExtra("hisImage", hisImage);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(new Random().nextInt(85 - 65), builder.build());

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createOreoNotification(String title, String message, String hisID, String hisImage, String chatID) {

        NotificationChannel channel = new NotificationChannel(AllConstants.CHANNEL_ID, "Message", NotificationManager.IMPORTANCE_HIGH);
        channel.setShowBadge(true);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra("hisID", hisID);
        intent.putExtra("hisImage", hisImage);
        intent.putExtra("chatID", chatID);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Notification notification = new Notification.Builder(this, AllConstants.CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        manager.notify(100, notification);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createGroupOreoNotification(String title, String message, String groupID) {

        NotificationChannel channel = new NotificationChannel(AllConstants.CHANNEL_ID, "Message", NotificationManager.IMPORTANCE_HIGH);
        channel.setShowBadge(true);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

//        Intent intent = new Intent(this, MessageActivity.class);
//        intent.putExtra("hisID", hisID);
//        intent.putExtra("hisImage", hisImage);
//        intent.putExtra("chatID", chatID);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Notification notification = new Notification.Builder(this, AllConstants.CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
//                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        manager.notify(100, notification);

    }

    private void createGroupNormalNotification(String title, String message, String groupId) {

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, AllConstants.CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true)
                .setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null))
                .setSound(uri);

//        Intent intent = new Intent(this, MessageActivity.class);
//        intent.putExtra("chatID", chatID);
//        intent.putExtra("hisID", hisID);
//        intent.putExtra("hisImage", hisImage);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

//        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(new Random().nextInt(85 - 65), builder.build());

    }

}
