package com.example.hospiton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements request{

    private String receiveruserid,Current_State,sendUserID;
    private CircleImageView User_profile_image;
    private TextView Username,UserStatus;
    private Button SendRequest,Cancel;
    private RequestQueue requestQueue;
    private String url_imp="https://fcm.googleapis.com/fcm/send";
    private DatabaseReference Ref,ChatRequest,ContactsRef, NotificationsRef,userref;
    private FirebaseAuth mAuth;
    private String username;
    private  String url_image;
    private request mcallback;
    private String usercontact="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mcallback=this;

        mAuth=FirebaseAuth.getInstance();
        sendUserID=mAuth.getCurrentUser().getUid();

        Ref= FirebaseDatabase.getInstance().getReference();
        ChatRequest=FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationsRef=FirebaseDatabase.getInstance().getReference().child("Notifications");
        userref=FirebaseDatabase.getInstance().getReference().child("Users");

        FirebaseMessaging.getInstance().subscribeToTopic(mAuth.getCurrentUser().getUid());
        
        requestQueue= Volley.newRequestQueue(this);

        receiveruserid=getIntent().getExtras().get("visit_user_id").toString();
        username=getIntent().getStringExtra("User_name");
        url_image=getIntent().getStringExtra("Image");

        Log.d("Image",url_image);

        User_profile_image=(CircleImageView)findViewById(R.id.visit_profile_image);
        Username=(TextView)findViewById(R.id.visit_user_name);
        UserStatus=(TextView)findViewById(R.id.visit_profile_status);
        SendRequest=(Button)findViewById(R.id.send_message_button);
        Cancel=(Button)findViewById(R.id.decline_message_button);

        Current_State="new";



        RetreiveUserInfo();

    }

    private void RetreiveUserInfo() {
        Ref.child("Users").child(receiveruserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.hasChild("image"))
                {
                    Username.setText(dataSnapshot.child("name").getValue().toString());
                    Picasso.get().load(dataSnapshot.child("image").getValue().toString()).placeholder(R.drawable.profile_image).into(User_profile_image);
                    usercontact=dataSnapshot.child(getString(R.string.contact)).getValue().toString();
                    ManageChatRequest();
                }
                else {
                    Username.setText(dataSnapshot.child("name").getValue().toString());
                    usercontact=dataSnapshot.child(getString(R.string.contact)).getValue().toString();
                    ManageChatRequest();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequest() {

        ChatRequest.child(sendUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(receiveruserid))
                {
                    String RequestType=dataSnapshot.child(receiveruserid).child("request_type").getValue().toString();

                    if(RequestType.equals("sent"))
                    {
                        Current_State="request_sent";
                        SendRequest.setText("Cancel Chat Request");
                        UserStatus.setVisibility(View.INVISIBLE);
                    }
                    else if(RequestType.equals("received"))
                    {
                        Current_State="request_received";
                        SendRequest.setText("Accept Request");
                        Cancel.setVisibility(View.VISIBLE);
                        Cancel.setEnabled(true);
                        UserStatus.setVisibility(View.INVISIBLE);
                        Cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CanceRequest();
                            }
                        });
                    }
                }
                else
                {
                    ContactsRef.child(sendUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(receiveruserid))
                            {
                                Current_State="friends";
                                SendRequest.setText("Unfriend");
                                mcallback.request_callback(usercontact);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(!sendUserID.equals(receiveruserid))
        {
            SendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendRequest.setEnabled(false);

                    if(Current_State.equals("new"))
                    {
                        SendChatRequest();
                    }
                    else if(Current_State.equals("request_sent"))
                    {
                        CanceRequest();
                    }
                    else if(Current_State.equals("request_received"))
                    {
                        AcceptChatRequest();
                    }
                    else if(Current_State.equals("friends"))
                    {
                        Unfriend();
                    }
                }
            });
        }
        else
        {
            SendRequest.setVisibility(View.INVISIBLE);
        }

    }

    private void Unfriend() {
        ContactsRef.child(sendUserID).child(receiveruserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    ContactsRef.child(receiveruserid).child(sendUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                SendRequest.setEnabled(true);
                                SendRequest.setText("Send Request");
                                Current_State="new";
                                Cancel.setVisibility(View.INVISIBLE);
                                Cancel.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void AcceptChatRequest() {
        ContactsRef.child(sendUserID).child(receiveruserid).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    ContactsRef.child(receiveruserid).child(sendUserID).child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                ChatRequest.child(sendUserID).child(receiveruserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            ChatRequest.child(receiveruserid).child(sendUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        SendRequest.setEnabled(true);
                                                        Current_State="friends";
                                                        SendRequest.setText("Unfriend");

                                                        Cancel.setVisibility(View.INVISIBLE);
                                                        Cancel.setEnabled(false);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void CanceRequest() {
        ChatRequest.child(sendUserID).child(receiveruserid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    ChatRequest.child(receiveruserid).child(sendUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                SendRequest.setEnabled(true);
                                SendRequest.setText("Send Request");
                                Current_State="new";
                                Cancel.setVisibility(View.INVISIBLE);
                                Cancel.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void SendChatRequest() {
        sendnotification();
        ChatRequest.child(sendUserID).child(receiveruserid)
                .child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    ChatRequest.child(receiveruserid).child(sendUserID)
                            .child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {

                                HashMap<String,String>chat_notification=new HashMap<>();
                                chat_notification.put("from",sendUserID);
                                chat_notification.put("type","request");

                                NotificationsRef.child(receiveruserid).push().setValue(chat_notification).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            SendRequest.setEnabled(true);
                                            Current_State="request_sent";
                                            SendRequest.setText("Cancel Chat Request");
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendnotification() {
        JSONObject mainobj=new JSONObject();
        try {
            mainobj.put("to","/topics/"+receiveruserid);
            JSONObject notification=new JSONObject();
            notification.put("title","Friend Request");
            notification.put("body","You have friend Request From "+username);
            notification.put("icon",url_image);
            mainobj.put("notification",notification);

            JsonObjectRequest request=new JsonObjectRequest(Request.Method.POST, url_imp,
                    mainobj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> header=new HashMap<>();
                    header.put("content-type","application/json");
                    header.put("authorization","key=AIzaSyDMYebxipz5x6KH_iSOe25G6TFz_O54FVo");
                    return header;
                }
            };

            requestQueue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void request_callback(String UserContact) {
        UserStatus.setVisibility(View.VISIBLE);
        UserStatus.setText("Contact Number:- "+UserContact);
    }
}
