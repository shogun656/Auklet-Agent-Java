package io.auklet.sink;

/** <p>This exception is thrown by constructors of {@link Sink} implementations.</p> */
public class SinkInitializationException extends SinkException {

    public static final long serialVersionUID = 0L;

    public SinkInitializationException() {}

    public SinkInitializationException(String message) {
        super(message);
    }

    public SinkInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SinkInitializationException(Throwable cause) {
        super(cause);
    }

    public SinkInitializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
