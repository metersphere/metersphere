package io.metersphere.api.utils.regex.exception;

public class UninitializedException extends Exception {

    public UninitializedException() {
        super();
    }

    public UninitializedException(String message) {
        super(message);
    }
}