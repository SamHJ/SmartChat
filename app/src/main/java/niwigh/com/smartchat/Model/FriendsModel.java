package niwigh.com.smartchat.Model;

public class FriendsModel {

    public String date, username, status;

    public FriendsModel(){
        //required empty constructor
    }

    public FriendsModel(String date, String username, String status) {
        this.date = date;
        this.username = username;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}