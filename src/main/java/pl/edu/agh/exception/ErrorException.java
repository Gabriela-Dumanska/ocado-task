package pl.edu.agh.exception;

public class ErrorException extends RuntimeException {
    public ErrorException(String message) {
        super(message);
    }
    public ErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
