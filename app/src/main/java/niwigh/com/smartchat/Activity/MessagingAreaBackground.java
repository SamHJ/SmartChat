package niwigh.com.smartchat.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;
import niwigh.com.smartchat.core.ImageCompressTask;
import niwigh.com.smartchat.listeners.IImageCompressTaskListener;


public class MessagingAreaBackground extends AppCompatActivity {

    RelativeLayout bg_settings_layout;
    RadioGroup bg_radio_group;
    Toolbar toolbar;
    DatabaseReference usersRef;
    FirebaseAuth mAuth;
    String currentUserID;
    final static int gallery_Pic = 11;
    private static final int REQUEST_STORAGE_PERMISSION = 1001;
    //create a single thread pool to our image compression class.
    private ExecutorService mExecutorService = Executors.newFixedThreadPool(1);

    private ImageCompressTask imageCompressTask;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging_area_background);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        preferences = getSharedPreferences("MESSAGING_AREA_BG", Context.MODE_PRIVATE);
        editor = preferences.edit();

        bg_settings_layout = findViewById(R.id.bg_settings_layout);
        bg_radio_group =findViewById(R.id.bg_radio_group);
        toolbar = findViewById(R.id.messages_bg_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Change Chat Background");

        if(preferences.contains("imagefromgallery")){

            try{
                Picasso.with(MessagingAreaBackground.this).
                        load(Uri.parse(preferences.getString("imagefromgallery",null))).into(new Target(){

                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        bg_settings_layout.setBackground(new BitmapDrawable(getResources(), bitmap));
                    }

                    @Override
                    public void onBitmapFailed(final Drawable errorDrawable) {
                        Log.d("TAG", "FAILED");
                        Toast.makeText(MessagingAreaBackground.this, "Couldn't set image as background!",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPrepareLoad(final Drawable placeHolderDrawable) {
                        Log.d("TAG", "Prepare Load");
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }

        }else{
            if(preferences.contains(("backgroundId"))){
                bg_settings_layout.setBackgroundResource(preferences.getInt("backgroundId", 0));

            }
        }


        bg_radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                int BackgroundCode = 0;

                switch (checkedId) {
                    case R.id.default_radio_btn:
                        BackgroundCode = R.mipmap.default_cbg;
                        break;
                    case R.id.bubble_radio_btn:
                        BackgroundCode = R.mipmap.bubble_purple;
                        break;
                    case R.id.goldie_radio_btn:
                        BackgroundCode = R.mipmap.goldie;
                        break;
                    case R.id.clown_radio_btn:
                        BackgroundCode = R.mipmap.clown;
                        break;
                    case R.id.headshot_radio_btn:
                        BackgroundCode = R.mipmap.headshot;
                        break;
                    case R.id.road_radio_btn:
                        BackgroundCode = R.mipmap.road;
                        break;
                    case R.id.robot_radio_btn:
                        BackgroundCode = R.mipmap.robot;
                        break;
                    case R.id.selfie_radio_btn:
                        BackgroundCode = R.mipmap.selfie;
                        break;
                }


                bg_settings_layout.setBackgroundResource(BackgroundCode);
                editor.putInt("backgroundId", BackgroundCode);
                //remove background image from gallery if any
                editor.remove("imagefromgallery");
                editor.apply();

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.view_group_menu, menu);

        MenuItem item = menu.findItem(R.id.action_view_group);
        item.setTitle("From Gallery");

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            overridePendingTransition(R.anim.left_in, R.anim.right_out);
        }else if(item.getItemId() == R.id.action_view_group){
            requestPermission();
        }
        return super.onOptionsItemSelected(item);
    }


    private void requestPermission() {
        try {
            if (PackageManager.PERMISSION_GRANTED !=
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_STORAGE_PERMISSION);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_STORAGE_PERMISSION);
                }
            } else {
                //Permission Granted, lets go pick photo
                selectImageFromGallery();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void selectImageFromGallery() {
        Intent gallery_intent = new Intent();
        gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
        gallery_intent.setType("image/*");
        startActivityForResult(Intent.createChooser(gallery_intent, "Select image"), gallery_Pic);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == gallery_Pic && resultCode == RESULT_OK &&
                    data != null) {
                //extract absolute image path from Uri
                Uri uri = data.getData();
                Cursor cursor = MediaStore.Images.Media.query(getContentResolver(), uri, new String[]{MediaStore.Images.Media.DATA});

                if (cursor != null && cursor.moveToFirst()) {
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                    //Create ImageCompressTask and execute with Executor.
                    imageCompressTask = new ImageCompressTask(this, path, iImageCompressTaskListener);

                    mExecutorService.execute(imageCompressTask);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //image compress task callback
    private IImageCompressTaskListener iImageCompressTaskListener = new IImageCompressTaskListener() {
        @Override
        public void onComplete(List<File> compressed) {
            try {
                //photo compressed. Yay!

                //prepare for uploads. Use an Http library like Retrofit, Volley or async-http-client (My favourite)

                File file = compressed.get(0);
                final Uri compressedImageUri = Uri.fromFile(file);

                Log.d("ImageCompressor", "New photo size ==> " + file.length()); //log new file size.

                try {
                    Picasso.with(MessagingAreaBackground.this).load(compressedImageUri).into(new Target() {

                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            bg_settings_layout.setBackground(new BitmapDrawable(getResources(), bitmap));
                            storeImageToPref(compressedImageUri);
                        }

                        @Override
                        public void onBitmapFailed(final Drawable errorDrawable) {
                            Log.d("TAG", "FAILED");
                            Toast.makeText(MessagingAreaBackground.this, "Couldn't set image as background!",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPrepareLoad(final Drawable placeHolderDrawable) {
                            Log.d("TAG", "Prepare Load");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onError(Throwable error) {
            //very unlikely, but it might happen on a device with extremely low storage.
            //log it, log.WhatTheFuck?, or show a dialog asking the user to delete some files....etc, etc
            Log.wtf("ImageCompressor", "Error occurred", error);
        }
    };

    private void storeImageToPref(Uri imageuri) {

        editor.putString("imagefromgallery",String.valueOf(imageuri));
        editor.apply();
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
}