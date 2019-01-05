package io.auklet.jvm;

import io.auklet.Auklet;
import io.auklet.AukletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** <p>This class sends all uncaught exceptions, except {@link ThreadDeath}, to Auklet.</p> */
public final class AukletExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AukletExceptionHandler.class);

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (e == null) return;
        if (!(e instanceof ThreadDeath)) {
            try {
                Auklet.send(e);
            } catch (AukletException ae) {
                String threadName = t == null ? "unknown thread" : "thread " + t.getName();
                LOGGER.error("Failed to write uncaught exception {} from {} to Auklet due to error", e.getClass().getName(), threadName, ae);
                // We don't want this exception to just disappear, so log it to SLF4J as a last resort.
                // We can't rethrow it because it will be ignored by the JVM.
                LOGGER.error("Uncaught exception received by Auklet handler that could not be sent to Auklet is: ", e);
            }
        }
    }

}
