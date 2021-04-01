package niwigh.com.smartchat.Activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import niwigh.com.smartchat.Adapter.AllPostsAdapter;
import niwigh.com.smartchat.Model.PostsModel;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;


public class AllMyPosts extends AppCompatActivity {

    Toolbar mToolbar;
    RecyclerView all_users_posts_recyclerview;
    FirebaseAuth mAuth;
    DatabaseReference allPostsRef, LikesRef,usersRef;
    String currentUserID;
    Boolean LikeChecker = false;
    com.github.clans.fab.FloatingActionButton AddNewPostFab, AddNewVideoPost;
    LinearLayoutManager linearLayoutManager;
    LinearLayout no_posts_layout;
    List<PostsModel> postsModelList;
    AllPostsAdapter adapter;
    TextView nothing_found_textview;
    ProgressBar postsLoader;
    Query query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_my_posts);

        mToolbar = findViewById(R.id.all_my_posts_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        allPostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts");
        allPostsRef.keepSynced(true);
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        LikesRef.keepSynced(true);

        query = allPostsRef.orderByChild("uid")
                .equalTo(currentUserID);


        AddNewPostFab = findViewById(R.id.add_post_fab);
        AddNewPostFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddNewPostActivity();
            }
        });
        AddNewVideoPost = findViewById(R.id.add_video_post_fab);
        AddNewVideoPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddNewVideoPostActivity();
            }
        });

        all_users_posts_recyclerview = findViewById(R.id.all_users_posts_recyclerview);
        all_users_posts_recyclerview.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        all_users_posts_recyclerview.setLayoutManager(linearLayoutManager);

        nothing_found_textview = findViewById(R.id.nothing_found_textview);


        no_posts_layout = findViewById(R.id.no_posts_layout);
        postsLoader = findViewById(R.id.postsLoader);
        postsModelList = new ArrayList<>();

        DisplayAllMyPosts();
    }

    private void DisplayAllMyPosts() {

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    if (dataSnapshot.hasChildren()) {

                        postsModelList.clear();

                        for (final DataSnapshot snapshots : dataSnapshot.getChildren()){

                            String postKey = snapshots.getKey();
                            String uid = snapshots.child("uid").getValue().toString();
                            String date = snapshots.child("date").getValue().toString();
                            String time = snapshots.child("time").getValue().toString();
                            String title = snapshots.child("title").getValue().toString();
                            String description = snapshots.child("description").getValue().toString();
                            String profileimage = snapshots.child("profileimage").getValue().toString();
                            String fullname = snapshots.child("fullname").getValue().toString();
                            String type = snapshots.child("type").getValue().toString();
                            String posttitletolowercase = snapshots.child("posttitletolowercase").getValue() != null ?
                                    snapshots.child("posttitletolowercase").getValue().toString() : "";
                            String timestamp = snapshots.child("timestamp").getValue().toString();
                            String postfilestoragename = snapshots.child("postfilestoragename").getValue().toString();
                            String counter = snapshots.child("counter").getValue().toString();
                            String postimage = snapshots.child("postimage").getValue() != null ?
                                    snapshots.child("postimage").getValue().toString() : "";
                            String postvideo = snapshots.child("postvideo").getValue() != null ?
                                    snapshots.child("postvideo").getValue().toString() : "";

                            postsModelList.add(new PostsModel(uid,date,time,title,description,postimage,
                                    profileimage,fullname,type,posttitletolowercase,timestamp,
                                    postfilestoragename,counter,postvideo,postKey));

                        }

                        postsLoader.setVisibility(View.GONE);
                        all_users_posts_recyclerview.setVisibility(View.VISIBLE);
                        no_posts_layout.setVisibility(View.GONE);
                        adapter = new AllPostsAdapter(AllMyPosts.this,postsModelList);
                        adapter.setHasStableIds(true);
                        all_users_posts_recyclerview.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                    }else{
                        postsLoader.setVisibility(View.GONE);
                        all_users_posts_recyclerview.setVisibility(View.GONE);
                        no_posts_layout.setVisibility(View.VISIBLE);
                    }
                }else{
                    postsLoader.setVisibility(View.GONE);
                    all_users_posts_recyclerview.setVisibility(View.GONE);
                    no_posts_layout.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void firebasePostsSearch(String searchText){

        if(!searchText.isEmpty()) {

            allPostsRef.orderByChild("uid")
                    .startAt(currentUserID).endAt(currentUserID + "\uf0ff");

            allPostsRef.orderByChild("posttitletolowercase").startAt(searchText).endAt(searchText + "\uf0ff")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                if (dataSnapshot.hasChildren()) {

                                    postsModelList.clear();

                                    for (final DataSnapshot snapshots : dataSnapshot.getChildren()) {

                                        String postKey = snapshots.getKey();
                                        String uid = snapshots.child("uid").getValue().toString();
                                        String date = snapshots.child("date").getValue().toString();
                                        String time = snapshots.child("time").getValue().toString();
                                        String title = snapshots.child("title").getValue().toString();
                                        String description = snapshots.child("description").getValue().toString();
                                        String profileimage = snapshots.child("profileimage").getValue().toString();
                                        String fullname = snapshots.child("fullname").getValue().toString();
                                        String type = snapshots.child("type").getValue().toString();
                                        String posttitletolowercase = snapshots.child("posttitletolowercase").getValue() != null ?
                                                snapshots.child("posttitletolowercase").getValue().toString() : "";
                                        String timestamp = snapshots.child("timestamp").getValue().toString();
                                        String postfilestoragename = snapshots.child("postfilestoragename").getValue().toString();
                                        String counter = snapshots.child("counter").getValue().toString();
                                        String postimage = snapshots.child("postimage").getValue() != null ?
                                                snapshots.child("postimage").getValue().toString() : "";
                                        String postvideo = snapshots.child("postvideo").getValue() != null ?
                                                snapshots.child("postvideo").getValue().toString() : "";

                                        postsModelList.add(new PostsModel(uid, date, time, title, description, postimage,
                                                profileimage, fullname, type, posttitletolowercase, timestamp,
                                                postfilestoragename, counter, postvideo, postKey));

                                    }

                                    postsLoader.setVisibility(View.GONE);
                                    all_users_posts_recyclerview.setVisibility(View.VISIBLE);
                                    no_posts_layout.setVisibility(View.GONE);
                                    adapter = new AllPostsAdapter(AllMyPosts.this, postsModelList);
                                    adapter.setHasStableIds(true);
                                    all_users_posts_recyclerview.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();

                                } else {
                                    postsLoader.setVisibility(View.GONE);
                                    all_users_posts_recyclerview.setVisibility(View.GONE);
                                    nothing_found_textview.setText("Oops! No post matches your search query. \n Try another search");
                                    no_posts_layout.setVisibility(View.VISIBLE);
                                }
                            } else {
                                postsLoader.setVisibility(View.GONE);
                                all_users_posts_recyclerview.setVisibility(View.GONE);
                                nothing_found_textview.setText("Oops! No post matches your search query. \n Try another search");
                                no_posts_layout.setVisibility(View.VISIBLE);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }else{
            DisplayAllMyPosts();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_view_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                firebasePostsSearch(query);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                firebasePostsSearch(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }



    public void openAddNewPostActivity(){
        //open the add new post activity
        Intent addPostIntent = new Intent(AllMyPosts.this ,AddNewPost.class);
        startActivity(addPostIntent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }
    private void openAddNewVideoPostActivity() {
        //open the add new video post activity
        Intent addVideoPostIntent = new Intent(AllMyPosts.this,AddNewVideoPost.class);
        startActivity(addVideoPostIntent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
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