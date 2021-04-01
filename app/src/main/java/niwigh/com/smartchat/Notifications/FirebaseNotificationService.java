package niwigh.com.smartchat.Notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import niwigh.com.smartchat.R;

public class FirebaseNotificationService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String msgTitle = remoteMessage.getNotification().getTitle();
        String msgBody = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();
        String dataMessage = remoteMessage.getData().get("message");
        String dataFrom = remoteMessage.getData().get("from_user_id");
        String dataTo = remoteMessage.getData().get("visit_user_id");
        String to_name = remoteMessage.getData().get("userFullName");
        String to_username = remoteMessage.getData().get("userName");


        Uri default_sound_uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(msgTitle)
                        .setContentText(msgBody)
                        .setSound(default_sound_uri);

        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("visit_user_id", dataTo);
        resultIntent.putExtra("userName", to_username);
        resultIntent.putExtra("userFullName", to_name);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);

        int mNotificationId = (int) System.currentTimeMillis();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(mNotificationId, builder.build());
    }
}