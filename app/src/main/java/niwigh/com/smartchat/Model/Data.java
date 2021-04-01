package niwigh.com.smartchat.Model;

public class Data {
    private String sender_id,message,title, visit_user_id,userName,userFullName;


    public Data(){
        //required empty constructor
    }


    public Data(String sender_id, String message, String title, String visit_user_id, String userName, String userFullName) {
        this.sender_id = sender_id;
        this.message = message;
        this.title = title;
        this.visit_user_id = visit_user_id;
        this.userName = userName;
        this.userFullName = userFullName;
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVisit_user_id() {
        return visit_user_id;
    }

    public void setVisit_user_id(String visit_user_id) {
        this.visit_user_id = visit_user_id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }
}