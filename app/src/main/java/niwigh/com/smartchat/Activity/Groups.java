package niwigh.com.smartchat.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import niwigh.com.smartchat.Model.GroupCatModel;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;


public class Groups extends AppCompatActivity {

    FloatingActionButton addNewGroupFab;
    Toolbar toolbar;
    RecyclerView list_view;
    DatabaseReference groupsRef,usersRef;
    FirebaseAuth mAuth;
    String currentUserID;
    FirebaseStorage firebaseStorage;



    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        groupsRef.keepSynced(true);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);

        firebaseStorage = FirebaseStorage.getInstance();

        toolbar = findViewById(R.id.all_groups_appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Groups");
        list_view = findViewById(R.id.all_groups_recyclerview);
        list_view.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        list_view.setLayoutManager(linearLayoutManager);

        addNewGroupFab = findViewById(R.id.add_new_group_fab);

        addNewGroupFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newgroupintent = new Intent(Groups.this, AddNewGroup.class);
                startActivity(newgroupintent);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });

        usersRef.child(currentUserID).child("userState").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("usertype")){

                        addNewGroupFab.setVisibility(View.VISIBLE);

                    }else{
                        addNewGroupFab.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DisplayAllGroups();
    }

    public void DisplayAllGroups(){
        FirebaseRecyclerAdapter<GroupCatModel, GroupsViewHolder> firebaseRecyclerAdapter = new
                FirebaseRecyclerAdapter<GroupCatModel, GroupsViewHolder>
                        (
                                GroupCatModel.class,
                                R.layout.groups_display_layout,
                                GroupsViewHolder.class,
                                groupsRef
                        )
                {
                    @Override
                    protected void populateViewHolder(final GroupsViewHolder viewHolder, GroupCatModel model, int position) {


                        final String collectionKey = getRef(position).getKey();
                        groupsRef.child(collectionKey).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(dataSnapshot.exists()) {

                                    final String groupname, groupimage;
                                    if (dataSnapshot.hasChild("image") && dataSnapshot.hasChild("name")) {


                                        groupname = dataSnapshot.child("name").getValue().toString();
                                        groupimage =
                                                dataSnapshot.child("image").getValue() != null ? dataSnapshot.child("image").getValue().toString() : null;


                                        viewHolder.setGroupimage(Groups.this, groupimage);
                                        viewHolder.setGroupname(groupname);
                                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                //open all the groups in this group category
                                                Intent message_this_user = new Intent(Groups.this, AssociatedGroups.class);
                                                message_this_user.putExtra("group_cat", groupname);
                                                startActivity(message_this_user);
                                                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                            }
                                        });


                                        usersRef.child(currentUserID)
                                                .child("userState").addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists()){
                                                    if(dataSnapshot.hasChild("usertype")){

                                                        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                                                            @Override
                                                            public boolean onLongClick(View v) {
                                                                CharSequence[] options_doc = new CharSequence[]{
                                                                        "Update this collection",
                                                                        "Add group to this collection",
                                                                        "Delete this this collection",
                                                                        "Cancel"
                                                                };
                                                                AlertDialog.Builder builderoptions_doc = 
                                                                        new AlertDialog.Builder(Groups.this);
                                                                builderoptions_doc.setTitle("What do you want to do?");

                                                                builderoptions_doc.setItems(options_doc, new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {

                                                                        if(which == 0){
                                                                           //update group collection
                                                                            updateGroupCollection(collectionKey,groupname
                                                                                    ,groupimage);
                                                                            dialog.dismiss();

                                                                        }
                                                                        else  if(which == 1){
                                                                           //add group to collection
                                                                            addGroupToCollection(collectionKey);
                                                                            dialog.dismiss();

                                                                        }else if(which == 2){
                                                                            //delete group collection
                                                                            android.support.v7.app.AlertDialog.Builder alertDialogBuilder =
                                                                                    new android.support.v7.app.AlertDialog.Builder(Groups.this);
                                                                            alertDialogBuilder.setMessage(Html.
                                                                                    fromHtml("Are you sure you want to delete this group collection? " +
                                                                                            "<br>All the groups within this collection would be deleted and this process" +
                                                                                            " cannot be reversed!"));
                                                                            alertDialogBuilder.setCancelable(false);
                                                                            alertDialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(DialogInterface dialog, int which) {
                                                                                    deleteGroupCollection(collectionKey,groupimage);
                                                                                }
                                                                            }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(DialogInterface dialog, int which) {
                                                                                    dialog.dismiss();
                                                                                }
                                                                            });
                                                                            final android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();

                                                                            alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                                                                                @SuppressLint("ResourceAsColor")
                                                                                @Override
                                                                                public void onShow(DialogInterface arg0) {
                                                                                    alertDialog.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE).setTextColor(R.color.colorPrimary);
                                                                                    alertDialog.getButton(android.support.v7.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(R.color.colorPrimary);
                                                                                }
                                                                            });
                                                                            alertDialog.show();
                                                                        }
                                                                    }
                                                                });
                                                                builderoptions_doc.show();
                                                                return false;
                                                            }
                                                        });
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                        groupsRef.child(collectionKey)
                                                .child("groups").addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists()){
                                                    int countGroups = (int)dataSnapshot.getChildrenCount();
                                                    viewHolder.setGroupNoOfGroups(Integer.toString(countGroups));
                                                }else {
                                                    viewHolder.setGroupNoOfGroups("0");
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

    private void addGroupToCollection(String collectionKey){
        Intent addGroupToCollection = new Intent(Groups.this, AddGroupToCollection.class);
        addGroupToCollection.putExtra("collectionkey",collectionKey);
        startActivity(addGroupToCollection);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    private void updateGroupCollection(String collectionKey,String collection_name,
                                       String collectionimage) {
        Intent updatecollection = new Intent(Groups.this, UpdateGroupCollection.class);
        updatecollection.putExtra("collectionkey",collectionKey);
        updatecollection.putExtra("collectionname",collection_name);
        updatecollection.putExtra("collectionimage",collectionimage);
        startActivity(updatecollection);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    private void deleteGroupCollection(String collectionKey,String collection_image) {
        final ProgressDialog dialog = new ProgressDialog(Groups.this);
        dialog.setMessage("Deleting collection...");
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();

        final StorageReference imageRef = firebaseStorage.getReferenceFromUrl(collection_image);
        groupsRef.child(collectionKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                //delete collection image
                imageRef.delete();

                Toasty.success(Groups.this,"Group Collection deleted successfully!",
                        Toasty.LENGTH_SHORT).show();
                dialog.dismiss();
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
            TextView  group_name = itemView.findViewById(R.id.group_name);
            group_name.setText(groupname);
        }

        @SuppressLint("SetTextI18n")
        public void setGroupNoOfGroups(String noOfGroupsinCollection) {
            TextView no_of_groups_in_collection = itemView.findViewById(R.id.no_of_groups_in_collection);
            if(noOfGroupsinCollection.equals("1")){
                no_of_groups_in_collection.setText(noOfGroupsinCollection + " Group");
            }else{
                no_of_groups_in_collection.setText(noOfGroupsinCollection + " Groups");
            }
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
                firebaseCollectionSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                firebaseCollectionSearch(newText);
                return false;
            }
        });
        return  super.onCreateOptionsMenu(menu);
    }

    public void firebaseCollectionSearch(String searchText){
        Query firebaseSearchQuery = groupsRef.orderByChild("name").startAt(searchText).endAt(searchText + "\uf0ff");

        FirebaseRecyclerAdapter<GroupCatModel, GroupsViewHolder> firebaseRecyclerAdapter = new
                FirebaseRecyclerAdapter<GroupCatModel, GroupsViewHolder>
                        (
                                GroupCatModel.class,
                                R.layout.groups_display_layout,
                                GroupsViewHolder.class,
                                firebaseSearchQuery
                        )
                {
                    @Override
                    protected void populateViewHolder(final GroupsViewHolder viewHolder, GroupCatModel model, int position) {


                        final String collectionKey = getRef(position).getKey();
                        groupsRef.child(collectionKey).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(dataSnapshot.exists()) {

                                    final String groupname, groupimage;
                                    if (dataSnapshot.hasChild("image") && dataSnapshot.hasChild("name")) {


                                        groupname = dataSnapshot.child("name").getValue().toString();
                                        groupimage =
                                                dataSnapshot.child("image").getValue() != null ? dataSnapshot.child("image").getValue().toString() : null;

                                        viewHolder.setGroupimage(Groups.this, groupimage);
                                        viewHolder.setGroupname(groupname);
                                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                //open all the groups in this group category
                                                Intent message_this_user = new Intent(Groups.this, AssociatedGroups.class);
                                                message_this_user.putExtra("group_cat", groupname);
                                                startActivity(message_this_user);
                                                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                            }
                                        });


                                        usersRef.child(currentUserID)
                                                .child("userState").addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists()){
                                                    if(dataSnapshot.hasChild("usertype")){

                                                        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                                                            @Override
                                                            public boolean onLongClick(View v) {
                                                                CharSequence[] options_doc = new CharSequence[]{
                                                                        "Update this collection",
                                                                        "Add group to this collection",
                                                                        "Delete this this collection",
                                                                        "Cancel"
                                                                };
                                                                AlertDialog.Builder builderoptions_doc =
                                                                        new AlertDialog.Builder(Groups.this);
                                                                builderoptions_doc.setTitle("What do you want to do?");

                                                                builderoptions_doc.setItems(options_doc, new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {

                                                                        if(which == 0){
                                                                            //update group collection
                                                                            updateGroupCollection(collectionKey,groupname
                                                                                    ,groupimage);
                                                                        }
                                                                        else  if(which == 1){
                                                                            //add group to collection
                                                                            addGroupToCollection(collectionKey);
                                                                        }else if(which == 2){
                                                                            //delete group collection
                                                                            android.support.v7.app.AlertDialog.Builder alertDialogBuilder =
                                                                                    new android.support.v7.app.AlertDialog.Builder(Groups.this);
                                                                            alertDialogBuilder.setMessage(Html.
                                                                                    fromHtml("Are you sure you want to delete this group collection? " +
                                                                                            "<br>All the groups within this collection would be deleted and this process" +
                                                                                            " cannot be reversed!"));
                                                                            alertDialogBuilder.setCancelable(false);
                                                                            alertDialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(DialogInterface dialog, int which) {
                                                                                    deleteGroupCollection(collectionKey,groupimage);
                                                                                }
                                                                            }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                                                                @Override
                                                                                public void onClick(DialogInterface dialog, int which) {
                                                                                    dialog.dismiss();
                                                                                }
                                                                            });
                                                                            final android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();

                                                                            alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
                                                                                @SuppressLint("ResourceAsColor")
                                                                                @Override
                                                                                public void onShow(DialogInterface arg0) {
                                                                                    alertDialog.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE).setTextColor(R.color.colorPrimary);
                                                                                    alertDialog.getButton(android.support.v7.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(R.color.colorPrimary);
                                                                                }
                                                                            });
                                                                            alertDialog.show();
                                                                        }
                                                                    }
                                                                });
                                                                builderoptions_doc.show();
                                                                return false;
                                                            }
                                                        });
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                        groupsRef.child(collectionKey)
                                                .child("groups").addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists()){
                                                    int countGroups = (int)dataSnapshot.getChildrenCount();
                                                    viewHolder.setGroupNoOfGroups(Integer.toString(countGroups));
                                                }else {
                                                    viewHolder.setGroupNoOfGroups("0");
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