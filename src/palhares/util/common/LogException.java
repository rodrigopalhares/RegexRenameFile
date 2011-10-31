package palhares.util.common;

public class LogException extends Exception {
    public LogException (String message, Exception e) {
	super (message, e);
    }

    public LogException (Exception e) {
	super (e);
    }
}
