package GameComponents.QuestionUtils;

import java.util.Collections;
import java.util.List;

public class Question {
    private String content;
    private List<String> answers;
    private int correctAnswerIndex;

    public Question(String content, List<String> answers, int correctAnswerIndex) {
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

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n")
                .append(content)
                .append("\na)")
                .append(answers.get(0))
                .append("\nb)")
                .append(answers.get(1))
                .append("\nc)")
                .append(answers.get(2));
        return sb.toString();
    }
}
