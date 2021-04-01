package niwigh.com.smartchat.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;
import android.widget.TextView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import niwigh.com.smartchat.Fragment.RequestsFragment;
import niwigh.com.smartchat.Util.AppRater;
import niwigh.com.smartchat.Fragment.FeedsFragment;
import niwigh.com.smartchat.Fragment.FriendsFragment;
import niwigh.com.smartchat.Fragment.MessagesFragment;
import niwigh.com.smartchat.Model.MessagesModel;
import niwigh.com.smartchat.Fragment.PostsFragment;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;


public class UserHome extends AppCompatActivity {

    private BottomNavigationView mMainNav;
    FrameLayout mMainFrame;
    FeedsFragment feedsFragment;
    PostsFragment postsFragment;
    MessagesFragment messagesFragment;
    FriendsFragment friendsFragment;
    RequestsFragment requestsFragment;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    Toolbar mToolbar;
    FirebaseAuth mAuth;
    DatabaseReference UserRef,rootRef;
    CircleImageView NavDrawerProfileImage;
    TextView NavDrawerProfileUsername;
    String currentUserID;
    TextView no_of_unread_messages;
    FirebaseFirestore firebaseFirestore;

    private final static String APP_TITLE = "SmartChat";// App Name
    private final static String APP_PNAME = "havotechstudios.com.smartmessenger";// Package Name




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UserRef.keepSynced(true);
        rootRef = FirebaseDatabase.getInstance().getReference();

        mToolbar = findViewById(R.id.userhome_navigation_opener_include);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("SmartChat");

        mMainFrame = findViewById(R.id.main_frame);
        mMainNav = findViewById(R.id.main_nav);
        firebaseFirestore = FirebaseFirestore.getInstance();


