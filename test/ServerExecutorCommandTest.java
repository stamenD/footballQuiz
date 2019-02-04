import Services.IOFile;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;

public class ServerExecutorCommandTest {

    @Test
    public void testSaveGameInformation() {
        OutputStream outputStream = new ByteArrayOutputStream();
        IOFile io = new IOFile(outputStream, null);
        io.saveGame("DUMMY GAME INFORMATION");
        String result = outputStream.toString();
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals("DUMMY GAME INFORMATION", result.trim());
    }
}
