package entities;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class GameTest {

    @Test
    public void testToCreateInstance() {
        final Player dummy = new Player(null);
        final Game game = new Game("name", dummy, null);
        assertNotNull(game);
        assertEquals("name", game.getNameRoom());
        assertEquals(dummy, game.getFirstPlayer());
    }

    @Test
    public void testToSetSecondPlayerInGame() {
        final Player dummy = new Player(null);
        final Game game = new Game("name", new Player(null), null);
        game.setSecondPlayer(dummy);
        assertEquals(dummy, game.getSecondPlayer());
    }

    @Test
    public void testRoomIsFull() {
        final Player dummy = new Player(null);
        final Game game = new Game("name", new Player(null), null);
        assertTrue(game.isFree());
        game.setSecondPlayer(dummy);
        assertFalse(game.isFree());
    }

    @Test
    public void testGameNameFormatted() {
        final Game one = new Game("t", new Player(null), null);

        final StringBuilder name = new StringBuilder();
        while (name.length() < Game.LENGTH_NAME) {
            name.append("t");
        }
        final Game two = new Game(name.toString(), new Player(null), null);
        assertEquals(" t" + " ".repeat(Game.LENGTH_NAME - "t".length()), one.getNameRoomFormatted());
        assertEquals("t" + "t".repeat(Game.LENGTH_NAME - "t".length()), two.getNameRoomFormatted());
    }


    @Test
    public void testGetAnswerFromPlayerWhenIsNotStartGame() {
        final SocketChannel socketChannelMock = mock(SocketChannel.class);
        final Player dummyPlayer = new Player(socketChannelMock);
        final Game one = new Game("t", dummyPlayer, null);
        one.getAnswer(dummyPlayer, "1");

        try {
            verify(socketChannelMock).write(ByteBuffer.wrap(("Waiting a second player!"
                    + System.lineSeparator()).getBytes()));
        } catch (final IOException e) {
            System.out.println("SocketChannel problem!");
            //  e.printStackTrace();
        }
    }


    @Test
    public void testStartGame() {
        final SocketChannel socketChannelMockOne = mock(SocketChannel.class);
        final SocketChannel socketChannelMockTwo = mock(SocketChannel.class);
        final Player dummyPlayerOne = new Player(socketChannelMockOne);
        final Player dummyPlayerTwo = new Player(socketChannelMockTwo);
        final Game game = new Game("t", dummyPlayerOne, null);
        game.setSecondPlayer(dummyPlayerTwo);
        game.startGame();
        try {
            verify(socketChannelMockOne).write(ByteBuffer.wrap(("The GAME will start soon! Get ready! :)"
                    + System.lineSeparator()).getBytes()));
            verify(socketChannelMockTwo).write(ByteBuffer.wrap(("The GAME will start soon! Get ready! :)"
                    + System.lineSeparator()).getBytes()));
        } catch (final IOException e) {
            System.out.println("SocketChannel problem!");
            //   e.printStackTrace();
        }
    }

}
