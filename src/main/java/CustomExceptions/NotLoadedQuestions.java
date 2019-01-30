package CustomExceptions;

public class NotLoadedQuestions extends RuntimeException {
    public NotLoadedQuestions(String message) {
        super(message);
    }
}