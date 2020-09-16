package com.example.hospiton;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText Email,Password;
    private FirebaseAuth firebaseAuth;
    private Button Register;
    private TextView AlreadyAccount;
    private ProgressDialog progressDialog;
    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firebaseAuth=FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();

        initializeviews();

        omclicklistener();
    }

    private void omclicklistener() {
        AlreadyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.right_slide_out,R.anim.slideoutleft);

            }
        });

        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=Email.getText().toString();
                String password=Password.getText().toString();

                if(TextUtils.isEmpty(email) && TextUtils.isEmpty(password))
                {
                    Toast.makeText(RegisterActivity.this,"Please Fill All The Above Details",Toast.LENGTH_SHORT).show();
                }
                else if(isValidPassword(password))
                {
                    progressDialog.setTitle("Creating New Account");
                    progressDialog.setMessage("Please Wait,While We are Creating New Account For You.....");
                    progressDialog.setCanceledOnTouchOutside(true);
                    progressDialog.show();

                    firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                firebaseAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            String currentuserID=firebaseAuth.getCurrentUser().getUid();
                                            RootRef.child(getString(R.string.Users)).child(currentuserID).setValue("");
                                            Toast.makeText(RegisterActivity.this, "Account Created Successfully,Please" +
                                                    "Verify your email", Toast.LENGTH_SHORT).show();


                                            progressDialog.dismiss();
                                        }
                                        else
                                        {
                                            String Error=task.getException().getMessage();
                                            Toast.makeText(RegisterActivity.this,Error,Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            else
                            {
                                String Error=task.getException().getMessage();
                                Toast.makeText(RegisterActivity.this,Error,Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });

                }
                else
                {
                    Toast.makeText(RegisterActivity.this,"Password Must Contain One Capital Letter" +
                            ", One Symbol, One Number",Toast.LENGTH_LONG).show();
                }
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
        Intent Main=new Intent(RegisterActivity.this,MainActivity.class);
        Main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(Main);
    }
    private void initializeviews() {
        Email=(EditText)findViewById(R.id.Signin_email_view);
        Password=(EditText) findViewById(R.id.Signin_password_view);
        Register=(Button)findViewById(R.id.Signin_button_view);
        AlreadyAccount=(TextView)findViewById(R.id.Signin_Need_New_account_text_view);
        progressDialog=new ProgressDialog(this);
    }
}
