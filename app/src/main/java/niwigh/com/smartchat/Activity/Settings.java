package niwigh.com.smartchat.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
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


public class Settings extends AppCompatActivity {
    Toolbar toolbar;
    TextView profile_settings, settings_username, settings_status;
    CircleImageView settins_user_profile_image;
    FirebaseAuth mAuth;
    String currentUserID;
    DatabaseReference UserRef;
    LinearLayout mLayout;
    LinearLayout shareLayout,data_layout;
    LinearLayout MessagesLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UserRef.keepSynced(true);


        toolbar = findViewById(R.id.settings_action_bar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Settings");


        //init views
        shareLayout = findViewById(R.id.extras_share);
        mLayout = findViewById(R.id.username_and_date_layout);
        profile_settings = findViewById(R.id.settings_profile);
        settings_username = findViewById(R.id.settings_profile_username);
        settings_status = findViewById(R.id.settings_text_profile_status);
        settins_user_profile_image = findViewById(R.id.settins_userprofile_image);
        MessagesLayout = findViewById(R.id.messages_layout);
        data_layout = findViewById(R.id.data_layout);

        MessagesLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent messagesLayoutIntent = new Intent(Settings.this, MessagingAreaBackground.class);
                startActivity(messagesLayoutIntent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });

        data_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent messagesLayoutIntent = new Intent(Settings.this, NotificationsSettings.class);
                startActivity(messagesLayoutIntent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });

        shareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String ShareBody = "Hey, check out this cool social media app called SmartChat :" +
                        "https://play.google.com/store/apps/details?id=niwigh.com.smartchat";
                String Sharetitle = "Hot Social Messaging App!";
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, Sharetitle);
                shareIntent.putExtra(Intent.EXTRA_TEXT, ShareBody);
                startActivity(Intent.createChooser(shareIntent, "Share SmartChat  using"));

            }
        });

        mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String PostToProfileKey = "settingstoprofile";
                Intent settings_to_profile = new Intent(Settings.this, Profile.class);
                settings_to_profile.putExtra("PostToProfile", PostToProfileKey);
                startActivity(settings_to_profile);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });

        profile_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileSettingsIntent = new Intent(Settings.this, ProfileSettings.class);
                startActivity(profileSettingsIntent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });


        UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("profileimage")){
                        final String profile_image = dataSnapshot.child("profileimage").getValue().toString();
                        try {
                            Picasso.with(Settings.this).load(profile_image)
                                    .networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.easy_to_use).into(settins_user_profile_image, new
                                    Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {

                                            Picasso.with(Settings.this).load(profile_image).placeholder(R.drawable.easy_to_use).into(settins_user_profile_image);

                                        }
                                    });
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                    if(dataSnapshot.hasChild("username")){
                        String user_name = dataSnapshot.child("username").getValue().toString();
                        settings_username.setText(user_name);

                    }

                    if(dataSnapshot.hasChild("profilestatus")){
                        String user_status = dataSnapshot.child("profilestatus").getValue().toString();
                        settings_status.setText(user_status);
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

    public void sendUserToHomeActivity() {
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendUserToHomeActivity();
    }
}