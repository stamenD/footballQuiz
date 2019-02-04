import CustomExceptions.NotFoundFreeRoom;
import GameComponents.Game;
import GameComponents.Player;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;


import static org.junit.Assert.assertEquals;

public class ServerExecutorCommandTest {

    @Test
    public void testToSetNickname() {
        Map<SocketChannel, Player> onlineUsersTest = new HashMap<>();
        Player firstPlayer = new Player(null);
        onlineUsersTest.put(null, firstPlayer);
        String result = ServerExecutorCommand.setNickname("nickname t".split(" "), onlineUsersTest, null);
        assertEquals("You set nickname successful!", result);
        assertEquals("t", onlineUsersTest.get(null).getUsername());
        assertEquals("t", firstPlayer.getUsername());
    }

    @Test
    public void testToSetExistNickname() {
        Map<SocketChannel, Player> onlineUsersTest = new HashMap<>();
        SocketChannel firstChannel = mock(SocketChannel.class);
        SocketChannel secondChannel = mock(SocketChannel.class);
        Player firstPlayer = new Player(firstChannel);
        Player secondPlayer = new Player(secondChannel);
        onlineUsersTest.put(firstChannel, firstPlayer);
        onlineUsersTest.put(secondChannel, secondPlayer);
        ServerExecutorCommand.setNickname("nickname t".split(" "), onlineUsersTest, firstChannel);
        String result = ServerExecutorCommand.setNickname("nickname t".split(" "), onlineUsersTest, secondChannel);
        assertEquals("This username is already used!", result);
        assertEquals("t", firstPlayer.getUsername());
        assertNull(secondPlayer.getUsername());
    }

    @Test
    public void testToSetLongNickname() {
        Map<SocketChannel, Player> onlineUsersTest = new HashMap<>();
        SocketChannel firstChannel = mock(SocketChannel.class);
        Player firstPlayer = new Player(firstChannel);
        onlineUsersTest.put(firstChannel, firstPlayer);
        StringBuilder name = new StringBuilder();
        while (name.length() < Player.LENGTH_NAME) {
            name.append("t");
        }
        name.insert(0, "nickname ");
        String result = ServerExecutorCommand.setNickname(name.toString().split(" "), onlineUsersTest, firstChannel);
        assertEquals("Your nickname must be smaller than " + Player.LENGTH_NAME + " characters!", result);
        assertNull(firstPlayer.getUsername());
    }

    @Test
    public void testToCreateGame() {
        Map<String, Game> games = new HashMap<>();
        Map<SocketChannel, Player> onlineUsersTest = new HashMap<>();
        SocketChannel firstChannel = mock(SocketChannel.class);
        Player firstPlayer = new Player(firstChannel);
        onlineUsersTest.put(firstChannel, firstPlayer);
        String result = ServerExecutorCommand.createGame("create-game f".split(" "),
                onlineUsersTest,
                firstChannel,
                games);
        assertEquals("Successful create room!", result);
    }

    @Test
    public void testToCreateGameRoomAlreadyExist() {
        Map<String, Game> games = new HashMap<>();
        Map<SocketChannel, Player> onlineUsersTest = new HashMap<>();
        SocketChannel firstChannel = mock(SocketChannel.class);
        SocketChannel secondChannel = mock(SocketChannel.class);
        Player firstPlayer = new Player(firstChannel);
        Player secondPlayer = new Player(secondChannel);
        onlineUsersTest.put(firstChannel, firstPlayer);
        onlineUsersTest.put(secondChannel, secondPlayer);
        ServerExecutorCommand.createGame("create-game f".split(" "),
                onlineUsersTest,
                firstChannel,
                games);
        String result = ServerExecutorCommand.createGame("create-game f".split(" "),
                onlineUsersTest,
                secondChannel,
                games);
        assertEquals("This room is already created!", result);
    }

    @Test
    public void testToCreateGameWithLongName() {
        Map<String, Game> games = new HashMap<>();
        Map<SocketChannel, Player> onlineUsersTest = new HashMap<>();
        SocketChannel firstChannel = mock(SocketChannel.class);
        Player firstPlayer = new Player(firstChannel);
        onlineUsersTest.put(firstChannel, firstPlayer);

        StringBuilder name = new StringBuilder();
        while (name.length() < Game.LENGTH_NAME) {
            name.append("f");
        }
        name.insert(0, "create-game ");

        String result = ServerExecutorCommand.createGame(name.toString().split(" "),
                onlineUsersTest,
                firstChannel,
                games);
        assertEquals("Room name must be smaller than " + Game.LENGTH_NAME + " characters!", result);
    }

