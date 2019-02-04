package GameComponents;

import CustomExceptions.NotLoadedQuestions;
import CustomExceptions.StreamError;
import GameComponents.QuestionUtils.Question;
import GameComponents.QuestionUtils.QuestionsGenerator;
import GameComponents.QuestionUtils.RequestSender;
import Services.IOFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Game {
    public static final int LENGTH_NAME = 5;


    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int ANSWER_START_INDEX = 3;
    private static final int ANSWER_END_INDEX = 4;
    private static final int BUFFER_SIZE = 1024;
    private static final int TIME_FOR_THINKING = 6000;
    private static final String ALREADY_ANSWERED = "You already answered for this question!";
    private static final String GET_READY_MESSAGE = "The GAME will start soon! Get ready! :)";
    private static final String WIN_MESSAGE = "You WIN!";
    private static final String ERROR_MASSAGE = "Аn error occurred in current game!";
    private static final String LOSE_MESSAGE = "You LOSE!";
    private static final String DRAW_MESSAGE = "DRAW!";
    private static final String WAITING_MESSAGE = "Waiting a second player!";
    private static final QuestionsGenerator GENERATOR = new QuestionsGenerator();

    private ByteBuffer commandBuffer;
    private String nameRoom;
    private Player firstPlayer;
    private Player secondPlayer;
    private List<Question> questions;
    private List<String> answersFromFirstPlayer;
    private List<String> answersFromSecondPlayer;
    private volatile int index;
    private boolean isFinished;
    private boolean isOpenToGetAnswers;

    public Game(String nameRoom, Player firstPlayer) {
        this.nameRoom = nameRoom;
        this.firstPlayer = firstPlayer;
        firstPlayer.setCurrentGame(this);
        this.commandBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    }

    public void setSecondPlayer(Player secondPlayer) {
        this.secondPlayer = secondPlayer;
        secondPlayer.setCurrentGame(this);
    }

    public boolean isFree() {
        return secondPlayer == null;
    }

    public String getNameRoom() {
        return nameRoom;
    }

    public String getNameRoomFormated() {
        if (nameRoom != null && nameRoom.length() < LENGTH_NAME)
            return " " + nameRoom + " ".repeat(LENGTH_NAME - nameRoom.length());
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

    private void sendMessageToPlayer(Player p, String msg) {
        try {
            commandBuffer.clear();
            commandBuffer.put((msg + System.lineSeparator()).getBytes());
            commandBuffer.flip();
            System.out.println(p.getSocketChannel().write(commandBuffer));
        } catch (IOException e) {
            e.getMessage();
        }
    }

    private void sendMessageToTwoPlayers(String msg) {
        sendMessageToPlayer(firstPlayer, msg);
        sendMessageToPlayer(secondPlayer, msg);
    }

    private void loadQuestions() {
        try {
            questions = Game.GENERATOR.generate(new RequestSender(), -1);
        } catch (Exception e) {
            throw new NotLoadedQuestions(e.getMessage());
        }

    }

    public void startGame() {
        sendMessageToTwoPlayers(GET_READY_MESSAGE);
        Thread t = new Thread(() -> {
            try {
                loadQuestions();

                index = 0;
                answersFromFirstPlayer = new ArrayList<>();
                answersFromSecondPlayer = new ArrayList<>();
                for (Question question : questions) {
                    sendMessageToTwoPlayers(question.toString());
                    isOpenToGetAnswers = true;
                    index = index + 1;
                    try {
                        Thread.sleep(TIME_FOR_THINKING);
                    } catch (InterruptedException e) {
                        System.out.println("Аn error occurred in current game!");
                    }
                }
                sendResult();
            } catch (NotLoadedQuestions e) {
                System.out.println("Can not load questions!");
                sendMessageToTwoPlayers(ERROR_MASSAGE);
            } finally {
                firstPlayer.setCurrentGame(null);
                secondPlayer.setCurrentGame(null);
            }

        });
        t.start();
    }

    public boolean isFinished() {
        return isFinished;
    }

    private void sendResult() {
        int first = calculateCorrectAnswers(answersFromFirstPlayer);
        int second = calculateCorrectAnswers(answersFromSecondPlayer);

        String result = String.format("Name:%s | Date:%s | %s (%d)  :  (%d) %s ",
                this.nameRoom,
                LocalDateTime.now().format(FORMATTER),
                firstPlayer.getUsername(), first,
                second, secondPlayer.getUsername());

        sendMessageToTwoPlayers(result);
        try {
            new IOFile(null, null).saveGame(result);
        } catch (StreamError e) {
            System.out.println("Result has not been saved!");
        }
        if (first > second) {
            sendMessageToPlayer(firstPlayer, WIN_MESSAGE);
            sendMessageToPlayer(secondPlayer, LOSE_MESSAGE);
        } else if (second > first) {
            sendMessageToPlayer(secondPlayer, WIN_MESSAGE);
            sendMessageToPlayer(firstPlayer, LOSE_MESSAGE);
        } else {
            sendMessageToTwoPlayers(DRAW_MESSAGE);
        }

        isFinished = true;
    }

    private int calculateCorrectAnswers(List<String> answers) {
        int count = 0;
        int lastCalculate = -1;
        for (String ans : answers) {
            if (Integer.parseInt(ans.substring(0, 1)) - 1 != lastCalculate) {
                lastCalculate = Integer.parseInt(ans.substring(0, 1)) - 1;
                if (ans.substring(ANSWER_START_INDEX, ANSWER_END_INDEX)
                        .equals(String.valueOf(questions.get(lastCalculate).getCorrectAnswerIndex()))) {
                    count++;
                }
            }
        }
        return count;
    }

    public void getAnswer(Player from, String answer) {
        if (isOpenToGetAnswers) {
            if (!answer.equals(" ") && !answer.equals(System.lineSeparator())) {
                System.out.println("answer :" + answer);
                if (from.equals(firstPlayer)) {
                    if (index == answersFromFirstPlayer.size()) {
                        sendMessageToPlayer(firstPlayer, ALREADY_ANSWERED);
                    } else {
                        answersFromFirstPlayer.add(index + ") " + answer);
                    }
                } else {
                    System.out.println("index:" + index);
                    System.out.println("size:" + answersFromSecondPlayer.size());
                    if (index == answersFromSecondPlayer.size()) {
                        sendMessageToPlayer(secondPlayer, ALREADY_ANSWERED);
                    } else {
                        answersFromSecondPlayer.add(index + ") " + answer);
                    }
                }
            }
        } else {
            if (from.equals(firstPlayer)) {
                sendMessageToPlayer(firstPlayer, WAITING_MESSAGE);
            }
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return Objects.equals(nameRoom, game.nameRoom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameRoom);
    }
}
