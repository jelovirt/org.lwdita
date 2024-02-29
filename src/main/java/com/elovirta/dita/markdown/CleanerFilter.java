package com.elovirta.dita.markdown;

import static com.elovirta.dita.markdown.renderer.TopicRenderer.TIGHT_LIST_P;

import java.util.ArrayDeque;
import java.util.Deque;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

public class CleanerFilter extends XMLFilterImpl {

  private final Deque<Boolean> retainElementStack = new ArrayDeque<>();

  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    boolean retain = !localName.equals(TIGHT_LIST_P);
    retainElementStack.push(retain);
    if (retain) {
      getContentHandler().startElement(uri, localName, qName, atts);
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (retainElementStack.pop()) {
      getContentHandler().endElement(uri, localName, qName);
    }
  }
}
