package com.akshatagarwal1265.test1;


import android.app.ProgressDialog;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignupChatFragment extends Fragment {

    private View mMainView;

    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;
    private String mCurrentUserUid;

    private ProgressDialog mProgress;

    private TextInputLayout mChatId;
    private TextInputLayout mChatName;
    private Button mCreateBtn;

    public SignupChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_signup_chat, container, false);

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mCurrentUserUid = mAuth.getCurrentUser().getUid();

        mProgress = new ProgressDialog(getActivity());

        mChatId = (TextInputLayout)mMainView.findViewById(R.id.signup_chat_id);
        mChatId.getEditText().setText(mRootRef.child("ChatIds").push().getKey());
        //TODO Make this field copy-text-only, that is make it uneditable, so that user automatically gets unique global chat id every time.

        mChatName = (TextInputLayout)mMainView.findViewById(R.id.signup_chat_name);
        mCreateBtn = (Button)mMainView.findViewById(R.id.signup_chat_create_btn);

        mCreateBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mCreateBtn.setEnabled(false);

                String id = mChatId.getEditText().getText().toString().trim();
                String name = mChatName.getEditText().getText().toString();

                mChatId.getEditText().setText(id);

                if (!(TextUtils.isEmpty(id) || TextUtils.isEmpty(name)))
                {
                    mProgress.setTitle("Registering User");
                    mProgress.setMessage("Please Wait While We Create Your Duo");
                    mProgress.setCanceledOnTouchOutside(false);
                    mProgress.show();

                    checkUniqueChatName(id, name);
                }
                else
                {
                    Toast.makeText(getActivity(), "Fields Cannot Be Empty", Toast.LENGTH_SHORT).show();
                    mCreateBtn.setEnabled(true);
                }

            }
        });

        return mMainView;
    }

    private void register_user(final String chatId, final String chatName) {

        mRootRef.child("ChatIds").keepSynced(true);
        mRootRef.child("ChatIds").child("_dummy_").push().setValue("SignupChatFragment", new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if(databaseError != null)
                {
                    Toast.makeText(getActivity(), "Error Connecting To Database", Toast.LENGTH_LONG).show();
                    hideProgressEnableBtn();
                }
                else
                {
                    mRootRef.child("ChatIds").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            mRootRef.child("ChatIds").child("_dummy_").setValue(null);

                            if (dataSnapshot != null) {

                                // For Debugging
                                // Log.e("Snapshot2",dataSnapshot.toString());
                                if (!dataSnapshot.hasChild(chatId)) {
                                    String chatsUserRef = "Chats/" + chatId + "/" + mCurrentUserUid;

                                    Map updateRootMap = new HashMap();
                                    updateRootMap.put("ChatIds/" + chatId + "/number_of_users", "1");
                                    updateRootMap.put("ChatIds/" + chatId + "/" + mCurrentUserUid, ServerValue.TIMESTAMP);
                                    updateRootMap.put(chatsUserRef + "/password", chatName);
                                    updateRootMap.put(chatsUserRef + "/image", "default");
                                    updateRootMap.put(chatsUserRef + "/status", "default");
                                    updateRootMap.put(chatsUserRef + "/thumb_image", "default");
                                    updateRootMap.put(chatsUserRef + "/last_seen", "true");
                                    updateRootMap.put(chatsUserRef + "/read_receipt", "true");
                                    updateRootMap.put("UserChats/" + mCurrentUserUid + "/IdPwd/" + chatId, chatName);
                                    updateRootMap.put("UserChats/" + mCurrentUserUid + "/PwdId/" + chatName, chatId);

                                    mRootRef.updateChildren(updateRootMap, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            if (databaseError != null) {
                                                hideProgressEnableBtn();
                                                Log.i("SignupChatFragment", databaseError.getMessage());
                                                Toast.makeText(getActivity(), "Unsuccessful: Try to Resubmit", Toast.LENGTH_SHORT).show();
                                            } else {
                                                dismissProgressEnableBtn();
                                                //No resetField(), to let user copy paste The Unique Chat Id
                                                String toastMessage = "DONE :)\nAsk Your DUO to Sign-Up with SAME Unique Chat Id";
                                                toastMessage += "\nLog-In After He/She Signs-Up";
                                                Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_LONG).show();
                                                Toast.makeText(getActivity(), "COPY Unique Id\nSHARE with Partner\nSign-Up Partner", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                } else {

                                    // For Debugging
                                    // Log.e("Snapshot3",dataSnapshot.toString());
                                    int noOfUsers = Integer.parseInt(dataSnapshot.child(chatId).child("number_of_users").getValue().toString());

                                    int counter = -1;
                                    for (DataSnapshot snapshot : dataSnapshot.child(chatId).getChildren()) {
                                        counter++;
                                    }

                                    if(noOfUsers!=counter) {
                                        Log.e("ERROR:", " Value Mismatch for \"no_of_users\"");
                                        mRootRef.child("ChatIds").child(chatId).child("number_of_users").setValue(""+counter);
                                        noOfUsers = counter;
                                    }

                                    if (noOfUsers == 1) {
                                        if(dataSnapshot.child(chatId).hasChild(mCurrentUserUid))
                                        {
                                            //No resetField(), to let user copy paste The Unique Chat Id
                                            dismissProgressEnableBtn();
                                            String toastMessage = "Chat Exists :)";
                                            toastMessage += "\nAsk Your DUO to Sign-Up with SAME Unique Chat Id";
                                            Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_LONG).show();
                                            toastMessage = "Log-In After He/She Signs-Up";
                                            toastMessage+="\nUse The Chat Name used during first Sign-Up\nSee You There :)";
                                            Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_LONG).show();
                                            //TODO DETAILED
                                            // Detail Error To User. Ask To inform partner to Sign-Up
                                            // Ask User to use old password or
                                            // Offer To Overwrite Previous Password and continue to ask partner to sign up
                                        }
                                        else
                                        {
                                            String chatsUserRef = "Chats/" + chatId + "/" + mCurrentUserUid;

                                            Map updateRootMap = new HashMap();
                                            updateRootMap.put("ChatIds/" + chatId + "/number_of_users", "2");
                                            updateRootMap.put("ChatIds/" + chatId + "/" + mCurrentUserUid, ServerValue.TIMESTAMP);
                                            updateRootMap.put(chatsUserRef + "/password", chatName);
                                            updateRootMap.put(chatsUserRef + "/image", "default");
                                            updateRootMap.put(chatsUserRef + "/status", "default");
                                            updateRootMap.put(chatsUserRef + "/thumb_image", "default");
                                            updateRootMap.put(chatsUserRef + "/last_seen", "true");
                                            updateRootMap.put(chatsUserRef + "/read_receipt", "true");
                                            updateRootMap.put("UserChats/" + mCurrentUserUid + "/IdPwd/" + chatId, chatName);
                                            updateRootMap.put("UserChats/" + mCurrentUserUid + "/PwdId/" + chatName, chatId);

                                            mRootRef.updateChildren(updateRootMap, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                    if (databaseError != null) {
                                                        hideProgressEnableBtn();
                                                        Log.i("SignupChatFragment", databaseError.getMessage());
                                                        Toast.makeText(getActivity(), "Unsuccessful: Try to Resubmit", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        resetFields();
                                                        dismissProgressEnableBtn();

                                                        String toastMessage = "DONE :)";
                                                        toastMessage += "\nLog-In To Start Chatting";
                                                        Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_LONG).show();

                                                        /*TODO Add Intent-Extras to ChatIntent on Sig-Up of Second User
                                                        Intent chatIntent = new Intent(getActivity(), ChatActivity.class);
                                                        startActivity(chatIntent);
                                                        Remove The Unnecessary Toast*/
                                                    }
                                                }
                                            });
                                        }

                                    } else if (noOfUsers == 2) {
                                        if(dataSnapshot.child(chatId).hasChild(mCurrentUserUid)) //User CONFUSED. Redirect Him To Log-In
                                        {
                                            String toastMessage = "Use Log-In Screen\nEnter Chat Name There";
                                            toastMessage+="\nUse The Chat Name used during Sign-Up\nSee You There :)";
                                            Toast.makeText(getActivity(),toastMessage, Toast.LENGTH_LONG).show();
                                        }
                                        else //Someone Trying To Create Chat With Existing ID, Prompt User To Create UNIQUE ID
                                        {
                                            Toast.makeText(getActivity(), "Unique Chat ID Is Taken :(\nTry Another Random ID", Toast.LENGTH_LONG).show();
                                        }
                                        resetFields();
                                        hideProgressEnableBtn();
                                    } else {
                                        resetFields();
                                        hideProgressEnableBtn();
                                        Toast.makeText(getActivity(), "FATAL ERROR: No. Of Users in Chat = " + noOfUsers, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            else
                            {
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

    private void checkUniqueChatName(final String chatId, final String chatName) {

        mRootRef.child("UserChats").child(mCurrentUserUid).keepSynced(true);
        mRootRef.child("UserChats").child(mCurrentUserUid).child("_dummy_").push().setValue("SignupChatFragment", new DatabaseReference.CompletionListener() {
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

                            if(dataSnapshot != null)
                            {
                                // For Debugging
                                // Log.e("Snapshot1",dataSnapshot.toString());
                                if(! dataSnapshot.hasChild(chatName))
                                {
                                    //If Unique Password In User Local Account
                                    register_user(chatId,chatName);
                                }
                                else
                                {
                                    Toast.makeText(getActivity(), "You Already Used This Chat Name For A Different DUO\nTry A New One", Toast.LENGTH_LONG).show();
                                    dismissProgressEnableBtn();
                                    mChatName.getEditText().getText().clear();//Only Clear Edit Text, So That User Can Understand Issue Better
                                }
                            }
                            else
                            {
                                Toast.makeText(getActivity(), "DataSnapshot - Null", Toast.LENGTH_SHORT).show();
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
        mChatId.getEditText().getText().clear();
        mChatName.getEditText().getText().clear();
        mChatId.getEditText().setText(mRootRef.child("ChatIds").push().getKey());
    }

    private void hideProgressEnableBtn()
    {
        mCreateBtn.setEnabled(true);
        mProgress.hide();
    }

    private void dismissProgressEnableBtn()
    {
        mCreateBtn.setEnabled(true);
        mProgress.dismiss();
    }

    private void resetFieldsDismissProgressEnableBtn() {
        mChatId.getEditText().getText().clear();
        mChatName.getEditText().getText().clear();
        dismissProgressEnableBtn();
    }

}
/*
TODO Triple the Toast Timing
Use Detailed Toast Message
OR,
If Possible Use Pop-Up Dialog
Set Pop-Up Disable Touch Outside - True*/