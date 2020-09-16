package com.example.hospiton;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class User_Profile extends AppCompatActivity implements View.OnClickListener, ContactsAdapter.ListItemClickListener
, View.OnFocusChangeListener {

    private Toolbar toolbar;
    private AlertDialog alertDialog;
    private int pos;
    private ImageView userphoto;
    private EditText user_name, user_contact, closed_contact1, closed_contact2, closed_contact3;
    private Button save_button;
    private static final String TAG=User_Profile.class.getSimpleName();
    List<androidcontacts> contacts;
    private RecyclerView recyclerView;
    private ContactsAdapter contactsAdapter;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 102;
    private static final int GalleryPicker=123;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference Rootref;
    private StorageReference mstorageref;
    private String Download_Url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user__profile);

        firebaseAuth=FirebaseAuth.getInstance();
        Rootref= FirebaseDatabase.getInstance().getReference();
        mstorageref= FirebaseStorage.getInstance().getReference().child(getString(R.string.ProfileImages));

        Initializeviews();
        setOnClickListener();

        Log.d("key",String.valueOf(recyclerView));

        if (ContextCompat.checkSelfPermission(User_Profile.this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(User_Profile.this,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(User_Profile.this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }

        FetchData();
    }

    private void FetchData() {
        String CurrentUserId=firebaseAuth.getCurrentUser().getUid();
        Rootref.child(getString(R.string.Users)).child(CurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(getString(R.string.DownloadableUrl)).exists())
                {
                    Download_Url=dataSnapshot.child(getString(R.string.DownloadableUrl)).getValue().toString();
                    Picasso.get().load(Download_Url).fit().into(userphoto);
                }
                if(dataSnapshot.exists() && dataSnapshot.child(getString(R.string.name)).exists())
                {
                    user_name.setText(dataSnapshot.child(getString(R.string.name)).getValue().toString());
                    user_contact.setText(dataSnapshot.child(getString(R.string.contact)).getValue().toString());
                    closed_contact1.setText(dataSnapshot.child(getString(R.string.alias_contact1)).getValue().toString());
                    closed_contact2.setText(dataSnapshot.child(getString(R.string.alias_contact2)).getValue().toString());
                    closed_contact3.setText(dataSnapshot.child(getString(R.string.alias_contact3)).getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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


    private void Initializeviews() {
        userphoto = (ImageView) findViewById(R.id.user_profile_image);
        user_name = (EditText) findViewById(R.id.user_name);
        user_contact = (EditText) findViewById(R.id.user_contact);
        closed_contact1 = (EditText) findViewById(R.id.user_closed1);
        closed_contact2 = (EditText) findViewById(R.id.user_closed2);
        closed_contact3 = (EditText) findViewById(R.id.user_closed3);
        save_button = (Button) findViewById(R.id.save_button);

        contacts = new ArrayList<>();
    }

    private void setOnClickListener() {

        closed_contact1.setOnFocusChangeListener(this);
        closed_contact2.setOnFocusChangeListener(this);
        closed_contact3.setOnFocusChangeListener(this);
        save_button.setOnClickListener(this);
        userphoto.setOnClickListener(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GalleryPicker && resultCode==RESULT_OK  && data!=null)
        {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                Uri resultUri=result.getUri();

                 final StorageReference Filepath=mstorageref.child(firebaseAuth.getCurrentUser().getUid() + ".jpeg");
                 Filepath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                     @Override
                     public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                         Filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                             @Override
                             public void onSuccess(Uri uri) {
                                 Download_Url=uri.toString();
                                 Picasso.get().load(Download_Url).fit().into(userphoto);
                             }
                         });
                     }
                 });
            }
        }
    }

    private void gotogallery()
    {
        Intent intent=new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,GalleryPicker);
    }

    private void FetchContacts() {
        ContentResolver contentResolver = getContentResolver();
        Cursor getPhoneNumber = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        if (getPhoneNumber != null) {
            Log.e("count", "" + getPhoneNumber.getCount());
            if (getPhoneNumber.getCount() == 0) {
                Toast.makeText(User_Profile.this, "No contacts in your contact list.", Toast.LENGTH_LONG).show();
            }

            List<String>Names=new ArrayList<>();

            while (getPhoneNumber.moveToNext()) {
                String id = getPhoneNumber.getString(getPhoneNumber.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                String name = getPhoneNumber.getString(getPhoneNumber.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = getPhoneNumber.getString(getPhoneNumber.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                androidcontacts mandroidcontacts=new androidcontacts(id,name,phoneNumber);

                if(!Names.contains(name))
                {
                    contacts.add(mandroidcontacts);
                    Names.add(name);
                }
            }
            getPhoneNumber.close();
        }
    }

    private void displaycontacts()
    {
        FetchContacts();
        AlertDialog.Builder builder = new AlertDialog.Builder(User_Profile.this);
        LayoutInflater inflater = getLayoutInflater();
        View converview = (View) inflater.inflate(R.layout.contactsview, null);
        builder.setView(converview);
        builder.setTitle("Contacts");
        Log.d(TAG, String.valueOf(contacts.size()));
        recyclerView = (RecyclerView) converview.findViewById(R.id.recycler_view);
        contactsAdapter = new ContactsAdapter(contacts,this);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(converview.getContext());
        DividerItemDecoration dividerItemDecoration=new DividerItemDecoration(User_Profile.this,DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.divider));
        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(contactsAdapter);
        alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onClick(View v) {
        int id=v.getId();

        switch (id)
        {

            case R.id.user_profile_image:
                gotogallery();
                break;

            case R.id.save_button:
                validateform();
                break;
        }
    }

    private void validateform() {
        String name=user_name.getText().toString();
        String Contact=user_contact.getText().toString();
        String closed_contact_1=closed_contact1.getText().toString();
        String closed_contact_2=closed_contact2.getText().toString();
        String closed_contact_3=closed_contact3.getText().toString();

        if(TextUtils.isEmpty(name) || TextUtils.isEmpty(Contact) || TextUtils.isEmpty(closed_contact_1) || TextUtils.isEmpty(closed_contact_2)
          || TextUtils.isEmpty(closed_contact_3) && Download_Url!=null)
        {
            Toast.makeText(User_Profile.this,"Please Fill all the details above", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String currentuserid=firebaseAuth.getCurrentUser().getUid();
            HashMap<String,String>Data=new HashMap<>();
            Data.put(getResources().getString(R.string.name),name);
            Data.put(getString(R.string.contact),Contact);
            Data.put(getString(R.string.alias_contact1),closed_contact_1);
            Data.put(getString(R.string.alias_contact2),closed_contact_2);
            Data.put(getString(R.string.alias_contact3),closed_contact_3);
            Data.put(getString(R.string.DownloadableUrl),Download_Url);

            Rootref.child(getString(R.string.Users)).child(currentuserid).setValue(Data).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        SendUserToMainActivity();

                    }
                    else {
                        String Error=task.getException().getMessage().toString();
                        Toast.makeText(User_Profile.this,Error,Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent intent=new Intent(User_Profile.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    @Override
    public void onListItemClicked(int Position) {
         androidcontacts contact_item=contactsAdapter.getitem(Position);
         String Contact=contact_item.getPhone_Number();
         if(pos==1)
         {
             closed_contact1.setText(Contact);
         }
         else if(pos==2)
         {
             closed_contact2.setText(Contact);
         }
         else if(pos==3)
         {
             closed_contact3.setText(Contact);
         }
         alertDialog.dismiss();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus)
        {
            int id=v.getId();
            displaycontacts();

            if(v.getId()==R.id.user_closed1)
            {
                pos=1;
            }
            else if(v.getId()==R.id.user_closed2)
            {
                pos=2;
            }
            else if(v.getId()==R.id.user_closed3)
            {
                pos=3;
            }
        }
    }
}