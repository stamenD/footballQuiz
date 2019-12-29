package entities;

import customexceptions.StreamError;
import services.IOFileService;
import services.QuestionsGenerator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Game {
    public static final int LENGTH_NAME = 5;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int BUFFER_SIZE = 1024;
    private static final int TIME_FOR_THINKING = 10000; // milliseconds
    private static final String ALREADY_ANSWERED = "You have already answered!";
    private static final String SUCCESSFUL_GET_ANSWER = "You have answered: ";
    private static final String GET_READY_MESSAGE = "The GAME will start soon! Get ready! :)";
    private static final String WIN_MESSAGE = "You WIN!";
    private static final String ERROR_MASSAGE = "Аn error occurred in current game!";
    private static final String LOSE_MESSAGE = "You LOSE!";
    private static final String DRAW_MESSAGE = "DRAW!";
    private static final String WAITING_MESSAGE = "Waiting a second player!";
    private final ByteBuffer commandBuffer;
    private final int timeForThinking;
    private final String nameRoom;
    private final Player firstPlayer;
    private final IOFileService recorder;
    private final List<Question> questions = new LinkedList<>();
    private Player secondPlayer;
    private String[] answersFromFirstPlayer;
    private String[] answersFromSecondPlayer;
    private volatile int index;
    private boolean isFinished;
    private boolean isOpenToGetAnswers;

    public Game(final String nameRoom, final int timeForThinking, final Player firstPlayer, final IOFileService recorder) {
        this.timeForThinking = timeForThinking;
        this.recorder = recorder;
        this.nameRoom = nameRoom;
        this.firstPlayer = firstPlayer;
        firstPlayer.setCurrentGame(this);
        commandBuffer = ByteBuffer.allocate(Game.BUFFER_SIZE);
    }

    public Game(final String nameRoom, final Player firstPlayer, final IOFileService recorder) {
        timeForThinking = TIME_FOR_THINKING;
        this.recorder = recorder;
        this.nameRoom = nameRoom;
        this.firstPlayer = firstPlayer;
        firstPlayer.setCurrentGame(this);
        commandBuffer = ByteBuffer.allocate(Game.BUFFER_SIZE);
    }

    public int getTimeForThinking() {
        return timeForThinking;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public boolean isFree() {
        return secondPlayer == null;
    }

    public String getNameRoom() {
        return nameRoom;
    }

    public String getNameRoomFormatted() {
        if (nameRoom != null && nameRoom.length() < Game.LENGTH_NAME) {
            return " " + nameRoom + " ".repeat(Game.LENGTH_NAME - nameRoom.length());
        }
        else {
            return nameRoom;
        }
    }

    public Player getFirstPlayer() {
        return firstPlayer;
    }

    public Player getSecondPlayer() {
        return secondPlayer;
    }

    public void setSecondPlayer(final Player secondPlayer) {
        this.secondPlayer = secondPlayer;
        secondPlayer.setCurrentGame(this);
    }

    private void sendMessageToPlayer(final Player player, final String msg) {
        try {
            commandBuffer.clear();
            commandBuffer.put((msg + System.lineSeparator()).getBytes());
            commandBuffer.flip();
            player.getSocketChannel().write(commandBuffer);
        } catch (final IOException e) {
            e.getMessage();
        }
    }

    private void sendMessageToTwoPlayers(final String msg) {
        sendMessageToPlayer(firstPlayer, msg);
        sendMessageToPlayer(secondPlayer, msg);
    }

    public void startGame() {
        System.out.println("current Thread : " + Thread.currentThread());
        sendMessageToTwoPlayers(Game.GET_READY_MESSAGE);
        QuestionsGenerator.generate(this, -1);
    }

    public void exceptionallyFinishGame(final Throwable e) {
        System.out.println("Can not load questions! " + Arrays.toString(e.getStackTrace()));
        sendMessageToTwoPlayers(Game.ERROR_MASSAGE);
        firstPlayer.setCurrentGame(null);
        secondPlayer.setCurrentGame(null);
        isFinished = true;
    }

    public void beginSendingQuestions() {
//        final Thread t = new Thread(() -> {
        System.out.println("current Thread here : " + Thread.currentThread());
        index = -1;
        answersFromFirstPlayer = new String[questions.size()];
        answersFromSecondPlayer = new String[questions.size()];
        for (final Question question : questions) {
            sendMessageToTwoPlayers(question.toString());
            isOpenToGetAnswers = true;
            index = index + 1;
            try {
                Thread.sleep(timeForThinking);
            } catch (final InterruptedException e) {
                System.out.println("Аn error occurred in current game!");
            }
        }
        sendResult();
        firstPlayer.setCurrentGame(null);
        secondPlayer.setCurrentGame(null);

//        });
//        t.start();
    }


    public boolean isFinished() {
        return isFinished;
    }

    private void sendResult() {
        final int first = calculateCorrectAnswers(answersFromFirstPlayer);
        final int second = calculateCorrectAnswers(answersFromSecondPlayer);

        final String result = String.format("Name:%s | Speed:%s | Date:%s | %s (%d)  :  (%d) %s ",
                                            nameRoom,
                                            timeForThinking,
                                            LocalDateTime.now().format(Game.FORMATTER),
                                            firstPlayer.getUsername(), first,
                                            second, secondPlayer.getUsername());

        sendMessageToTwoPlayers(result);
        try {
            recorder.saveGame(result);
        } catch (final StreamError e) {
            System.out.println("Result has not been saved!");
        }
        if (first > second) {
            sendMessageToPlayer(firstPlayer, Game.WIN_MESSAGE);
            sendMessageToPlayer(secondPlayer, Game.LOSE_MESSAGE);
        }
        else if (second > first) {
            sendMessageToPlayer(secondPlayer, Game.WIN_MESSAGE);
            sendMessageToPlayer(firstPlayer, Game.LOSE_MESSAGE);
        }
        else {
            sendMessageToTwoPlayers(Game.DRAW_MESSAGE);
        }

        isFinished = true;
    }

    private int calculateCorrectAnswers(final String[] answers) {
        int count = 0;
        int innerIndex = 0;
        for (final String ans : answers) {
            System.out.println(innerIndex);
            System.out.println(">>>>" + ans);
            System.out.println("--->" + String.valueOf(questions.get(innerIndex).getCorrectAnswerIndex()).trim());
            if (ans != null && ans.trim().equals(String.valueOf(questions.get(innerIndex).getCorrectAnswerIndex()).trim())) {
                count++;
            }
            innerIndex++;
        }
        return count;
    }

    void getAnswer(final Player from, final String answer) {
        if (isOpenToGetAnswers) {
            if (!answer.equals(" ") && !answer.equals(System.lineSeparator())) {
                System.out.println("answer :" + answer);
                if (from.equals(firstPlayer)) {
                    if (answersFromFirstPlayer[index] != null) {
                        sendMessageToPlayer(firstPlayer, Game.ALREADY_ANSWERED);
                    }
                    else {
                        answersFromFirstPlayer[index] = answer;
                        sendMessageToPlayer(firstPlayer, Game.SUCCESSFUL_GET_ANSWER + answer);
                    }
                }
                else {
                    if (answersFromSecondPlayer[index] != null) {
                        sendMessageToPlayer(secondPlayer, Game.ALREADY_ANSWERED);
                    }
                    else {
                        answersFromSecondPlayer[index] = answer;
                        sendMessageToPlayer(secondPlayer, Game.SUCCESSFUL_GET_ANSWER + answer);
                    }
                }
            }
        }
        else {
            if (from.equals(firstPlayer)) {
                sendMessageToPlayer(firstPlayer, Game.WAITING_MESSAGE);
            }
        }
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Game game = (Game) o;
        return Objects.equals(nameRoom, game.nameRoom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameRoom);
    }
}
