package niwigh.com.smartchat.Activity;


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


public class ViewPersonPosts extends AppCompatActivity {

    Toolbar mToolbar;
    RecyclerView all_users_posts_recyclerview;
    FirebaseAuth mAuth;
    DatabaseReference allPostsRef, LikesRef, UserRef;
    String currentUserID, receiverUserID;

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
        setContentView(R.layout.activity_view_person_posts);


        mAuth = FirebaseAuth.getInstance();
        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        currentUserID = mAuth.getCurrentUser().getUid();
        allPostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts");
        allPostsRef.keepSynced(true);
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        LikesRef.keepSynced(true);
        UserRef  = FirebaseDatabase.getInstance().getReference().child("Users");
        UserRef.keepSynced(true);

        query = allPostsRef.orderByChild("uid")
                .equalTo(receiverUserID);

        mToolbar = findViewById(R.id.all_my_posts_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        try {
            //display the this user's name at the actionbar
            UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        try {
                            String user_name = dataSnapshot.child("username").getValue().toString();
                            getSupportActionBar().setTitle("@" + user_name + "'s Posts");
                        }catch (Exception e){
                            e.printStackTrace();
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

        DisplayAllCurrentUserPosts();
    }


    private void DisplayAllCurrentUserPosts() {
        try {
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChildren()) {

                            postsModelList.clear();

                            for (final DataSnapshot snapshots : dataSnapshot.getChildren()) {
                                try {
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
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }

                            postsLoader.setVisibility(View.GONE);
                            all_users_posts_recyclerview.setVisibility(View.VISIBLE);
                            no_posts_layout.setVisibility(View.GONE);
                            adapter = new AllPostsAdapter(ViewPersonPosts.this, postsModelList);
                            adapter.setHasStableIds(true);
                            all_users_posts_recyclerview.setAdapter(adapter);
                            adapter.notifyDataSetChanged();

                        } else {
                            postsLoader.setVisibility(View.GONE);
                            all_users_posts_recyclerview.setVisibility(View.GONE);
                            no_posts_layout.setVisibility(View.VISIBLE);
                        }
                    } else {
                        postsLoader.setVisibility(View.GONE);
                        all_users_posts_recyclerview.setVisibility(View.GONE);
                        no_posts_layout.setVisibility(View.VISIBLE);
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


    public void firebasePostsSearch(String searchText){
        try {
            if (!searchText.isEmpty()) {

                allPostsRef.orderByChild("uid")
                        .startAt(receiverUserID).endAt(receiverUserID + "\uf0ff");

                allPostsRef.orderByChild("posttitletolowercase").startAt(searchText).endAt(searchText + "\uf0ff")
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (dataSnapshot.hasChildren()) {

                                        postsModelList.clear();

                                        for (final DataSnapshot snapshots : dataSnapshot.getChildren()) {
                                            try {
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
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }

                                        postsLoader.setVisibility(View.GONE);
                                        all_users_posts_recyclerview.setVisibility(View.VISIBLE);
                                        no_posts_layout.setVisibility(View.GONE);
                                        adapter = new AllPostsAdapter(ViewPersonPosts.this, postsModelList);
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
            } else {
                DisplayAllCurrentUserPosts();
            }
        }catch (Exception e){
            e.printStackTrace();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
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