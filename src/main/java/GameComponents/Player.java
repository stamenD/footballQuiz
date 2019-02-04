package GameComponents;

import java.nio.channels.SocketChannel;

public class Player {
    public static final int LENGTH_NAME = 8;

    private String username;
    private SocketChannel sc;
    private Game currentGame;

    public Player(SocketChannel sc) {
        this.sc = sc;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public String getUsernameFormat() {
        if (username != null && username.length() < LENGTH_NAME)
            return " " + username + " ".repeat(LENGTH_NAME - username.length());
        else {
            return username;
        }
    }

    public Game getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(Game currentGame) {
        this.currentGame = currentGame;
    }

    public void sendAnswer(String answer) {
        this.currentGame.getAnswer(this, answer);
    }

    public SocketChannel getSocketChannel() {
        return sc;
    }
}
