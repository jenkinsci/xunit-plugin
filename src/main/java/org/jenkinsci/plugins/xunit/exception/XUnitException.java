package org.jenkinsci.plugins.xunit.exception;

public class XUnitException extends RuntimeException {

    public XUnitException(Throwable throwable) {
        super(throwable);
    }

    public XUnitException(String message) {
        super(message);
    }

    public XUnitException(String message, Throwable cause) {
        super(message, cause);
    }
}
