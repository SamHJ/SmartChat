package niwigh.com.smartchat.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import niwigh.com.smartchat.Model.FindFriendsModel;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;
import id.zelory.compressor.Compressor;

public class AddNewGroup extends AppCompatActivity {

    Toolbar toolbar;
    String group_name,collection_name;
    boolean could_join;
    RecyclerView all_friends_to_add_recyclerview;
    DatabaseReference usersRef,groupsRef;
    FirebaseAuth mAuth;
    String currentUserID;
    ProgressDialog loadingBar;
    String saveCurrentDate, saveCurrentTime;
    LinearLayout info_layout;


    CircleImageView collection_image_select;
    TextInputEditText collection_name_input,group_name_input,group_could_join;
    Button continue_btn;
    private Bitmap compressed_bitmap;
    private byte[] compressed_byte;
    private static final int REQUEST_STORAGE_PERMISSION = 1003;
    private StorageReference groupsImageRef;
    FloatingActionButton add_friends_fab;
    Map<String,Object> usersMap;
    Spinner spinner;
    private ArrayList<Object> spinnerArray;
    private ArrayAdapter spinnerArrayAdapter;
    Utilities utilities;
    View currentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_group);

        currentView = getWindow().getDecorView().getRootView();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);
        groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        groupsRef.keepSynced(true);

        utilities = Utilities.getInstance(this);

        groupsImageRef = FirebaseStorage.getInstance().getReference().child("Group Images");

        toolbar = findViewById(R.id.add_friends_in_group_appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(group_name);
        getSupportActionBar().setSubtitle("Create New Group");
        loadingBar = new ProgressDialog(this);

        info_layout = findViewById(R.id.info_layout);

        all_friends_to_add_recyclerview = findViewById(R.id.all_friends_to_add_recyclerview);

        all_friends_to_add_recyclerview.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AddNewGroup.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        all_friends_to_add_recyclerview.setLayoutManager(linearLayoutManager);


        collection_image_select = findViewById(R.id.collection_image);
        collection_name_input = findViewById(R.id.collection_name);
        group_name_input = findViewById(R.id.group_name);
        continue_btn = findViewById(R.id.continue_btn);
        add_friends_fab = findViewById(R.id.add_friends_fab);

        spinner = findViewById(R.id.spinner);
        group_could_join = findViewById(R.id.group_could_join);

        spinnerArray = new ArrayList<>();
        spinnerArray.add("true");
        spinnerArray.add("false");
        spinnerArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    group_could_join.setText("true");
                }else {
                    group_could_join.setText("false");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        collection_image_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });


        continue_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                collection_name = collection_name_input.getText().toString().trim();
                group_name = group_name_input.getText().toString().trim();
                could_join = group_could_join.equals("true");

                if(compressed_byte != null){
                    if(collection_name.isEmpty() || group_name.isEmpty()){
                        showCustomErrorDialog("OK","Error","All fields are required!");
                    }else {
                        uploadCollectionImage(collection_name);
                    }
                }else{
                    showCustomErrorDialog("OK","Error","Select collection image first!");
                }
            }
        });

        DisplayAllFriends();

    }

    private void showCustomErrorDialog(String button_text, String error_title, String message) {
        //{.....................
        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(AddNewGroup.this).inflate(R.layout.error_dialog, viewGroup, false);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(AddNewGroup.this);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);

        //finally creating the alert dialog and displaying it
        final AlertDialog alertDialog = builder.create();

        Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
        TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
        TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

        dialog_btn.setText(button_text);
        success_title.setText(error_title);
        success_text.setText(Html.fromHtml(message));

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
                    //Yeah! I want both block to do the same thing, you can write your own logic, but this works for me.
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_STORAGE_PERMISSION);
                }
            } else {
                //Permission Granted, lets go pick photo
                startImageSelect();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void startImageSelect() {
        try {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(AddNewGroup.this);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if (resultCode == RESULT_OK) {
                    try {

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
                        collection_image_select.setImageBitmap(compressed_bitmap);

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    @SuppressLint("SimpleDateFormat")
    private void uploadCollectionImage(final String collection_name){
        try {
            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("dd-MMMM-yyyy HH:mm");
            saveCurrentTime = currentTime.format(calFordTime.getTime());

            final StorageReference filePath = groupsImageRef.child(saveCurrentTime + "-collection-image.jpg");
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

                                groupsRef.child(collection_name)
                                        .updateChildren(groupCollectionMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    getSupportActionBar().setTitle(group_name);
                                                    getSupportActionBar().setSubtitle("Add participants");
                                                    info_layout.setVisibility(View.GONE);
                                                    all_friends_to_add_recyclerview.setVisibility(View.VISIBLE);
                                                    loadingBar.dismiss();
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
                        View dialogView = LayoutInflater.from(AddNewGroup.this).inflate(R.layout.error_dialog, viewGroup, false);


                        //Now we need an AlertDialog.Builder object
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddNewGroup.this);

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

        }catch(Exception e){
            e.printStackTrace();
        }
    }


    @SuppressLint({"SetTextI18n","SimpleDateFormat"})
    private void createNewGroup(Map<String, Object> usersMap) {

        try {
            //add admin to the group
            usersMap.put(currentUserID, currentUserID);

            loadingBar.setTitle("Creating " + group_name + " group");
            loadingBar.setMessage("a moment please...");
            loadingBar.show();
            //for date
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currentDate.format(calFordDate.getTime());
            //for time
            Calendar calFordTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            saveCurrentTime = currentTime.format(calFordTime.getTime());
            Map<String, Object> adminMap = new HashMap<String, Object>();
            adminMap.put(currentUserID, currentUserID);

            Map<String, Object> group_details_map = new HashMap<>();
            group_details_map.put("groupname", group_name);
            group_details_map.put("users", usersMap);
            group_details_map.put("couldjoin", could_join);
            group_details_map.put("datecreated", saveCurrentDate);
            group_details_map.put("timecreated", saveCurrentTime);
            group_details_map.put("admins", adminMap);
            group_details_map.put("groupdescription", "");

            groupsRef.child(collection_name).child("groups").child(group_name)
                    .setValue(group_details_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {
                        loadingBar.dismiss();
                        Toasty.success(AddNewGroup.this,
                                group_name + " group created successfully", Toasty.LENGTH_SHORT).show();
                        finish();
                        overridePendingTransition(R.anim.left_in, R.anim.right_out);
                    }
                }
            });

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void DisplayAllFriends() {

        try {
            final int[] count = {0};

            usersMap = new HashMap<>();

            FirebaseRecyclerAdapter<FindFriendsModel, FriendsViewHolder> firebaseRecyclerAdapter = new
                    FirebaseRecyclerAdapter<FindFriendsModel, FriendsViewHolder>
                            (
                                    FindFriendsModel.class,
                                    R.layout.all_users_display_layout,
                                    FriendsViewHolder.class,
                                    usersRef.orderByChild("fullname")
                            ) {
                        @Override
                        protected void populateViewHolder(final FriendsViewHolder viewHolder,
                                                          FindFriendsModel model, final int position) {

                            viewHolder.setStatus(model.getStatus());
                            viewHolder.setUsername(model.getUsername());

                            final String usersIDs = getRef(position).getKey();
                            usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.exists()) {
                                        try {
                                            final String type;
                                            final String fullName = dataSnapshot.child("fullname").getValue() != null ? dataSnapshot.child("fullname").getValue().toString() : null;
                                            final String userName = dataSnapshot.child("username").getValue() != null ? dataSnapshot.child("username").getValue().toString() : null;
                                            final String userProfileImage = dataSnapshot.child("profileimage").getValue() != null ? dataSnapshot.child("profileimage").getValue().toString() : null;
                                            final String userStatus = dataSnapshot.child("profilestatus").getValue() != null ? dataSnapshot.child("profilestatus").getValue().toString() : null;
                                            final String userId = dataSnapshot.child("id").getValue() != null ? dataSnapshot.child("id").getValue().toString() : null;


                                            if (dataSnapshot.hasChild("userState")) {
                                                type = dataSnapshot.child("userState").child("type").getValue().toString();
                                                if (type.equals("Online")) {
                                                    viewHolder.onlineStatusImage.setVisibility(View.VISIBLE);
                                                    viewHolder.offlineStatusImage.setVisibility(View.GONE);
                                                } else {
                                                    viewHolder.offlineStatusImage.setVisibility(View.VISIBLE);
                                                    viewHolder.onlineStatusImage.setVisibility(View.GONE);
                                                }

                                            }


                                            viewHolder.setFullname(fullName);
                                            viewHolder.setUsername(userName);
                                            viewHolder.setStatus(userStatus);
                                            viewHolder.setProfileimage(AddNewGroup.this, userProfileImage);


                                            viewHolder.selected_friend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                @SuppressLint("RestrictedApi")
                                                @Override
                                                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                                                    try {
                                                        if (compoundButton.isChecked()) {
                                                            usersMap.put(userId, userId);
                                                            if (usersMap.isEmpty()) {
                                                                add_friends_fab.setVisibility(View.GONE);
                                                                count[0] = 0;
                                                            } else {
                                                                add_friends_fab.setVisibility(View.VISIBLE);
                                                                count[0] = count[0] + 1;
                                                            }
                                                        } else {
                                                            if (usersMap.containsKey(userId)) {
                                                                usersMap.remove(userId);
                                                                if (usersMap.isEmpty()) {
                                                                    add_friends_fab.setVisibility(View.GONE);
                                                                    count[0] = 0;
                                                                } else {
                                                                    add_friends_fab.setVisibility(View.VISIBLE);
                                                                    count[0] = count[0] - 1;
                                                                }
                                                            }
                                                        }

                                                        getSupportActionBar().setSubtitle("Add Participants" + " (" + count[0] + ")");

                                                        add_friends_fab.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View view) {
                                                                if (usersMap.isEmpty()) {
                                                                    utilities.customErrorDialog(currentView,
                                                                            "OK", "Error",
                                                                            "Select atleast one participant for this group!");
                                                                } else {
                                                                    createNewGroup(usersMap);
                                                                }
                                                            }
                                                        });

                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }

                                                }
                                            });
                                        }catch(Exception e){
                                            e.printStackTrace();
                                        }
                                        }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }

                        @Override
                        public int getItemCount() {
                            return super.getItemCount();
                        }
                    };

            all_friends_to_add_recyclerview.setAdapter(firebaseRecyclerAdapter);
            firebaseRecyclerAdapter.notifyDataSetChanged();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void FirebaseFriends(String searchText){
        try {
            final int[] count = {0};

            usersMap = new HashMap<>();

            Query firebaseSearchQuery = usersRef.orderByChild("fullname").startAt(searchText).endAt(searchText + "\uf0ff");

            FirebaseRecyclerAdapter<FindFriendsModel, FriendsViewHolder> firebaseRecyclerAdapter = new
                    FirebaseRecyclerAdapter<FindFriendsModel, FriendsViewHolder>
                            (
                                    FindFriendsModel.class,
                                    R.layout.all_users_display_layout,
                                    FriendsViewHolder.class,
                                    firebaseSearchQuery
                            ) {
                        @Override
                        protected void populateViewHolder(final FriendsViewHolder viewHolder, FindFriendsModel model, int position) {

                            try {
                                viewHolder.setStatus(model.getStatus());
                                viewHolder.setUsername(model.getUsername());

                                final String usersIDs = getRef(position).getKey();
                                usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.exists()) {
                                            try {
                                                final String type;
                                                final String fullName = dataSnapshot.child("fullname").getValue() != null ? dataSnapshot.child("fullname").getValue().toString() : null;
                                                final String userName = dataSnapshot.child("username").getValue() != null ? dataSnapshot.child("username").getValue().toString() : null;
                                                final String userProfileImage = dataSnapshot.child("profileimage").getValue() != null ? dataSnapshot.child("profileimage").getValue().toString() : null;
                                                final String userStatus = dataSnapshot.child("profilestatus").getValue() != null ? dataSnapshot.child("profilestatus").getValue().toString() : null;
                                                final String userId = dataSnapshot.child("id").getValue() != null ? dataSnapshot.child("id").getValue().toString() : null;


                                                if (dataSnapshot.hasChild("userState")) {
                                                    type = dataSnapshot.child("userState").child("type").getValue().toString();
                                                    if (type.equals("Online")) {
                                                        viewHolder.onlineStatusImage.setVisibility(View.VISIBLE);
                                                        viewHolder.offlineStatusImage.setVisibility(View.GONE);
                                                    } else {
                                                        viewHolder.offlineStatusImage.setVisibility(View.VISIBLE);
                                                        viewHolder.onlineStatusImage.setVisibility(View.GONE);
                                                    }

                                                }


                                                viewHolder.setFullname(fullName);
                                                viewHolder.setUsername(userName);
                                                viewHolder.setStatus(userStatus);
                                                viewHolder.setProfileimage(AddNewGroup.this, userProfileImage);

                                                viewHolder.selected_friend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                    @SuppressLint("RestrictedApi")
                                                    @Override
                                                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                                                        try {
                                                            if (compoundButton.isChecked()) {
                                                                usersMap.put(userId, userId);
                                                                if (usersMap.isEmpty()) {
                                                                    add_friends_fab.setVisibility(View.GONE);
                                                                    count[0] = 0;
                                                                } else {
                                                                    add_friends_fab.setVisibility(View.VISIBLE);
                                                                    count[0] = count[0] + 1;
                                                                }
                                                            } else {
                                                                if (usersMap.containsKey(userId)) {
                                                                    usersMap.remove(userId);
                                                                    if (usersMap.isEmpty()) {
                                                                        add_friends_fab.setVisibility(View.GONE);
                                                                        count[0] = 0;
                                                                    } else {
                                                                        add_friends_fab.setVisibility(View.VISIBLE);
                                                                        count[0] = count[0] - 1;
                                                                    }
                                                                }
                                                            }
                                                            getSupportActionBar().setSubtitle("Add Participants" + " (" + count[0] + ")");

                                                            add_friends_fab.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View view) {
                                                                    if (usersMap.isEmpty()) {
                                                                        utilities.customErrorDialog(currentView,
                                                                                "OK", "Error",
                                                                                "Select atleast one participant for this group!");
                                                                    } else {
                                                                        createNewGroup(usersMap);
                                                                    }
                                                                }
                                                            });

                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }

                                                    }
                                                });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    };

            all_friends_to_add_recyclerview.setAdapter(firebaseRecyclerAdapter);
            firebaseRecyclerAdapter.notifyDataSetChanged();

        }catch(Exception e){
        e.printStackTrace();
    }
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        ImageView onlineStatusImage, offlineStatusImage;
        final CircleImageView friendsprofileimage;
        CheckBox selected_friend;
        View mView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            onlineStatusImage = itemView.findViewById(R.id.user_online_icon);
            offlineStatusImage = itemView.findViewById(R.id.user_offline_icon);
            onlineStatusImage.setVisibility(View.INVISIBLE);
            offlineStatusImage.setVisibility(View.INVISIBLE);
            friendsprofileimage = mView.findViewById(R.id.search_all_users_profile_image);
            selected_friend = mView.findViewById(R.id.selected_friend);
            selected_friend.setVisibility(View.VISIBLE);
        }

        public void setProfileimage(final Context ctx, final String profileimage){

          try{
              Picasso.with(ctx).load(profileimage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.easy_to_use)
                      .into(friendsprofileimage, new Callback() {
                          @Override
                          public void onSuccess() {

                          }

                          @Override
                          public void onError() {
                              try {
                                  Picasso.with(ctx).load(profileimage).placeholder(R.drawable.easy_to_use).into(friendsprofileimage);
                              }catch(Exception e){
                                  e.printStackTrace();
                              }
                          }
                      });
          }catch (Exception e){
              e.printStackTrace();
          }
        }

        public void setFullname(String fullname) {
            TextView friendsfullname = mView.findViewById(R.id.search_all_users_profile_name);
            friendsfullname.setText(fullname);
        }

        public void setUsername(String username) {
            TextView friends_name = mView.findViewById(R.id.search_all_users_profile_username);
            friends_name.setText("@" + username);
        }

        public void setStatus(String status) {
            TextView friends_date = mView.findViewById(R.id.search_all_users_profile_school);
            friends_date.setText(status);
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_view_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView)MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                FirebaseFriends(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                FirebaseFriends(s);
                return false;
            }
        });

        return true;
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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
        finish();
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

}