package niwigh.com.smartchat.Notifications;

import niwigh.com.smartchat.Model.Data;

public class Sender {

    public Data data;
    public String to;

    public Sender(){
        //required empty constructor
    }

    public Sender(Data data, String to) {
        this.data = data;
        this.to = to;
    }
}