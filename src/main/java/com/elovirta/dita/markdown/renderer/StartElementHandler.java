package com.elovirta.dita.markdown.renderer;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class StartElementHandler extends DefaultHandler {

  private final ContentHandler contentHandler;

  StartElementHandler(ContentHandler contentHandler) {
    this.contentHandler = contentHandler;
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    contentHandler.startElement(uri, localName, qName, atts);
  }
}
