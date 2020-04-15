package io.metersphere.exception;

/**
 * @author jianxing.chen
 */
public class ExcelImportException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ExcelImportException(String message, Exception e){
        super(message, e);
    }

    public ExcelImportException(String message){
        super(message);
    }

}
