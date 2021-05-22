package niwigh.com.smartchat.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import niwigh.com.smartchat.Adapter.GroupMessagesAdapter;
import niwigh.com.smartchat.Helper.GroupRecyclerItemTouchHelper;
import niwigh.com.smartchat.Model.MessagesModel;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;
import niwigh.com.smartchat.core.ImageCompressTask;
import niwigh.com.smartchat.listeners.IImageCompressTaskListener;


public class GroupMessagingArea extends AppCompatActivity implements
        GroupRecyclerItemTouchHelper.GroupRecyclerItemTouchHelperListener  {

    EmojiconEditText add_message_edit_text;
    ImageButton btn_send_message;
    ImageButton smiley_btn;
    EmojIconActions emojIconActions;
    String  messageSenderID, saveCurrentDate, saveCurrentTime;
    DatabaseReference RootRef, usersRef;
    RecyclerView All_messages_recycler_view;
    final List<MessagesModel> messagesModelList = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    GroupMessagesAdapter messagesAdapter;
    ImageView sent_icon,msg_type_img_icon;
    RelativeLayout relativeLayout,chatter_date_layout;
    ImageButton more_options_btn,close_btn;
    FirebaseFirestore firebaseFirestore;
    boolean notify = false;
    LinearLayout vertical_line;
    TextView chatter_user_name,chatter_text;




    Toolbar toolbar;
    DatabaseReference groupsRef;
    FirebaseAuth mAuth;
    String currentUserID;
    TextView groupName;
    CircleImageView groupImage;
    View rootView;
    LinearLayout only_admins_layout,sending_tools_layout;

    String replyMessage,replyMessageType,replyFrom,
            replyMessagePositionInRecyclerview,replyfrom,replyFromId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messaging_area);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        only_admins_layout = findViewById(R.id.only_admins_layout);
