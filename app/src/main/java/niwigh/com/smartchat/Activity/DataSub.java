package niwigh.com.smartchat.Activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import niwigh.com.smartchat.Fragment.RequestsFragment;
import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;

import static niwigh.com.smartchat.Util.AppConstants.ADMIN_ID;

public class DataSub extends AppCompatActivity {

    private Toolbar toolbar;
    DatabaseReference usersRef,FriendsRef;
    FirebaseAuth mAuth;
    String currentUserID,saveCurrentDate;
    TextInputEditText data_bundle_full_name,data_bundle_phone_number,data_bundle_transaction_id,
            network_type,data_bundle;
    Button btn_buy_data_bundle;
    String[] networkType = { "MTN","Vodafone","AirtelTigo"};
    String[] dataBundlesMTN  = { "1GB (GHC 7)","2GB (GHC 13)","5GB (GHC 30)","10GB (GHC 55)","20GB (GHC 110)","30GB (GHC 165)"};
    String[] dataBundlesVODAFONE = { "2.5GB (GHC 22)","3.8GB (GHC 33)","5.0GB (GHC 43)","6.3GB (GHC 52)","10.0GB (GHC 80)"};
    String[] dataBundlesAIRTEL = { "1.6GB + 1.6GB BONUS (GHC 11)","4.3GB + 4.3GB (GHC 21)","8.6GB + 8.6GB (GHC 39)","10.4GB + 10.4GB (GHC 50)","20.8GB + 20.8GB (GHC 91)","31.2GB + 31.2GB (GHC 138)","41.6GB + 41.6GB (GHC 171)"};
    Utilities utilities;
    String[] selectedDataBundle = dataBundlesMTN;
    boolean hasSentBefore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_sub);
        init();
    }
    
    private void init(){
        utilities = Utilities.getInstance(this);

        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendsRef.keepSynced(true);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Data Subscription");

        network_type = findViewById(R.id.network_type);
        network_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder_options_network_type = new AlertDialog.Builder(DataSub.this);
                builder_options_network_type.setTitle("Select Network Type");

                builder_options_network_type.setItems(networkType, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        network_type.setText(networkType[which]);
                        if(which==0){
                            selectedDataBundle = dataBundlesMTN;
                        }else if(which==1){
                            selectedDataBundle = dataBundlesVODAFONE;
                        }else{
                            selectedDataBundle = dataBundlesAIRTEL;
                        }
                        data_bundle.setText(selectedDataBundle[0]);
                    }
                });
                builder_options_network_type.show();
            }
        });

        data_bundle = findViewById(R.id.data_bundle);
        data_bundle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder_options_network_type = new AlertDialog.Builder(DataSub.this);
                builder_options_network_type.setTitle("Select Subscription");

                builder_options_network_type.setItems(selectedDataBundle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        data_bundle.setText(selectedDataBundle[which]);
                    }
                });
                builder_options_network_type.show();
            }
        });
        
        data_bundle_full_name = findViewById(R.id.data_bundle_full_name);
        data_bundle_phone_number = findViewById(R.id.data_bundle_phone_number);
        data_bundle_transaction_id = findViewById(R.id.data_bundle_transaction_id);
        btn_buy_data_bundle = findViewById(R.id.btn_buy_data_bundle);

        data_bundle_full_name.setText("");

        btn_buy_data_bundle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //notice dialog
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DataSub.this);
                alertDialogBuilder.setTitle("Confirm");
                alertDialogBuilder.setMessage("Are you sure you have made the payment for this data bundle?");
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("Yes, I have", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //proceed
                        proceedBuyBundle();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });


        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    try{
                        String user_fullname = String.valueOf(dataSnapshot.child("fullname").getValue());
                        String user_name = String.valueOf(dataSnapshot.child("username").getValue());
                        String userName = !user_fullname.isEmpty()?user_fullname:user_name;
                        data_bundle_full_name.setText(userName);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        network_type.setText(networkType[0]);
        data_bundle.setText(selectedDataBundle[0]);
    }
    
    private void proceedBuyBundle(){
        String network = network_type.getText().toString().trim();
        String dataBundle = data_bundle.getText().toString().trim();
        String name = data_bundle_full_name.getText().toString().trim();
        String phone = data_bundle_phone_number.getText().toString().trim();
        String transactionID = data_bundle_transaction_id.getText().toString().trim();

        if(name.isEmpty() || phone.isEmpty() || transactionID.isEmpty()){
            utilities.dialogError(DataSub.this,"All fields are required!");
        }else{
            if(!ADMIN_ID.equals(currentUserID)) {
                final String message = "Hi! My name is <b>" + name + "</b> and my phone number is <b>" + phone + "</b>." +
                        "<br><br>I want to purchase <b>" + network + "</b> data bundle/subscription of <b>" + dataBundle +"</b>." +
                        "<br><br>I have made payment and my transaction ID is <b>" + transactionID+"</b>";

                //setup friend request if they are not yet friends
                if(!AreFriends()){
                    AcceptFriendRequest(currentUserID,ADMIN_ID);
                }

                usersRef.child(ADMIN_ID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!hasSentBefore) {
                            if (dataSnapshot.exists()) {

                                hasSentBefore = true;

                                final String fullName = dataSnapshot.child("fullname").getValue().toString();
                                final String userName = dataSnapshot.child("username").getValue().toString();

                                Intent message_this_user = new Intent(DataSub.this, MessagingArea.class);
                                message_this_user.putExtra("visit_user_id", ADMIN_ID);
                                message_this_user.putExtra("userName", userName);
                                message_this_user.putExtra("userFullName", fullName);
                                message_this_user.putExtra("data_sub_message", message);
                                message_this_user.putExtra("fromDataSub", "yes");
                                startActivity(message_this_user);
                                overridePendingTransition(R.anim.right_in, R.anim.left_out);

                            } else {
                                utilities.dialogError(DataSub.this, "Sorry! Can't make this request at this moment!");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        utilities.dialogError(DataSub.this, "ERROR: " + databaseError.getMessage());
                    }
                });

            }else{
                utilities.dialogError(DataSub.this,"Sorry! You can't send a message to yourself!");
            }

        }
    }

    public boolean AreFriends(){
        final boolean[] areFriends = {false};

        FriendsRef.child(ADMIN_ID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(currentUserID)){
                    areFriends[0] = true;
                }else{
                    areFriends[0] = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                areFriends[0] = false;
            }
        });

        return areFriends[0];
    }

    public void AcceptFriendRequest(final String senderUserID, final String receiverUserID) {
        try {
            //for date
            Calendar calFordDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currentDate.format(calFordDate.getTime());

            usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        try {
                            String friednusername = dataSnapshot.child("username").getValue().toString();
                            final Map<String, Object> friendsMap = new HashMap<String, Object>();
                            friendsMap.put("friendsname", friednusername);
                            friendsMap.put("date", saveCurrentDate);

                            usersRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        try {
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
                                                                    FriendsRef.child(receiverUserID).child(senderUserID).updateChildren(onlineUserfriendsMap);
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
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
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
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
    protected void onStart() {
        super.onStart();
        hasSentBefore = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        hasSentBefore = false;
    }
}