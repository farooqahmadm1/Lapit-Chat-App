package com.example.farooq.lapitchat;


import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mUserDatabase;

    private android.support.v7.widget.Toolbar main_page_toolbar;
    private TabLayout main_tabs;
    private ViewPager main_pager;
    private SectionPagerAdapter  sectionPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //firebase instance
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser= mAuth.getCurrentUser();
        if(mCurrentUser!=null){
            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(mCurrentUser.getUid()).child("online");
            mUserDatabase.keepSynced(true);
        }


        //toolbar
        main_page_toolbar =(android.support.v7.widget.Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(main_page_toolbar);
        main_page_toolbar.setTitle("Lapit Chat");

        //View Pager
        main_pager = (ViewPager) findViewById(R.id.main_tabs_pager);
        sectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        main_pager.setAdapter(sectionPagerAdapter);
        //TabLayout
        main_tabs = (TabLayout) findViewById(R.id.main_tabs);
        main_tabs.setupWithViewPager(main_pager);

    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null){
            sendStart();
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

    private void sendStart() {
        Intent intent =  new Intent(MainActivity.this,StartActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (R.id.logout == item.getItemId()) {
            FirebaseAuth.getInstance().signOut();
            sendStart();
        }
        if(item.getItemId() == R.id.settings){
            Intent intent =  new Intent(MainActivity.this,AccountSetting.class);
            startActivity(intent);
        }
        if(item.getItemId()==R.id.all_user){
            Intent intent =  new Intent(MainActivity.this,UsersActivity.class);
            startActivity(intent);
        }
        return true;
    }
}