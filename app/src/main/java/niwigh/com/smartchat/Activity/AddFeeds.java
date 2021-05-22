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
import android.support.design.widget.TextInputEditText;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import es.dmoral.toasty.Toasty;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;
import niwigh.com.smartchat.core.ImageCompressTask;
import niwigh.com.smartchat.listeners.IImageCompressTaskListener;

public class AddFeeds extends AppCompatActivity {

    private Toolbar toolbar;
    ImageView feed_image;
    FloatingActionButton btn_add_feed_image;
    TextInputEditText feed_title_edit_text,feed_url_edit_text,feed_type_edit_text;
    Button btn_add_feed;
    private ArrayList<Object> spinnerArray;
    private ArrayAdapter spinnerArrayAdapter;
    Spinner spinner;
    DatabaseReference feedsRef,usersRef;
    final static int gallery_Pic = 2;
    FirebaseAuth mAuths;
    private String currentUserID;
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    String saveCurrentDate, saveCurrentTime, postRandomName;
    ProgressDialog loadingBar;
    StorageReference postsImagesStorageRef;

    //create a single thread pool to our image compression class.
    private ExecutorService mExecutorService = Executors.newFixedThreadPool(1);

    private ImageCompressTask imageCompressTask;
    private String feedTitle,feedUrl,feedType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_feeds);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add New Feed");

        loadingBar = new ProgressDialog(this);
        postsImagesStorageRef = FirebaseStorage.getInstance().getReference();

        feedsRef = FirebaseDatabase.getInstance().getReference().child("Feeds");
        feedsRef.keepSynced(true);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);

        mAuths = FirebaseAuth.getInstance();
        currentUserID = mAuths.getCurrentUser().getUid();

        feed_image = findViewById(R.id.feed_image);
        btn_add_feed_image = findViewById(R.id.btn_add_feed_image);
        feed_title_edit_text = findViewById(R.id.feed_title_edit_text);
        feed_url_edit_text = findViewById(R.id.feed_url_edit_text);
        btn_add_feed = findViewById(R.id.btn_add_feed);
        feed_type_edit_text = findViewById(R.id.feed_type_edit_text);
        spinner = findViewById(R.id.spinner);

        spinnerArray = new ArrayList<>();
        spinnerArray.add("url");
        spinnerArray.add("image");
        spinnerArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    feed_type_edit_text.setText("url");
                    enableFields();
                }else {
                    feed_type_edit_text.setText("image");
                    disableFields();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btn_add_feed_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });
        btn_add_feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatePostInfo();
            }
        });
    }

    public void validatePostInfo(){
        try {

            feedTitle = feed_title_edit_text.getText().toString().trim();
            feedUrl = feed_url_edit_text.getText().toString().trim();
            feedType = feed_type_edit_text.getText().toString().trim();


            if (feed_image.getTag() == null) {
                showErrorCustomDialog();
            } else if (feedTitle.isEmpty()) {
                //all fields are required dialog
                showAllFieldsAreRequired();
            } else {

                storeImageToFirebaseStorage(feedTitle, feedUrl, feedType);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @SuppressLint("SimpleDateFormat")
    private void storeImageToFirebaseStorage(final String feedTitle, final String feedUrl, final String feedType) {
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
            final Uri compressedPostImageUri = Uri.parse(feed_image.getTag().toString());

            final StorageReference filePath = postsImagesStorageRef.child("Feed Images")
                    .child(compressedPostImageUri.getLastPathSegment() + postRandomName + ".jpg");


            filePath.putFile(compressedPostImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()) {
                        try {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downUrl = uri;
                                    final String fileUrl = downUrl.toString();

                                    Map<String, Object> feedMap = new HashMap<String, Object>();
                                    feedMap.put("image_url", fileUrl);
                                    feedMap.put("title", feedTitle);
                                    feedMap.put("type", feedType);
                                    feedMap.put("url", feedUrl);
                                    feedMap.put("feedimagestoragename", compressedPostImageUri.getLastPathSegment() + postRandomName + ".jpg");

                                    feedsRef.child(feedTitle.toLowerCase()).updateChildren(feedMap)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        loadingBar.dismiss();
                                                        Toasty.success(AddFeeds.this, "Feed added successfully!", Toasty.LENGTH_SHORT).show();
                                                        feed_title_edit_text.setText("");
                                                        feed_url_edit_text.setText("");
                                                        feed_image.setImageDrawable(getResources().getDrawable(R.drawable.easy_to_use));
                                                    }
                                                }
                                            });

                                }
                            });
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    } else {
                        String message = task.getException().getMessage();

                        //{.....................
                        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                        ViewGroup viewGroup = findViewById(android.R.id.content);

                        //then we will inflate the custom alert dialog xml that we created
                        View dialogView = LayoutInflater.from(AddFeeds.this)
                                .inflate(R.layout.error_dialog, viewGroup, false);


                        //Now we need an AlertDialog.Builder object
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddFeeds.this);

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
                    loadingBar.setTitle("Uploading Feed");
                    loadingBar.setMessage(taskSnapshot.getBytesTransferred() / (1024 * 1024) + " / " + taskSnapshot.getTotalByteCount() / (1024 * 1024) + "MB");
                    loadingBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    loadingBar.setProgress((int) progress);
                    loadingBar.show();
                    loadingBar.setCanceledOnTouchOutside(false);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
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

    @SuppressLint("SetTextI18n")
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
        success_text.setText("Please select an image for this feed!");

        // if the OK button is clicked, close the success dialog
        dialog_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();


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
                selectFeedImageFromGallery();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == gallery_Pic && resultCode == RESULT_OK &&
                data != null) {
            try {
                //extract absolute image path from Uri
                Uri uri = data.getData();
                Cursor cursor = MediaStore.Images.Media.query(getContentResolver(), uri, new String[]{MediaStore.Images.Media.DATA});

                if (cursor != null && cursor.moveToFirst()) {
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                    //Create ImageCompressTask and execute with Executor.
                    imageCompressTask = new ImageCompressTask(this, path, iImageCompressTaskListener);

                    mExecutorService.execute(imageCompressTask);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
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
                Uri compressedImageUri = Uri.fromFile(file);

                Log.d("ImageCompressor", "New photo size ==> " + file.length()); //log new file size.

                feed_image.setImageURI(compressedImageUri);
                feed_image.setTag(compressedImageUri);
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

    private void selectFeedImageFromGallery() {
        Intent gallery_intent = new Intent();
        gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
        gallery_intent.setType("image/*");
        startActivityForResult(Intent.createChooser(gallery_intent, "Select image"), gallery_Pic);
    }

    private void enableFields() {
        feed_url_edit_text.setEnabled(true);
    }

    private void disableFields(){
        feed_url_edit_text.setEnabled(false);
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
