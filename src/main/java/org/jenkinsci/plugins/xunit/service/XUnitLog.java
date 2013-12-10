package org.jenkinsci.plugins.xunit.service;

import com.google.inject.Inject;
import hudson.model.BuildListener;

import java.io.Serializable;

public class XUnitLog implements Serializable {

    private BuildListener buildListener;

    @Inject
    @SuppressWarnings("unused")
    void set(BuildListener buildListener) {
        this.buildListener = buildListener;
    }

    /**
     * Log an info output to the console logger
     *
     * @param message The message to be outputted
     */
    public void infoConsoleLogger(String message) {
        buildListener.getLogger().println("[xUnit] [INFO] - " + message);
    }

    /**
     * Log an error output to the console logger
     *
     * @param message The message to be outputted
     */
    public void errorConsoleLogger(String message) {
        buildListener.getLogger().println("[xUnit] [ERROR] - " + message);
    }

    /**
     * Log a warning output to the console logger
     *
     * @param message The message to be outputted
     */
    public void warningConsoleLogger(String message) {
        buildListener.getLogger().println("[xUnit] [WARNING] - " + message);
    }

}