//        sending_tools_layout = findViewById(R.id.sending_tools_layout);


        String group_category = getIntent().getStringExtra("group_cat");
        String group_key = getIntent().getStringExtra("group_key");
        final String group_image = getIntent().getStringExtra("group_image");
        groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_category)
                .child("groups").child(group_key);
        groupsRef.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_category)
                .child("groups").child(group_key);

        RootRef.keepSynced(true);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);
        firebaseFirestore = FirebaseFirestore.getInstance();

        add_message_edit_text = findViewById(R.id.add_message_edit_text);
        smiley_btn = findViewById(R.id.smiley_btn);
        btn_send_message = findViewById(R.id.send_message_btn);
        more_options_btn = findViewById(R.id.more_options_btn);

        SharedPreferences preferences = getSharedPreferences("MESSAGING_AREA_BG", Context.MODE_PRIVATE);

        toolbar = findViewById(R.id.add_new_post_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View action_bar_view = layoutInflater.inflate(R.layout.group_custom_bar_layout, null);
        groupName = action_bar_view.findViewById(R.id.group_name);
        groupImage = action_bar_view.findViewById(R.id.group_image);

        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChildren()){
                        try {
                            final String group_Name = dataSnapshot.child("groupname").getValue().toString();
                            final String date_created = dataSnapshot.child("datecreated").getValue().toString();
                            final String time_created = dataSnapshot.child("timecreated").getValue().toString();
                            final String group_description = dataSnapshot.child("groupdescription").getValue().toString();
                            final boolean couldjoin = (boolean) dataSnapshot.child("couldjoin").getValue();

                            groupName.setText(group_Name);

                            action_bar_view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    try {
                                        String group_category = getIntent().getStringExtra("group_cat");
                                        String group_key = getIntent().getStringExtra("group_key");
                                        final String group_image = getIntent().getStringExtra("group_image");
                                        Intent groupInfoIntent = new Intent(GroupMessagingArea.this, GroupInfo.class);
                                        groupInfoIntent.putExtra("group_image", group_image);
                                        groupInfoIntent.putExtra("group_key", group_key);
                                        groupInfoIntent.putExtra("group_cat", group_category);
                                        groupInfoIntent.putExtra("date_created", date_created);
                                        groupInfoIntent.putExtra("time_created", time_created);
                                        groupInfoIntent.putExtra("group_name", group_Name);
                                        groupInfoIntent.putExtra("group_description", group_description);
                                        groupInfoIntent.putExtra("couldjoin", couldjoin);
                                        startActivity(groupInfoIntent);
                                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
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

        try {
            Picasso.with(GroupMessagingArea.this)
                    .load(group_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.easy_to_use)
                    .into(groupImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            try {
                                Picasso.with(GroupMessagingArea.this).load(group_image).placeholder(R.drawable.easy_to_use).into(groupImage);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }

        getSupportActionBar().setCustomView(action_bar_view);

        rootView = findViewById(R.id.messaging_area_layout);
        if(preferences.contains("imagefromgallery")){

            try {
                Picasso.with(GroupMessagingArea.this).
                        load(Uri.parse(preferences.getString("imagefromgallery", null))).into(new Target() {

                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        rootView.setBackground(new BitmapDrawable(getResources(), bitmap));
                    }

                    @Override
                    public void onBitmapFailed(final Drawable errorDrawable) {
                        Log.d("TAG", "FAILED");
                        Toast.makeText(GroupMessagingArea.this, "Couldn't set image as background!",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPrepareLoad(final Drawable placeHolderDrawable) {
                        Log.d("TAG", "Prepare Load");
                    }
                });

            }catch (Exception e){
                e.printStackTrace();
            }

        }else{
            if(preferences.contains(("backgroundId"))){
                rootView.setBackgroundResource(preferences.getInt("backgroundId", 0));

            }
        }

        more_options_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetDialogFragment();

            }
        });

        emojIconActions = new EmojIconActions(this,rootView, add_message_edit_text, smiley_btn, "#FF2CA96F","#e8e8e8","#f4f4f4");
        emojIconActions.ShowEmojIcon();
        // use this to change the default toggle icon
        emojIconActions.setIconsIds(R.drawable.ic_keyboard,R.drawable.ic_smiley);
        add_message_edit_text.setEmojiconSize(35);
        emojIconActions.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {


            }

            @Override
            public void onKeyboardClose() {

            }
        });


        btn_send_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                if((chatter_date_layout.getVisibility() == View.VISIBLE)){
                    SendReplyMessage();
                }else{
                    SendMessage();
                }
            }
        });


        chatter_date_layout = findViewById(R.id.chatter_date_layout);
        close_btn = findViewById(R.id.close_btn);
        chatter_user_name = findViewById(R.id.chatter_user_name);
        chatter_text = findViewById(R.id.chatter_text);
        msg_type_img_icon = findViewById(R.id.msg_type_img_icon);
        vertical_line = findViewById(R.id.vertical_line);

        close_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatter_date_layout.setVisibility(View.GONE);
            }
        });

        messagesAdapter = new GroupMessagesAdapter(messagesModelList,group_category,group_key, new GroupMessagesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MessagesModel item, int position) {
                if(item.getReplymessagetypeformat() != null){
                    try {
                        All_messages_recycler_view.
                                smoothScrollToPosition(Integer.valueOf(item.getReplymessagepositioninrecyclerview()));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        All_messages_recycler_view = findViewById(R.id.messages_recycler_view);
        linearLayoutManager = new LinearLayoutManager(this);
        All_messages_recycler_view.setHasFixedSize(true);
        All_messages_recycler_view.setLayoutManager(linearLayoutManager);
        All_messages_recycler_view.setItemAnimator(new DefaultItemAnimator());
        All_messages_recycler_view.setAdapter(messagesAdapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new
                GroupRecyclerItemTouchHelper(0, ItemTouchHelper.RIGHT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(All_messages_recycler_view);





        relativeLayout = (RelativeLayout)GroupMessagingArea.this.getLayoutInflater().inflate(R.layout.chat_messages_layout, null);

        sent_icon = relativeLayout.findViewById(R.id.icon_sent);
        sent_icon.setVisibility(View.GONE);

        FetchMessages();
    }

    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    private void SendReplyMessage() {

        try {
            final String message = add_message_edit_text.getText().toString();

            if (message.trim().isEmpty()) {
                //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                ViewGroup viewGroup = findViewById(android.R.id.content);

                //then we will inflate the custom alert dialog xml that we created
                View dialogView = LayoutInflater.from(GroupMessagingArea.this).inflate(R.layout.error_dialog,
                        viewGroup, false);


                //Now we need an AlertDialog.Builder object
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupMessagingArea.this);

                //setting the view of the builder to our custom view that we already inflated
                builder.setView(dialogView);

                //finally creating the alert dialog and displaying it
                final AlertDialog alertDialog = builder.create();

                Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
                TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
                TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

                dialog_btn.setText("OK");
                success_title.setText("Error");
                success_text.setText("The message input field can't be empty! Type a message and try again.");

                // if the OK button is clicked, close the success dialog
                dialog_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();

            } else {

                String message_sender_ref = "Messages";

                DatabaseReference user_message_key = RootRef.child("Messages").push();
                String message_push_id = user_message_key.getKey();

                //for date
                Calendar calFordDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                saveCurrentDate = currentDate.format(calFordDate.getTime());
                //for time
                Calendar calFordTime = Calendar.getInstance();
                final SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
                saveCurrentTime = currentTime.format(calFordTime.getTime());

                Map<String, Object> messageTextBody = new HashMap<String, Object>();
                messageTextBody.put("replymessage", replyMessage);
                messageTextBody.put("replymessagetype", replyMessageType);
                messageTextBody.put("replyfrom", replyfrom);
                messageTextBody.put("replyfromid", replyFromId);
                messageTextBody.put("replymessagepositioninrecyclerview", replyMessagePositionInRecyclerview);
                messageTextBody.put("replymessagetypeformat", "reply");
                messageTextBody.put("message", message);
                messageTextBody.put("time", saveCurrentTime);
                messageTextBody.put("date", saveCurrentDate);
                messageTextBody.put("isseen", true);
                messageTextBody.put("type", "text"); //The type of message to send. In this case it is a text message.
                messageTextBody.put("from", messageSenderID);
                messageTextBody.put("messageID", message_push_id);

                Map<String, Object> messageBodyDetails = new HashMap<String, Object>();
                messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);

                RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            add_message_edit_text.setText("");
                            chatter_date_layout.setVisibility(View.GONE);
                        } else {
                            sent_icon.setVisibility(View.GONE);
                            add_message_edit_text.setText("");
                            String erromessage = task.getException().getMessage();

                            // {...........................
                            //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                            ViewGroup viewGroup = findViewById(android.R.id.content);

                            //then we will inflate the custom alert dialog xml that we created
                            View dialogView = LayoutInflater.from(GroupMessagingArea.this).inflate(R.layout.error_dialog, viewGroup, false);


                            //Now we need an AlertDialog.Builder object
                            AlertDialog.Builder builder = new AlertDialog.Builder(GroupMessagingArea.this);

                            //setting the view of the builder to our custom view that we already inflated
                            builder.setView(dialogView);

                            //finally creating the alert dialog and displaying it
                            final AlertDialog alertDialog = builder.create();

                            Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
                            TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
                            TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

                            dialog_btn.setText("OK");
                            success_title.setText("Error");
                            success_text.setText(erromessage);

                            // if the OK button is clicked, close the success dialog
                            dialog_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alertDialog.dismiss();
                                }
                            });

                            alertDialog.show();
                            //...................}


                        }
                    }
                });

                add_message_edit_text.setText("");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void FetchMessages() {
        try {
            RootRef.child("Messages")
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            if (dataSnapshot.exists()) {
                                try {
                                    MessagesModel messagesModel = dataSnapshot.getValue(MessagesModel.class);
                                    messagesModelList.add(messagesModel);
                                    messagesAdapter.notifyDataSetChanged();
                                    //scroll to last position of message when new message is received.
                                    All_messages_recycler_view.smoothScrollToPosition(
                                            All_messages_recycler_view.getAdapter().getItemCount()
                                    );
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }

                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    private void SendMessage() {

        try {
            final String message = add_message_edit_text.getText().toString();


            if (message.trim().isEmpty()) {
                //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                ViewGroup viewGroup = findViewById(android.R.id.content);

                //then we will inflate the custom alert dialog xml that we created
                View dialogView = LayoutInflater.from(GroupMessagingArea.this).inflate(R.layout.error_dialog,
                        viewGroup, false);


                //Now we need an AlertDialog.Builder object
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupMessagingArea.this);

                //setting the view of the builder to our custom view that we already inflated
                builder.setView(dialogView);

                //finally creating the alert dialog and displaying it
                final AlertDialog alertDialog = builder.create();

                Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
                TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
                TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

                dialog_btn.setText("OK");
                success_title.setText("Error");
                success_text.setText("The message input field can't be empty!");

                // if the OK button is clicked, close the success dialog
                dialog_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();

            } else {

                String message_sender_ref = "Messages";

                DatabaseReference user_message_key = RootRef.child("Messages").push();
                String message_push_id = user_message_key.getKey();

                //for date
                Calendar calFordDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                saveCurrentDate = currentDate.format(calFordDate.getTime());
                //for time
                Calendar calFordTime = Calendar.getInstance();
                final SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
                saveCurrentTime = currentTime.format(calFordTime.getTime());

                Map<String, Object> messageTextBody = new HashMap<String, Object>();
                messageTextBody.put("message", message);
                messageTextBody.put("time", saveCurrentTime);
                messageTextBody.put("date", saveCurrentDate);
                messageTextBody.put("isseen", true);
                messageTextBody.put("type", "text"); //The type of message to send. In this case it is a text message.
                messageTextBody.put("from", messageSenderID);
                messageTextBody.put("messageID", message_push_id);

                Map<String, Object> messageBodyDetails = new HashMap<String, Object>();
                messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);

                RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            add_message_edit_text.setText("");
                        } else {
                            sent_icon.setVisibility(View.GONE);
                            add_message_edit_text.setText("");
                            String erromessage = task.getException().getMessage();

                            // {...........................
                            //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                            ViewGroup viewGroup = findViewById(android.R.id.content);

                            //then we will inflate the custom alert dialog xml that we created
                            View dialogView = LayoutInflater.from(GroupMessagingArea.this).inflate(R.layout.error_dialog, viewGroup, false);


                            //Now we need an AlertDialog.Builder object
                            AlertDialog.Builder builder = new AlertDialog.Builder(GroupMessagingArea.this);

                            //setting the view of the builder to our custom view that we already inflated
                            builder.setView(dialogView);

                            //finally creating the alert dialog and displaying it
                            final AlertDialog alertDialog = builder.create();

                            Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
                            TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
                            TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

                            dialog_btn.setText("OK");
                            success_title.setText("Error");
                            success_text.setText(erromessage);

                            // if the OK button is clicked, close the success dialog
                            dialog_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alertDialog.dismiss();
                                }
                            });

                            alertDialog.show();
                            //...................}


                        }
                    }
                });
                add_message_edit_text.setText("");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * callback when recycler view is swiped
     * the reply item will be shown
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {

        try {
            if (viewHolder instanceof GroupMessagesAdapter.MessageViewHolder) {

                messagesAdapter.notifyItemChanged(position);

                replyMessage = messagesModelList.get(position).getMessage();
                replyMessageType = messagesModelList.get(position).getType();
                replyFrom = messagesModelList.get(position).getFrom();
                replyMessagePositionInRecyclerview = String.valueOf(position);
                replyFromId = messagesModelList.get(position).getFrom();

                if (replyMessageType.equals("text")) {
                    chatter_text.setVisibility(View.VISIBLE);
                    chatter_text.setText(replyMessage);
                    msg_type_img_icon.setVisibility(View.GONE);
                }
                if (replyMessageType.equals("document")) {
                    chatter_text.setVisibility(View.GONE);
                    msg_type_img_icon.setVisibility(View.VISIBLE);
                    msg_type_img_icon.setImageResource(R.drawable.ic_document);

                } else if (replyMessageType.equals("image")) {
                    chatter_text.setVisibility(View.GONE);
                    msg_type_img_icon.setVisibility(View.VISIBLE);
                    msg_type_img_icon.setImageResource(R.drawable.ic_image);
                } else if (replyMessageType.equals("video")) {
                    chatter_text.setVisibility(View.GONE);
                    msg_type_img_icon.setVisibility(View.VISIBLE);
                    msg_type_img_icon.setImageResource(R.drawable.ic_videocam);
                } else if (replyMessageType.equals("audio")) {
                    chatter_text.setVisibility(View.GONE);
                    msg_type_img_icon.setVisibility(View.VISIBLE);
                    msg_type_img_icon.setImageResource(R.drawable.ic_music);
                }

                if (replyFrom.equals(messageSenderID)) {
                    chatter_user_name.setText("You");
                    chatter_user_name.setTextColor(getResources()
                            .getColor(R.color.colorPrimaryDark));
                    vertical_line.setBackgroundColor(getResources()
                            .getColor(R.color.colorPrimaryDark));
                    replyfrom = "You";
                } else {

                    usersRef.child(replyFrom).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String userName = dataSnapshot.child("fullname").getValue().toString();
                                chatter_user_name.setText(userName);
                                replyfrom = userName;
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    chatter_user_name.setTextColor(getResources()
                            .getColor(R.color.orange));
                    vertical_line.setBackgroundColor(getResources()
                            .getColor(R.color.orange));
                }

                chatter_date_layout.setVisibility(View.VISIBLE);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void checkGroupStatus(){
        try {
            String group_category = getIntent().getStringExtra("group_cat");
            String group_key = getIntent().getStringExtra("group_key");
            final DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_category)
                    .child("groups").child(group_key);

            groupsRef.child("admins").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChild(currentUserID)) {
                            groupsRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        if (dataSnapshot.hasChild("isgroupforadminsonly")) {
                                            only_admins_layout.setVisibility(View.GONE);
//                                        sending_tools_layout.setVisibility(View.VISIBLE);
                                        } else {
                                            only_admins_layout.setVisibility(View.GONE);
//                                        sending_tools_layout.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        } else {
                            groupsRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        if (dataSnapshot.hasChild("isgroupforadminsonly")) {
                                            only_admins_layout.setVisibility(View.VISIBLE);
//                                        sending_tools_layout.setVisibility(View.GONE);
                                        } else {
                                            only_admins_layout.setVisibility(View.GONE);
//                                        sending_tools_layout.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    } else {
//                    sending_tools_layout.setVisibility(View.VISIBLE);
                        only_admins_layout.setVisibility(View.GONE);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_group_background:
                Intent bgIntent = new Intent(GroupMessagingArea.this, MessagingAreaBackground.class);
                startActivity(bgIntent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                return true;
            case R.id.action_group_info:
                TakeToGroupDetails();
                return true;
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void TakeToGroupDetails() {
        try {
            groupsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChildren()) {
                            try {
                                String group_Name = dataSnapshot.child("groupname").getValue().toString();
                                String date_created = dataSnapshot.child("datecreated").getValue().toString();
                                String time_created = dataSnapshot.child("timecreated").getValue().toString();
                                String group_description = dataSnapshot.child("groupdescription").getValue().toString();

                                String group_category = getIntent().getStringExtra("group_cat");
                                String group_key = getIntent().getStringExtra("group_key");
                                final String group_image = getIntent().getStringExtra("group_image");
                                Intent groupInfoIntent = new Intent(GroupMessagingArea.this, GroupInfo.class);
                                groupInfoIntent.putExtra("group_image", group_image);
                                groupInfoIntent.putExtra("group_key", group_key);
                                groupInfoIntent.putExtra("group_cat", group_category);
                                groupInfoIntent.putExtra("date_created", date_created);
                                groupInfoIntent.putExtra("time_created", time_created);
                                groupInfoIntent.putExtra("group_name", group_Name);
                                groupInfoIntent.putExtra("group_description", group_description);
                                startActivity(groupInfoIntent);
                                overridePendingTransition(R.anim.right_in, R.anim.left_out);
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
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkGroupStatus();
        Utilities.getInstance(this).updateUserStatus("Online");
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkGroupStatus();
        Utilities.getInstance(this).updateUserStatus("Online");
        SharedPreferences preferences = getSharedPreferences("MESSAGING_AREA_BG", Context.MODE_PRIVATE);
        if(preferences.contains("imagefromgallery")){

           try{
               Picasso.with(GroupMessagingArea.this).
                       load(Uri.parse(preferences.getString("imagefromgallery",null))).into(new Target(){

                   @Override
                   public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                       rootView.setBackground(new BitmapDrawable(getResources(), bitmap));
                   }

                   @Override
                   public void onBitmapFailed(final Drawable errorDrawable) {
                       Log.d("TAG", "FAILED");
                       Toast.makeText(GroupMessagingArea.this, "Couldn't set image as background!",
                               Toast.LENGTH_SHORT).show();
                   }

                   @Override
                   public void onPrepareLoad(final Drawable placeHolderDrawable) {
                       Log.d("TAG", "Prepare Load");
                   }
               });
           }catch (Exception e){
               e.printStackTrace();
           }

        }else{
            if(preferences.contains(("backgroundId"))){
                rootView.setBackgroundResource(preferences.getInt("backgroundId", 0));

            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
        finish();
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

    public void showBottomSheetDialogFragment() {
        BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }


    public static class BottomSheetFragment extends BottomSheetDialogFragment {
        ImageView send_documents_imagebtn, send_pictures_imagebtn, send_video_message,
                send_audio_imageview;
        private static final int REQUEST_STORAGE_PERMISSION = 100;
        final static int gallery_Pic = 1;
        private StorageTask storageTask;
        private String messageSenderID;
        FirebaseAuth mAuth;
        DatabaseReference RootRef,groupsRef;
        String saveCurrentTime, saveCurrentDate;
        ProgressDialog loadingBar;
        final static int gallery_file_Pic = 7;
        final static int gallery_video_Pic = 27;
        final static int gallery_audio_pic = 8419;

        private ExecutorService mExecutorService = Executors.newFixedThreadPool(1);

        private ImageCompressTask imageCompressTask;

        public BottomSheetFragment() {
            // Required empty public constructor
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            // Inflate the layout for this fragment
            View bottomSheetView = inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false);
            send_documents_imagebtn = bottomSheetView.findViewById(R.id.send_documents_imagebtn);
            send_pictures_imagebtn = bottomSheetView.findViewById(R.id.send_pictures_imagebtn);
            send_audio_imageview = bottomSheetView.findViewById(R.id.send_audio_imageview);
            send_video_message = bottomSheetView.findViewById(R.id.send_video_message);
            mAuth = FirebaseAuth.getInstance();

            try {
                messageSenderID = mAuth.getCurrentUser().getUid();
                String group_category = getActivity().getIntent().getStringExtra("group_cat");
                String group_key = getActivity().getIntent().getStringExtra("group_key");
                groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_category)
                        .child("groups").child(group_key);
                groupsRef.keepSynced(true);

                mAuth = FirebaseAuth.getInstance();
                messageSenderID = mAuth.getCurrentUser().getUid();
                RootRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_category)
                        .child("groups").child(group_key);

                RootRef.keepSynced(true);

                loadingBar = new ProgressDialog(getActivity());

                send_audio_imageview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (PackageManager.PERMISSION_GRANTED !=
                                    ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            REQUEST_STORAGE_PERMISSION);
                                } else {
                                    //Yeah! I want both block to do the same thing, you can write your own logic, but this works for me.
                                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            REQUEST_STORAGE_PERMISSION);
                                }
                            } else {
                                //Permission Granted, lets go pick photo
                                selectAudioFromGallery();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });

                send_video_message.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (PackageManager.PERMISSION_GRANTED !=
                                    ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            REQUEST_STORAGE_PERMISSION);
                                } else {
                                    //Yeah! I want both block to do the same thing, you can write your own logic, but this works for me.
                                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            REQUEST_STORAGE_PERMISSION);
                                }
                            } else {
                                //Permission Granted, lets go pick photo
                                selectVideoFromGallery();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
                send_pictures_imagebtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestPermission();
                    }
                });

                send_documents_imagebtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {
                            if (PackageManager.PERMISSION_GRANTED !=
                                    ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            REQUEST_STORAGE_PERMISSION);
                                } else {
                                    //Yeah! I want both block to do the same thing, you can write your own logic, but this works for me.
                                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            REQUEST_STORAGE_PERMISSION);
                                }
                            } else {
                                //Permission Granted, lets go pick photo
                                selectFileFromGallery();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });

            }catch (Exception e){
                e.printStackTrace();
            }

            return bottomSheetView;

        }


        private void selectVideoFromGallery() {
            Intent select_video_intent = new Intent();
            select_video_intent.setAction(Intent.ACTION_GET_CONTENT);
            select_video_intent.setType("video/*");
            startActivityForResult(Intent.createChooser(select_video_intent, "Select video"), gallery_video_Pic);

        }
        private void selectFileFromGallery() {
            Intent gallery_intent = new Intent();
            gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
            gallery_intent.setType("application/*");
            startActivityForResult(Intent.createChooser(gallery_intent, "Select file"), gallery_file_Pic);
        }

        private void requestPermission() {
            try {
                if (PackageManager.PERMISSION_GRANTED !=
                        ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_STORAGE_PERMISSION);
                    } else {
                        //Yeah! I want both block to do the same thing, you can write your own logic, but this works for me.
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_STORAGE_PERMISSION);
                    }
                } else {
                    //Permission Granted, lets go pick photo
                    selectImageFromGallery();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        private void selectImageFromGallery() {

            Intent gallery_intent = new Intent();
            gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
            gallery_intent.setType("image/*");
            startActivityForResult(Intent.createChooser(gallery_intent, "Select image"), gallery_Pic);
        }

        private void selectAudioFromGallery(){
            Intent select_audio_intent = new Intent();
            select_audio_intent.setAction(Intent.ACTION_GET_CONTENT);
            select_audio_intent.setType("audio/*");
            startActivityForResult(Intent.createChooser(select_audio_intent, "Select audio"), gallery_audio_pic);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            try {
                if (requestCode == gallery_Pic && resultCode == RESULT_OK &&
                        data != null) {
                    try {
                        //extract absolute image path from Uri
                        Uri uri = data.getData();
                        Cursor cursor = MediaStore.Images.Media.query(getActivity().getContentResolver(), uri, new String[]{MediaStore.Images.Media.DATA});

                        if (cursor != null && cursor.moveToFirst()) {
                            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

                            //Create ImageCompressTask and execute with Executor.
                            imageCompressTask = new ImageCompressTask(getActivity(), path, iImageCompressTaskListener);

                            mExecutorService.execute(imageCompressTask);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } else if (requestCode == gallery_file_Pic && resultCode == RESULT_OK && data != null) {
                    try {
                        Uri uri = data.getData();
                        String uriString = uri.toString();
                        File file = new File(uriString);
                        String path = file.getAbsolutePath();
                        String displayName = null;
                        if (uriString.startsWith("content://")) {
                            Cursor cursor = null;
                            try {
                                cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
                                if (cursor != null && cursor.moveToFirst()) {
                                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                                }
                            } finally {

                                cursor.close();
                            }
                        } else if (uriString.startsWith("file://")) {
                            displayName = file.getName();

                        }

                        final MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                        String fileNameFromUrlPluseExtension = displayName;
                        String fileExtensionFromUrl = mimeTypeMap.getExtensionFromMimeType(getContext().getContentResolver().getType(uri));

                        SendFileAsMessage(fileNameFromUrlPluseExtension, uri, fileExtensionFromUrl);

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } else if (requestCode == gallery_video_Pic && resultCode == RESULT_OK && data != null) {
                    try {
                        Uri uri = data.getData();

                        String[] proj = {MediaStore.Video.Media.DATA};
                        String result = null;
                        CursorLoader cursorLoader = new CursorLoader(getActivity(), uri, proj, null, null, null);
                        Cursor cursor = cursorLoader.loadInBackground();
                        if (cursor != null) {
                            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                            cursor.moveToFirst();
                            result = cursor.getString(column_index);
                            String fileUrl = result;
                            File filePath = new File(fileUrl);
                            String fileName = filePath.getName();

                            SendVideoAsMessage(fileName, uri);
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }

                } else if (requestCode == gallery_audio_pic && resultCode == RESULT_OK && data != null) {

                    try {
                        Uri uri = data.getData();
                        String uriString = uri.toString();
                        File file = new File(uriString);
                        String displayName = null;
                        if (uriString.startsWith("content://")) {
                            Cursor cursor = null;
                            try {
                                cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
                                if (cursor != null && cursor.moveToFirst()) {
                                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                                }
                            } finally {

                                cursor.close();
                            }
                        } else if (uriString.startsWith("file://")) {
                            displayName = file.getName();

                        }

                        final MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                        String audiofileName = displayName;
                        String audiofileExtensionFromUrl = mimeTypeMap.getExtensionFromMimeType(getContext().getContentResolver().getType(uri));

                        sendAudioAsMessage(audiofileName, audiofileExtensionFromUrl, uri);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        private void sendAudioAsMessage(final String audiofileName, final String audiofileExtensionFromUrl, Uri uri) {
            try {
                //send audio as message
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Messages Audio");

                final String message_sender_ref = "Messages";

                DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderID).push();
                final String message_push_id = user_message_key.getKey();
                final StorageReference filePath = storageReference.child(message_push_id + "." + audiofileExtensionFromUrl);
                filePath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String fileUrl = uri.toString();

                                    //for date
                                    Calendar calFordDate = Calendar.getInstance();
                                    SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                                    saveCurrentDate = currentDate.format(calFordDate.getTime());
                                    //for time
                                    Calendar calFordTime = Calendar.getInstance();
                                    SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
                                    saveCurrentTime = currentTime.format(calFordTime.getTime());

                                    Map messageTextBody = new HashMap();
                                    messageTextBody.put("message", fileUrl);
                                    messageTextBody.put("name", audiofileName);
                                    messageTextBody.put("isseen", true);
                                    messageTextBody.put("time", saveCurrentTime);
                                    messageTextBody.put("extension", audiofileExtensionFromUrl);
                                    messageTextBody.put("date", saveCurrentDate);
                                    messageTextBody.put("type", "audio"); //The type of message to send. In this case it is an audio message.
                                    messageTextBody.put("from", messageSenderID);
                                    messageTextBody.put("messageID", message_push_id);

                                    Map messageBodyDetails = new HashMap();
                                    messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);

                                    RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {

                                            if (task.isSuccessful()) {
                                                loadingBar.dismiss();
                                                Toasty.success(getActivity(), "Audio sent", Toasty.LENGTH_SHORT, true).show();
                                            } else {
                                                String erromessage = task.getException().getMessage();

                                                // {...........................
                                                //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                                                ViewGroup viewGroup = getView().findViewById(android.R.id.content);

                                                //then we will inflate the custom alert dialog xml that we created
                                                View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.error_dialog, viewGroup, false);


                                                //Now we need an AlertDialog.Builder object
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                                                //setting the view of the builder to our custom view that we already inflated
                                                builder.setView(dialogView);

                                                //finally creating the alert dialog and displaying it
                                                final AlertDialog alertDialog = builder.create();

                                                Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
                                                TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
                                                TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

                                                dialog_btn.setText("OK");
                                                success_title.setText("Error");
                                                success_text.setText(erromessage);

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


                                        }
                                    });
                                }
                            });


                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {


                        // {...........................
                        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                        ViewGroup viewGroup = getView().findViewById(android.R.id.content);

                        //then we will inflate the custom alert dialog xml that we created
                        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.error_dialog, viewGroup, false);


                        //Now we need an AlertDialog.Builder object
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        //setting the view of the builder to our custom view that we already inflated
                        builder.setView(dialogView);

                        //finally creating the alert dialog and displaying it
                        final AlertDialog alertDialog = builder.create();

                        Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
                        TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
                        TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

                        dialog_btn.setText("OK");
                        success_title.setText("Error");
                        success_text.setText(e.getMessage());

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
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        loadingBar.setTitle("Sending audio");
                        loadingBar.setMessage(taskSnapshot.getBytesTransferred() / (1024 * 1024) + " / " + taskSnapshot.getTotalByteCount() / (1024 * 1024) + "MB");
                        loadingBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        loadingBar.setProgress((int) progress);
                        loadingBar.show();
                        loadingBar.setCanceledOnTouchOutside(false);
                    }
                });

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        private void SendVideoAsMessage(final String fileName, Uri uri) {
            try {
                //send the video as a message
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Messages Videos");

                final String message_sender_ref = "Messages";

                DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderID).push();
                final String message_push_id = user_message_key.getKey();
                final StorageReference filePath = storageReference.child(message_push_id + ".mp4");
                filePath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String fileUrl = uri.toString();

                                    //for date
                                    Calendar calFordDate = Calendar.getInstance();
                                    SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                                    saveCurrentDate = currentDate.format(calFordDate.getTime());
                                    //for time
                                    Calendar calFordTime = Calendar.getInstance();
                                    SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
                                    saveCurrentTime = currentTime.format(calFordTime.getTime());

                                    Map messageTextBody = new HashMap();
                                    messageTextBody.put("message", fileUrl);
                                    messageTextBody.put("name", fileName);
                                    messageTextBody.put("isseen", true);
                                    messageTextBody.put("time", saveCurrentTime);
                                    messageTextBody.put("extension", "mp4");
                                    messageTextBody.put("date", saveCurrentDate);
                                    messageTextBody.put("type", "video"); //The type of message to send. In this case it is a text message.
                                    messageTextBody.put("from", messageSenderID);
                                    messageTextBody.put("messageID", message_push_id);

                                    Map messageBodyDetails = new HashMap();
                                    messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);

                                    RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {

                                            if (task.isSuccessful()) {
                                                loadingBar.dismiss();
                                                Toasty.success(getActivity(), "Video sent", Toasty.LENGTH_SHORT, true).show();
                                            } else {
                                                String erromessage = task.getException().getMessage();

                                                // {...........................
                                                //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                                                ViewGroup viewGroup = getView().findViewById(android.R.id.content);

                                                //then we will inflate the custom alert dialog xml that we created
                                                View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.error_dialog, viewGroup, false);


                                                //Now we need an AlertDialog.Builder object
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                                                //setting the view of the builder to our custom view that we already inflated
                                                builder.setView(dialogView);

                                                //finally creating the alert dialog and displaying it
                                                final AlertDialog alertDialog = builder.create();

                                                Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
                                                TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
                                                TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

                                                dialog_btn.setText("OK");
                                                success_title.setText("Error");
                                                success_text.setText(erromessage);

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


                                        }
                                    });
                                }
                            });


                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {


                        // {...........................
                        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                        ViewGroup viewGroup = getView().findViewById(android.R.id.content);

                        //then we will inflate the custom alert dialog xml that we created
                        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.error_dialog, viewGroup, false);


                        //Now we need an AlertDialog.Builder object
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        //setting the view of the builder to our custom view that we already inflated
                        builder.setView(dialogView);

                        //finally creating the alert dialog and displaying it
                        final AlertDialog alertDialog = builder.create();

                        Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
                        TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
                        TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

                        dialog_btn.setText("OK");
                        success_title.setText("Error");
                        success_text.setText(e.getMessage());

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
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        loadingBar.setTitle("Sending video");
                        loadingBar.setMessage(taskSnapshot.getBytesTransferred() / (1024 * 1024) + " / " + taskSnapshot.getTotalByteCount() / (1024 * 1024) + "MB");
                        loadingBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        loadingBar.setProgress((int) progress);
                        loadingBar.show();
                        loadingBar.setCanceledOnTouchOutside(false);
                    }
                });


            }catch (Exception e){
                e.printStackTrace();
            }
        }

        private void SendFileAsMessage(final String fileNameFromUrlPluseExtension, Uri uri,
                                       final String fileExtensionFromUrl) {

            try {
                //send the file as a message
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Messages Files");

                final String message_sender_ref = "Messages";

                DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderID).push();
                final String message_push_id = user_message_key.getKey();
                final StorageReference filePath = storageReference.child(message_push_id + "." + fileExtensionFromUrl);
                filePath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri downUrl = uri;
                                    final String fileUrl = downUrl.toString();

                                    //for date
                                    Calendar calFordDate = Calendar.getInstance();
                                    SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                                    saveCurrentDate = currentDate.format(calFordDate.getTime());
                                    //for time
                                    Calendar calFordTime = Calendar.getInstance();
                                    SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
                                    saveCurrentTime = currentTime.format(calFordTime.getTime());

                                    Map messageTextBody = new HashMap();
                                    messageTextBody.put("message", fileUrl);
                                    messageTextBody.put("name", fileNameFromUrlPluseExtension);
                                    messageTextBody.put("time", saveCurrentTime);
                                    messageTextBody.put("isseen", true);
                                    messageTextBody.put("extension", fileExtensionFromUrl);
                                    messageTextBody.put("date", saveCurrentDate);
                                    messageTextBody.put("type", "document"); //The type of message to send. In this case it is a text message.
                                    messageTextBody.put("from", messageSenderID);
                                    messageTextBody.put("messageID", message_push_id);

                                    Map messageBodyDetails = new HashMap();
                                    messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);

                                    RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {

                                            if (task.isSuccessful()) {
                                                loadingBar.dismiss();
                                                Toasty.success(getActivity(), "File sent", Toasty.LENGTH_SHORT, true).show();
                                            } else {
                                                String erromessage = task.getException().getMessage();

                                                // {...........................
                                                //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                                                ViewGroup viewGroup = getView().findViewById(android.R.id.content);

                                                //then we will inflate the custom alert dialog xml that we created
                                                View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.error_dialog, viewGroup, false);


                                                //Now we need an AlertDialog.Builder object
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                                                //setting the view of the builder to our custom view that we already inflated
                                                builder.setView(dialogView);

                                                //finally creating the alert dialog and displaying it
                                                final AlertDialog alertDialog = builder.create();

                                                Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
                                                TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
                                                TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

                                                dialog_btn.setText("OK");
                                                success_title.setText("Error");
                                                success_text.setText(erromessage);

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


                                        }
                                    });
                                }
                            });


                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {


                        // {...........................
                        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                        ViewGroup viewGroup = getView().findViewById(android.R.id.content);

                        //then we will inflate the custom alert dialog xml that we created
                        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.error_dialog, viewGroup, false);


                        //Now we need an AlertDialog.Builder object
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        //setting the view of the builder to our custom view that we already inflated
                        builder.setView(dialogView);

                        //finally creating the alert dialog and displaying it
                        final AlertDialog alertDialog = builder.create();

                        Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
                        TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
                        TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

                        dialog_btn.setText("OK");
                        success_title.setText("Error");
                        success_text.setText(e.getMessage());

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
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        loadingBar.setTitle("Sending file");
                        loadingBar.setMessage(taskSnapshot.getBytesTransferred() / (1024 * 1024) + " / " + taskSnapshot.getTotalByteCount() / (1024 * 1024) + "MB");
                        loadingBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        loadingBar.setProgress((int) progress);
                        loadingBar.show();
                        loadingBar.setCanceledOnTouchOutside(false);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        //image compress task callback
        private IImageCompressTaskListener iImageCompressTaskListener = new IImageCompressTaskListener() {
            @Override
            public void onComplete(List<File> compressed) {
                try {
                    //photo compressed. Yay!

                    //prepare for uploads. Use an Http library like Retrofit, Volley or async-http-client (My favourite)

                    File file = compressed.get(0);
                    Uri compressedImageUri = Uri.fromFile(file);

                    Log.d("ImageCompressor", "New photo size ==> " + file.length()); //log new file size.

                    SendImageAsMessage(compressedImageUri);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable error) {
                //very unlikely, but it might happen on a device with extremely low storage.
                //log it, log.WhatTheFuck?, or show a dialog asking the user to delete some files....etc, etc
                Log.wtf("ImageCompressor", "Error occurred", error);
            }
        };

        private void SendImageAsMessage(Uri compressedImageUri) {

            try {
                //send the image as a message

                loadingBar.setTitle("Sending Image");
                loadingBar.setMessage("a moment please");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Messages Images");

                final String message_sender_ref = "Messages";

                DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderID).push();
                final String message_push_id = user_message_key.getKey();
                final StorageReference filePath = storageReference.child(message_push_id + ".jpg");
                storageTask = filePath.putFile(compressedImageUri);
                storageTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();
                            String imageUrl = downloadUrl.toString();
                            String imageName = filePath.getPath();
                            String imgName = imageName.substring(imageName.lastIndexOf('/') + 1);


                            //for date
                            Calendar calFordDate = Calendar.getInstance();
                            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                            saveCurrentDate = currentDate.format(calFordDate.getTime());
                            //for time
                            Calendar calFordTime = Calendar.getInstance();
                            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
                            //when you figure out a better way of displaying the time, use this on below instead of the one above
                            //SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa-MMMM dd, yyyy");
                            saveCurrentTime = currentTime.format(calFordTime.getTime());

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", imageUrl);
                            messageTextBody.put("name", imgName);
                            messageTextBody.put("isseen", true);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);
                            messageTextBody.put("type", "image"); //The type of message to send. In this case it is a text message.
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("messageID", message_push_id);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(message_sender_ref + "/" + message_push_id, messageTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if (task.isSuccessful()) {
                                        loadingBar.dismiss();
                                        Toasty.success(getActivity(), "Image sent", Toasty.LENGTH_SHORT, true).show();
                                    } else {
                                        String erromessage = task.getException().getMessage();

                                        // {...........................
                                        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                                        ViewGroup viewGroup = getView().findViewById(android.R.id.content);

                                        //then we will inflate the custom alert dialog xml that we created
                                        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.error_dialog, viewGroup, false);


                                        //Now we need an AlertDialog.Builder object
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                                        //setting the view of the builder to our custom view that we already inflated
                                        builder.setView(dialogView);

                                        //finally creating the alert dialog and displaying it
                                        final AlertDialog alertDialog = builder.create();

                                        Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
                                        TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
                                        TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

                                        dialog_btn.setText("OK");
                                        success_title.setText("Error");
                                        success_text.setText(erromessage);

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


                                }
                            });

                        }
                    }
                });

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            try {
                //clean up!
                mExecutorService.shutdown();

                mExecutorService = null;
                imageCompressTask = null;

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}