package com.akshatagarwal1265.test1;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class ChatSelectionActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private DatabaseReference mRootRef;

    private FirebaseUser mCurrentUser;

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_selection);

        getSupportActionBar().setTitle("Start Conversation");
        //TODO - Customize Up Action - Set ClearTop|NewTask & finish() - New Intent MainActivity - Display Home As Up Enabled

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mCurrentUser = mAuth.getCurrentUser();

        mViewPager = (ViewPager)findViewById(R.id.chat_selection_tabPager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = (TabLayout)findViewById(R.id.chat_selection_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mCurrentUser==null)
        {
            sendToStart();
        }
        else
        {
            final DatabaseReference mUserDatabaseRef = mRootRef.child("Users").child(mCurrentUser.getUid());

            mUserDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        mUserDatabaseRef.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                        mUserDatabaseRef.child("online").setValue(true);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    private void sendToStart()
    {
        Intent startIntent = new Intent(ChatSelectionActivity.this, StartActivity.class);
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.chat_selection_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.chat_selection_menu_logout)
        {
            mRootRef.child("Users").child(mCurrentUser.getUid()).child("online").setValue(ServerValue.TIMESTAMP);
            mAuth.signOut();
            sendToStart();
        }

        if(item.getItemId() == R.id.chat_selection_menu_profile_settings)
        {
            Intent profileSettingsIntent = new Intent(ChatSelectionActivity.this, ProfileSettingsActivity.class);
            startActivity(profileSettingsIntent);
        }

        return true;
    }
}
