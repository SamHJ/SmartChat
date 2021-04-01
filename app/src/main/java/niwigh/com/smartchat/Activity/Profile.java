package niwigh.com.smartchat.Activity;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;


public class Profile extends AppCompatActivity {
    Toolbar toolbar;
    String PostToProfileKey;
    CircleImageView userprofile_image;
    TextView full_name, username_text, userprofile_status, user_interested_in, location_in_text, user_phone, user_gender,dob;

    DatabaseReference profileUserRef, FriendsRef, PostsRef,usersRef;
    FirebaseAuth mAuth;
    String currentUserID;
    TextView no_of_user_posts, no_of_user_friends;
    LinearLayout all_my_no_of_posts_layout;
    int countFriends = 0, countPosts = 0;
    ImageButton editProfilebtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.profile_action_bar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profile");

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);


        PostToProfileKey = getIntent().getExtras().get("PostToProfile").toString();
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        profileUserRef.keepSynced(true);
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendsRef.keepSynced(true);
        PostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts");
        PostsRef.keepSynced(true);

        no_of_user_posts = findViewById(R.id.no_of_user_posts);
        no_of_user_friends = findViewById(R.id.no_of_friends);
        all_my_no_of_posts_layout = findViewById(R.id.all_my_no_of_posts_layout);


        userprofile_image = findViewById(R.id.userhome_profile_dialog_image);
        full_name = findViewById(R.id.user_full_name_text);
        username_text = findViewById(R.id.user_name_tex);
        userprofile_status =  findViewById(R.id.profile_status);
        user_interested_in = findViewById(R.id.interested_in_text);
        location_in_text = findViewById(R.id.location_in_text);
        user_phone = findViewById(R.id.phone_in_text);
        user_gender = findViewById(R.id.gender_in_text);
        dob = findViewById(R.id.dob_in_text);
        editProfilebtn = findViewById(R.id.editProfilebtn);

        editProfilebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profile_to_settings = new Intent(Profile.this, ProfileSettings.class);
                Pair[] pairs = new Pair[1];
                pairs[0] = new Pair<View, String>(userprofile_image, "sharedProfileImageTransition");
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Profile.this, pairs);
                startActivity(profile_to_settings, options.toBundle());
            }
        });



        userprofile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent dialog_to_profile = new Intent(Profile.this, FullModeProfileImage.class);
                Pair[] pairs = new Pair[1];
                pairs[0] = new Pair<View, String>(userprofile_image, "sharedProfileImageTransition");

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Profile.this, pairs);
                startActivity(dialog_to_profile, options.toBundle());

            }
        });

        all_my_no_of_posts_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent all_my_post_intent = new Intent(Profile.this, AllMyPosts.class);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(Profile.this);
                startActivity(all_my_post_intent, options.toBundle());
            }
        });

        PostsRef.orderByChild("uid")
                .startAt(currentUserID).endAt(currentUserID + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){

                            countPosts = (int)dataSnapshot.getChildrenCount();
                            no_of_user_posts.setText(Integer.toString(countPosts));
                        }
                        else {

                            no_of_user_posts.setText("0");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        FriendsRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    countFriends = (int) dataSnapshot.getChildrenCount();
                    no_of_user_friends.setText(Integer.toString(countFriends));
                }
                else {
                    no_of_user_friends.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    try{
                        final String profile_image = dataSnapshot.child("profileimage").getValue().toString();
                        String user_fullname = dataSnapshot.child("fullname").getValue().toString();
                        String user_name = dataSnapshot.child("username").getValue().toString();
                        String user_phone_p = dataSnapshot.child("phone").getValue().toString();
                        String interested_in = dataSnapshot.child("interestedin").getValue().toString();
                        String user_gender_name = dataSnapshot.child("gender").getValue().toString();
                        String user_status = dataSnapshot.child("profilestatus").getValue().toString();
                        String user_dob = dataSnapshot.child("dob").getValue().toString();
                        String user_location_name = dataSnapshot.child("location").getValue().toString();
                        location_in_text.setText(user_location_name);

                        try{
                            Picasso.with(Profile.this).load(profile_image).networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.easy_to_use).into(userprofile_image, new
                                    Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {

                                            Picasso.with(Profile.this).load(profile_image).placeholder(R.drawable.easy_to_use).into(userprofile_image);
                                        }
                                    });
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        full_name.setText(user_fullname);
                        username_text.setText("@" + user_name);
                        userprofile_status.setText(user_status);
                        user_interested_in.setText(interested_in);
                        user_phone.setText(user_phone_p);
                        user_gender.setText(user_gender_name);
                        dob.setText(user_dob);

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