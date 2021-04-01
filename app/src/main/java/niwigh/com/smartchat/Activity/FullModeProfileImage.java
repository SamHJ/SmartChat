package niwigh.com.smartchat.Activity;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import java.text.SimpleDateFormat;
import java.util.Locale;

import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.TouchImageView;
import niwigh.com.smartchat.Util.Utilities;


public class FullModeProfileImage extends AppCompatActivity {

    Toolbar toolbar;
    TouchImageView full_profile_image;
    FloatingActionButton share_fab;
    DatabaseReference profileUserRef,usersRef;
    String currentUserID;
    FirebaseAuth mAuth;
    String profile_image,user_fullname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_mode_profile_image);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);

        toolbar = findViewById(R.id.navigation_opener_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(FullModeProfileImage.this.getResources().getColor(R.color.intro_title_color));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        profileUserRef.keepSynced(true);

        full_profile_image = findViewById(R.id.full_profile_image);
        full_profile_image.setDrawingCacheEnabled(true);
        share_fab = findViewById(R.id.share_fab);



        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    profile_image = dataSnapshot.child("profileimage").getValue().toString();
                    user_fullname = dataSnapshot.child("fullname").getValue().toString();
                    getSupportActionBar().setTitle(user_fullname);

                   try{
                       Picasso.with(FullModeProfileImage.this).load(profile_image).networkPolicy(NetworkPolicy.OFFLINE)
                               .placeholder(R.drawable.easy_to_use).into(full_profile_image, new
                               Callback() {
                                   @Override
                                   public void onSuccess() {

                                   }

                                   @Override
                                   public void onError() {

                                       Picasso.with(FullModeProfileImage.this).load(profile_image).placeholder(R.drawable.easy_to_use).into(full_profile_image);
                                   }
                               });
                   }catch (Exception e){
                       e.printStackTrace();
                   }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        share_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri uri = Uri.parse(profile_image);
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType("image/*");
                startActivity(Intent.createChooser(shareIntent, "Share " + user_fullname + "'s profile image via"));

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

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(System.currentTimeMillis());

        File root = Environment.getExternalStorageDirectory();
        root.mkdirs();
        String path = root.toString();
        String FileName = user_fullname + timestamp;
        String FileExtension = ".jpg";
        String DestinationDirectory = path + "/SmartChat" + "/Profile Images";

        DownloadManager downloadManager = (DownloadManager) FullModeProfileImage.this.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri1 = Uri.parse(profile_image);
        DownloadManager.Request request = new DownloadManager.Request(uri1);
        request.setTitle("SmartChat (" + FileName + FileExtension +")");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(FullModeProfileImage.this, DestinationDirectory, FileName + FileExtension);
        downloadManager.enqueue(request);
    }


}