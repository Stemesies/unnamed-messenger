package server.elements;

import utils.Ansi;

import java.sql.Timestamp;

public class Message {
    private final int id;
    private final String content;
    private final String senderName; // по id
    private final int senderId;
    private final Timestamp time;

    public Message(int id, String content, String senderName, int senderId, Timestamp time) {
        this.id = id;
        this.content = content;
        this.senderName = senderName;
        this.senderId = senderId;
        this.time = time;
    }

    public String getFormatted() {
        return "[" + senderName + "] " + content;
    }

    public String getFormattedSelf() {
        var offset = 100 - getFormatted().length();
        return Ansi.Colors.YELLOW.apply(
            " ".repeat(Math.max(offset, 0)) + getFormatted()
        );
    }

    public String getContent() {
        return content;
    }

    public String getSenderName() {
        return senderName;
    }

    public int getSenderId() {
        return senderId;
    }
}
