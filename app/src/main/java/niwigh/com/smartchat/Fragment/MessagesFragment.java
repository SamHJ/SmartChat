package niwigh.com.smartchat.Fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
import niwigh.com.smartchat.Activity.Groups;
import niwigh.com.smartchat.Activity.MessagingArea;
import niwigh.com.smartchat.Model.MessagesFragmentModel;
import niwigh.com.smartchat.Model.MessagesModel;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;


/**
 * A simple {@link Fragment} subclass.
 */
public class MessagesFragment extends Fragment {
    Toolbar mToolbar;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    RecyclerView all_user_messages_recyclerview;

    FirebaseAuth mAuth;
    DatabaseReference MessagesRef, usersRef;
    String onlineUserID;
    LinearLayout all_friends_layout;
    View no_friends_inflate_two;
    LayoutInflater inflaters;


    public MessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View messagesFragmentView = inflater.inflate(R.layout.fragment_messages, container, false);
        setHasOptionsMenu(true);

        mToolbar = messagesFragmentView.findViewById(R.id.userhome_navigation_opener_include);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Messages");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowCustomEnabled(true);

        drawerLayout = messagesFragmentView.findViewById(R.id.all_posts_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, R.string.drawer_open, R.string.drawer_close);
        all_user_messages_recyclerview = messagesFragmentView.findViewById(R.id.all_users_messages_lists);

