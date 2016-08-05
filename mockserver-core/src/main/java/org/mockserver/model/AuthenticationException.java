package org.mockserver.model;

public class AuthenticationException extends Exception {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(Throwable t) {
        super(t);
    }

    public AuthenticationException(String message, Throwable t) {
        super(message, t);
    }
}
