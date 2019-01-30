package io.auklet.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * <p>Auklet daemon task executor that logs exceptions that occur in submitted tasks.</p>
 *
 * <p>To prevent an infinite loop, exceptions that are logged by this executor are not submitted
 * to the Auklet data sink and are only logged to SLF4J.</p>
 */
public final class AukletDaemonExecutor extends ScheduledThreadPoolExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AukletDaemonExecutor.class);

    /**
     * Constructor.
     *
     * @param corePoolSize the number of threads in this executor.
     * @param threadFactory the thread factory to use.
     */
    public AukletDaemonExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    /* Logs exceptions that occur in tasks. */
    @Override protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t == null && r instanceof Future<?>) {
            try {
                Future<?> future = (Future<?>) r;
                if (future.isDone()) future.get();
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        if (t != null) LOGGER.warn("Exception in Auklet daemon task.", t);
    }

}
