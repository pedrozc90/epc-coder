package com.pedrozc90.epcs.exception;

public class EPCParseException extends Exception {

    public EPCParseException(final Throwable cause, final String message) {
        super(message, cause);
    }

    public EPCParseException(final Throwable cause, final String fmt, final Object... args) {
        this(cause, fmt.formatted(args));
    }

    public EPCParseException(final String message) {
        this(null, message);
    }

    public EPCParseException(final String fmt, final Object... args) {
        this(fmt.formatted(args));
    }

}
