package Services;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileReader;
import java.util.stream.Collectors;

public class IOFile {
    private final static String PATH_TO_FILE = "result.txt";

    synchronized public static void saveGame(String result) {
        try (PrintWriter objectStream = new PrintWriter(new FileOutputStream(PATH_TO_FILE, true));) {
            objectStream.println(result);
            objectStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized public static String getAllPlayedGames() {
        try (BufferedReader oi = new BufferedReader(new FileReader(PATH_TO_FILE))) {
            return oi.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            e.printStackTrace();
            return "Not available!";
        }
    }

}
