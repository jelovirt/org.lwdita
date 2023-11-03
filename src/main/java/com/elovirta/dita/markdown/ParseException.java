package com.elovirta.dita.markdown;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Markdown parsing exception. Extends RuntimeException because underlying
 * parser framework doesn't allow throwing checked exceptions.
 */
public class ParseException extends RuntimeException {

  public ParseException(final String msg) {
    super(new SAXException(msg));
  }

  ParseException(final SAXException cause) {
    super(cause);
  }

  public ParseException(String msg, Exception cause) {
    super(new SAXException(msg, cause));
  }
}
