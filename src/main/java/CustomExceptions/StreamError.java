package CustomExceptions;

public class StreamError extends RuntimeException {
    public StreamError(String message) {
        super(message);
    }
}