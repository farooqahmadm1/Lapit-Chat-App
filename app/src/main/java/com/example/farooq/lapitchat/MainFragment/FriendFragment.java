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


public class FriendFragment extends Fragment {

    private DatabaseReference databaseReference;
    private DatabaseReference userDatabase;
    private FirebaseUser currentUser;
    private String mCurrentUser;


    private View mFriendView;
    private RecyclerView recyclerView;
    public FriendFragment() { }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mFriendView=inflater.inflate(R.layout.fragment_friend, container, false);
        recyclerView =(RecyclerView) mFriendView.findViewById(R.id.friend_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUser= currentUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("friends").child(mCurrentUser);
        databaseReference.keepSynced(true);
        userDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        userDatabase.keepSynced(true);

        return mFriendView;
    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Friends,FriendViewHolder> adapter= new FirebaseRecyclerAdapter<Friends, FriendViewHolder>(
                Friends.class,
                R.layout.friend_single_layout,
                FriendViewHolder.class,
                databaseReference
        ){
            @Override
            protected void populateViewHolder(final FriendViewHolder viewHolder, Friends model, int position) {

                viewHolder.setDate(model.getDate());
                final String uid = getRef(position).getKey().toString();
                userDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child(uid).child("name").getValue().toString();
                        String image = dataSnapshot.child(uid).child("thumbnail").getValue().toString();
                        DataSnapshot v=dataSnapshot.child(uid);
                        if(v.hasChild("online")){
                            String onlineStatus = dataSnapshot.child(uid).child("online").getValue().toString();
                            viewHolder.setStatus(onlineStatus);
                        }
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
                        CharSequence charSequence[] = new CharSequence[] {"Open Profile","Send Message"};
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Select Options");
                        builder.setItems(charSequence, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which==0){
                                    Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                    profileIntent.putExtra("id",uid);
                                    startActivity(profileIntent);
                                }else if(which==1){
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("user_id",uid);
                                    chatIntent.putExtra("current_id",mCurrentUser);
                                    startActivity(chatIntent);
                                }
                            }
                        });
                        builder.show();
                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);

    }
    public static class FriendViewHolder extends RecyclerView.ViewHolder{
        View itemView;

        public FriendViewHolder(View ItemView)
        {
            super(ItemView);
            itemView = ItemView;
        }
        public void setDate(final String date) {
            TextView friendSinceDate = itemView.findViewById(R.id.friend_status);
            friendSinceDate.setText(date);
        }
        public void setName(final String name) {
            TextView friendName = itemView.findViewById(R.id.friend_name);
            friendName.setText(name);
        }
        public void setImage(final String image) {
            CircleImageView mImage =(CircleImageView) itemView.findViewById(R.id.friend_image);
            //picasso image downloading and
            Picasso.get().load(image).placeholder(R.drawable.profile).into(mImage);
        }

        public void setStatus(String onlineStatus) {
            TextView mUserOnlineStatus =  (TextView) itemView.findViewById(R.id.friend_online_status);
            if(onlineStatus.equals("online")){
                mUserOnlineStatus.setVisibility(View.VISIBLE);
            }else {
                mUserOnlineStatus.setVisibility(View.INVISIBLE);
            }
        }
    }
}