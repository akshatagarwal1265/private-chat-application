package com.akshatagarwal1265.test1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private Button mLogInBtn;
    private Button mSignUpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        getSupportActionBar().setTitle("Welcome");
        //TODO - Customize Up Action - Set ClearTop|NewTask & finish() - New Intent MainActivity - Display Home As Up Enabled

        mSignUpBtn = (Button) findViewById(R.id.start_signup_btn);
        mSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signupIntent = new Intent(StartActivity.this, SignupActivity.class);
                startActivity(signupIntent);
            }
        });

        mLogInBtn = (Button) findViewById(R.id.start_login_btn);
        mLogInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });
    }
}