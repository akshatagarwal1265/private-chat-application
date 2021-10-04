package com.akshatagarwal1265.test1;

import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Akshat on 20-09-2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> mMessageList;
    private FirebaseAuth mAuth;
    private String mCurrentUid;

    public MessageAdapter (List<Message> mMessageList)
    {
        this.mMessageList = mMessageList;
        //Initialization
        mAuth = FirebaseAuth.getInstance();
        mCurrentUid = mAuth.getCurrentUser().getUid();
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_chat_bubble, parent, false);
        return  new MessageViewHolder(v);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText;
        public TextView timeText;
        public ImageView seenIndicator;
        public RelativeLayout parentRelative;
        public FrameLayout parentFrame;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.custom_bubble_message_text);
            timeText = (TextView) itemView.findViewById(R.id.custom_bubble_message_time);
            seenIndicator = (ImageView) itemView.findViewById(R.id.custom_bubble_message_seen);
            parentRelative = (RelativeLayout) itemView.findViewById(R.id.custom_bubble_parent_relative);
            parentFrame = (FrameLayout) itemView.findViewById(R.id.custom_bubble_parent_frame);
        }
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {

        //Getting Current Item
        Message messageItem = mMessageList.get(position);

        //Message Text
        holder.messageText.setText(messageItem.getMessage());

        //Message Time
        Long longTime = messageItem.getTimestamp();
        Date timeObject = new Date(longTime);
        SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mm a");
        String timeToDisplay = timeFormatter.format(timeObject);
        holder.timeText.setText(timeToDisplay);

        float factor = holder.parentRelative.getContext().getResources().getDisplayMetrics().density;
        int leftMargin = (int)(16 * factor);
        int rightMargin = (int)(72 * factor);
        int topMargin;
        String currentFromUid = messageItem.getFrom();

        //Set Bunch Message TopMargin
        if(position==0 || !mMessageList.get(position-1).getFrom().equals(currentFromUid))
            topMargin = (int)(12 * factor);
        else
            topMargin = (int)(4 * factor);

        //Background
        if(mCurrentUid.equals(currentFromUid))
        {
            holder.parentRelative.setBackgroundResource(R.drawable.bubble_sent_background);
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams)holder.parentFrame.getLayoutParams();
            lp.setMargins(rightMargin, topMargin, leftMargin, 0);  // left, top, right, bottom
            holder.parentFrame.setLayoutParams(lp);
            FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams)holder.parentRelative.getLayoutParams();
            flp.gravity = Gravity.END;
            holder.parentRelative.setLayoutParams(flp);
            holder.seenIndicator.setVisibility(View.VISIBLE);

            if(!messageItem.getSeen())
            {
                holder.seenIndicator.setImageResource(R.drawable.ic_done_all_black_24dp);
            }
            else
            {
                holder.seenIndicator.setImageResource(R.drawable.ic_visibility_black_24dp);
            }
        }
        else
        {
            holder.parentRelative.setBackgroundResource(R.drawable.bubble_received_background);
            RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams)holder.parentFrame.getLayoutParams();
            lp.setMargins(leftMargin, topMargin, rightMargin, 0);  // left, top, right, bottom
            holder.parentFrame.setLayoutParams(lp);
            FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams)holder.parentRelative.getLayoutParams();
            flp.gravity = Gravity.START;
            holder.parentRelative.setLayoutParams(flp);
            holder.seenIndicator.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}
