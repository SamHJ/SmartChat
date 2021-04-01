package niwigh.com.smartchat.Model;

public class GroupModel {
    String groupimage, groupname, groupdescription, datecreated, timecreated;
    boolean couldjoin;

    public GroupModel(){
        //required empty constructor
    }

    public GroupModel(String groupimage, String groupname,
                      String groupdescription, String groupimage1, String datecreated,
                      String timecreated,boolean couldjoin) {
        this.groupimage = groupimage;
        this.groupname = groupname;
        this.groupdescription = groupdescription;
        this.groupimage = groupimage1;
        this.datecreated = datecreated;
        this.timecreated = timecreated;
        this.couldjoin = couldjoin;
    }

    public boolean isCouldjoin() {
        return couldjoin;
    }

    public void setCouldjoin(boolean couldjoin) {
        this.couldjoin = couldjoin;
    }

    public String getGroupimage() {
        return groupimage;
    }

    public void setGroupimage(String groupimage) {
        this.groupimage = groupimage;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public String getGroupdescription() {
        return groupdescription;
    }

    public void setGroupdescription(String groupdescription) {
        this.groupdescription = groupdescription;
    }

    public String getDatecreated() {
        return datecreated;
    }

    public void setDatecreated(String datecreated) {
        this.datecreated = datecreated;
    }

    public String getTimecreated() {
        return timecreated;
    }

    public void setTimecreated(String timecreated) {
        this.timecreated = timecreated;
    }
}