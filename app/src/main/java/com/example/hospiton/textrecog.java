package com.example.hospiton;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class textrecog extends AppCompatActivity {

    private Button btncamera;
    private ImageView take;
    private Bitmap bitmap;
    private TextView recognize;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 102;
    private FirebaseAuth mAuth;
    private DatabaseReference userref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_textrecog);

        btncamera=(Button)findViewById(R.id.btncamera);
        take=(ImageView)findViewById(R.id.picture);
        recognize=(TextView)findViewById(R.id.display_text);
        mAuth=FirebaseAuth.getInstance();
        userref=FirebaseDatabase.getInstance().getReference();

        if (ContextCompat.checkSelfPermission(textrecog.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(textrecog.this,
                    Manifest.permission.CAMERA)){
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            }else
            {
                ActivityCompat.requestPermissions(textrecog.this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);


            }
            // Permission is not granted
        }




        btncamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,0);
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         bitmap=(Bitmap)data.getExtras().get("data");
         take.setImageBitmap(bitmap);
         extracttextfromimage();

    }

    private void extracttextfromimage() {
        TextRecognizer textRecognizer=new TextRecognizer.Builder(textrecog.this).build();

        if(!textRecognizer.isOperational())
        {
            Toast.makeText(this,"Could not get the text",Toast.LENGTH_SHORT).show();
        }
        else {
            Log.d("textrecog",bitmap.toString());
            Frame frame=new Frame.Builder().setBitmap(bitmap).build();

            SparseArray<TextBlock>items=textRecognizer.detect(frame);

            StringBuilder sb=new StringBuilder();

            for(int i=0;i<items.size();i++)
            {
                TextBlock myitems=items.valueAt(i);
                sb.append(myitems.getValue());
                sb.append("\n");
            }
            recognize.setText(sb.toString());


            if(sb!=null)
            {
                  userref.child("Users").child(mAuth.getCurrentUser().getUid()).child("Number Plate").setValue(sb.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                      @Override
                      public void onComplete(@NonNull Task<Void> task) {
                          if(task.isSuccessful())
                          {

                          }
                      }
                  });
            }
        }
    }
}
