package niwigh.com.smartchat.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import niwigh.com.smartchat.Notifications.Client;
import niwigh.com.smartchat.Model.Data;
import niwigh.com.smartchat.Notifications.Sender;
import niwigh.com.smartchat.Model.Token;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Services.ApiService;
import niwigh.com.smartchat.Util.Utilities;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;


public class PersonProfile extends AppCompatActivity {


    Button btn_send_friend_request, btn_decline_friendRequest;
    CircleImageView userprofile_image;
    TextView full_name, username_text, userprofile_status, user_interested_in, location_in_text, user_phone, user_gender,dob;

    DatabaseReference friendRequestRef, UserRef, FriendsRef, allPostsRef;
    FirebaseAuth mAuth;
    String senderUserID, receiverUserID, CURRENT_STATE;
    String saveCurrentDate;
    int countFriends = 0, countPosts = 0;
    TextView no_of_friends, no_of_user_posts;
    LinearLayout all_my_no_of_posts_layout;
    DatabaseReference NotificationsRef, RootRef, msgRef,usersRef;
    boolean notify = false;

    final private String FCM_API = "https://fcm.googleapis.com/";
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.keepSynced(true);
        msgRef = FirebaseDatabase.getInstance().getReference().child("Messages");
        msgRef.keepSynced(true);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);


        mAuth = FirebaseAuth.getInstance();
        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        senderUserID = mAuth.getCurrentUser().getUid();
        UserRef =  FirebaseDatabase.getInstance().getReference().child("Users");
        UserRef.keepSynced(true);
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        friendRequestRef.keepSynced(true);
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendsRef.keepSynced(true);
        allPostsRef = FirebaseDatabase.getInstance().getReference().child("AllPosts");
        allPostsRef.keepSynced(true);
        NotificationsRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        NotificationsRef.keepSynced(true);

        apiService = Client.getClient(FCM_API).create(ApiService.class);


        no_of_friends = findViewById(R.id.no_of_friends);
        no_of_user_posts = findViewById(R.id.no_of_user_posts);
        all_my_no_of_posts_layout = findViewById(R.id.all_my_no_of_posts_layout);

        all_my_no_of_posts_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewUserDetailsIntent = new Intent(PersonProfile.this, ViewPersonPosts.class);
                viewUserDetailsIntent.putExtra("visit_user_id", receiverUserID);
                startActivity(viewUserDetailsIntent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });

        //get the no of posts of the profile of the user been viewed
        allPostsRef.orderByChild("uid")
                .startAt(receiverUserID).endAt(receiverUserID + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists()){

                            countPosts = (int)dataSnapshot.getChildrenCount();

                            no_of_user_posts.setText(Integer.toString(countPosts));

                        }
                        else {

                            no_of_user_posts.setText("0");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        //get the no of friends of the profile of the user been viewed
        FriendsRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    countFriends = (int)dataSnapshot.getChildrenCount();
                    no_of_friends.setText(Integer.toString(countFriends));
                }

                else {
                    no_of_friends.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    try{
                        final String profile_image = dataSnapshot.child("profileimage").getValue().toString();
                        String user_fullname = dataSnapshot.child("fullname").getValue().toString();
                        String user_name = dataSnapshot.child("username").getValue().toString();
                        String user_phone_p = dataSnapshot.child("phone").getValue().toString();
                        String interested_in = dataSnapshot.child("interestedin").getValue().toString();
                        String user_gender_name = dataSnapshot.child("gender").getValue().toString();
                        String user_status = dataSnapshot.child("profilestatus").getValue().toString();
                        String user_dob = dataSnapshot.child("dob").getValue().toString();
                        String user_location_name = dataSnapshot.child("location").getValue().toString();
                        location_in_text.setText(user_location_name);

                        try{
                            Picasso.with(PersonProfile.this).load(profile_image).networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.easy_to_use).into(userprofile_image, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    try {
                                        Picasso.with(PersonProfile.this).load(profile_image).placeholder(R.drawable.easy_to_use)
                                                .into(userprofile_image);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }

                                }
                            });
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        full_name.setText(user_fullname);
                        username_text.setText("@" + user_name);
                        userprofile_status.setText(user_status);
                        user_interested_in.setText(interested_in);
                        user_phone.setText(user_phone_p);
                        user_gender.setText(user_gender_name);
                        dob.setText(user_dob);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                    MaintenanceOfButtons();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        CURRENT_STATE = "not_friends";

        btn_send_friend_request = findViewById(R.id.btn_send_friend_request);
        btn_decline_friendRequest = findViewById(R.id.btn_decline_friend_request);


        btn_send_friend_request.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));


        userprofile_image = findViewById(R.id.userhome_profile_dialog_image);
        full_name = findViewById(R.id.user_full_name_text);
        username_text = findViewById(R.id.user_name_tex);
        userprofile_status =  findViewById(R.id.profile_status);
        user_interested_in = findViewById(R.id.interested_in_text);
        location_in_text = findViewById(R.id.location_in_text);
        user_phone = findViewById(R.id.phone_in_text);
        user_gender = findViewById(R.id.gender_in_text);
        dob = findViewById(R.id.dob_in_text);

        btn_decline_friendRequest.setVisibility(View.GONE);

        userprofile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent full_image_view = new Intent(PersonProfile.this, FullPersonProfileImage.class);
                full_image_view.putExtra("PostKey", receiverUserID);
                startActivity(full_image_view);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });

        if(!senderUserID.equals(receiverUserID)){

            btn_send_friend_request.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btn_send_friend_request.setEnabled(false);

                    if(CURRENT_STATE.equals("not_friends")){

                        SendFriendRequestToAPerson();
                    }
                    if(CURRENT_STATE.equals("request_sent")){
                        CancelFriendRequest();
                    }
                    if(CURRENT_STATE.equals("request_received")){
                        AcceptFriendRequest();
                    }
                    if(CURRENT_STATE.equals("friends")){
                        UnFriendAnExistingFriend();
                    }
                }
            });
        }
        else {

            btn_send_friend_request.setVisibility(View.INVISIBLE);
        }

        updateToken(FirebaseInstanceId.getInstance().getToken());

    }

    private void UnFriendAnExistingFriend()
    {

        try {
            FriendsRef.child(senderUserID).child(receiverUserID)
                    .removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                FriendsRef.child(receiverUserID).child(senderUserID)
                                        .removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @SuppressLint("SetTextI18n")
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {
                                                    DeleteMessagesIfNotFriends();
                                                    btn_send_friend_request.setEnabled(true);
                                                    CURRENT_STATE = "not_friends";
                                                    btn_send_friend_request.setText("Send Friend Request");
                                                    btn_send_friend_request.setBackgroundResource(R.color.colorPrimaryDark);
                                                    btn_decline_friendRequest.setVisibility(View.INVISIBLE);
                                                    btn_decline_friendRequest.setEnabled(false);
                                                }
                                            }
                                        });
                            }
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void sendNotification(final String message, final String senderName, final String messageSenderID,
                                  final String messageReceiverID, final String
                                          messageReceiverName, final String messageReceiverFullName)
    {
        try {
            DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
            Query query = tokens.orderByKey().equalTo(messageReceiverID);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        try {
                            Token token = snapshot.getValue(Token.class);
                            Data data = new Data(receiverUserID, senderName + " sent you a friend request",
                                    "New Friend Request ",
                                    "friendrequest", senderName, senderName);

                            Sender sender = new Sender(data, token.getToken());

                            apiService.sendNotification(sender)
                                    .enqueue(new retrofit2.Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            if (response.code() == 200) {
                                                if (!response.isSuccessful()) {
                                                    Log.i("NOTIFICATION INFO: ", "Failed!");
                                                }
                                            }
                                        }
                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {

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
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void updateToken(String token){
        try {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
            Token token1 = new Token(token);
            reference.child(senderUserID).setValue(token1);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void AcceptFriendRequest() {
        try {
            //for date
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currentDate.format(calFordDate.getTime());


            UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        try {
                            String friednusername = dataSnapshot.child("username").getValue().toString();
                            final Map<String, Object> friendsMap = new HashMap<String, Object>();
                            friendsMap.put("friendsname", friednusername);
                            friendsMap.put("date", saveCurrentDate);

                            UserRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {

                                        String onlineuserfriednusername = dataSnapshot.child("username").getValue().toString();
                                        final Map<String, Object> onlineUserfriendsMap = new HashMap<String, Object>();
                                        onlineUserfriendsMap.put("friendsname", onlineuserfriednusername);
                                        onlineUserfriendsMap.put("date", saveCurrentDate);

                                        FriendsRef.child(senderUserID).child(receiverUserID).updateChildren(friendsMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()) {

                                                            try {
                                                                FriendsRef.child(receiverUserID).child(senderUserID).updateChildren(onlineUserfriendsMap)
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if (task.isSuccessful()) {

                                                                                    try {
                                                                                        friendRequestRef.child(senderUserID).child(receiverUserID)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                                        if (task.isSuccessful()) {
                                                                                                            try {
                                                                                                                friendRequestRef.child(receiverUserID).child(senderUserID)
                                                                                                                        .removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @SuppressLint("SetTextI18n")
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                                                                if (task.isSuccessful()) {
                                                                                                                                    btn_send_friend_request.setEnabled(true);
                                                                                                                                    CURRENT_STATE = "friends";
                                                                                                                                    btn_send_friend_request.setText("Unfriend this person");
                                                                                                                                    btn_send_friend_request.setBackgroundResource(R.color.errorColor);
                                                                                                                                    btn_decline_friendRequest.setVisibility(View.INVISIBLE);
                                                                                                                                    btn_decline_friendRequest.setEnabled(false);
                                                                                                                                }
                                                                                                                            }
                                                                                                                        });
                                                                                                            }catch (Exception e){
                                                                                                                e.printStackTrace();
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }catch (Exception e){
                                                                                        e.printStackTrace();
                                                                                    }
                                                                                }
                                                                            }
                                                                        });
                                                            }catch (Exception e){
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    }
                                                });
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

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void CancelFriendRequest() {

        try {
            friendRequestRef.child(senderUserID).child(receiverUserID)
                    .removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                try {
                                    friendRequestRef.child(receiverUserID).child(senderUserID)
                                            .removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @SuppressLint("SetTextI18n")
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        btn_send_friend_request.setEnabled(true);
                                                        CURRENT_STATE = "not_friends";
                                                        btn_send_friend_request.setText("Send Friend Request");
                                                        btn_send_friend_request.setBackgroundResource(R.color.colorPrimaryDark);
                                                        btn_decline_friendRequest.setVisibility(View.INVISIBLE);
                                                        btn_decline_friendRequest.setEnabled(false);
                                                    }
                                                }
                                            });
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void MaintenanceOfButtons() {
        try {
            friendRequestRef.child(senderUserID)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(receiverUserID)) {
                                try {
                                    String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();
                                    if (request_type.equals("sent")) {
                                        CURRENT_STATE = "request_sent";
                                        btn_send_friend_request.setText("Cancel Friend Request");
                                        btn_send_friend_request.setBackgroundResource(R.color.errorColor);

                                        btn_decline_friendRequest.setVisibility(View.INVISIBLE);
                                        btn_decline_friendRequest.setEnabled(false);
                                    } else if (request_type.equals("received")) {
                                        CURRENT_STATE = "request_received";
                                        btn_send_friend_request.setText("Accept Friend Request");
                                        btn_send_friend_request.setBackgroundResource(R.color.colorPrimaryDark);
                                        btn_decline_friendRequest.setVisibility(View.VISIBLE);
                                        btn_decline_friendRequest.setEnabled(true);

                                        btn_decline_friendRequest.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CancelFriendRequest();
                                            }
                                        });
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            } else {
                                FriendsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild(receiverUserID)) {
                                            CURRENT_STATE = "friends";
                                            btn_send_friend_request.setText("Unfriend this person");
                                            btn_send_friend_request.setBackgroundResource(R.color.errorColor);
                                            btn_decline_friendRequest.setVisibility(View.INVISIBLE);
                                            btn_decline_friendRequest.setEnabled(false);
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
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void DeleteMessagesIfNotFriends() {

        try {
            RootRef.child("Messages").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {

                        try {
                            msgRef.child(senderUserID).child(receiverUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                try {
                                                    msgRef.child(receiverUserID).child(senderUserID)
                                                            .removeValue();
                                                }catch (Exception e){
                                                    e.printStackTrace();
                                                }
                                            }
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
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void SendFriendRequestToAPerson() {
        notify = true;
        try {
            friendRequestRef.child(senderUserID).child(receiverUserID)
                    .child("request_type").setValue("sent")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                try {
                                    friendRequestRef.child(receiverUserID).child(senderUserID)
                                            .child("request_type").setValue("received")
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()) {
                                                        HashMap<String, String> notificationsData = new HashMap<String, String>();
                                                        notificationsData.put("from", senderUserID);
                                                        notificationsData.put("type", "request");

                                                        try {
                                                            NotificationsRef.child(receiverUserID).push().setValue(notificationsData)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @SuppressLint("SetTextI18n")
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            if (task.isSuccessful()) {
                                                                                try {
                                                                                    btn_send_friend_request.setEnabled(true);
                                                                                    CURRENT_STATE = "request_sent";
                                                                                    btn_send_friend_request.setText("Cancel Friend Request");
                                                                                    btn_send_friend_request.setBackgroundResource(R.color.errorColor);
                                                                                    btn_decline_friendRequest.setVisibility(View.INVISIBLE);
                                                                                    btn_decline_friendRequest.setEnabled(false);

                                                                                    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users")
                                                                                            .child(senderUserID);
                                                                                    usersRef.addValueEventListener(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                            if (dataSnapshot.exists()) {
                                                                                                if (dataSnapshot.hasChildren()) {
                                                                                                    try {
                                                                                                        String messageReceiverFullName = dataSnapshot.child("fullname").getValue().toString();
                                                                                                        String senderName = dataSnapshot.child("username").getValue().toString();

                                                                                                        if (notify) {
                                                                                                            sendNotification(senderName + " sent you a friend request",
                                                                                                                    senderName, "friendrequest", receiverUserID,
                                                                                                                    "", messageReceiverFullName);
                                                                                                        }
                                                                                                        notify = false;
                                                                                                    } catch (Exception e) {
                                                                                                        e.printStackTrace();
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                        }
                                                                                    });
                                                                                } catch (Exception e) {
                                                                                    e.printStackTrace();
                                                                                }
                                                                            }

                                                                        }
                                                                    });

                                                        }catch (Exception e){
                                                            e.printStackTrace();
                                                        }

                                                    }
                                                }
                                            });
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }
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