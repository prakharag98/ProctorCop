package com.example.hospiton;

import androidx.arch.core.executor.TaskExecutor;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class Verify extends AppCompatActivity {

    private String verificationid;
    private EditText OTP;
    private Button verify;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private DatabaseReference RootRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        mAuth=FirebaseAuth.getInstance();
        RootRef=FirebaseDatabase.getInstance().getReference();

        initializeviews();

        String number=getIntent().getStringExtra("number");
        sendVerificationCode(number);

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              String Code=OTP.getText().toString().trim();
              if(TextUtils.isEmpty(Code))
              {
                  Toast.makeText(Verify.this,"Please Enter The Code",Toast.LENGTH_LONG).show();
              }
              else {
                  progressBar.setVisibility(View.VISIBLE);
                  verifycode(Code);
              }
            }
        });
    }

    private void verifycode(String Code)
    {
        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationid,Code);
        signinwithCredential(credential);
    }

    private void signinwithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                      if(task.isSuccessful())
                      {
                          String userid=mAuth.getCurrentUser().getUid();

                          RootRef.child(getString(R.string.Users)).child(userid).addValueEventListener(new ValueEventListener() {
                              @Override
                              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                  if(dataSnapshot.child(getString(R.string.name)).exists())
                                  {

                                  }
                                  else
                                  {
                                      RootRef.child(getString(R.string.Users)).child(mAuth.getCurrentUser().getUid()).setValue("");
                                  }
                              }

                              @Override
                              public void onCancelled(@NonNull DatabaseError databaseError) {

                              }
                          });

                           progressBar.setVisibility(View.INVISIBLE);
                          Intent intent=new Intent(Verify.this,MainActivity.class);
                          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                          startActivity(intent);
                          Toast.makeText(Verify.this,"Welcome",Toast.LENGTH_SHORT).show();
                      }
                      else
                      {
                          Toast.makeText(Verify.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                      }
                    }
                });
    }

    private void initializeviews() {
       OTP=(EditText)findViewById(R.id.OTP_text);
       verify=(Button)findViewById(R.id.verify_button);
       progressBar=(ProgressBar)findViewById(R.id.verify_progress_bar);
    }

    private void sendVerificationCode(String number){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(number,60, TimeUnit.SECONDS, TaskExecutors.MAIN_THREAD,mCallback );
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
           mCallback=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
           String code=phoneAuthCredential.getSmsCode();
           if(code!=null)
           {
               OTP.setText(code);
               progressBar.setVisibility(View.VISIBLE);
               verifycode(code);
           }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(Verify.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            verificationid=s;
        }
    };
}
