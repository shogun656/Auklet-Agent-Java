package io.auklet.config;

import io.auklet.Auklet;

import java.io.File;

/**
 * <p>Implementations of this interface represent a configuration file located inside the Auklet agent's
 * configuration directory.</p>
 *
 * <p>Type {@code T} represents the data type that is used by the rest of the Auklet agent code.</p>
 */
public abstract class AbstractConfigFile<T> {

    protected final Auklet agent;
    protected final File file;

    /**
     * <p>Constructor.</p>
     *
     * @param agent the Auklet agent object.
     */
    protected AbstractConfigFile(Auklet agent) {
        this.agent = agent;
        this.file = new File(this.agent.getConfigDir(), this.getName());
    }

    /**
     * <p>Returns the name of the config file.</p>
     *
     * @return never {@code null}.
     */
    protected abstract String getName();

}
