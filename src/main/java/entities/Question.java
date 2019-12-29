package entities;

import java.util.Collections;
import java.util.List;

public class Question {
    private final String content;
    private final List<String> answers;
    private final int correctAnswerIndex;

    public Question(final String content, final List<String> answers, final int correctAnswerIndex) {
        this.content = content;
        this.answers = answers;
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public String getContent() {
        return content;
    }

    public List<String> getAnswers() {
        return Collections.unmodifiableList(answers);
    }

    int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    @Override
    public String toString() {
        return "\n" +
                content +
//                correctAnswerIndex +
                "\n0)" +
                answers.get(0) +
                "\n1)" +
                answers.get(1) +
                "\n2)" +
                answers.get(2);
    }
}
