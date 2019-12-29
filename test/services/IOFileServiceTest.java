package services;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IOFileServiceTest {


    @Test
    public void testSaveGameInformation() {
        final OutputStream outputStream = new ByteArrayOutputStream();
        final IOFileService io = new IOFileService(outputStream, null);
        io.saveGame("DUMMY GAME INFORMATION");
        final String result = outputStream.toString();
        try {
            outputStream.close();
        } catch (final IOException e) {
            System.err.println("Error occurred when close stream");
        }
        assertEquals("DUMMY GAME INFORMATION", result.trim());
    }

    @Test
    public void testSaveMultipleGameInformation() {
        final OutputStream outputStream = new ByteArrayOutputStream();
        final IOFileService io = new IOFileService(outputStream, null);
        io.saveGame("DUMMY GAME INFORMATION1");
        io.saveGame("DUMMY GAME INFORMATION2");
        final String result = outputStream.toString();
        try {
            outputStream.close();
        } catch (final IOException e) {
            System.err.println("Error occurred when close stream");
        }
        assertEquals("DUMMY GAME INFORMATION1" + System.lineSeparator() + "DUMMY GAME INFORMATION2", result.trim());
    }

    @Test
    public void testReadGamesInformation() {
        final OutputStream outputStream = new ByteArrayOutputStream();
        final InputStream inputStream = new ByteArrayInputStream("result1 result2".getBytes());
        final IOFileService io = new IOFileService(null, inputStream);
        try {
            outputStream.close();
        } catch (final IOException e) {
            System.err.println("Error occurred when close stream");
        }
        assertEquals("result1 result2", io.getAllPlayedGames().trim());
    }

    @Test
    public void testReadGamesInformationFromDefaultDir() {
        final IOFileService io = new IOFileService(null, null);
        io.saveGame("test1");
        assertTrue(io.getAllPlayedGames().trim().contains("test1"));
    }

}
