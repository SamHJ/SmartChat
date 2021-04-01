package niwigh.com.smartchat.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;
import id.zelory.compressor.Compressor;

public class UpdateGroupCollection extends AppCompatActivity {

    private Toolbar toolbar;

    CircleImageView collection_image;
    TextInputEditText collection_name;
    Button update_btn;
    String collectionName;
    Utilities utilities;
    View currentView;
    private Bitmap compressed_bitmap;
    private byte[] compressed_byte;
    private static final int REQUEST_STORAGE_PERMISSION = 1003;
    private StorageReference groupsImageRef;
    FirebaseStorage storage;
    FirebaseAuth mAuth;
    String currentUserID;
    private DatabaseReference groupsRef;

    String collectionkey,collectionname,collectionimage;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_group_collection);

        toolbar = findViewById(R.id.all_groups_appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Group Collection");
        utilities = Utilities.getInstance(this);
        loadingBar = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        collectionkey = getIntent().getStringExtra("collectionkey");
        collectionimage = getIntent().getStringExtra("collectionimage");
        collectionname = getIntent().getStringExtra("collectionname");
        groupsImageRef = FirebaseStorage.getInstance().getReference().child("Group Images");


        groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(collectionkey);
        groupsRef.keepSynced(true);

        storage  = FirebaseStorage.getInstance();

        currentView = getWindow().getDecorView().getRootView();

        collection_image = findViewById(R.id.collection_image);
        collection_name = findViewById(R.id.collection_name);
        update_btn = findViewById(R.id.update_btn);

        collection_name.setText(collectionname);

        if(collectionimage != null){
            Picasso.with(this).load(collectionimage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.easy_to_use)
                    .into(collection_image, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            Picasso.with(UpdateGroupCollection.this)
                                    .load(collectionimage).placeholder(R.drawable.easy_to_use).into(collection_image);
                        }
                    });
        }


        collection_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });

        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collectionName = collection_name.getText().toString().trim();

                if(compressed_byte != null){
                    if(collectionName.isEmpty()){
                        utilities.customErrorDialog(currentView,"OK","Error",
                                "Collection name must not be empty!");
                    }else{
                        //update both collection name and image
                        uploadCollectionImage(collectionName);
                    }
                }else{
                    //update only collection name
                    updateGroupCollection(collectionName);
                }

            }
        });
    }

    private void updateGroupCollection(final String collectionName) {

        loadingBar.setTitle("Updating " + collectionName);
        loadingBar.setMessage("a moment please...");
        loadingBar.show();

        groupsRef.child("name").setValue(collectionName)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toasty.success(UpdateGroupCollection.this,
                                    collectionName + " collection updated successfully", Toasty.LENGTH_SHORT).show();
                            finish();
                            overridePendingTransition(R.anim.left_in, R.anim.right_out);
                        }
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
            startImageSelect();
        }
    }

    private void startImageSelect() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(UpdateGroupCollection.this);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();
                File compressed_image_path = new File(resultUri.getPath());
                try {

                    compressed_bitmap = new Compressor(this)
                            .setMaxWidth(500)
                            .setMaxHeight(500)
                            .setQuality(80)
                            .compressToBitmap(compressed_image_path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                compressed_bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                compressed_byte = byteArrayOutputStream.toByteArray();
                collection_image.setImageBitmap(compressed_bitmap);
            }
        }

    }


    private void uploadCollectionImage(final String collection_name){
        final StorageReference filePath = groupsImageRef.child(currentUserID + ".jpg");
        filePath.putBytes(compressed_byte).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    //tell them that the image was stored in the storage
                    //first we get the link of this image uploaded

                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downUrl = uri;
                            final String fileUrl = downUrl.toString();

                            Map<String, Object> groupCollectionMap = new HashMap<String, Object>();
                            groupCollectionMap.put("image", fileUrl);
                            groupCollectionMap.put("name", collection_name);
                            loadingBar.setTitle("");
                            loadingBar.setMessage("Updating records...");

                            groupsRef.updateChildren(groupCollectionMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toasty.success(UpdateGroupCollection.this,
                                                        collection_name + " collection updated successfully",
                                                        Toasty.LENGTH_SHORT).show();
                                                finish();
                                                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                                            }
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
                    View dialogView = LayoutInflater.from(UpdateGroupCollection.this).inflate(R.layout.error_dialog, viewGroup, false);


                    //Now we need an AlertDialog.Builder object
                    AlertDialog.Builder builder = new AlertDialog.Builder(UpdateGroupCollection.this);

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
                loadingBar.setTitle("Uploading Collection Image");
                loadingBar.setMessage(taskSnapshot.getBytesTransferred() / (1024 * 1024) + " / " + taskSnapshot.getTotalByteCount() / (1024 * 1024) + "MB");
                loadingBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                loadingBar.setProgress((int) progress);
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(false);
            }
        });
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
