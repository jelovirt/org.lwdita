package com.elovirta.dita.markdown;

/**
 * Markdown parsing exception. Extends RuntimeException because underlying
 * parser framework doesn't allow throwing checked exceptions.
 */
class ParseException extends RuntimeException {

    ParseException() {
        super();
    }

    ParseException(final String msg) {
        super(msg);
    }

    ParseException(final Throwable cause) {
        super(cause);
    }

}
