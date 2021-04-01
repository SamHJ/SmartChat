package niwigh.com.smartchat.Model;

public class MessagesModel {

    public String message, time, date, type, from, to, messageID, name, extension,
            replymessage,replymessagetype,replyfrom,replymessagepositioninrecyclerview,
            replymessagetypeformat,replyfromid;
    private boolean isseen;

    public MessagesModel(){
        //required empty constructor
    }

    public MessagesModel(String message, String time, String date, String type,
                         String from, String to, String messageID, String name,
                         String extension, String replymessage, String replymessagetype,
                         String replyfrom, String replymessagepositioninrecyclerview,
                         String replymessagetypeformat, boolean isseen,String replyfromid) {
        this.message = message;
        this.time = time;
        this.date = date;
        this.type = type;
        this.from = from;
        this.to = to;
        this.messageID = messageID;
        this.name = name;
        this.extension = extension;
        this.replymessage = replymessage;
        this.replymessagetype = replymessagetype;
        this.replyfrom = replyfrom;
        this.replymessagepositioninrecyclerview = replymessagepositioninrecyclerview;
        this.replymessagetypeformat = replymessagetypeformat;
        this.isseen = isseen;
        this.replyfromid = replyfromid;
    }

    public String getReplyfromid() {
        return replyfromid;
    }

    public void setReplyfromid(String replyfromid) {
        this.replyfromid = replyfromid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getReplymessage() {
        return replymessage;
    }

    public void setReplymessage(String replymessage) {
        this.replymessage = replymessage;
    }

    public String getReplymessagetype() {
        return replymessagetype;
    }

    public void setReplymessagetype(String replymessagetype) {
        this.replymessagetype = replymessagetype;
    }

    public String getReplyfrom() {
        return replyfrom;
    }

    public void setReplyfrom(String replyfrom) {
        this.replyfrom = replyfrom;
    }

    public String getReplymessagepositioninrecyclerview() {
        return replymessagepositioninrecyclerview;
    }

    public void setReplymessagepositioninrecyclerview(String replymessagepositioninrecyclerview) {
        this.replymessagepositioninrecyclerview = replymessagepositioninrecyclerview;
    }

    public String getReplymessagetypeformat() {
        return replymessagetypeformat;
    }

    public void setReplymessagetypeformat(String replymessagetypeformat) {
        this.replymessagetypeformat = replymessagetypeformat;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }
}