        BottomNavigationMenuView bottomNavigationMenuView
                = (BottomNavigationMenuView) mMainNav.getChildAt(0);
        View view = bottomNavigationMenuView.getChildAt(2);
        BottomNavigationItemView itemView = (BottomNavigationItemView) view;
        final View badge = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.notification_bagde_layout, itemView, true);
        no_of_unread_messages = badge.findViewById(R.id.notifications_badge);
        no_of_unread_messages.setVisibility(View.GONE);


        String requestsFragmentIntent = getIntent().getStringExtra("requestsFragment");
        feedsFragment = new FeedsFragment();
        postsFragment = new PostsFragment();
        messagesFragment = new MessagesFragment();
        friendsFragment = new FriendsFragment();
        requestsFragment = new RequestsFragment();

        if(requestsFragmentIntent != null){
            setFragment(requestsFragment);
            mMainNav.setSelectedItemId(R.id.nav_requests);
        }else {
            setFragment(feedsFragment);
        }



        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(UserHome.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.navigation_drawer_nav_view);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_drawer_header);
        NavDrawerProfileImage = navView.findViewById(R.id.navigation_drawer_user_profile_logo);
        NavDrawerProfileUsername = navView.findViewById(R.id.navigation_drawer_user_name);

        NavDrawerProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dialog_to_profile = new Intent(UserHome.this, Profile.class);
                final  String PostToProfileKey = "userhomeprofiledialogtoprofileactivity";
                dialog_to_profile.putExtra("PostToProfile", PostToProfileKey);
                startActivity(dialog_to_profile);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });

        UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    if(dataSnapshot.hasChild("fullname")){
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        NavDrawerProfileUsername.setText(fullname);

                    }
                    if(dataSnapshot.hasChild("profileimage")){
                       try{
                           final String profileimage = dataSnapshot.child("profileimage").getValue().toString();
                           Picasso.with(UserHome.this).load(profileimage)
                                   .networkPolicy(NetworkPolicy.OFFLINE)
                                   .placeholder(R.drawable.easy_to_use).into(NavDrawerProfileImage, new
                                   Callback() {
                                       @Override
                                       public void onSuccess() {

                                       }

                                       @Override
                                       public void onError() {

                                           Picasso.with(UserHome.this).load(profileimage).placeholder(R.drawable.easy_to_use).into(NavDrawerProfileImage);

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


        //bottom navigation on selected item listener
        mMainNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                                                         @Override
                                                         public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                                                             switch (menuItem.getItemId()) {

                                                                 case R.id.nav_feeds :
                                                                     setFragment(feedsFragment); //loads the feeds Fragment
                                                                     return true;

                                                                 case R.id.nav_posts :
                                                                     setFragment(postsFragment); //loads the posts Fragment
                                                                     return true;

                                                                 case R.id.nav_messages :
                                                                     setFragment(messagesFragment); //loads the messages Fragment
                                                                     return true;

                                                                 case R.id.nav_friends :
                                                                     setFragment(friendsFragment); //loads the friends Fragment
                                                                     return true;

                                                                 case R.id.nav_requests :
                                                                     setFragment(requestsFragment); //loads the requests Fragment
                                                                     return true;

                                                                 default:
                                                                     return false;
                                                             }
                                                         }


                                                     }
        );

        //navigation view on selected item listener
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                userMenuSelector(menuItem);
                return false;
            }
        });

        final Menu menu = navigationView.getMenu();

        UserRef.child(currentUserID).child("userState").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("usertype")){

                    menu.findItem(R.id.nav_drawer_feeds_add).setVisible(true);
                    menu.findItem(R.id.nav_drawer_group_add).setVisible(true);

                    }else{
                        menu.findItem(R.id.nav_drawer_feeds_add).setVisible(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    @Override
    protected void onStart() {
        super.onStart();
        AppRater.app_launched(this);

        Utilities.getInstance(this).updateUserStatus("Online");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            //send user to login activity
            sendUserToLoginActivity();
        }
        else {

            checkUserExistence();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Utilities.getInstance(this).updateUserStatus("Online");
        checkformessages();
    }

    @Override
    protected void onPause() {
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

    public void checkformessages(){

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("Messages")){
                    final DatabaseReference currentuserFriend = FirebaseDatabase.getInstance().getReference().child("Messages");

                    currentuserFriend.child(currentUserID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(dataSnapshot.exists()){

                                int unread = 0;
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    final String key_one = snapshot.getKey();
                                    for(DataSnapshot snapshot1 : dataSnapshot.child(key_one).getChildren()) {
                                        MessagesModel messagesModel = snapshot1.getValue(MessagesModel.class);
                                        String to = messagesModel.getTo();
                                        boolean isseen = messagesModel.isIsseen();
                                        if(to.equals(currentUserID) && !isseen){
                                            unread++;

                                            if(unread == 0){

                                                no_of_unread_messages.setVisibility(View.GONE);

                                            }else {

                                                no_of_unread_messages.setText("new");
                                                no_of_unread_messages.setVisibility(View.VISIBLE);
                                            }
                                        }else{
                                            no_of_unread_messages.setVisibility(View.GONE);
                                        }

                                    }


                                }

                            }else{
                                no_of_unread_messages.setVisibility(View.GONE);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserExistence() {
        final String current_user_id = mAuth.getCurrentUser().getUid();
        UserRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if( (!dataSnapshot.hasChild("fullname")) || (!dataSnapshot.hasChild("username")) ||
                            (!dataSnapshot.hasChild("location")) ){
                        sendUserToSetupActivity();
                    }
                }else{
                    sendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToSetupActivity() {
        Intent send_to_setup_activity  = new Intent(UserHome.this, RegisterSetup.class);
        startActivity(send_to_setup_activity);
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
        finish();
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(getApplicationContext(),Login.class);
        startActivity(loginIntent);
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void userMenuSelector(MenuItem menuItem) {
        switch (menuItem.getItemId()){

            case R.id.nav_drawer_profile:
                // start intent to profile activity
                Intent dialog_to_profile = new Intent(UserHome.this, Profile.class);
                final  String PostToProfileKey = "userhomeprofiledialogtoprofileactivity";
                dialog_to_profile.putExtra("PostToProfile", PostToProfileKey);
                startActivity(dialog_to_profile);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;

            case R.id.nav_drawer_my_posts:
                //start the my posts intent
                Intent allMyPostsIntent = new Intent(UserHome.this, AllMyPosts.class);
                startActivity(allMyPostsIntent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;

            case R.id.nav_drawer_group:
                Intent allGroupsIntent = new Intent(UserHome.this, Groups.class);
                startActivity(allGroupsIntent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;

            case R.id.nav_drawer_feeds_add:
                Intent addFeedsIntent = new Intent(UserHome.this, AddFeeds.class);
                startActivity(addFeedsIntent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;

            case R.id.nav_drawer_group_add:
                Intent addGroupsIntent = new Intent(UserHome.this, AddNewGroup.class);
                startActivity(addGroupsIntent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;

            case R.id.nav_drawer_settings:
                //start the settings intent
                Intent settingsIntent = new Intent(getApplicationContext(), Settings.class);
                startActivity(settingsIntent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                break;

            case R.id.nav_drawer_logout:
                Map<String, Object> tokenMap = new HashMap<>();
                tokenMap.put("token_id", FieldValue.delete());
                firebaseFirestore.collection("Users").document(currentUserID).update(tokenMap);
                Utilities.getInstance(this).updateUserStatus("Offline");
                mAuth.signOut();
                sendUserToLoginActivity();

                break;

            case R.id.nav_drawer_rate:
                showRateDialog(this);
                break;
        }
    }

    public static void showRateDialog(final Context mContext) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
        builder.setTitle("Rate " + APP_TITLE);
        builder.setMessage("If you enjoy using " + APP_TITLE + ", please take a moment to rate it. Thanks for your support!");

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setIcon(R.mipmap.ic_launcher).setPositiveButton("RATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    public void setFragment(Fragment fragment) {
        checkformessages();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}