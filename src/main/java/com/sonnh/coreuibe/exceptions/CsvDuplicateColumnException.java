package com.sonnh.coreuibe.exceptions;

public class CsvDuplicateColumnException extends Exception {
    public CsvDuplicateColumnException() {
    }

    public CsvDuplicateColumnException(String message) {
        super(message);
    }

    public CsvDuplicateColumnException(String message, Throwable cause) {
        super(message, cause);
    }

    public CsvDuplicateColumnException(Throwable cause) {
        super(cause);
    }

    public CsvDuplicateColumnException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