    @Test
    public void testToViewCurrentGames() {

        assertEquals("| NAME | CREATOR | ISFREE |\n" +
                "|------+---------+--------|\n" +
                "|------+---------+--------|\n", ServerExecutorCommand.listCurrentGames(null));
    }

    @Test
    public void testToViewCreatedGames() {

        Map<String, Game> games = new HashMap<>();
        Player creator = new Player(null);
        creator.setUsername("you");
        games.put("room", new Game("room", creator));
        assertEquals("| NAME | CREATOR | ISFREE |\n" +
                "|------+---------+--------|\n" +
                "| room | you     |   yes  |\n" +
                "|------+---------+--------|\n", ServerExecutorCommand.listCurrentGames(games));
    }

    @Test
    public void testToJoinInNotExistedGame() {
        Map<String, Game> games = new HashMap<>();
        Map<SocketChannel, Player> onlineUsersTest = new HashMap<>();
        SocketChannel firstChannel = mock(SocketChannel.class);
        Player firstPlayer = new Player(firstChannel);
        onlineUsersTest.put(firstChannel, firstPlayer);

        String result = ServerExecutorCommand.joinInGame("join-game f".split(" "),
                onlineUsersTest,
                firstChannel,
                games);
        assertEquals("There is not room with that name!", result);
    }

    @Test
    public void testToJoinInFullRoomGame() {
        Map<String, Game> games = new HashMap<>();

        Game fullRoom = mock(Game.class);
        when(fullRoom.isFree()).thenReturn(false);
        when(fullRoom.getNameRoom()).thenReturn("f");
        games.put("f", fullRoom);

        String result = ServerExecutorCommand.joinInGame("join-game f".split(" "),
                null,
                null,
                games);
        assertEquals("This room is full!", result);
    }

    @Test(expected = NotFoundFreeRoom.class)
    public void testToJoinInRandomGameButThisOneNotExist()  {
        Map<String, Game> games = new HashMap<>();

        String result = ServerExecutorCommand.joinInGame("join-game".split(" "),
                null,
                null,
                games);
        assertEquals("Successful join in room!", result);
    }

    @Test
    public void testToJoinInRandomGame() {
        Map<String, Game> games = new HashMap<>();
        Map<SocketChannel, Player> onlineUsersTest = new HashMap<>();
        SocketChannel firstChannel = mock(SocketChannel.class);
        SocketChannel secondChannel = mock(SocketChannel.class);
        Player firstPlayer = new Player(firstChannel);
        Player secondPlayer = new Player(secondChannel);
        onlineUsersTest.put(firstChannel, firstPlayer);
        onlineUsersTest.put(secondChannel, secondPlayer);
        Game freeRoom = new Game("f",firstPlayer);
        games.put("f",freeRoom);

        String result = ServerExecutorCommand.joinInGame("join-game".split(" "),
                onlineUsersTest,
                secondChannel,
                games);
        assertEquals("Successful join in room!", result);
        assertEquals(firstPlayer, freeRoom.getFirstPlayer());
        assertEquals(secondPlayer, freeRoom.getSecondPlayer());

    }

    @Test
    public void testToJoinInSpecificGame() {
        Map<String, Game> games = new HashMap<>();
        Map<SocketChannel, Player> onlineUsersTest = new HashMap<>();
        SocketChannel firstChannel = mock(SocketChannel.class);
        SocketChannel secondChannel = mock(SocketChannel.class);
        Player firstPlayer = new Player(firstChannel);
        Player secondPlayer = new Player(secondChannel);
        onlineUsersTest.put(firstChannel, firstPlayer);
        onlineUsersTest.put(secondChannel, secondPlayer);
        Game freeRoom = new Game("f",firstPlayer);
        games.put("f",freeRoom);

        String result = ServerExecutorCommand.joinInGame("join-game f".split(" "),
                onlineUsersTest,
                secondChannel,
                games);
        assertEquals("Successful join in room!", result);
        assertEquals(firstPlayer, freeRoom.getFirstPlayer());
        assertEquals(secondPlayer, freeRoom.getSecondPlayer());

    }

}
