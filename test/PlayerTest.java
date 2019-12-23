import entities.Game;
import entities.Player;
import services.IOFile;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class PlayerTest {

    @Test
    public void testToCreatePlayer() {
        Player one = new Player(null);
        assertNotNull(one);
        assertNull(one.getSocketChannel());
    }

    @Test
    public void testToSetPlayerName() {
        Player one = new Player(null);
        one.setUsername("123");
        assertEquals("123", one.getUsername());
        assertNotNull(one.getUsername());
    }

    @Test
    public void testToSetPlayerGame() {
        Player one = new Player(null);
        Game randomGame = new Game("f", one, new IOFile());
        one.setCurrentGame(randomGame);
        assertEquals(randomGame, one.getCurrentGame());
    }

    @Test
    public void testToGetFormattedPlayerName() {
        Player one = new Player(null);
        Player two = new Player(null);

        StringBuilder name = new StringBuilder();
        while (name.length() < Player.LENGTH_NAME) {
            name.append("t");
        }
        two.setUsername(name.toString());
        one.setUsername("t");
        assertEquals(" t" + " ".repeat(Player.LENGTH_NAME - "t".length()), one.getUsernameFormat());
        assertEquals("t" + "t".repeat(Player.LENGTH_NAME - "t".length()), two.getUsernameFormat());
    }

    @Test
    public void testToPlayerToSendAnswers(){

        Game mockGame = mock(Game.class);

        Player one = new Player(null);
        one.setCurrentGame(mockGame);
        one.sendAnswer("dummy Answer");

        verify(mockGame, times(1)).getAnswer(one,"dummy Answer");
    }
}
