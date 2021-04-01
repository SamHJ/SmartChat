package niwigh.com.smartchat.Model;

public class FriendRequestFragmentModel {
    private String fullname, school, profileimage;


    public FriendRequestFragmentModel(){
        //required empty constructor
    }

    public FriendRequestFragmentModel(String fullname, String school, String profileimage) {
        this.fullname = fullname;
        this.school = school;
        this.profileimage = profileimage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }
}