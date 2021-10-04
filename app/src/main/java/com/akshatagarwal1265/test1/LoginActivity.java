package com.akshatagarwal1265.test1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private DatabaseReference mUsersRef;

    private ProgressDialog mProgress;

    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mEnterBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        getSupportActionBar().setTitle("Log-In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        mUsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mProgress = new ProgressDialog(this);

        mEmail = (TextInputLayout)findViewById(R.id.login_email);
        mPassword = (TextInputLayout)findViewById(R.id.login_password);
        mEnterBtn = (Button)findViewById(R.id.login_enter_btn);

        mEnterBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();

                if (!(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)))
                {
                    mProgress.setTitle("Logging-In User");
                    mProgress.setMessage("Please Wait While We Log You Into Your Account");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();

                    login_user(email, password);
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "Fields Cannot Be Empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void login_user(final String email, String password)
    {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        Log.d("LoginActivity", "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {

                            mProgress.hide();

                            Log.w("LoginActivity", "signInWithEmail:failed", task.getException());

                            Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            String current_uid = mAuth.getCurrentUser().getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            mUsersRef.child(current_uid).child("device_token").setValue(deviceToken)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                mProgress.dismiss();

                                                Intent chatSelectionIntent = new Intent(LoginActivity.this, ChatSelectionActivity.class);
                                                chatSelectionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(chatSelectionIntent);
                                                finish();
                                            }
                                            else
                                            {
                                                mProgress.hide();

                                                Log.w("LoginActivity", "Token Generation:failed", task.getException());

                                                Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });


                        }

                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mProgress.dismiss();
    }
}