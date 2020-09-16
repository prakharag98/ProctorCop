package com.example.hospiton;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private EditText Email,Password;
    private Button LoginButton,PhoneVerification;
    private TextView forgetPassword,NeednewAccount;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton signInButton;
    private DatabaseReference RootRef;
    private static final String TAG=LoginActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeviews();
        onclicklistener();
        firebaseAuth=FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder()
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 123);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 123) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if(account!=null)
                {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(LoginActivity.this,"Google Sign in Failed",Toast.LENGTH_SHORT).show();
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // Sign in success, update UI with the signed-in user's information
                            String userid = firebaseAuth.getCurrentUser().getUid();
                            final String Devicetoken= FirebaseInstanceId.getInstance().getToken();
                            RootRef.child(getString(R.string.Users)).child(userid).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.child(getString(R.string.name)).exists())
                                    {
                                       RootRef.child("Users").child(firebaseAuth.getCurrentUser().getUid()).child("device_token").setValue(Devicetoken);
                                    }
                                    else {
                                        String id=firebaseAuth.getUid();
                                        RootRef.child(getString(R.string.Users)).child(id).setValue("");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                            Toast.makeText(LoginActivity.this,"Welcome",Toast.LENGTH_SHORT).show();
                        } else {
                             // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this,"Login Failed",Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
        progressDialog.dismiss();
    }
    private void onclicklistener() {
        NeednewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent SignIn=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(SignIn);
                overridePendingTransition(R.anim.right_slide_in,R.anim.left_slide_in);

            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=Email.getText().toString();
                String password=Password.getText().toString();

                if(TextUtils.isEmpty(password) || TextUtils.isEmpty(email))
                {
                    Toast.makeText(LoginActivity.this,"Please Fill All the details",Toast.LENGTH_SHORT).show();
                }
                else if(isValidPassword(password)){
                    progressDialog.setTitle("Signing In");
                    progressDialog.setMessage("Please Wait");
                    progressDialog.setCanceledOnTouchOutside(true);
                    progressDialog.show();
                    Log.d(TAG,password);

                    firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                if(firebaseAuth.getCurrentUser().isEmailVerified()) {
                                    Toast.makeText(LoginActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                                    sendusertoMainActivity();
                                }
                                else
                                {
                                    Toast.makeText(LoginActivity.this,"Please Verify Your Email",Toast.LENGTH_SHORT).show();
                                }
                                progressDialog.dismiss();
                            }
                            else
                            {
                                String Error=task.getException().getMessage();
                                Toast.makeText(LoginActivity.this,Error,Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(LoginActivity.this,"Password Must Contain One Capital Letter" +
                            ", One Symbol, One Number",Toast.LENGTH_LONG).show();
                }
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        PhoneVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent=new Intent(LoginActivity.this,Phone_Login.class);
               startActivity(intent);
               overridePendingTransition(R.anim.right_slide_out,R.anim.slideoutleft);
            }
        });
    }

    private boolean isValidPassword(String password) {
        Pattern pattern;
        Matcher matcher;

        final String Password_Pattern="^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$";
        pattern=Pattern.compile(Password_Pattern);
        matcher=pattern.matcher(password);
        return matcher.matches();
    }

    private void sendusertoMainActivity()
    {
        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void initializeviews() {
        Email=(EditText)findViewById(R.id.login_email_view);
        Password=(EditText)findViewById(R.id.login_password_view);
        LoginButton=(Button)findViewById(R.id.login_button_view);
        NeednewAccount=(TextView)findViewById(R.id.Need_New_account_text_view);
        progressDialog=new ProgressDialog(this);
        signInButton=(SignInButton)findViewById(R.id.googlesignin);
        PhoneVerification=(Button)findViewById(R.id.phone_verify);
    }
}
