package services;

import customexceptions.NotFoundFreeRoom;
import entities.Game;
import entities.Player;
import org.junit.Test;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServerExecutorCommandTest {

    @Test
    public void testToSetNickname() {
        final Map<SocketChannel, Player> onlineUsersTest = new HashMap<>();
        final Player firstPlayer = new Player(null);
        onlineUsersTest.put(null, firstPlayer);
        final String result = services.ServerExecutorCommand.setNickname("nickname t".split(" "), onlineUsersTest, null);
        assertEquals("You set nickname successful!", result);
        assertEquals("t", onlineUsersTest.get(null).getUsername());
        assertEquals("t", firstPlayer.getUsername());
    }

    @Test
    public void testToSetExistNickname() {
        final Map<SocketChannel, Player> onlineUsersTest = new HashMap<>();
        final SocketChannel firstChannel = mock(SocketChannel.class);
        final SocketChannel secondChannel = mock(SocketChannel.class);
        final Player firstPlayer = new Player(firstChannel);
        final Player secondPlayer = new Player(secondChannel);
        onlineUsersTest.put(firstChannel, firstPlayer);
        onlineUsersTest.put(secondChannel, secondPlayer);
        services.ServerExecutorCommand.setNickname("nickname t".split(" "), onlineUsersTest, firstChannel);
        final String result = services.ServerExecutorCommand.setNickname("nickname t".split(" "), onlineUsersTest, secondChannel);
        assertEquals("This username is already used!", result);
        assertEquals("t", firstPlayer.getUsername());
        assertNull(secondPlayer.getUsername());
    }

    @Test
    public void testToSetLongNickname() {
        final Map<SocketChannel, Player> onlineUsersTest = new HashMap<>();
        final SocketChannel firstChannel = mock(SocketChannel.class);
        final Player firstPlayer = new Player(firstChannel);
        onlineUsersTest.put(firstChannel, firstPlayer);
        final StringBuilder name = new StringBuilder();
        while (name.length() < Player.LENGTH_NAME) {
            name.append("t");
        }
        name.insert(0, "nickname ");
        final String result = services.ServerExecutorCommand.setNickname(name.toString().split(" "), onlineUsersTest, firstChannel);
        assertEquals("Your nickname must be smaller than " + Player.LENGTH_NAME + " characters!", result);
        assertNull(firstPlayer.getUsername());
    }

    @Test
    public void testToCreateGame() {
        final Map<String, Game> games = new HashMap<>();
        final Map<SocketChannel, Player> onlineUsersTest = new HashMap<>();
        final SocketChannel firstChannel = mock(SocketChannel.class);
        final Player firstPlayer = new Player(firstChannel);
        onlineUsersTest.put(firstChannel, firstPlayer);
        final String result = services.ServerExecutorCommand.createGame("create-game f".split(" "),
                                                                        onlineUsersTest,
                                                                        firstChannel,
                                                                        games);
        assertEquals("Successful create room!", result);
    }

    @Test
    public void testToCreateGameRoomAlreadyExist() {
        final Map<String, Game> games = new HashMap<>();
        final Map<SocketChannel, Player> onlineUsersTest = new HashMap<>();
        final SocketChannel firstChannel = mock(SocketChannel.class);
        final SocketChannel secondChannel = mock(SocketChannel.class);
        final Player firstPlayer = new Player(firstChannel);
        final Player secondPlayer = new Player(secondChannel);
        onlineUsersTest.put(firstChannel, firstPlayer);
        onlineUsersTest.put(secondChannel, secondPlayer);
        services.ServerExecutorCommand.createGame("create-game f".split(" "),
                                                  onlineUsersTest,
                                                  firstChannel,
                                                  games);
        final String result = services.ServerExecutorCommand.createGame("create-game f".split(" "),
                                                                        onlineUsersTest,
                                                                        secondChannel,
                                                                        games);
        assertEquals("This room is already created!", result);
    }

    @Test
    public void testToCreateGameWithLongName() {
        final Map<String, Game> games = new HashMap<>();
        final Map<SocketChannel, Player> onlineUsersTest = new HashMap<>();
        final SocketChannel firstChannel = mock(SocketChannel.class);
        final Player firstPlayer = new Player(firstChannel);
        onlineUsersTest.put(firstChannel, firstPlayer);

        final StringBuilder name = new StringBuilder();
        while (name.length() < Game.LENGTH_NAME) {
            name.append("f");
        }
        name.insert(0, "create-game ");

        final String result = services.ServerExecutorCommand.createGame(name.toString().split(" "),
                                                                        onlineUsersTest,
                                                                        firstChannel,
                                                                        games);
        assertEquals("Room name must be smaller than " + Game.LENGTH_NAME + " characters!", result);
    }

    @Test
    public void testToViewCurrentGames() {

        assertEquals("| NAME | SPEED | CREATOR | ISFREE |\n" +
                             "|------+-------+---------+--------|\n" +
                             "|------+-------+---------+--------|\n", services.ServerExecutorCommand.listCurrentGames(null));
    }

    @Test
    public void testToViewCreatedGames() {

        final Map<String, Game> games = new HashMap<>();
        final Player creator = new Player(null);
        creator.setUsername("you");
        games.put("room", new Game("room", creator, null));
        assertEquals("| NAME | SPEED | CREATOR | ISFREE |\n" +
                             "|------+-------+---------+--------|\n" +
                             "| room | 10000 | you     |   yes  |\n" +
                             "|------+-------+---------+--------|\n", services.ServerExecutorCommand.listCurrentGames(games));
    }

    @Test
    public void testToJoinInNotExistedGame() {
        final Map<String, Game> games = new HashMap<>();
        final Map<SocketChannel, Player> onlineUsersTest = new HashMap<>();
        final SocketChannel firstChannel = mock(SocketChannel.class);
        final Player firstPlayer = new Player(firstChannel);
        onlineUsersTest.put(firstChannel, firstPlayer);

        final String result = services.ServerExecutorCommand.joinInGame("join-game f".split(" "),
                                                                        onlineUsersTest,
                                                                        firstChannel,
                                                                        games);
        assertEquals("There is not room with that name!", result);
    }

    @Test
    public void testToJoinInFullRoomGame() {
        final Map<String, Game> games = new HashMap<>();

        final Game fullRoom = mock(Game.class);
        when(fullRoom.isFree()).thenReturn(false);
        when(fullRoom.getNameRoom()).thenReturn("f");
        games.put("f", fullRoom);

        final String result = services.ServerExecutorCommand.joinInGame("join-game f".split(" "),
                                                                        null,
                                                                        null,
                                                                        games);
        assertEquals("This room is full!", result);
    }

    @Test(expected = NotFoundFreeRoom.class)
    public void testToJoinInRandomGameButThisOneNotExist() {
        final Map<String, Game> games = new HashMap<>();

        final String result = services.ServerExecutorCommand.joinInGame("join-game".split(" "),
                                                                        null,
                                                                        null,
                                                                        games);
        assertEquals("Successful join in room!", result);
    }

    @Test
    public void testToJoinInRandomGame() {
        final Map<String, Game> games = new HashMap<>();
        final Map<SocketChannel, Player> onlineUsersTest = new HashMap<>();
        final SocketChannel firstChannel = mock(SocketChannel.class);
        final SocketChannel secondChannel = mock(SocketChannel.class);
        final Player firstPlayer = new Player(firstChannel);
        final Player secondPlayer = new Player(secondChannel);
        onlineUsersTest.put(firstChannel, firstPlayer);
        onlineUsersTest.put(secondChannel, secondPlayer);
        final Game freeRoom = new Game("f", firstPlayer, null);
        games.put("f", freeRoom);

        final String result = services.ServerExecutorCommand.joinInGame("join-game".split(" "),
                                                                        onlineUsersTest,
                                                                        secondChannel,
                                                                        games);
        assertEquals("Successful join in room!", result);
        assertEquals(firstPlayer, freeRoom.getFirstPlayer());
        assertEquals(secondPlayer, freeRoom.getSecondPlayer());

    }

    @Test
    public void testToJoinInSpecificGame() {
        final Map<String, Game> games = new HashMap<>();
        final Map<SocketChannel, Player> onlineUsersTest = new HashMap<>();
        final SocketChannel firstChannel = mock(SocketChannel.class);
        final SocketChannel secondChannel = mock(SocketChannel.class);
        final Player firstPlayer = new Player(firstChannel);
        final Player secondPlayer = new Player(secondChannel);
        onlineUsersTest.put(firstChannel, firstPlayer);
        onlineUsersTest.put(secondChannel, secondPlayer);
        final Game freeRoom = new Game("f", firstPlayer, null);
        games.put("f", freeRoom);

        final String result = services.ServerExecutorCommand.joinInGame("join-game f".split(" "),
                                                                        onlineUsersTest,
                                                                        secondChannel,
                                                                        games);
        assertEquals("Successful join in room!", result);
        assertEquals(firstPlayer, freeRoom.getFirstPlayer());
        assertEquals(secondPlayer, freeRoom.getSecondPlayer());

    }

}
