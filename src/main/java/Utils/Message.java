package Utils;

import java.io.Serializable;

public class Message implements Serializable {
    private String message;
    private String from;
    private String roomInfo;

    public static final String WELCOME_MSG = "The GAME will start soon! Get ready! :)";

    public Message(String message, String from) {
        this.message = message;
        this.from = from;
    }

    public Message(String message, String from, String roomInfo) {
        this.message = message;
        this.from = from;
        this.roomInfo = roomInfo;
    }

    public String getRoomInfo() {
        return roomInfo;
    }

    public String getMessage() {
        return message;
    }

    public String getFrom() {
        return from;
    }
}
