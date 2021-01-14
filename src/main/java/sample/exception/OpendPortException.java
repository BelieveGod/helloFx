package sample.exception;

/**
 * @author LTJ
 * @version 1.0
 * @description 已打开串口异常
 * @date 2020/11/20 14:08
 */
public class OpendPortException extends RuntimeException{
    public OpendPortException() {
    }

    public OpendPortException(String message) {
        super(message);
    }

    public OpendPortException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpendPortException(Throwable cause) {
        super(cause);
    }

    public OpendPortException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
