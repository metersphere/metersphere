package io.metersphere.commons.exception;

public class CustomFieldValidateException extends Exception {
    private static final long serialVersionUID = 1L;

    public CustomFieldValidateException(String message) {
        super(message);
    }

    public static void throwException(String message) throws CustomFieldValidateException {
        throw new CustomFieldValidateException(message);
    }
}
