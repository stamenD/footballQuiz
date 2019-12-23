package services;

import customexceptions.NotFoundFreeRoom;
import entities.Game;
import entities.Player;

import java.nio.channels.SocketChannel;
import java.util.Map;

public class ServerExecutorCommand {
    private static final IOFile RECORDER = new IOFile();


    public static String setNickname(
            final String[] cmdParts,
            final Map<SocketChannel, Player> onlineUsers,
            final SocketChannel caller) {
        String answer = null;
        if (onlineUsers.get(caller).getUsername() == null) {
            if (cmdParts[1].length() < Player.LENGTH_NAME) {
                if (onlineUsers.values().stream()
                        .anyMatch(player -> player.getUsername() != null && player.getUsername().equals(cmdParts[1]))) {
                    answer = "This username is already used!";
                }
                else {
                    onlineUsers.get(caller).setUsername(cmdParts[1]);
                    answer = "You set nickname successful!";
                }
            }
            else {
                answer = "Your nickname must be smaller than " + Player.LENGTH_NAME + " characters!";
            }
        }
        else {
            answer = "You already have a nickname.";
        }
        return answer;
    }

    public static String createGame(
            final String[] cmdParts,
            final Map<SocketChannel, Player> onlineUsers,
            final SocketChannel caller,
            final Map<String, Game> games) {
        String answer = null;
        if (cmdParts[1].length() < Game.LENGTH_NAME) {

            if (games.values().stream()
                    .anyMatch(game -> game.getNameRoom().equals(cmdParts[1]))) {
                answer = "This room is already created!";
            }
            else {
                games.put(cmdParts[1], new Game(cmdParts[1], onlineUsers.get(caller), services.ServerExecutorCommand.RECORDER));
                answer = "Successful create room!";
            }

        }
        else {
            answer = "Room name must be smaller than " + Game.LENGTH_NAME + " characters!";
        }
        return answer;
    }

    public static String joinInGame(
            final String[] cmdParts,
            final Map<SocketChannel, Player> onlineUsers,
            final SocketChannel caller,
            final Map<String, Game> games) throws NotFoundFreeRoom {
        String answer = null;
        if (cmdParts.length == 2) {
            if (games.containsKey(cmdParts[1])) {
                if (!games.get(cmdParts[1]).isFree()) {
                    answer = "This room is full!";
                }
                else {
                    games.get(cmdParts[1]).setSecondPlayer(onlineUsers.get(caller));
                    games.get(cmdParts[1]).startGame();
                    answer = "Successful join in room!";
                }
            }
            else {
                answer = "There is not room with that name!";
            }
        }
        else if (cmdParts.length == 1) {
            final Game pendingGame = games.values().stream()
                    .filter(Game::isFree)
                    .findFirst()
                    .orElseThrow(NotFoundFreeRoom::new);
            games.get(pendingGame.getNameRoom()).setSecondPlayer(onlineUsers.get(caller));
            games.get(pendingGame.getNameRoom()).startGame();
            answer = "Successful join in room!";
        }
        return answer;

    }

    public static String listCurrentGames(final Map<String, Game> games) {
        final StringBuilder sb = new StringBuilder();
        sb.append("| NAME | CREATOR | ISFREE |")
                .append("\n")
                .append("|------+---------+--------|")
                .append("\n");
        if (games != null) {
            for (final Game game : games.values()) {
                sb.append("|")
                        .append(game.getNameRoomFormatted())
                        .append("|")
                        .append(game.getFirstPlayer().getUsernameFormat())
                        .append("|");
                if (game.isFree()) {
                    sb.append("   yes  ");
                }
                else {
                    sb.append("   no   ");
                }
                sb.append("|").append("\n");
            }
        }
        sb.append("|------+---------+--------|").append("\n");
        return sb.toString();
    }

    public static String showHistoryGames() {
        return ServerExecutorCommand.RECORDER.getAllPlayedGames();
    }

}
