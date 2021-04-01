package niwigh.com.smartchat.Model;

public class FeedsModel {

    public String image_url, url,type,title;

    public FeedsModel(){
        //required empty constructor
    }

    public FeedsModel(String image_url, String url, String type,String title) {
        this.image_url = image_url;
        this.url = url;
        this.type = type;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}