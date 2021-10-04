package com.akshatagarwal1265.test1;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    /*private String mReadReceiptToggle;*/

    private EditText mChatMessageView;
    private ImageView mChatAddBtn;
    private ImageView mChatCameraBtn;
    private ImageView mChatSendBtn;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;

    private String mSelfUid;
    private String mChatUid;

    private TextView mUserNameView;
    private TextView mUserLastSeenView;
    private CircleImageView mUserThumbImageView;
    private CircleImageView mSelfThumbImageView;

    private RecyclerView mMessagesList;
    private final List<Message> messageList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;
    private SwipeRefreshLayout mRefreshLayout;

    private DatabaseReference messageRef;

    private static final int TOTAL_ITEMS_TO_LOAD = 25;
//    private static final int TOTAL_ITEMS_TO_RELOAD = 10;
//    private int mCurrentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        /*TODO - Customize Up Action - Set ClearTop|NewTask & finish() - No New Intent ChatSelectionActivity needed
        actionBar.setDisplayHomeAsUpEnabled(true);*/

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.custom_chat_app_bar, null);
        actionBar.setCustomView(action_bar_view);
        //TODO Check Bar Scrolling - If yes, Prevent actionBar from Scrolling Up - actionBar.setHideOnContentScrollEnabled(false);
        //TODO Remove space to right of Home Up Button

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mSelfUid = mAuth.getCurrentUser().getUid();

        //INTENTS
        mChatUid = getIntent().getStringExtra("chat_uid");

        mUserNameView = (TextView) findViewById(R.id.custom_app_bar_name);
        mUserLastSeenView = (TextView) findViewById(R.id.custom_app_bar_last_seen);
        mUserThumbImageView = (CircleImageView) findViewById(R.id.custom_app_bar_image);
        mSelfThumbImageView = (CircleImageView) findViewById(R.id.custom_app_bar_image_self);

        mChatAddBtn = (ImageView)findViewById(R.id.chat_add_btn);
        mChatSendBtn = (ImageView)findViewById(R.id.chat_send_btn);
        mChatCameraBtn = (ImageView)findViewById(R.id.chat_camera_btn);
        mChatMessageView = (EditText)findViewById(R.id.chat_message_view);

        mAdapter = new MessageAdapter(messageList);

        mMessagesList = (RecyclerView)findViewById(R.id.messages_list);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);

        mMessagesList.setAdapter(mAdapter);

        mRootRef.child("ChatIds").child(mChatUid).keepSynced(true);
        mRootRef.child("ChatIds").child(mChatUid).child("_dummy_").setValue("ChatActivity", new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if(databaseError != null)
                {
                    Toast.makeText(ChatActivity.this, "Network Error, Try Resubmitting", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //Getting UserUid
                    mRootRef.child("ChatIds").child(mChatUid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            mRootRef.child("ChatIds").child(mChatUid).child("_dummy_").setValue(null);

                            if(dataSnapshot!=null){
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    String key = snapshot.getKey();
                                    if (!key.equals("number_of_users") && !key.equals(mSelfUid) && !key.equals("_dummy_")) {
                                        String mUserUid = key;
                                        onCreateContinued(mUserUid);
                                    }
                                }
                            }else {
                                Toast.makeText(ChatActivity.this, "Network Error, Try Resubmitting", Toast.LENGTH_LONG).show();
                                Log.e("ERROR","DATASNAPSHOT = NULL");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(ChatActivity.this, "ERROR - Try Logging In Again", Toast.LENGTH_LONG).show();
                            Log.e("ERROR",databaseError.getMessage());
                        }
                    });
                }
            }
        });
    }

    private void onCreateContinued(final String mUserUid) {

        messageRef = mRootRef.child("Chats").child(mChatUid).child("Messages");
        messageRef.keepSynced(true);

        loadMessages();

        //Listener For UserName & Last-Seen (on App Action Bar)
        mRootRef.child("Users").child(mUserUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null)
                {
                    mUserNameView.setText(dataSnapshot.child("name").getValue().toString());
                    String lastSeen = dataSnapshot.child("online").getValue().toString();
                    if(lastSeen.equals("true"))
                    {
                        mUserLastSeenView.setText("online");
                    }
                    else
                    {
                        String lastSeenTime = GetTimeAgo.getTimeAgo(Long.parseLong(lastSeen),getApplicationContext());
                        mUserLastSeenView.setText(lastSeenTime);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        final ValueEventListener userGeneralThumbListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null)
                {
                    String userGeneralThumbImage = dataSnapshot.getValue().toString();
                    if(!userGeneralThumbImage.equals("default"))
                    {
                        Picasso.with(ChatActivity.this)
                                .load(userGeneralThumbImage)
                                .placeholder(R.drawable.default_avatar)
                                .into(mUserThumbImageView);
                    }
                    else
                    {
                        mUserThumbImageView.setImageResource(R.drawable.default_avatar);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };


        final ValueEventListener selfGeneralThumbListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null)
                {
                    String selfGeneralThumbImage = dataSnapshot.getValue().toString();
                    if(!selfGeneralThumbImage.equals("default"))
                    {
                        Picasso.with(ChatActivity.this)
                                .load(selfGeneralThumbImage)
                                .placeholder(R.drawable.default_avatar)
                                .into(mSelfThumbImageView);
                    }
                    else
                    {
                        mSelfThumbImageView.setImageResource(R.drawable.default_avatar);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };


        //Listener For UserInfo (on App Action Bar)
        mRootRef.child("Chats").child(mChatUid).child(mUserUid).keepSynced(true);
        mRootRef.child("Chats").child(mChatUid).child(mUserUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null)
                {
                    //Listener for User Thumb
                    String userSpecificThumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                    if(!userSpecificThumbImage.equals("default"))
                    {
                        Picasso.with(ChatActivity.this)
                                .load(userSpecificThumbImage)
                                .placeholder(R.drawable.default_avatar)
                                .into(mUserThumbImageView);
                    }
                    else
                    {
                        mRootRef.child("Users").child(mUserUid).child("thumb_image").addValueEventListener(userGeneralThumbListener);
                    }

                    //Listener for User LastSeenToggle
                    String userLastSeenToggle = dataSnapshot.child("last_seen").getValue().toString();
                    if(userLastSeenToggle.equals("true"))
                    {
                        mUserLastSeenView.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        mUserLastSeenView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        //Listener For SelfThumbImages (on App Action Bar)
        mRootRef.child("Chats").child(mChatUid).child(mSelfUid).keepSynced(true);
        mRootRef.child("Chats").child(mChatUid).child(mSelfUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot != null)
                {
                    //Listener for Self Thumb
                    String selfSpecificThumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                    if(!selfSpecificThumbImage.equals("default"))
                    {
                        Picasso.with(ChatActivity.this)
                                .load(selfSpecificThumbImage)
                                .placeholder(R.drawable.default_avatar)
                                .into(mSelfThumbImageView);
                    }
                    else
                    {
                        mRootRef.child("Users").child(mSelfUid).child("thumb_image").addValueEventListener(selfGeneralThumbListener);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        //Set Listeners
        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        mChatCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "Functionality Coming Soon...", Toast.LENGTH_LONG).show();
            }
        });
        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "Functionality Coming Soon...", Toast.LENGTH_LONG).show();
            }
        });

        //Set SwipeLayout RefreshListener
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMessages();
            }
        });
    }

    private void loadMessages() {

        final Query messageQuery;
        final int pos = messageList.size();

        if(messageList.isEmpty()) {
            messageQuery = messageRef.orderByChild("timestamp").limitToLast(TOTAL_ITEMS_TO_LOAD);
            mRefreshLayout.setRefreshing(false);
        }else {
            messageQuery = messageRef.orderByChild("timestamp").endAt(messageList.get(0).getTimestamp()).limitToLast(TOTAL_ITEMS_TO_LOAD);
        }
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Message message = dataSnapshot.getValue(Message.class);
                if(pos==0 || !message.toString().equals(messageList.get(messageList.size()-pos).toString())) {
                    messageList.add(messageList.size()-pos, message);
                    mAdapter.notifyDataSetChanged();
                    //TODO Try to Use notifyItemInserted instead of notifyDataSetChanged

                    mMessagesList.scrollToPosition(messageList.size()-pos-1);
                    //For Debugging
                    //Log.e("ERRRRRRRRRRRRRRRRRRRRRR", "pos = "+pos+" size = "+messageList.size()+" LIST = "+messageList);

                    if(!message.getFrom().equals(mSelfUid))
                    {
                        if(!message.getSeen())
                            messageRef.child(dataSnapshot.getKey()).child("seen").setValue(true);
                    }
                }

                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //TODO notifyItemChanged, when message gets seen, change messageList and notifyItemChanged
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {

        mChatSendBtn.setEnabled(false);

        String message = mChatMessageView.getText().toString().trim();

        if(!TextUtils.isEmpty(message))
        {

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("timestamp", ServerValue.TIMESTAMP);
            messageMap.put("type","text");
            messageMap.put("from",mSelfUid);

            mRootRef.child("Chats").child(mChatUid).child("Messages").push().updateChildren(messageMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError != null)
                    {
                        Log.i("ChatActivity",databaseError.getMessage());
                        Toast.makeText(ChatActivity.this, "Retry", Toast.LENGTH_SHORT).show();
                        mChatSendBtn.setEnabled(true);
                    }
                    else
                    {
                        mChatMessageView.getText().clear();
                        mChatSendBtn.setEnabled(true);
                    }
                }
            });
        }
        else
        {
            mChatSendBtn.setEnabled(true);
        }
    }
}