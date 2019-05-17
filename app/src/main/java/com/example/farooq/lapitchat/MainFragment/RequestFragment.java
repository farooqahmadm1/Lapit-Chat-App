package com.example.farooq.lapitchat.MainFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.farooq.lapitchat.ChatActivity;
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

import java.io.BufferedReader;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestFragment extends Fragment {

    private DatabaseReference friendRequestReference;
    private DatabaseReference userDatabase;
    private FirebaseUser currentUser;
    private String mCurrentUser;

    //Views
    private RecyclerView recyclerViewAccept;
    private View mRequestView;

    public RequestFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRequestView = inflater.inflate(R.layout.fragment_request, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        //RecyclerView

        recyclerViewAccept = (RecyclerView) mRequestView.findViewById(R.id.request_fragment_recycler_view_accept);
        recyclerViewAccept.setLayoutManager(layoutManager);
        recyclerViewAccept.setHasFixedSize(true);
        //Creating Database refrence
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUser = currentUser.getUid();
        friendRequestReference = FirebaseDatabase.getInstance().getReference().child("friend_req").child(mCurrentUser);
        friendRequestReference.keepSynced(true);
        userDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        userDatabase.keepSynced(true);

        return mRequestView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Accept, RequestFragment.AcceptViewHolder> acceptAdapter =
                new FirebaseRecyclerAdapter<Accept, RequestFragment.AcceptViewHolder>(
                Accept.class,
                R.layout.request_single_layout,
                RequestFragment.AcceptViewHolder.class,
                friendRequestReference
        ) {
            @Override
            protected void populateViewHolder(final RequestFragment.AcceptViewHolder viewHolder, Accept model, int position) {

                String requestType = model.getRequest_type();

                final String uid = getRef(position).getKey().toString();
                userDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child(uid).child("name").getValue().toString();
                        String image = dataSnapshot.child(uid).child("thumbnail").getValue().toString();
                        String status = dataSnapshot.child(uid).child("status").getValue().toString();
                        viewHolder.setStatus(status);
                        viewHolder.setName(name);
                        viewHolder.setImage(image);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        recyclerViewAccept.setAdapter(acceptAdapter);
    }

    //View Holder class for Accept Request
    public static class AcceptViewHolder extends RecyclerView.ViewHolder {
        View acceptView;
        Button acceptButton;
        Button declineButton;
        public AcceptViewHolder(View itemView) {
            super(itemView);
            acceptView = itemView;
            declineButton = acceptView.findViewById(R.id.request_fragment_button_decline);
            acceptButton = acceptView.findViewById(R.id.request_fragment_button);
        }
        public void setButtons(String type) {
            declineButton.setVisibility(View.INVISIBLE);
            acceptButton.setText("Cancel");
        }
        public void setStatus(final String status) {
            TextView mStatus = acceptView.findViewById(R.id.request_fragment_status);
            mStatus.setText(status);
        }
        public void setName(final String name) {
            TextView acceptName = acceptView.findViewById(R.id.request_fragment_name);
            acceptName.setText(name);
        }
        public void setImage(final String image) {
            CircleImageView mImage = (CircleImageView) acceptView.findViewById(R.id.request_fragment_image);
            //picasso image downloading and
            Picasso.get().load(image).placeholder(R.drawable.profile).into(mImage);
        }
    }
}