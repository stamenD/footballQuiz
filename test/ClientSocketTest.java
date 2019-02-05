import GameComponents.Game;
import GameComponents.Player;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClientSocketTest {

    private static Thread serverStarterThread;
    private static Server es;

    @BeforeClass
    public static void setUpBeforeClass() {
        serverStarterThread = new Thread(() -> {
            try (Server ces = new Server(Server.SERVER_PORT);) {
                es = ces;
                es.start();
            } catch (Exception e) {
                System.out.println("An error has occurred");
                e.printStackTrace();
            }
        });

        serverStarterThread.start();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        // es.stopServer();
        // Wake up the server so that it can exit
        serverStarterThread.interrupt();
    }

    @Test
    public void testToConnect() {
        assertEquals("Please set your nickname to continue!", sendMessage("dummy massage").trim());
    }


    @Test
    public void testToSetNickname() {
        assertEquals("You set nickname successful!", sendMessage("nickname az").trim());
    }

    @Test
    public void testToSetNicknameThatWasUsedBefore() {
        assertEquals("You set nickname successful!", sendMessage("nickname az").trim());
        assertEquals("You set nickname successful!", sendMessage("nickname az").trim());
    }

    @Test
    public void testToSetNicknameSecondTime() {
        assertTrue(sendMessage("nickname az" +
                System.lineSeparator() +
                "nickname ti").trim().contains("You set nickname successful!"
                + System.lineSeparator() +
                "You already have a nickname."));
    }

    @Test
    public void testToSetLongNickname() {
        StringBuilder name = new StringBuilder();
        while (name.length() < Player.LENGTH_NAME) {
            name.append("t");
        }
        name.insert(0, "nickname ");
        assertTrue(sendMessage(name.toString()).trim().contains("Your nickname must be smaller than " + Player.LENGTH_NAME + " characters!"));
    }


    @Test
    public void testToSetUsedNickname() {
//        class Answers{
//            private String answersFromFirstSocket = "";
//            private String answersFromSecondSocket;
//
//            boolean checkAnswers(){
//                return  (answersFromFirstSocket.trim().contains("This username is already used!")
//                        &&
//                     answersFromSecondSocket.trim().contains("You set nickname successful!"))
//                        ||
//                        (answersFromSecondSocket.trim().contains("This username is already used!")
//                                &&
//                                answersFromFirstSocket.trim().contains("You set nickname successful!"));
//
//
//
//            }
//        }
//        Answers answers = new Answers();
//        new Thread(() -> {
//            answers.answersFromFirstSocket =  sendMessage("nickname az").trim();
//        }).start();
//        answers.answersFromSecondSocket =  sendMessage("nickname az").trim();
//        assertTrue(answers.checkAnswers());
        assertTrue(sendMessageFromTwoChannels("nickname az","nickname az").trim().contains("This username is already used!"));
//        assertTrue(sendMessageFromTwoChannels("nickname az","nickname az").trim().contains("You set nickname successful!"));


    }


    @Test
    public void testToCreateGameRoom() {
        assertTrue(sendMessage("nickname az" +
                System.lineSeparator() +
                "create-game 1").trim().contains("You set nickname successful!"
                + System.lineSeparator() +
                "Successful create room!"));
    }

    @Test
    public void testToCreateGameRoomWithLongName() {
        StringBuilder name = new StringBuilder();
        while (name.length() < Game.LENGTH_NAME) {
            name.append("t");
        }
        name.insert(0, "create-game ");
        assertTrue(sendMessage("nickname az" +
                System.lineSeparator() +
                name.toString()).trim().contains(
                "Room name must be smaller than " + Game.LENGTH_NAME + " characters!"));
    }


    @Test
    public void testToCreateRoomWithUsedName() {
        assertTrue(sendMessageFromTwoChannels("nickname az" +
                System.lineSeparator() +
                "create-game 1","nickname ti" +
                System.lineSeparator() +
                "create-game 1").trim().contains("This room is already created!"));
    }


    @Test
    public void testToListCurrentGames() {
        assertTrue(sendMessageFromTwoChannels("nickname az" +
                System.lineSeparator() +
                "create-game 1","nickname ti" +
                System.lineSeparator() +
                "list-rooms").trim().contains("| NAME | CREATOR | ISFREE |\n" +
                "|------+---------+--------|\n" +
                "| 1    | az      |   yes  |\n" +
                "|------+---------+--------|"));
    }


    @Test
    public void testToJoinInNotExistingGame() {
        assertTrue(sendMessage("nickname az" +
                System.lineSeparator() +
                "join-game 1").trim().contains("There is not room with that name!"));
    }

    @Test
    public void testToJoinInNotExistingGameRandom() {
        assertTrue(sendMessage("nickname az" +
                System.lineSeparator() +
                "join-game").trim().contains("There are not free rooms!"));
    }


//    @Test
//    public void testToJoinInNotExistingGameRandom() {
//        assertTrue(sendMessage("nickname az" +
//                System.lineSeparator() +
//                "join-game").trim().contains("There are not free rooms!"));
//    }

    private String sendMessage(String msg) {
        String response = "fail";
        try (ByteArrayInputStream in = new ByteArrayInputStream((msg + System.lineSeparator() + "q").getBytes());
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             Client client = new Client("localhost", Server.SERVER_PORT, out, in);) {
            System.out.println("Client " + client + " connected to server");

            client.start();
            // Read the response from the server
            response = out.toString();
            System.out.println("########" + response);

        } catch (IOException e) {
            System.out.println("An error has occurred " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Connection failed  " + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    private String sendMessageFromTwoChannels(String msg1, String msg2) {
        String response = "fail";
        try (ByteArrayInputStream in1 = new ByteArrayInputStream((msg1 + System.lineSeparator() + "q").getBytes());
             ByteArrayOutputStream out1 = new ByteArrayOutputStream();
             ByteArrayInputStream in2 = new ByteArrayInputStream((msg2 + System.lineSeparator() + "q").getBytes());
             ByteArrayOutputStream out2 = new ByteArrayOutputStream();
             Client client1 = new Client("localhost", Server.SERVER_PORT, out1, in1);
             Client client2 = new Client("localhost", Server.SERVER_PORT, out2, in2);) {
            System.out.println("Client " + client1 + " connected to server");
            System.out.println("Client " + client2 + " connected to server");

            client1.start();
            client2.start();
            // Read the response from the server
            response = out1.toString();
            response += out2.toString();
            System.out.println("########" + response);

        } catch (IOException e) {
            System.out.println("An error has occurred " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Connection failed  " + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }
}
