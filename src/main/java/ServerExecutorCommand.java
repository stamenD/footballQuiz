import CustomExceptions.NotFoundFreeRoom;
import GameComponents.Game;
import GameComponents.Player;
import Services.IOFile;

import java.nio.channels.SocketChannel;
import java.util.Map;

public class ServerExecutorCommand {
    public static String setNickname(String[] cmdParts,
                                     Map<SocketChannel, Player> onlineUsers,
                                     SocketChannel caller) {
        String answer = null;
        if (cmdParts[1].length() < Player.LENGTH_NAME) {
            if (onlineUsers.values().stream()
                    .anyMatch(player -> player.getUsername() != null && player.getUsername().equals(cmdParts[1]))) {
                answer = "This username is already used!";
            } else {
                onlineUsers.get(caller).setUsername(cmdParts[1]);
                answer = "You set nickname successful!";
            }
        } else {
            answer = "Your nickname must be smaller than " + Player.LENGTH_NAME + " characters!";
        }
        return answer;
    }

    public static String createGame(String[] cmdParts,
                                    Map<SocketChannel, Player> onlineUsers,
                                    SocketChannel caller,
                                    Map<String, Game> games) {
        String answer = null;
        if (cmdParts[1].length() < Game.LENGTH_NAME) {

            if (games.values().stream()
                    .anyMatch(game -> game.getNameRoom().equals(cmdParts[1]))) {
                answer = "This room is already created!";
            } else {
                games.put(cmdParts[1], new Game(cmdParts[1], onlineUsers.get(caller)));
                answer = "Successful create room!";
            }

        } else {
            answer = "Room name must be smaller than " + Game.LENGTH_NAME + " characters!";
        }
        return answer;
    }

    public static String joinInGame(String[] cmdParts,
                                    Map<SocketChannel, Player> onlineUsers,
                                    SocketChannel caller,
                                    Map<String, Game> games) throws NotFoundFreeRoom {
        String answer = null;
        if (cmdParts.length == 2) {
            if (games.containsKey(cmdParts[1])) {
                if (!games.get(cmdParts[1]).isFree()) {
                    answer = "This room is full!";
                } else {
                    games.get(cmdParts[1]).setSecondPlayer(onlineUsers.get(caller));
                    answer = "Successful join in room!";
                }
            } else {
                answer = "There is not room with that name!";
            }
        } else if (cmdParts.length == 1) {
            Game pendingGame = games.values().stream()
                    .filter(Game::isFree)
                    .findFirst()
                    .orElseThrow(NotFoundFreeRoom::new);
            games.get(pendingGame.getNameRoom()).setSecondPlayer(onlineUsers.get(caller));
            games.get(pendingGame.getNameRoom()).startGame();
            answer = "Successful join in room!";
        }
        return answer;

    }

    public static String listCurrentGames(Map<String, Game> games) {
        StringBuilder sb = new StringBuilder("");
        sb.append("| NAME | CREATOR | ISFREE |")
                .append("\n")
                .append("|------+---------+--------|")
                .append("\n");
        if (games != null) {
            for (Game game : games.values()) {
                sb.append("|")
                        .append(game.getNameRoomFormated())
                        .append("|")
                        .append(game.getFirstPlayer().getUsernameFormat())
                        .append("|");
                if (game.isFree()) {
                    sb.append("   yes  ");
                } else {
                    sb.append("   no   ");
                }
                sb.append("|").append("\n");
            }
        }
        sb.append("|------+---------+--------|").append("\n");
        return sb.toString();
    }

    public static String showHistoryGames() {
        return new IOFile(null, null).getAllPlayedGames();
    }

}
