package niwigh.com.smartchat.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import niwigh.com.smartchat.R;


/**
 * Created by Emenike Samuel on 4/7/18.
 */
public class Utilities implements AppConstants {
    private ConnectivityManager cm;
    static Context context;
    private static Utilities singleton = null;
    public static String input = "yyyy-MM-dd HH:mm:ss";
    public static String input1 = "yyyy-MM-dd";
    public static String input2 = "HH:mm";
    public static String outPut = "yyyy:MM:dd";
    public static String outPut1 = "MMM dd, yyyy";
    public static String outPut2 = "yyyy:MM:dd:HH:mm";

    /* A private Constructor prevents any other
     * class from instantiating.
     */
    public Utilities() {
    }

    /* Static 'instance' method */
    public static Utilities getInstance(Context mContext) {
        context = mContext;
        if (singleton == null)
            singleton = new Utilities();
        return singleton;
    }

    public void dialogError(final Context context, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(Html.fromHtml(message));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               dialog.dismiss();
            }
        });
        final AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onShow(DialogInterface arg0) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(R.color.colorPrimary);
            }
        });
        alertDialog.show();

    }

    public void customErrorDialog(View view,String button_text,String error_title,
                                  String message){
        //{.....................
        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = view.findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(context).inflate(R.layout.error_dialog, viewGroup, false);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);

        //finally creating the alert dialog and displaying it
        final AlertDialog alertDialog = builder.create();

        Button dialog_btn = (Button) dialogView.findViewById(R.id.buttonError);
        TextView success_text = (TextView) dialogView.findViewById(R.id.error_text);
        TextView success_title = (TextView) dialogView.findViewById(R.id.error_title);

        dialog_btn.setText(button_text);
        success_title.setText(error_title);
        success_text.setText(Html.fromHtml(message));

        // if the OK button is clicked, close the success dialog
        dialog_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    @SuppressLint("SimpleDateFormat")
    public void updateUserStatus(final String state){
        try{

            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            final DatabaseReference UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
            UserRef.keepSynced(true);
            final String currentUserID = mAuth.getCurrentUser().getUid();

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd");
            final String saveCurrentDate = currentDate.format(calForDate.getTime());

            final Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
            final String saveCurrentTime = currentTime.format(calForTime.getTime());

            Map<String,Object> currentStateMap = new HashMap<String, Object>();
            currentStateMap.put("time", saveCurrentTime);
            currentStateMap.put("date", saveCurrentDate);
            currentStateMap.put("type", state);

            UserRef.child(currentUserID).child("userState").updateChildren(currentStateMap);
            UserRef.child(currentUserID).child("status").setValue(state);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

