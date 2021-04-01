package niwigh.com.smartchat.Model;

public class User {

    private  String id, username, fullname, profileimage, status,profilestatus;

    public User(){
        //required empty constructor
    }

    public User(String id, String username, String fullname, String profileimage, String status,String profilestatus) {
        this.id = id;
        this.username = username;
        this.fullname = fullname;
        this.profileimage = profileimage;
        this.status = status;
        this.profilestatus = profilestatus;
    }

    public String getProfilestatus() {
        return profilestatus;
    }

    public void setProfilestatus(String profilestatus) {
        this.profilestatus = profilestatus;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }
}