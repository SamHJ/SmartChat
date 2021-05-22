package niwigh.com.smartchat.Util;


import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeAgo {


    String convTime = null;
    String suffix = "Ago";

    private static Date currentDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    public  String getTimeAgo(Date date) {
        long time = date.getTime();
        if (time < 1000000000000L) {
            time *= 1000;
        }

        long now = currentDate().getTime();
        if (time > now || time <= 0) {
            return "in the future";
        }

        Date nowTime = new Date();

        long dateDiff = nowTime.getTime() - date.getTime();

        long second = TimeUnit.MILLISECONDS.toSeconds(dateDiff);
        long minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff);
        long hour   = TimeUnit.MILLISECONDS.toHours(dateDiff);
        long day  = TimeUnit.MILLISECONDS.toDays(dateDiff);

        if (second < 60) {
            if(second == 1){
                convTime = second+" Second "+suffix;
            }
            else {
                convTime = second+" Seconds "+suffix;
            }
        } else if (minute < 60) {
            if(minute == 1){
                convTime = minute+" Minute "+suffix;
            }
            else {
                convTime = minute+" Minutes "+suffix;
            }

        } else if (hour < 24) {
            if(hour == 1){
                convTime = hour+" Hour "+suffix;
            }
            else {
                convTime = hour+" Hours "+suffix;
            }
        } else if (day >= 7) {
            if (day > 30) {
                if(day<60){

                    convTime = (day / 30)+" Month "+suffix;
                }
                else {
                    convTime = (day / 30)+" Months "+suffix;
                }

            } else if (day > 360) {
                if(day <720){
                    convTime = (day / 360)+" Year "+suffix;
                }
                else {
                    convTime = (day / 360)+" Years "+suffix;
                }
            } else {
                if(day/7 == 1){
                    convTime = (day / 7) + " Week "+suffix;
                }
                else {
                    convTime = (day / 7) + " Weeks "+suffix;
                }
            }
        } else if (day < 7) {
            if(day == 1){
                convTime = day+" Day "+suffix;
            }
            else {
                convTime = day+" Days "+suffix;
            }
        }
        return convTime;
    }
}