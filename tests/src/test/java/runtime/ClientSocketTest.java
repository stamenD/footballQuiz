package runtime;

import entities.Game;
import entities.Player;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertTrue;

public class ClientSocketTest {

    private static void runClient(final Thread clientThread) throws InterruptedException {
        class InnerSharedVariables {
            private volatile Server es;
            private volatile boolean result;
        }

        final InnerSharedVariables is = new InnerSharedVariables();

        final Thread serverThread = new Thread(() -> {
            try (final Server innerServer = new Server(Server.SERVER_PORT)) {
                is.es = innerServer;
                is.es.start();
            } catch (final Exception e) {
                System.out.println("An error has occurred");
            }
        });
        serverThread.start();

        while (is.es == null) {
        }
        clientThread.start();
        clientThread.join();
        is.es.stopServer();
        serverThread.interrupt();
    }

    private static String sendMessage(final String msg) {

        String response = "fail";
        try (final ByteArrayInputStream in = new ByteArrayInputStream((msg + System.lineSeparator() + "q").getBytes());
             final ByteArrayOutputStream out = new ByteArrayOutputStream();
             final Client client = new Client("localhost", Server.SERVER_PORT, out, in)) {
            client.start();
            // Read the response from the server
            response = out.toString();
            System.out.println("########" + response);

        } catch (final Exception e) {
            System.out.println("Connection failed  " + e.getMessage());
        }

        return response;
    }

    private static String sendMessageFromTwoChannels(final String msg1, final String msg2) {
        String response = "fail";
        try (final ByteArrayInputStream in1 = new ByteArrayInputStream((msg1 + System.lineSeparator() + "q").getBytes());
             final ByteArrayOutputStream out1 = new ByteArrayOutputStream();
             final ByteArrayInputStream in2 = new ByteArrayInputStream((msg2 + System.lineSeparator() + "q").getBytes());
             final ByteArrayOutputStream out2 = new ByteArrayOutputStream();
             final Client client1 = new Client("localhost", Server.SERVER_PORT, out1, in1);
             final Client client2 = new Client("localhost", Server.SERVER_PORT, out2, in2)) {
            client1.start();
            client2.start();
            // Read the response from the server
            response = out1.toString();
            response += out2.toString();
            System.out.println("########" + response);

        } catch (final Exception e) {
            System.out.println("Connection failed  " + e.getMessage());
        }

        return response;
    }

    @Test
    public void testToConnect() throws InterruptedException {
        final Thread clientThread = new Thread(() -> assertTrue(sendMessage("dummy massage").trim().contains("Please set your nickname to continue!")));
        runClient(clientThread);
    }

    @Test
    public void testToSetNickname() throws InterruptedException {
        final Thread clientThread = new Thread(() -> assertTrue(sendMessage("nickname az").trim().contains("You set nickname successful!")));
        runClient(clientThread);
    }

    @Test
    public void testToSetNicknameThatWasUsedBefore() throws InterruptedException {
        final Thread clientThread = new Thread(() -> {
            assertTrue(sendMessage("nickname az").trim().contains("You set nickname successful!"));
            assertTrue(sendMessage("nickname az").trim().contains("You set nickname successful!"));
        });
        runClient(clientThread);
    }

    @Test
    public void testToSetNicknameSecondTime() throws InterruptedException {
        final Thread clientThread = new Thread(() -> assertTrue(sendMessage("nickname az" +
                                                                                    System.lineSeparator() +
                                                                                    "nickname ti").trim().contains("You set nickname successful!"
                                                                                                                           + System.lineSeparator() +
                                                                                                                           "You already have a nickname.")));
        runClient(clientThread);
    }

    @Test
    public void testToSetLongNickname() throws InterruptedException {
        final Thread clientThread = new Thread(() -> {
            final StringBuilder name = new StringBuilder();
            while (name.length() < Player.LENGTH_NAME) {
                name.append("t");
            }
            name.insert(0, "nickname ");
            assertTrue(sendMessage(name.toString())
                               .trim()
                               .contains("Your nickname must be smaller than " + Player.LENGTH_NAME + " characters!"));
        });
        runClient(clientThread);

    }

