package niwigh.com.smartchat.Fragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import niwigh.com.smartchat.Activity.DataSub;
import niwigh.com.smartchat.Activity.FullMessageImageView;
import niwigh.com.smartchat.Activity.Groups;
import niwigh.com.smartchat.Model.FeedsModel;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.UpdateHelper;
import niwigh.com.smartchat.Util.Utilities;



/**
 * A simple {@link Fragment} subclass.
 */
public class FeedsFragment extends Fragment implements UpdateHelper.OnUpdateCheckListener{

    Toolbar mToolbar;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;

    FirebaseAuth mAuth;
    DatabaseReference UserRef,FeedsRef;
    String currentUserID;
    RecyclerView feedsList;


    public FeedsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View feedsFragmentView =  inflater.inflate(R.layout.fragment_feeds, container, false);
        setHasOptionsMenu(true);

        //check for new app updates
        UpdateHelper.with(getContext())
                .onUpdateCheck(this)
                .check();

        mToolbar = feedsFragmentView.findViewById(R.id.userhome_navigation_opener_include);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Feeds");
        ((AppCompatActivity)getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowCustomEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        UserRef.keepSynced(true);
        FeedsRef = FirebaseDatabase.getInstance().getReference().child("Feeds");
        FeedsRef.keepSynced(true);

        drawerLayout = feedsFragmentView.findViewById(R.id.all_posts_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, R.string.drawer_open, R.string.drawer_close);


        feedsList = feedsFragmentView.findViewById(R.id.feeds_list);
        feedsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        feedsList.setLayoutManager(linearLayoutManager);

        FetchFeeds();

        return feedsFragmentView;
    }

    public void FetchFeeds(){
        FirebaseRecyclerAdapter<FeedsModel, FeedsViewHolder> firebaseRecyclerAdapter = new
                FirebaseRecyclerAdapter<FeedsModel, FeedsViewHolder>
                        (
                                FeedsModel.class,
                                R.layout.feeds_layout,
                                FeedsViewHolder.class,
                                FeedsRef
                        )
                {
                    @Override
                    protected void populateViewHolder(final FeedsViewHolder viewHolder, FeedsModel model, int position) {


                        final String usersIDs = getRef(position).getKey();
                        FeedsRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {

                                if(dataSnapshot.exists()) {

                                    try {

                                        final String feedImage, feedUrl, feedTitle, feedtype;

                                        if (dataSnapshot.hasChild("type") && dataSnapshot.hasChild("image_url")
                                                && dataSnapshot.hasChild("url") && dataSnapshot.hasChild("title")) {

                                            try {

                                                feedtype = dataSnapshot.child("type").getValue().toString();
                                                feedImage = dataSnapshot.child("image_url").getValue().toString();
                                                feedUrl = dataSnapshot.child("url").getValue().toString();
                                                feedTitle = dataSnapshot.child("title").getValue().toString();
                                                final String feedimagestoragename = dataSnapshot.child("feedimagestoragename").getValue() != null ?
                                                        dataSnapshot.child("feedimagestoragename").getValue().toString() : "";


                                                viewHolder.setFeedimage(getActivity(), feedImage);

                                                final String finalFeedtype = feedtype;
                                                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                        if (finalFeedtype.equals("url")) {
                                                            openWebPage(feedUrl);
                                                        } else {
                                                            Intent feedIntent = new Intent(getActivity(), FullMessageImageView.class);
                                                            feedIntent.putExtra("url", feedImage);
                                                            feedIntent.putExtra("name", feedTitle);
                                                            startActivity(feedIntent);
                                                            getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                                        }

                                                    }
                                                });

                                                UserRef.child(currentUserID).child("userState")
                                                        .addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot mSnapshots) {
                                                                if (mSnapshots.exists()) {
                                                                    if (mSnapshots.hasChild("usertype")) {

                                                                        viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                                                                            @Override
                                                                            public boolean onLongClick(View v) {
                                                                                final AlertDialog alertDialog = new
                                                                                        AlertDialog.Builder(getContext()).create();
                                                                                alertDialog.setMessage("Delete this feed");
                                                                                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                                                        alertDialog.dismiss();
                                                                                    }
                                                                                });
                                                                                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                                        deleteFeed(dataSnapshot.getKey(), feedimagestoragename);
                                                                                    }
                                                                                });
                                                                                alertDialog.show();
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
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }
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

        feedsList.setAdapter(firebaseRecyclerAdapter);
    }

    private void deleteFeed(String key, String feedimagestoragename) {
        try {
            FeedsRef.child(key).removeValue();
            StorageReference postimageRef = FirebaseStorage.getInstance().getReference()
                    .child("Feed Images").child(feedimagestoragename);
            postimageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    Log.d("SUCCESS:", "onSuccess: deleted file");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Log.d("ERROR:", "onFailure: did not delete file");
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

        Toasty.success(getContext(),"Feed deleted successfully!",Toasty.LENGTH_SHORT).show();
    }


    public void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(Objects.requireNonNull(getActivity()).getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onUpdateCheckListener(String urlApp, String versionApp) {
        showUpdateCustomDialog(versionApp, urlApp);
    }

    @SuppressLint("InflateParams")
    private void showUpdateCustomDialog(String currentVersion, final String urlApp) {
        //before inflating the custom alert dialog layout, we will get the current activity viewgroup

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.update_dialog, null);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);

        //finally creating the alert dialog and displaying it
        final AlertDialog alertDialog = builder.create();

        Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
        TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
        success_text.setText("A newer version of SmartChat, version " +currentVersion + ", is now available on App stores!");

        // if the OK button is clicked, close the success dialog
        dialog_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String appPackageName = getContext().getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="
                            + appPackageName)));
                }
            }
        });
        alertDialog.show();
    }


    public static class FeedsViewHolder extends RecyclerView.ViewHolder {

        ImageView feedImage;
        View mView;

        public FeedsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            feedImage = itemView.findViewById(R.id.feed_image);
        }

        public void setFeedimage(final Context ctx, final String feedImageUrl){
           try{
               Picasso.with(ctx).load(feedImageUrl).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.easy_to_use)
                       .into(feedImage, new Callback() {
                           @Override
                           public void onSuccess() {

                           }

                           @Override
                           public void onError() {

                               Picasso.with(ctx).load(feedImageUrl).placeholder(R.drawable.easy_to_use).into(feedImage);
                           }
                       });
           }catch (Exception e){
               e.printStackTrace();
           }
        }

    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.data_sub_menu, menu);
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
            case R.id.data_sub:
                Intent data_sub_intent = new Intent(getContext(), DataSub.class);
                startActivity(data_sub_intent);
                getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FetchFeeds();
        try {
            Utilities.getInstance(getContext()).updateUserStatus("Online");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        FetchFeeds();
        try {
            Utilities.getInstance(getContext()).updateUserStatus("Online");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            Utilities.getInstance(getContext()).updateUserStatus("Offline");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            Utilities.getInstance(getContext()).updateUserStatus("Offline");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            Utilities.getInstance(getContext()).updateUserStatus("Offline");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}