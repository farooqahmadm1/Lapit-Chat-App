package com.example.farooq.lapitchat.MainFragment;


import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.farooq.lapitchat.ChatActivity;
import com.example.farooq.lapitchat.ProfileActivity;
import com.example.farooq.lapitchat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatsFragment extends Fragment {

    private DatabaseReference databaseReference;
    private DatabaseReference userDatabase;
    private FirebaseUser currentUser;
    private String mCurrentUser;

    private View mChatView;
    private RecyclerView recyclerView;

    public ChatsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mChatView = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = (RecyclerView) mChatView.findViewById(R.id.fragment_chat_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUser = currentUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("chat").child(mCurrentUser);
        databaseReference.keepSynced(true);
        userDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        userDatabase.keepSynced(true);
        return mChatView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Chats, ChatsFragment.ChatViewHolder> adapter =
                new FirebaseRecyclerAdapter<Chats, ChatsFragment.ChatViewHolder>(
                Chats.class,
                R.layout.chat_single_layout,
                ChatsFragment.ChatViewHolder.class,
                databaseReference
        ) {
            @Override
            protected void populateViewHolder(final ChatsFragment.ChatViewHolder viewHolder, Chats model, int position) {

                final String uid = getRef(position).getKey().toString();
                userDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child(uid).child("name").getValue().toString();
                        String image = dataSnapshot.child(uid).child("thumbnail").getValue().toString();
                        viewHolder.setName(name);
                        viewHolder.setImage(image);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                        ActivityOptions options = ActivityOptions.makeCustomAnimation(getContext(), android.R.anim.fade_in, android.R.anim.fade_out);
                        chatIntent.putExtra("user_id", uid);
                        chatIntent.putExtra("current_id", mCurrentUser);
                        startActivity(chatIntent,options.toBundle());
                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);

    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        View itemView;

        public ChatViewHolder(View ItemView) {
            super(ItemView);
            itemView = ItemView;
        }

        public void setName(final String name) {
            TextView friendName = itemView.findViewById(R.id.fragment_chat_name);
            friendName.setText(name);
        }

        public void setImage(final String image) {
            CircleImageView mImage = (CircleImageView) itemView.findViewById(R.id.fragment_chat_image);
            //picasso image downloading and
            Picasso.get().load(image).placeholder(R.drawable.profile).into(mImage);
        }
    }
}