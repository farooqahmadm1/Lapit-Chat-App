package com.example.farooq.lapitchat;

import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    private static final String TAG = "ChatActivity";
    private int currentPage = 1;
    private int itemPos = 0;
    private String mLastKey="";
    private String mPreKey="";
    private static final int LOAD_ITEMS_LIMIT = 10;
    //Views
    private String mChatUser;
    private Toolbar mChatToolbar;
    private TextView mChatUserName,mChatUserLastSeen;
    private CircleImageView mChatUserCicleImage;
    private Button mChatMessageAddBtn,mChatMessageSendBtn;
    private EditText mChatMessageEditText;
    private RecyclerView  mRecyclerView;
    private SwipeRefreshLayout refreshLayout;

    private List<Messages> list;
    private MessageAdapter adapter;
    //Firebase
    private DatabaseReference mRootRef;
    private FirebaseUser currentUser;
    private String mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Log.d(TAG,"onCreate : start");
        //Views
        mChatToolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(mChatToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Log.d(TAG,"onCreate : Toolbar set");

        mChatUserName = (TextView) findViewById(R.id.chat_toolbar_display_name);
        mChatUserLastSeen = (TextView) findViewById(R.id.chat_toolbar_last_seen);
        mChatUserCicleImage = (CircleImageView) findViewById(R.id.chat_toolbar_image);
        mChatMessageAddBtn =  (Button) findViewById(R.id.chat_user_add_button);
        mChatMessageSendBtn = (Button) findViewById(R.id.chat_user_send_button);
        mChatMessageEditText =(EditText) findViewById(R.id.chat_user_message);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.chat_message_swipe_layout);

        list = new ArrayList<Messages>();

        Log.d(TAG,"onCreate : view ans list intilize");

        mRecyclerView =  (RecyclerView)  findViewById(R.id.chat_message_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mRecyclerView.setHasFixedSize(false);

        Log.d(TAG,"onCreate : Recyler View has been intilized");

        //Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUser = currentUser.getUid();
        mChatUser = getIntent().getStringExtra("user_id");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        adapter = new MessageAdapter(this,list);
        mRecyclerView.setAdapter(adapter);
        Log.d(TAG,"onCreate :   recyler view adapter is set");
        loadMessages();
        Log.d(TAG,"onCreate : Messages things are loading");
        mRootRef.child("users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mChatUserName.setText(dataSnapshot.child("name").getValue().toString());
                Picasso.get().load(dataSnapshot.child("thumbnail").getValue().toString())
                        .placeholder(R.drawable.profile).into(mChatUserCicleImage);
                if (dataSnapshot.hasChild("online")){
                    String online=dataSnapshot.child("online").getValue().toString();
                    if (online.equals("online")){
                        mChatUserLastSeen.setText(online);
                    }else {
                        GetTimeAgo getTimeAgo= new GetTimeAgo();
                        long lastSeen = Long.parseLong(online);
                        mChatUserLastSeen.setText(getTimeAgo.getTimeAgo(lastSeen,getApplicationContext()).toString());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
        mRootRef.child("chat").child(mCurrentUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(mChatUser)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp",ServerValue.TIMESTAMP);

                    Map chatMap = new HashMap();
                    chatMap.put("chat/" + mCurrentUser + "/" + mChatUser ,chatAddMap);
                    chatMap.put("chat/" + mChatUser + "/" + mCurrentUser ,chatAddMap);

                    mRootRef.updateChildren(chatMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null){ }
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage++;
                itemPos=0;
                loadMoreMessages();
            }
        });
    }
    private void loadMoreMessages() {
        Log.d(TAG,"loadMoreMessages : start");
        mRootRef.keepSynced(true);
        DatabaseReference reference =mRootRef.child("messages").child(mCurrentUser).child(mChatUser);
        Query messageQuery = reference.orderByKey().endAt(mLastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG,"loadMessages : onChildAdded ");
                Messages c= dataSnapshot.getValue(Messages.class);
                String message = dataSnapshot.getKey();

                if (!mPreKey.equals(message)){
                    list.add(itemPos++,c);
                }else {
                    mPreKey=mLastKey;
                }

                if (itemPos==1){

                    mLastKey= message;
                }
                adapter.notifyDataSetChanged();
                mRecyclerView.scrollToPosition(list.size()-1);
                refreshLayout.setRefreshing(false);
            }
            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
            @Override public void onChildRemoved(DataSnapshot dataSnapshot) { }
            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
            @Override public void onCancelled(DatabaseError databaseError) { }
        });
    }
    private void loadMessages() {
        Log.d(TAG,"loadMessages : start");
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("messages").child(mCurrentUser).child(mChatUser);
        Query messageQuery = reference.limitToLast(currentPage * LOAD_ITEMS_LIMIT);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG,"loadMessages : onChildAdded ");
                Messages c= dataSnapshot.getValue(Messages.class);

                list.add(c);
                itemPos++;
                if (itemPos==1){
                    String message = dataSnapshot.getKey();
                    mLastKey= message;
                    mPreKey=message;
                }
                adapter.notifyDataSetChanged();
                mRecyclerView.scrollToPosition(list.size()-1);
            }
            @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
            @Override public void onChildRemoved(DataSnapshot dataSnapshot) { }
            @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
            @Override public void onCancelled(DatabaseError databaseError) { }
        });
    }
    public void sendMessage(View view) {
        String message = mChatMessageEditText.getText().toString();
        if(!message.equals("")){
            mChatMessageEditText.setText("");
            String key = mRootRef.child("messages").child(mCurrentUser).child(mChatUser).push().getKey().toString();

            String current_user_ref = "messages/" + mCurrentUser + "/" + mChatUser;
            String chat_user_ref =    "messages/" + mChatUser + "/" + mCurrentUser;

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",mCurrentUser);

            Map messageUserMap =  new HashMap();
            messageUserMap.put(current_user_ref + "/" + key,messageMap);
            messageUserMap.put(chat_user_ref + "/" + key ,messageMap);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError !=  null){
                        Log.d(TAG,"Error  in sendig meassages");
                    }
                }
            });
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
        refreshLayout.setRefreshing(false);
        LapitChat.activityPaused();
    }
}
