package niwigh.com.smartchat.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;
import niwigh.com.smartchat.core.ImageCompressTask;
import niwigh.com.smartchat.listeners.IImageCompressTaskListener;

public class AddNewPost extends AppCompatActivity {
    Toolbar toolbar;
    ImageView post_image;
    FloatingActionButton btn_add_post_image;
    EditText post_title;
    EditText post_description;
    Button publish_post;
    final static int gallery_Pic = 1;
    String post_Title, post_Description;
    StorageReference postsImagesStorageRef;
    String saveCurrentDate, saveCurrentTime, postRandomName, currentUserID;
    ProgressDialog loadingBar;
    DatabaseReference usersRef, allPostsRef;
    FirebaseAuth mAuths;
    long countPosts = 0;
    private static final int REQUEST_STORAGE_PERMISSION = 100;

    //create a single thread pool to our image compression class.
    private ExecutorService mExecutorService = Executors.newFixedThreadPool(1);

    private ImageCompressTask imageCompressTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_post);

        postsImagesStorageRef = FirebaseStorage.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);
        allPostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts");
        allPostsRef.keepSynced(true);
        mAuths = FirebaseAuth.getInstance();
        currentUserID = mAuths.getCurrentUser().getUid();



        toolbar = findViewById(R.id.add_new_post_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add New Image Post");

        //init views
        post_image = findViewById(R.id.post_image);
        btn_add_post_image = findViewById(R.id.btn_add_post_image);
        post_title = findViewById(R.id.post_title_edit_text);
        post_description = findViewById(R.id.post_description_edit_text);
        publish_post = findViewById(R.id.btn_publish_post);
        loadingBar = new ProgressDialog(this);



        btn_add_post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });

        publish_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatePostInfo();
            }
        });
    }

    private void requestPermission() {
        if(PackageManager.PERMISSION_GRANTED !=
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            }else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            }
        }else {
            //Permission Granted, lets go pick photo
            selectPostImageFromGallery();
        }
    }


    public void validatePostInfo() {

        post_Title = post_title.getText().toString();
        post_Description = post_description.getText().toString();


         if(post_Title.isEmpty() || post_Description.isEmpty()){
            //all fields are required dialog
            showAllFieldsAreRequired();
        }
        else {
             if(post_image.getTag() == null){
                 uploadPost();
             }else{
                 storeImageToFirebaseStorage();
             }
        }
    }


    @SuppressLint("SimpleDateFormat")
    private void uploadPost(){
        loadingBar.setTitle("Uploading post");
        loadingBar.setMessage("a moment please...");
        loadingBar.setCancelable(false);
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();


        //for date
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
        saveCurrentDate = currentDate.format(calFordDate.getTime());
        //for time
        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime.format(calFordTime.getTime());

        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    //get the user details
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String post_title_lowercase = post_Title.toLowerCase();

                    //save the posts details to database
                    Map<String,Object> postsMap = new HashMap<String, Object>();
                    postsMap.put("uid", currentUserID);
                    postsMap.put("date", saveCurrentDate);
                    postsMap.put("time", saveCurrentTime);
                    postsMap.put("title", post_Title);
                    postsMap.put("description", post_Description);
                    postsMap.put("postimage", "");
                    postsMap.put("profileimage", userProfileImage);
                    postsMap.put("fullname", userFullName);
                    postsMap.put("type", "image");
                    postsMap.put("posttitletolowercase", post_title_lowercase);
                    postsMap.put("timestamp", ServerValue.TIMESTAMP);
                    postsMap.put("postfilestoragename", "");
                    postsMap.put("counter", countPosts);

                    allPostsRef.child(currentUserID + postRandomName).updateChildren(postsMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @SuppressLint("SetTextI18n")
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        post_image.setImageResource(R.drawable.easy_to_use);
                                        post_title.setText("");
                                        post_description.setText("");
                                        loadingBar.dismiss();
                                        finish();
                                    }
                                    else {
                                        //{.....................
                                        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                                        ViewGroup viewGroup = findViewById(android.R.id.content);

                                        //then we will inflate the custom alert dialog xml that we created
                                        View dialogView = LayoutInflater.from(AddNewPost.this).inflate(R.layout.error_dialog, viewGroup, false);


                                        //Now we need an AlertDialog.Builder object
                                        AlertDialog.Builder builder = new AlertDialog.Builder(AddNewPost.this);

                                        //setting the view of the builder to our custom view that we already inflated
                                        builder.setView(dialogView);

                                        //finally creating the alert dialog and displaying it
                                        final AlertDialog alertDialog = builder.create();

                                        Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
                                        TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
                                        TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

                                        dialog_btn.setText("OK");
                                        success_title.setText("Error");
                                        success_text.setText("An unexpected error occured while uploading your post.");

                                        // if the OK button is clicked, close the success dialog
                                        dialog_btn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                alertDialog.dismiss();
                                            }
                                        });

                                        alertDialog.show();
                                        //...................}
                                        loadingBar.dismiss();
                                    }
                                }
                            });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == gallery_Pic && resultCode == RESULT_OK &&
                data != null) {
            //extract absolute image path from Uri
            Uri uri = data.getData();
            Cursor cursor = MediaStore.Images.Media.query(getContentResolver(), uri, new String[]{MediaStore.Images.Media.DATA});

            if(cursor != null && cursor.moveToFirst()) {
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                //Create ImageCompressTask and execute with Executor.
                imageCompressTask = new ImageCompressTask(this, path, iImageCompressTaskListener);

                mExecutorService.execute(imageCompressTask);
            }
        }
    }

    //image compress task callback
    private IImageCompressTaskListener iImageCompressTaskListener = new IImageCompressTaskListener() {
        @Override
        public void onComplete(List<File> compressed) {
            //photo compressed. Yay!

            //prepare for uploads. Use an Http library like Retrofit, Volley or async-http-client (My favourite)

            File file = compressed.get(0);
            Uri compressedImageUri = Uri.fromFile(file);

            Log.d("ImageCompressor", "New photo size ==> " + file.length()); //log new file size.

            post_image.setImageURI(compressedImageUri);
            post_image.setTag(compressedImageUri);

        }

        @Override
        public void onError(Throwable error) {
            //very unlikely, but it might happen on a device with extremely low storage.
            //log it, log.WhatTheFuck?, or show a dialog asking the user to delete some files....etc, etc
            Log.wtf("ImageCompressor", "Error occurred", error);
        }
    };


    @SuppressLint("SimpleDateFormat")
    private void storeImageToFirebaseStorage() {

        //for date
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
        saveCurrentDate = currentDate.format(calFordDate.getTime());
        //for time
        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime.format(calFordTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;
        final Uri compressedPostImageUri = Uri.parse(post_image.getTag().toString());

        final StorageReference filePath = postsImagesStorageRef.child("Post Images").child(compressedPostImageUri.getLastPathSegment() + postRandomName + ".jpg");



        filePath.putFile(compressedPostImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if(task.isSuccessful()){

                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downUrl = uri;
                            final String fileUrl = downUrl.toString();

                            allPostsRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.exists()){

                                        countPosts = dataSnapshot.getChildrenCount();
                                    }
                                    else {

                                        countPosts = 0;
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.exists()){
                                        //get the user details
                                        String userFullName = dataSnapshot.child("fullname").getValue().toString();
                                        String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                                        String post_title_lowercase = post_Title.toLowerCase();

                                        //save the posts details to database
                                        Map<String,Object> postsMap = new HashMap<String, Object>();
                                        postsMap.put("uid", currentUserID);
                                        postsMap.put("date", saveCurrentDate);
                                        postsMap.put("time", saveCurrentTime);
                                        postsMap.put("title", post_Title);
                                        postsMap.put("description", post_Description);
                                        postsMap.put("postimage", fileUrl);
                                        postsMap.put("profileimage", userProfileImage);
                                        postsMap.put("fullname", userFullName);
                                        postsMap.put("type", "image");
                                        postsMap.put("posttitletolowercase", post_title_lowercase);
                                        postsMap.put("timestamp", ServerValue.TIMESTAMP);
                                        postsMap.put("postfilestoragename", compressedPostImageUri.getLastPathSegment() + postRandomName + ".jpg");
                                        postsMap.put("counter", countPosts);

                                        allPostsRef.child(currentUserID + postRandomName).updateChildren(postsMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @SuppressLint("SetTextI18n")
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            post_image.setImageResource(R.drawable.easy_to_use);
                                                            post_title.setText("");
                                                            post_description.setText("");
                                                            loadingBar.dismiss();
                                                            finish();
                                                        }
                                                        else {
                                                            //{.....................
                                                            //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                                                            ViewGroup viewGroup = findViewById(android.R.id.content);

                                                            //then we will inflate the custom alert dialog xml that we created
                                                            View dialogView = LayoutInflater.from(AddNewPost.this).inflate(R.layout.error_dialog, viewGroup, false);


                                                            //Now we need an AlertDialog.Builder object
                                                            AlertDialog.Builder builder = new AlertDialog.Builder(AddNewPost.this);

                                                            //setting the view of the builder to our custom view that we already inflated
                                                            builder.setView(dialogView);

                                                            //finally creating the alert dialog and displaying it
                                                            final AlertDialog alertDialog = builder.create();

                                                            Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
                                                            TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
                                                            TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

                                                            dialog_btn.setText("OK");
                                                            success_title.setText("Error");
                                                            success_text.setText("An unexpected error occured while uploading your post.");

                                                            // if the OK button is clicked, close the success dialog
                                                            dialog_btn.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    alertDialog.dismiss();
                                                                }
                                                            });

                                                            alertDialog.show();
                                                            //...................}
                                                            loadingBar.dismiss();
                                                        }
                                                    }
                                                });

                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                    });

                }
                else{
                    String message = task.getException().getMessage();

                    //{.....................
                    //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                    ViewGroup viewGroup = findViewById(android.R.id.content);

                    //then we will inflate the custom alert dialog xml that we created
                    View dialogView = LayoutInflater.from(AddNewPost.this).inflate(R.layout.error_dialog, viewGroup, false);


                    //Now we need an AlertDialog.Builder object
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddNewPost.this);

                    //setting the view of the builder to our custom view that we already inflated
                    builder.setView(dialogView);

                    //finally creating the alert dialog and displaying it
                    final AlertDialog alertDialog = builder.create();

                    Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
                    TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
                    TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

                    dialog_btn.setText("OK");
                    success_title.setText("Error");
                    success_text.setText(message);

                    // if the OK button is clicked, close the success dialog
                    dialog_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.show();
                    //...................}
                    loadingBar.dismiss();
                }

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                loadingBar.setTitle("Uploading Post");
                loadingBar.setMessage(taskSnapshot.getBytesTransferred()/(1024*1024) + " / " + taskSnapshot.getTotalByteCount()/(1024*1024) + "MB");
                loadingBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                loadingBar.setProgress((int)progress);
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(false);
            }
        });
    }


    private void showAllFieldsAreRequired() {
        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(this).inflate(R.layout.error_dialog, viewGroup, false);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);

        //finally creating the alert dialog and displaying it
        final AlertDialog alertDialog = builder.create();

        Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
        TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
        TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

        dialog_btn.setText("OK");
        success_title.setText("Error");
        success_text.setText("All fields are required!");

        // if the OK button is clicked, close the success dialog
        dialog_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void selectPostImageFromGallery() {
        Intent gallery_intent = new Intent();
        gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
        gallery_intent.setType("image/*");
        startActivityForResult(Intent.createChooser(gallery_intent, "Select image"), gallery_Pic);
    }


    public void showErrorCustomDialog() {
        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(this).inflate(R.layout.error_dialog, viewGroup, false);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);

        //finally creating the alert dialog and displaying it
        final AlertDialog alertDialog = builder.create();

        Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
        TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
        TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

        dialog_btn.setText("OK");
        success_title.setText("Error");
        success_text.setText("Please select an image for this post!");

        // if the OK button is clicked, close the success dialog
        dialog_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendUserToHomeActivity() {
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
        finish();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendUserToHomeActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
        Utilities.getInstance(this).updateUserStatus("Online");
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.getInstance(this).updateUserStatus("Online");
    }

    @Override
    public void onPause() {
        super.onPause();
        Utilities.getInstance(this).updateUserStatus("Offline");
    }

    @Override
    public void onStop() {
        super.onStop();
        Utilities.getInstance(this).updateUserStatus("Offline");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utilities.getInstance(this).updateUserStatus("Offline");

        //clean up!
        mExecutorService.shutdown();

        mExecutorService = null;
        imageCompressTask = null;
    }

}