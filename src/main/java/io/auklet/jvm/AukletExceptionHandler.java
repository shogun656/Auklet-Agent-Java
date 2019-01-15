package io.auklet.jvm;

import io.auklet.Auklet;
import net.jcip.annotations.Immutable;

/** <p>This class sends all uncaught exceptions, except {@link ThreadDeath}, to Auklet.</p> */
@Immutable
public final class AukletExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override public void uncaughtException(Thread t, Throwable e) {
        if (e == null) return;
        if (!(e instanceof ThreadDeath)) Auklet.send(e);
    }

}
