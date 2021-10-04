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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabaseRef;

    private ProgressDialog mProgress;

    private TextInputLayout mName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private Button mCreateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().setTitle("Sign-Up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        mProgress = new ProgressDialog(this);

        mName = (TextInputLayout) findViewById(R.id.signup_name);
        mEmail = (TextInputLayout) findViewById(R.id.signup_email);
        mPassword = (TextInputLayout) findViewById(R.id.signup_password);
        mCreateBtn = (Button) findViewById(R.id.signup_create_btn);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String Name = mName.getEditText().getText().toString().trim();
                String email = mEmail.getEditText().getText().toString().trim();
                String password = mPassword.getEditText().getText().toString();

                mName.getEditText().setText(Name);
                mEmail.getEditText().setText(email);

                if (!(TextUtils.isEmpty(Name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password))) {
                    mProgress.setTitle("Registering User");
                    mProgress.setMessage("Please Wait While We Create Your New Account");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();

                    register_user(Name, email, password);
                } else {
                    Toast.makeText(SignupActivity.this, "Fields Cannot Be Empty", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void register_user(final String displayName, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        Log.d("SignupActivity", "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {

                            mProgress.hide();
                            Log.w("SignupActivity", "signInWithEmail:failed", task.getException());
                            Toast.makeText(SignupActivity.this,
                                    "Authentication Failed\nUse Valid Email\nPassword atleast 6 characters\nCheck Internet Connection",
                                    Toast.LENGTH_LONG).show();
                        } else {

                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = current_user.getUid();

                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            mUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("device_token", deviceToken);
                            userMap.put("name", displayName);
                            userMap.put("status", "Hi There");
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");

                            mUserDatabaseRef.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {
                                        mProgress.dismiss();

                                        /*TODO DeviceToken May Fail on Sign-Up (1st time), Use the commented code to "refreshToken"
                                        DummyClassForTokenRefresh waitingTokenRefresh = new DummyClassForTokenRefresh();
                                        waitingTokenRefresh.onTokenRefresh();*/

                                        Intent chatSelectionIntent = new Intent(SignupActivity.this, ChatSelectionActivity.class);
                                        chatSelectionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(chatSelectionIntent);
                                        finish();
                                    } else {
                                        mProgress.hide();
                                        Log.w("SignupActivity", "HashMapUpdate:failed", task.getException());
                                        Toast.makeText(SignupActivity.this, "Authentication Failed", Toast.LENGTH_LONG).show();
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

    /*TODO DeviceToken May Fail on Sign-Up (1st time), Use the commented code to "refreshToken"
    Because it is not tested, might not work properly, Self-written code.
    Try to arrange proper code from internet
    On Signup devicetoken generation was failing, hence tried this class for onTokenRefresh(), not sure if it works.

    public class DummyClassForTokenRefresh extends FirebaseInstanceIdService {

        @Override
        public void onTokenRefresh() {

            // Get updated InstanceID token.
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference().child("Users").child(uid)
                    .child("device_token").setValue(refreshedToken);
        }

    }*/
}