    @Test
    public void testToSetUsedNickname() throws InterruptedException {
        final Thread clientThread = new Thread(() -> assertTrue(sendMessageFromTwoChannels("nickname az", "nickname az").trim().contains("This username is already used!")));
        runClient(clientThread);

    }

    @Test
    public void testToCreateGameRoom() throws InterruptedException {
        final Thread clientThread = new Thread(() -> assertTrue(sendMessage("nickname az" +
                                                                                    System.lineSeparator() +
                                                                                    "create-game 1").trim().contains("You set nickname successful!"
                                                                                                                             + System.lineSeparator() +
                                                                                                                             "Successful create room!")));
        runClient(clientThread);
    }

    @Test
    public void testToCreateGameRoomWithLongName() throws InterruptedException {
        final Thread clientThread = new Thread(() -> {
            final StringBuilder name = new StringBuilder();
            while (name.length() < Game.LENGTH_NAME) {
                name.append("t");
            }
            name.insert(0, "create-game ");
            assertTrue(sendMessage("nickname az" +
                                           System.lineSeparator() +
                                           name.toString()).trim().contains(
                    "Room name must be smaller than " + Game.LENGTH_NAME + " characters!"));
        });
        runClient(clientThread);

    }

    @Test
    public void testToCreateRoomWithUsedName() throws InterruptedException {
        final Thread clientThread = new Thread(() -> assertTrue(sendMessageFromTwoChannels("nickname az" +
                                                                                                   System.lineSeparator() +
                                                                                                   "create-game 1", "nickname ti" +
                                                                                                   System.lineSeparator() +
                                                                                                   "create-game 1").trim().contains("This room is already created!")));
        runClient(clientThread);

    }

    @Test
    public void testToListCurrentGames() throws InterruptedException {
        final Thread clientThread = new Thread(() -> assertTrue(sendMessageFromTwoChannels("nickname az" +
                                                                                                   System.lineSeparator() +
                                                                                                   "create-game 1", "nickname ti" +
                                                                                                   System.lineSeparator() +
                                                                                                   "list-rooms").trim().contains("| NAME | CREATOR | ISFREE |\n" +
                                                                                                                                         "|------+---------+--------|\n" +
                                                                                                                                         "| 1    | az      |   yes  |\n" +
                                                                                                                                         "|------+---------+--------|")));
        runClient(clientThread);
    }

    @Test
    public void testToJoinInNotExistingGame() throws InterruptedException {
        final Thread clientThread = new Thread(() -> assertTrue(sendMessage("nickname az" +
                                                                                    System.lineSeparator() +
                                                                                    "join-game 1").trim().contains("There is not room with that name!")));
        runClient(clientThread);
    }

    @Test
    public void testToJoinInNotExistingGameRandom() throws InterruptedException {
        final Thread clientThread = new Thread(() -> assertTrue(sendMessage("nickname az" +
                                                                                    System.lineSeparator() +
                                                                                    "join-game").trim().contains("There are not free rooms!")));
        runClient(clientThread);
    }

    @Test
    public void testToJoinInGame() throws InterruptedException {
        final Thread clientThread = new Thread(() -> assertTrue(sendMessageFromTwoChannels("nickname az" +
                                                                                                   System.lineSeparator() +
                                                                                                   "create-game 1", "nickname ti" +
                                                                                                   System.lineSeparator() +
                                                                                                   "join-game 1").trim().contains("Successful join in room!")));
        runClient(clientThread);
    }

    @Test
    public void testToStartGame() throws InterruptedException {
        final Thread clientThread = new Thread(() -> assertTrue(sendMessageFromTwoChannels("nickname az" +
                                                                                                   System.lineSeparator() +
                                                                                                   "create-game 1", "nickname ti" +
                                                                                                   System.lineSeparator() +
                                                                                                   "join-game 1").trim().contains("The GAME will start soon! Get ready! :)")));
        runClient(clientThread);
    }
}
