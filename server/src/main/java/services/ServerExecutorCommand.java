package services;

import customexceptions.NotFoundFreeRoom;
import customexceptions.NotSetUsername;
import customexceptions.StreamError;
import entities.Game;
import entities.Player;
import runtime.Server;

import java.nio.channels.SocketChannel;
import java.util.Map;

public class ServerExecutorCommand {
    private static final IOFileService RECORDER = new IOFileService();


    public static String executeCommand(final Server server, final String receiveMsg, final SocketChannel caller) {
        //  System.out.print("->>>" + receiveMsg + ".");
        final String[] cmdParts = receiveMsg.split("\\s+");
        String answer = null;
        if (cmdParts.length > 0) {
            final String command = cmdParts[0].trim();
            try {
                if (command.equalsIgnoreCase("nickname") && cmdParts.length == 2) {
                    answer = ServerExecutorCommand.setNickname(cmdParts, server.getOnlineUsers(), caller);
                }
                else if (server.getOnlineUsers().get(caller).getUsername() != null) {
                    if (command.equalsIgnoreCase("create-game")) {
                        answer = ServerExecutorCommand.createGame(cmdParts, server.getOnlineUsers(), caller, server.getRooms());
                    }
                    else if (command.equalsIgnoreCase("join-game")) {
                        answer = ServerExecutorCommand.joinInGame(cmdParts, server.getOnlineUsers(), caller, server.getRooms());
                    }
                    else if (command.equalsIgnoreCase("list-rooms") && cmdParts.length == 1) {
                        answer = ServerExecutorCommand.listCurrentGames(server.getRooms());
                    }
                    else if (command.equalsIgnoreCase("show-history") && cmdParts.length == 1) {
                        answer = ServerExecutorCommand.showHistoryGames();
                    }
                    else {
                        answer = "Unknown command :(.Use one of these:" + "\n" +
                                "create-game <name_room> [<time for thinking in milliseconds>]" + "\n" +
                                "join-game [<name_rom>]" + "\n" +
                                "list-rooms" + "\n" +
                                "show-history" + "\n";
                    }
                }
                else {
                    throw new customexceptions.NotSetUsername();
                }
            } catch (final StreamError e) {
                return e.getMessage();
            } catch (final NotFoundFreeRoom e) {
                answer = "There are not free rooms!";
                return answer;
            } catch (final NotSetUsername e) {
                answer = "Please set your nickname to continue! \n" +
                        "nickname <something>";
                return answer;
            }

        }
        if (answer == null) {
            answer = "Unknown command :(.Use one of these:" + "\n" +
                    "create-game <name_room> [<time for thinking in milliseconds>]" + "\n" +
                    "join-game [<name_rom>]" + "\n" +
                    "list-rooms" + "\n" +
                    "show-history" + "\n";
        }
        return answer;
    }


    static String setNickname(
            final String[] cmdParts,
            final Map<SocketChannel, Player> onlineUsers,
            final SocketChannel caller) {
        String answer;
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

    static String createGame(
            final String[] cmdParts,
            final Map<SocketChannel, Player> onlineUsers,
            final SocketChannel caller,
            final Map<String, Game> games) {
        String answer;
        if (cmdParts[1].length() < Game.LENGTH_NAME) {
            if (games.values().stream()
                    .anyMatch(game -> game.getNameRoom().equals(cmdParts[1]))) {
                answer = "This room is already created!";
            }
            else {
                final Game newGame;
                if (cmdParts.length == 3) {
                    try {
                        newGame = new Game(cmdParts[1], Integer.parseInt(cmdParts[2]), onlineUsers.get(caller), ServerExecutorCommand.RECORDER);
                    } catch (final NumberFormatException e) {
                        return "Second argument is not a number!";
                    }
                }
                else {
                    newGame = new Game(cmdParts[1], onlineUsers.get(caller), ServerExecutorCommand.RECORDER);
                }
                games.put(cmdParts[1], newGame);
                answer = "Successful create room!";
            }
        }
        else {
            return "Room name must be smaller than " + Game.LENGTH_NAME + " characters!";
        }


        return answer;
    }

    static String joinInGame(
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

    static String listCurrentGames(final Map<String, Game> games) {
        final StringBuilder sb = new StringBuilder();
        sb.append("| NAME | SPEED | CREATOR | ISFREE |")
                .append("\n")
                .append("|------+-------+---------+--------|")
                .append("\n");
        if (games != null) {
            for (final Game game : games.values()) {
                sb.append("|")
                        .append(game.getNameRoomFormatted())
                        .append("| ")
                        .append(game.getTimeForThinking())
                        .append(" |")
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
        sb.append("|------+-------+---------+--------|").append("\n");
        return sb.toString();
    }

    private static String showHistoryGames() {
        return ServerExecutorCommand.RECORDER.getAllPlayedGames();
    }

}
