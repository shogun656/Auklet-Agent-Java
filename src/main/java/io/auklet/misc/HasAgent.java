package io.auklet.misc;

import io.auklet.Auklet;
import io.auklet.AukletException;

/**
 * <p>Descendants of this class have a reference to the {@link Auklet} agent.</p>
 */
public abstract class HasAgent {

    private final Object lock = new Object();
    private Auklet agent = null;

    /**
     * <p>Sets the Auklet agent reference.</p>
     *
     * @param agent the Auklet agent object.
     * @throws AukletException if the agent reference has already been set.
     */
    public void setAgent(Auklet agent) throws AukletException {
        synchronized(this.lock) {
            if (this.agent != null) throw new AukletException("Auklet agent already set");
            this.agent = agent;
        }
    }

    /**
     * <p>Returns the Auklet agent reference.</p>
     *
     * @return never {@code null}.
     * @throws AukletException if the agent reference has not been set.
     */
    protected final Auklet getAgent() throws AukletException {
        synchronized(this.lock) {
            if (this.agent == null) throw new AukletException("Auklet agent not set");
            return this.agent;
        }
    }

}
