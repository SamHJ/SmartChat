package niwigh.com.smartchat.Notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

import niwigh.com.smartchat.R;

public class OreoNotification extends ContextWrapper {

    private static final String CHANNEL_ID = "havotechstudios.com.smartmessenger";
    private static final String CHANNEL_NAME = "smartmessenger";

    private NotificationManager notificationManager;

    public OreoNotification(Context base) {
        super(base);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void setupChannels(){

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        adminChannel.enableLights(false);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        adminChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(adminChannel);
    }

    public  NotificationManager getManager(){
        if(notificationManager == null){
            notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return  notificationManager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getOreoNotification( String title, String body, PendingIntent pendingIntent,
                                                     Uri notificationSoundUri){
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.notifications_icon);

        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_messages)
                .setLargeIcon(largeIcon)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(notificationSoundUri)
                .setContentIntent(pendingIntent);

    }
}