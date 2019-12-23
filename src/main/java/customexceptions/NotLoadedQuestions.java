package customexceptions;

public class NotLoadedQuestions extends RuntimeException {
    private static final long serialVersionUID = -5476105072223554534L;

    public NotLoadedQuestions(final String message) {
        super(message);
    }
}