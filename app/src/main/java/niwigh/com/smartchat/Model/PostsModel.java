package niwigh.com.smartchat.Model;

public class PostsModel {

    String uid,date,time,title,description,postimage,profileimage,fullname,type,
            posttitletolowercase,timestamp,postfilestoragename,counter,postvideo,postKey;

    public PostsModel() {
    }

    public PostsModel(String uid, String date, String time, String title,
                      String description, String postimage, String profileimage, String fullname, String type,
                      String posttitletolowercase, String timestamp,
                      String postfilestoragename, String counter, String postvideo,String postKey) {
        this.uid = uid;
        this.date = date;
        this.time = time;
        this.title = title;
        this.description = description;
        this.postimage = postimage;
        this.profileimage = profileimage;
        this.fullname = fullname;
        this.type = type;
        this.posttitletolowercase = posttitletolowercase;
        this.timestamp = timestamp;
        this.postfilestoragename = postfilestoragename;
        this.counter = counter;
        this.postvideo = postvideo;
        this.postKey = postKey;
    }

    public String getPostKey() {
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPosttitletolowercase() {
        return posttitletolowercase;
    }

    public void setPosttitletolowercase(String posttitletolowercase) {
        this.posttitletolowercase = posttitletolowercase;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPostfilestoragename() {
        return postfilestoragename;
    }

    public void setPostfilestoragename(String postfilestoragename) {
        this.postfilestoragename = postfilestoragename;
    }

    public String getCounter() {
        return counter;
    }

    public void setCounter(String counter) {
        this.counter = counter;
    }

    public String getPostvideo() {
        return postvideo;
    }

    public void setPostvideo(String postvideo) {
        this.postvideo = postvideo;
    }
}
