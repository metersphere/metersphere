package io.metersphere.api.utils.regex.exception;

public class RegexpIllegalException extends Exception {

    public RegexpIllegalException() {
        super();
    }

    public RegexpIllegalException(String message) {
        super(message);
    }

    public RegexpIllegalException(String regexp, int index) {
        super(String.format("Invalid regular expression: %s, Index: %d", regexp, index));
    }
}
