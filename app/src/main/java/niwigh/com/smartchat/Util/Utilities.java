package niwigh.com.smartchat.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import niwigh.com.smartchat.R;

import static android.content.Context.CONNECTIVITY_SERVICE;

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
//    public static String outPut = "MMMM dd, yyyy @ hh:mm a";
    public static String outPut1 = "MMM dd, yyyy";
    public static String outPut2 = "yyyy:MM:dd:HH:mm";

    public static String inputForAcc = "dd MM yyyy hh:mm";
    public static String outPutForAcc = "MMM dd, yyyy";
    public static String outPutForedit = "dd-MM-yyyy";
    public static final String KEY_request_id = "request_id";

    /* A private Constructor prevents any other
     * class from instantiating.
     */
    public Utilities() {
    }

    public static String formatDateShow(String inputDate) {
        String date = "";
        SimpleDateFormat format = new SimpleDateFormat(input1);
        try {
            Date newDate = format.parse(inputDate);
            format = new SimpleDateFormat(outPut1);
            date = format.format(newDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String formateDateShow5(String inputDate) {
        String date = "";
        SimpleDateFormat format = new SimpleDateFormat(input1);
        try {
            Date newDate = format.parse(inputDate);
            format = new SimpleDateFormat(outPut1);
            date = format.format(newDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String formateDateShow6(String inputDate) {
        String date = "";
        SimpleDateFormat format = new SimpleDateFormat(input2);
        try {
            Date newDate = format.parse(inputDate);
            format = new SimpleDateFormat(outPut2);
            date = format.format(newDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    /* Static 'instance' method */
    public static Utilities getInstance(Context mContext) {
        context = mContext;
        if (singleton == null)
            singleton = new Utilities();
        return singleton;
    }

    /**
     * Method for checking network availability
     */
    public boolean isNetworkAvailable(String s) {
        try {
            cm = (ConnectivityManager) context
                    .getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            // if no network is available networkInfo will be null
            // otherwise check if we are connected
            if (networkInfo != null && networkInfo.isConnected())
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Check if the connection is fast
     *
     * @param type
     * @param subType
     * @return
     */

    public boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            Log.i(getClass().getName(), "Wifi State");
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    Log.i(getClass().getName(), "50 - 100 kbps");
                    return true; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    Log.i(getClass().getName(), "14 - 64 kbps");
                    return true; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    Log.i(getClass().getName(), "50 - 100 kbps");
                    return true; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    Log.i(getClass().getName(), "400 - 1000 kbps");
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    Log.i(getClass().getName(), "600 - 1400 kbps");
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    Log.i(getClass().getName(), " 100 kbps");
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    Log.i(getClass().getName(), "2 - 14 Mbps");
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    Log.i(getClass().getName(), "700 - 1700 kbps");
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    Log.i(getClass().getName(), "1 - 23 Mbps");
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    Log.i(getClass().getName(), "400 - 7000 kbps");
                    return true; // ~ 400-7000 kbps
            /*
             * Above API level 7, make sure to set android:targetSdkVersion
             * to appropriate level to use these
             */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    Log.i(getClass().getName(), "1 - 2 Mbps");
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    Log.i(getClass().getName(), "5 Mbps");
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    Log.i(getClass().getName(), "10 - 20 Mbps");
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    Log.i(getClass().getName(), "25 kbps");
                    return false; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    Log.i(getClass().getName(), "10+ Mbps");
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        } else {
            return false;
        }
    }


    /**
     * Method for checking network availability
     */
    public boolean isNetworkAvailable() {
        try {
            cm = (ConnectivityManager) context
                    .getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();

            // if no network is available networkInfo will be null
            // otherwise check if we are connected
            if (networkInfo != null && networkInfo.isConnected()) {
                // dialogOK(context,"","Your connection is too low", "OK", false);
                return isConnectionFast(networkInfo.getType(), networkInfo.getSubtype());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void dialogError(final Context context, String message) {
        // https://www.google.com/design/spec/components/dialogs.html#dialogs-specs
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

    public void dialogOK(final Context context, String title, String message,
                         String btnText, final boolean isFinish) {
        // https://www.google.com/design/spec/components/dialogs.html#dialogs-specs
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("");
        alertDialogBuilder.setMessage(Html.fromHtml(message));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(btnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isFinish)
                    ((Activity) context).finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
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


//            UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    if(dataSnapshot.exists()){
//                        if( dataSnapshot.hasChild("fullname") && dataSnapshot.hasChild("username") &&
//                                dataSnapshot.hasChild("location") ){
//
//                            Map<String,Object> currentStateMap = new HashMap<String, Object>();
//                            currentStateMap.put("time", saveCurrentTime);
//                            currentStateMap.put("date", saveCurrentDate);
//                            currentStateMap.put("type", state);
//
//                            UserRef.child(currentUserID).child("userState").updateChildren(currentStateMap);
//                            UserRef.child(currentUserID).child("status").setValue(state);
//                        }
//                    }
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                }
//            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

