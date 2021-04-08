package niwigh.com.smartchat.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import niwigh.com.smartchat.Model.CommentsModel;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.TimeAgo;
import niwigh.com.smartchat.Util.Utilities;
import niwigh.com.smartchat.core.ImageCompressTask;
import niwigh.com.smartchat.listeners.IImageCompressTaskListener;



public class PostDetail extends AppCompatActivity {
    Toolbar toolbar;
    ImageView post_detail_image;
    FloatingActionButton post_detail_add_new_image;
    TextView post_title, post_description, edit_post_username,
            edit_post_a_date, edit_post_a_time, no_of_post_likes,no_of_post_comments;
    String PostKey, currentUserID, databaseUserID, title, postImage, description,post_author_image, edit_post_author_username,
            edit_post_date, edit_post_time;
    DatabaseReference clickPostsRef, LikesRef, UsersRef, postsRef;
    FirebaseAuth mAuth;
    FloatingActionMenu materialDesignFAM;
    com.github.clans.fab.FloatingActionButton floatingActionButton1,
            floatingActionButton2;
    CircleImageView edit_post_author_profile;

    RecyclerView comments_recycler_view;
    EmojiconEditText add_comment_editText;
    ImageButton btn_post_comment, like_a_post, download_a_post;
    ImageButton smiley_btn;
    EmojIconActions emojIconActions;
    View rootView;
    long countComments = 0;
    Boolean LikeChecker = false;
    int countLikes, commentsCount;
    final static int gallery_Pic = 1;
    StorageReference postsImagesStorageRef, UserProfileImageRef;
    ProgressDialog loadingBar;
    String saveCurrentDate, saveCurrentTime, postRandomName;
    CardView comments_cardview;
    TextView comments_text;

    private static final int REQUEST_STORAGE_PERMISSION = 100;

    //create a single thread pool to our image compression class.
    private ExecutorService mExecutorService = Executors.newFixedThreadPool(1);

