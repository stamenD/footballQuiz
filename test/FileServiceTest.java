import Services.IOFile;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileServiceTest {


    @Test
    public void testSaveGameInformation() {
        OutputStream outputStream = new ByteArrayOutputStream();
        IOFile io = new IOFile(outputStream, null);
        io.saveGame("DUMMY GAME INFORMATION");
        String result = outputStream.toString();
        try {
            outputStream.close();
        } catch (IOException e) {
            System.err.println("Error occurred when close stream");
        }
        assertEquals("DUMMY GAME INFORMATION", result.trim());
    }

    @Test
    public void testSaveMultipleGameInformation() {
        OutputStream outputStream = new ByteArrayOutputStream();
        IOFile io = new IOFile(outputStream, null);
        io.saveGame("DUMMY GAME INFORMATION1");
        io.saveGame("DUMMY GAME INFORMATION2");
        String result = outputStream.toString();
        try {
            outputStream.close();
        } catch (IOException e) {
            System.err.println("Error occurred when close stream");
        }
        assertEquals("DUMMY GAME INFORMATION1" + System.lineSeparator() + "DUMMY GAME INFORMATION2", result.trim());
    }

    @Test
    public void testReadGamesInformation() {
        OutputStream outputStream = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream("result1 result2".getBytes());
        IOFile io = new IOFile(null, inputStream);
        try {
            outputStream.close();
        } catch (IOException e) {
            System.err.println("Error occurred when close stream");
        }
        assertEquals("result1 result2", io.getAllPlayedGames().trim());
    }

    @Test
    public void testReadGamesInformationFromDefaultDir() {
        IOFile io = new IOFile(null, null);
        io.saveGame("test1");
        assertTrue(io.getAllPlayedGames().trim().contains("test1"));
    }

}
