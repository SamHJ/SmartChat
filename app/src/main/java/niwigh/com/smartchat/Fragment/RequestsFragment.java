package niwigh.com.smartchat.Fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import niwigh.com.smartchat.Model.FriendRequestFragmentModel;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;



/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {
    Toolbar mToolbar;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    RecyclerView all_users_friend_request_lists;
    DatabaseReference FriendRequestRef, UsersRef, FriendsRef,NotificationsRef;
    FirebaseAuth mAuth;
    String online_user_id;
    String saveCurrentDate;
    LinearLayout all_friend_request_layout;
    View no_friendsRequest_inflate;
    LayoutInflater inflaters;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View requestsFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);
        setHasOptionsMenu(true);

        mToolbar = requestsFragmentView.findViewById(R.id.userhome_navigation_opener_include);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Friend Requests");
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowCustomEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        UsersRef  = FirebaseDatabase.getInstance().getReference().child("Users");
        UsersRef.keepSynced(true);
        FriendRequestRef  = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendRequestRef.keepSynced(true);
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendsRef.keepSynced(true);
        NotificationsRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        NotificationsRef.keepSynced(true);

        inflaters = inflater;

        all_users_friend_request_lists = requestsFragmentView.findViewById(R.id.all_users_friend_request_lists);
        all_users_friend_request_lists.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        all_users_friend_request_lists.setLayoutManager(linearLayoutManager);

        drawerLayout = requestsFragmentView.findViewById(R.id.all_posts_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, R.string.drawer_open, R.string.drawer_close);


        all_friend_request_layout = requestsFragmentView.findViewById(R.id.no_friends_request_layout);
        all_friend_request_layout.setVisibility(View.GONE);

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("FriendRequests")){
                    DatabaseReference friendRequestref = FirebaseDatabase.getInstance().getReference()
                            .child("FriendRequests");
                    friendRequestref.child(online_user_id).addValueEventListener(new ValueEventListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                all_friend_request_layout.setVisibility(View.GONE);
                                all_users_friend_request_lists.setVisibility(View.VISIBLE);
                                DisplayFriendRequets();
                            }else
                            {
                                all_friend_request_layout.removeAllViews();
                                all_users_friend_request_lists.setVisibility(View.GONE);
                                no_friendsRequest_inflate = inflater.inflate(
                                        R.layout.no_video_posts_query, all_friend_request_layout);
                                all_friend_request_layout.setVisibility(View.VISIBLE);
                                TextView displayText = no_friendsRequest_inflate.findViewById(R.id.tv_one_two);
                                displayText.setText("You have no friend request yet. \n Your friend requests will appear here.");
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }else {
                    all_friend_request_layout.removeAllViews();
                    all_users_friend_request_lists.setVisibility(View.VISIBLE);
                   no_friendsRequest_inflate = inflater.inflate(
                            R.layout.no_video_posts_query, all_friend_request_layout);
                    all_friend_request_layout.setVisibility(View.VISIBLE);
                    TextView displayText = no_friendsRequest_inflate.findViewById(R.id.tv_one_two);
                    displayText.setText("You have no friend request yet. \n Your friend requests will appear here.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        return requestsFragmentView;
    }

    private void CheckForFriendRequests() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("FriendRequests")){
                    DatabaseReference friendRequestref = FirebaseDatabase.getInstance().getReference()
                            .child("FriendRequests");
                    friendRequestref.child(online_user_id).addValueEventListener(new ValueEventListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                all_friend_request_layout.setVisibility(View.GONE);
                                all_users_friend_request_lists.setVisibility(View.VISIBLE);
                                DisplayFriendRequets();
                            }else
                            {
                                all_friend_request_layout.removeAllViews();
                                all_users_friend_request_lists.setVisibility(View.GONE);
                                no_friendsRequest_inflate = inflaters.inflate(
                                        R.layout.no_video_posts_query, all_friend_request_layout);
                                all_friend_request_layout.setVisibility(View.VISIBLE);
                                TextView displayText = no_friendsRequest_inflate.findViewById(R.id.tv_one_two);
                                displayText.setText("You have no friend request yet. \n Your friend requests will appear here.");
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }else {
                    all_friend_request_layout.removeAllViews();
                    all_users_friend_request_lists.setVisibility(View.GONE);
                   no_friendsRequest_inflate = inflaters.inflate(
                            R.layout.no_video_posts_query, all_friend_request_layout);
                    all_friend_request_layout.setVisibility(View.VISIBLE);
                    TextView displayText = no_friendsRequest_inflate.findViewById(R.id.tv_one_two);
                    displayText.setText("You have no friend request yet. \n  Your friend requests will appear here.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void DisplayFriendRequets() {
        FirebaseRecyclerAdapter<FriendRequestFragmentModel, RequestViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<FriendRequestFragmentModel, RequestViewHolder>
                        (
                                FriendRequestFragmentModel.class,
                                R.layout.all_users_friend_request_layout,
                                RequestViewHolder.class,
                                FriendRequestRef.child(online_user_id)

                        )
                {
                    @Override
                    protected void populateViewHolder(final RequestViewHolder viewHolder, FriendRequestFragmentModel model, int position) {

                        final String list_user_id = getRef(position).getKey();

                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()) {

                                    try {

                                        final String userName = dataSnapshot.child("fullname").getValue().toString();
                                        final String userLocation = dataSnapshot.child("location").getValue().toString();
                                        final String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();


                                        FriendRequestRef.child(online_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                if (dataSnapshot.exists()) {

                                                    for (DataSnapshot childDatasnapshot : dataSnapshot.getChildren()) {
                                                        final String key = childDatasnapshot.getKey();

                                                        FriendRequestRef.child(online_user_id).child(key).addValueEventListener(new ValueEventListener() {
                                                            @SuppressLint("SetTextI18n")
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                if (dataSnapshot.exists()) {

                                                                    String requestType = dataSnapshot.child("request_type").getValue().toString();

                                                                    if (requestType.equals("received")) {

                                                                        viewHolder.setUserName(userName);
                                                                        viewHolder.setUserLocation(userLocation);
                                                                        viewHolder.setUserProfileImage(userProfileImage, getContext());
                                                                        viewHolder.accept_reqeust_btn.setOnClickListener(new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View v) {
                                                                                AcceptFriendRequest(key, online_user_id);
                                                                                String requestAccepted = "Request Accepted";
                                                                                successToast(requestAccepted);
                                                                            }
                                                                        });
                                                                        viewHolder.decline_reqeust_btn.setOnClickListener(new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View v) {
                                                                                CancelFriendRequest(online_user_id, key);
                                                                                String requestDeclined = "Request declined";
                                                                                successToast(requestDeclined);
                                                                            }
                                                                        });
                                                                    } else {

                                                                        viewHolder.setUserName(userName);
                                                                        viewHolder.setUserLocation(userLocation);
                                                                        viewHolder.setUserProfileImage(userProfileImage, getContext());

                                                                        viewHolder.accept_reqeust_btn.setText("Cancel Friend Request");
                                                                        viewHolder.decline_reqeust_btn.setVisibility(View.GONE);

                                                                        viewHolder.accept_reqeust_btn.setOnClickListener(new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View v) {
                                                                                CancelFriendRequest(online_user_id, key);
                                                                                String cancelmessage = "Request Cancelled";
                                                                                successToast(cancelmessage);
                                                                            }
                                                                        });

                                                                    }

                                                                } else {
                                                                    all_friend_request_layout.removeAllViews();
                                                                    all_users_friend_request_lists.setVisibility(View.GONE);
                                                                    no_friendsRequest_inflate = inflaters.inflate(
                                                                            R.layout.no_video_posts_query, all_friend_request_layout);
                                                                    all_friend_request_layout.setVisibility(View.VISIBLE);
                                                                    TextView displayText = no_friendsRequest_inflate.findViewById(R.id.tv_one_two);
                                                                    displayText.setText("You have no friend request yet. \n Your friend requests will appear here.");
                                                                }

                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

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
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });



                    }


                };
        all_users_friend_request_lists.setAdapter(firebaseRecyclerAdapter);
    }


    public void AcceptFriendRequest(final String senderUserID, final String receiverUserID) {
        //for date
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());



        UsersRef.child(online_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String friednusername = dataSnapshot.child("username").getValue().toString();
                    final Map<String,Object> friendsMap = new HashMap<String, Object>();
                    friendsMap.put("friendsname", friednusername);
                    friendsMap.put("date", saveCurrentDate);

                    UsersRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                String onlineuserfriednusername = dataSnapshot.child("username").getValue().toString();
                                final Map<String,Object> onlineUserfriendsMap = new HashMap<String, Object>();
                                onlineUserfriendsMap.put("friendsname", onlineuserfriednusername);
                                onlineUserfriendsMap.put("date", saveCurrentDate);

                                FriendsRef.child(senderUserID).child(receiverUserID).updateChildren(friendsMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful()){

                                                    FriendsRef.child(receiverUserID).child(senderUserID).updateChildren(onlineUserfriendsMap)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if(task.isSuccessful()){

                                                                        FriendRequestRef.child(receiverUserID).child(senderUserID)
                                                                                .removeValue()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                        if(task.isSuccessful()){

                                                                                            NotificationsRef.child(receiverUserID).removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if(task.isSuccessful()){
                                                                                                                FriendRequestRef.child(senderUserID).child(receiverUserID)
                                                                                                                        .removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                                                                if(task.isSuccessful()){
                                                                                                                                    CheckForFriendRequests();
                                                                                                                                }
                                                                                                                            }
                                                                                                                        });
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                        }
                                                                                    }
                                                                                });
                                                                    }
                                                                }
                                                            });
                                                }
                                            }
                                        });
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


    private void CancelFriendRequest( final String senderUserID, final String receiverUserID) {

        FriendRequestRef.child(receiverUserID).child(senderUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            NotificationsRef.child(receiverUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){

                                                FriendRequestRef.child(senderUserID).child(receiverUserID).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if(task.isSuccessful()){
                                                                    CheckForFriendRequests();
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });

    }
    private void successToast(String message){
        Toasty.success(getActivity(),message, Toasty.LENGTH_SHORT, true).show();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        View mView;
        Button accept_reqeust_btn,decline_reqeust_btn;


        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            accept_reqeust_btn = mView.findViewById(R.id.accept_reqeust_btn);
            decline_reqeust_btn = mView.findViewById(R.id.decline_reqeust_btn);
        }

        public void setUserProfileImage( final String userProfileImage, final Context ctx) {
            final CircleImageView userprofileimage = mView.findViewById(R.id.all_friend_request_profile_image);
            try{
                Picasso.with(ctx).load(userProfileImage).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.easy_to_use).into(userprofileimage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                        Picasso.with(ctx).load(userProfileImage).placeholder(R.drawable.easy_to_use).into(userprofileimage);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public void setUserName(String userName) {
            TextView userfullname = mView.findViewById(R.id.friend_request_user_fullname);
            userfullname.setText(userName);

        }

        public void setUserLocation(String userSchool) {
            TextView userschool = mView.findViewById(R.id.friend_request_user_school);
            userschool.setText(String.format("From: %s", userSchool));
        }


    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.data_sub_menu, menu);


        MenuItem tittle = menu.findItem(R.id.data_sub_title);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
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