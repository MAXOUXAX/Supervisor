package me.maxouxax.supervisor.exceptions;

public class InvalidSupervisedException extends Exception {

    public InvalidSupervisedException(String message) {
        super(message);
    }

    public InvalidSupervisedException(Throwable cause) {
        super(cause);
    }

    public InvalidSupervisedException(String message, Throwable cause) {
        super(message, cause);
    }

}
