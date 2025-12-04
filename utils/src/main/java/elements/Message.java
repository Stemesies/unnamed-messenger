package elements;

import java.sql.Timestamp;

public abstract class Message {
    private int id;
    private String content;
    private int senderId; // по id
    private Timestamp time;

}