        mAuth = FirebaseAuth.getInstance();
        onlineUserID = mAuth.getCurrentUser().getUid();
        MessagesRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(onlineUserID);
        MessagesRef.keepSynced(true);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);

        inflaters = inflater;


        all_user_messages_recyclerview.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        all_user_messages_recyclerview.setLayoutManager(linearLayoutManager);

        all_friends_layout = messagesFragmentView.findViewById(R.id.no_messages_layout);
        all_friends_layout.setVisibility(View.GONE);

        return messagesFragmentView;

    }


    private void DisplayAllMessages(final String from, final String to, final boolean isseen, final String time,
                                    final String message, final int unread) {

        FirebaseRecyclerAdapter<MessagesFragmentModel, MessagesViewHolder> firebaseRecyclerAdapter = new
                FirebaseRecyclerAdapter<MessagesFragmentModel, MessagesViewHolder>
                        (
                                MessagesFragmentModel.class,
                                R.layout.all_users_display_layout,
                                MessagesViewHolder.class,
                                MessagesRef
                        ) {
                    @Override
                    protected void populateViewHolder(final MessagesViewHolder viewHolder, MessagesFragmentModel model, int position) {


                        final String usersIDs = getRef(position).getKey();
                        usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (from.equals(onlineUserID) || to.equals(onlineUserID)) {

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
                                        viewHolder.setProfileimage(getActivity(), userProfileImage);
                                        viewHolder.setMessages(usersIDs);
                                        viewHolder.setIssenStatus(isseen,from);

                                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
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
                                    } else {
                                        all_friends_layout.removeAllViews();
                                        no_friends_inflate_two = inflaters.inflate(
                                                R.layout.no_image_posts_query_result, all_friends_layout);
                                        all_friends_layout.setVisibility(View.VISIBLE);
                                        all_user_messages_recyclerview.setVisibility(View.GONE);
                                        TextView displayText = no_friends_inflate_two.findViewById(R.id.tv_one_two);
                                        displayText.setText("You have no messages yet.");
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                };

        all_user_messages_recyclerview.setAdapter(firebaseRecyclerAdapter);
    }


    @Override
    public void onStart() {
        super.onStart();

        Utilities.getInstance(getContext()).updateUserStatus("Online");

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("Messages")) {
                    final DatabaseReference currentuserFriend = FirebaseDatabase.getInstance().getReference().child("Messages");

                    currentuserFriend.child(onlineUserID).addValueEventListener(new ValueEventListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChildren()) {

                                int unread = 0;
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    final String key_one = snapshot.getKey();
                                    for (DataSnapshot snapshot1 : dataSnapshot.child(key_one).getChildren()) {
                                        MessagesModel messagesModel = snapshot1.getValue(MessagesModel.class);
                                        String from = messagesModel.getFrom();
                                        String to = messagesModel.getTo();
                                        boolean isseen = messagesModel.isIsseen();
                                        String time = messagesModel.getTime();
                                        String message = messagesModel.getMessage();
                                        if (to.equals(onlineUserID) && !isseen) {
                                            unread++;
                                        }
                                        DisplayAllMessages(from, to, isseen, time, message, unread);

                                    }


                                }
                            }
                            else {
                                all_friends_layout.removeAllViews();
                                no_friends_inflate_two = inflaters.inflate(
                                        R.layout.no_image_posts_query_result, all_friends_layout);
                                all_friends_layout.setVisibility(View.VISIBLE);
                                all_user_messages_recyclerview.setVisibility(View.GONE);
                                TextView displayText = no_friends_inflate_two.findViewById(R.id.tv_one_two);
                                displayText.setText("You have no messages yet.");
                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                } else {
                    all_friends_layout.removeAllViews();
                    no_friends_inflate_two = inflaters.inflate(
                            R.layout.no_image_posts_query_result, all_friends_layout);
                    all_friends_layout.setVisibility(View.VISIBLE);
                    all_user_messages_recyclerview.setVisibility(View.GONE);
                    TextView displayText = no_friends_inflate_two.findViewById(R.id.tv_one_two);
                    displayText.setText("You have no messages yet.");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public static class MessagesViewHolder extends RecyclerView.ViewHolder {

        ImageView onlineStatusImage, offlineStatusImage, msg_type_img,msg_type_img_icon;
        final CircleImageView friendsprofileimage;
        TextView unread_messages;
        TextView friends_name, muser_status;

        View mView;

        public MessagesViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            onlineStatusImage = (ImageView) itemView.findViewById(R.id.user_online_icon);
            offlineStatusImage = (ImageView) itemView.findViewById(R.id.user_offline_icon);
            msg_type_img_icon = itemView.findViewById(R.id.msg_type_img_icon);
            onlineStatusImage.setVisibility(View.INVISIBLE);
            offlineStatusImage.setVisibility(View.INVISIBLE);
            friendsprofileimage = mView.findViewById(R.id.search_all_users_profile_image);
            unread_messages = itemView.findViewById(R.id.unread_messages);
            friends_name = mView.findViewById(R.id.search_all_users_profile_username);
            muser_status = mView.findViewById(R.id.search_all_users_profile_school);
            msg_type_img = itemView.findViewById(R.id.msg_type_img);
        }

        public void setProfileimage(final Context ctx, final String profileimage) {

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


        public void setMessages(final String userIDs) {
            final String onlineUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.hasChild("Messages")) {
                        final DatabaseReference currentuserFriend = FirebaseDatabase.getInstance().getReference().child("Messages");

                        currentuserFriend.child(userIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    int unread = 0;
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        final String key_one = snapshot.getKey();
                                        for (DataSnapshot snapshot1 : dataSnapshot.child(key_one).getChildren()) {
                                            MessagesModel messagesModel = snapshot1.getValue(MessagesModel.class);
                                            String time = messagesModel.getTime();
                                            String message = messagesModel.getMessage();
                                            String to = messagesModel.getTo();
                                            boolean isseen = messagesModel.isIsseen();
                                            String type = messagesModel.getType();

                                            friends_name.setText(time);
                                            if (type.equals("document")) {
                                                muser_status.setVisibility(View.GONE);
                                                msg_type_img_icon.setVisibility(View.VISIBLE);
                                                msg_type_img_icon.setImageResource(R.drawable.ic_document);
                                                if(to.equals(onlineUser) && !isseen){
                                                    msg_type_img_icon.setColorFilter(mView.getContext().getResources().getColor(R.color.colorPrimary));
                                                }
                                            } else if (type.equals("image")) {
                                                muser_status.setVisibility(View.GONE);
                                                msg_type_img_icon.setVisibility(View.VISIBLE);
                                                msg_type_img_icon.setImageResource(R.drawable.ic_image);
                                                if(to.equals(onlineUser) && !isseen){
                                                    msg_type_img_icon.setColorFilter(mView.getContext().getResources().getColor(R.color.colorPrimary));
                                                }
                                            } else if (type.equals("video")) {
                                                muser_status.setVisibility(View.GONE);
                                                msg_type_img_icon.setVisibility(View.VISIBLE);
                                                msg_type_img_icon.setImageResource(R.drawable.ic_videocam);
                                                if(to.equals(onlineUser) && !isseen){
                                                    msg_type_img_icon.setColorFilter(mView.getContext().getResources().getColor(R.color.colorPrimary));
                                                }
                                            } else if (type.equals("audio")) {
                                                muser_status.setVisibility(View.GONE);
                                                msg_type_img_icon.setVisibility(View.VISIBLE);
                                                msg_type_img_icon.setImageResource(R.drawable.ic_music);
                                                if(to.equals(onlineUser) && !isseen){
                                                    msg_type_img_icon.setColorFilter(mView.getContext().getResources().getColor(R.color.colorPrimary));
                                                }
                                            } else {
                                                muser_status.setVisibility(View.VISIBLE);
                                                msg_type_img_icon.setVisibility(View.GONE);
                                                muser_status.setText(message);
                                                if(to.equals(onlineUser) && !isseen){
                                                    muser_status.setTextColor(itemView.getContext().getResources()
                                                    .getColor(R.color.colorPrimary));
                                                }
                                            }

                                            if (to.equals(onlineUser) && !isseen) {
                                                unread++;
                                            }
                                        }

                                        if (unread == 0) {
                                            unread_messages.setVisibility(View.GONE);
                                        } else {
                                            unread_messages.setVisibility(View.VISIBLE);
                                            unread_messages.setText(String.valueOf(unread));
                                        }


                                    }

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

        public void setIssenStatus(boolean isseen, String from) {

            if(from.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){

                msg_type_img.setVisibility(View.VISIBLE);
                if (isseen){
                    msg_type_img.setColorFilter(mView.getContext().getResources().getColor(R.color.colorPrimaryDark));
                }else{
                    msg_type_img.setColorFilter(mView.getContext().getResources().getColor(R.color.light_gray));
                }

            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.view_group_menu, menu);
        if (menu instanceof MenuBuilder) {
            MenuBuilder menuBuilder = (MenuBuilder) menu;
            menuBuilder.setOptionalIconsVisible(true);
        }
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