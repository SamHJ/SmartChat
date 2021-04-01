package niwigh.com.smartchat.Notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

import niwigh.com.smartchat.Activity.MessagingArea;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Activity.UserHome;

public class MyFirebaseMessagingService extends FirebaseMessagingService {



    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String visit_user_id = remoteMessage.getData().get("visit_user_id");
        SharedPreferences sharedPreferences = getSharedPreferences("NOTIFICATIONPREFS",MODE_PRIVATE);
        String messageReceiverID = sharedPreferences.getString("messageReceiverID",null);

        String sender = remoteMessage.getData().get("sender");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        FirebaseUser firebaseUser = mAuth.getCurrentUser();

//        if(firebaseUser != null && sender.equals(firebaseUser.getUid())){
//
//            sendNotification(remoteMessage,notificationManager);
//        }

        //used to show notifications
        //that is check if the user has enabled notifications
        //if true then show the notifications
        //else do nothing
        SharedPreferences pref = this.getSharedPreferences("disablenotifications", 0);
        Boolean isnotificationsavailable = pref.getBoolean("isnotificationsavailable", true);
        if(isnotificationsavailable){
        if(firebaseUser != null){
            if(!messageReceiverID.equals(visit_user_id)){
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    sendOreoNotification(remoteMessage);
                }else {
                    sendNotification(remoteMessage, notificationManager);
                }
            }

        }

        }


    }

    private void sendOreoNotification(RemoteMessage remoteMessage) {
        String visit_user_id = remoteMessage.getData().get("visit_user_id");
        String userName = remoteMessage.getData().get("userName");
        String userFullName = remoteMessage.getData().get("userFullName");
        String senderId = remoteMessage.getData().get("sender_id");

        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("message");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = returnInteger(visit_user_id);
        Intent intent = returnIntent(visit_user_id);


        Bundle bundle = new Bundle();
        bundle.putString("visit_user_id",visit_user_id);
        bundle.putString("userName", userName);
        bundle.putString("userFullName", userFullName);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j , intent, PendingIntent.FLAG_ONE_SHOT);

        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        OreoNotification oreoNotification = new OreoNotification(this);
        Notification.Builder builder = oreoNotification.getOreoNotification(title, body, pendingIntent,notificationSoundUri);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            builder.setSmallIcon(R.drawable.ic_messages);
            builder.setColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        int i = 0;
        if(j > 0){
            i = j;
        }

        oreoNotification.getManager().notify(i, builder.build());
    }


    public int returnInteger(String visit_user_id){
        if(visit_user_id.equals("friendrequest")){
            return  new Random().nextInt(3000);
        }else {
            return Integer.parseInt(visit_user_id.replaceAll("[\\D]", ""));
        }
    }


    public Intent  returnIntent(String visit_user_id){

        if(visit_user_id.equals("friendrequest")){
            return new Intent(this, UserHome.class).putExtra("requestsFragment","requestsFragment");
        }else {
            return new Intent(this, MessagingArea.class);
        }
    }

    private void sendNotification(RemoteMessage remoteMessage, NotificationManager notificationManager) {
        String visit_user_id = remoteMessage.getData().get("visit_user_id");
        String userName = remoteMessage.getData().get("userName");
        String userFullName = remoteMessage.getData().get("userFullName");
        String senderId = remoteMessage.getData().get("sender_id");

        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("message");

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.notifications_icon);

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int j = returnInteger(visit_user_id);
        Intent intent = returnIntent(visit_user_id);

        Bundle bundle = new Bundle();
        bundle.putString("visit_user_id",visit_user_id);
        bundle.putString("userName", userName);
        bundle.putString("userFullName", userFullName);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, j , intent, PendingIntent.FLAG_ONE_SHOT);

        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_messages)
                .setLargeIcon(largeIcon)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(notificationSoundUri)
                .setContentIntent(pendingIntent);



        //Set notification color to match your app color template
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            notificationBuilder.setColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        int i = 0;
        if(j > 0){
            i = j;
        }

        notificationManager.notify(i, notificationBuilder.build());
    }

}