package niwigh.com.smartchat.Model;

public class MessagesFragmentModel {

    private String user_status;

    public MessagesFragmentModel(){
        //required empty constructor
    }

    public MessagesFragmentModel(String user_status) {
        this.user_status = user_status;
    }

    public String getUser_status() {
        return user_status;
    }

    public void setUser_status(String user_status) {
        this.user_status = user_status;
    }
}