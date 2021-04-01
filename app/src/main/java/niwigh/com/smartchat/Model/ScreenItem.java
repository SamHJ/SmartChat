package niwigh.com.smartchat.Model;

public class ScreenItem {
    String Title, Description,ShortDesc;
    int ScreenImg;

    public ScreenItem(){
        //required empty constructor
    }

    public ScreenItem(String title, String description, int screenImg,String shortDesc){
        Title = title;
        Description = description;
        ScreenImg = screenImg;
        this.ShortDesc = shortDesc;
    }


    public String getShortDesc() {
        return ShortDesc;
    }

    public void setShortDesc(String shortDesc) {
        ShortDesc = shortDesc;
    }

    public void setTitle(String title){
        Title = title;
    }

    public void setDescription(String description){
        Description = description;
    }

    public void setScreenImg(int screenImg){
        ScreenImg = screenImg;
    }

    public String getTitle(){
        return Title;
    }

    public String getDescription(){
        return Description;
    }

    public int getScreenImg(){
        return ScreenImg;
    }
}