    private ImageCompressTask imageCompressTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        toolbar = findViewById(R.id.add_new_post_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Post Details");

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UsersRef.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        PostKey = getIntent().getExtras().get("PostKey").toString();
        clickPostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts").child(PostKey);
        clickPostsRef.keepSynced(true);
        postsImagesStorageRef = FirebaseStorage.getInstance().getReference();
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        LikesRef.keepSynced(true);
        loadingBar = new ProgressDialog(this);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        postsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts");
        postsRef.keepSynced(true);




        materialDesignFAM = findViewById(R.id.material_design_android_floating_action_menu);
        floatingActionButton1 =  findViewById(R.id.material_design_floating_action_menu_item_edit_post);
        floatingActionButton2 = findViewById(R.id.material_design_floating_action_menu_item_delete_post);
        edit_post_author_profile = findViewById(R.id.user_post_detail_profile_image);
        edit_post_username = findViewById(R.id.edit_post_username);
        edit_post_a_date = findViewById(R.id.edit_post_date);
        edit_post_a_time = findViewById(R.id.edit_post_time);

        comments_text = findViewById(R.id.comments_text);
        comments_cardview = findViewById(R.id.comments_cardview);

        //comments
        comments_recycler_view = findViewById(R.id.comments_recyclerview);
        add_comment_editText = findViewById(R.id.add_comment_edit_text);
        btn_post_comment = findViewById(R.id.post_comment_btn);
        smiley_btn = findViewById(R.id.smiley_btn);
        LinearLayoutManager commentLayoutManager = new LinearLayoutManager(this);
        commentLayoutManager.setReverseLayout(true);
        commentLayoutManager.setStackFromEnd(true);
        comments_recycler_view.setLayoutManager(commentLayoutManager);
        loadComments();

        rootView = findViewById(R.id.rootView);
        like_a_post = findViewById(R.id.like_a_post);
        no_of_post_likes = findViewById(R.id.number_of_likes_textView);
        no_of_post_comments = findViewById(R.id.no_of_comments_textView);
        download_a_post = findViewById(R.id.download_post_image);

        emojIconActions = new EmojIconActions(this,rootView, add_comment_editText, smiley_btn, "#FF2CA96F","#e8e8e8","#f4f4f4");
        emojIconActions.ShowEmojIcon();
        // use this to change the default toggle icon
        emojIconActions.setIconsIds(R.drawable.ic_keyboard,R.drawable.ic_smiley);
        add_comment_editText.setEmojiconSize(50);
        emojIconActions.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {


            }

            @Override
            public void onKeyboardClose() {

            }
        });

        final DatabaseReference postImageDownload = FirebaseDatabase.getInstance().getReference().child("AllPosts");
        download_a_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                postImageDownload.child(PostKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String postImage = dataSnapshot.child("postimage").getValue().toString();

                            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                                    .format(System.currentTimeMillis());

                            File root = Environment.getExternalStorageDirectory();
                            root.mkdirs();
                            String path = root.toString();

                            downloadPostImage(PostDetail.this, timestamp
                                    , ".jpg", path + "/SmartChat" + "/Post Images" ,
                                    postImage);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

        DatabaseReference CommentsPostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts")
                .child(PostKey).child("Comments");
        CommentsPostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    countComments = dataSnapshot.getChildrenCount();
                }
                else {

                    countComments = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        btn_post_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){
                            String user_name = dataSnapshot.child("username").getValue().toString();
                            String user_profile_image = dataSnapshot.child("profileimage").getValue().toString();

                            validateCommentInput(user_name, user_profile_image);

                            add_comment_editText.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });




        floatingActionButton2.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                DeleteCurrentPost();
            } });

        setLikeButtonStatus(PostKey);
        setNoOfComments(PostKey);



        //init views
        post_detail_image = findViewById(R.id.post_detail_image);
        post_detail_add_new_image = findViewById(R.id.btn_edit_post_fab);
        post_title = findViewById(R.id.post_detail_title);
        post_description = findViewById(R.id.post_detail_description);

        post_detail_add_new_image.hide();
        materialDesignFAM.setVisibility(View.INVISIBLE);

        post_detail_add_new_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                requestPermission();
            }
        });


        clickPostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    title = dataSnapshot.child("title").getValue().toString();
                    description = dataSnapshot.child("description").getValue().toString();
                    postImage = dataSnapshot.child("postimage").getValue().toString();
                    databaseUserID = dataSnapshot.child("uid").getValue().toString();
                    post_author_image = dataSnapshot.child("profileimage").getValue().toString();
                    edit_post_author_username = dataSnapshot.child("fullname").getValue().toString();
                    edit_post_date = dataSnapshot.child("date").getValue().toString();
                    edit_post_time = dataSnapshot.child("time").getValue().toString();


                    final TimeAgo timeAgo = new TimeAgo();

                    String time = edit_post_date +" "+edit_post_time;
                    Date date = null;
                    try {
                        date = new SimpleDateFormat("yy-MM-dd hh:mm:ss").parse(time);
                        edit_post_a_date.setText(timeAgo.getTimeAgo(date));

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                    post_title.setText(title);
                    edit_post_a_time.setText("");
                    edit_post_username.setText(edit_post_author_username + "'s");
                    post_description.setText(description);
                    if(postImage == null || postImage.equals("")){
                        post_detail_image.setVisibility(View.GONE);
                    }else{
                        try{
                            Picasso.with(PostDetail.this).load(postImage).networkPolicy(NetworkPolicy.OFFLINE)
                                    .into(post_detail_image, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            Picasso.with(PostDetail.this).load(postImage).into(post_detail_image);
                                        }
                                    });
                            Picasso.with(PostDetail.this).load(post_author_image).networkPolicy(NetworkPolicy.OFFLINE)
                                    .into(edit_post_author_profile, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {

                                            Picasso.with(PostDetail.this).load(post_author_image).into(edit_post_author_profile);
                                        }
                                    });

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    if(currentUserID.equals(databaseUserID)){

                        post_detail_add_new_image.show();
                        materialDesignFAM.setVisibility(View.VISIBLE);
                    }

                    like_a_post.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            LikeChecker = true;

                            LikesRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if(LikeChecker.equals(true)){
                                        if(dataSnapshot.child(PostKey).hasChild(currentUserID)){
                                            LikesRef.child(PostKey).child(currentUserID).removeValue();
                                            LikeChecker = false;
                                        }
                                        else{

                                            LikesRef.child(PostKey).child(currentUserID).setValue(true);
                                            LikeChecker = false;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });



                    floatingActionButton1.setOnClickListener(new View.OnClickListener()
                    { public void onClick(View v)
                    {
                        EditCurrentPost(title, description, post_author_image, edit_post_author_username);
                    } });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
                //Yeah! I want both block to do the same thing, you can write your own logic, but this works for me.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            }
        }else {
            //Permission Granted, lets go pick photo
            selectPostImageFromGallery();
        }
    }



    private void selectPostImageFromGallery() {
        Intent gallery_intent = new Intent();
        gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
        gallery_intent.setType("image/*");
        startActivityForResult(gallery_intent, gallery_Pic);
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

            post_detail_image.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));


            //for date
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currentDate.format(calFordDate.getTime());
            //for time
            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            saveCurrentTime = currentTime.format(calFordTime.getTime());

            postRandomName = saveCurrentDate + saveCurrentTime;


            final StorageReference filePath = postsImagesStorageRef.child("Post Images").child(postRandomName + ".jpg");

            filePath.putFile(compressedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Uri downUrl = uri;
                                final String fileUrl = downUrl.toString();

                                postsRef.child(PostKey).addValueEventListener(new ValueEventListener() {
                                    @SuppressLint("SetTextI18n")
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (task.isSuccessful()){
                                            postsRef.child(PostKey).child("postimage").setValue(fileUrl);
                                            post_detail_image.setImageBitmap(BitmapFactory.decodeFile(fileUrl));
                                            postsRef.child(PostKey).child("postfilestoragename").setValue(postRandomName + ".jpg");
                                            loadingBar.dismiss();
                                            Toasty.success(PostDetail.this,"Post image updated successfully!", Toasty.LENGTH_LONG, true).show();

                                        }else {

                                            //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                                            ViewGroup viewGroup = findViewById(android.R.id.content);

                                            //then we will inflate the custom alert dialog xml that we created
                                            View dialogView = LayoutInflater.from(PostDetail.this).inflate(R.layout.error_dialog, viewGroup, false);


                                            //Now we need an AlertDialog.Builder object
                                            AlertDialog.Builder builder = new AlertDialog.Builder(PostDetail.this);

                                            //setting the view of the builder to our custom view that we already inflated
                                            builder.setView(dialogView);

                                            //finally creating the alert dialog and displaying it
                                            final AlertDialog alertDialog = builder.create();

                                            Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
                                            TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
                                            TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

                                            dialog_btn.setText("OK");
                                            success_title.setText("Error");
                                            success_text.setText("Post image could not be updated!");

                                            // if the OK button is clicked, close the success dialog
                                            dialog_btn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    alertDialog.dismiss();
                                                }
                                            });

                                            try{
                                                alertDialog.show();
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }
                                            loadingBar.dismiss();
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

                        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                        ViewGroup viewGroup = findViewById(android.R.id.content);

                        //then we will inflate the custom alert dialog xml that we created
                        View dialogView = LayoutInflater.from(PostDetail.this).inflate(R.layout.error_dialog, viewGroup, false);


                        //Now we need an AlertDialog.Builder object
                        AlertDialog.Builder builder = new AlertDialog.Builder(PostDetail.this);

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

                        try{
                            alertDialog.show();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
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

        @Override
        public void onError(Throwable error) {
            //very unlikely, but it might happen on a device with extremely low storage.
            //log it, log.WhatTheFuck?, or show a dialog asking the user to delete some files....etc, etc
            Log.wtf("ImageCompressor", "Error occurred", error);
        }
    };

    public void setNoOfComments(final String PostKey) {

        DatabaseReference CommentsPostsRef;
        CommentsPostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts").child(PostKey).child("Comments");

        CommentsPostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                dataSnapshot.getKey();
                commentsCount  = (int)dataSnapshot.getChildrenCount();
                no_of_post_comments.setText(commentsCount + " Comments");

                if(commentsCount == 0){
                    comments_text.setVisibility(View.GONE);
                    comments_cardview.setVisibility(View.GONE);
                }else{
                    comments_text.setVisibility(View.VISIBLE);
                    comments_cardview.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {


            }
        });

    }

    public void setLikeButtonStatus(final String PostKey){
        LikesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child(PostKey).hasChild(currentUserID)){
                    countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                    like_a_post.setImageResource(R.drawable.ic_liked);
                    no_of_post_likes.setText((Integer.toString(countLikes)) + (" Likes"));
                }
                else {
                    countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                    like_a_post.setImageResource(R.drawable.ic_like);
                    no_of_post_likes.setText((Integer.toString(countLikes)) + (" Likes"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Utilities.getInstance(this).updateUserStatus("Online");
        loadComments();
    }

    private void loadComments() {
        final DatabaseReference CommentsPostsRef;
        CommentsPostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts").child(PostKey).child("Comments");
        Query orderPostCommentsInDescendingOrder = CommentsPostsRef.orderByChild("countComments");
        FirebaseRecyclerAdapter<CommentsModel, CommentsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<CommentsModel, CommentsViewHolder>
                        (
                                CommentsModel.class,
                                R.layout.comments_layout,
                                CommentsViewHolder.class,
                                orderPostCommentsInDescendingOrder

                        ) {
                    @Override
                    protected void populateViewHolder(CommentsViewHolder viewHolder, CommentsModel model, int position) {

                        viewHolder.setComment(model.getComment());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setTime(model.getDate(),model.getTime());
                        viewHolder.setUsername(model.getUsername());
                        viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());
                    }
                };

        comments_recycler_view.setAdapter(firebaseRecyclerAdapter);
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setComment(String comment) {
            TextView myComment = mView.findViewById(R.id.user_comment_text);
            myComment.setText(comment);

        }

        public void setDate(String date_published) {
            TextView myDate = mView.findViewById(R.id.user_comment_date);
            myDate.setText("");

        }

        public void setTime(String date_published, String time_published) {
            TextView myTime = mView.findViewById(R.id.user_comment_time);
            final TimeAgo timeAgo = new TimeAgo();

            String time = date_published +" "+time_published;
            Date date = null;
            try {
                date = new SimpleDateFormat("yy-MM-dd hh:mm:ss").parse(time);
                myTime.setText(timeAgo.getTimeAgo(date));

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

        public void setUsername(String username) {
            TextView myUsername = mView.findViewById(R.id.user_comment_profile_name);
            myUsername.setText("@" + username);

        }

        public void setProfileimage(final Context ctx, final String profileimage) {
            final CircleImageView myProfileImage = mView.findViewById(R.id.user_comment_profile_image);
            try{
                Picasso.with(ctx).load(profileimage).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.easy_to_use).into(myProfileImage, new
                        Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {

                                Picasso.with(ctx).load(profileimage).placeholder(R.drawable.easy_to_use).into(myProfileImage);

                            }
                        });
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private void validateCommentInput(String user_name, String user_profile_image) {
        String commentText = add_comment_editText.getText().toString().trim();
        if(commentText.isEmpty()){

            //before inflating the custom alert dialog layout, we will get the current activity viewgroup
            ViewGroup viewGroup = findViewById(android.R.id.content);

            //then we will inflate the custom alert dialog xml that we created
            View dialogView = LayoutInflater.from(PostDetail.this).inflate(R.layout.error_dialog, viewGroup, false);


            //Now we need an AlertDialog.Builder object
            AlertDialog.Builder builder = new AlertDialog.Builder(PostDetail.this);

            //setting the view of the builder to our custom view that we already inflated
            builder.setView(dialogView);

            //finally creating the alert dialog and displaying it
            final AlertDialog alertDialog = builder.create();

            Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
            TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
            TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

            dialog_btn.setText("OK");
            success_title.setText("Error");
            success_text.setText("No comment(s) in the input field provided!");

            // if the OK button is clicked, close the success dialog
            dialog_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });

            try{
                alertDialog.show();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else {
            @SuppressLint("SimpleDateFormat")
            //for date
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
            final String saveCurrentDate = currentDate.format(calFordDate.getTime());
            //for time
            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
            final  String saveCurrentTime = currentTime.format(calFordTime.getTime());

            final String RandomKey = currentUserID + saveCurrentDate + saveCurrentTime;

            Map<String,Object> commentsMap = new HashMap<String, Object>();
            commentsMap.put("uid", currentUserID);
            commentsMap.put("comment", commentText);
            commentsMap.put("date", saveCurrentDate);
            commentsMap.put("time", saveCurrentTime);
            commentsMap.put("username", user_name);
            commentsMap.put("profileimage", user_profile_image);
            commentsMap.put("countComments", countComments);



            final DatabaseReference CommentsPostsRef;
            CommentsPostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts").child(PostKey).child("Comments");
            CommentsPostsRef.child(RandomKey).updateChildren(commentsMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toasty.success(PostDetail.this,"Comment added successfully.", Toasty.LENGTH_LONG, true).show();
                                comments_text.setVisibility(View.VISIBLE);
                                comments_cardview.setVisibility(View.VISIBLE);
                            }
                            else {
                                //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                                ViewGroup viewGroup = findViewById(android.R.id.content);

                                //then we will inflate the custom alert dialog xml that we created
                                View dialogView = LayoutInflater.from(PostDetail.this).inflate(R.layout.error_dialog, viewGroup, false);


                                //Now we need an AlertDialog.Builder object
                                AlertDialog.Builder builder = new AlertDialog.Builder(PostDetail.this);

                                //setting the view of the builder to our custom view that we already inflated
                                builder.setView(dialogView);

                                //finally creating the alert dialog and displaying it
                                final AlertDialog alertDialog = builder.create();

                                Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
                                TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
                                TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

                                dialog_btn.setText("OK");
                                success_title.setText("Error");
                                success_text.setText("Your comment was not added. Try again.");

                                // if the OK button is clicked, close the success dialog
                                dialog_btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        alertDialog.dismiss();
                                    }
                                });

                                try{
                                    alertDialog.show();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    });


        }
    }

    public void EditCurrentPost(String title, String description, final String post_author_image, String edit_post_author_username) {

        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(PostDetail.this).inflate(R.layout.edit_post_dialog, viewGroup, false);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(PostDetail.this);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);

        //finally creating the alert dialog and displaying it
        final AlertDialog alertDialog = builder.create();


        final CircleImageView imageView = dialogView.findViewById(R.id.user_post_edit_profile_image);
        Button cancel_btn =  dialogView.findViewById(R.id.cancel_btn);
        Button publish_btn = dialogView.findViewById(R.id.publish_btn);
        final EditText post_title = dialogView.findViewById(R.id.text_input_edit_post_title);
        final EditText post_description = dialogView.findViewById(R.id.text_input_edit_post_description);

        TextView edit_post_username =  dialogView.findViewById(R.id.edit_post_username);

        post_title.setText(title);
        post_description.setText(description);
        edit_post_username.setText(edit_post_author_username);
        try{
            Picasso.with(PostDetail.this).load(post_author_image).networkPolicy(NetworkPolicy.OFFLINE)
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            Picasso.with(PostDetail.this).load(post_author_image).into(imageView);
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }

        publish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clickPostsRef.child("title").setValue(post_title.getText().toString());
                clickPostsRef.child("description").setValue(post_description.getText().toString());

                Toasty.success(PostDetail.this,"Post updated successfully!", Toasty.LENGTH_LONG, true).show();
                alertDialog.dismiss();
            }
        });

        // if the cancel button is clicked, close the success dialog
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        try{
            alertDialog.show();
        }catch (Exception e){
            e.printStackTrace();
        }




    }


    private void DeleteCurrentPost() {
        clickPostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //also we delete the image associated with this post from the storage
                //so we first retrieve the name of the image from the database
                if(dataSnapshot.exists()){
                    String postimagenameindatabase = dataSnapshot.child("postfilestoragename").getValue().toString();

                    try {

                        //now, delete the image from the storage
                        StorageReference postimageRef = FirebaseStorage.getInstance().getReference()
                                .child("Post Images").child(postimagenameindatabase);
                        postimageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // File deleted successfully
                                Log.d("SUCCESS:", "onSuccess: deleted file");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Uh-oh, an error occurred!
                                Log.d("ERROR:", "onFailure: did not delete file");
                            }
                        });
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    //delete associated likes
                    LikesRef.child(PostKey).child(currentUserID).removeValue();

                    //remove post
                    clickPostsRef.removeValue();
                    post_deleted_success_toast();
                    overridePendingTransition(R.anim.left_in, R.anim.right_out);
                    finish();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void post_deleted_success_toast() {
        Toasty.success(this,"Post deleted Successfully!", Toasty.LENGTH_LONG, true).show();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.getInstance(this).updateUserStatus("Online");
        loadComments();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
        finish();
    }

    public void downloadPostImage(Context context, String FileName, String FileExtension, String
            DestinationDirectory, String uri){
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri1 = Uri.parse(uri);
        DownloadManager.Request request = new DownloadManager.Request(uri1);
        request.setTitle("SmartChat (" + FileName + FileExtension +")");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, DestinationDirectory, FileName + FileExtension);
        downloadManager.enqueue(request);
    }
}