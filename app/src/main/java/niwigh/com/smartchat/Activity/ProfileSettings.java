package niwigh.com.smartchat.Activity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;
import id.zelory.compressor.Compressor;


public class ProfileSettings extends AppCompatActivity {

    EditText userfullname, username, dob, profile_settings_location, user_phone, user_interestedin, gender, status;
    Button btn_save_changes;
    DatabaseReference saveProfileSettingsRef,usersRef;
    FirebaseAuth mAuth;
    String currentUserID;
    CircleImageView profile_post_image;
    ProgressDialog loadingBar;
    final static int gallery_Pic = 1;
    StorageReference UserProfileImageRef;
    final Calendar myCalender = Calendar.getInstance();
    Bitmap compressed_bitmap = null;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);
        toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        saveProfileSettingsRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        saveProfileSettingsRef.keepSynced(true);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");


        //init views
        loadingBar = new ProgressDialog(this);

        profile_post_image = findViewById(R.id.user_profile_image_view);
        btn_save_changes = findViewById(R.id.btn_save_changes);
        userfullname = findViewById(R.id.profile_setting_full_name);
        username  = findViewById(R.id.profile_setting_user_name);
        dob = findViewById(R.id.profile_settings_dob_edittext);
        profile_settings_location = findViewById(R.id.profile_settings_location);
        user_phone = findViewById(R.id.profile_settings_phone_number_edit_text);
        user_interestedin = findViewById(R.id.profile_settings_interested_in);
        gender = findViewById(R.id.profile_settings_gender);
        status = findViewById(R.id.profile_status_edit_text);

        saveProfileSettingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    final String profile_image = String.valueOf(dataSnapshot.child("profileimage").getValue());
                    String user_fullname = String.valueOf(dataSnapshot.child("fullname").getValue());
                    String user_name = String.valueOf(dataSnapshot.child("username").getValue());
                    String user_phone_p = String.valueOf(dataSnapshot.child("phone").getValue());
                    String interested_in = String.valueOf(dataSnapshot.child("interestedin").getValue());
                    String user_gender = String.valueOf(dataSnapshot.child("gender").getValue());
                    String user_status = String.valueOf(dataSnapshot.child("profilestatus").getValue());
                    String user_dob = String.valueOf(dataSnapshot.child("dob").getValue());
                    try{
                        String user_location = String.valueOf(dataSnapshot.child("location").getValue());
                        profile_settings_location.setText(user_location);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    try{
                        Picasso.with(ProfileSettings.this).load(profile_image)
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.easy_to_use).into(profile_post_image, new
                                Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {

                                        Picasso.with(ProfileSettings.this).load(profile_image).placeholder(R.drawable.easy_to_use).into(profile_post_image);

                                    }
                                });
                    }catch (Exception e){
                        e.printStackTrace();
                    }


                    userfullname.setText(user_fullname);
                    username.setText(user_name);
                    dob.setText(user_dob);
                    user_phone.setText(user_phone_p);
                    user_interestedin.setText(interested_in);
                    gender.setText(user_gender);
                    status.setText(user_status);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        profile_post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dialog_to_profile = new Intent(ProfileSettings.this, FullModeProfileImage.class);
                startActivity(dialog_to_profile);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(ProfileSettings.this);
            }
        });


        btn_save_changes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateUserInputs();
            }
        });


        //datepickerdialog
        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                myCalender.set(Calendar.YEAR, year);
                myCalender.set(Calendar.MONTH, month);
                myCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(ProfileSettings.this, dateSetListener, myCalender
                        .get(Calendar.YEAR), myCalender.get(Calendar.MONTH),
                        myCalender.get(Calendar.DAY_OF_MONTH)).show();
            }
        });




    }

    public void updateLabel(){
        String myFormat = "dd/MM/yy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(myFormat, Locale.US);
        dob.setText(simpleDateFormat.format(myCalender.getTime()));
    }


    @SuppressLint("SetTextI18n")
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK){

                final Uri resultUri = result.getUri();
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
                final  byte[] compressed_byte =  byteArrayOutputStream.toByteArray();

                final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");
                filePath.putBytes(compressed_byte).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            //tell them that the image was stored in the firebase storage
                            //first we get the link of this image uploaded

                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downUrl = uri;
                                    final String fileUrl = downUrl.toString();

                                    Map<String,Object> imageMap = new HashMap<String, Object>();
                                    imageMap.put("profileimage",fileUrl);
                                    imageMap.put("profileimagestoragename",currentUserID + ".jpg");

                                    saveProfileSettingsRef.updateChildren(imageMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                // image stored to firebase database successfully
                                                loadingBar.dismiss();
                                                Toasty.success(getApplicationContext(),"Profile Image updated successfully.", Toasty.LENGTH_SHORT, true).show();
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
                            View dialogView = LayoutInflater.from(ProfileSettings.this).inflate(R.layout.error_dialog, viewGroup, false);


                            //Now we need an AlertDialog.Builder object
                            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileSettings.this);

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
                View dialogView = LayoutInflater.from(ProfileSettings.this).inflate(R.layout.error_dialog, viewGroup, false);


                //Now we need an AlertDialog.Builder object
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileSettings.this);

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
    private void ValidateUserInputs() {

        String user_full_name = userfullname.getText().toString().trim();
        String user_name = username.getText().toString().trim();
        String user_dob = dob.getText().toString().trim();
        String user_location = profile_settings_location.getText().toString().trim();
        String user_phone_pp = user_phone.getText().toString().trim();
        String user_interest = user_interestedin.getText().toString().trim();
        String user_gender = gender.getText().toString().trim();
        String user_status = status.getText().toString().trim();

        if(user_full_name.isEmpty() || user_location.isEmpty() || user_gender.isEmpty() || user_name.isEmpty()){

            //{.....................
            //before inflating the custom alert dialog layout, we will get the current activity viewgroup
            ViewGroup viewGroup = findViewById(android.R.id.content);

            //then we will inflate the custom alert dialog xml that we created
            View dialogView = LayoutInflater.from(ProfileSettings.this).inflate(R.layout.error_dialog, viewGroup, false);


            //Now we need an AlertDialog.Builder object
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileSettings.this);

            //setting the view of the builder to our custom view that we already inflated
            builder.setView(dialogView);

            //finally creating the alert dialog and displaying it
            final AlertDialog alertDialog = builder.create();

            Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
            TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
            TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

            dialog_btn.setText("OK");
            success_title.setText("Error");
            success_text.setText("Full Name, User Name, Location and Gender must not be empty!");

            // if the OK button is clicked, close the success dialog
            dialog_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });

            alertDialog.show();
            //...................}
        }else{

            loadingBar.setTitle("Please Wait");
            loadingBar.setMessage("Please wait while your profile details are being saved");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(false);
            upDateAccountInformation( user_full_name, user_name, user_dob, user_location, user_phone_pp, user_interest, user_gender, user_status);
        }


    }

    private void upDateAccountInformation(String user_full_name, String user_name, String user_dob, String user_location, String user_phone_pp, String user_interest, String user_gender, String user_status) {

        Map<String,Object> userMap = new HashMap<String, Object>();
        userMap.put("fullname", user_full_name);
        userMap.put("username", user_name);
        userMap.put("dob", user_dob);
        userMap.put("location", user_location);
        userMap.put("phone", user_phone_pp);
        userMap.put("interestedin", user_interest);
        userMap.put("gender", user_gender);
        userMap.put("profilestatus", user_status);
        saveProfileSettingsRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    loadingBar.dismiss();
                    Toasty.success(getApplicationContext(),"Profile updated successfully!", Toasty.LENGTH_LONG, true).show();
                }else {
                    //{.....................
                    //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                    ViewGroup viewGroup = findViewById(android.R.id.content);

                    //then we will inflate the custom alert dialog xml that we created
                    View dialogView = LayoutInflater.from(ProfileSettings.this).inflate(R.layout.error_dialog, viewGroup, false);


                    //Now we need an AlertDialog.Builder object
                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileSettings.this);

                    //setting the view of the builder to our custom view that we already inflated
                    builder.setView(dialogView);

                    //finally creating the alert dialog and displaying it
                    final AlertDialog alertDialog = builder.create();

                    Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
                    TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
                    TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

                    dialog_btn.setText("OK");
                    success_title.setText("Error");
                    success_text.setText("Your profile was not updated. Try again later.");

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