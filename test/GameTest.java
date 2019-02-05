import GameComponents.Game;
import GameComponents.Player;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class GameTest {

    @Test
    public void testToCreateInstance() {
        Player dummy = new Player(null);
        Game game = new Game("name", dummy);
        assertNotNull(game);
        assertEquals("name", game.getNameRoom());
        assertEquals(dummy, game.getFirstPlayer());
    }

    @Test
    public void testToSetSecondPlayerInGame() {
        Player dummy = new Player(null);
        Game game = new Game("name", new Player(null));
        game.setSecondPlayer(dummy);
        assertEquals(dummy, game.getSecondPlayer());
    }

    @Test
    public void testRoomIsFull() {
        Player dummy = new Player(null);
        Game game = new Game("name", new Player(null));
        assertTrue(game.isFree());
        game.setSecondPlayer(dummy);
        assertFalse(game.isFree());
    }

    @Test
    public void testGameNameFormatted() {
        Game one = new Game("t", new Player(null));

        StringBuilder name = new StringBuilder();
        while (name.length() < Game.LENGTH_NAME) {
            name.append("t");
        }
        Game two = new Game(name.toString(), new Player(null));
        assertEquals(" t" + " ".repeat(Game.LENGTH_NAME - "t".length()), one.getNameRoomFormatted());
        assertEquals("t" + "t".repeat(Game.LENGTH_NAME - "t".length()), two.getNameRoomFormatted());
    }


    @Test
    public void testGetAnswerFromPlayerWhenIsNotStartGame() {
        SocketChannel socketChannelMock = mock(SocketChannel.class);
        Player dummyPlayer = new Player(socketChannelMock);
        Game one = new Game("t", dummyPlayer);
        one.getAnswer(dummyPlayer, "1");

        try {
            verify(socketChannelMock).write(ByteBuffer.wrap(("Waiting a second player!"
                    + System.lineSeparator()).getBytes()));
        } catch (IOException e) {
            System.out.println("SocketChannel problem!");
            e.printStackTrace();
        }
    }


    @Test
    public void testStartGame() {
        SocketChannel socketChannelMockOne = mock(SocketChannel.class);
        SocketChannel socketChannelMockTwo = mock(SocketChannel.class);
        Player dummyPlayerOne = new Player(socketChannelMockOne);
        Player dummyPlayerTwo = new Player(socketChannelMockTwo);
        Game game = new Game("t", dummyPlayerOne);
        game.setSecondPlayer(dummyPlayerTwo);
        game.startGame();
        try {
            verify(socketChannelMockOne).write(ByteBuffer.wrap(("The GAME will start soon! Get ready! :)"
                    + System.lineSeparator()).getBytes()));
            verify(socketChannelMockTwo).write(ByteBuffer.wrap(("The GAME will start soon! Get ready! :)"
                    + System.lineSeparator()).getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
