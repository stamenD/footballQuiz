package services;

import customexceptions.StreamError;

import java.io.*;
import java.util.stream.Collectors;

public class IOFile {
    private final static String DEFAULT_PATH_TO_FILE = "result.txt";
    private OutputStream whereToSave;
    private InputStream fromWhereRead;

    public IOFile() {
        this(null, null);
        System.out.println("NEW INSTANCE!");
    }

    public IOFile(final OutputStream whereToSave, final InputStream fromWhereRead) {
        this.whereToSave = whereToSave;
        this.fromWhereRead = fromWhereRead;
    }

    synchronized public void saveGame(final String result) {
        PrintWriter objectStream = null;
        boolean isOutsideSetStream = false;
        try {
            if (whereToSave == null) {
                whereToSave = new FileOutputStream(services.IOFile.DEFAULT_PATH_TO_FILE, true);
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
//                try {
                if (objectStream != null) {
                    whereToSave = null;
                    objectStream.close();
                }
//                } catch (IOException e) {
//                    System.out.println("Not close correct stream!");
//                    throw new StreamError(e.getMessage());
//                }
            }
        }
    }

    synchronized public String getAllPlayedGames() {
        boolean isOutsideSetStream = false;
        BufferedReader bufferedReader = null;
        try {
            if (fromWhereRead == null) {
                fromWhereRead = new FileInputStream(services.IOFile.DEFAULT_PATH_TO_FILE);
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
                        fromWhereRead = null;
                        bufferedReader.close();
                    }
                } catch (final IOException e) {
                    System.out.println("Not close correct stream!");
                    throw new StreamError(e.getMessage());
                }
            }
        }


    }

}
