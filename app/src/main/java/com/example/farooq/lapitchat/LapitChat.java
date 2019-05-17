package com.example.farooq.lapitchat;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class LapitChat extends Application {

    private static boolean activityVisible;
    static DatabaseReference mUserDatabase;
    static FirebaseUser currentUser;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);


    }

    public static void activityResumed() {
        activityVisible = true;
        onlineStatus();
    }

    public static void activityPaused() {
        activityVisible = false;
        onlineStatus();
    }
    public static void onlineStatus(){
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser!=null) {
            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());
            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        mUserDatabase.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
//                        if(activityVisible==false){
//                            mUserDatabase.child("online").setValue(ServerValue.TIMESTAMP);
//                        }else{
//                            mUserDatabase.child("online").setValue("online");
//                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
