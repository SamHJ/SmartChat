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
import com.google.firebase.auth.FirebaseAuth;

import es.dmoral.toasty.Toasty;
import niwigh.com.smartchat.R;

public class RecoverPassword extends AppCompatActivity {
    TextView register_login_text;
    EditText forgot_email_input_edit_text;
    Button btn_password_recovery;
    FirebaseAuth mAuth;
    ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_recover_password);

        mAuth = FirebaseAuth.getInstance();

        //init views
        register_login_text = findViewById(R.id.register_login_here_text);
        forgot_email_input_edit_text = findViewById(R.id.forgot_input_email);
        btn_password_recovery = findViewById(R.id.btn_password_recovery);
        loadingBar = new ProgressDialog(this);

        btn_password_recovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String resetPasswordEmail = forgot_email_input_edit_text.getText().toString();

                    if (resetPasswordEmail.isEmpty()) {
                        showErroCustomDialog();
                    } else {

                        loadingBar.setTitle("Validating email");
                        loadingBar.setMessage("Please wait while your email address is been validated!");
                        loadingBar.show();
                        loadingBar.setCanceledOnTouchOutside(false);

                        mAuth.sendPasswordResetEmail(resetPasswordEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {
                                    try {
                                        recover_success_toast();
                                        startActivity(new Intent(RecoverPassword.this, Login.class));
                                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                                        finish();
                                        loadingBar.dismiss();
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                } else {
                                    try {
                                        String message = task.getException().getMessage();
                                        // {...........before inflating the custom alert dialog layout, we will get the current activity viewgroup
                                        ViewGroup viewGroup = findViewById(android.R.id.content);

                                        //then we will inflate the custom alert dialog xml that we created
                                        View dialogView = LayoutInflater.from(RecoverPassword.this).inflate(R.layout.error_dialog, viewGroup, false);


                                        //Now we need an AlertDialog.Builder object
                                        AlertDialog.Builder builder = new AlertDialog.Builder(RecoverPassword.this);

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
                                        loadingBar.dismiss();
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });


        //register_login_text click listener
        register_login_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
                finish();
            }
        });
    }

    private void recover_success_toast() {
        Toasty.success(this,"Password Reset Email sent Successfully!", Toasty.LENGTH_LONG, true).show();
    }

    public void showErroCustomDialog(){

        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(RecoverPassword.this).inflate(R.layout.error_dialog, viewGroup, false);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(RecoverPassword.this);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);

        //finally creating the alert dialog and displaying it
        final AlertDialog alertDialog = builder.create();

        Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
        TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
        TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

        dialog_btn.setText("OK");
        success_title.setText("Error");
        success_text.setText("Please key in your e-mail address to continue!");
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