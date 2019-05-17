package com.example.farooq.lapitchat;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class StatusActivity extends AppCompatActivity {
    private Toolbar statusToolbar;
    private DatabaseReference reference;
    private FirebaseUser current_user;
    private ProgressDialog progressDialog;

    //android View
    private EditText status_edit_text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        //Toolbar
        statusToolbar =(Toolbar)  findViewById(R.id.status_toolbar);
        setSupportActionBar(statusToolbar);
        getSupportActionBar().setTitle("Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        status_edit_text =(EditText) findViewById(R.id.status_edit_text);
        status_edit_text.setText(getIntent().getStringExtra("status_text").toString());

        current_user = FirebaseAuth.getInstance().getCurrentUser();
        String uid =  current_user.getUid().toString();
        reference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
    }

    public void status_save_change(View view) {
        progressDialog = new ProgressDialog(StatusActivity.this);
        progressDialog.setTitle("Saving Changes...");
        progressDialog.setMessage("Please Wait while we update your Status.");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        reference.child("status").setValue(status_edit_text.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    progressDialog.dismiss();
                }else {
                    progressDialog.dismiss();
                    Toast.makeText(StatusActivity.this, "Some Error Ocurring, Please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
