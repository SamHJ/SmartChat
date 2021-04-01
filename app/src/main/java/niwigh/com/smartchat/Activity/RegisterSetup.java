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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
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
import id.zelory.compressor.Compressor;

public class RegisterSetup extends AppCompatActivity {
    EditText register_setup_username, register_setup_full_name, register_setup_location;
    Button register_save_setup_btn;
    CircleImageView register_setup_profile_image;
    FirebaseAuth mAuth;
    DatabaseReference UserRef;
    String currentUserID;
    final static int gallery_Pic = 1;
    StorageReference UserProfileImageRef;
    ProgressDialog loadingBar;
    Bitmap compressed_bitmap = null;
    FirebaseFirestore firebaseFirestore;
    byte[] compressed_byte;
    String profileimage;
    private static final int REQUEST_STORAGE_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserRef.keepSynced(true);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        firebaseFirestore = FirebaseFirestore.getInstance();

        register_setup_username = findViewById(R.id.register_setup_username);
        register_setup_full_name = findViewById(R.id.register_setup_full_name);
        register_setup_location = findViewById(R.id.register_setup_location);
        register_save_setup_btn = findViewById(R.id.btn_register_save_setup);
        register_setup_profile_image = findViewById(R.id.register_setup_profile);
        loadingBar = new ProgressDialog(this);


        register_save_setup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveUserDetails();
            }
        });

        register_setup_profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               requestPermission();
            }
        });

        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    if(dataSnapshot.hasChild("profileimage")){
                        try{

                            profileimage = dataSnapshot.child("profileimage").getValue().toString();
                            Picasso.with(RegisterSetup.this).load(profileimage)
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.easy_to_use).into(register_setup_profile_image, new
                                    Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {

                                            Picasso.with(RegisterSetup.this).load(profileimage).placeholder(R.drawable.easy_to_use).into(register_setup_profile_image);

                                        }
                                    });

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

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
            startImageSelect();
        }
    }

    private void startImageSelect() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(RegisterSetup.this);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){

                Uri resultUri = result.getUri();
                File compressed_image_path = new File(resultUri.getPath());
                try{

                    compressed_bitmap = new Compressor(this)
                            .setMaxWidth(500)
                            .setMaxHeight(500)
                            .setQuality(80)
                            .compressToBitmap(compressed_image_path);
                }
                catch (IOException e){
                    e.printStackTrace();
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                compressed_bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
                compressed_byte =  byteArrayOutputStream.toByteArray();


                final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");
                filePath.putBytes(compressed_byte).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            //tell them that the image was stored in the storage
                            //first we get the link of this image uploaded

                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downUrl = uri;
                                    final String fileUrl = downUrl.toString();

                                    Map<String,Object> imageMap = new HashMap<String, Object>();
                                    imageMap.put("profileimage",fileUrl);
                                    imageMap.put("profileimagestoragename",currentUserID + ".jpg");

                                    UserRef.updateChildren(imageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                // image stored to firebase database successfully
                                                loadingBar.dismiss();
                                                Toasty.success(getApplicationContext(),"Profile Image uploaded successfully", Toasty.LENGTH_SHORT, true).show();
                                            }
                                        }
                                    });

                                }
                            });
                        }else {
                            String message = task.getException().getMessage();
                            //{.....................
                            //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                            ViewGroup viewGroup = findViewById(android.R.id.content);

                            //then we will inflate the custom alert dialog xml that we created
                            View dialogView = LayoutInflater.from(RegisterSetup.this).inflate(R.layout.error_dialog, viewGroup, false);


                            //Now we need an AlertDialog.Builder object
                            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterSetup.this);

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
                        double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                        loadingBar.setTitle("Uploading Image");
                        loadingBar.setMessage(taskSnapshot.getBytesTransferred()/(1024*1024) + " / " + taskSnapshot.getTotalByteCount()/(1024*1024) + "MB");
                        loadingBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        loadingBar.setProgress((int)progress);
                        loadingBar.show();
                        loadingBar.setCanceledOnTouchOutside(false);
                    }
                });

            }else {
                //{.....................
                //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                ViewGroup viewGroup = findViewById(android.R.id.content);

                //then we will inflate the custom alert dialog xml that we created
                View dialogView = LayoutInflater.from(RegisterSetup.this).inflate(R.layout.error_dialog, viewGroup, false);


                //Now we need an AlertDialog.Builder object
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterSetup.this);

                //setting the view of the builder to our custom view that we already inflated
                builder.setView(dialogView);

                //finally creating the alert dialog and displaying it
                final AlertDialog alertDialog = builder.create();

                Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
                TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
                TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

                dialog_btn.setText("OK");
                success_title.setText("Error");
                success_text.setText("Image can't be cropped.");

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

    }

    @SuppressLint("SetTextI18n")
    private void SaveUserDetails() {
        final String username, fullname, location;
        username = register_setup_username.getText().toString().trim();
        fullname = register_setup_full_name.getText().toString().trim();
        location = register_setup_location.getText().toString().trim();

        if(username.isEmpty() || fullname.isEmpty() || location.isEmpty()){
            //display an error
            showErrorCustomDialog();
        }else {

            loadingBar.setTitle("Saving details...");
            loadingBar.setMessage("a moment please");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(false);

            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if(task.isSuccessful()){
                                String deviceToken = task.getResult().getToken();
                                //save the user details to database;
                                Map<String,Object> userMap = new HashMap<String, Object>();
                                userMap.put("username", username);
                                userMap.put("fullname", fullname);
                                userMap.put("location", location);
                                userMap.put("profilestatus", "Hey there, I'm using SmartChat");
                                userMap.put("gender", "none");
                                userMap.put("dob", "none");
                                userMap.put("device_token",deviceToken);
                                userMap.put("profileimage", profileimage != null ? profileimage : "");
                                userMap.put("id", currentUserID);
                                userMap.put("interestedin", "Male and Female");
                                userMap.put("phone", "none");
                                UserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                            Map<String, Object> fullnameMap = new HashMap<>();
                                            fullnameMap.put("name", fullname);
                                            firebaseFirestore.collection("Users").document(currentUserID)
                                                    .update(fullnameMap);

                                            loadingBar.dismiss();
                                            successToast();
                                            sendUserToHomeActivity();

                                        }
                                        else {
                                            String message = task.getException().getMessage();
                                            //{.....................
                                            //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                                            ViewGroup viewGroup = findViewById(android.R.id.content);

                                            //then we will inflate the custom alert dialog xml that we created
                                            View dialogView = LayoutInflater.from(RegisterSetup.this).inflate(R.layout.error_dialog, viewGroup, false);


                                            //Now we need an AlertDialog.Builder object
                                            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterSetup.this);

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
                                });
                            }
                        }
                    });


        }
    }

    private void sendUserToHomeActivity() {
        Intent userhomeIntent = new Intent(getApplicationContext(), UserHome.class);
        startActivity(userhomeIntent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        finish();
    }
    private void successToast(){
        Toasty.success(this,"Account setup completed", Toasty.LENGTH_LONG, true).show();
    }
    @SuppressLint("SetTextI18n")
    private void showErrorCustomDialog() {
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
        success_text.setText("All fields are required");

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
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
        finish();
    }
}