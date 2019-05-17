package com.example.farooq.lapitchat;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    List<Messages> mlistMessages;
    String mCurrentUser;
    Context mContext;

    public MessageAdapter(Context context,List<Messages> mlistMessages) {
        this.mlistMessages = mlistMessages;
        mContext=context;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);
        MessageViewHolder holder=  new MessageViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Messages c = mlistMessages.get(position);

        //Layout Changing
        FirebaseUser currentUser= FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUser=currentUser.getUid();
        if(c.getFrom().equals(mCurrentUser)){
            holder.messageText.setBackgroundResource(R.drawable.message_send_background_layout);
            holder.messageText.setTextColor(Color.BLACK);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.messageText.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
            holder.messageText.setLayoutParams(params);
        }else {
            holder.messageText.setBackgroundResource(R.drawable.message_received_background_layout);
            holder.messageText.setTextColor(Color.WHITE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)holder.messageText.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.messageText.setLayoutParams(params);
        }
        holder.messageText.setText(c.getMessage());
        String timeAgo = GetTimeAgo.getTimeAgo(c.getTime(),mContext);
        holder.messageTime.setText(timeAgo);
    }
    @Override
    public int getItemCount() {
        return mlistMessages.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public TextView messageTime;
        public MessageViewHolder(View itemView) {
            super(itemView);
            messageText = (TextView) itemView.findViewById(R.id.message_single_message);
            messageTime = (TextView) itemView.findViewById(R.id.message_single_time);
        }
    }
}
