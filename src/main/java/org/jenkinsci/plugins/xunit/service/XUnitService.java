package org.jenkinsci.plugins.xunit.service;

import java.io.Serializable;
import java.util.logging.Logger;

public class XUnitService implements Serializable {

    private static final Logger LOGGER = Logger.getLogger("XUnitService");

    /**
     * Log an info output to the system logger
     *
     * @param message The message to be outputted
     */
    protected void infoSystemLogger(String message) {
        LOGGER.info("[xUnit] - " + message);
    }

    /**
     * Log an error output to the system logger
     *
     * @param message The message to be outputted
     */
    protected void errorSystemLogger(String message) {
        LOGGER.severe("[xUnit] - " + message);
    }

    /**
     * Log a warning output to the system logger
     *
     * @param message The message to be outputted
     */
    public void warningSystemLogger(String message) {
        LOGGER.warning("[xUnit] - " + message);
    }
}
