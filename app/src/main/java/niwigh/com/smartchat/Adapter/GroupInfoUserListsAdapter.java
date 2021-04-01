package niwigh.com.smartchat.Adapter;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
        import android.app.ProgressDialog;
        import android.content.Context;
        import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import niwigh.com.smartchat.Activity.PersonProfile;
import niwigh.com.smartchat.Model.User;
        import niwigh.com.smartchat.R;

public class GroupInfoUserListsAdapter extends RecyclerView.Adapter<GroupInfoUserListsAdapter.AdapterViewHolder> {

    private Context mContext;
    private List<User> userList;
    private ProgressDialog dialog;
    String group_key,group_category;
    FirebaseAuth mAuth;
    ViewGroup viewGroup;

    public GroupInfoUserListsAdapter(Context mContext, List<User> userList, String group_key, String group_category,
                                     ViewGroup viewGroup) {
        this.mContext = mContext;
        this.userList = userList;
        this.group_key = group_key;
        this.group_category = group_category;
        this.viewGroup = viewGroup;
    }

    @NonNull
    @Override
    public AdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.group_users_layout, null);

        return new AdapterViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final AdapterViewHolder holder, final int position) {
        dialog = new ProgressDialog(mContext);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.setMessage("Processing...");

        mAuth = FirebaseAuth.getInstance();

        holder.user_name.setText(userList.get(position).getFullname());
        holder.profile_status.setText(userList.get(position).getProfilestatus());
        try{
            Picasso.with(mContext).load(userList.get(position).getProfileimage()).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.chatter).error(R.drawable.chatter).into(holder.user_image,
                    new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            try{
                                Picasso.with(mContext).load(userList.get(position).getProfileimage())
                                        .placeholder(R.drawable.chatter)
                                        .error(R.drawable.chatter).into(holder.user_image);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }

        userHandlers(userList.get(position).getId(),holder,mContext);
    }

    private void userHandlers(final String id, final AdapterViewHolder holder, final Context mContext) {

        final Activity activity = (Activity) mContext;
        DatabaseReference adminsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_category)
                                                .child("groups").child(group_key).child("admins");
                                        adminsRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    if (dataSnapshot.hasChild(id)) {
                                                        holder.btn_layout.setVisibility(View.VISIBLE);
                                                    } else {
                                                        holder.btn_layout.setVisibility(View.GONE);
                                                    }
                                                } else {
                                                    holder.btn_layout.setVisibility(View.GONE);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

        DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_category)
                                                .child("groups").child(group_key).child("admins");

                                        groupsRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {

                                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                Intent viewUserDetailsIntent = new Intent(mContext, PersonProfile.class);
                                                                viewUserDetailsIntent.putExtra("visit_user_id", id);
                                                                mContext.startActivity(viewUserDetailsIntent);
                                                                activity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                                            }
                                                        });

                                                        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                                                            @Override
                                                            public boolean onLongClick(View view) {

                                                                try {
                                                                    final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                                                                    alertDialog.setMessage("What do you want to do?");
                                                                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "MAKE PARTICIPANT AN ADMIN",
                                                                            new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                                    makeParticapantAnAdmin(id);
                                                                                }
                                                                            });
                                                                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "REMOVE PARTICIPANT FROM ADMIN",
                                                                            new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                                    removeParticipantFromAdmin(id);
                                                                                }
                                                                            });
                                                                    alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "DELETE PARTICIPANT",
                                                                            new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                                                    deleteParticipantFromGroup(id, group_key, group_category);
                                                                                }
                                                                            });
                                                                    alertDialog.show();
                                                                } catch (WindowManager.BadTokenException e) {
                                                                    //use a log message
                                                                }


                                                                return false;
                                                            }
                                                        });

                                                    }else{
                                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                Intent viewUserDetailsIntent = new Intent(mContext, PersonProfile.class);
                                                                viewUserDetailsIntent.putExtra("visit_user_id", id);
                                                                mContext.startActivity(viewUserDetailsIntent);
                                                                activity.overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                                            }
                                                        });
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
    }

    private void removeParticipantFromAdmin(final String key) {

        final ProgressDialog loadingBar =  new ProgressDialog(mContext);
        loadingBar.setTitle("Removing participant from admin");
        loadingBar.setMessage("A moment please");
        loadingBar.setCancelable(false);
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        final DatabaseReference removeFromAdminRef = FirebaseDatabase.getInstance()
                .getReference().child("Groups").child(group_category)
                .child("groups").child(group_key).child("admins");
        removeFromAdminRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.getChildrenCount() < 2 && dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())){

                        try {
                            //{.....................

                            //then we will inflate the custom alert dialog xml that we created
                            View dialogView = LayoutInflater.from(mContext)
                                    .inflate(R.layout.error_dialog, viewGroup, false);


                            //Now we need an AlertDialog.Builder object
                            android.support.v7.app.AlertDialog.Builder builder =
                                    new android.support.v7.app.AlertDialog.Builder(mContext);

                            //setting the view of the builder to our custom view that we already inflated
                            builder.setView(dialogView);

                            //finally creating the alert dialog and displaying it
                            final android.support.v7.app.AlertDialog alertDialog = builder.create();

                            Button dialog_btn = dialogView.findViewById(R.id.buttonError);
                            TextView success_text = dialogView.findViewById(R.id.error_text);
                            TextView success_title = dialogView.findViewById(R.id.error_title);

                            dialog_btn.setText("OK");
                            success_title.setText("Error");
                            success_text.setText("This group requires atleast one admin!");

                            // if the OK button is clicked, close the success dialog
                            dialog_btn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alertDialog.dismiss();
                                }
                            });

                            alertDialog.show();
                            //...................}

                            Map<String,Object> addAdmin = new HashMap<>();
                            addAdmin.put(""+mAuth.getCurrentUser().getUid(),mAuth.getCurrentUser().getUid());
                            removeFromAdminRef.updateChildren(addAdmin);

                        }
                        catch (WindowManager.BadTokenException e) {
                            //use a log message
                        }

                        loadingBar.dismiss();


                    }else {
                        removeFromAdminRef.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                loadingBar.dismiss();
                                Toasty.success(mContext,"Participant removed from admin successfully",
                                        Toasty.LENGTH_LONG, true).show();

                            }

                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                try {
                                    //{.....................

                                    //then we will inflate the custom alert dialog xml that we created
                                    View dialogView = LayoutInflater.from(mContext)
                                            .inflate(R.layout.error_dialog, viewGroup, false);


                                    //Now we need an AlertDialog.Builder object
                                    android.support.v7.app.AlertDialog.Builder builder =
                                            new android.support.v7.app.AlertDialog.Builder(mContext);

                                    //setting the view of the builder to our custom view that we already inflated
                                    builder.setView(dialogView);

                                    //finally creating the alert dialog and displaying it
                                    final android.support.v7.app.AlertDialog alertDialog = builder.create();

                                    Button dialog_btn = dialogView.findViewById(R.id.buttonError);
                                    TextView success_text = dialogView.findViewById(R.id.error_text);
                                    TextView success_title = dialogView.findViewById(R.id.error_title);

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
                                }
                                catch (WindowManager.BadTokenException error) {
                                    //use a log message
                                }

                                loadingBar.dismiss();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void makeParticapantAnAdmin(String key) {
        final ProgressDialog loadingBar =  new ProgressDialog(mContext);
        loadingBar.setTitle("Converting participant to admin");
        loadingBar.setMessage("A moment please");
        loadingBar.setCancelable(false);
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        Map<String,Object> adminMap  = new HashMap<>();
        adminMap.put(key, key);
        DatabaseReference makeAdminRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_category)
                .child("groups").child(group_key).child("admins");
        makeAdminRef.updateChildren(adminMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    loadingBar.dismiss();
                    Toasty.success(mContext,"Participant converted to admin successfully",
                            Toasty.LENGTH_LONG, true).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Exception e) {
                try {
                    //{.....................

                    //then we will inflate the custom alert dialog xml that we created
                    View dialogView = LayoutInflater.from(mContext).inflate(R.layout.error_dialog, viewGroup, false);


                    //Now we need an AlertDialog.Builder object
                    android.support.v7.app.AlertDialog.Builder builder = new
                            android.support.v7.app.AlertDialog.Builder(mContext);

                    //setting the view of the builder to our custom view that we already inflated
                    builder.setView(dialogView);

                    //finally creating the alert dialog and displaying it
                    final android.support.v7.app.AlertDialog alertDialog = builder.create();

                    Button dialog_btn = dialogView.findViewById(R.id.buttonError);
                    TextView success_text = dialogView.findViewById(R.id.error_text);
                    TextView success_title = dialogView.findViewById(R.id.error_title);

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
                }
                catch (WindowManager.BadTokenException error) {
                    //use a log message
                }

                loadingBar.dismiss();
            }
        });

    }

    private void deleteParticipantFromGroup(String key, String group_key, String group_category) {

        final ProgressDialog loadingBars = new ProgressDialog(mContext);
        loadingBars.setTitle("Deleting Participant");
        loadingBars.setMessage("A moment please");
        loadingBars.setCancelable(false);
        loadingBars.setCanceledOnTouchOutside(false);
        loadingBars.show();
        DatabaseReference removeParticipantRef = FirebaseDatabase.getInstance().getReference()
                .child("Groups").child(group_category)
                .child("groups").child(group_key).child("users").child(key);

        removeParticipantRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadingBars.dismiss();
                Toasty.success(mContext,"Participant deleted successfully", Toasty.LENGTH_LONG, true).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Exception e) {

                try {
                    //{.....................

                    //then we will inflate the custom alert dialog xml that we created
                    View dialogView = LayoutInflater.from(mContext).inflate(R.layout.error_dialog, viewGroup, false);


                    //Now we need an AlertDialog.Builder object
                    android.support.v7.app.AlertDialog.Builder builder =
                            new android.support.v7.app.AlertDialog.Builder(mContext);

                    //setting the view of the builder to our custom view that we already inflated
                    builder.setView(dialogView);

                    //finally creating the alert dialog and displaying it
                    final android.support.v7.app.AlertDialog alertDialog = builder.create();

                    Button dialog_btn = dialogView.findViewById(R.id.buttonError);
                    TextView success_text = dialogView.findViewById(R.id.error_text);
                    TextView success_title = dialogView.findViewById(R.id.error_title);

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
                }
                catch (WindowManager.BadTokenException error) {
                    //use a log message
                }

                loadingBars.dismiss();
            }
        });


    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    class AdapterViewHolder extends RecyclerView.ViewHolder{

        TextView user_name,profile_status;
        CircleImageView user_image;
        LinearLayout btn_layout;

        private AdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            user_image = itemView.findViewById(R.id.user_image);
            user_name = itemView.findViewById(R.id.user_name);
            profile_status = itemView.findViewById(R.id.profile_status);
            btn_layout = itemView.findViewById(R.id.btn_layout);

        }
    }


}
