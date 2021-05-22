package niwigh.com.smartchat.Activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;


public class AddNewVideoPost extends AppCompatActivity {
    Toolbar toolbar;
    FloatingActionButton btn_add_post_video;
    private static final int PICK_VIDEO_REQUEST = 3;
    private Uri videouri;
    private MediaController mediaController;
    private VideoView videoView;
    EditText post_title;
    EditText post_description;
    Button publish_post;
    ProgressDialog loadingBar;
    String saveCurrentDate, saveCurrentTime, postRandomName, currentUserID;
    long countPosts = 0;
    StorageReference postsVideoStorageRef;
    String post_Title, post_Description;
    DatabaseReference usersRef, allPostsRef;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_video_post);

        postsVideoStorageRef = FirebaseStorage.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);
        allPostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts");
        allPostsRef.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        toolbar = findViewById(R.id.add_new_post_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add New Video Post");


        videoView = findViewById(R.id.post_video_view);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                        try {
                            mediaController = new MediaController(AddNewVideoPost.this);
                            videoView.setMediaController(mediaController);
                            mediaController.setAnchorView(videoView);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        videoView.start();

        post_title = findViewById(R.id.post_title_edit_text);
        post_description = findViewById(R.id.post_description_edit_text);
        publish_post = findViewById(R.id.btn_publish_post);
        loadingBar = new ProgressDialog(this);


        btn_add_post_video = findViewById(R.id.btn_add_post_video);
        btn_add_post_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectVideoFromGallery();
            }
        });

        publish_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatePostInfo();
            }
        });
    }

    private void selectVideoFromGallery() {
        Intent select_video_intent = new Intent();
        select_video_intent.setType("video/*");
        select_video_intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(select_video_intent, "Select video"), PICK_VIDEO_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null ){
            try {
                videouri = data.getData();
                videoView.setVideoURI(videouri);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void validatePostInfo() {
        try {
            post_Title = post_title.getText().toString();
            post_Description = post_description.getText().toString();

            if (videouri == null) {
                showErrorCustomDialog();
            } else if (post_Title.isEmpty() || post_Description.isEmpty()) {
                //all fields are required dialog
                showAllFieldsAreRequired();
            } else {
                storeVideoToFirebaseStorage();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @SuppressLint("SimpleDateFormat")
    private void storeVideoToFirebaseStorage() {

        try {
            //for date
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
            saveCurrentDate = currentDate.format(calFordDate.getTime());
            //for time
            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
            saveCurrentTime = currentTime.format(calFordTime.getTime());

            postRandomName = saveCurrentDate + saveCurrentTime;

            final StorageReference filePath = postsVideoStorageRef.child("Post Videos").child(videouri.getLastPathSegment() + postRandomName + ".mp4");


            filePath.putFile(videouri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()) {

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String fileUrl = uri.toString();

                                allPostsRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists()) {

                                            countPosts = dataSnapshot.getChildrenCount();
                                        } else {

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

                                        if (dataSnapshot.exists()) {
                                            try {
                                                //get the user details
                                                String userFullName = dataSnapshot.child("fullname").getValue().toString();
                                                String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                                                //save the posts details to database
                                                Map<String, Object> postsMap = new HashMap<String, Object>();
                                                postsMap.put("uid", currentUserID);
                                                postsMap.put("date", saveCurrentDate);
                                                postsMap.put("time", saveCurrentTime);
                                                postsMap.put("title", post_Title);
                                                postsMap.put("description", post_Description);
                                                postsMap.put("postvideo", fileUrl);
                                                postsMap.put("profileimage", userProfileImage);
                                                postsMap.put("fullname", userFullName);
                                                postsMap.put("type", "video");
                                                postsMap.put("posttitletolowercase", post_Title.toLowerCase());
                                                postsMap.put("timestamp", ServerValue.TIMESTAMP);
                                                postsMap.put("postfilestoragename", videouri.getLastPathSegment() + postRandomName + ".mp4");
                                                postsMap.put("counter", countPosts);

                                                allPostsRef.child(currentUserID + postRandomName).updateChildren(postsMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @SuppressLint("SetTextI18n")
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    videoView.setVideoURI(videouri);
                                                                    post_title.setText("");
                                                                    post_description.setText("");
                                                                    loadingBar.dismiss();
                                                                    finish();
                                                                } else {
                                                                    //{.....................
                                                                    //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                                                                    ViewGroup viewGroup = findViewById(android.R.id.content);

                                                                    //then we will inflate the custom alert dialog xml that we created
                                                                    View dialogView = LayoutInflater.from(AddNewVideoPost.this).inflate(R.layout.error_dialog, viewGroup, false);


                                                                    //Now we need an AlertDialog.Builder object
                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(AddNewVideoPost.this);

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

                                            }catch(Exception e){
                                                e.printStackTrace();
                                            }

                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                        });

                    } else {
                        String message = task.getException().getMessage();

                        //{.....................
                        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                        ViewGroup viewGroup = findViewById(android.R.id.content);

                        //then we will inflate the custom alert dialog xml that we created
                        View dialogView = LayoutInflater.from(AddNewVideoPost.this).inflate(R.layout.error_dialog, viewGroup, false);


                        //Now we need an AlertDialog.Builder object
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddNewVideoPost.this);

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
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    loadingBar.setTitle("Uploading Post");
                    loadingBar.setMessage(taskSnapshot.getBytesTransferred() / (1024 * 1024) + " / " + taskSnapshot.getTotalByteCount() / (1024 * 1024) + "MB");
                    loadingBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    loadingBar.setProgress((int) progress);
                    loadingBar.show();
                    loadingBar.setCanceledOnTouchOutside(false);
                }
            });
            ;
        }catch(Exception e){
            e.printStackTrace();
        }
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
        success_text.setText("Please select a video for this post!");

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
    }

}