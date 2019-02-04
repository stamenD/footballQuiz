package Services;

import CustomExceptions.StreamError;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.stream.Collectors;

public class IOFile {
    private final static String DEFAULT_PATH_TO_FILE = "result.txt";
    private OutputStream whereToSave;
    private InputStream fromWhereRead;

    public IOFile(OutputStream whereToSave, InputStream fromWhereRead) {
        if (whereToSave != null) {
            this.whereToSave = whereToSave;
        }
        if (fromWhereRead != null) {
            this.fromWhereRead = fromWhereRead;
        }
    }

    synchronized public void saveGame(String result) {
        PrintWriter objectStream = null;
        boolean isOutsideSetStream = false;
        try {
            if (whereToSave == null) {
                whereToSave = new FileOutputStream(DEFAULT_PATH_TO_FILE, true);
            } else {
                isOutsideSetStream = true;
            }
            objectStream = new PrintWriter(whereToSave);
            objectStream.println(result);
            objectStream.flush();
        } catch (IOException e) {
            System.out.println("Not open correct stream!");
            throw new StreamError(e.getMessage());
        } finally {
            if (!isOutsideSetStream) {
                try {
                    objectStream.close();
                    whereToSave.close();
                } catch (IOException e) {
                    System.out.println("Not close correct stream!");
                    throw new StreamError(e.getMessage());
                }
            }
        }
    }

    synchronized public String getAllPlayedGames() {
        boolean isOutsideSetStream = false;
        BufferedReader bufferedReader = null;
        try {
            if (fromWhereRead == null) {
                fromWhereRead = new FileInputStream(DEFAULT_PATH_TO_FILE);
            } else {
                isOutsideSetStream = true;
            }
            bufferedReader = new BufferedReader(new InputStreamReader(fromWhereRead));
            return bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            System.out.println("Not open correct stream!");
            throw new StreamError(e.getMessage());
        } finally {
            if (!isOutsideSetStream) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    System.out.println("Not close correct stream!");
                    throw new StreamError(e.getMessage());
                }
            }
        }


    }

}
