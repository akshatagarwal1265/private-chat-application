package com.akshatagarwal1265.test1;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginChatFragment extends Fragment {

    private View mMainView;

    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;
    private String mCurrentUserUid;

    private ProgressDialog mProgress;

    private TextInputLayout mChatName;
    private Button mEnterBtn;

    public LoginChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_login_chat, container, false);

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mCurrentUserUid = mAuth.getCurrentUser().getUid();

        mProgress = new ProgressDialog(getActivity());

        mChatName = (TextInputLayout)mMainView.findViewById(R.id.login_chat_name);
        mEnterBtn = (Button)mMainView.findViewById(R.id.login_chat_enter_btn);

        mEnterBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mEnterBtn.setEnabled(false);

                String name = mChatName.getEditText().getText().toString();

                if (!TextUtils.isEmpty(name))
                {
                    mProgress.setTitle("Logging In User");
                    mProgress.setMessage("Please Wait While We Find Your Duo");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();

                    checkValidChatName(name);
                }
                else
                {
                    Toast.makeText(getActivity(), "Field Cannot Be Empty", Toast.LENGTH_SHORT).show();
                    mEnterBtn.setEnabled(true);
                }

            }
        });

        return mMainView;
    }

    private void register_user(final String chatId) {

        mRootRef.child("ChatIds").keepSynced(true);
        mRootRef.child("ChatIds").child("_dummy_").setValue("LoginChatFragment", new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError != null)
                {
                    Toast.makeText(getActivity(), "Error Connecting To Database", Toast.LENGTH_SHORT).show();
                    hideProgressEnableBtn();
                }
                else
                {
                    mRootRef.child("ChatIds").child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            // For Debugging
                            // Log.e("ERRRORRRRRRR",dataSnapshot.toString());

                            mRootRef.child("ChatIds").child("_dummy_").setValue(null);

                            if (dataSnapshot != null) {

                                // For Debugging
                                // Log.e("Snapshot1",dataSnapshot.toString());

                                int noOfUsers = Integer.parseInt(dataSnapshot.child("number_of_users").getValue().toString());
                                // For Debugging
                                // Log.e("ERRRORRRRRRR",noOfUsers+"");

                                int counter = -1;
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    if(! snapshot.getKey().equals("_dummy_")) //Someone might have just logged
                                        // in to Chat Activity, Hence _dummy_ might be present
                                        counter++;
                                    // For Debugging
                                    // Log.e("ERRRORRRRRRR",counter+"");

                                }

                                if(noOfUsers!=counter) {
                                    Log.e("ERROR:", " Value Mismatch for \"no_of_users\"");
                                    mRootRef.child("ChatIds").child(chatId).child("number_of_users").setValue(""+counter);
                                    noOfUsers = counter;
                                }

                                if (noOfUsers == 1) {
                                    //Password Is Correct
                                    //Partner Hasn't signed in yet
                                    hideProgressEnableBtn();
                                    String toastMessage = "Partner Not Signed-Up Yet :)";
                                    toastMessage += "\nAsk Your DUO to Sign-Up with The Unique Chat Id";
                                    toastMessage += "\nLog-In After He/She Signs-Up";
                                    Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_LONG).show();

                                } else if (noOfUsers == 2) {
                                    //Valid Attempt

                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("chat_uid",chatId);
                                    startActivity(chatIntent);
                                    /*TODO Add Intent-Extras to ChatIntent on Log-In*/

                                    resetFieldsDismissProgressEnableBtn();
                                } else {
                                    resetFields();
                                    hideProgressEnableBtn();
                                    Toast.makeText(getActivity(), "FATAL ERROR: No. Of Users in Chat = " + noOfUsers, Toast.LENGTH_SHORT).show();
                                }
                            }
                            else
                            {
                                resetFields();
                                hideProgressEnableBtn();
                                Toast.makeText(getActivity(), "DataSnapshot - Null", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            hideProgressEnableBtn();
                            Toast.makeText(getActivity(), "Unsuccessful: Try to Resubmit", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void checkValidChatName(final String chatName) {

        mRootRef.child("UserChats").child(mCurrentUserUid).keepSynced(true);
        mRootRef.child("UserChats").child(mCurrentUserUid).child("_dummy_").push()
            .setValue("LoginChatFragment", new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError != null)
                    {
                        Toast.makeText(getActivity(), "Error Connecting To Database", Toast.LENGTH_LONG).show();
                        hideProgressEnableBtn();
                    }
                    else
                    {
                        mRootRef.child("UserChats").child(mCurrentUserUid).child("PwdId").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                mRootRef.child("UserChats").child(mCurrentUserUid).child("_dummy_").setValue(null);

                                if (dataSnapshot != null) {
                                    if (dataSnapshot.hasChild(chatName)) {
                                        //If Valid Password In User Local Account
                                        register_user(dataSnapshot.child(chatName).getValue().toString());
                                    } else {
                                        Toast.makeText(getActivity(), "You Have No Such Chat Name\nEnter A Valid One\nNOTE: Case Sensitive", Toast.LENGTH_LONG).show();
                                        resetFields();
                                        hideProgressEnableBtn();
                                    }
                                } else {
                                    Toast.makeText(getActivity(), "DataSnapshot - Null", Toast.LENGTH_SHORT).show();
                                    resetFields();
                                    hideProgressEnableBtn();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(getActivity(), "Error - Try Resubmitting", Toast.LENGTH_SHORT).show();
                                hideProgressEnableBtn();
                            }
                        });
                    }
                }
            });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mProgress.dismiss();
    }

    private void resetFields()
    {
        mChatName.getEditText().getText().clear();
    }

    private void hideProgressEnableBtn()
    {
        mEnterBtn.setEnabled(true);
        mProgress.hide();
    }

    private void dismissProgressEnableBtn()
    {
        mEnterBtn.setEnabled(true);
        mProgress.dismiss();
    }

    private void resetFieldsDismissProgressEnableBtn() {
        resetFields();
        dismissProgressEnableBtn();
    }

}