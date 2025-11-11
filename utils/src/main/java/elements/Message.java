package elements;

//import elements.User;
import java.sql.Timestamp;
import java.util.Date;

public abstract class Message {
    private int id;
    private String content;
    private int sender; // по id
    private Timestamp time;

}
