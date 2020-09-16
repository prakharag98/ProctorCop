package com.example.hospiton;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMessagingServce";
    private static final String ChannelId="Friend_Request";
    private static final int notification_id=1134;
    private static final int Gotoreq = 3417;


    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);



        AsyncTask<Void,Void,Void>asyncTask=new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                String notificationTitle = null, notificationBody = null;
                Bitmap image=null;
                if (remoteMessage.getNotification() != null) {
                    Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
                    notificationTitle = remoteMessage.getNotification().getTitle();
                    notificationBody = remoteMessage.getNotification().getBody();
                    String icon=null;
                    icon=remoteMessage.getNotification().getIcon();

                    Log.d("Service",icon);
                    Log.d("Data","Message Data payload" + remoteMessage.getData());

                    image=getBitmapfromUrl(icon);
                }

                // Also if you intend on generating your own notifications as a result of a received FCM
                // message, here is where that should be initiated. See sendNotification method below.
                sendNotification(notificationTitle, notificationBody,image);
                return null;
            }
        };

        asyncTask.execute();
        // Check if message contains a notification payload.
    }

    private void sendNotification(String notificationTitle, String notificationBody,Bitmap image) {

        NotificationManager notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            NotificationChannel mchannel=new NotificationChannel(ChannelId,"Primary",NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mchannel);
        }

        Uri alarmSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        NotificationCompat.Builder builder=new NotificationCompat.Builder(this,ChannelId)
                .setColor(ContextCompat.getColor(this,R.color.colorPrimary))
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setSound(alarmSound)
                .setContentIntent(reqfragment(this));

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.logo);
            builder.setLargeIcon(image);
            builder.setColor(getResources().getColor(R.color.colorPrimary));
        } else {
             builder.setSmallIcon(R.drawable.logo);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
        notificationManager.notify(notification_id, builder.build());

    }

    private Bitmap largeicon(Context context) {
        Resources res = context.getResources();
        Bitmap largeIcon = BitmapFactory.decodeResource(res, R.drawable.logo);
        return largeIcon;
    }

    public Bitmap getBitmapfromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);

        } catch (Exception e) {
            Log.e("awesome", "Error in getting notification image: " + e.getLocalizedMessage());
            return null;
        }
    }

    private PendingIntent reqfragment(Context context)
    {
        Intent startintent=new Intent(this,MainActivity.class);

        return PendingIntent.getActivity(context,Gotoreq,startintent,PendingIntent.FLAG_UPDATE_CURRENT);

    }
}
