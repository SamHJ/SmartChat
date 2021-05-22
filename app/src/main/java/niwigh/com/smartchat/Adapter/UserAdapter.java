package niwigh.com.smartchat.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import niwigh.com.smartchat.Model.MessagesModel;
import niwigh.com.smartchat.Activity.MessagingArea;
import niwigh.com.smartchat.Model.User;
import niwigh.com.smartchat.R;

public class UserAdapter extends RecyclerView.Adapter <UserAdapter.ViewHolder>{

    private Context mContext;
    private List<User> mUsers;
    private boolean isChat;
    String theLastMessage;
    String theLastMessageTime;

    public UserAdapter(Context mContext, List<User> mUsers, boolean isChat){
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.all_users_display_layout, viewGroup, false);

        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position) {

        try {
            final Activity activity = (Activity) viewHolder.itemView.getContext();

            final User user = mUsers.get(position);
            viewHolder.fullname.setText(user.getFullname());
            try {
                Picasso.with(viewHolder.profile_image.getContext()).load(user.getProfileimage()).into(viewHolder.profile_image);

                Picasso.with(viewHolder.profile_image.getContext()).load(user.getProfileimage()).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.easy_to_use).into(viewHolder.profile_image,
                        new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                try {
                                    Picasso.with(viewHolder.profile_image.getContext()).load(user.getProfileimage())
                                            .placeholder(R.drawable.easy_to_use).into(viewHolder.profile_image);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (isChat) {
                lastMessage(user.getId(), viewHolder.lastmessage, viewHolder.last_msg_time);
            } else {
                viewHolder.lastmessage.setVisibility(View.GONE);
                viewHolder.last_msg_time.setVisibility(View.GONE);
            }

            if (isChat) {
                if (user.getStatus().equals("Online")) {
                    viewHolder.user_online_icon.setVisibility(View.VISIBLE);
                    viewHolder.user_offline_icon.setVisibility(View.GONE);
                } else {
                    viewHolder.user_online_icon.setVisibility(View.GONE);
                    viewHolder.user_offline_icon.setVisibility(View.VISIBLE);
                }
            } else {
                viewHolder.user_online_icon.setVisibility(View.GONE);
                viewHolder.user_offline_icon.setVisibility(View.GONE);
            }

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, MessagingArea.class);
                    intent.putExtra("visit_user_id", user.getId());
                    intent.putExtra("userFullName", user.getFullname());
                    intent.putExtra("userName", user.getUsername());
                    mContext.startActivity(intent);
                    activity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView fullname, lastmessage, last_msg_time;
        public CircleImageView profile_image;
        public ImageView user_online_icon, user_offline_icon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fullname = itemView.findViewById(R.id.search_all_users_profile_name);
            profile_image = itemView.findViewById(R.id.search_all_users_profile_image);
            user_online_icon = itemView.findViewById(R.id.user_online_icon);
            user_offline_icon = itemView.findViewById(R.id.user_offline_icon);
            lastmessage = itemView.findViewById(R.id.search_all_users_profile_school);
            last_msg_time = itemView.findViewById(R.id.search_all_users_profile_username);
        }
    }


    //check for last message
    public void lastMessage(final String userId, final TextView last_msg, final TextView last_msg_time){
        try {
            theLastMessage = "default";
            theLastMessageTime = "default";
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            final String onlineuId = firebaseUser.getUid();

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Messages");

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            try {
                                String keyone = snapshot.getKey();
                                for (DataSnapshot snapshot1 : snapshot.child(keyone).getChildren()) {

                                    try {
                                        String key_two = snapshot1.getKey();

                                        for (DataSnapshot snapshot2 : snapshot1.child(key_two).getChildren()) {
                                            try {
                                                MessagesModel messagesModel = snapshot2.getValue(MessagesModel.class);
                                                String from = messagesModel.getFrom();
                                                String to = messagesModel.getTo();
                                                boolean isseen = messagesModel.isIsseen();
                                                String time = messagesModel.getTime();
                                                String message = messagesModel.getMessage();
                                                if (to.equals(onlineuId) && from.equals(userId) ||
                                                        to.equals(userId) && from.equals(onlineuId)) {

                                                    Toast.makeText(mContext, key_two, Toast.LENGTH_SHORT).show();
                                                    theLastMessage = message;
                                                    theLastMessageTime = time;
                                                }
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        if ("default".equals(theLastMessage)) {
                            last_msg.setText("No message");
                            last_msg_time.setText("No Time");
                        } else {
                            last_msg.setText(theLastMessage);
                            last_msg_time.setText(theLastMessageTime);
                        }
                        theLastMessage = "default";
                        theLastMessageTime = "default";
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