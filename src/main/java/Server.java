import CustomExceptions.NotFoundFreeRoom;
import CustomExceptions.NotSetUsername;
import GameComponents.Game;
import GameComponents.Player;
import Utils.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Server implements AutoCloseable {

    private static final int SERVER_PORT = 4444;
    private static final int BUFFER_SIZE = 1024;

    private Map<String, Game> rooms;
    private Map<SocketChannel, Player> onlineUsers;

    private Selector selector;
    private ByteBuffer commandBuffer;
    private ServerSocketChannel serverSocketChannel;
    private boolean runServer = true;

    public Server(int port) throws IOException {
        rooms = new HashMap<>();
        onlineUsers = new HashMap<>();

        commandBuffer = ByteBuffer.allocate(BUFFER_SIZE);

        selector = Selector.open();

        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
    }

    private void start() throws IOException {
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println(String.format("Server run on %s", InetAddress.getLocalHost().getHostAddress()));

        while (runServer) {
            int readyChannels = selector.select(); //block operation
            if (readyChannels <= 0) {
                continue;
            }

            refreshRooms();

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                if (key.isReadable()) {
                    this.read(key);
                } else if (key.isAcceptable()) {
                    this.accept(key);
                }
                keyIterator.remove();
            }

        }
    }

    public void stopServer() {
        runServer = false;
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel newSocketChannel = ssc.accept();
        System.out.println("New socket connect " + newSocketChannel);
        onlineUsers.put(newSocketChannel, new Player(newSocketChannel));
        newSocketChannel.configureBlocking(false);
        newSocketChannel.register(selector, SelectionKey.OP_READ);
    }


    private void read(SelectionKey key) {
        SocketChannel currentSocketChannel = (SocketChannel) key.channel();
        try {
            commandBuffer.clear();
            int r = currentSocketChannel.read(commandBuffer); // size ?
            if (r == -1) {
                throw new RuntimeException("Channel is broken!");
            }
            commandBuffer.flip();

            String message = Charset.forName("UTF-8").decode(commandBuffer).toString();

            //check whether player is playing in this moment
            if (onlineUsers.get(currentSocketChannel).getCurrentGame() != null) {
                onlineUsers.get(currentSocketChannel).sendAnswer(message);
            } else {
                Message result = executeCommand(message, currentSocketChannel);

                commandBuffer.clear();
                commandBuffer.put((result.getMessage() + System.lineSeparator()).getBytes());
                commandBuffer.flip();
                currentSocketChannel.write(commandBuffer);
            }
        } catch (IOException e) {
            System.err.println("Channel is broken! " + currentSocketChannel);
            clearUserData(currentSocketChannel);
        } catch (RuntimeException e) {
            System.err.println("Socket disconnected! " + currentSocketChannel);
            e.printStackTrace();
            clearUserData(currentSocketChannel);
            key.cancel();
        }
    }


    private void clearUserData(SocketChannel currentSocketChannel) {
        try {
            onlineUsers.remove(currentSocketChannel);
            this.rooms.entrySet()
                    .removeIf(stringGameEntry ->
                            stringGameEntry.getValue().getFirstPlayer().getSc().equals(currentSocketChannel) &&
                                    stringGameEntry.getValue().isFree());
            currentSocketChannel.close();
        } catch (IOException E) {
            System.err.println("Unsuccessful close socket!");
        }
    }

    private void refreshRooms() {
        this.rooms.entrySet().removeIf(stringGameEntry -> stringGameEntry.getValue().isFinished());
    }


    private Message executeCommand(String receiveMsg, SocketChannel caller) {
        System.out.print("->>>" + receiveMsg + ".");
        String[] cmdParts = receiveMsg.split("\\s+");
        Message answer = null;
        System.out.println(cmdParts);
        System.out.println(cmdParts.length);
        if (cmdParts.length > 0) {
            String command = cmdParts[0].trim();
            try {
                if (command.equalsIgnoreCase("nickname") && cmdParts.length == 2) {
                    answer = ServerExecutorCommand.setNickname(cmdParts, onlineUsers, caller);
                } else if (onlineUsers.get(caller).getUsername() != null) {
                    if (command.equalsIgnoreCase("create-game") && cmdParts.length == 2) {
                        answer = ServerExecutorCommand.createGame(cmdParts, onlineUsers, caller, this.rooms);
                    } else if (command.equalsIgnoreCase("join-game")) {
                        answer = ServerExecutorCommand.joinInGame(cmdParts, onlineUsers, caller, this.rooms);
                    } else if (command.equalsIgnoreCase("list-rooms") && cmdParts.length == 1) {
                        answer = ServerExecutorCommand.listCurrentGames(this.rooms);
                    } else if (command.equalsIgnoreCase("show-history") && cmdParts.length == 1) {
                        answer = ServerExecutorCommand.showHistoryGames();
                    } else {
                        answer = new Message("Unknown command", "server");
                    }
                } else {
                    throw new NotSetUsername();
                }
            } catch (NotFoundFreeRoom e) {
                answer = new Message("There are not free rooms!", "server");
                return answer;
            } catch (NotSetUsername e) {
                answer = new Message("Please set your nickname to continue!", "server");
                return answer;
            }
        }
        if (answer == null) {
            answer = new Message("Unknown command", "server");
        }
        return answer;
    }


    @Override
    public void close() throws Exception {
        serverSocketChannel.close();
        selector.close();
    }

    public static void main(String[] args) {
        try (Server es = new Server(SERVER_PORT)) {
            es.start();
        } catch (Exception e) {
            System.out.println("An error has occurred!");
        }
    }
}