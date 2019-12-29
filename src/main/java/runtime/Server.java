package runtime;

import entities.Game;
import entities.Player;
import services.ServerExecutorCommand;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Server implements AutoCloseable {

    static final int SERVER_PORT = 4444;
    private static final int BUFFER_SIZE = 1024;
    private final Map<String, Game> rooms;
    private final Map<SocketChannel, Player> onlineUsers;
    private final Selector selector;
    private final ByteBuffer commandBuffer;
    private final ServerSocketChannel serverSocketChannel;
    private boolean runServer;

    public Server(final int port) throws IOException {
        rooms = new HashMap<>();
        onlineUsers = new HashMap<>();

        commandBuffer = ByteBuffer.allocate(Server.BUFFER_SIZE);

        selector = Selector.open();

        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
    }

    public static void main(final String[] args) {
        try (final Server es = new Server(Server.SERVER_PORT)) {
            es.start();
        } catch (final Exception e) {
            System.err.println("An error has occurred!");
        }
    }

    public Map<String, Game> getRooms() {
        return rooms;
    }

    public Map<SocketChannel, Player> getOnlineUsers() {
        return onlineUsers;
    }

    void start() throws IOException {
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println(String.format("Server run on %s", InetAddress.getLocalHost().getHostAddress()));
        runServer = true;
        while (runServer) {
            final int readyChannels = selector.select(); //block operation
            if (readyChannels <= 0) {
                continue;
            }

            refreshRooms();

            final Set<SelectionKey> selectedKeys = selector.selectedKeys();
            final Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            while (keyIterator.hasNext()) {
                final SelectionKey key = keyIterator.next();
                if (key.isReadable()) {
                    read(key);
                }
                else if (key.isAcceptable()) {
                    accept(key);
                }
                keyIterator.remove();
            }

        }
    }

    void stopServer() {
        runServer = false;
    }

    public boolean isRunningServer() {
        return runServer;
    }

    private void accept(final SelectionKey key) throws IOException {
        final ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        final SocketChannel newSocketChannel = ssc.accept();
        System.out.println("New socket connect " + newSocketChannel);
        onlineUsers.put(newSocketChannel, new Player(newSocketChannel));
        newSocketChannel.configureBlocking(false);
        newSocketChannel.register(selector, SelectionKey.OP_READ);
    }

    private void read(final SelectionKey key) {
        final SocketChannel currentSocketChannel = (SocketChannel) key.channel();
        try {
            commandBuffer.clear();
            final int r = currentSocketChannel.read(commandBuffer); // size ?
            if (r == -1) {
                throw new RuntimeException("Channel is broken!");
            }
            commandBuffer.flip();

            final String message = java.nio.charset.StandardCharsets.UTF_8.decode(commandBuffer).toString();

            //check whether the player is playing in this moment
            if (onlineUsers.get(currentSocketChannel).getCurrentGame() != null) {
                onlineUsers.get(currentSocketChannel).sendAnswer(message);
            }
            else {
                final String result = ServerExecutorCommand.executeCommand(this, message, currentSocketChannel);
                //System.out.println("data to send:" + result);
                commandBuffer.clear();
                commandBuffer.put((result + System.lineSeparator()).getBytes());
                commandBuffer.flip();
                currentSocketChannel.write(commandBuffer);
            }
        } catch (final IOException e) {
            System.err.println("Channel is broken! " + currentSocketChannel);
            clearUserData(currentSocketChannel);
        } catch (final RuntimeException e) {
            System.err.println("Socket disconnected! " + currentSocketChannel);
            //e.printStackTrace();
            clearUserData(currentSocketChannel);
            key.cancel();
        }
    }

    private void clearUserData(final SocketChannel currentSocketChannel) {
        try {
            onlineUsers.remove(currentSocketChannel);
            rooms.entrySet()
                    .removeIf(stringGameEntry ->
                                      stringGameEntry.getValue().getFirstPlayer().getSocketChannel()
                                              .equals(currentSocketChannel) &&
                                              stringGameEntry.getValue().isFree());
            currentSocketChannel.close();
        } catch (final IOException E) {
            System.err.println("Unsuccessful close socket!");
        }
    }

    private void refreshRooms() {
        rooms.entrySet().removeIf(stringGameEntry -> stringGameEntry.getValue().isFinished());
    }

    @Override
    public void close() throws Exception {
        serverSocketChannel.close();
        selector.close();
    }
}