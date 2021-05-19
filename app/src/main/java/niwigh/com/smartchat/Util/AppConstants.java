package niwigh.com.smartchat.Util;

import java.util.regex.Pattern;


public interface AppConstants {

    String ADMIN_ID = "uSWUholEyAg0Yu05fPlxrhRYf2I3";

    String ENGLISH = "en";


    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";


    /**
     * Validation regular expression
     */
    Pattern EMAIL_ADDRESS_PATTERN = Pattern
            .compile("^([a-zA-Z0-9._-]+)@{1}(([a-zA-Z0-9_-]{1,67})|([a-zA-Z0-9-]+\\.[a-zA-Z0-9-]{1,67}))\\.(([a-zA-Z0-9]{2,6})(\\.[a-zA-Z0-9]{2,6})?)$");
    Pattern MOBILE_NUMBER_PATTERN = Pattern.compile("^[0-9]{8,14}$");
    Pattern USER_NAME_PATTERN = Pattern.compile("^([a-zA-Z0-9._-]){6,20}$");
    Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,}$");

    enum Months {
        JAN, FEB, MAR, APR, MAY, JUN, JUL, AUG, SEP, OCT, NOV, DEC
    }

}
