package com.akshatagarwal1265.test1;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeNameActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabaseRef;
    private FirebaseUser mCurrentUser;

    private TextInputLayout mText;
    private Button mSaveBtn;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_name);

        getSupportActionBar().setTitle("Profile Name");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        mSaveBtn = (Button)findViewById(R.id.change_name_save_btn);
        mText = (TextInputLayout)findViewById(R.id.change_name_text);

        String old_name = getIntent().getStringExtra("old_name");
        mText.getEditText().setText(old_name);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        mSaveBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                mProgress = new ProgressDialog(ChangeNameActivity.this);
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please Wait While We Save Your Changes");
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.show();

                String name = mText.getEditText().getText().toString();

                if(name.trim().isEmpty())
                {
                    Toast.makeText(getApplicationContext(), "Lord Voldemort?", Toast.LENGTH_SHORT).show();
                    mProgress.dismiss();
                    return;
                }

                mUserDatabaseRef.child("name").setValue(name).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        mProgress.dismiss();

                        if(task.isSuccessful())
                        {
                            finish();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Error: Couldn't Save Changes", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });

    }
}
