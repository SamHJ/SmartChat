package niwigh.com.smartchat.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import niwigh.com.smartchat.Activity.FullMessageImageView;
import niwigh.com.smartchat.Activity.FullMessageVideoView;
import niwigh.com.smartchat.Model.MessagesModel;
import niwigh.com.smartchat.R;

public class MessagesAdapter  extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private FirebaseAuth mAuth;
    private DatabaseReference usersDatabaseRef;

    private List<MessagesModel> userMessagesList;
    private final OnItemClickListener listener;
    DatabaseReference usersRef;



    public MessagesAdapter (List<MessagesModel> userMessagesList,OnItemClickListener listener){

        this.userMessagesList = userMessagesList;
        this.listener = listener;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        private TextView senderMessageText, senderMessageTime, receiverMessageText, receiverMessageTime,
                receiver_image_message_time, sender_message_image_time,receiver_audio_title
                ,receiver_video_name, receiver_message_video_time, sender_video_name, sender_message_video_time;
        private CircleImageView receiverProfileImage, receiverImageProfileImage, receiver_profile_file_for_image
                ,receiver_profile_video_for_image, receiver_profile_audio_for_image;
        private LinearLayout receiver_message_layout, sender_message_layout, receiver_image_message_layout, sender_message_image_layout;
        ImageView receiver_image_imageView, sender_image_imageView, sent_icon, sender_icon_sent_image, icon_sent
                ,sender_icon_sent_audio;

        public LinearLayout receiver_message_file_layout, sender_message_file_layout
                , receiver_message_video_layout, sender_message_video_layout;

        ImageView sender_file_imageView, receiver_file_imageView, sender_icon_sent_video, sender_icon_sent_file;
        public TextView sender_message_file_time, receiver_message_file_time, receiver_file_name, sender_file_name;

        VideoView receiver_message_video, sender_message_video;
        ProgressBar receiver_loading_video_progress_bar, sender_loading_video_progress_bar,receiver_progressBar,
                receiver_video_progress,sender_progressBar, sender_video_progress;

        ImageButton receiver_file_download_btn, sender_file_download_btn, receiver_play_btn,sender_play_btn;
        RelativeLayout sender_video_layout,receiver_video_layout;
        LinearLayout receiver_message_audio_layout,sender_audio_layout;
        TextView receiver_counter_timer,receiver_duration_timer,receiver_message_audio_time,sender_audio_title,
                sender_counter_timer, sender_duration_timer, sender_message_audio_time;

        LinearLayout chatter_date_layout, reply_sender_message_layout;
        TextView chatter_date_text,chatter_user_name_text,chatter_text_text,new_sender_message_text,
                new_sender_message_time;
        ImageView msg_type_img_icon_text,new_icon_sent,sender_msg_type_img_icon_text;

        LinearLayout reply_receiver_message_layout_main,sender_vertical_line;
        CircleImageView reply_receiver_profile_image;
        TextView sender_chatter_user_name_text,sender_chatter_text_text,
                new_receiver_message_text,new_receiver_message_time;


        private MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            reply_receiver_message_layout_main = itemView.findViewById(R.id.reply_receiver_message_layout_main);
            reply_receiver_profile_image = itemView.findViewById(R.id.reply_receiver_profile_image);
            sender_vertical_line = itemView.findViewById(R.id.sender_vertical_line);
            sender_chatter_user_name_text = itemView.findViewById(R.id.sender_chatter_user_name_text);
            sender_chatter_text_text = itemView.findViewById(R.id.sender_chatter_text_text);
            sender_msg_type_img_icon_text = itemView.findViewById(R.id.sender_msg_type_img_icon_text);
            new_receiver_message_text = itemView.findViewById(R.id.new_receiver_message_text);
            new_receiver_message_time = itemView.findViewById(R.id.new_receiver_message_time);


            reply_sender_message_layout = itemView.findViewById(R.id.reply_sender_message_layout);
            chatter_user_name_text = itemView.findViewById(R.id.chatter_user_name_text);
            chatter_text_text = itemView.findViewById(R.id.chatter_text_text);
            msg_type_img_icon_text = itemView.findViewById(R.id.msg_type_img_icon_text);
            new_sender_message_text = itemView.findViewById(R.id.new_sender_message_text);
            new_sender_message_time = itemView.findViewById(R.id.new_sender_message_time);
            new_icon_sent = itemView.findViewById(R.id.new_icon_sent);

            chatter_date_layout = itemView.findViewById(R.id.chatter_date_layout);
            chatter_date_text = itemView.findViewById(R.id.chatter_date_text);


            //for audio sender
            sender_audio_layout = itemView.findViewById(R.id.sender_audio_layout);
            sender_progressBar = itemView.findViewById(R.id.sender_progressBar);
            sender_audio_title = itemView.findViewById(R.id.sender_audio_title);
            sender_play_btn = itemView.findViewById(R.id.sender_play_btn);
            sender_counter_timer = itemView.findViewById(R.id.sender_counter_timer);
            sender_video_progress = itemView.findViewById(R.id.sender_video_progress);
            sender_video_progress.setMax(100);
            sender_duration_timer  = itemView.findViewById(R.id.sender_duration_timer);
            sender_message_audio_time = itemView.findViewById(R.id.sender_message_audio_time);
            sender_icon_sent_audio = itemView.findViewById(R.id.sender_icon_sent_audio);


            //for audio receiver
            receiver_message_audio_layout = itemView.findViewById(R.id.receiver_message_audio_layout);
            receiver_profile_audio_for_image = itemView.findViewById(R.id.receiver_profile_audio_for_image);
            receiver_progressBar = itemView.findViewById(R.id.receiver_progressBar);
            receiver_audio_title = itemView.findViewById(R.id.receiver_audio_title);
            receiver_play_btn = itemView.findViewById(R.id.receiver_play_btn);
            receiver_counter_timer = itemView.findViewById(R.id.receiver_counter_timer);
            receiver_video_progress = itemView.findViewById(R.id.receiver_video_progress);
            receiver_video_progress.setMax(100);
            receiver_duration_timer = itemView.findViewById(R.id.receiver_duration_timer);
            receiver_message_audio_time = itemView.findViewById(R.id.receiver_message_audio_time);


            icon_sent = itemView.findViewById(R.id.icon_sent);
            sender_icon_sent_file = itemView.findViewById(R.id.sender_icon_sent_file);
            sender_icon_sent_video = itemView.findViewById(R.id.sender_icon_sent_video);
            sender_icon_sent_image = itemView.findViewById(R.id.sender_icon_sent_image);
            receiver_video_layout = itemView.findViewById(R.id.receiver_video_layout);
            sender_video_layout = itemView.findViewById(R.id.sender_video_layout);
            receiver_message_video_layout = itemView.findViewById(R.id.receiver_message_video_layout);
            receiver_profile_video_for_image = itemView.findViewById(R.id.receiver_profile_video_for_image);
            receiver_message_video = itemView.findViewById(R.id.receiver_message_video);
            receiver_loading_video_progress_bar = itemView.findViewById(R.id.receiver_loading_video_progress_bar);
            receiver_video_name = itemView.findViewById(R.id.receiver_video_name);
            receiver_message_video_time = itemView.findViewById(R.id.receiver_message_video_time);

            sender_message_video_layout = itemView.findViewById(R.id.sender_message_video_layout);
            sender_message_video = itemView.findViewById(R.id.sender_message_video);
            sender_loading_video_progress_bar = itemView.findViewById(R.id.sender_loading_video_progress_bar);
            sender_video_name = itemView.findViewById(R.id.sender_video_name);
            sender_message_video_time = itemView.findViewById(R.id.sender_message_video_time);


            sender_file_download_btn = itemView.findViewById(R.id.sender_file_download_btn);
            receiver_file_download_btn = itemView.findViewById(R.id.receiver_file_download_btn);
            receiver_file_name = itemView.findViewById(R.id.receiver_file_name);
            sender_file_name = itemView.findViewById(R.id.sender_file_name);
            receiver_profile_file_for_image = itemView.findViewById(R.id.receiver_profile_file_for_image);
            sender_file_imageView = itemView.findViewById(R.id.sender_file_imageView);
            receiver_file_imageView = itemView.findViewById(R.id.receiver_file_imageView);
            sender_message_file_time = itemView.findViewById(R.id.sender_message_file_time);
            receiver_message_file_time = itemView.findViewById(R.id.receiver_message_file_time);

            receiver_message_file_layout = itemView.findViewById(R.id.receiver_message_file_layout);
            sender_message_file_layout = itemView.findViewById(R.id.sender_message_file_layout);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            senderMessageTime = itemView.findViewById(R.id.sender_message_time);
            sender_message_image_time = itemView.findViewById(R.id.sender_message_image_time);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiver_image_message_time = itemView.findViewById(R.id.receiver_message_image_time);
            receiverMessageTime = itemView.findViewById(R.id.receiver_message_time);
            receiverProfileImage = itemView.findViewById(R.id.receiver_profile_image);
            receiverImageProfileImage = itemView.findViewById(R.id.receiver_profile_image_for_image);
            receiver_image_imageView = itemView.findViewById(R.id.receiver_image_imageView);
            sender_image_imageView = itemView.findViewById(R.id.sender_image_imageView);

            receiver_message_layout = itemView.findViewById(R.id.receiver_message_layout);
            receiver_image_message_layout = itemView.findViewById(R.id.receiver_message_image_layout);
            sender_message_layout = itemView.findViewById(R.id.sender_message_layout);
            sender_message_image_layout = itemView.findViewById(R.id.sender_message_image_layout);

            sent_icon = itemView.findViewById(R.id.icon_sent);
            sender_image_imageView.setClipToOutline(true);
            receiver_image_imageView.setClipToOutline(true);
            sender_file_imageView.setClipToOutline(true);
            receiver_file_imageView.setClipToOutline(true);
            sender_message_video.setClipToOutline(true);
            receiver_message_video.setClipToOutline(true);
            sender_video_layout.setClipToOutline(true);
            receiver_video_layout.setClipToOutline(true);
        }


        public void bind(final MessagesModel messagesModel, final OnItemClickListener listener, View itemView,
                         final int position) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(messagesModel,position);
                }
            });
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View V = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.chat_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(V);
    }

    private void processDate(@NonNull TextView tv, LinearLayout ll, String dateAPIStr
            , String dateAPICompareStr
            , boolean isFirstItem) {

        try {
            SimpleDateFormat f = new SimpleDateFormat("dd-MMMM-yyyy");
            if (isFirstItem) {
                //first item always got date/today to shows
                //and overkill to compare with next item flow
                Date dateFromAPI = null;
                try {
                    dateFromAPI = f.parse(dateAPIStr);
                    if (DateUtils.isToday(dateFromAPI.getTime())) tv.setText("Today");
                    else if (DateUtils.isToday(dateFromAPI.getTime() + DateUtils.DAY_IN_MILLIS))
                        tv.setText("Yesterday");
                    else tv.setText(dateAPIStr);
                    tv.setIncludeFontPadding(false);
                    tv.setVisibility(View.VISIBLE);
                    ll.setVisibility(View.VISIBLE);
                } catch (ParseException e) {
                    e.printStackTrace();
                    tv.setVisibility(View.GONE);
                    ll.setVisibility(View.GONE);
                }
            } else {
                if (!dateAPIStr.equalsIgnoreCase(dateAPICompareStr)) {
                    try {
                        Date dateFromAPI = f.parse(dateAPIStr);
                        if (DateUtils.isToday(dateFromAPI.getTime())) tv.setText("Today");
                        else if (DateUtils.isToday(dateFromAPI.getTime() + DateUtils.DAY_IN_MILLIS))
                            tv.setText("Yesterday");
                        else tv.setText(dateAPIStr);
                        tv.setIncludeFontPadding(false);
                        tv.setVisibility(View.VISIBLE);
                        ll.setVisibility(View.VISIBLE);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        tv.setVisibility(View.GONE);
                        ll.setVisibility(View.GONE);
                    }
                } else {
                    tv.setVisibility(View.GONE);
                    ll.setVisibility(View.GONE);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @SuppressLint({"SimpleDateFormat", "SetTextI18n","RecyclerView"})
    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder,final int position) {

        try {


            final MessagesModel messagesModel = userMessagesList.get(position);
            messageViewHolder.bind(messagesModel, listener, messageViewHolder.itemView, position);
            String messageSenderID = mAuth.getCurrentUser().getUid();

            usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
            usersRef.keepSynced(true);

            final Activity activity = (Activity) messageViewHolder.itemView.getContext();

            String fromUserID = messagesModel.getFrom();
            String fromMessageType = messagesModel.getType();
            final MediaController[] mediaController = new MediaController[1];
            final MediaController[] receiverMediaController = new MediaController[1];
            final MediaPlayer sender_media_player;
            final MediaPlayer receiver_media_player;


            if (position != 0) {
                processDate(messageViewHolder.chatter_date_text,
                        messageViewHolder.chatter_date_layout, messagesModel.getDate()
                        , this.userMessagesList.get(position - 1).getDate()
                        , false)
                ;
            } else {
                processDate(messageViewHolder.chatter_date_text, messageViewHolder.chatter_date_layout,
                        messagesModel.getDate()
                        , null
                        , true)
                ;
            }


            usersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
            usersDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        final String image = dataSnapshot.child("profileimage").getValue().toString();
                        try {
                            Picasso.with(messageViewHolder.itemView.getContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.easy_to_use).into(messageViewHolder.receiverProfileImage,
                                    new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {

                                            Picasso.with(messageViewHolder.itemView.getContext()).load(image)
                                                    .placeholder(R.drawable.easy_to_use).into(messageViewHolder.receiverProfileImage);
                                        }
                                    });

                            Picasso.with(messageViewHolder.itemView.getContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.easy_to_use).into(messageViewHolder.receiverImageProfileImage,
                                    new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {

                                            Picasso.with(messageViewHolder.itemView.getContext()).load(image)
                                                    .placeholder(R.drawable.easy_to_use).into(messageViewHolder.receiverImageProfileImage);
                                        }
                                    });

                            Picasso.with(messageViewHolder.itemView.getContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.easy_to_use).into(messageViewHolder.receiver_profile_file_for_image,
                                    new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {

                                            Picasso.with(messageViewHolder.itemView.getContext()).load(image)
                                                    .placeholder(R.drawable.easy_to_use)
                                                    .into(messageViewHolder.receiver_profile_file_for_image);
                                        }
                                    });


                            Picasso.with(messageViewHolder.itemView.getContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.easy_to_use).into(messageViewHolder.receiver_profile_audio_for_image,
                                    new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {

                                            Picasso.with(messageViewHolder.itemView.getContext()).load(image)
                                                    .placeholder(R.drawable.easy_to_use).into(messageViewHolder.receiver_profile_audio_for_image);
                                        }
                                    });

                            Picasso.with(messageViewHolder.itemView.getContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.easy_to_use).into(messageViewHolder.reply_receiver_profile_image,
                                    new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {

                                            Picasso.with(messageViewHolder.itemView.getContext()).load(image)
                                                    .placeholder(R.drawable.easy_to_use).into(messageViewHolder.reply_receiver_profile_image);
                                        }
                                    });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            usersDatabaseRef.keepSynced(true);

            //for sender reply message text
            messageViewHolder.reply_sender_message_layout.setVisibility(View.GONE);
            messageViewHolder.chatter_user_name_text.setVisibility(View.GONE);
            messageViewHolder.chatter_text_text.setVisibility(View.GONE);
            messageViewHolder.msg_type_img_icon_text.setVisibility(View.GONE);
            messageViewHolder.new_sender_message_text.setVisibility(View.GONE);
            messageViewHolder.new_sender_message_time.setVisibility(View.GONE);
            messageViewHolder.new_icon_sent.setVisibility(View.GONE);

            //for receiver reply message text
            messageViewHolder.reply_receiver_message_layout_main.setVisibility(View.GONE);
            messageViewHolder.reply_receiver_profile_image.setVisibility(View.GONE);
            messageViewHolder.sender_vertical_line.setVisibility(View.GONE);
            messageViewHolder.sender_chatter_user_name_text.setVisibility(View.GONE);
            messageViewHolder.sender_chatter_text_text.setVisibility(View.GONE);
            messageViewHolder.sender_msg_type_img_icon_text.setVisibility(View.GONE);
            messageViewHolder.new_receiver_message_text.setVisibility(View.GONE);
            messageViewHolder.new_receiver_message_time.setVisibility(View.GONE);

            //for audio sender
            messageViewHolder.sender_audio_layout.setVisibility(View.GONE);
            messageViewHolder.sender_progressBar.setVisibility(View.GONE);
            messageViewHolder.sender_audio_title.setVisibility(View.GONE);
            messageViewHolder.sender_play_btn.setVisibility(View.GONE);
            messageViewHolder.sender_counter_timer.setVisibility(View.GONE);
            messageViewHolder.sender_video_progress.setVisibility(View.GONE);
            messageViewHolder.sender_duration_timer.setVisibility(View.GONE);
            messageViewHolder.sender_message_audio_time.setVisibility(View.GONE);
            messageViewHolder.sender_icon_sent_audio.setVisibility(View.GONE);


            //for audio receiver
            messageViewHolder.receiver_message_audio_layout.setVisibility(View.GONE);
            messageViewHolder.receiver_profile_audio_for_image.setVisibility(View.GONE);
            messageViewHolder.receiver_progressBar.setVisibility(View.GONE);
            messageViewHolder.receiver_audio_title.setVisibility(View.GONE);
            messageViewHolder.receiver_play_btn.setVisibility(View.GONE);
            messageViewHolder.receiver_counter_timer.setVisibility(View.GONE);
            messageViewHolder.receiver_video_progress.setVisibility(View.GONE);
            messageViewHolder.receiver_duration_timer.setVisibility(View.GONE);
            messageViewHolder.receiver_message_audio_time.setVisibility(View.GONE);


            //for text message
            messageViewHolder.receiverMessageText.setVisibility(View.GONE);
            messageViewHolder.receiverMessageTime.setVisibility(View.GONE);
            messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
            messageViewHolder.receiver_message_layout.setVisibility(View.GONE);
            //for image message
            messageViewHolder.receiverImageProfileImage.setVisibility(View.GONE);
            messageViewHolder.receiver_image_imageView.setVisibility(View.GONE);
            messageViewHolder.receiver_image_message_time.setVisibility(View.GONE);
            messageViewHolder.receiver_image_message_layout.setVisibility(View.GONE);

            //for text message
            messageViewHolder.icon_sent.setVisibility(View.GONE);
            messageViewHolder.sender_icon_sent_image.setVisibility(View.GONE);
            messageViewHolder.senderMessageText.setVisibility(View.GONE);
            messageViewHolder.senderMessageTime.setVisibility(View.GONE);
            messageViewHolder.sender_message_layout.setVisibility(View.GONE);
            //for image message
            messageViewHolder.sender_image_imageView.setVisibility(View.GONE);
            messageViewHolder.sender_message_image_time.setVisibility(View.GONE);
            messageViewHolder.sender_message_image_layout.setVisibility(View.GONE);


            //for sender file message
            messageViewHolder.sender_icon_sent_file.setVisibility(View.GONE);
            messageViewHolder.sender_file_download_btn.setVisibility(View.GONE);
            messageViewHolder.sender_message_file_layout.setVisibility(View.GONE);
            messageViewHolder.sender_file_imageView.setVisibility(View.GONE);
            messageViewHolder.sender_message_file_time.setVisibility(View.GONE);
            messageViewHolder.sender_file_name.setVisibility(View.GONE);

//        ,

            //for receiver file message
            messageViewHolder.receiver_file_download_btn.setVisibility(View.GONE);
            messageViewHolder.receiver_profile_file_for_image.setVisibility(View.GONE);
            messageViewHolder.receiver_message_file_layout.setVisibility(View.GONE);
            messageViewHolder.receiver_file_imageView.setVisibility(View.GONE);
            messageViewHolder.receiver_message_file_time.setVisibility(View.GONE);
            messageViewHolder.receiver_file_name.setVisibility(View.GONE);


            //for receiver_message_video
            messageViewHolder.receiver_message_video_layout.setVisibility(View.GONE);
            messageViewHolder.receiver_profile_video_for_image.setVisibility(View.GONE);
            messageViewHolder.receiver_message_video.setVisibility(View.GONE);
            messageViewHolder.receiver_loading_video_progress_bar.setVisibility(View.GONE);
            messageViewHolder.receiver_video_name.setVisibility(View.GONE);
            messageViewHolder.receiver_message_video_time.setVisibility(View.GONE);

            //for sender_message_video
            messageViewHolder.sender_icon_sent_video.setVisibility(View.GONE);
            messageViewHolder.sender_message_video_layout.setVisibility(View.GONE);
            messageViewHolder.sender_message_video.setVisibility(View.GONE);
            messageViewHolder.sender_loading_video_progress_bar.setVisibility(View.GONE);
            messageViewHolder.sender_video_name.setVisibility(View.GONE);
            messageViewHolder.sender_message_video_time.setVisibility(View.GONE);

            if (messagesModel.getReplymessagetype() == null) {


                if (fromMessageType.equals("text")) {
                    try {

                        if (fromUserID.equals(messageSenderID)) {
                            messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                            messageViewHolder.senderMessageTime.setVisibility(View.VISIBLE);
                            messageViewHolder.sender_message_layout.setVisibility(View.VISIBLE);
                            messageViewHolder.icon_sent.setVisibility(View.VISIBLE);

                            if (messagesModel.isIsseen()) {
                                messageViewHolder.icon_sent.setImageResource(R.drawable.ic_sent);
                            } else {
                                messageViewHolder.icon_sent.setImageResource(R.drawable.ic_delivered);
                            }

                            messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_message_text_background);
                            messageViewHolder.senderMessageText.setTextColor(Color.WHITE);
                            messageViewHolder.senderMessageText.setGravity(Gravity.START);
                            messageViewHolder.senderMessageText.setText(Html.fromHtml(messagesModel.getMessage()));
                            messageViewHolder.senderMessageTime.setText(messagesModel.getTime());
                        } else {
                            messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                            messageViewHolder.receiverMessageTime.setVisibility(View.VISIBLE);
                            messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                            messageViewHolder.receiver_message_layout.setVisibility(View.VISIBLE);

                            messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_text_background);
                            messageViewHolder.receiverMessageText.setGravity(Gravity.START);
                            messageViewHolder.receiverMessageText.setText(Html.fromHtml(messagesModel.getMessage()));
                            messageViewHolder.receiverMessageTime.setText(messagesModel.getTime());
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } else if (fromMessageType.equals("image")) {

                    if (fromUserID.equals(messageSenderID)) {
                        try {
                            messageViewHolder.sender_image_imageView.setVisibility(View.VISIBLE);
                            messageViewHolder.sender_message_image_time.setVisibility(View.VISIBLE);
                            messageViewHolder.sender_message_image_layout.setVisibility(View.VISIBLE);
                            messageViewHolder.sender_icon_sent_image.setVisibility(View.VISIBLE);

                            if (messagesModel.isIsseen()) {
                                messageViewHolder.sender_icon_sent_image.setImageResource(R.drawable.ic_sent);
                            } else {
                                messageViewHolder.sender_icon_sent_image.setImageResource(R.drawable.ic_delivered);
                            }

                            try {
                                Picasso.with(messageViewHolder.sender_image_imageView.getContext()).load(messagesModel.getMessage()).networkPolicy(NetworkPolicy.OFFLINE)
                                        .placeholder(R.drawable.easy_to_use).into(messageViewHolder.sender_image_imageView, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        try {
                                            Picasso.with(messageViewHolder.sender_image_imageView.getContext()).load(messagesModel.getMessage()).placeholder(R.drawable.easy_to_use)
                                                    .into(messageViewHolder.sender_image_imageView);
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            messageViewHolder.sender_message_image_time.setText(messagesModel.getTime());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            messageViewHolder.receiverImageProfileImage.setVisibility(View.VISIBLE);
                            messageViewHolder.receiver_image_imageView.setVisibility(View.VISIBLE);
                            messageViewHolder.receiver_image_message_time.setVisibility(View.VISIBLE);
                            messageViewHolder.receiver_image_message_layout.setVisibility(View.VISIBLE);

                            try {
                                Picasso.with(messageViewHolder.receiver_image_imageView.getContext()).load(messagesModel.getMessage()).networkPolicy(NetworkPolicy.OFFLINE)
                                        .placeholder(R.drawable.easy_to_use).into(messageViewHolder.receiver_image_imageView, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        try {
                                            Picasso.with(messageViewHolder.receiver_image_imageView.getContext()).load(messagesModel.getMessage()).placeholder(R.drawable.easy_to_use)
                                                    .into(messageViewHolder.receiver_image_imageView);
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            messageViewHolder.receiver_image_message_time.setText(messagesModel.getTime());
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                } else if (fromMessageType.equals("document")) {
                    if (fromUserID.equals(messageSenderID)) {

                        messageViewHolder.sender_file_download_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    File root = Environment.getExternalStorageDirectory();
                                    root.mkdirs();
                                    String path = root.toString();


                                    DownloadManager downloadManager = (DownloadManager) messageViewHolder.itemView.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                                    Uri uri1 = Uri.parse(userMessagesList.get(position).getMessage());
                                    DownloadManager.Request request = new DownloadManager.Request(uri1);
                                    request.setTitle("SmartChat (" + messagesModel.getName() + ")");
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    request.setDestinationInExternalFilesDir(messageViewHolder.itemView.getContext(), path + "/SmartChat" + "/Messages" + "/Documents", messagesModel.getName());
                                    downloadManager.enqueue(request);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                        messageViewHolder.sender_file_download_btn.setVisibility(View.VISIBLE);
                        messageViewHolder.sender_message_file_layout.setVisibility(View.VISIBLE);
                        messageViewHolder.sender_file_imageView.setVisibility(View.VISIBLE);
                        messageViewHolder.sender_message_file_time.setVisibility(View.VISIBLE);
                        messageViewHolder.sender_file_name.setVisibility(View.VISIBLE);
                        messageViewHolder.sender_message_file_time.setText(messagesModel.getTime());
                        messageViewHolder.sender_file_name.setText(messagesModel.getName());
                        messageViewHolder.sender_icon_sent_file.setVisibility(View.VISIBLE);

                        if (messagesModel.isIsseen()) {
                            messageViewHolder.sender_icon_sent_file.setImageResource(R.drawable.ic_sent);
                        } else {
                            messageViewHolder.sender_icon_sent_file.setImageResource(R.drawable.ic_delivered);
                        }

                        try {
                            Picasso.with(messageViewHolder.sender_file_imageView.getContext()).load(R.drawable.document).networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.document).into(messageViewHolder.sender_file_imageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    try {
                                        Picasso.with(messageViewHolder.sender_file_imageView.getContext()).load(R.drawable.document).placeholder(R.drawable.document)
                                                .into(messageViewHolder.sender_file_imageView);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {


                        messageViewHolder.receiver_file_download_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    File root = Environment.getExternalStorageDirectory();
                                    root.mkdirs();
                                    String path = root.toString();


                                    DownloadManager downloadManager = (DownloadManager) messageViewHolder.itemView.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                                    Uri uri1 = Uri.parse(userMessagesList.get(position).getMessage());
                                    DownloadManager.Request request = new DownloadManager.Request(uri1);
                                    request.setTitle("SmartChat (" + messagesModel.getName() + ")");
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    request.setDestinationInExternalFilesDir(messageViewHolder.itemView.getContext(), path + "/SmartChat" + "/Messages" + "/Documents", messagesModel.getName());
                                    downloadManager.enqueue(request);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                        messageViewHolder.receiver_file_download_btn.setVisibility(View.VISIBLE);
                        messageViewHolder.receiver_profile_file_for_image.setVisibility(View.VISIBLE);
                        messageViewHolder.receiver_message_file_layout.setVisibility(View.VISIBLE);
                        messageViewHolder.receiver_file_imageView.setVisibility(View.VISIBLE);
                        messageViewHolder.receiver_message_file_time.setVisibility(View.VISIBLE);
                        messageViewHolder.receiver_file_name.setVisibility(View.VISIBLE);
                        messageViewHolder.receiver_message_file_time.setText(messagesModel.getTime());
                        messageViewHolder.receiver_file_name.setText(messagesModel.getName());

                        try {
                            Picasso.with(messageViewHolder.receiver_file_imageView.getContext()).load(R.drawable.document).networkPolicy(NetworkPolicy.OFFLINE)
                                    .placeholder(R.drawable.document).into(messageViewHolder.receiver_file_imageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    try {
                                        Picasso.with(messageViewHolder.receiver_file_imageView.getContext()).load(R.drawable.document).placeholder(R.drawable.document)
                                                .into(messageViewHolder.receiver_file_imageView);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (fromMessageType.equals("video")) {
                    if (fromUserID.equals(messageSenderID)) {
                        try {
                            messageViewHolder.sender_icon_sent_video.setVisibility(View.VISIBLE);
                            messageViewHolder.sender_message_video_layout.setVisibility(View.VISIBLE);
                            messageViewHolder.sender_message_video.setVisibility(View.VISIBLE);
                            messageViewHolder.sender_loading_video_progress_bar.setVisibility(View.VISIBLE);
                            messageViewHolder.sender_video_name.setVisibility(View.VISIBLE);
                            messageViewHolder.sender_message_video_time.setVisibility(View.VISIBLE);

                            if (messagesModel.isIsseen()) {
                                messageViewHolder.sender_icon_sent_video.setImageResource(R.drawable.ic_sent);
                            } else {
                                messageViewHolder.sender_icon_sent_video.setImageResource(R.drawable.ic_delivered);
                            }


                            messageViewHolder.sender_message_video_time.setText(messagesModel.getTime());
                            messageViewHolder.sender_video_name.setText(messagesModel.getName());
                            messageViewHolder.sender_message_video.setVideoURI(Uri.parse(messagesModel.getMessage()));
                            messageViewHolder.sender_message_video.requestFocus();
                            messageViewHolder.sender_message_video.setBackgroundColor(messageViewHolder.sender_message_video.getContext().getResources().getColor(R.color.default_video_post_bg));

                            messageViewHolder.sender_message_video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    try {
                                        mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                                            @Override
                                            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                                                mediaController[0] = new MediaController(messageViewHolder.sender_message_video.getContext());
                                                messageViewHolder.sender_message_video.setMediaController(mediaController[0]);
                                                mediaController[0].setAnchorView(messageViewHolder.sender_message_video);
                                            }
                                        });


                                        mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                                            @Override
                                            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                                                if (what == mp.MEDIA_INFO_BUFFERING_END) {
                                                    messageViewHolder.sender_loading_video_progress_bar.setVisibility(View.INVISIBLE);
                                                    return true;
                                                } else if (what == mp.MEDIA_INFO_BUFFERING_START) {
                                                    messageViewHolder.sender_loading_video_progress_bar.setVisibility(View.VISIBLE);
                                                }
                                                return false;
                                            }
                                        });

                                        messageViewHolder.sender_message_video.start();
                                        messageViewHolder.sender_message_video.setBackgroundColor(0);
                                        messageViewHolder.sender_loading_video_progress_bar.setVisibility(View.INVISIBLE);


                                        messageViewHolder.sender_message_video.seekTo(messageViewHolder.sender_message_video.getCurrentPosition());
                                        if (messageViewHolder.sender_message_video.getCurrentPosition() != 0) {
                                            messageViewHolder.sender_message_video.start();

                                        } else {
                                            messageViewHolder.sender_message_video.pause();
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }

                                }
                            });
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    } else {

                        try {

                            messageViewHolder.receiver_message_video_layout.setVisibility(View.VISIBLE);
                            messageViewHolder.receiver_profile_video_for_image.setVisibility(View.VISIBLE);
                            messageViewHolder.receiver_message_video.setVisibility(View.VISIBLE);
                            messageViewHolder.receiver_loading_video_progress_bar.setVisibility(View.VISIBLE);
                            messageViewHolder.receiver_video_name.setVisibility(View.VISIBLE);
                            messageViewHolder.receiver_message_video_time.setVisibility(View.VISIBLE);


                            messageViewHolder.receiver_message_video_time.setText(messagesModel.getTime());
                            messageViewHolder.receiver_video_name.setText(messagesModel.getName());
                            messageViewHolder.receiver_message_video.setVideoURI(Uri.parse(messagesModel.getMessage()));
                            messageViewHolder.receiver_message_video.requestFocus();
                            messageViewHolder.receiver_message_video.setBackgroundColor(messageViewHolder.receiver_message_video.getContext().getResources().getColor(R.color.default_video_post_bg));

                            messageViewHolder.receiver_message_video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {

                                    mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                                        @Override
                                        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                                            receiverMediaController[0] = new MediaController(messageViewHolder.receiver_message_video.getContext());
                                            messageViewHolder.receiver_message_video.setMediaController(receiverMediaController[0]);
                                            receiverMediaController[0].setAnchorView(messageViewHolder.receiver_message_video);
                                        }
                                    });


                                    mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                                        @Override
                                        public boolean onInfo(MediaPlayer mp, int what, int extra) {
                                            if (what == mp.MEDIA_INFO_BUFFERING_END) {
                                                messageViewHolder.receiver_loading_video_progress_bar.setVisibility(View.INVISIBLE);
                                                return true;
                                            } else if (what == mp.MEDIA_INFO_BUFFERING_START) {
                                                messageViewHolder.receiver_loading_video_progress_bar.setVisibility(View.VISIBLE);
                                            }
                                            return false;
                                        }
                                    });

                                    messageViewHolder.receiver_message_video.start();
                                    messageViewHolder.receiver_message_video.setBackgroundColor(0);
                                    messageViewHolder.receiver_loading_video_progress_bar.setVisibility(View.INVISIBLE);


                                    messageViewHolder.receiver_message_video.seekTo(messageViewHolder.receiver_message_video.getCurrentPosition());
                                    if (messageViewHolder.receiver_message_video.getCurrentPosition() != 0) {
                                        messageViewHolder.receiver_message_video.start();

                                    } else {
                                        messageViewHolder.receiver_message_video.pause();
                                    }

                                }
                            });
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                } else if (fromMessageType.equals("audio")) {
                    if (fromUserID.equals(messageSenderID)) {
                        try {
                            final int[] current = {0};
                            final int[] duration = {0};
                            //for audio sender
                            messageViewHolder.sender_audio_layout.setVisibility(View.VISIBLE);
                            messageViewHolder.sender_audio_title.setVisibility(View.VISIBLE);
                            messageViewHolder.sender_play_btn.setVisibility(View.VISIBLE);
                            messageViewHolder.sender_counter_timer.setVisibility(View.VISIBLE);
                            messageViewHolder.sender_video_progress.setVisibility(View.VISIBLE);
                            messageViewHolder.sender_duration_timer.setVisibility(View.VISIBLE);
                            messageViewHolder.sender_message_audio_time.setVisibility(View.VISIBLE);
                            messageViewHolder.sender_icon_sent_audio.setVisibility(View.VISIBLE);

                            if (messagesModel.isIsseen()) {
                                messageViewHolder.sender_icon_sent_audio.setImageResource(R.drawable.ic_sent);
                            } else {
                                messageViewHolder.sender_icon_sent_audio.setImageResource(R.drawable.ic_delivered);
                            }

                            messageViewHolder.sender_audio_title.setText(messagesModel.getName());
                            messageViewHolder.sender_message_audio_time.setText(messagesModel.getTime());


                            sender_media_player = new MediaPlayer();
                            class AudioAsyncTask extends AsyncTask<String, Integer, String> {
                                @Override
                                protected void onPreExecute() {
                                    super.onPreExecute();
                                    messageViewHolder.sender_progressBar.setVisibility(View.VISIBLE);
                                }

                                @Override
                                protected String doInBackground(String... strings) {
                                    try {
                                        String url = new String(strings[0]);
                                        sender_media_player.reset();
                                        try {
                                            sender_media_player.setDataSource(url);
                                            sender_media_player.prepare();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }


                                    } catch (IllegalArgumentException | IllegalStateException e) {
                                        e.printStackTrace();
                                    }

                                    do {

                                        if (sender_media_player.isPlaying()) {
                                            current[0] = sender_media_player.getCurrentPosition() / 1000;
                                            publishProgress(current[0]);
                                        }


                                    } while (messageViewHolder.sender_video_progress.getProgress() <= 100);

                                    return null;
                                }

                                @Override
                                protected void onProgressUpdate(Integer... values) {
                                    super.onProgressUpdate(values);

                                    try {
                                        int currentPercent = values[0] * 100 / duration[0];
                                        messageViewHolder.sender_video_progress.setProgress(currentPercent);
                                        String currentString = String.format("%02d:%02d", values[0] / 60, values[0] % 60);
                                        messageViewHolder.sender_counter_timer.setText(currentString);


                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Log.v("Error", e.getMessage());

                                    }
                                }

                                @Override
                                protected void onPostExecute(String s) {
                                    super.onPostExecute(s);
                                    messageViewHolder.sender_progressBar.setVisibility(View.INVISIBLE);
                                    messageViewHolder.sender_video_progress.setProgress(0);
                                    messageViewHolder.sender_video_progress.setMax(100);

                                }
                            }


                            new AudioAsyncTask().execute(userMessagesList.get(position).getMessage());

                            sender_media_player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                                @Override
                                public void onPrepared(MediaPlayer mp) {

                                    try {

                                        current[0] = mp.getCurrentPosition() / 1000;
                                        String currentString = String.format("%02d:%02d", current[0] / 60, current[0] % 60);
                                        messageViewHolder.sender_counter_timer.setText(currentString);


                                        try {

                                            int currentPercent = current[0] * 100 / duration[0];
                                            messageViewHolder.sender_video_progress.setProgress(currentPercent);


                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Log.v("Error", e.getMessage());

                                        }

                                        duration[0] = mp.getDuration() / 1000;
                                        String durationString = String.format("%02d:%02d", duration[0] / 60, duration[0] % 60);
                                        messageViewHolder.sender_duration_timer.setText(durationString);

                                        if (current[0] == duration[0]) {
                                            mp.release();
                                            messageViewHolder.sender_play_btn.setImageResource(R.drawable.ic_play);
                                        }


                                        mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                                            @Override
                                            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                                                if (what == mp.MEDIA_INFO_BUFFERING_END) {
                                                    messageViewHolder.sender_progressBar.setVisibility(View.INVISIBLE);
                                                    return true;
                                                } else if (what == mp.MEDIA_INFO_BUFFERING_START) {
                                                    messageViewHolder.sender_progressBar.setVisibility(View.VISIBLE);
                                                }
                                                return false;

                                            }

                                        });

                                        messageViewHolder.sender_play_btn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (!sender_media_player.isPlaying()) {
                                                    sender_media_player.start();
                                                    messageViewHolder.sender_progressBar.setVisibility(View.INVISIBLE);
                                                    messageViewHolder.sender_play_btn.setImageResource(R.drawable.ic_pause);

                                                } else {
                                                    sender_media_player.pause();
                                                    messageViewHolder.sender_play_btn.setImageResource(R.drawable.ic_play);
                                                }

                                            }
                                        });

                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }

                                }
                            });

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    } else {

                        try {
                            final int[] current = {0};
                            final int[] duration = {0};

                            //for audio receiver
                            messageViewHolder.receiver_message_audio_layout.setVisibility(View.VISIBLE);
                            messageViewHolder.receiver_profile_audio_for_image.setVisibility(View.VISIBLE);
                            messageViewHolder.receiver_audio_title.setVisibility(View.VISIBLE);
                            messageViewHolder.receiver_play_btn.setVisibility(View.VISIBLE);
                            messageViewHolder.receiver_counter_timer.setVisibility(View.VISIBLE);
                            messageViewHolder.receiver_video_progress.setVisibility(View.VISIBLE);
                            messageViewHolder.receiver_duration_timer.setVisibility(View.VISIBLE);
                            messageViewHolder.receiver_message_audio_time.setVisibility(View.VISIBLE);

                            messageViewHolder.receiver_audio_title.setText(messagesModel.getName());
                            messageViewHolder.receiver_message_audio_time.setText(messagesModel.getTime());

                            receiver_media_player = new MediaPlayer();
                            class AudioAsyncTask extends AsyncTask<String, Integer, String> {
                                @Override
                                protected void onPreExecute() {
                                    super.onPreExecute();
                                    messageViewHolder.receiver_progressBar.setVisibility(View.VISIBLE);
                                }

                                @Override
                                protected String doInBackground(String... strings) {
                                    try {
                                        String url = new String(strings[0]);
                                        receiver_media_player.reset();
                                        try {
                                            receiver_media_player.setDataSource(url);
                                            receiver_media_player.prepare();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }


                                    } catch (IllegalArgumentException | IllegalStateException e) {
                                        e.printStackTrace();
                                    }

                                    do {

                                        if (receiver_media_player.isPlaying()) {
                                            current[0] = receiver_media_player.getCurrentPosition() / 1000;
                                            publishProgress(current[0]);
                                        }


                                    } while (messageViewHolder.receiver_video_progress.getProgress() <= 100);

                                    return null;
                                }

                                @Override
                                protected void onProgressUpdate(Integer... values) {
                                    super.onProgressUpdate(values);

                                    try {
                                        int currentPercent = values[0] * 100 / duration[0];
                                        messageViewHolder.receiver_video_progress.setProgress(currentPercent);
                                        String currentString = String.format("%02d:%02d", values[0] / 60, values[0] % 60);
                                        messageViewHolder.receiver_counter_timer.setText(currentString);


                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Log.v("Error", e.getMessage());

                                    }
                                }

                                @Override
                                protected void onPostExecute(String s) {
                                    super.onPostExecute(s);
                                    messageViewHolder.receiver_progressBar.setVisibility(View.INVISIBLE);
                                    messageViewHolder.receiver_video_progress.setProgress(0);
                                    messageViewHolder.receiver_video_progress.setMax(100);

                                }
                            }

                            new AudioAsyncTask().execute(userMessagesList.get(position).getMessage());

                            receiver_media_player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    try {
                                        current[0] = mp.getCurrentPosition() / 1000;
                                        String currentString = String.format("%02d:%02d", current[0] / 60, current[0] % 60);
                                        messageViewHolder.receiver_counter_timer.setText(currentString);


                                        try {

                                            int currentPercent = current[0] * 100 / duration[0];
                                            messageViewHolder.receiver_video_progress.setProgress(currentPercent);


                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Log.v("Error", e.getMessage());

                                        }

                                        duration[0] = mp.getDuration() / 1000;
                                        String durationString = String.format("%02d:%02d", duration[0] / 60, duration[0] % 60);
                                        messageViewHolder.receiver_duration_timer.setText(durationString);

                                        if (current[0] == duration[0]) {
                                            mp.release();
                                            messageViewHolder.receiver_play_btn.setImageResource(R.drawable.ic_play);
                                        }


                                        mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                                            @Override
                                            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                                                if (what == mp.MEDIA_INFO_BUFFERING_END) {
                                                    messageViewHolder.receiver_progressBar.setVisibility(View.INVISIBLE);
                                                    return true;
                                                } else if (what == mp.MEDIA_INFO_BUFFERING_START) {
                                                    messageViewHolder.receiver_progressBar.setVisibility(View.VISIBLE);
                                                }
                                                return false;

                                            }

                                        });

                                        messageViewHolder.receiver_play_btn.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (!receiver_media_player.isPlaying()) {
                                                    receiver_media_player.start();
                                                    messageViewHolder.receiver_progressBar.setVisibility(View.INVISIBLE);
                                                    messageViewHolder.receiver_play_btn.setImageResource(R.drawable.ic_pause);

                                                } else {
                                                    receiver_media_player.pause();
                                                    messageViewHolder.receiver_play_btn.setImageResource(R.drawable.ic_play);
                                                }

                                            }
                                        });
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


            } else {

                if (fromMessageType.equals("text")) {

                    if (fromUserID.equals(messageSenderID)) {
                        try {
                            messageViewHolder.reply_sender_message_layout.setVisibility(View.VISIBLE);
                            messageViewHolder.chatter_user_name_text.setVisibility(View.VISIBLE);
                            messageViewHolder.chatter_text_text.setVisibility(View.VISIBLE);
                            messageViewHolder.msg_type_img_icon_text.setVisibility(View.VISIBLE);
                            messageViewHolder.new_sender_message_text.setVisibility(View.VISIBLE);
                            messageViewHolder.new_sender_message_time.setVisibility(View.VISIBLE);
                            messageViewHolder.new_icon_sent.setVisibility(View.VISIBLE);


                            if (messagesModel.isIsseen()) {
                                messageViewHolder.new_icon_sent.setImageResource(R.drawable.ic_sent);
                            } else {
                                messageViewHolder.new_icon_sent.setImageResource(R.drawable.ic_delivered);
                            }
                            String replyMessageType = messagesModel.getReplymessagetype();

                            if (messagesModel.getReplyfromid().equals(messageSenderID)) {
                                messageViewHolder.chatter_user_name_text.setText("You");
                                messageViewHolder.chatter_user_name_text.setTextColor(
                                        messageViewHolder.itemView.getContext().getResources()
                                                .getColor(R.color.colorPrimaryDark));
                            } else {
                                usersRef.child(messagesModel.getReplyfromid())
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    try {
                                                        String userName = dataSnapshot.child("fullname").getValue().toString();
                                                        messageViewHolder.chatter_user_name_text.setText(userName);
                                                    }catch (Exception e){
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                messageViewHolder.chatter_user_name_text.setTextColor(
                                        messageViewHolder.itemView.getContext().getResources()
                                                .getColor(R.color.orange));
                            }

                            if (replyMessageType.equals("text")) {
                                messageViewHolder.chatter_text_text.setVisibility(View.VISIBLE);
                                messageViewHolder.chatter_text_text.setText(messagesModel.getReplymessage());
                                messageViewHolder.msg_type_img_icon_text.setVisibility(View.GONE);
                            }
                            switch (replyMessageType) {
                                case "document":
                                    messageViewHolder.chatter_text_text.setVisibility(View.GONE);
                                    messageViewHolder.msg_type_img_icon_text.setVisibility(View.VISIBLE);
                                    messageViewHolder.msg_type_img_icon_text.setImageResource(R.drawable.ic_document);

                                    break;
                                case "image":
                                    messageViewHolder.chatter_text_text.setVisibility(View.GONE);
                                    messageViewHolder.msg_type_img_icon_text.setVisibility(View.VISIBLE);
                                    messageViewHolder.msg_type_img_icon_text.setImageResource(R.drawable.ic_image);
                                    break;
                                case "video":
                                    messageViewHolder.chatter_text_text.setVisibility(View.GONE);
                                    messageViewHolder.msg_type_img_icon_text.setVisibility(View.VISIBLE);
                                    messageViewHolder.msg_type_img_icon_text.setImageResource(R.drawable.ic_videocam);
                                    break;
                                case "audio":
                                    messageViewHolder.chatter_text_text.setVisibility(View.GONE);
                                    messageViewHolder.msg_type_img_icon_text.setVisibility(View.VISIBLE);
                                    messageViewHolder.msg_type_img_icon_text.setImageResource(R.drawable.ic_music);
                                    break;
                            }


                            messageViewHolder.new_sender_message_text.setBackgroundResource(R.drawable.sender_message_text_background);
                            messageViewHolder.new_sender_message_text.setTextColor(Color.WHITE);
                            messageViewHolder.new_sender_message_text.setGravity(Gravity.START);
                            messageViewHolder.new_sender_message_text.setText(Html.fromHtml(messagesModel.getMessage()));
                            messageViewHolder.new_sender_message_time.setText(messagesModel.getTime());

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    } else {
                        messageViewHolder.reply_receiver_message_layout_main.setVisibility(View.VISIBLE);
                        messageViewHolder.reply_receiver_profile_image.setVisibility(View.VISIBLE);
                        messageViewHolder.sender_vertical_line.setVisibility(View.VISIBLE);
                        messageViewHolder.sender_chatter_user_name_text.setVisibility(View.VISIBLE);
                        messageViewHolder.sender_chatter_text_text.setVisibility(View.VISIBLE);
                        messageViewHolder.sender_msg_type_img_icon_text.setVisibility(View.VISIBLE);
                        messageViewHolder.new_receiver_message_text.setVisibility(View.VISIBLE);
                        messageViewHolder.new_receiver_message_time.setVisibility(View.VISIBLE);

                        String replyMessageType = messagesModel.getReplymessagetype();

                        if (messagesModel.getReplyfromid().equals(messageSenderID)) {
                            messageViewHolder.sender_chatter_user_name_text.setText("You");

                            messageViewHolder.sender_chatter_user_name_text.setTextColor(
                                    messageViewHolder.itemView.getContext().getResources()
                                            .getColor(R.color.orange));
                            messageViewHolder.sender_vertical_line.
                                    setBackgroundColor(messageViewHolder.itemView.getContext().getResources()
                                            .getColor(R.color.orange));
                        } else {

                            usersRef.child(messagesModel.getReplyfromid())
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                String userName = dataSnapshot.child("fullname").getValue().toString();
                                                messageViewHolder.sender_chatter_user_name_text.setText(userName);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });


                            messageViewHolder.sender_chatter_user_name_text.setTextColor(
                                    messageViewHolder.itemView.getContext().getResources()
                                            .getColor(R.color.colorPrimaryDark));
                            messageViewHolder.sender_vertical_line.
                                    setBackgroundColor(messageViewHolder.itemView.getContext().getResources()
                                            .getColor(R.color.colorPrimaryDark));
                        }

                        if (replyMessageType.equals("text")) {
                            messageViewHolder.sender_chatter_text_text.setVisibility(View.VISIBLE);
                            messageViewHolder.sender_chatter_text_text.setText(messagesModel.getReplymessage());
                            messageViewHolder.sender_msg_type_img_icon_text.setVisibility(View.GONE);
                        }
                        if (replyMessageType.equals("document")) {
                            messageViewHolder.sender_chatter_text_text.setVisibility(View.GONE);
                            messageViewHolder.sender_msg_type_img_icon_text.setVisibility(View.VISIBLE);
                            messageViewHolder.sender_msg_type_img_icon_text.setImageResource(R.drawable.ic_document);

                        } else if (replyMessageType.equals("image")) {
                            messageViewHolder.sender_chatter_text_text.setVisibility(View.GONE);
                            messageViewHolder.sender_msg_type_img_icon_text.setVisibility(View.VISIBLE);
                            messageViewHolder.sender_msg_type_img_icon_text.setImageResource(R.drawable.ic_image);
                        } else if (replyMessageType.equals("video")) {
                            messageViewHolder.sender_chatter_text_text.setVisibility(View.GONE);
                            messageViewHolder.sender_msg_type_img_icon_text.setVisibility(View.VISIBLE);
                            messageViewHolder.sender_msg_type_img_icon_text.setImageResource(R.drawable.ic_videocam);
                        } else if (replyMessageType.equals("audio")) {
                            messageViewHolder.sender_chatter_text_text.setVisibility(View.GONE);
                            messageViewHolder.sender_msg_type_img_icon_text.setVisibility(View.VISIBLE);
                            messageViewHolder.sender_msg_type_img_icon_text.setImageResource(R.drawable.ic_music);
                        }

                        messageViewHolder.new_receiver_message_text.setBackgroundResource(R.drawable.receiver_message_text_background);
                        messageViewHolder.new_receiver_message_text.setGravity(Gravity.START);
                        messageViewHolder.new_receiver_message_text.setText(Html.fromHtml(messagesModel.getMessage()));
                        messageViewHolder.new_receiver_message_time.setText(messagesModel.getTime());
                    }
                }

            }


            if (fromUserID.equals(messageSenderID)) {
                messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        try {

                            MessagesModel mgsDoc = userMessagesList.get(position);

                            if (mgsDoc != null) {

                                if (mgsDoc.getType().equals("document")) {

                                    CharSequence options_doc[] = new CharSequence[]{
                                            "Delete for me",
                                            "Delete for everyone",
                                            "Cancel"
                                    };
                                    AlertDialog.Builder builderoptions_doc = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                                    builderoptions_doc.setTitle("Delete this message?");

                                    builderoptions_doc.setItems(options_doc, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            if (which == 0) {

                                                DeleteSentMessages(position, messageViewHolder);
                                            } else if (which == 1) {

                                                DeleteForEveryoneMessages(position, messageViewHolder);
                                                DeleteFileFromStorage(position, messageViewHolder);
                                            }
                                        }
                                    });
                                    builderoptions_doc.show();
                                } else if (mgsDoc.getType().equals("video")) {

                                    CharSequence options_doc[] = new CharSequence[]{
                                            "Delete for me",
                                            "Delete for everyone",
                                            "View in full screen",
                                            "Cancel"
                                    };
                                    AlertDialog.Builder builderoptions_doc = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                                    builderoptions_doc.setTitle("Delete this message?");

                                    builderoptions_doc.setItems(options_doc, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            try {
                                                if (which == 0) {

                                                    DeleteSentMessages(position, messageViewHolder);
                                                } else if (which == 1) {

                                                    DeleteForEveryoneMessages(position, messageViewHolder);
                                                    DeleteVideoFromStorage(position, messageViewHolder);
                                                } else if (which == 2) {
                                                    //view in full screen intent
                                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), FullMessageVideoView.class);
                                                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                                                    intent.putExtra("name", userMessagesList.get(position).getName());
                                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                                    activity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                                }
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    builderoptions_doc.show();
                                } else if (mgsDoc.getType().equals("audio")) {

                                    CharSequence options_doc[] = new CharSequence[]{
                                            "Delete for me",
                                            "Delete for everyone",
                                            "Download Audio",
                                            "Cancel"
                                    };
                                    AlertDialog.Builder builderoptions_doc = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                                    builderoptions_doc.setTitle("Delete this message?");

                                    builderoptions_doc.setItems(options_doc, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            if (which == 0) {

                                                DeleteSentMessages(position, messageViewHolder);
                                            } else if (which == 1) {

                                                DeleteForEveryoneMessages(position, messageViewHolder);
                                                DeleteAudioFromStorage(position, messageViewHolder);
                                            } else if (which == 2) {
                                                DownloadAudio(position, messageViewHolder);
                                            }
                                        }
                                    });
                                    builderoptions_doc.show();
                                } else if (mgsDoc.getType().equals("image")) {

                                    CharSequence options_image[] = new CharSequence[]{
                                            "Delete for me",
                                            "Delete for everyone",
                                            "View picture",
                                            "Cancel"
                                    };
                                    AlertDialog.Builder builderoptions_image = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                                    builderoptions_image.setTitle("Delete message?");

                                    builderoptions_image.setItems(options_image, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            try {
                                                if (which == 0) {

                                                    DeleteSentMessages(position, messageViewHolder);
                                                } else if (which == 1) {

                                                    DeleteForEveryoneMessages(position, messageViewHolder);
                                                    DeletePictureFromStorage(position, messageViewHolder);
                                                } else if (which == 2) {
                                                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), FullMessageImageView.class);
                                                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                                                    intent.putExtra("name", userMessagesList.get(position).getName());
                                                    messageViewHolder.itemView.getContext().startActivity(intent);
                                                    activity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                                }
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    builderoptions_image.show();
                                } else if (mgsDoc.getType().equals("text")) {

                                    CharSequence options_text[] = new CharSequence[]{
                                            "Delete for me",
                                            "Delete for everyone",
                                            "Copy message to clipboard",
                                            "Cancel"
                                    };
                                    AlertDialog.Builder builderoptions_text = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                                    builderoptions_text.setTitle("Delete message?");

                                    builderoptions_text.setItems(options_text, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            if (which == 0) {

                                                DeleteSentMessages(position, messageViewHolder);
                                            } else if (which == 1) {

                                                DeleteForEveryoneMessages(position, messageViewHolder);
                                            } else if (which == 2) {
                                                CopyToClipBoard(position, messageViewHolder);
                                                Toasty.success(messageViewHolder.itemView.getContext(), "Message copied", Toasty.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                    builderoptions_text.show();
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return false;
                    }
                });

            } else if (!fromUserID.equals(messageSenderID)) {
                //allow user to delete sender messages, but only his own version

                messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        if (userMessagesList.get(position).getType().equals("document")) {

                            CharSequence options_r_doc[] = new CharSequence[]{
                                    "Delete for me",
                                    "Cancel"
                            };
                            AlertDialog.Builder builderoptions_r_doc = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builderoptions_r_doc.setTitle("Delete this message?");

                            builderoptions_r_doc.setItems(options_r_doc, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (which == 0) {

                                        DeleteReceivedMessages(position, messageViewHolder);
                                    }
                                }
                            });
                            builderoptions_r_doc.show();
                        } else if (userMessagesList.get(position).getType().equals("video")) {

                            CharSequence options_doc[] = new CharSequence[]{
                                    "Delete for me",
                                    "View in full screen",
                                    "Cancel"
                            };
                            AlertDialog.Builder builderoptions_doc = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builderoptions_doc.setTitle("Delete this message?");

                            builderoptions_doc.setItems(options_doc, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        if (which == 0) {

                                            DeleteSentMessages(position, messageViewHolder);
                                        } else if (which == 1) {
                                            //view in full mode intent
                                            Intent intent = new Intent(messageViewHolder.itemView.getContext(), FullMessageVideoView.class);
                                            intent.putExtra("url", userMessagesList.get(position).getMessage());
                                            intent.putExtra("name", userMessagesList.get(position).getName());
                                            messageViewHolder.itemView.getContext().startActivity(intent);
                                            activity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            });
                            builderoptions_doc.show();
                        } else if (userMessagesList.get(position).getType().equals("audio")) {

                            CharSequence options_doc[] = new CharSequence[]{
                                    "Delete for me",
                                    "Download Audio",
                                    "Cancel"
                            };
                            AlertDialog.Builder builderoptions_doc = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builderoptions_doc.setTitle("Delete this message?");

                            builderoptions_doc.setItems(options_doc, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (which == 0) {

                                        DeleteSentMessages(position, messageViewHolder);
                                    } else if (which == 1) {
                                        DownloadAudio(position, messageViewHolder);
                                    }
                                }
                            });
                            builderoptions_doc.show();
                        } else if (userMessagesList.get(position).getType().equals("image")) {

                            CharSequence options_r_image[] = new CharSequence[]{
                                    "Delete for me",
                                    "View picture",
                                    "Cancel"
                            };
                            AlertDialog.Builder builderoptions_r_image = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builderoptions_r_image.setTitle("Delete message?");

                            builderoptions_r_image.setItems(options_r_image, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        if (which == 0) {

                                            DeleteReceivedMessages(position, messageViewHolder);
                                        } else if (which == 1) {

                                            Intent intent = new Intent(messageViewHolder.itemView.getContext(), FullMessageImageView.class);
                                            intent.putExtra("url", userMessagesList.get(position).getMessage());
                                            intent.putExtra("name", userMessagesList.get(position).getName());
                                            messageViewHolder.itemView.getContext().startActivity(intent);
                                            activity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            });
                            builderoptions_r_image.show();
                        } else if (userMessagesList.get(position).getType().equals("text")) {

                            CharSequence options_r_text[] = new CharSequence[]{
                                    "Delete for me",
                                    "Copy message to clipboard",
                                    "Cancel"
                            };
                            AlertDialog.Builder builderoptions_r_text = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                            builderoptions_r_text.setTitle("Delete message?");

                            builderoptions_r_text.setItems(options_r_text, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (which == 0) {

                                        DeleteReceivedMessages(position, messageViewHolder);
                                    } else if (which == 1) {
                                        CopyToClipBoard(position, messageViewHolder);
                                        Toasty.success(messageViewHolder.itemView.getContext(), "Message copied", Toasty.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            builderoptions_r_text.show();
                        }

                        return false;
                    }
                });
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public interface OnItemClickListener extends GroupMessagesAdapter.OnItemClickListener {
        void onItemClick(MessagesModel item, int position);
    }

    private void DownloadAudio(int position, MessageViewHolder messageViewHolder) {
        try {
            File root = Environment.getExternalStorageDirectory();
            root.mkdirs();
            String path = root.toString();


            DownloadManager downloadManager = (DownloadManager) messageViewHolder.itemView.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri1 = Uri.parse(userMessagesList.get(position).getMessage());
            DownloadManager.Request request = new DownloadManager.Request(uri1);
            request.setTitle("SmartChat (" + userMessagesList.get(position).getName() + ")");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalFilesDir(messageViewHolder.itemView.getContext(), path + "/SmartChat" + "/Messages" + "/Audio", userMessagesList.get(position).getName());
            downloadManager.enqueue(request);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void DeleteAudioFromStorage(int position, MessageViewHolder messageViewHolder) {
        try {
            String id = userMessagesList.get(position).getMessageID();
            String extension = userMessagesList.get(position).getExtension();
            String fileInStorage = id + "." + extension;
            StorageReference fileRef = FirebaseStorage.getInstance().getReference();
            fileRef.child("Messages Audio")
                    .child(fileInStorage)
                    .delete();
            userMessagesList.remove(position);
            notifyItemRemoved(position);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void DeleteSentMessages(final int position, final MessageViewHolder holder){
        try {
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            rootRef.child("Messages")
                    .child(userMessagesList.get(position).getFrom())
                    .child(userMessagesList.get(position).getTo())
                    .child(userMessagesList.get(position).getMessageID())
                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toasty.success(holder.itemView.getContext(), "Message deleted", Toasty.LENGTH_SHORT, true).show();
                    } else {
                        Toasty.error(holder.itemView.getContext(), "Message couldn't be deleted", Toasty.LENGTH_SHORT, true).show();
                    }
                    userMessagesList.remove(position);
                    notifyItemRemoved(position);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void DeletePictureFromStorage(final int position, final MessageViewHolder holder){
        try {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            storageReference.child("Messages Images")
                    .child(userMessagesList.get(position).getMessageID() + ".jpg")
                    .delete();
            userMessagesList.remove(position);
            notifyItemRemoved(position);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void CopyToClipBoard(final int position, final MessageViewHolder holder){
        try {

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager) holder.itemView.getContext()
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                String msg = userMessagesList.get(position).getMessage().replace("<br><br>", "  ");
                clipboardManager.setText(msg);
            } else {
                String msg = userMessagesList.get(position).getMessage().replace("<br><br>", "  ");
                android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager)
                        holder.itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clipData = android.content.ClipData.newPlainText("Text Copied", msg);
                clipboardManager.setPrimaryClip(clipData);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void DeleteFileFromStorage(final int position, final MessageViewHolder holder){
        try {
            String id = userMessagesList.get(position).getMessageID();
            String extension = userMessagesList.get(position).getExtension();
            String fileInStorage = id + "." + extension;
            StorageReference fileRef = FirebaseStorage.getInstance().getReference();
            fileRef.child("Messages Files")
                    .child(fileInStorage)
                    .delete();
            userMessagesList.remove(position);
            notifyItemRemoved(position);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void DeleteVideoFromStorage(final int position, final MessageViewHolder holder){
        try {
            String id = userMessagesList.get(position).getMessageID();
            String fileInStorage = id + ".mp4";
            StorageReference fileRef = FirebaseStorage.getInstance().getReference();
            fileRef.child("Messages Videos")
                    .child(fileInStorage)
                    .delete();
            userMessagesList.remove(position);
            notifyItemRemoved(position);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void DeleteReceivedMessages(final int position, final MessageViewHolder holder){
        try {
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            rootRef.child("Messages")
                    .child(userMessagesList.get(position).getTo())
                    .child(userMessagesList.get(position).getFrom())
                    .child(userMessagesList.get(position).getMessageID())
                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    try {
                        if (task.isSuccessful()) {
                            Toasty.success(holder.itemView.getContext(), "Message deleted", Toasty.LENGTH_SHORT, true).show();
                        } else {
                            Toasty.error(holder.itemView.getContext(), "Message couldn't be deleted", Toasty.LENGTH_SHORT, true).show();
                        }
                        userMessagesList.remove(position);
                        notifyItemRemoved(position);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void DeleteForEveryoneMessages(final int position, final MessageViewHolder holder){
        try {
            final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            rootRef.child("Messages")
                    .child(userMessagesList.get(position).getFrom())
                    .child(userMessagesList.get(position).getTo())
                    .child(userMessagesList.get(position).getMessageID())
                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        try {

                            rootRef.child("Messages")
                                    .child(userMessagesList.get(position).getTo())
                                    .child(userMessagesList.get(position).getFrom())
                                    .child(userMessagesList.get(position).getMessageID())
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    try {

                                        if (task.isSuccessful()) {
                                            Toasty.success(holder.itemView.getContext(), "Message deleted", Toasty.LENGTH_SHORT, true).show();
                                        } else {
                                            Toasty.error(holder.itemView.getContext(), "Message couldn't be deleted", Toasty.LENGTH_SHORT, true).show();
                                        }
                                        userMessagesList.remove(position);
                                        notifyItemRemoved(position);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        Toasty.error(holder.itemView.getContext(), "Message couldn't be deleted", Toasty.LENGTH_SHORT, true).show();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}