package com.elovirta.dita.markdown.renderer;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class EndElementHandler extends DefaultHandler {

  private final ContentHandler contentHandler;

  EndElementHandler(ContentHandler contentHandler) {
    this.contentHandler = contentHandler;
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    contentHandler.endElement(uri, localName, qName);
  }
}
