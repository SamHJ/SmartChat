package niwigh.com.smartchat.Fragment;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import niwigh.com.smartchat.Activity.FindFriends;
import niwigh.com.smartchat.Activity.Groups;
import niwigh.com.smartchat.Activity.PersonProfile;
import niwigh.com.smartchat.Activity.MessagingArea;
import niwigh.com.smartchat.Model.FriendsModel;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;



/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {
    FloatingActionButton find_friends_btn;
    Toolbar mToolbar;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    RecyclerView friendsList;

    FirebaseAuth mAuth;
    DatabaseReference friendsRef, usersRef;
    String onlineUserID;
    Dialog profilePopupDialog;
    Button btnViewProfile;
    CircleImageView PopupProfileImage;
    LinearLayout phone_call_layout, message_layout;
    TextView text_close_dialog;
    LinearLayout all_friends_layout;
    View no_friends_inflate, no_friends_inflate_two;
    LayoutInflater inflaters;



    AlertDialog  callerDialog, alertDialog;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View friendFragmentsView =  inflater.inflate(R.layout.fragment_friends, container, false);
        setHasOptionsMenu(true);

        mAuth = FirebaseAuth.getInstance();
        onlineUserID = mAuth.getCurrentUser().getUid();
        friendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(onlineUserID);
        friendsRef.keepSynced(true);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);
        alertDialog = new AlertDialog.Builder(getContext()).create();
        callerDialog = new AlertDialog.Builder(getContext()).create();

        inflaters = inflater;

        profilePopupDialog = new Dialog(getActivity());

        profilePopupDialog.setContentView(R.layout.friend_list_message_or_profile_dialog);
        btnViewProfile = profilePopupDialog.findViewById(R.id.btn_view_profile);
        PopupProfileImage = profilePopupDialog.findViewById(R.id.userhome_profile_dialog_image);
        phone_call_layout = profilePopupDialog.findViewById(R.id.phone_call_this_friend);
        message_layout = profilePopupDialog.findViewById(R.id.message_this_friend_layout);
        text_close_dialog = profilePopupDialog.findViewById(R.id.text_close_dialog);


        mToolbar = friendFragmentsView.findViewById(R.id.userhome_navigation_opener_include);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Friends");
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowCustomEnabled(true);


        drawerLayout = friendFragmentsView.findViewById(R.id.all_posts_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, R.string.drawer_open, R.string.drawer_close);

        friendsList = friendFragmentsView.findViewById(R.id.all_users_friends_lists);
        friendsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        friendsList.setLayoutManager(linearLayoutManager);

        all_friends_layout = friendFragmentsView.findViewById(R.id.no_friends_layout);


       checkForFriends(inflater);



        find_friends_btn = friendFragmentsView.findViewById(R.id.find_friends_fab_btn);

        find_friends_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeUserToFindFriendsActivity();
            }
        });


        return friendFragmentsView;


    }

    private void checkForFriends(final LayoutInflater inflater) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("Friends")){

                    DatabaseReference currentuserFriend = FirebaseDatabase.getInstance().getReference().child("Friends");
                    currentuserFriend.addListenerForSingleValueEvent(new ValueEventListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(onlineUserID)){
                                all_friends_layout.setVisibility(View.GONE);
                                friendsList.setVisibility(View.VISIBLE);
                                DisplayAllFriends();
                            }else {
                                all_friends_layout.removeAllViews();
                                friendsList.setVisibility(View.GONE);
                                no_friends_inflate = inflater.inflate(
                                        R.layout.no_video_posts_query, all_friends_layout);
                                all_friends_layout.setVisibility(View.VISIBLE);
                                TextView displayText = no_friends_inflate.findViewById(R.id.tv_one_two);
                                displayText.setText("You have no friends yet. \n Your friends will appear here.");

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }else {
                    all_friends_layout.removeAllViews();
                    friendsList.setVisibility(View.GONE);
                    no_friends_inflate_two = inflater.inflate(
                            R.layout.no_image_posts_query_result, all_friends_layout);
                    all_friends_layout.setVisibility(View.VISIBLE);
                    TextView displayText = no_friends_inflate_two.findViewById(R.id.tv_one_two);
                    displayText.setText("You have no friends yet. \n Your friends will appear here.");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    public void ShowProfilePopUpDialog(){



        text_close_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profilePopupDialog.dismiss();
            }
        });
        profilePopupDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        profilePopupDialog.show();
    }

    private void DisplayAllFriends() {

        FirebaseRecyclerAdapter<FriendsModel, FriendsViewHolder> firebaseRecyclerAdapter = new
                FirebaseRecyclerAdapter<FriendsModel, FriendsViewHolder>
                        (
                                FriendsModel.class,
                                R.layout.all_my_friends_display_layout,
                                FriendsViewHolder.class,
                                friendsRef
                        )
                {
                    @Override
                    protected void populateViewHolder(final FriendsViewHolder viewHolder, FriendsModel model, int position) {

                        viewHolder.setDate(model.getDate());
                        viewHolder.setUsername(model.getUsername());

                        final String usersIDs = getRef(position).getKey();
                        usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(dataSnapshot.exists()){

                                    try {

                                        final String type;
                                        final String fullName = dataSnapshot.child("fullname").getValue().toString();
                                        final String userName = dataSnapshot.child("username").getValue().toString();
                                        final String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();


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
                                        viewHolder.setProfileimage(getActivity(), userProfileImage);

                                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                try {
                                                    Picasso.with(getContext()).load(userProfileImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.easy_to_use)
                                                            .into(PopupProfileImage,
                                                                    new Callback() {
                                                                        @Override
                                                                        public void onSuccess() {

                                                                        }

                                                                        @Override
                                                                        public void onError() {
                                                                            Picasso.with(getContext()).load(userProfileImage).placeholder(R.drawable.easy_to_use).into(PopupProfileImage);

                                                                        }
                                                                    });
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                                ShowProfilePopUpDialog();
                                                btnViewProfile.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        Intent view_this_user_profile = new Intent(getActivity(), PersonProfile.class);
                                                        view_this_user_profile.putExtra("visit_user_id", usersIDs);
                                                        startActivity(view_this_user_profile);
                                                        getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                                    }
                                                });

                                                message_layout.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                        Intent message_this_user = new Intent(getActivity(), MessagingArea.class);
                                                        message_this_user.putExtra("visit_user_id", usersIDs);
                                                        message_this_user.putExtra("userName", userName);
                                                        message_this_user.putExtra("userFullName", fullName);
                                                        startActivity(message_this_user);
                                                        getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);

                                                    }
                                                });

                                            }
                                        });
                                    }catch(NullPointerException e){
                                        e.printStackTrace();
                                    }
                                }else{
                                    all_friends_layout.removeAllViews();
                                    friendsList.setVisibility(View.GONE);
                                    no_friends_inflate = inflaters.inflate(
                                            R.layout.no_video_posts_query, all_friends_layout);
                                    all_friends_layout.setVisibility(View.VISIBLE);
                                    TextView displayText = no_friends_inflate.findViewById(R.id.tv_one_two);
                                    displayText.setText("You have no friends yet. \n Your friends will appear here.");

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                };

        friendsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        ImageView onlineStatusImage, offlineStatusImage;
        View mView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            onlineStatusImage = itemView.findViewById(R.id.user_online_icon);
            offlineStatusImage = itemView.findViewById(R.id.user_offline_icon);
            onlineStatusImage.setVisibility(View.INVISIBLE);
            offlineStatusImage.setVisibility(View.INVISIBLE);
        }

        public void setProfileimage(final Context ctx, final String profileimage){
            final CircleImageView friendsprofileimage = mView.findViewById(R.id.search_all_users_profile_image);
            try{
                Picasso.with(ctx).load(profileimage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.easy_to_use)
                        .into(friendsprofileimage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {

                                Picasso.with(ctx).load(profileimage).placeholder(R.drawable.easy_to_use).into(friendsprofileimage);
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

        public void setDate(String date) {
            TextView friends_date = mView.findViewById(R.id.search_all_users_profile_school);
            friends_date.setText("Friends since " + date);
        }


    }


    @SuppressLint("RestrictedApi")
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_view_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView)MenuItemCompat.getActionView(item);
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

        inflater.inflate(R.menu.view_group_menu, menu);
        if (menu instanceof MenuBuilder) {
            MenuBuilder menuBuilder = (MenuBuilder) menu;
            menuBuilder.setOptionalIconsVisible(true);
        }
    }

    public void firebasePostsSearch(String searchText){
        Query firebaseSearchQuery = friendsRef.orderByChild("friendsname").startAt(searchText).endAt(searchText + "\uf0ff");

        FirebaseRecyclerAdapter<FriendsModel, FriendsViewHolder> firebaseRecyclerAdapter = new
                FirebaseRecyclerAdapter<FriendsModel, FriendsViewHolder>
                        (
                                FriendsModel.class,
                                R.layout.all_users_display_layout,
                                FriendsViewHolder.class,
                                firebaseSearchQuery
                        )
                {
                    @Override
                    protected void populateViewHolder(final FriendsViewHolder viewHolder, FriendsModel model, int position) {

                        viewHolder.setDate(model.getDate());
                        viewHolder.setUsername(model.getUsername());

                        final String usersIDs = getRef(position).getKey();
                        usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(dataSnapshot.exists()){

                                    try{

                                    final String type;
                                    final String fullName = dataSnapshot.child("fullname").getValue().toString();
                                    final String userName = dataSnapshot.child("username").getValue().toString();
                                    final String userProfileImage =dataSnapshot.child("profileimage").getValue().toString();


                                        Picasso.with(getContext()).load(userProfileImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.easy_to_use)
                                                .into(PopupProfileImage,
                                                        new Callback() {
                                                            @Override
                                                            public void onSuccess() {

                                                            }

                                                            @Override
                                                            public void onError() {
                                                                Picasso.with(getContext()).load(userProfileImage).placeholder(R.drawable.easy_to_use).into(PopupProfileImage);

                                                            }
                                                        });


                                    if(dataSnapshot.hasChild("userState")){
                                        type = dataSnapshot.child("userState").child("type").getValue().toString();
                                        if(type.equals("Online")){
                                            viewHolder.onlineStatusImage.setVisibility(View.VISIBLE);
                                            viewHolder.offlineStatusImage.setVisibility(View.GONE);
                                        }
                                        else {
                                            viewHolder.offlineStatusImage.setVisibility(View.VISIBLE);
                                            viewHolder.onlineStatusImage.setVisibility(View.GONE);
                                        }

                                    }


                                    viewHolder.setFullname(fullName);
                                    viewHolder.setUsername(userName);
                                    viewHolder.setProfileimage(getActivity(), userProfileImage);

                                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            ShowProfilePopUpDialog();
                                            btnViewProfile.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent view_this_user_profile = new Intent(getActivity(), PersonProfile.class);
                                                    view_this_user_profile.putExtra("visit_user_id", usersIDs);
                                                    startActivity(view_this_user_profile);
                                                    getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                                    profilePopupDialog.dismiss();
                                                }
                                            });

                                            message_layout.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                    Intent message_this_user = new Intent(getActivity(), MessagingArea.class);
                                                    message_this_user.putExtra("visit_user_id", usersIDs);
                                                    message_this_user.putExtra("userName", userName);
                                                    message_this_user.putExtra("userFullName", fullName);
                                                    startActivity(message_this_user);
                                                    getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                                    profilePopupDialog.dismiss();

                                                }
                                            });
                                        }
                                    });

                                    }catch (NullPointerException e){
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                };

        friendsList.setAdapter(firebaseRecyclerAdapter);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_view_group:
                Intent group_intent = new Intent(getContext(), Groups.class);
                startActivity(group_intent);
                getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void takeUserToFindFriendsActivity() {
        Intent takemetofindfriends = new Intent(getContext(), FindFriends.class);
        startActivity(takemetofindfriends);
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
        if(profilePopupDialog != null){
            profilePopupDialog.dismiss();
        }
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