package niwigh.com.smartchat.Activity;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

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
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import niwigh.com.smartchat.Model.CommentsModel;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.TimeAgo;
import niwigh.com.smartchat.Util.Utilities;


public class PostVideoDetails extends AppCompatActivity {
    Toolbar toolbar;
    TextView post_title, post_description, edit_post_username, edit_post_a_date,
            edit_post_a_time, no_of_post_comments,no_of_post_likes;
    String PostKey, currentUserID;
    DatabaseReference videopostsRef,LikesRef, UsersRef;
    FirebaseAuth mAuth;
    FloatingActionMenu materialDesignFAM;
    com.github.clans.fab.FloatingActionButton floatingActionButton1,
            floatingActionButton2;
    CircleImageView edit_post_author_profile;

    RecyclerView comments_recycler_view;
    EmojiconEditText add_comment_editText;
    ImageButton btn_post_comment;
    ImageButton smiley_btn, download_a_post,like_a_post_button;
    EmojIconActions emojIconActions;
    View rootView;
    int current = 0;
    int duration = 0;
    boolean isPlaying = false;
    long countComments = 0;
    int countLikes, commentsCount;
    Boolean LikeChecker = false;
    CardView comments_cardview;
    TextView comments_text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_video_details);
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
        videopostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts").child(PostKey);
        videopostsRef.keepSynced(true);
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        LikesRef.keepSynced(true);




        //init views
        post_title = findViewById(R.id.post_detail_title);
        post_description = findViewById(R.id.post_detail_description);
        materialDesignFAM = findViewById(R.id.material_design_android_floating_action_menu);
        floatingActionButton1 =  findViewById(R.id.material_design_floating_action_menu_item_edit_post);
        floatingActionButton2 = findViewById(R.id.material_design_floating_action_menu_item_delete_post);
        edit_post_author_profile = findViewById(R.id.user_post_detail_profile_image);
        edit_post_username = findViewById(R.id.edit_post_username);
        edit_post_a_date = findViewById(R.id.edit_post_date);
        edit_post_a_time = findViewById(R.id.edit_post_time);
        materialDesignFAM.setVisibility(View.INVISIBLE);
        download_a_post = findViewById(R.id.download_post_image);
        no_of_post_comments = findViewById(R.id.no_of_comments_textView);
        like_a_post_button = findViewById(R.id.like_a_post);
        no_of_post_likes = findViewById(R.id.number_of_likes_textView);

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


        setLikeButtonStatus(PostKey);
        setNoOfComments(PostKey);

        download_a_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                videopostsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String postImage = dataSnapshot.child("postvideo").getValue().toString();

                            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                                    .format(System.currentTimeMillis());

                            File root = Environment.getExternalStorageDirectory();
                            root.mkdirs();
                            String path = root.toString();

                            downloadPostImage(PostVideoDetails.this, timestamp
                                    , ".mp4", path + "/SmartChat" + "/Post Videos" ,
                                    postImage);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
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






        videopostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    final String title, description, postVideo, databaseUserID, post_author_image, edit_post_author_username,
                            edit_post_date, edit_post_time;




                    title = dataSnapshot.child("title").getValue().toString();
                    description = dataSnapshot.child("description").getValue().toString();
                    postVideo = dataSnapshot.child("postvideo").getValue().toString();
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
                    edit_post_username.setText(edit_post_author_username + "'s");
                    edit_post_a_time.setText("");
                    post_description.setText(description);

                    final ImageButton play_btn = findViewById(R.id.play_btn);
                    final TextView counter_timer = findViewById(R.id.counter_timer);
                    final ProgressBar video_progress = findViewById(R.id.video_progress);
                    video_progress.setMax(100);
                    final TextView duration_timer = findViewById(R.id.duration_timer);


                    final ProgressBar loading_video_progress_bar = findViewById(R.id.loading_video_progress_bar);
                    final VideoView videoView = findViewById(R.id.post_video);

                    videoView.setVideoURI(Uri.parse(postVideo));
                    videoView.requestFocus();
                    videoView.setBackgroundColor(getResources().getColor(R.color.default_video_post_bg));

                    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {


                        class VideoProgress extends AsyncTask<Void, Integer, Void> {

                            @Override
                            protected Void doInBackground(Void... voids) {
                                do {

                                    if(isPlaying){
                                        current = videoView.getCurrentPosition()/1000;
                                        publishProgress(current);
                                    }


                                }while (video_progress.getProgress() <= 100);

                                return null;
                            }

                            @Override
                            protected void onProgressUpdate(Integer... values) {
                                super.onProgressUpdate(values);

                                try{
                                    int currentPercent = values[0] * 100/duration;
                                    video_progress.setProgress(currentPercent);
                                    String currentString = String.format("%02d:%02d", values[0]/60, values[0]%60);
                                    counter_timer.setText(currentString);


                                }catch (Exception e){
                                    e.printStackTrace();
                                    Log.v("Error", e.getMessage());

                                }

                            }
                        }


                        @Override
                        public void onPrepared(MediaPlayer mp) {

                            duration = mp.getDuration()/1000;
                            String durationString = String.format("%02d:%02d", duration /60, duration %60);
                            duration_timer.setText(durationString);

                            mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                                @Override
                                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                                    if(what == mp.MEDIA_INFO_BUFFERING_END){
                                        loading_video_progress_bar.setVisibility(View.INVISIBLE);
                                        return true;
                                    }
                                    else if(what == mp.MEDIA_INFO_BUFFERING_START){
                                        loading_video_progress_bar.setVisibility(View.VISIBLE);
                                    }
                                    return false;
                                }
                            });

                            videoView.start();
                            isPlaying = true;
                            videoView.setBackgroundColor(0);
                            loading_video_progress_bar.setVisibility(View.INVISIBLE);
                            play_btn.setImageResource(R.drawable.ic_pause);
                            new VideoProgress().execute();



                            play_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(isPlaying){

                                        videoView.pause();
                                        isPlaying = false;
                                        play_btn.setImageResource(R.drawable.ic_play);
                                    }
                                    else {

                                        videoView.start();
                                        isPlaying = true;
                                        play_btn.setImageResource(R.drawable.ic_pause);
                                    }
                                }
                            });


                        }
                    });

                    try{
                        Picasso.with(PostVideoDetails.this).load(post_author_image).networkPolicy(NetworkPolicy.OFFLINE)
                                .into(edit_post_author_profile, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {

                                        Picasso.with(PostVideoDetails.this).load(post_author_image).into(edit_post_author_profile);
                                    }
                                });
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    if(currentUserID.equals(databaseUserID)){
                        materialDesignFAM.setVisibility(View.VISIBLE);
                    }

                    like_a_post_button.setOnClickListener(new View.OnClickListener() {
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

    public void setLikeButtonStatus(final String PostKey){
        LikesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child(PostKey).hasChild(currentUserID)){
                    countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                    like_a_post_button.setImageResource(R.drawable.ic_liked);
                    no_of_post_likes.setText((Integer.toString(countLikes)) + (" Likes"));
                }
                else {
                    countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                    like_a_post_button.setImageResource(R.drawable.ic_like);
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
        implementCommentsInDescendingOrder();
        loadComments();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Utilities.getInstance(this).updateUserStatus("Offline");
        isPlaying = false;
    }

    private void implementCommentsInDescendingOrder() {
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
    }

    private void loadComments() {

        final DatabaseReference CommentsPostsRef;
        CommentsPostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts").child(PostKey).child("Comments");
        Query PostCommentsCountOrdering = CommentsPostsRef.orderByChild("commentsCount");
        FirebaseRecyclerAdapter<CommentsModel, PostVideoDetails.CommentsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<CommentsModel, PostVideoDetails.CommentsViewHolder>
                        (
                                CommentsModel.class,
                                R.layout.comments_layout,
                                PostVideoDetails.CommentsViewHolder.class,
                                PostCommentsCountOrdering

                        ) {
                    @Override
                    protected void populateViewHolder(PostVideoDetails.CommentsViewHolder viewHolder, CommentsModel model, int position) {

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

        public void setDate(String date) {
            TextView myDate = mView.findViewById(R.id.user_comment_date);
            myDate.setText("");

        }

        public void setTime(String edit_post_date, String edit_post_time) {
            TextView myTime = mView.findViewById(R.id.user_comment_time);

            final TimeAgo timeAgo = new TimeAgo();

            String time = edit_post_date +" "+edit_post_time;
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

    @SuppressLint("SetTextI18n")
    private void validateCommentInput(String user_name, String user_profile_image) {
        String commentText = add_comment_editText.getText().toString().trim();
        if(commentText.isEmpty()){

            //before inflating the custom alert dialog layout, we will get the current activity viewgroup
            ViewGroup viewGroup = findViewById(android.R.id.content);

            //then we will inflate the custom alert dialog xml that we created
            View dialogView = LayoutInflater.from(PostVideoDetails.this).inflate(R.layout.error_dialog, viewGroup, false);


            //Now we need an AlertDialog.Builder object
            AlertDialog.Builder builder = new AlertDialog.Builder(PostVideoDetails.this);

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

            //for date
            Calendar calFordDate = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd");
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
            commentsMap.put("commentsCount", countComments);



            final DatabaseReference CommentsPostsRef;
            CommentsPostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts").child(PostKey).child("Comments");
            CommentsPostsRef.child(RandomKey).updateChildren(commentsMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                Toasty.success(PostVideoDetails.this,"Comment added successfully.", Toasty.LENGTH_LONG, true).show();
                                comments_text.setVisibility(View.VISIBLE);
                                comments_cardview.setVisibility(View.VISIBLE);
                            }
                            else {
                                //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                                ViewGroup viewGroup = findViewById(android.R.id.content);

                                //then we will inflate the custom alert dialog xml that we created
                                View dialogView = LayoutInflater.from(PostVideoDetails.this).inflate(R.layout.error_dialog, viewGroup, false);


                                //Now we need an AlertDialog.Builder object
                                AlertDialog.Builder builder = new AlertDialog.Builder(PostVideoDetails.this);

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
        View dialogView = LayoutInflater.from(PostVideoDetails.this).inflate(R.layout.edit_post_dialog, viewGroup, false);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(PostVideoDetails.this);

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
            Picasso.with(PostVideoDetails.this).load(post_author_image).networkPolicy(NetworkPolicy.OFFLINE)
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            Picasso.with(PostVideoDetails.this).load(post_author_image).into(imageView);
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }

        publish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videopostsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        videopostsRef.child("title").setValue(post_title.getText().toString());
                        videopostsRef.child("description").setValue(post_description.getText().toString());
                        alertDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                Toasty.success(PostVideoDetails.this,"Post updated successfully!", Toasty.LENGTH_LONG, true).show();

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
        videopostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //also we delete the video associated with this post from the storage
                //so we first retrieve the name of the video from the database
                if(dataSnapshot.exists()){
                    String videopoststoragename = dataSnapshot
                            .child("postfilestoragename").getValue().toString();


                    //now, delete the video from the storage
                    StorageReference postvideoRef = FirebaseStorage.getInstance().getReference()
                            .child("Post Videos").child(videopoststoragename);
                    postvideoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
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

                    //delete associated likes
                    LikesRef.child(PostKey).child(currentUserID).removeValue();

                    videopostsRef.removeValue();
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
    }

    @Override
    public void onPause() {
        super.onPause();
        Utilities.getInstance(this).updateUserStatus("Offline");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utilities.getInstance(this).updateUserStatus("Offline");
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