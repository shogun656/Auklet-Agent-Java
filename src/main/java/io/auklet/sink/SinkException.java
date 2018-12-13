package io.auklet.sink;

import io.auklet.AukletException;

/** <p>This exception is thrown {@link Sink} implementations when attempting to send data or close the sink.</p> */
public class SinkException extends AukletException {

    public static final long serialVersionUID = 0L;

    public SinkException() {}

    public SinkException(String message) {
        super(message);
    }

    public SinkException(String message, Throwable cause) {
        super(message, cause);
    }

    public SinkException(Throwable cause) {
        super(cause);
    }

    public SinkException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
