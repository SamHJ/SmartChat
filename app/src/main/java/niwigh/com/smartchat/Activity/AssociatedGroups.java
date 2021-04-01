package niwigh.com.smartchat.Activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
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
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import niwigh.com.smartchat.Model.GroupModel;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;


public class AssociatedGroups extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView list_view;
    DatabaseReference groupsRef,usersRef;
    FirebaseAuth mAuth;
    String currentUserID;
    ProgressDialog loadingBars;
    AlertDialog alertDialog;
    Utilities utilities;
    View currentView;
    String group_category,group_category_title;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_associated_groups);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        group_category = getIntent().getStringExtra("group_cat");
        group_category_title = getIntent().getStringExtra("group_cat");
        groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_category).child("groups");
        groupsRef.keepSynced(true);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);
        
        utilities = Utilities.getInstance(this);
        currentView = getWindow().getDecorView().getRootView();


        loadingBars = new ProgressDialog(AssociatedGroups.this);

        toolbar = findViewById(R.id.all_groups_appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(group_category_title);
        list_view = findViewById(R.id.all_groups_recyclerview);
        list_view.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        list_view.setLayoutManager(linearLayoutManager);

        DisplayAllAssociatedGroups();
    }

    private void DisplayAllAssociatedGroups() {

        FirebaseRecyclerAdapter<GroupModel, GroupsViewHolder> firebaseRecyclerAdapter = new
                FirebaseRecyclerAdapter<GroupModel, GroupsViewHolder>
                        (
                                GroupModel.class,
                                R.layout.all_groups_display_layout,
                                GroupsViewHolder.class,
                                groupsRef
                        )
                {
                    @Override
                    protected void populateViewHolder(final GroupsViewHolder viewHolder, GroupModel model, int position) {


                        final String groupKey = getRef(position).getKey();
                        groupsRef.child(groupKey).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(dataSnapshot.exists()) {

                                    final String groupname, groupimage;
                                    final boolean couldjoin;
                                    if (dataSnapshot.hasChild("groupname")) {

                                        groupname = dataSnapshot.child("groupname").getValue().toString();
                                        groupimage = dataSnapshot.child("groupimage").getValue() != null ?
                                                dataSnapshot.child("groupimage").getValue().toString() : null;
                                        couldjoin = (boolean)dataSnapshot.child("couldjoin").getValue();

                                        viewHolder.setGroupimage(AssociatedGroups.this, groupimage);
                                        viewHolder.setGroupname(groupname);
                                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                loadingBars.setTitle("Opening group");
                                                loadingBars.setMessage("A moment please");
                                                loadingBars.setCanceledOnTouchOutside(false);
                                                loadingBars.setCancelable(false);
                                                loadingBars.show();
                                                //check if the user is in the group
                                                DatabaseReference groupsRef =
                                                        FirebaseDatabase.getInstance().getReference()
                                                                .child("Groups").child(group_category)
                                                        .child("groups").child(groupKey).child("users");

                                                mAuth = FirebaseAuth.getInstance();
                                                currentUserID = mAuth.getCurrentUser().getUid();

                                                groupsRef.addValueEventListener(new ValueEventListener() {
                                                    @SuppressLint("SetTextI18n")
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if(dataSnapshot.exists()){
                                                            if(dataSnapshot.hasChild(currentUserID)){
                                                                //open messaging area for this group
                                                                loadingBars.dismiss();
                                                                Intent message_this_user = new Intent(
                                                                        AssociatedGroups.this, GroupMessagingArea.class);
                                                                message_this_user.putExtra("group_cat", group_category);
                                                                message_this_user.putExtra("group_key", groupKey);
                                                                message_this_user.putExtra("group_image",groupimage);
                                                                startActivity(message_this_user);
                                                                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                                                finish();
                                                            }else {
                                                               checkToJoin(loadingBars,couldjoin,group_category,
                                                                       groupKey,groupimage);
                                                            }
                                                        }else{
                                                            checkToJoin(loadingBars,couldjoin,group_category,
                                                                    groupKey,groupimage);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });

                                            }
                                        });

                                        groupsRef.child(groupKey).child("users").addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists()){
                                                    int countParticipants = (int)dataSnapshot.getChildrenCount();
                                                    viewHolder.setGroupNoOfParticipants(Integer.toString(countParticipants));
                                                }else {
                                                    viewHolder.setGroupNoOfParticipants("0");
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
                    }
                };

        list_view.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }

    @SuppressLint("SetTextI18n")
    private void checkToJoin(ProgressDialog loadingBars, boolean couldjoin,
                             final String groupCategory, final String groupKey, final String groupimage) {

        //{.....................
        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(AssociatedGroups.this).inflate(R.layout.error_dialog, viewGroup, false);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(AssociatedGroups.this);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);

        //finally creating the alert dialog and displaying it
        alertDialog = builder.create();

        Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
        Button btn_join = dialogView.findViewById(R.id.btn_join);
        TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
        TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

        if(couldjoin){
            btn_join.setVisibility(View.VISIBLE);
            success_text.setText("This group is open but " +
                    "you are not yet a participant!");
        }else{
            btn_join.setVisibility(View.GONE);
            success_text.setText("This group is currently not open");
        }


        dialog_btn.setText("OK");
        btn_join.setText("JOIN GROUP");
        success_title.setText("Sorry");

        // if the OK button is clicked, close the success dialog
        dialog_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });


        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUserToGroup(currentUserID,groupCategory,groupKey
                        ,groupimage);
            }
        });
        try{
            alertDialog.show();
        }catch (Exception e){
            e.printStackTrace();
        }
        //...................}
        loadingBars.dismiss();
    }

    private void addUserToGroup(String currentUserID, final String groupCategory, final String groupKey
            , final String groupImage) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Joining group...");
        dialog.setMessage("a moment please");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        Map<String, Object> addtogroupmap = new HashMap<>();
        addtogroupmap.put(currentUserID, currentUserID);
        DatabaseReference addToGroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        addToGroupRef.child(groupCategory).child("groups").child(groupKey).child("users")
                .updateChildren(addtogroupmap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toasty.success(AssociatedGroups.this, "You are now a participant of this group",
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    Intent message_this_user = new Intent(AssociatedGroups.this, GroupMessagingArea.class);
                    message_this_user.putExtra("group_cat", groupCategory);
                    message_this_user.putExtra("group_key", groupKey);
                    message_this_user.putExtra("group_image",groupImage);
                    startActivity(message_this_user);
                    overridePendingTransition(R.anim.right_in, R.anim.left_out);
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toasty.error(AssociatedGroups.this, "An error occurred!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class GroupsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public GroupsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void setGroupimage(final Context ctx, final String groupImageUrl){
            final CircleImageView groupimage = mView.findViewById(R.id.group_image);
           try{
               Picasso.with(ctx).load(groupImageUrl).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.easy_to_use)
                       .into(groupimage, new Callback() {
                           @Override
                           public void onSuccess() {

                           }

                           @Override
                           public void onError() {

                               Picasso.with(ctx).load(groupImageUrl).placeholder(R.drawable.easy_to_use).into(groupimage);
                           }
                       });
           }catch (Exception e){
               e.printStackTrace();
           }
        }

        public void setGroupname(final String groupname) {
            TextView group_name = itemView.findViewById(R.id.group_name);
            group_name.setText(groupname);
        }

        public void setGroupNoOfParticipants(final String noOfParticipants){
            TextView no_of_participants = itemView.findViewById(R.id.no_of_participants_in_group);
            no_of_participants.setText(noOfParticipants+" participants");
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
                firebaseGroupSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                firebaseGroupSearch(newText);
                return false;
            }
        });
        return  super.onCreateOptionsMenu(menu);
    }
    

    public void firebaseGroupSearch(String searchText){

        Query firebaseSearchQuery = groupsRef.orderByChild("groupname").startAt(searchText).endAt(searchText + "\uf0ff");

        FirebaseRecyclerAdapter<GroupModel, GroupsViewHolder> firebaseRecyclerAdapter = new
                FirebaseRecyclerAdapter<GroupModel, GroupsViewHolder>
                        (
                                GroupModel.class,
                                R.layout.all_groups_display_layout,
                                GroupsViewHolder.class,
                                firebaseSearchQuery
                        )
                {
                    @Override
                    protected void populateViewHolder(final GroupsViewHolder viewHolder, GroupModel model, int position) {


                        final String groupKey = getRef(position).getKey();
                        groupsRef.child(groupKey).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(dataSnapshot.exists()) {

                                    final String groupname, groupimage;
                                    final boolean couldjoin;
                                    if (dataSnapshot.hasChild("groupname")) {

                                        groupname = dataSnapshot.child("groupname").getValue().toString();
                                        groupimage = dataSnapshot.child("groupimage").getValue() != null ?
                                                dataSnapshot.child("groupimage").getValue().toString() : null;
                                        couldjoin = (boolean)dataSnapshot.child("couldjoin").getValue();

                                        viewHolder.setGroupimage(AssociatedGroups.this, groupimage);
                                        viewHolder.setGroupname(groupname);
                                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                                loadingBars.setTitle("Opening group");
                                                loadingBars.setMessage("A moment please");
                                                loadingBars.setCanceledOnTouchOutside(false);
                                                loadingBars.setCancelable(false);
                                                loadingBars.show();
                                                //check if the user is in the group
                                                DatabaseReference groupsRef =
                                                        FirebaseDatabase.getInstance().getReference()
                                                                .child("Groups").child(group_category)
                                                                .child("groups").child(groupKey).child("users");

                                                mAuth = FirebaseAuth.getInstance();
                                                currentUserID = mAuth.getCurrentUser().getUid();

                                                groupsRef.addValueEventListener(new ValueEventListener() {
                                                    @SuppressLint("SetTextI18n")
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                        if(dataSnapshot.exists()){
                                                            if(dataSnapshot.hasChild(currentUserID)){
                                                                //open messaging area for this group
                                                                loadingBars.dismiss();
                                                                Intent message_this_user = new Intent(
                                                                        AssociatedGroups.this, GroupMessagingArea.class);
                                                                message_this_user.putExtra("group_cat", group_category);
                                                                message_this_user.putExtra("group_key", groupKey);
                                                                message_this_user.putExtra("group_image",groupimage);
                                                                startActivity(message_this_user);
                                                                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                                                finish();
                                                            }else {
                                                                checkToJoin(loadingBars,couldjoin,group_category,
                                                                        groupKey,groupimage);
                                                            }
                                                        }else{
                                                            checkToJoin(loadingBars,couldjoin,group_category,
                                                                    groupKey,groupimage);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });

                                            }
                                        });

                                        groupsRef.child(groupKey).child("users").addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists()){
                                                    int countParticipants = (int)dataSnapshot.getChildrenCount();
                                                    viewHolder.setGroupNoOfParticipants(Integer.toString(countParticipants));
                                                }else {
                                                    viewHolder.setGroupNoOfParticipants("0");
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
                    }
                };

        list_view.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();

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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
        finish();
    }


    @Override
    public void onStart() {
        super.onStart();
        loadingBars.dismiss();
        Utilities.getInstance(this).updateUserStatus("Online");
    }

    @Override
    public void onResume() {
        super.onResume();
        loadingBars.dismiss();

        if(alertDialog != null){
            alertDialog.dismiss();
        }
        Utilities.getInstance(this).updateUserStatus("Online");
    }

    @Override
    public void onPause() {
        super.onPause();
        loadingBars.dismiss();
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