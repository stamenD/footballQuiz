package customexceptions;

public class StreamError extends RuntimeException {
    private static final long serialVersionUID = 6779244982607478856L;

    public StreamError(final String message) {
        super(message);
    }
}