package niwigh.com.smartchat.Model;

public class ChatList {
    public String id;

    public ChatList(){
        //required empty constructor
    }

    public ChatList(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
