package niwigh.com.smartchat.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;
import niwigh.com.smartchat.R;

public class Register extends AppCompatActivity {
    TextView register_login_text;
    Button btn_register;
    TextInputEditText register_email;
    TextInputEditText register_password;
    FirebaseAuth mAuth;
    ProgressDialog loadingBar;
    DatabaseReference usersRef;
    FirebaseFirestore firebaseFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        firebaseFirestore = FirebaseFirestore.getInstance();

        //init views
        register_login_text = findViewById(R.id.register_login_here_text);
        btn_register = findViewById(R.id.btn_register);
        register_email = findViewById(R.id.register_input_email);
        loadingBar = new ProgressDialog(this);
        register_password = findViewById(R.id.register_input_password);



        // register_login_text click listener
        register_login_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
            }
        });

        //btn_register click listener
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegisteration();
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

    private void sendUserToHomeActivity() {
        Intent userhomeIntent = new Intent(getApplicationContext(), UserHome.class);
        startActivity(userhomeIntent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        finish();
    }

    public void performRegisteration(){
        final String email_reg, pass_reg;
        email_reg = register_email.getText().toString();
        pass_reg = register_password.getText().toString();

        if(email_reg.isEmpty() || pass_reg.isEmpty() ){
            showErrorCustomDialog();
        }
        else {
            if(validateEmail(email_reg)) {

                if(pass_reg.length() > 5 ){

                    //perform registeration
                    loadingBar.setTitle("Creating account...");
                    loadingBar.setMessage("a moment please");
                    loadingBar.show();
                    loadingBar.setCanceledOnTouchOutside(false);

                    mAuth.createUserWithEmailAndPassword(email_reg, pass_reg).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                sendUserToSetupActivity();
                                loadingBar.dismiss();
                                successToast();

                            }
                            else {
                                String message = task.getException().getMessage();
                                //{.....................
                                //before inflating the custom alert dialog layout, we will get the current activity viewgroup
                                ViewGroup viewGroup = findViewById(android.R.id.content);

                                //then we will inflate the custom alert dialog xml that we created
                                View dialogView = LayoutInflater.from(Register.this).inflate(R.layout.error_dialog, viewGroup, false);


                                //Now we need an AlertDialog.Builder object
                                AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);

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
                    //display password too short error dialog
                    password_too_short_dialog();
                }

            }
            else {
                //display invalid email error
                invalid_email_address_erro_CustomDialog();
            }
        }
    }

    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent(getApplicationContext(), RegisterSetup.class);
        startActivity(setupIntent);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        finish();
    }



    private boolean validateEmail(String data){
        Pattern emailPattern = Pattern.compile(".+@.+\\.[a-z]+");
        Matcher emailMatcher = emailPattern.matcher(data);
        return emailMatcher.matches();
    }

    private void successToast(){
        Toasty.success(this,"Sign Up Successful.", Toasty.LENGTH_LONG, true).show();
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
        success_text.setText("Your registeration was not successful! Make sure all fields are filled up correctly or check your internet connection.");

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

    private void password_too_short_dialog() {
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
        success_text.setText("Your password is too short! Fix it and try again.");

        // if the OK button is clicked, close the success dialog
        dialog_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
        finish();
    }
}