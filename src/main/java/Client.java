import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class Client implements AutoCloseable {
    private static final int SERVER_PORT = 4444;
    private static final String HOSTNAME = "127.0.0.1";
    private static final int BUFFER_SIZE = 1024;
    private static final int TIME_TO_SHUTDOWN = 1000;
    private final ByteBuffer byteBuffer;
    private final Selector selector;
    private final OutputStream outputStream;
    private final InputStream inputStream;
    private final SocketChannel socketChannel;

    public Client(final String hostname, final int port, final OutputStream out, final InputStream in) throws IOException {

        outputStream = out;
        inputStream = in;

        byteBuffer = ByteBuffer.allocate(Client.BUFFER_SIZE);

        selector = Selector.open();

        socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(hostname, port));
        System.out.println("Successful connection!");
    }

    public static void main(final String[] args) {
        try (final Client client = new Client(Client.HOSTNAME, Client.SERVER_PORT, System.out, System.in)) {
            client.start();
        } catch (final Exception e) {
            System.err.println("Unsuccessful connection!");
            //e.printStackTrace();
        }
    }

    void start() throws IOException {

        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);

        //start selector works in another thread
        final Thread t = new Thread(this::run);
        t.start();

        try (final Scanner scanner = new Scanner(inputStream)) {

            while (true) {

                if (scanner.hasNextLine()) {
                    final String msg = scanner.nextLine();
                    if (!t.isAlive()) {
                        break;
                    }

                    if (msg.equals("q")) {
                        Thread.sleep(Client.TIME_TO_SHUTDOWN);
                        t.interrupt();
                        break;
                    }
                    for (final String s : msg.split(System.lineSeparator())) {
                        //     System.err.println("I send : " + s);
                        send(s);
                    }
                }
            }
        } catch (final IOException e) {
            System.err.println("Unsuccessful write command!");
            t.interrupt();
        } catch (final InterruptedException e) {
            System.err.println("Occurred shutdown error.");
        }
    }

    private void send(final String message) throws IOException {
        byteBuffer.clear();
        byteBuffer.put((message + System.lineSeparator()).getBytes());
        byteBuffer.flip();
        while (byteBuffer.hasRemaining()) {
            socketChannel.write(byteBuffer);
        }
    }

    private void read(final SelectionKey key) throws IOException, RuntimeException {
        final SocketChannel currentSocketChannel = (SocketChannel) key.channel();
        while (true) {
            byteBuffer.clear();
            final int r = currentSocketChannel.read(byteBuffer);
            if (r == 0) {
                break;
            }
            byteBuffer.flip();
            final String message = java.nio.charset.StandardCharsets.UTF_8.decode(byteBuffer).toString();
            outputStream.write(message.getBytes());
        }
    }

    @Override
    public void close() throws Exception {
        socketChannel.close();
        selector.close();
    }

    private void run() {
        while (true) {
            if (Thread.interrupted()) {
                break;
            }
            int readyChannels = 0;
            try {
                readyChannels = selector.select();
            } catch (final IOException e) {
                System.err.println("Error in communication!");
            }
            if (readyChannels <= 0) {
                continue;
            }

            final Set<SelectionKey> selectedKeys = selector.selectedKeys();
            final Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            try {
                while (keyIterator.hasNext()) {
                    final SelectionKey key = keyIterator.next();
                    if (key.isReadable()) {
                        read(key);

                    }
                    keyIterator.remove();
                }
            } catch (final IOException e) {
                System.err.println("An error occurred on server side!");
//                e.printStackTrace();
            } catch (final RuntimeException e) {
                System.err.println("Channel was broken!");
                // e.printStackTrace();
            }
        }

    }

}
