package niwigh.com.smartchat.Fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import niwigh.com.smartchat.Activity.AddNewPost;
import niwigh.com.smartchat.Activity.AddNewVideoPost;
import niwigh.com.smartchat.Adapter.AllPostsAdapter;
import niwigh.com.smartchat.Model.PostsModel;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;



/**
 * A simple {@link Fragment} subclass.
 */
public class PostsFragment extends Fragment {
    com.github.clans.fab.FloatingActionButton AddNewPostFab, AddNewVideoPost;
    RecyclerView all_users_posts_recyclerview;
    ProgressBar postsLoader;
    FirebaseAuth mAuth;
    DatabaseReference usersRef, allPostsRef, LikesRef;
    String currentUserID;
    Toolbar mToolbar;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    LinearLayoutManager linearLayoutManager;
    LinearLayout no_posts_layout;
    List<PostsModel> postsModelList;
    AllPostsAdapter adapter;
    TextView nothing_found_textview;


    public PostsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View postFragmentView = inflater.inflate(R.layout.fragment_posts, container, false);
        setHasOptionsMenu(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);
        allPostsRef =  FirebaseDatabase.getInstance().getReference().child("AllPosts");
        allPostsRef.keepSynced(true);
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        LikesRef.keepSynced(true);


        all_users_posts_recyclerview = postFragmentView.findViewById(R.id.all_users_posts_recyclerview);
        all_users_posts_recyclerview.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        all_users_posts_recyclerview.setLayoutManager(linearLayoutManager);
        all_users_posts_recyclerview.setItemAnimator(new DefaultItemAnimator());

        nothing_found_textview = postFragmentView.findViewById(R.id.nothing_found_textview);


        no_posts_layout = postFragmentView.findViewById(R.id.no_posts_layout);
        postsLoader = postFragmentView.findViewById(R.id.postsLoader);
        postsModelList = new ArrayList<>();


        //Delete image or video posts older than 2 days
        DeletePostsOlderThanFourtyEightHours();


        mToolbar = postFragmentView.findViewById(R.id.userhome_navigation_opener_include);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Posts");
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowCustomEnabled(true);

        drawerLayout = postFragmentView.findViewById(R.id.all_posts_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, R.string.drawer_open, R.string.drawer_close);


        allPostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() != 0){

                        //posts available so we get all posts
                        displayAllPosts();

                }else{
                    //no posts available
                    postsLoader.setVisibility(View.GONE);
                    all_users_posts_recyclerview.setVisibility(View.GONE);
                    no_posts_layout.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        AddNewPostFab = postFragmentView.findViewById(R.id.add_post_fab);
        AddNewPostFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddNewPostActivity();
            }
        });
        AddNewVideoPost = postFragmentView.findViewById(R.id.add_video_post_fab);
        AddNewVideoPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddNewVideoPostActivity();
            }
        });


        // Inflate the layout for this fragment
        return postFragmentView;
    }

    public void displayAllPosts() {

        allPostsRef.orderByChild("counter").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    if (dataSnapshot.hasChildren()) {

                        postsModelList.clear();

                        int i = 0;

                        for (final DataSnapshot snapshots : dataSnapshot.getChildren()){

                           if(i > 1 && i % 4 == 0){
                               //add null values every 4th position. Used to display admob banner ad
                               postsModelList.add(null);

                           }else{
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
                            i++;

                        }

                        postsLoader.setVisibility(View.GONE);
                        all_users_posts_recyclerview.setVisibility(View.VISIBLE);
                        no_posts_layout.setVisibility(View.GONE);
                        adapter = new AllPostsAdapter(getContext(),postsModelList);
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


    private void  DeletePostsOlderThanFourtyEightHours(){
        //display post for 48 hours only
        long cutoff = new Date().getTime() - TimeUnit.MILLISECONDS.convert(365, TimeUnit.DAYS);

        Query oldImagePosts = allPostsRef.orderByChild("timestamp").endAt(cutoff);
        oldImagePosts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot itemSnapshot: dataSnapshot.getChildren()){

                    //also we delete the image or video associated with this post from the storage

                    if(dataSnapshot.exists()){

                        try {

                            //so we first retrieve the name of the image or video file from the database and the type of post
                            String postfilestoragename = itemSnapshot.child("postfilestoragename").getValue().toString();
                            String posttype = itemSnapshot.child("type").getValue().toString();

                            if (posttype.equals("image")) {

                                //delete associated image
                                try {
                                    StorageReference postimageRef = FirebaseStorage.getInstance().getReference()
                                            .child("Post Images").child(postfilestoragename);
                                    postimageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // File deleted successfully
                                            Log.d("SUCCESS:", "onSuccess: deleted file");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Uh-oh, an error occurred!
                                            Log.d("ERROR:", "onFailure: did not delete file");
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } else {

                                //delete associated video
                                try {
                                    StorageReference postvideoRef = FirebaseStorage.getInstance().getReference()
                                            .child("Post Videos").child(postfilestoragename);
                                    postvideoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // File deleted successfully
                                            Log.d("SUCCESS:", "onSuccess: deleted file");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Uh-oh, an error occurred!
                                            Log.d("ERROR:", "onFailure: did not delete file");
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }


                    }


                    //remove/delete the file
                    itemSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void openAddNewVideoPostActivity() {
        //open the add new video post activity
        Intent addVideoPostIntent = new Intent(getContext(), AddNewVideoPost.class);
        startActivity(addVideoPostIntent);
        getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }


    public void firebasePostsSearch(String searchText){

        allPostsRef.orderByChild("posttitletolowercase").startAt(searchText).endAt(searchText + "\uf0ff")
                .addValueEventListener(new ValueEventListener() {
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
                        adapter = new AllPostsAdapter(getContext(),postsModelList);
                        adapter.setHasStableIds(true);
                        all_users_posts_recyclerview.setAdapter(adapter);
                        adapter.notifyDataSetChanged();

                    }else{
                        postsLoader.setVisibility(View.GONE);
                        all_users_posts_recyclerview.setVisibility(View.GONE);
                        nothing_found_textview.setText("Oops! No post matches your search query. \n Try another search");
                        no_posts_layout.setVisibility(View.VISIBLE);
                    }
                }else{
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

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void openAddNewPostActivity(){
        //open the add new post activity
        Intent addPostIntent = new Intent(getContext(), AddNewPost.class);
        startActivity(addPostIntent);
        getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    public void onStart() {
        super.onStart();
        Utilities.getInstance(getContext()).updateUserStatus("Online");
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.getInstance(getContext()).updateUserStatus("Online");
    }

    @Override
    public void onPause() {
        super.onPause();
        Utilities.getInstance(getContext()).updateUserStatus("Offline");
    }

    @Override
    public void onStop() {
        super.onStop();
        Utilities.getInstance(getContext()).updateUserStatus("Offline");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utilities.getInstance(getContext()).updateUserStatus("Offline");
    }
}