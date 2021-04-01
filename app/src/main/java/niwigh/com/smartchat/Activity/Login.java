package niwigh.com.smartchat.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import niwigh.com.smartchat.R;

public class Login extends AppCompatActivity {
    TextView login_recover_text;
    TextView login_register_text;
    Button login_btn;
    EditText login_email;
    EditText login_password;
    FirebaseAuth mAuth;
    ProgressDialog loadingBar;
    DatabaseReference usersRef;
    FirebaseFirestore firebaseFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        firebaseFirestore = FirebaseFirestore.getInstance();
        //init views
        login_recover_text = findViewById(R.id.login_recover_password);
        login_register_text = findViewById(R.id.login_register_here_text);
        login_btn = findViewById(R.id.btn_login);
        login_email = findViewById(R.id.login_input_email);
        login_password = findViewById(R.id.login_input_password);
        loadingBar = new ProgressDialog(this);

        //login_recover_text click listener
        login_recover_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent recover = new Intent(getApplicationContext(), RecoverPassword.class);
                startActivity(recover);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
            }
        });

        //login_register_text click listener
        login_register_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent register = new Intent(getApplicationContext(), Register.class);
                startActivity(register);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
            }
        });

        //login btn click listener
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });
    }




    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //send user to home activity
            sendUserToHomeActivity();
        }
    }

    private void performLogin() {
        final String email_login, pass_login;
        email_login = login_email.getText().toString().trim();
        pass_login = login_password.getText().toString().trim();

        if(email_login.isEmpty() || pass_login.isEmpty() ){
            showErrorCustomDialog();
        }
        else {
            if(validateEmail(email_login)) {

                //perform login
                loadingBar.setTitle("Login in...");
                loadingBar.setMessage("a moment please");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(false);

                mAuth.signInWithEmailAndPassword(email_login, pass_login)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){


                                    FirebaseInstanceId.getInstance().getInstanceId()
                                            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                                    if(task.isSuccessful()){
                                                        final String DeviceToken = task.getResult().getToken();

                                                        String current_user_id = mAuth.getCurrentUser().getUid();
                                                        Map<String, Object> tokenMap = new HashMap<>();
                                                        tokenMap.put("token_id", DeviceToken);
                                                        firebaseFirestore.collection("Users").document(current_user_id)
                                                                .update(tokenMap);
                                                        sendUserToHomeActivity();
                                                        loadingBar.dismiss();
                                                    }
                                                }
                                            });



                                }
                                else {
                                    String message = task.getException().getMessage();
                                    //{.....................
                                    //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                                    ViewGroup viewGroup = findViewById(android.R.id.content);

                                    //then we will inflate the custom alert dialog xml that we created
                                    View dialogView = LayoutInflater.from(Login.this).inflate(R.layout.error_dialog, viewGroup, false);


                                    //Now we need an AlertDialog.Builder object
                                    AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);

                                    //setting the view of the builder to our custom view that we already inflated
                                    builder.setView(dialogView);

                                    //finally creating the alert dialog and displaying it
                                    final AlertDialog alertDialog = builder.create();

                                    Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
                                    TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
                                    TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

                                    dialog_btn.setText("OK");
                                    success_title.setText("Error");
                                    success_text.setText(message);

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
            else {
                //display invalid email error
                invalid_email_address_erro_CustomDialog();
            }
        }
    }



    private void sendUserToHomeActivity() {
        Intent userhomeIntent = new Intent(getApplicationContext(), UserHome.class);
        startActivity(userhomeIntent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        finish();
    }

    private boolean validateEmail(String data){
        Pattern emailPattern = Pattern.compile(".+@.+\\.[a-z]+");
        Matcher emailMatcher = emailPattern.matcher(data);
        return emailMatcher.matches();
    }

    private void showErrorCustomDialog() {
        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(this).inflate(R.layout.error_dialog, viewGroup, false);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);

        //finally creating the alert dialog and displaying it
        final AlertDialog alertDialog = builder.create();

        Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
        TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
        TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

        dialog_btn.setText("OK");
        success_title.setText("Error");
        success_text.setText("Your login was not successful! Make sure all fields are filled up correctly or check your internet connection.");

        // if the OK button is clicked, close the success dialog
        dialog_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();


    }

    private void invalid_email_address_erro_CustomDialog() {
        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(this).inflate(R.layout.error_dialog, viewGroup, false);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);

        //finally creating the alert dialog and displaying it
        final AlertDialog alertDialog = builder.create();

        Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
        TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
        TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

        dialog_btn.setText("OK");
        success_title.setText("Error");
        success_text.setText("Your E-mail address is invalid! Fix it and try again.");

        // if the OK button is clicked, close the success dialog
        dialog_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();


    }
}