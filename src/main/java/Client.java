import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class Client implements AutoCloseable {
    private static final int SERVER_PORT = 4444;
    private static final String HOSTNAME = "127.0.0.1";
    private static final int BUFFER_SIZE = 1024;
    private static final int TIME_TO_SHUTDOWN = 1000;
    private SocketChannel socketChannel;
    private ByteBuffer byteBuffer;
    private Selector selector;
    private OutputStream outputStream;
    private InputStream inputStream;

    public Client(String hostname, int port, OutputStream out, InputStream in) throws IOException {

        this.outputStream = out;
        this.inputStream = in;

        byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);

        selector = Selector.open();

        socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress(hostname, port));
        System.out.println("Successful connection!");
    }

    public void start() throws IOException {

        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);

        //start selector works in another thread
        Thread t = new Thread(this::run);
        t.start();

        try (Scanner scanner = new Scanner(this.inputStream)) {

            while (true) {

                if (scanner.hasNextLine()) {
                    String msg = scanner.nextLine();
                    if (!t.isAlive()) {
                        break;
                    }

                    if (msg.equals("q")) {
                        Thread.sleep(TIME_TO_SHUTDOWN);
                        t.interrupt();
                        break;
                    }
                    for (String s : msg.split(System.lineSeparator())) {
                        //     System.err.println("I send : " + s);
                        send(s);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Unsuccessful write command!");
            t.interrupt();
        } catch (InterruptedException e) {
            System.err.println("Occurred shutdown error.");
        }
    }

    private void send(String message) throws IOException {
        byteBuffer.clear();
        byteBuffer.put((message + System.lineSeparator()).getBytes());
        byteBuffer.flip();
        while (byteBuffer.hasRemaining()) {
            socketChannel.write(byteBuffer);
        }
    }

    private void read(SelectionKey key) throws IOException, RuntimeException {
        SocketChannel currentSocketChannel = (SocketChannel) key.channel();
        while (true) {
            byteBuffer.clear();
            int r = currentSocketChannel.read(byteBuffer);
            if (r == 0) {
                break;
            }
            byteBuffer.flip();
            String message = Charset.forName("UTF-8").decode(byteBuffer).toString();
            this.outputStream.write(message.getBytes());
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
            } catch (IOException e) {
                System.err.println("Error in communication!");
            }
            if (readyChannels <= 0) {
                continue;
            }

            Set<SelectionKey> selectedKeys = this.selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
            try {
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isReadable()) {
                        this.read(key);

                    }
                    keyIterator.remove();
                }
            } catch (IOException e) {
                System.err.println("An error occurred on server side!");
//                e.printStackTrace();
            } catch (RuntimeException e) {
                System.err.println("Channel was broken!");
                // e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        try (Client client = new Client(HOSTNAME, SERVER_PORT, System.out, System.in)) {
            client.start();
        } catch (Exception e) {
            System.err.println("Unsuccessful connection!");
            //e.printStackTrace();
        }
    }

}
