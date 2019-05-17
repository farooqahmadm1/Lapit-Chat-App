package com.example.farooq.lapitchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    //android Views
    private Toolbar toolbar;
    private EditText mEmail,mPassword;
    private Button mLogBtn;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        progressDialog= new ProgressDialog(this);
        toolbar = (Toolbar) findViewById(R.id.lToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEmail=(EditText)findViewById(R.id.lmEmail);
        mPassword=(EditText)findViewById(R.id.lmPassword);
        mLogBtn = (Button) findViewById(R.id.mLogIn);

        mLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                if( email.equals("") || password.equals("") ){
                    Toast.makeText(LoginActivity.this, "Please Enter your Information", Toast.LENGTH_SHORT).show();
                }else {
                    progressDialog.setTitle("Loging to Your Account");
                    progressDialog.setMessage("Please wait while we check your credential");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    login_user(email,password);
                }
            }
        });
    }

    private void login_user(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            String current = currentUser.getUid();
                            String mDeviceToken = FirebaseInstanceId.getInstance().getToken().toString();
                            mUserDatabase.child(current).child("device_token").setValue(mDeviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aoid) {
                                    progressDialog.dismiss();
                                    // Sign in success, update UI with the signed-in user's information
                                    Intent mainIntent= new Intent(LoginActivity.this,MainActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(mainIntent);
                                    finish();
                                }
                            });
                        } else {
                            progressDialog.hide();
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Invalid Username and Password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}