package com.kemall.datasync.exception;

public class DataException extends RuntimeException {

    public final static String DDL_DO_NOT_SYNC = "ddl语句不需要同步";

    public DataException(String message) {
        super(message);
    }
}
