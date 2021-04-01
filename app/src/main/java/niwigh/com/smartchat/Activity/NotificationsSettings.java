package niwigh.com.smartchat.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import niwigh.com.smartchat.R;
import niwigh.com.smartchat.Util.Utilities;


public class NotificationsSettings extends AppCompatActivity {
    Switch bookmark_switch;
    RelativeLayout settingsLayout;
    DatabaseReference usersRef;
    FirebaseAuth mAuth;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_settings);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        usersRef.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Notifications");



        settingsLayout = findViewById(R.id.settings_layout);

        bookmark_switch = findViewById(R.id.bookmark_switch);
        SharedPreferences pref = NotificationsSettings.this.getSharedPreferences("disablebookmark", 0);
        boolean isbookmarkavailable = pref.getBoolean("isbookmarkavailable", true);
        if(isbookmarkavailable){
            bookmark_switch.setChecked(false);
        }else {
            bookmark_switch.setChecked(true);
        }
        bookmark_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.isChecked()){
                    disable_notifications();
                }else {
                    enable_notifications();
                }
            }
        });

    }

    public void enable_notifications(){

        SharedPreferences pref = NotificationsSettings.this.getSharedPreferences("disablenotifications", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isnotificationsavailable", true);
        editor.apply();

        final Snackbar snackbar = Snackbar
                .make(settingsLayout, "Notifications Enabled!", Snackbar.LENGTH_SHORT);

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
    }

    private void disable_notifications() {
        SharedPreferences pref = NotificationsSettings.this.getSharedPreferences("disablenotifications", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isnotificationsavailable", false);
        editor.apply();

        final Snackbar snackbar = Snackbar
                .make(settingsLayout, "Notifications Disabled!", Snackbar.LENGTH_SHORT);

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
        sendUserToSettingsActivity();
    }

    public void sendUserToSettingsActivity() {
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
        finish();
    }
}