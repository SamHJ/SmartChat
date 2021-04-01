package niwigh.com.smartchat.Activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
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
import java.util.HashMap;
import java.util.Map;

import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;


public class GroupSettings extends AppCompatActivity {

    Switch bookmark_switch;
    RelativeLayout settingsLayout;
    DatabaseReference usersRef;
    String currentUserID;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_settings);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();


        String groupName = getIntent().getStringExtra("group_Name");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(groupName+" settings");

        settingsLayout = findViewById(R.id.settings_layout);

        bookmark_switch = findViewById(R.id.bookmark_switch);

        bookmark_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()){
                    convertGroupToAdmins();
                }else {
                    convertGroupToAllParticipants();
                }
            }
        });

    }

    private void convertGroupToAllParticipants() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Converting group");
        progressDialog.setMessage("A moment please");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();

        String group_category = getIntent().getStringExtra("group_category");
        String group_key = getIntent().getStringExtra("group_key");

        final DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_category)
                .child("groups").child(group_key);

        groupsRef.child("isgroupforadminsonly").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    bookmark_switch.setChecked(false);
                    progressDialog.dismiss();
                    final Snackbar snackbar = Snackbar
                            .make(settingsLayout, "All participants can chat in this group!", Snackbar.LENGTH_SHORT);

                    snackbar.setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            snackbar.dismiss();
                        }
                    });

                    snackbar.setActionTextColor(getResources().getColor(R.color.colorAccent));
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(getResources().getColor(R.color.intro_slide_bg_color));
                    snackbar.show();
                }else {
                    bookmark_switch.setChecked(true);
                    //{.....................
                    //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                    ViewGroup viewGroup = findViewById(android.R.id.content);

                    //then we will inflate the custom alert dialog xml that we created
                    View dialogView = LayoutInflater.from(GroupSettings.this).inflate(R.layout.error_dialog, viewGroup, false);


                    //Now we need an AlertDialog.Builder object
                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupSettings.this);

                    //setting the view of the builder to our custom view that we already inflated
                    builder.setView(dialogView);

                    //finally creating the alert dialog and displaying it
                    final AlertDialog alertDialog = builder.create();

                    Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
                    TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
                    TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

                    dialog_btn.setText("OK");
                    success_title.setText("Error");
                    success_text.setText("An error occurred!");

                    // if the OK button is clicked, close the success dialog
                    dialog_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.show();
                    //...................}
                    progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                bookmark_switch.setChecked(true);
                //{.....................
                //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                ViewGroup viewGroup = findViewById(android.R.id.content);

                //then we will inflate the custom alert dialog xml that we created
                View dialogView = LayoutInflater.from(GroupSettings.this).inflate(R.layout.error_dialog, viewGroup, false);


                //Now we need an AlertDialog.Builder object
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupSettings.this);

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
                progressDialog.dismiss();
            }
        });

    }

    private void convertGroupToAdmins() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Converting group");
        progressDialog.setMessage("A moment please");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.show();

        String group_category = getIntent().getStringExtra("group_category");
        String group_key = getIntent().getStringExtra("group_key");

        final DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_category)
                .child("groups").child(group_key);

        Map<String,Object> groupSettingsMap = new HashMap<>();
        groupSettingsMap.put("isgroupforadminsonly",true);
        groupsRef.updateChildren(groupSettingsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    bookmark_switch.setChecked(true);
                    progressDialog.dismiss();
                    final Snackbar snackbar = Snackbar
                            .make(settingsLayout, "Only admins can chat in this group!", Snackbar.LENGTH_SHORT);

                    snackbar.setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            snackbar.dismiss();
                        }
                    });

                    snackbar.setActionTextColor(getResources().getColor(R.color.colorAccent));
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(getResources().getColor(R.color.intro_slide_bg_color));
                    snackbar.show();
                }else {
                    bookmark_switch.setChecked(false);
                    //{.....................
                    //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                    ViewGroup viewGroup = findViewById(android.R.id.content);

                    //then we will inflate the custom alert dialog xml that we created
                    View dialogView = LayoutInflater.from(GroupSettings.this).inflate(R.layout.error_dialog, viewGroup, false);


                    //Now we need an AlertDialog.Builder object
                    AlertDialog.Builder builder = new AlertDialog.Builder(GroupSettings.this);

                    //setting the view of the builder to our custom view that we already inflated
                    builder.setView(dialogView);

                    //finally creating the alert dialog and displaying it
                    final AlertDialog alertDialog = builder.create();

                    Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
                    TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
                    TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

                    dialog_btn.setText("OK");
                    success_title.setText("Error");
                    success_text.setText("An error occurred!");

                    // if the OK button is clicked, close the success dialog
                    dialog_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.show();
                    //...................}
                    progressDialog.dismiss();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                bookmark_switch.setChecked(false);
                //{.....................
                //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                ViewGroup viewGroup = findViewById(android.R.id.content);

                //then we will inflate the custom alert dialog xml that we created
                View dialogView = LayoutInflater.from(GroupSettings.this).inflate(R.layout.error_dialog, viewGroup, false);


                //Now we need an AlertDialog.Builder object
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupSettings.this);

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
                progressDialog.dismiss();
            }
        });
    }

    private void checkGroupStatus() {
        String group_category = getIntent().getStringExtra("group_category");
        String group_key = getIntent().getStringExtra("group_key");

        DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(group_category)
                .child("groups").child(group_key);

        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("isgroupforadminsonly")){
                        bookmark_switch.setChecked(true);
                    }else {
                        bookmark_switch.setChecked(false);
                    }
                }else {
                    bookmark_switch.setChecked(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        checkGroupStatus();
        Utilities.getInstance(this).updateUserStatus("Online");
    }

    @Override
    public void onResume() {
        super.onResume();
        checkGroupStatus();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.right_out);
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
        finish();
    }
}