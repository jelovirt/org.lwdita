package com.elovirta.dita.markdown;

/**
 * Markdown parsing exception. Extends RuntimeException because underlying
 * parser framework doesn't allow throwing checked exceptions.
 */
public class ParseException extends RuntimeException {

  ParseException() {
    super();
  }

  public ParseException(final String msg) {
    super(msg);
  }

  ParseException(final Throwable cause) {
    super(cause);
  }

  public ParseException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
