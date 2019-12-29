package services;

import customexceptions.StreamError;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.stream.Collectors;

public class IOFileService {
    private final static String DEFAULT_PATH_TO_FILE = "result.txt";
    private OutputStream whereToSave;
    private InputStream fromWhereRead;

    public IOFileService() {
        this(null, null);
        System.out.println("NEW INSTANCE!");
    }

    IOFileService(final OutputStream whereToSave, final InputStream fromWhereRead) {
        this.whereToSave = whereToSave;
        this.fromWhereRead = fromWhereRead;
    }

    synchronized public void saveGame(final String result) {
        PrintWriter objectStream = null;
        boolean isOutsideSetStream = false;
        try {
            if (whereToSave == null) {
                whereToSave = new FileOutputStream(IOFileService.DEFAULT_PATH_TO_FILE, true);
            }
            else {
                isOutsideSetStream = true;
            }
            objectStream = new PrintWriter(whereToSave);
            objectStream.println(result);
            objectStream.flush();
        } catch (final IOException e) {
            System.out.println("Not open correct stream!");
            throw new StreamError(e.getMessage());
        } finally {
            if (!isOutsideSetStream) {
                try {
                    if (objectStream != null) {
                        whereToSave.close();
                        objectStream.close();
                        whereToSave = null;
                    }
                } catch (final IOException e) {
                    System.out.println("Not close correct stream!");
                    throw new StreamError(e.getMessage());
                }
            }
        }
    }

    synchronized String getAllPlayedGames() {
        boolean isOutsideSetStream = false;
        BufferedReader bufferedReader = null;
        try {
            if (fromWhereRead == null) {
                fromWhereRead = new FileInputStream(IOFileService.DEFAULT_PATH_TO_FILE);
            }
            else {
                isOutsideSetStream = true;
            }
            bufferedReader = new BufferedReader(new InputStreamReader(fromWhereRead));
            return bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (final IOException e) {
            System.out.println("Not open correct stream!");
            throw new StreamError(e.getMessage());
        } finally {
            if (!isOutsideSetStream) {
                try {
                    if (bufferedReader != null) {
                        fromWhereRead.close();
                        bufferedReader.close();
                        fromWhereRead = null;
                    }
                } catch (final IOException e) {
                    System.out.println("Not close correct stream!");
                    throw new StreamError(e.getMessage());
                }
            }
        }


    }

}
