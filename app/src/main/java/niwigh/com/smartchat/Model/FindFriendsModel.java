package niwigh.com.smartchat.Model;

public class FindFriendsModel {

    public String profileimage, fullname, username, location,status;

    public  FindFriendsModel(){
        //required empty constructor
    }

    public FindFriendsModel(String profileimage, String fullname, String username, String location,
                            String status)
    {
        this.profileimage = profileimage;
        this.fullname = fullname;
        this.username = username;
        this.location = location;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserLocation() {
        return location;
    }

    public void setUserLocation(String school) {
        this.location = school;
    }
}