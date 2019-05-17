package com.example.farooq.lapitchat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class AccountSetting extends AppCompatActivity {
    private static final String TAG = "AccountSetting";
    public static final int RESULT_LOAD_IMG= 1000;

    //Firebase
    private DatabaseReference reference;
    private StorageReference storageReference;
    private FirebaseUser currentUser;
    private String uid;
    //Android Layout
    private TextView mName,mStatus;
    private CircleImageView mImage;
    private Button changeStatus;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);
        Log.d(TAG,"onCreate : start");
        //initialize the views
        mName=(TextView) findViewById(R.id.account_display_name);
        mStatus=(TextView) findViewById(R.id.account_status);
        mImage=(CircleImageView) findViewById(R.id.profile_image);

        //Firebase database Storage Refrence
        storageReference = FirebaseStorage.getInstance().getReference();

        //get current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        uid= currentUser.getUid();

        //get reference of current user and its data

        reference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name= dataSnapshot.child("name").getValue().toString();
                String status= dataSnapshot.child("status").getValue().toString();
                String image= dataSnapshot.child("image").getValue().toString();

                mName.setText(name);
                mStatus.setText(status);
                if(!image.equals("Default")) {
                    //picasso image downloading and
                    Log.d(TAG,"onCreate : start");
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(mImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    //Change profile Image
    public void changeImage(View view) {
        // start picker to get image for cropping and then use the image in cropping activity
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_LOAD_IMG) {
            if (resultCode == RESULT_OK) {
                //progress Dialog
                progressDialog = new ProgressDialog(AccountSetting.this);
                progressDialog.setTitle("Uploading Image...");
                progressDialog.setMessage("Please wait while we upload and proccess the image");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                //here it get thee image that we want to select and crop
                Uri resultUri = data.getData();
                File thumb_file = new File(resultUri.getPath());

                try {
                    Bitmap thumb_bitmap= new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_file);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
                    final byte[] thumb_byte = baos.toByteArray();
                    //here we upload the image to storage database
                    final StorageReference filePath =storageReference.child("profile_images").child(uid+".jpg");
                    final StorageReference thumFilePath =storageReference.child("profile_images").child("thumb").child(uid+".jpg");

                    UploadTask uploadTask = filePath.putFile(resultUri);
                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            // Continue with the task to get the download URL
                            return filePath.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                final String downloadUrl = task.getResult().toString();
                                UploadTask uploadTaskThumb = thumFilePath.putBytes(thumb_byte);
                                Task<Uri> urlTask = uploadTaskThumb.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                    @Override
                                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> taskThumb) throws Exception {
                                        if (!taskThumb.isSuccessful()) {
                                            throw taskThumb.getException();
                                        }
                                        // Continue with the task to get the download URL
                                        return thumFilePath.getDownloadUrl();
                                    }
                                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> taskThumb) {
                                        if (taskThumb.isSuccessful()) {
                                            String downloadUrl_thumb = taskThumb.getResult().toString();
                                            Map map= new HashMap();
                                            map.put("image",downloadUrl);
                                            map.put("thumbnail",downloadUrl_thumb);
                                            reference.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        progressDialog.dismiss();
                                                    }else{
                                                        progressDialog.dismiss();
                                                        Toast.makeText(AccountSetting.this, "Error in uploading", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        } else {
                                            progressDialog.dismiss();
                                            Toast.makeText(AccountSetting.this, "Error in uploading", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(AccountSetting.this, "Error in uploading", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                    Toast.makeText(AccountSetting.this, "Error in uploading", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void changeStatus(View view) {
        Intent  statusIntent = new Intent(AccountSetting.this,StatusActivity.class);
        statusIntent.putExtra("status_text",mStatus.getText().toString());
        startActivity(statusIntent);
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