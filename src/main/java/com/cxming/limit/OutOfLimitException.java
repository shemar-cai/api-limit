package com.cxming.limit;

/**
 * @author caixiaoming
 * @create 2021-03-21 23:53
 */
public class OutOfLimitException extends RuntimeException{

    public OutOfLimitException() {
        super();
    }

    public OutOfLimitException(String message) {
        super(message);
    }

    public OutOfLimitException(String message,Throwable cause) {
        super(message, cause);
    }

}
