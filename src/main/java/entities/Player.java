package entities;

import java.nio.channels.SocketChannel;

public class Player {
    public static final int LENGTH_NAME = 8;
    private final SocketChannel sc;
    private String username;
    private Game currentGame;

    public Player(final SocketChannel sc) {
        this.sc = sc;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getUsernameFormat() {
        if (username != null && username.length() < Player.LENGTH_NAME) {
            return " " + username + " ".repeat(Player.LENGTH_NAME - username.length());
        }
        else {
            return username;
        }
    }

    public Game getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(final Game currentGame) {
        this.currentGame = currentGame;
    }

    public void sendAnswer(final String answer) {
        currentGame.getAnswer(this, answer);
    }

    public SocketChannel getSocketChannel() {
        return sc;
    }
}
