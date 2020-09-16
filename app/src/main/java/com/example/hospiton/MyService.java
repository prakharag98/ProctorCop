package com.example.hospiton;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyService extends Service {
    private static final String ChannelId="It's a match id";
    private static final int notification_id=1134;
    boolean variable=true;
    private FirebaseAuth mAuth;
    private DatabaseReference contactsRef,userref,receiverref;
    private String Destination1,Destination2;
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        onTaskRemoved(intent);

        if(variable) {

            mAuth = FirebaseAuth.getInstance();
            String CurrentUserId = mAuth.getCurrentUser().getUid();
            contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(CurrentUserId);
            userref = FirebaseDatabase.getInstance().getReference();
            receiverref = FirebaseDatabase.getInstance().getReference();

            userref.child("Users").child(CurrentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("Destination")) {
                        Destination1 = dataSnapshot.child("Destination").getValue().toString();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            contactsRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    String receiverid = dataSnapshot.getKey();
                    Log.d("Service", receiverid);

                    receiverref.child("Users").child(receiverid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild("Destination")) {
                                Destination2 = dataSnapshot.child("Destination").getValue().toString();

                                Log.d("Destination1", Destination1);
                                Log.d("Destination2", Destination2);

                                if (Destination1.equals(Destination2)) {
                                    buildnotification();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            variable=false;
        }



        return START_NOT_STICKY;
    }

    public void buildnotification()
    {
        NotificationManager notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            NotificationChannel mchannel=new NotificationChannel(ChannelId,"Primary",NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mchannel);
        }

        Uri alarmSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        NotificationCompat.Builder builder=new NotificationCompat.Builder(this,ChannelId)
                .setColor(ContextCompat.getColor(this,R.color.colorPrimary))
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("It's a Match !!!")
                .setContentText("You can now go with your friend")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("You can now go with your friend"))
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setSound(alarmSound);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
        notificationManager.notify(notification_id, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
