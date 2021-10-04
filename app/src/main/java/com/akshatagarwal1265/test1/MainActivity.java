package com.akshatagarwal1265.test1;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //TODO Add local Storage For StringPassword,3TapCounterPasswords & allow user to change password
    private static final String mDUMMY_SCREEN_PASSWORD = "";
    private static final int mCOUNTER_1_TAPS = 1;
    private static final int mCOUNTER_2_TAPS = 2;
    private static final int mCOUNTER_3_TAPS = 1;

    private int mCounter1;
    private int mCounter2;
    private int mCounter3;

    private TextView mDummyScreenCounter1;
    private TextView mDummyScreenCounter2;
    private TextView mDummyScreenCounter3;
    private TextInputLayout mDummyScreen1;
    private TextInputLayout mDummyScreen2;
    private TextInputLayout mDummyScreenPassword;
    private Button mDummyScreenButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCounter1 = 0;
        mCounter2 = 0;
        mCounter3 = 0;

        mDummyScreenCounter1 = (TextView)findViewById(R.id.dummy_screen_counter_1);
        mDummyScreenCounter2 = (TextView)findViewById(R.id.dummy_screen_counter_2);
        mDummyScreenCounter3 = (TextView)findViewById(R.id.dummy_screen_counter_3);
        mDummyScreen1 = (TextInputLayout)findViewById(R.id.dummy_screen_1);
        mDummyScreen2 = (TextInputLayout)findViewById(R.id.dummy_screen_2);
        mDummyScreenPassword = (TextInputLayout)findViewById(R.id.dummy_screen_password);
        mDummyScreenButton = (Button)findViewById(R.id.dummy_screen_button);

        mDummyScreenCounter1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCounter1++;
            }
        });

        mDummyScreenCounter2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCounter2++;
            }
        });

        mDummyScreenCounter3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCounter3++;
            }
        });

        mDummyScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCounter1 == mCOUNTER_1_TAPS && mCounter2 == mCOUNTER_2_TAPS && mCounter3 == mCOUNTER_3_TAPS
                        && mDummyScreenPassword.getEditText().getText().toString().equals(mDUMMY_SCREEN_PASSWORD))
                {
                    Toast.makeText(MainActivity.this, "WELCOME", Toast.LENGTH_SHORT).show();
                    Intent chatSelectionIntent = new Intent(MainActivity.this, ChatSelectionActivity.class);
                    startActivity(chatSelectionIntent);
                }
                else if(!TextUtils.isEmpty(mDummyScreen1.getEditText().getText().toString().trim()) &&
                        !TextUtils.isEmpty(mDummyScreen2.getEditText().getText().toString().trim()) &&
                        !TextUtils.isEmpty(mDummyScreenPassword.getEditText().getText().toString().trim()))
                {
                    Toast.makeText(MainActivity.this, "Thank You For Your feedback", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Fields Cannot Be Empty", Toast.LENGTH_SHORT).show();
                }
                resetFields();
            }
        });
    }

    private void resetFields()
    {
        mCounter1 = 0;
        mCounter2 = 0;
        mCounter3 = 0;
        mDummyScreen1.getEditText().getText().clear();
        mDummyScreen2.getEditText().getText().clear();
        mDummyScreenPassword.getEditText().getText().clear();
    }
}
//TODO include all versions [mdpi,l,x,xx,..] of default wallpaper [whatsapp default background]
