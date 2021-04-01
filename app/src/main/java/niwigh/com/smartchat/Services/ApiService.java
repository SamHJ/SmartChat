package niwigh.com.smartchat.Services;


import niwigh.com.smartchat.Notifications.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import okhttp3.ResponseBody;

public interface ApiService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA97gbo-k:APA91bH6oa7F-rTsRQR8tLkx4KqA1u-iCCFBT9RLbMhNfc3BGlz8jAvL57avUmc45SC-LyODYpGgCL760JN45eWOIwEH_yNMv_-DidUtQ9Rq_fUAtEEUa0cRQpWmYckTk7JlVbfKOnjn"
    })

    @POST("fcm/send")
    Call<ResponseBody> sendNotification(@Body Sender body);
}