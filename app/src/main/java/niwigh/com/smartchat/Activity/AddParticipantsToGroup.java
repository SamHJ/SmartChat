package niwigh.com.smartchat.Activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import niwigh.com.smartchat.Model.FindFriendsModel;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;


public class AddParticipantsToGroup extends AppCompatActivity {

    RecyclerView all_users_recyclerview;
    Toolbar toolbar;
    DatabaseReference allUsersRef;
    FloatingActionButton add_friends_fab;
    String currentUserID;
    FirebaseAuth mAuth;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_participants_to_group);

        String group_Name = getIntent().getStringExtra("group_Name");

        allUsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        allUsersRef.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add participants to");
        getSupportActionBar().setSubtitle(group_Name);

        all_users_recyclerview = findViewById(R.id.all_users_recyclerview);
        add_friends_fab = findViewById(R.id.add_friends_fab);
        add_friends_fab.setVisibility(View.GONE);
        all_users_recyclerview.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        all_users_recyclerview.setLayoutManager(linearLayoutManager);

        DisplayAllUsers();


    }

    private void DisplayAllUsers() {

        final String group_Name = getIntent().getStringExtra("group_Name");
        final int[] count = {0};

        final Map<String,Object> usersMap = new HashMap<>();
        FirebaseRecyclerAdapter<FindFriendsModel, FindFriendsviewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<FindFriendsModel, FindFriendsviewHolder>
                        (
                                FindFriendsModel.class,
                                R.layout.all_users_to_add_to_group,
                                FindFriendsviewHolder.class,
                                allUsersRef

                        ) {
                    @Override
                    protected void populateViewHolder(final FindFriendsviewHolder viewHolder, FindFriendsModel model, final int position) {


                        final String key = getRef(position).getKey();
                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setUsername(model.getUsername());
                        viewHolder.setUserLocation(model.getUserLocation());
                        viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());

                        viewHolder.select_user_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @SuppressLint("RestrictedApi")
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                if(compoundButton.isChecked()){
                                    usersMap.put(key,key);
                                    if(usersMap.isEmpty()){
                                        add_friends_fab.setVisibility(View.GONE);
                                        count[0] = 0;
                                    }else {
                                        add_friends_fab.setVisibility(View.VISIBLE);
                                        count[0] = count[0] +1;
                                    }
                                }else {
                                    if(usersMap.containsKey(key)){
                                        usersMap.remove(key);
                                        if (usersMap.isEmpty()){
                                            add_friends_fab.setVisibility(View.GONE);
                                            count[0] = 0;
                                        }else {
                                            add_friends_fab.setVisibility(View.VISIBLE);
                                            count[0] = count[0] -1;
                                        }
                                    }
                                }

                                getSupportActionBar().setSubtitle(group_Name + " ("+ count[0] +")");

                                add_friends_fab.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        AddUserToGroup(usersMap);
                                    }
                                });
                            }
                        });



                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String visit_user_id = getRef(position).getKey();
                                Intent viewUserDetailsIntent = new Intent(AddParticipantsToGroup.this, PersonProfile.class);
                                viewUserDetailsIntent.putExtra("visit_user_id", visit_user_id);
                                startActivity(viewUserDetailsIntent);
                                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                            }
                        });

                    }
                };
        all_users_recyclerview.setAdapter(firebaseRecyclerAdapter);

    }

    private void searchForFriends(String searchBoxInput) {

        Query findusersSearchQuery = allUsersRef.orderByChild("fullname")
                .startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");

        final String group_Name = getIntent().getStringExtra("group_Name");
        final int[] count = {0};

        final Map<String,Object> usersMap = new HashMap<>();
        FirebaseRecyclerAdapter<FindFriendsModel, FindFriendsviewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<FindFriendsModel, FindFriendsviewHolder>
                        (
                                FindFriendsModel.class,
                                R.layout.all_users_to_add_to_group,
                                FindFriendsviewHolder.class,
                                findusersSearchQuery

                        ) {
                    @Override
                    protected void populateViewHolder(final FindFriendsviewHolder viewHolder, FindFriendsModel model, final int position) {


                        final String key = getRef(position).getKey();
                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setUsername(model.getUsername());
                        viewHolder.setUserLocation(model.getUserLocation());
                        viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());

                        viewHolder.select_user_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @SuppressLint("RestrictedApi")
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                if(compoundButton.isChecked()){
                                    usersMap.put(key,key);
                                    if(usersMap.isEmpty()){
                                        add_friends_fab.setVisibility(View.GONE);
                                        count[0] = 0;
                                    }else {
                                        add_friends_fab.setVisibility(View.VISIBLE);
                                        count[0] = count[0] +1;
                                    }
                                }else {
                                    if(usersMap.containsKey(key)){
                                        usersMap.remove(key);
                                        if (usersMap.isEmpty()){
                                            add_friends_fab.setVisibility(View.GONE);
                                            count[0] = 0;
                                        }else {
                                            add_friends_fab.setVisibility(View.VISIBLE);
                                            count[0] = count[0] -1;
                                        }
                                    }
                                }

                                getSupportActionBar().setSubtitle(group_Name + " ("+ count[0] +")");

                                add_friends_fab.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        AddUserToGroup(usersMap);
                                    }
                                });
                            }
                        });



                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String visit_user_id = getRef(position).getKey();
                                Intent viewUserDetailsIntent = new Intent(AddParticipantsToGroup.this, PersonProfile.class);
                                viewUserDetailsIntent.putExtra("visit_user_id", visit_user_id);
                                startActivity(viewUserDetailsIntent);
                                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                            }
                        });

                    }
                };
        all_users_recyclerview.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FindFriendsviewHolder extends RecyclerView.ViewHolder {
        View mView;
        final CircleImageView userprofileimage;
        TextView userfullname;
        CheckBox select_user_checkbox;
        public FindFriendsviewHolder(@NonNull View itemView) {
            super(itemView);
            this.mView = itemView;
            userprofileimage = mView.findViewById(R.id.search_all_users_profile_image);
            userfullname = mView.findViewById(R.id.search_all_users_profile_name);
            select_user_checkbox = mView.findViewById(R.id.select_user_checkbox);
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
            userfullname.setText(fullname);
        }

        public void setUsername(String username) {
            TextView user_name = mView.findViewById(R.id.search_all_users_profile_username);
            user_name.setText("@" + username);
        }

        public void setUserLocation(String location) {
            TextView user_school = mView.findViewById(R.id.search_all_users_profile_school);
            user_school.setText("From " + location);
        }
    }


    public void AddUserToGroup(Map<String, Object> usersMap){
        final ProgressDialog loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Adding participants");
        loadingBar.setMessage("A moment please");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.setCancelable(false);
        loadingBar.show();
        String group_category = getIntent().getStringExtra("group_category");
        String group_key = getIntent().getStringExtra("group_key");
        DatabaseReference groupsUserRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_category)
                .child("groups").child(group_key).child("users");
        groupsUserRef.updateChildren(usersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadingBar.dismiss();
                Toasty.success(AddParticipantsToGroup.this,
                        "Users added successfully", Toasty.LENGTH_LONG, true).show();
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.getMessage();
                //{.....................
                //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                ViewGroup viewGroup = findViewById(android.R.id.content);

                //then we will inflate the custom alert dialog xml that we created
                View dialogView = LayoutInflater.from(AddParticipantsToGroup.this).inflate(R.layout.error_dialog, viewGroup, false);


                //Now we need an AlertDialog.Builder object
                AlertDialog.Builder builder = new AlertDialog.Builder(AddParticipantsToGroup.this);

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
        });

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
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            default:
                return super.onOptionsItemSelected(item);
        }
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