package niwigh.com.smartchat.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import niwigh.com.smartchat.Adapter.GroupInfoUserListsAdapter;
import niwigh.com.smartchat.Model.User;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;
import niwigh.com.smartchat.core.ImageCompressTask;
import niwigh.com.smartchat.listeners.IImageCompressTaskListener;


public class GroupInfo extends AppCompatActivity {

    DatabaseReference groupsRef,usersRef;
    AppBarLayout app_bar_layout;
    FirebaseAuth mAuth;
    String currentUserID;
    StorageReference groupImageStorageRef;
    RecyclerView all_users_recyclerview;
    GroupInfoUserListsAdapter adapter;

    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private static final int REQUEST_PICK_PHOTO = 105;

    //create a single thread pool to our image compression class.
    private ExecutorService mExecutorService = Executors.newFixedThreadPool(1);

    String saveCurrentDate, saveCurrentTime, postRandomName;
    private ImageCompressTask imageCompressTask;
    ProgressDialog loadingBar;
    CircleImageView imageView;
    TextView groupDescription,no_of_participants_in_group;
    LinearLayout exit_layout;
    String group_category,group_key;
    String fullName,userImage,profileStatus,userStatus,userName;
    private User user;
    ViewGroup viewGroup;
    ProgressBar usersLoader;
    List<User> usersList;
    FirebaseStorage firebaseStorage;

    String group_Name,group_image;

    LinearLayout delete_group_layout;
    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        group_category = getIntent().getStringExtra("group_cat");
        group_key = getIntent().getStringExtra("group_key");
        group_image = getIntent().getStringExtra("group_image");
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        firebaseStorage = FirebaseStorage.getInstance();

        groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_category)
                .child("groups").child(group_key);
        groupsRef.keepSynced(true);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);

        groupImageStorageRef = FirebaseStorage.getInstance().getReference();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        app_bar_layout = findViewById(R.id.app_bar_layout);
        loadingBar = new ProgressDialog(this);
        usersLoader = findViewById(R.id.usersLoader);

        final ImageView img = new ImageView(this);
        try{
            Picasso.with(img.getContext()).load(group_image).into(img, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    app_bar_layout.setBackgroundDrawable(img.getDrawable());
                }

                @Override
                public void onError() {
                    app_bar_layout.setBackgroundResource(R.drawable.easy_to_use);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

        group_Name = getIntent().getStringExtra("group_name");
        String date_created = getIntent().getStringExtra("date_created");
        String time_created = getIntent().getStringExtra("time_created");
        String group_description = getIntent().getStringExtra("group_description");
        getSupportActionBar().setTitle(group_Name);
        toolbar.setSubtitle("Created on "+date_created+ " at "+time_created);

        groupDescription = findViewById(R.id.group_description);
        no_of_participants_in_group = findViewById(R.id.no_of_participants_in_group);
        exit_layout = findViewById(R.id.exit_layout);
        final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);

        viewGroup = findViewById(android.R.id.content);

        usersList = new ArrayList<>();

        all_users_recyclerview = findViewById(R.id.all_users_recyclerview);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        all_users_recyclerview.setLayoutManager(linearLayoutManager);
        all_users_recyclerview.setItemAnimator(new DefaultItemAnimator());

        delete_group_layout = findViewById(R.id.delete_group_layout);

        groupDescription.setText(group_description);

        exit_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    final AlertDialog alertDialog = new AlertDialog.Builder(GroupInfo.this).create();
                    alertDialog.setMessage("Exit \""+group_Name+"\" group?");
                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "EXIT", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            removeUserFromGroup(currentUserID);
                        }
                    });
                    alertDialog.show();
                }
                catch (WindowManager.BadTokenException e) {
                    //use a log message
                }

            }
        });


        usersRef.child(currentUserID).child("userState").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("usertype")){

                       delete_group_layout.setVisibility(View.VISIBLE);

                    }else{
                        delete_group_layout.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        delete_group_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.support.v7.app.AlertDialog.Builder alertDialogBuilder =
                        new android.support.v7.app.AlertDialog.Builder(GroupInfo.this);
                alertDialogBuilder.setMessage(Html.
                        fromHtml("Are you sure you want to delete this group group? " +
                                "<br>All the messages within this group would be deleted and this process" +
                                " cannot be reversed!"));
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteGroup();
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                final android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();

                alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onShow(DialogInterface arg0) {
                        alertDialog.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE).setTextColor(R.color.colorPrimary);
                        alertDialog.getButton(android.support.v7.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(R.color.colorPrimary);
                    }
                });
                alertDialog.show();
            }
        });


    }

    private void deleteGroup() {

        try {

            final ProgressDialog dialog = new ProgressDialog(GroupInfo.this);
            dialog.setMessage("Deleting group...");
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();


            groupsRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    try {
                        //delete group image
                        StorageReference imageRef = firebaseStorage.getReferenceFromUrl(group_image);
                        imageRef.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    Toasty.success(GroupInfo.this, "Group deleted successfully!",
                            Toasty.LENGTH_SHORT).show();
                    dialog.dismiss();
                    startActivity(new Intent(GroupInfo.this, Groups.class));
                    finish();
                    overridePendingTransition(R.anim.left_in, R.anim.right_out);
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void fetchGroupUsers(final String group_key, final String group_category) {

        try {
            groupsRef.child("users").addValueEventListener(new ValueEventListener() {
                @SuppressLint({"DefaultLocale", "SetTextI18n"})
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChildren()) {
                            int countParticipants = (int) dataSnapshot.getChildrenCount();
                            no_of_participants_in_group.setText(String.format("%d participants", countParticipants));

                            usersList.clear();

                            for (final DataSnapshot snapshots : dataSnapshot.getChildren()) {


                                usersRef.child(snapshots.getKey()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            try {
                                                User user = dataSnapshot.getValue(User.class);
                                                usersList.add(user);


                                                usersLoader.setVisibility(View.GONE);
                                                all_users_recyclerview.setVisibility(View.VISIBLE);
                                                adapter = new GroupInfoUserListsAdapter(GroupInfo.this, removeDuplicates(usersList),
                                                        group_key, group_category, viewGroup);
                                                all_users_recyclerview.setAdapter(adapter);
                                                adapter.notifyDataSetChanged();
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }

                        } else {
                            no_of_participants_in_group.setText("0 participants");
                        }
                    } else {
                        no_of_participants_in_group.setText("0 participants");
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

    public List<User>  removeDuplicates(List<User> list){
        TreeSet set = new TreeSet(new Comparator<User>() {

            @Override
            public int compare(User o1, User o2) {
                if(o1.getId().equalsIgnoreCase(o2.getId())){
                    return 0;
                }
                return 1;
            }
        });
        set.addAll(list);

        return (List) new ArrayList(set);
    }

    private void removeUserFromGroup(final String currentUserID) {

        try {
            final ProgressDialog loadingBar = new ProgressDialog(GroupInfo.this);
            loadingBar.setTitle("Exiting group");
            loadingBar.setMessage("A moment please");
            loadingBar.setCancelable(false);
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            final DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_category)
                    .child("groups").child(group_key).child("users").child(currentUserID);

            final DatabaseReference adminsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_category)
                    .child("groups").child(group_key).child("admins");
            adminsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.getChildrenCount() < 2 && dataSnapshot.hasChild(currentUserID)) {
                            try {
                                //{.....................
                                //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                                ViewGroup viewGroup = findViewById(android.R.id.content);

                                //then we will inflate the custom alert dialog xml that we created
                                View dialogView = LayoutInflater.from(GroupInfo.this).inflate(R.layout.error_dialog, viewGroup, false);


                                //Now we need an AlertDialog.Builder object
                                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfo.this);

                                //setting the view of the builder to our custom view that we already inflated
                                builder.setView(dialogView);

                                //finally creating the alert dialog and displaying it
                                final AlertDialog alertDialog = builder.create();

                                Button dialog_btn = dialogView.findViewById(R.id.buttonError);
                                TextView success_text = dialogView.findViewById(R.id.error_text);
                                TextView success_title = dialogView.findViewById(R.id.error_title);

                                dialog_btn.setText("OK");
                                success_title.setText("Error");
                                success_text.setText("This group requires at least on admin!");

                                // if the OK button is clicked, close the success dialog
                                dialog_btn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        alertDialog.dismiss();
                                    }
                                });

                                alertDialog.show();
                                //...................}
                            } catch (WindowManager.BadTokenException error) {
                                //use a log message
                            }

                            loadingBar.dismiss();

                        } else {
                            groupsRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        loadingBar.dismiss();
                                        Intent groupInfoIntent = new Intent(GroupInfo.this, Groups.class);
                                        startActivity(groupInfoIntent);
                                        overridePendingTransition(R.anim.left_in, R.anim.right_out);
                                        finish();
                                        Toasty.success(GroupInfo.this, "Group exited successfully",
                                                Toasty.LENGTH_LONG, true).show();

                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    try {
                                        //{.....................
                                        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                                        ViewGroup viewGroup = findViewById(android.R.id.content);

                                        //then we will inflate the custom alert dialog xml that we created
                                        View dialogView = LayoutInflater.from(GroupInfo.this).inflate(R.layout.error_dialog, viewGroup, false);


                                        //Now we need an AlertDialog.Builder object
                                        AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfo.this);

                                        //setting the view of the builder to our custom view that we already inflated
                                        builder.setView(dialogView);

                                        //finally creating the alert dialog and displaying it
                                        final AlertDialog alertDialog = builder.create();

                                        Button dialog_btn = dialogView.findViewById(R.id.buttonError);
                                        TextView success_text = dialogView.findViewById(R.id.error_text);
                                        TextView success_title = dialogView.findViewById(R.id.error_title);

                                        dialog_btn.setText("OK");
                                        success_title.setText("Error");
                                        success_text.setText(e.getMessage());

                                        // if the OK button is clicked, close the success dialog
                                        dialog_btn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                alertDialog.dismiss();
                                            }
                                        });

                                        alertDialog.show();
                                        //...................}
                                    } catch (WindowManager.BadTokenException error) {
                                        //use a log message
                                    }

                                    loadingBar.dismiss();
                                }
                            });
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

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_detail_menu, menu);

        return true;
    }


    @SuppressLint("NewApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
                return true;
            case R.id.action_edit_group_info:
                try {
                    final String group_category = getIntent().getStringExtra("group_cat");
                    final String group_key = getIntent().getStringExtra("group_key");
                    mAuth = FirebaseAuth.getInstance();
                    currentUserID = mAuth.getCurrentUser().getUid();

                    final String group_image = getIntent().getStringExtra("group_image");
                    final String group_Name = getIntent().getStringExtra("group_name");
                    final String group_description = getIntent().getStringExtra("group_description");
                    final boolean couldjoin = getIntent().getBooleanExtra("couldjoin", false);

                    DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_category)
                            .child("groups").child(group_key).child("admins");

                    groupsRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                if (dataSnapshot.hasChild(currentUserID)) {

                                    try {
                                        final AlertDialog alertDialog = new AlertDialog.Builder(GroupInfo.this).create();
                                        alertDialog.setMessage("What do you want to do?");
                                        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "GROUP SETTINGS", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent groupInfoIntent = new Intent(GroupInfo.this, GroupSettings.class);
                                                groupInfoIntent.putExtra("group_category", group_category);
                                                groupInfoIntent.putExtra("group_key", group_key);
                                                groupInfoIntent.putExtra("group_Name", group_Name);
                                                startActivity(groupInfoIntent);
                                                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                            }
                                        });


                                        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "EDIT GROUP", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                openGroupDetailsEditDialog(group_Name, group_description, group_image, group_Name,
                                                        couldjoin);
                                            }
                                        });
                                        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "ADD PARTICIPANTS", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                Intent groupInfoIntent = new Intent(GroupInfo.this, AddParticipantsToGroup.class);
                                                groupInfoIntent.putExtra("group_category", group_category);
                                                groupInfoIntent.putExtra("group_key", group_key);
                                                groupInfoIntent.putExtra("group_Name", group_Name);
                                                startActivity(groupInfoIntent);
                                                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                            }
                                        });
                                        alertDialog.show();
                                    } catch (WindowManager.BadTokenException e) {
                                        //use a log message
                                    }


                                } else {
                                    try {
                                        final AlertDialog alertDialog = new AlertDialog.Builder(GroupInfo.this).create();
                                        alertDialog.setMessage("Only admins can edit this group's info");
                                        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                alertDialog.dismiss();
                                            }
                                        });
                                        alertDialog.show();
                                    } catch (WindowManager.BadTokenException e) {
                                        //use a log message
                                    }

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
                return true;
            case R.id.action_download_group_image:
                final String groupImage = getIntent().getStringExtra("group_image");
                final String groupName = getIntent().getStringExtra("group_name");
                File root = Environment.getExternalStorageDirectory();
                root.mkdirs();
                String path = root.toString();

                DownloadManager downloadManager = (DownloadManager) GroupInfo.this.getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri1 = Uri.parse(groupImage);
                DownloadManager.Request request = new DownloadManager.Request(uri1);
                request.setTitle("SmartChat (" + groupName + ".jpg)");
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalFilesDir(GroupInfo.this, path + "/SmartChat" + "/Groups" + "/"+groupName +  "/Images", groupName+".jpg");
                downloadManager.enqueue(request);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }


    @SuppressLint("SetTextI18n")
    public void openGroupDetailsEditDialog(String title, String description, final
    String post_author_image, String edit_post_author_username, final boolean couldjoin) {

        try {

            String group_category = getIntent().getStringExtra("group_cat");
            String group_key = getIntent().getStringExtra("group_key");

            groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_category)
                    .child("groups").child(group_key);

            //before inflating the custom alert dialog layout, we will get the current activity viewgroup
            ViewGroup viewGroup = findViewById(android.R.id.content);

            //then we will inflate the custom alert dialog xml that we created
            View dialogView = LayoutInflater.from(GroupInfo.this).inflate(R.layout.edit_group_dialog, viewGroup, false);


            //Now we need an AlertDialog.Builder object
            AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfo.this);

            //setting the view of the builder to our custom view that we already inflated
            builder.setView(dialogView);

            //finally creating the alert dialog and displaying it
            final AlertDialog alertDialog = builder.create();


            imageView = dialogView.findViewById(R.id.group_image);
            Button cancel_btn = dialogView.findViewById(R.id.cancel_btn);
            Button publish_btn = dialogView.findViewById(R.id.publish_btn);
            final EditText post_title = dialogView.findViewById(R.id.text_input_edit_post_title);
            final EditText post_description = dialogView.findViewById(R.id.text_input_edit_post_description);
            LinearLayout username_and_date_layout_ = dialogView.findViewById(R.id.username_and_date_layout_);
            final TextInputEditText group_could_join = dialogView.findViewById(R.id.group_could_join);


            Spinner spinner = dialogView.findViewById(R.id.spinner);

            ArrayList<Object> spinnerArray = new ArrayList<>();
            spinnerArray.add("true");
            spinnerArray.add("false");
            ArrayAdapter spinnerArrayAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item, spinnerArray);
            spinner.setAdapter(spinnerArrayAdapter);

            if (couldjoin) {
                spinner.setSelection(0);
            } else {
                spinner.setSelection(1);
            }

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        group_could_join.setText("true");
                    } else {
                        group_could_join.setText("false");
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

            username_and_date_layout_.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    requestPermission();
                }
            });

            TextView edit_post_username = dialogView.findViewById(R.id.edit_post_username);

            post_title.setText(title);
            post_description.setText(description);
            edit_post_username.setText(edit_post_author_username);

            Picasso.with(GroupInfo.this).load(post_author_image).networkPolicy(NetworkPolicy.OFFLINE)
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            Picasso.with(GroupInfo.this).load(post_author_image).into(imageView);
                        }
                    });

            publish_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    groupsRef.child("groupname").setValue(post_title.getText().toString());
                    groupsRef.child("groupdescription").setValue(post_description.getText().toString());
                    if (group_could_join.getText().toString().trim().equals("true")) {
                        groupsRef.child("couldjoin").setValue(true);
                    } else {
                        groupsRef.child("couldjoin").setValue(false);
                    }

                    Toasty.success(GroupInfo.this, "Group updated successfully!", Toasty.LENGTH_LONG, true).show();
                    alertDialog.dismiss();
                }
            });

            // if the cancel button is clicked, close the success dialog
            cancel_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });

            alertDialog.show();

        }catch (Exception e){
            e.printStackTrace();
        }
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
                selectGroupImageFromGallery();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void selectGroupImageFromGallery() {
        Intent gallery_intent = new Intent();
        gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
        gallery_intent.setType("image/*");
        startActivityForResult(gallery_intent, REQUEST_PICK_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_PICK_PHOTO && resultCode == RESULT_OK &&
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
                Uri compressedImageUri = Uri.fromFile(file);


                Log.d("ImageCompressor", "New photo size ==> " + file.length()); //log new file size.

                imageView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));


                //for date
                Calendar calFordDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                saveCurrentDate = currentDate.format(calFordDate.getTime());
                //for time
                Calendar calFordTime = Calendar.getInstance();
                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
                saveCurrentTime = currentTime.format(calFordTime.getTime());

                postRandomName = saveCurrentDate + saveCurrentTime;

                String group_category = getIntent().getStringExtra("group_cat");
                String group_key = getIntent().getStringExtra("group_key");

                final DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_category)
                        .child("groups").child(group_key);

                final StorageReference filePath = groupImageStorageRef.child("Group Images").child(postRandomName + ".jpg");

                filePath.putFile(compressedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {

                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downUrl = uri;
                                    final String fileUrl = downUrl.toString();

                                    groupsRef.child("groupimage").setValue(fileUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                final ImageView img = new ImageView(GroupInfo.this);
                                                Picasso.with(img.getContext()).load(fileUrl).into(img, new com.squareup.picasso.Callback() {
                                                    @Override
                                                    public void onSuccess() {
                                                        app_bar_layout.setBackgroundDrawable(img.getDrawable());
                                                    }

                                                    @Override
                                                    public void onError() {
                                                        app_bar_layout.setBackgroundResource(R.drawable.easy_to_use);
                                                    }
                                                });

                                                Toasty.success(GroupInfo.this, "Group image updated successfully!",
                                                        Toasty.LENGTH_LONG, true).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //{.....................
                                            //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                                            ViewGroup viewGroup = findViewById(android.R.id.content);

                                            //then we will inflate the custom alert dialog xml that we created
                                            View dialogView = LayoutInflater.from(GroupInfo.this).inflate(R.layout.error_dialog, viewGroup, false);


                                            //Now we need an AlertDialog.Builder object
                                            AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfo.this);

                                            //setting the view of the builder to our custom view that we already inflated
                                            builder.setView(dialogView);

                                            //finally creating the alert dialog and displaying it
                                            final AlertDialog alertDialog = builder.create();

                                            Button dialog_btn = dialogView.findViewById(R.id.buttonError);
                                            TextView success_text = dialogView.findViewById(R.id.error_text);
                                            TextView success_title = dialogView.findViewById(R.id.error_title);

                                            dialog_btn.setText("OK");
                                            success_title.setText("Error");
                                            success_text.setText(e.getMessage());

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
                                    });


                                }
                            });

                        } else {
                            String message = task.getException().getMessage();

                            //{.....................
                            //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                            ViewGroup viewGroup = findViewById(android.R.id.content);

                            //then we will inflate the custom alert dialog xml that we created
                            View dialogView = LayoutInflater.from(GroupInfo.this).inflate(R.layout.error_dialog, viewGroup, false);


                            //Now we need an AlertDialog.Builder object
                            AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfo.this);

                            //setting the view of the builder to our custom view that we already inflated
                            builder.setView(dialogView);

                            //finally creating the alert dialog and displaying it
                            final AlertDialog alertDialog = builder.create();

                            Button dialog_btn = dialogView.findViewById(R.id.buttonError);
                            TextView success_text = dialogView.findViewById(R.id.error_text);
                            TextView success_title = dialogView.findViewById(R.id.error_title);

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
                        loadingBar.setTitle("Updating Group Image");
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

        @Override
        public void onError(Throwable error) {
            //very unlikely, but it might happen on a device with extremely low storage.
            //log it, log.WhatTheFuck?, or show a dialog asking the user to delete some files....etc, etc
            Log.wtf("ImageCompressor", "Error occurred", error);
        }
    };


    @Override
    public void onStart() {
        super.onStart();
        Utilities.getInstance(this).updateUserStatus("Online");
        fetchGroupUsers(group_key,group_category);
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
    protected void onResume() {
        super.onResume();
        Utilities.getInstance(this).updateUserStatus("Online");

        fetchGroupUsers(group_key,group_category);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utilities.getInstance(this).updateUserStatus("Online");
        //clean up!
        mExecutorService.shutdown();

        mExecutorService = null;
        imageCompressTask = null;
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
        finish();
    }

}