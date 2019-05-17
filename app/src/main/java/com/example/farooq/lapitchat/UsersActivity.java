package com.example.farooq.lapitchat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        //Toolbar
        toolbar = (Toolbar) findViewById(R.id.user_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //RecyclerView
        recyclerView = (RecyclerView) findViewById(R.id.user_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //Creating Database refrence
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        databaseReference.keepSynced(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Users,UserViewHolder> adapter = new FirebaseRecyclerAdapter<Users, UserViewHolder>(
                Users.class,
                R.layout.user_single_layout,
                UserViewHolder.class,
                databaseReference
        ) {
            @Override
            protected void populateViewHolder(UserViewHolder holder, Users model, int position) {

                holder.setName(model.getName());
                holder.setStatus(model.getStatus());
                holder.setThumbnail(model.getThumbnail());
                final String user_id = getRef(position).getKey().toString();
                holder.mItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(UsersActivity.this,ProfileActivity.class);
                        intent.putExtra("id",user_id);
                        startActivity(intent);
                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);

    }
    public static class UserViewHolder extends RecyclerView.ViewHolder{

        public View mItemView;
        public UserViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
        }
        public void setName(String name){
            TextView mName = (TextView) mItemView.findViewById(R.id.user_name);
            mName.setText(name);
        }
        public void setStatus(String status){
            TextView mStatus =(TextView) mItemView.findViewById(R.id.user_status);
            mStatus.setText(status);
        }
        public void setThumbnail(String thumbnail) {
            CircleImageView mImage =(CircleImageView) mItemView.findViewById(R.id.user_image);
            //picasso image downloading and
            Picasso.get().load(thumbnail).placeholder(R.drawable.profile).into(mImage);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        LapitChat.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LapitChat.activityPaused();
    }
}
