package utils.elements;

import java.sql.Timestamp;

public class Message {
    private int id;
    private String content;
    private int senderId; // по id
    private Timestamp time;

    public Message(int id, String content, int senderId, Timestamp time) {
        this.id = id;
        this.content = content;
        this.senderId = senderId;
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public int getSenderId() {
        return senderId;
    }
}
