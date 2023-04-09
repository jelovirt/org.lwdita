package com.elovirta.dita.markdown;

import static javax.xml.XMLConstants.NULL_NS_URI;

import com.vladsch.flexmark.util.ast.Node;
import java.util.ArrayDeque;
import java.util.Deque;
import org.dita.dost.util.DitaClass;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.Locator2Impl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Write to SAX ContentHandler.
 */
public class SaxWriter extends XMLFilterImpl {

  public final Deque<String> tagStack = new ArrayDeque<>();
  private final Locator2Impl locator = new Locator2Impl();

  public SaxWriter(ContentHandler out) {
    setContentHandler(out);
  }

  @Override
  public void startDocument() {
    locator.setLineNumber(1);
    locator.setColumnNumber(1);
    getContentHandler().setDocumentLocator(locator);
  }

  @Override
  public void setDocumentLocator(Locator locator) {
    // Ignore
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    getContentHandler().startElement(uri, localName, qName, atts);
    tagStack.addFirst(localName);
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (tagStack.isEmpty()) {
      throw new IllegalStateException("Empty tag stack");
    }
    final String tag = tagStack.removeFirst();
    if (!tag.equals(localName)) {
      throw new IllegalStateException(String.format("Expected end tag %s but was %s", tag, localName));
    }
    getContentHandler().endElement(uri, localName, qName);
  }

  public void setDocumentLocator() {
    getContentHandler().setDocumentLocator(locator);
  }

  public void setLocation(Node node) {
    if (node != null) {
      locator.setLineNumber(node.getLineNumber() + 1);
      locator.setColumnNumber(node.getStartOffset() - node.getStartOfLine() + 1);
    }
  }

  public void startElement(final Node node, final DitaClass tag, final Attributes atts) {
    startElement(node, tag.localName, atts);
  }

  public void startElement(final Node node, final String tag, final Attributes atts) {
    setLocation(node);
    try {
      startElement(NULL_NS_URI, tag, tag, atts);
    } catch (final SAXException e) {
      throw new ParseException(e);
    }
  }

  public void endElement() {
    final String tag = tagStack.peekFirst();
    endElement(tag);
  }

  public void endElement(final DitaClass cls) {
    endElement(cls.localName);
  }

  public void endElement(final String tag) {
    try {
      endElement(NULL_NS_URI, tag, tag);
    } catch (final SAXException e) {
      throw new ParseException(e);
    }
  }

  public void characters(final char c) {
    try {
      getContentHandler().characters(new char[] { c }, 0, 1);
    } catch (final SAXException e) {
      throw new ParseException(e);
    }
  }

  public void characters(final String t) {
    final char[] cs = t.toCharArray();
    try {
      getContentHandler().characters(cs, 0, cs.length);
    } catch (final SAXException e) {
      throw new ParseException(e);
    }
  }

  public void close() {
    while (!tagStack.isEmpty()) {
      endElement();
    }
  }

  public void processingInstruction(String name, String data) {
    try {
      getContentHandler().processingInstruction(name, data != null ? data : "");
    } catch (final SAXException e) {
      throw new ParseException(e);
    }
  }
}
