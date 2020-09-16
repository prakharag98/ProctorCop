package com.example.hospiton;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Phone_Login extends AppCompatActivity {

    private EditText PhoneNumber;
    private Button Continue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone__login);


        initializeviews();

        Continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(PhoneNumber.getText().toString()))
                {
                    Toast.makeText(Phone_Login.this,"Please Enter Your Phone Number",Toast.LENGTH_SHORT).show();
                }
                else if(PhoneNumber.getText().toString().length()<10)
                {
                    Toast.makeText(Phone_Login.this,"Please Enter a Valid Phone Number",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    String number="+91"+PhoneNumber.getText().toString();
                    Intent intent=new Intent(Phone_Login.this,Verify.class);
                    intent.putExtra("number",number);
                    startActivity(intent);
                }
            }
        });
    }

    private void initializeviews() {
        PhoneNumber=(EditText)findViewById(R.id.phone_text);
        Continue=(Button)findViewById(R.id.continue_button);
    }
}
