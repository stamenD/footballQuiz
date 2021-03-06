package entities;

import org.junit.Test;
import services.IOFileService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PlayerTest {

    @Test
    public void testToCreatePlayer() {
        final Player one = new Player(null);
        assertNotNull(one);
        assertNull(one.getSocketChannel());
    }

    @Test
    public void testToSetPlayerName() {
        final Player one = new Player(null);
        one.setUsername("123");
        assertEquals("123", one.getUsername());
        assertNotNull(one.getUsername());
    }

    @Test
    public void testToSetPlayerGame() {
        final Player one = new Player(null);
        final Game randomGame = new Game("f", one, new IOFileService());
        one.setCurrentGame(randomGame);
        assertEquals(randomGame, one.getCurrentGame());
    }

    @Test
    public void testToGetFormattedPlayerName() {
        final Player one = new Player(null);
        final Player two = new Player(null);

        final StringBuilder name = new StringBuilder();
        while (name.length() < Player.LENGTH_NAME) {
            name.append("t");
        }
        two.setUsername(name.toString());
        one.setUsername("t");
        assertEquals(" t" + " ".repeat(Player.LENGTH_NAME - "t".length()), one.getUsernameFormat());
        assertEquals("t" + "t".repeat(Player.LENGTH_NAME - "t".length()), two.getUsernameFormat());
    }

    @Test
    public void testToPlayerToSendAnswers() {

        final Game mockGame = mock(Game.class);

        final Player one = new Player(null);
        one.setCurrentGame(mockGame);
        one.sendAnswer("dummy Answer");

        verify(mockGame, times(1)).getAnswer(one, "dummy Answer");
    }
}
