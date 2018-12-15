package io.auklet.config;

import io.auklet.Auklet;
import io.auklet.AukletException;
import io.auklet.misc.HasAgent;

import java.io.File;

/**
 * <p>Implementations of this interface represent a configuration file located inside the Auklet agent's
 * configuration directory.</p>
 */
public abstract class AbstractConfigFile extends HasAgent {

    protected File file;

    @Override
    public void setAgent(Auklet agent) throws AukletException {
        super.setAgent(agent);
        this.file = new File(agent.getConfigDir(), this.getName());
    }

    /**
     * <p>Returns the name of the config file.</p>
     *
     * @return never {@code null}.
     */
    protected abstract String getName();

}
