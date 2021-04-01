package niwigh.com.smartchat.Activity;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.File;

import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.TouchImageView;
import niwigh.com.smartchat.Util.Utilities;


public class FullMessageImageView extends AppCompatActivity {

    Toolbar toolbar;
    TouchImageView full_profile_image;
    DatabaseReference profileUserRef,usersRef;
    String currentUserID;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_message_image_view);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);

        toolbar = findViewById(R.id.navigation_opener_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(FullMessageImageView.this.getResources().getColor(R.color.intro_title_color));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        profileUserRef.keepSynced(true);

        String imgUrl = getIntent().getStringExtra("url");
        String fileName = getIntent().getStringExtra("name");
        getSupportActionBar().setTitle(fileName);

        full_profile_image = findViewById(R.id.full_profile_image);
        full_profile_image.setDrawingCacheEnabled(true);

        try{
            Picasso.with(FullMessageImageView.this).load(imgUrl).into(full_profile_image);
        }catch (Exception e){
            e.printStackTrace();
        }
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

        String fileName = getIntent().getStringExtra("name");
        String fileUrl = getIntent().getStringExtra("url");

        File root = Environment.getExternalStorageDirectory();
        root.mkdirs();
        String path = root.toString();
        String FileName = fileName;
        String FileExtension = ".jpg";
        String DestinationDirectory = path + "/SmartChat" + "/Messages" + "/Images";

        DownloadManager downloadManager = (DownloadManager) FullMessageImageView.this.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri1 = Uri.parse(fileUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri1);
        request.setTitle("SmartChat (" + FileName + FileExtension +")");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(FullMessageImageView.this, DestinationDirectory, FileName + FileExtension);
        downloadManager.enqueue(request);



    }


}