package niwigh.com.smartchat.Adapter;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import niwigh.com.smartchat.Activity.PostDetail;
import niwigh.com.smartchat.Activity.PostVideoDetails;
import niwigh.com.smartchat.Model.PostsModel;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.TimeAgo;

public class AllPostsAdapter extends RecyclerView.Adapter<AllPostsAdapter.AdapterViewHolder> {

    private Context mContext;
    private List<PostsModel> postsModelList;
    private ProgressDialog dialog;
    FirebaseAuth mAuth;
    Boolean LikeChecker = false;
    DatabaseReference likesRef, allPostsRef;
    private int AD_TYPE = 4, CONTENT_TYPE = 13;
    private static final int LIST_AD_DELTA = 3;

    public AllPostsAdapter(Context mContext, List<PostsModel> postsModelList) {
        this.mContext = mContext;
        this.postsModelList = postsModelList;
    }

    @NonNull
    @Override
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View view = null;

        if (viewType == AD_TYPE) {

            //show admob ads
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.admob_banner_ad, null);
            return new AdapterViewHolder(view);

        } else {

            //show normal content
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.all_posts_layout, null);
            return new AdapterViewHolder(view);
        }

    }


    @Override
    public void onBindViewHolder(@NonNull final AdapterViewHolder holder, final int position) {

        if (getItemViewType(position) == CONTENT_TYPE) {
            try {

                dialog = new ProgressDialog(mContext);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.setMessage("Processing...");

                mAuth = FirebaseAuth.getInstance();
                final Activity activity = (Activity) mContext;
                allPostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts");
                allPostsRef.keepSynced(true);
                likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
                likesRef.keepSynced(true);

                if (postsModelList.get(position).getType().equals("image")) {

                    if (postsModelList.get(position).getPostimage() == null || postsModelList.get(position).getPostimage().equals("")) {
                        holder.image_post_image.setVisibility(View.GONE);
                    }

                    //render image post
                    holder.all_image_posts_cardview.setVisibility(View.VISIBLE);
                    holder.all_video_posts_cardview.setVisibility(View.GONE);

                    holder.setFullname(postsModelList.get(position).getFullname());
                    holder.setDate(postsModelList.get(position).getDate(), postsModelList.get(position).getTime());
                    holder.setDescription(postsModelList.get(position).getDescription());
                    holder.setTime(postsModelList.get(position).getTime());
                    holder.setTitle(postsModelList.get(position).getTitle());
                    holder.setPostimage(activity, postsModelList.get(position).getPostimage());
                    holder.setProfileimage(activity, postsModelList.get(position).getProfileimage());

                    holder.setLikeButtonStatus(postsModelList.get(position).getPostKey());
                    holder.setNoOfComments(postsModelList.get(position).getPostKey());

                    holder.all_image_posts_cardview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent postClickintent = new Intent(mContext, PostDetail.class);
                            postClickintent.putExtra("PostKey", postsModelList.get(position).getPostKey());
                            mContext.startActivity(postClickintent);
                            activity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        }
                    });


                    final DatabaseReference postImageDownload = FirebaseDatabase.getInstance().getReference().child("Posts");
                    holder.image_download_post_image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            postImageDownload.child(postsModelList.get(position).getPostKey())
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                String postImage = dataSnapshot.child("postimage").getValue().toString();

                                                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                                                        .format(System.currentTimeMillis());

                                                File root = Environment.getExternalStorageDirectory();
                                                root.mkdirs();
                                                String path = root.toString();

                                                downloadPostImage(activity, timestamp
                                                        , ".jpg", path + "/SmartChat" + "/Post Images",
                                                        postImage);
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                        }
                    });

                    holder.image_comment_on_post.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent postcomment = new Intent(activity, PostDetail.class);
                            postcomment.putExtra("PostKey", postsModelList.get(position).getPostKey());
                            mContext.startActivity(postcomment);
                            activity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        }
                    });

                    holder.image_like_a_post.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            LikeChecker = true;

                            likesRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (LikeChecker.equals(true)) {
                                        if (dataSnapshot.child(postsModelList.get(position).getPostKey())
                                                .hasChild(mAuth.getCurrentUser().getUid())) {
                                            likesRef.child(postsModelList.get(position).getPostKey())
                                                    .child(mAuth.getCurrentUser().getUid()).removeValue();
                                            LikeChecker = false;
                                        } else {

                                            likesRef.child(postsModelList.get(position).getPostKey())
                                                    .child(mAuth.getCurrentUser().getUid()).setValue(true);
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

                } else {

                    //render_video_post
                    holder.all_image_posts_cardview.setVisibility(View.GONE);
                    holder.all_video_posts_cardview.setVisibility(View.VISIBLE);

                    holder.setVideoFullname(postsModelList.get(position).getFullname());
                    holder.setVideoDate(postsModelList.get(position).getDate(), postsModelList.get(position).getTime());
                    holder.setVideoDescription(postsModelList.get(position).getDescription());
                    holder.setVideoTime(postsModelList.get(position).getTime());
                    holder.setVideoTitle(postsModelList.get(position).getTitle());
                    holder.setPostvideo(activity, postsModelList.get(position).getPostvideo());
                    holder.setVideoProfileimage(activity, postsModelList.get(position).getProfileimage());

                    holder.setVideoLikeButtonStatus(postsModelList.get(position).getPostKey());
                    holder.setVideoNoOfComments(postsModelList.get(position).getPostKey());

                    holder.all_video_posts_cardview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent postClickintent = new Intent(activity, PostVideoDetails.class);
                            postClickintent.putExtra("PostKey", postsModelList.get(position).getPostKey());
                            mContext.startActivity(postClickintent);
                            activity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        }
                    });


                    holder.video_download_post_image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            allPostsRef.child(postsModelList.get(position).getPostKey())
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                String postImage = dataSnapshot.child("postvideo").getValue().toString();

                                                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                                                        .format(System.currentTimeMillis());

                                                File root = Environment.getExternalStorageDirectory();
                                                root.mkdirs();
                                                String path = root.toString();

                                                downloadPostImage(activity, timestamp
                                                        , ".mp4", path + "/SmartChat" + "/Post Videos",
                                                        postImage);
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                        }
                    });

                    holder.video_comment_on_post.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent postcomment = new Intent(activity, PostVideoDetails.class);
                            postcomment.putExtra("PostKey", postsModelList.get(position).getPostKey());
                            mContext.startActivity(postcomment);
                            activity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        }
                    });

                    holder.video_like_a_post.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            LikeChecker = true;

                            likesRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (LikeChecker.equals(true)) {
                                        if (dataSnapshot.child(postsModelList.get(position).getPostKey())
                                                .hasChild(mAuth.getCurrentUser().getUid())) {
                                            likesRef.child(postsModelList.get(position).getPostKey())
                                                    .child(mAuth.getCurrentUser().getUid()).removeValue();
                                            LikeChecker = false;
                                        } else {

                                            likesRef.child(postsModelList.get(position).getPostKey())
                                                    .child(mAuth.getCurrentUser().getUid()).setValue(true);
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

                }
            }catch (Exception e){
                e.printStackTrace();
            }
    }else{

        //show admob banner add
            loadBannerAdd(holder.adView);

    }
}


    private void loadBannerAdd(final AdView adView) {
        try {
            MobileAds.initialize(mContext);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
            adView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    adView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    // Code to be executed when an ad request fails.
                    adView.setVisibility(View.GONE);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public int getItemCount() {
       return  postsModelList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (postsModelList.get(position) == null){
            //show admob banner ad at null positions in the recyclerview created in
            // the displayAllPosts method in PostsFragment.java file
            return AD_TYPE;
        }
        return CONTENT_TYPE;
    }


    class AdapterViewHolder extends RecyclerView.ViewHolder{

        CardView all_image_posts_cardview,all_video_posts_cardview;
        CircleImageView image_user_post_profile_image,video_user_post_profile_image;
        TextView image_post_username,image_text_uploaded_a_post,image_post_date,
                image_post_time,image_post_title,image_post_description,
                image_number_of_likes_textView,image_no_of_comments_textView,video_post_username,video_text_uploaded_a_post,
                video_post_date,video_post_time,video_post_title,video_post_description,counter_timer,duration_timer,
                video_number_of_likes_textView,video_no_of_comments_textView;

        LinearLayout image_username_and_date_layout,image_img_username_date_layout,image_layout_partition_one,
                image_post_title_and_description,image_post_image_layout,image_layout_partition,
                video_username_and_date_layout,video_img_username_date_layout,video_layout_partition_one,
                video_post_title_and_description,controls_layout,video_layout_partition;

        ImageView image_post_image;
        ImageButton image_like_a_post,image_comment_on_post,image_download_post_image,play_btn,video_like_a_post,
                video_comment_on_post,video_download_post_image;
        RelativeLayout video_post_image_layout,post_video_layout;
        VideoView videoView;
        ProgressBar loading_video_progress_bar,video_progress;

        int countLikes, commentsCount;
        String currentuser_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        MediaController mediaController;
        AdView adView;

        private AdapterViewHolder(@NonNull View itemView) {
            super(itemView);


            adView = itemView.findViewById(R.id.adView);

            all_video_posts_cardview = itemView.findViewById(R.id.all_video_posts_cardview);
            video_username_and_date_layout = itemView.findViewById(R.id.video_username_and_date_layout);
            video_user_post_profile_image = itemView.findViewById(R.id.video_user_post_profile_image);
            video_post_username = itemView.findViewById(R.id.video_post_username);
            video_img_username_date_layout = itemView.findViewById(R.id.video_img_username_date_layout);
            video_text_uploaded_a_post = itemView.findViewById(R.id.video_text_uploaded_a_post);
            video_post_date = itemView.findViewById(R.id.video_post_date);
            video_post_time = itemView.findViewById(R.id.video_post_time);
            video_layout_partition_one = itemView.findViewById(R.id.video_layout_partition_one);
            video_post_title_and_description = itemView.findViewById(R.id.video_post_title_and_description);
            video_post_title = itemView.findViewById(R.id.video_post_title);
            video_post_description = itemView.findViewById(R.id.video_post_description);
            video_post_image_layout = itemView.findViewById(R.id.video_post_image_layout);
            post_video_layout= itemView.findViewById(R.id.post_video_layout);
            videoView = itemView.findViewById(R.id.post_video);
            loading_video_progress_bar = itemView.findViewById(R.id.loading_video_progress_bar);
            controls_layout = itemView.findViewById(R.id.controls_layout);
            play_btn = itemView.findViewById(R.id.play_btn);
            counter_timer = itemView.findViewById(R.id.counter_timer);
            video_progress = itemView.findViewById(R.id.video_progress);
            duration_timer = itemView.findViewById(R.id.duration_timer);
            video_layout_partition = itemView.findViewById(R.id.video_layout_partition);
            video_like_a_post = itemView.findViewById(R.id.video_like_a_post);
            video_number_of_likes_textView = itemView.findViewById(R.id.video_number_of_likes_textView);
            video_comment_on_post = itemView.findViewById(R.id.video_comment_on_post);
            video_no_of_comments_textView = itemView.findViewById(R.id.video_no_of_comments_textView);
            video_download_post_image = itemView.findViewById(R.id.video_download_post_image);


            all_image_posts_cardview = itemView.findViewById(R.id.all_image_posts_cardview);
            image_user_post_profile_image = itemView.findViewById(R.id.image_user_post_profile_image);
            image_post_username = itemView.findViewById(R.id.image_post_username);
            image_username_and_date_layout = itemView.findViewById(R.id.image_username_and_date_layout);
            image_img_username_date_layout = itemView.findViewById(R.id.image_img_username_date_layout);
            image_text_uploaded_a_post = itemView.findViewById(R.id.image_text_uploaded_a_post);
            image_post_date= itemView.findViewById(R.id.image_post_date);
            image_post_time = itemView.findViewById(R.id.image_post_time);
            image_layout_partition_one = itemView.findViewById(R.id.image_layout_partition_one);
            image_post_title_and_description = itemView.findViewById(R.id.image_post_title_and_description);
            image_post_title = itemView.findViewById(R.id.image_post_title);
            image_post_description = itemView.findViewById(R.id.image_post_description);
            image_post_image_layout = itemView.findViewById(R.id.image_post_image_layout);
            image_post_image = itemView.findViewById(R.id.image_post_image);
            image_layout_partition = itemView.findViewById(R.id.image_layout_partition);
            image_like_a_post = itemView.findViewById(R.id.image_like_a_post);
            image_number_of_likes_textView = itemView.findViewById(R.id.image_number_of_likes_textView);
            image_comment_on_post = itemView.findViewById(R.id.image_comment_on_post);
            image_no_of_comments_textView = itemView.findViewById(R.id.image_no_of_comments_textView);
            image_download_post_image  = itemView.findViewById(R.id.image_download_post_image);

        }

        public void setNoOfComments(final String PostKey) {

            try {

                DatabaseReference CommentsPostsRef;
                CommentsPostsRef = allPostsRef.child(PostKey).child("Comments");

                CommentsPostsRef.addValueEventListener(new ValueEventListener() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        dataSnapshot.getKey();
                        commentsCount = (int) dataSnapshot.getChildrenCount();
                        image_no_of_comments_textView.setText(String.format("%d Comments", commentsCount));

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {


                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        public void setLikeButtonStatus(final String PostKey){

            try {

                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            try {
                                if (dataSnapshot.child(PostKey).hasChild(currentuser_id)) {

                                    countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                                    image_like_a_post.setImageResource(R.drawable.ic_liked);
                                    image_number_of_likes_textView.setText(String.format("%s Likes", Integer.toString(countLikes)));
                                } else {
                                    countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                                    image_like_a_post.setImageResource(R.drawable.ic_like);
                                    image_number_of_likes_textView.setText(String.format("%s%s", Integer.toString(countLikes), " Likes"));
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        public void setFullname(String fullname){
            image_post_username.setText(fullname);
        }
        public void setProfileimage(final Context ctx, final String profileimage){

            try{
                Picasso.with(ctx).load(profileimage).networkPolicy(NetworkPolicy.OFFLINE)
                        .into(image_user_post_profile_image, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                try {
                                    Picasso.with(ctx).load(profileimage).into(image_user_post_profile_image);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        public void setTime(String time){
            image_post_time.setText("");
        }
        public void setDate(String date_published, String time_published){
            final TimeAgo timeAgo = new TimeAgo();

            String time = date_published +" "+time_published;
            Date date = null;
            try {
                date = new SimpleDateFormat("yy-MM-dd hh:mm:ss").parse(time);
                image_post_date.setText(timeAgo.getTimeAgo(date));

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        public void setTitle(String title){
            image_post_title.setText(title);
        }
        public void setDescription(String description){
            image_post_description.setText(description);
        }
        public void setPostimage(final Context ctx, final String postimage){
            try{
                Picasso.with(ctx).load(postimage).networkPolicy(NetworkPolicy.OFFLINE)
                        .into(image_post_image, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                try {
                                    Picasso.with(ctx).load(postimage).into(image_post_image);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public void setVideoFullname(String fullname) {
            video_post_username.setText(fullname);
        }
        public void setVideoDate(String date_published, String time_published) {
            final TimeAgo timeAgo = new TimeAgo();

            String time = date_published +" "+time_published;
            Date date = null;
            try {
                date = new SimpleDateFormat("yy-MM-dd hh:mm:ss").parse(time);
                video_post_date.setText(timeAgo.getTimeAgo(date));

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        public void setVideoDescription(String description) {
            video_post_description.setText(description);
        }
        public void setVideoTime(String time) {
            video_post_time.setText("");
        }
        public void setVideoTitle(String title) {
            video_post_title.setText(title);
        }
        public void setPostvideo(Activity activity, String postvideo) {

            try {
                controls_layout.setVisibility(View.GONE);

                videoView.setVideoURI(Uri.parse(postvideo));
                videoView.requestFocus();
                videoView.setBackgroundColor(mContext.getResources().getColor(R.color.default_video_post_bg));


                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {


                    @Override
                    public void onPrepared(MediaPlayer mp) {

                        mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                            @Override
                            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                                mediaController = new MediaController(mContext);
                                videoView.setMediaController(mediaController);
                                mediaController.setAnchorView(videoView);
                            }
                        });


                        mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                            @Override
                            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                                if (what == mp.MEDIA_INFO_BUFFERING_END) {
                                    loading_video_progress_bar.setVisibility(View.INVISIBLE);
                                    return true;
                                } else if (what == mp.MEDIA_INFO_BUFFERING_START) {
                                    loading_video_progress_bar.setVisibility(View.VISIBLE);
                                }
                                return false;
                            }
                        });

                        videoView.start();
                        videoView.setBackgroundColor(0);
                        loading_video_progress_bar.setVisibility(View.INVISIBLE);


                        videoView.seekTo(videoView.getCurrentPosition());
                        if (videoView.getCurrentPosition() != 0) {
                            videoView.start();

                        } else {
                            videoView.pause();
                        }


                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        public void setVideoProfileimage(Activity activity, final String profileimage) {
           try{
               Picasso.with(mContext).load(profileimage).networkPolicy(NetworkPolicy.OFFLINE)
                       .into(video_user_post_profile_image, new Callback() {
                           @Override
                           public void onSuccess() {

                           }

                           @Override
                           public void onError() {
                               try {
                                   Picasso.with(mContext).load(profileimage).into(video_user_post_profile_image);
                               }catch (Exception e){
                                   e.printStackTrace();
                               }
                           }
                       });
           }catch (Exception e){
               e.printStackTrace();
           }
        }
        public void setVideoLikeButtonStatus(final String postKey) {
            try {
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            try {
                                if (dataSnapshot.child(postKey).hasChild(currentuser_id)) {
                                    countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                                    video_like_a_post.setImageResource(R.drawable.ic_liked);
                                    video_number_of_likes_textView.setText(String.format("%s%s", Integer.toString(countLikes), " Likes"));
                                } else {
                                    countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                                    video_like_a_post.setImageResource(R.drawable.ic_like);
                                    video_number_of_likes_textView.setText(String.format("%s%s", Integer.toString(countLikes), " Likes"));
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        public void setVideoNoOfComments(String postKey) {

            try {
                DatabaseReference CommentsPostsRef;
                CommentsPostsRef = allPostsRef.child(postKey).child("Comments");

                CommentsPostsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        dataSnapshot.getKey();
                        commentsCount = (int) dataSnapshot.getChildrenCount();
                        video_no_of_comments_textView.setText(commentsCount + " Comments");

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    public void downloadPostImage(Context context, String FileName, String FileExtension, String
            DestinationDirectory, String uri){
        try {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri1 = Uri.parse(uri);
            DownloadManager.Request request = new DownloadManager.Request(uri1);
            request.setTitle("SmartChat (" + FileName + FileExtension + ")");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalFilesDir(context, DestinationDirectory, FileName + FileExtension);
            downloadManager.enqueue(request);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
