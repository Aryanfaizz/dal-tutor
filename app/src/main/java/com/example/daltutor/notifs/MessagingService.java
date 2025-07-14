package com.example.daltutor.notifs;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.daltutor.R;
import com.example.daltutor.ui.SessionDetailsActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d("Notification", "Message Received");
        assert message.getNotification() != null;
        final String title = message.getNotification().getTitle();
        final String body = message.getNotification().getBody();

        final Map<String, String> data = message.getData();
        final String postingId = data.get("postingID");

        Intent intent = new Intent(this, SessionDetailsActivity.class);
        intent.putExtra("POSTING_ID", postingId);

       PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 10, intent,  PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, data.get("tutor"))
                        .setSmallIcon(R.drawable.dtlogo)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationBuilder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        int id = (int) System.currentTimeMillis();
        if (notificationManager == null) {
            Log.e("NotificationError", "NotificationManager is null.");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(data.get("tutor"), data.get("tutor"), NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(id, notificationBuilder.build());
    }
}
