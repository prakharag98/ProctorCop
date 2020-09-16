package com.example.hospiton;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;


import androidx.appcompat.widget.Toolbar;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;


import android.telephony.SmsManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements MyCallback{
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private Toolbar toolbar;
    private DatabaseReference Rootref,Dangerref;
    private GoogleSignInClient mGoogleSignInClient;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private TextView sos_text,message;
    private Drawable d;
    private Animation fade;
    private LocationManager locationManager;
    private MyCallback myCallback;
    private TextView gotolink;
    private String Username;
    private String userimage;
    private MediaPlayer mediaPlayer;
    private int length;

    private RelativeLayout relativeLayout;

    private static final int Permission_All = 1;
    private String url_imp="https://fcm.googleapis.com/fcm/send";
    private RequestQueue requestQueue;
    String Google="https://www.google.com";
    String gotogogle="Google";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        requestQueue= Volley.newRequestQueue(this);
        FirebaseApp.initializeApp(this);
        myCallback=this;
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        Rootref=FirebaseDatabase.getInstance().getReference();
        Dangerref=FirebaseDatabase.getInstance().getReference();
        sos_text=(TextView)findViewById(R.id.sos_button);
        d=getResources().getDrawable(R.drawable.circle3);
        message=(TextView)findViewById(R.id.message);
        fade= AnimationUtils.loadAnimation(this,R.anim.fade);
        mediaPlayer=MediaPlayer.create(this,R.raw.emergency);
        length=mediaPlayer.getCurrentPosition();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        relativeLayout=(RelativeLayout)findViewById(R.id.main_back);

        drawerLayout=findViewById(R.id.drawer_layout);
        navigationView=findViewById(R.id.navigation_view);

        toolbar=(Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder()
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);


        String[] Permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
                , Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE};

        if (!haspermission(this, Permissions)) {
            ActivityCompat.requestPermissions(this, Permissions, Permission_All);
        }

        subscribetotopic();


        setnavigation();
        onClicklistener();




    }

    private void subscribetotopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.app_name)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
               Log.d("Main Activity", "Topic Subscribed");
            }
        });
    }

    private void sendsms() {
        String CurrentUserid=firebaseAuth.getCurrentUser().getUid();

        final String[] Destination = {""};
        final String[] Phonenumber1 = {""};
        final String[] Phonenumber2 = {""};
        final String[] Phonenumber3 = {""};

        Rootref.child(getString(R.string.Users)).child(CurrentUserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(getString(R.string.alias_contact1)) && dataSnapshot.hasChild("Destination") && dataSnapshot.hasChild("image"))
                {
                    Log.d("karan","Inside Fetching");
                    Phonenumber1[0] =dataSnapshot.child(getString(R.string.alias_contact1)).getValue().toString().replace("\\s","");;
                    Phonenumber2[0] =dataSnapshot.child(getString(R.string.alias_contact2)).getValue().toString().replace("\\s","");;
                    Phonenumber3[0] =dataSnapshot.child(getString(R.string.alias_contact3)).getValue().toString().replace("\\s","");;
                    Destination[0]=dataSnapshot.child("Destination").getValue().toString();
                    Username=dataSnapshot.child("name").getValue().toString();
                    userimage=dataSnapshot.child("image").getValue().toString();
                    myCallback.onCallbackk(Phonenumber1[0],Phonenumber2[0],Phonenumber3[0],Destination[0]);
                }
                else{
                    Toast.makeText(MainActivity.this,"Set Your Destination and profile image",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private boolean haspermission(Context context, String... permissions) {

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && context!=null && permissions!=null)
        {
            for (String Permission:permissions)
            {
                if(ActivityCompat.checkSelfPermission(context,Permission)!= PackageManager.PERMISSION_GRANTED)
                {
                    return false;
                }
            }
        }
        return true;
    }

    private void onClicklistener() {
        sos_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mediaPlayer.start();
                sos_text.setBackground(d);
                sos_text.setTextColor(getResources().getColor(R.color.change_background));
                message.setTextColor(getResources().getColor(R.color.white));
                relativeLayout.setBackgroundColor(getResources().getColor(R.color.change_background));
                relativeLayout.startAnimation(fade);
                sendsms();

            }
        });
    }

    private void setnavigation() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId())
                {
                    case R.id.item1:
                        menuItem.setChecked(true);
                        //displayMessage("item 1");
                        drawerLayout.closeDrawers();
                        return true;

                    case R.id.item2:
                        menuItem.setChecked(true);
                        //displayMessage("Item 2");
                        drawerLayout.closeDrawers();
                        return true;

                    case R.id.item3:
                        menuItem.setChecked(true);
                        //displayMessage("Item 2");
                        drawerLayout.closeDrawers();
                        return true;

                    case R.id.item4:
                        menuItem.setChecked(true);
                        //displayMessage("Item 2");
                        drawerLayout.closeDrawers();
                        return true;

                }
                return false;
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.options_menu,menu);
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();

        if(id==R.id.signout)
        {
          firebaseAuth.signOut();
          mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {
                  Toast.makeText(MainActivity.this,"Signed out successfully",Toast.LENGTH_SHORT).show();
              }
          });

          Intent intent=new Intent(MainActivity.this,LoginActivity.class);
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
          startActivity(intent);

        }
        else if(id==R.id.profile)
        {
            Intent intent=new Intent(MainActivity.this,User_Profile.class);
            startActivity(intent);
        }
        else if(id==R.id.destination)
        {
            Intent intent=new Intent(MainActivity.this,Destination.class);
            startActivity(intent);
        }
        else if(id==R.id.Find_Friends)
        {
            Intent intent=new Intent(MainActivity.this,FriendsActivity.class);
            startActivity(intent);
        }
        else if(id==R.id.camera)
        {
            Intent intent=new Intent(MainActivity.this,textrecog.class);
            startActivity(intent);
        }
        else if(id==R.id.contacts)
        {
            Intent intent=new Intent(MainActivity.this,Contacts_Fragment.class);
            startActivity(intent);
        }
        else if(id==R.id.Requests)
        {
            Intent intent=new Intent(MainActivity.this,Request_Fragment.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {


        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        Log.d("Main", "On Back Pressed");

    }

    @Override
    protected void onStart() {
        super.onStart();

           if(firebaseAuth.getCurrentUser()!=null)
           {
              if(firebaseAuth.getCurrentUser().isEmailVerified())
              {
                 verifyexistence();
              }
              else
              {
                  if(firebaseAuth.getCurrentUser().getPhoneNumber().isEmpty()) {
                      Log.d("TAG", "Else invoked");
                      Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                      startActivity(intent);
                  }else {
                        verifyexistence();
                  }
              }
           }
           else
           {
               Intent intent=new Intent(MainActivity.this,LoginActivity.class);
               intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
               startActivity(intent);
           }


    }

    private void verifyexistence() {
       String userid=firebaseAuth.getCurrentUser().getUid();
       Rootref.child(getString(R.string.Users)).child(userid).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if(dataSnapshot.child(getString(R.string.name)).exists())
               {
                 Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_SHORT).show();
                   Dangerref.child("Danger").addValueEventListener(new ValueEventListener() {
                       @Override
                       public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                           if(dataSnapshot.exists())
                           {
                               LayoutInflater inflater=getLayoutInflater();
                               View convert=(View)inflater.inflate(R.layout.danger_text,null);
                               gotolink=(TextView)convert.findViewById(R.id.link_textview);
                               String latitude=dataSnapshot.child("latitude").getValue().toString();
                               String longitude=dataSnapshot.child("longitude").getValue().toString();
                               Google="https://www.google.com/maps/search/?api=1&query="+latitude+","+longitude;
                               gotogogle="Track him";
                               gotolink.setText(Html.fromHtml("<a href=\""+ Google + "\">" + gotogogle + "</a>"));
                               gotolink.setClickable(true);
                               gotolink.setMovementMethod(LinkMovementMethod.getInstance());

                               AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                               builder.setView(convert);
                               builder.setTitle("Danger!!");
                               builder.setCancelable(true);

                               AlertDialog alertDialog=builder.create();
                               alertDialog.show();

                           }
                       }

                       @Override
                       public void onCancelled(@NonNull DatabaseError databaseError) {

                       }
                   });
               }
               else
               {
                   Intent intent=new Intent(MainActivity.this,User_Profile.class);
                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                   startActivity(intent);
                   finish();
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });
    }

    @Override
    public void onCallbackk(String phone1, String phone2, String phone3,String Destination) {
        Toast.makeText(this,String.valueOf(phone1.replaceAll("\\s","").length()),Toast.LENGTH_LONG).show();

        Double latitude=0.0;
        Double longitude=0.0;
        String Message="";

        LocationListener locationListener = new MyLocationListener(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            latitude=MyLocationListener.latitude;
            longitude=MyLocationListener.longitude;
            /*gotolink.setText(Html.fromHtml("<a href=\""+ Google + "\">" + gotogogle + "</a>"));
            gotolink.setClickable(true);
            gotolink.setMovementMethod(LinkMovementMethod.getInstance());*/

        }
        else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            latitude=MyLocationListener.latitude;
            longitude=MyLocationListener.longitude;
            /*gotolink.setText(Html.fromHtml("<a href=\""+ Google + "\">" + gotogogle + "</a>"));
            gotolink.setClickable(true);
            gotolink.setMovementMethod(LinkMovementMethod.getInstance());*/
        }

        Map<String,String>location=new HashMap<>();
        location.put("latitude", String.valueOf(latitude));
        location.put("longitude",String.valueOf(longitude));

        Dangerref.child("Danger").setValue(location).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {

                }
                else
                {
                    Toast.makeText(MainActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });

        Message+= "Please Help Me I Am in Trouble"+ "\n My Location is https://www.google.com/maps/search/?api=1&query="+latitude+","+longitude;
        Google="https://www.google.com/maps/search/?api=1&query="+latitude+","+longitude;

        String Message2="Destination:-"+Destination;

        Log.d("Interface",Destination);
        Log.d("Message",Message);

        SmsManager smsManager1=SmsManager.getDefault();
        SmsManager smsManager2=SmsManager.getDefault();
        SmsManager smsManager3=SmsManager.getDefault();
        smsManager1.sendTextMessage(phone1.replace("\\s",""),null,Message,null,null);
        smsManager2.sendTextMessage(phone2.replaceAll("\\s",""),null,Message,null,null);
        smsManager3.sendTextMessage(phone3.replaceAll("\\s",""),null,Message,null,null);

        smsManager1.sendTextMessage(phone1.replace("\\s",""),null,Message2,null,null);
        smsManager2.sendTextMessage(phone2.replaceAll("\\s",""),null,Message2,null,null);
        smsManager3.sendTextMessage(phone3.replaceAll("\\s",""),null,Message2,null,null);

        sendnotification();
    }

    private void sendnotification() {
        JSONObject mainobj=new JSONObject();
        try {
            mainobj.put("to","/topics/"+getString(R.string.app_name));
            JSONObject notification=new JSONObject();
            notification.put("title","Need Help!!!");
            notification.put("body",Username + " is in danger please help!!!");
            notification.put("icon",userimage);
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
    protected void onPause() {
        super.onPause();
        if(mediaPlayer.isPlaying())
        {
            mediaPlayer.pause();
        }
    }
}
