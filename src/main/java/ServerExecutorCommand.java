import CustomExceptions.NotFoundFreeRoom;
import GameComponents.Game;
import GameComponents.Player;
import Services.IOFile;
import Utils.Message;

import java.nio.channels.SocketChannel;
import java.util.Map;

public class ServerExecutorCommand {
    public static Message setNickname(String[] cmdParts,
                                      Map<SocketChannel, Player> onlineUsers,
                                      SocketChannel caller) {
        Message answer = null;
        if (cmdParts[1].length() < Player.LENGTH_NAME) {
            if (onlineUsers.values().stream()
                    .anyMatch(player -> player.getUsername() != null && player.getUsername().equals(cmdParts[1]))) {
                answer = new Message("This username is already used!", "server");
            } else {
                onlineUsers.get(caller).setUsername(cmdParts[1]);
                answer = new Message("You set nickname successful!", "server");
            }
        } else {
            answer = new Message("Your nickname must be smaller than " + Player.LENGTH_NAME + " characters!",
                    "server");
        }
        return answer;
    }

    public static Message createGame(String[] cmdParts,
                                     Map<SocketChannel, Player> onlineUsers,
                                     SocketChannel caller,
                                     Map<String, Game> games) {
        Message answer = null;
        if (cmdParts[1].length() < Game.LENGTH_NAME) {
            games.put(cmdParts[1], new Game(cmdParts[1], onlineUsers.get(caller)));
            answer = new Message("Successful create room!", "server");
        } else {
            answer = new Message("Room name must be smaller than " + Game.LENGTH_NAME + " characters!",
                    "server");
        }
        return answer;
    }

    public static Message joinInGame(String[] cmdParts,
                                     Map<SocketChannel, Player> onlineUsers,
                                     SocketChannel caller,
                                     Map<String, Game> games) throws NotFoundFreeRoom {
        Message answer = null;
        if (cmdParts.length == 2) {
            if (games.containsKey(cmdParts[1])) {
                if (!games.get(cmdParts[1]).isFree()) {
                    answer = new Message("This room is full!", "server");
                } else {
                    games.get(cmdParts[1]).setSecondPlayer(onlineUsers.get(caller));
                    answer = new Message("Successful join in room!", "server", cmdParts[1]);
                }
            } else {
                answer = new Message("There is not room with that name?!", "server");
            }
        } else if (cmdParts.length == 1) {
            Game pendingGame = games.values().stream().filter(Game::isFree).findFirst().orElseThrow(NotFoundFreeRoom::new);
            games.get(pendingGame.getNameRoom()).setSecondPlayer(onlineUsers.get(caller));
            games.get(pendingGame.getNameRoom()).startGame();
            answer = new Message("Successful join in room!", "server", pendingGame.getNameRoom());
        }
        return answer;

    }

    public static Message listCurrentGames(Map<String, Game> games) {
        StringBuilder sb = new StringBuilder("");
        sb.append("| NAME | CREATOR | ISFREE |")
                .append("\n")
                .append("|------+---------+--------|")
                .append("\n");
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
        sb.append("|------+---------+--------|").append("\n");
        return new Message(sb.toString(), "server");
    }

    public static Message showHistoryGames() {
        return new Message(IOFile.getAllPlayedGames(), "server");
    }

}
