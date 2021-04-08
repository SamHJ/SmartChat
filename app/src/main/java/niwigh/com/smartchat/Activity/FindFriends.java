package niwigh.com.smartchat.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import niwigh.com.smartchat.Model.FindFriendsModel;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;


public class FindFriends extends AppCompatActivity {
    Toolbar mToolbar;
    RecyclerView display_find_friends_recyclerview;
    DatabaseReference allUsersRef;
    FirebaseAuth mAuth;
    String currentUserID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        allUsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        allUsersRef.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        mToolbar = findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //init views
        display_find_friends_recyclerview = findViewById(R.id.search_results_recyclerview);
        display_find_friends_recyclerview.setHasFixedSize(true);
        display_find_friends_recyclerview.setLayoutManager(new LinearLayoutManager(this));



        DisplayAllUsers();
    }

    private void DisplayAllUsers() {


        FirebaseRecyclerAdapter<FindFriendsModel, FindFriendsviewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<FindFriendsModel, FindFriendsviewHolder>
                        (
                                FindFriendsModel.class,
                                R.layout.all_friends_display_layout,
                                FindFriendsviewHolder.class,
                                allUsersRef

                        ) {
                    @Override
                    protected void populateViewHolder(final FindFriendsviewHolder viewHolder, FindFriendsModel model, final int position) {

                            viewHolder.setFullname(model.getFullname());
                            viewHolder.setUsername(model.getUsername());
                            viewHolder.setUserLocation(model.getUserLocation());
                            viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());

                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String visit_user_id = getRef(position).getKey();
                                    Intent viewUserDetailsIntent = new Intent(FindFriends.this, PersonProfile.class);
                                    viewUserDetailsIntent.putExtra("visit_user_id", visit_user_id);
                                    startActivity(viewUserDetailsIntent);
                                    overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                }
                            });

                    }
                };
        display_find_friends_recyclerview.setAdapter(firebaseRecyclerAdapter);

    }

    private void searchForFriends(String searchBoxInput) {

        Query findusersSearchQuery = allUsersRef.orderByChild("fullname")
                .startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");

        FirebaseRecyclerAdapter<FindFriendsModel, FindFriendsviewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<FindFriendsModel, FindFriendsviewHolder>
                        (
                                FindFriendsModel.class,
                                R.layout.all_friends_display_layout,
                                FindFriendsviewHolder.class,
                                findusersSearchQuery

                        ) {
                    @Override
                    protected void populateViewHolder(final FindFriendsviewHolder viewHolder, FindFriendsModel model, final int position) {

                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setUsername(model.getUsername());
                        viewHolder.setUserLocation(model.getUserLocation());
                        viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String visit_user_id = getRef(position).getKey();
                                Intent viewUserDetailsIntent = new Intent(FindFriends.this, PersonProfile.class);
                                viewUserDetailsIntent.putExtra("visit_user_id", visit_user_id);
                                startActivity(viewUserDetailsIntent);
                                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                            }
                        });

                    }
                };
        display_find_friends_recyclerview.setAdapter(firebaseRecyclerAdapter);

    }

    public static class FindFriendsviewHolder extends RecyclerView.ViewHolder {
        View mView;
        final CircleImageView userprofileimage;
        TextView userfullname;
        public FindFriendsviewHolder(@NonNull View itemView) {
            super(itemView);
            this.mView = itemView;
            userprofileimage = mView.findViewById(R.id.search_all_users_profile_image);
            userfullname = mView.findViewById(R.id.search_all_users_profile_name);
        }

        public void setProfileimage(final Context ctx, final String profileimage){
           try{
               Picasso.with(ctx).load(profileimage).
                       networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.easy_to_use).into(userprofileimage
                       , new Callback() {
                           @Override
                           public void onSuccess() {

                           }

                           @Override
                           public void onError() {

                               Picasso.with(ctx).load(profileimage).placeholder(R.drawable.easy_to_use).into(userprofileimage);
                           }
                       });
           }catch (Exception e){
               e.printStackTrace();
           }
        }

        public void setFullname(String fullname) {
            if(fullname == null){
                mView.setVisibility(View.GONE);
            }else{
                userfullname.setText(fullname);
            }
        }

        public void setUsername(String username) {
            TextView user_name = mView.findViewById(R.id.search_all_users_profile_username);
            user_name.setText("@" + username);
        }

        public void setUserLocation(String location) {
            TextView user_school = mView.findViewById(R.id.search_all_users_profile_school);
            user_school.setText(String.format("From %s", location));
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_view_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView)MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchForFriends(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchForFriends(newText);
                return false;
            }
        });
        return  super.onCreateOptionsMenu(menu);
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