package com.example.farooq.lapitchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegsiterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private DatabaseReference reference;
    private EditText mDisplayName;
    private EditText mEmail;
    private EditText mPassword;
    private Button mCreateBtn;

    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regsiter);
        //create instance of firebase
        mAuth = FirebaseAuth.getInstance();
        toolbar =(Toolbar) findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressDialog = new ProgressDialog(this);

        mDisplayName = (EditText) findViewById(R.id.rmDisplayName);
        mEmail =(EditText) findViewById(R.id.rmEmail);
        mPassword =(EditText) findViewById(R.id.rmPassword);
        mCreateBtn= (Button) findViewById(R.id.mCreateBtn);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String displayName =mDisplayName.getText().toString().trim();
                String email =mEmail.getText().toString().trim();
                String password =mPassword.getText().toString().trim();
                if(displayName.equals("") || email.equals("") || password.equals("")){
                    Toast.makeText(RegsiterActivity.this, "Please Enter Your Information", Toast.LENGTH_SHORT).show();
                }else{
                    progressDialog.setTitle("Registering User");
                    progressDialog.setMessage("Please wait while we create your account");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    register_user(displayName,email,password);
                }

            }
        });
    }

    private void register_user(final String displayName, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String uid= currentUser.getUid().toString();
                            String mDeviceToken = FirebaseInstanceId.getInstance().getToken().toString();

                            reference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
                            HashMap<String,String> hashMap= new HashMap<String, String>();
                            hashMap.put("name",displayName);
                            hashMap.put("status","Hi there, I am using Lapit Chat App");
                            hashMap.put("image","Default");
                            hashMap.put("thumbnail","default");
                            hashMap.put("device_token",mDeviceToken);
                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        progressDialog.dismiss();
                                        Intent mainIntent= new Intent(RegsiterActivity.this,MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                }
                            });
                        } else {
                            progressDialog.hide();
                            Toast.makeText(RegsiterActivity.this, "some error please try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}