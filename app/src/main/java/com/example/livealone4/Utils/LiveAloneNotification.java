package com.example.livealone4.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.example.livealone4.Activities.MainActivity;
import com.example.livealone4.Activities.SigninActivity;
import com.example.livealone4.R;
import com.example.livealone4.Services.LiveAloneService;

public class LiveAloneNotification extends Notification {

    //싱글톤
    private static NotificationManager notificationManager;
    private static int MESSAGE_TAG = 1000;

    private LiveAloneNotification(){}

    public static void notifyNewMessage(Context context, String content){
        if(notificationManager == null)
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("uid", LiveAloneService.getUid());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);

        Notification notification = notificationBuilder
                .setContentInfo("같이먹기")
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.livealone1)
                .setContentIntent(pendingIntent).build();

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, SigninActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(contentIntent);

        notificationManager.notify(MESSAGE_TAG, notification);

    }


}
