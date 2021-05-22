package niwigh.com.smartchat.Activity;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;

import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;


public class FullMessageVideoView extends AppCompatActivity {

    Toolbar toolbar;
    VideoView full_message_video;
    DatabaseReference profileUserRef,usersRef;
    String currentUserID;
    FirebaseAuth mAuth;
    ProgressBar loading_video_progress_bar;
    MediaController mediaController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_message_video_view);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);

        toolbar = findViewById(R.id.navigation_opener_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(FullMessageVideoView.this.getResources().getColor(R.color.intro_title_color));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        profileUserRef.keepSynced(true);

        String vidUrl = getIntent().getStringExtra("url");
        String fileName = getIntent().getStringExtra("name");
        getSupportActionBar().setTitle(fileName);

        full_message_video = findViewById(R.id.full_message_video);

        full_message_video.setVideoURI(Uri.parse(vidUrl));

        loading_video_progress_bar = findViewById(R.id.loading_video_progress_bar);


        full_message_video.requestFocus();
        full_message_video.setBackgroundColor(this.getResources().getColor(R.color.default_video_post_bg));





        full_message_video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {



            @Override
            public void onPrepared(MediaPlayer mp) {

                try {
                    mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                        @Override
                        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                            mediaController = new MediaController(FullMessageVideoView.this);
                            full_message_video.setMediaController(mediaController);
                            mediaController.setAnchorView(full_message_video);
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

                    full_message_video.start();
                    full_message_video.setBackgroundColor(0);
                    loading_video_progress_bar.setVisibility(View.INVISIBLE);


                    full_message_video.seekTo(full_message_video.getCurrentPosition());
                    if (full_message_video.getCurrentPosition() != 0) {
                        full_message_video.start();

                    } else {
                        full_message_video.pause();
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }


    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.full_image_menu, menu);
        if(menu instanceof MenuBuilder){
            MenuBuilder menuBuilder = (MenuBuilder) menu;
            menuBuilder.setOptionalIconsVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.full_image_menu_id:
                DownloadImageToPhone();
                return true;
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            default:
                return super.onOptionsItemSelected(item);
        }
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



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
        finish();
    }

    public void DownloadImageToPhone(){

        try {
            String fileName = getIntent().getStringExtra("name");
            String fileUrl = getIntent().getStringExtra("url");

            File root = Environment.getExternalStorageDirectory();
            root.mkdirs();
            String path = root.toString();
            String FileName = fileName;
            String FileExtension = ".mp4";
            String DestinationDirectory = path + "/SmartChat" + "/Messages" + "/Videos";

            DownloadManager downloadManager = (DownloadManager) FullMessageVideoView.this.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri1 = Uri.parse(fileUrl);
            DownloadManager.Request request = new DownloadManager.Request(uri1);
            request.setTitle("SmartChat (" + FileName + FileExtension + ")");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalFilesDir(FullMessageVideoView.this, DestinationDirectory, FileName + FileExtension);
            downloadManager.enqueue(request);
        }catch(Exception e){
            e.printStackTrace();
        }

    }


}