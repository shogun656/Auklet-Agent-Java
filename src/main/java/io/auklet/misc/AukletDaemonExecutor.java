package io.auklet.misc;

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
            Future<?> future = (Future<?>) r;
            try {
                if (future.isDone()) future.get();
            } catch (CancellationException ce) {
                if (!(future instanceof CancelSilentlyFutureTask)) t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        if (t != null) LOGGER.warn("Exception in Auklet daemon task.", t);
    }

    /**
     * A {@link FutureTask} that the {@link AukletDaemonExecutor} will not log if it is cancelled.
     *
     * @inheritDoc
     */
    public static final class CancelSilentlyFutureTask<V> extends FutureTask<V> {
        /** @inheritDoc */
        public CancelSilentlyFutureTask(Callable<V> callable) {
            super(callable);
        }
        /** @inheritDoc */
        public CancelSilentlyFutureTask(Runnable runnable, V result) {
            super(runnable, result);
        }
    }

}
