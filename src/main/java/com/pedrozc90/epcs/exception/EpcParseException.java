package com.pedrozc90.epcs.exception;

public class EpcParseException extends Exception {

    public EpcParseException(final Throwable cause, final String message) {
        super(message, cause);
    }

    public EpcParseException(final Throwable cause, final String fmt, final Object... args) {
        this(cause, fmt.formatted(args));
    }

    public EpcParseException(final String message) {
        this(null, message);
    }

    public EpcParseException(final String fmt, final Object... args) {
        this(fmt.formatted(args));
    }

}
