package com.elovirta.dita.markdown;

import com.vladsch.flexmark.util.ast.Node;
import org.dita.dost.util.DitaClass;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.Locator2Impl;
import org.xml.sax.helpers.XMLFilterImpl;

import java.util.ArrayDeque;
import java.util.Deque;

import static javax.xml.XMLConstants.NULL_NS_URI;

/**
 * Write to SAX ContentHandler.
 */
public class SaxWriter extends XMLFilterImpl {
    public final Deque<String> tagStack = new ArrayDeque<>();
    private final Locator2Impl locator = new Locator2Impl();

    public SaxWriter(ContentHandler out) {
        setContentHandler(out);
    }

    public void startDocument() {
        locator.setLineNumber(1);
        locator.setColumnNumber(1);
        getContentHandler().setDocumentLocator(locator);
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        // Ignore
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

    public void startElement(final Node node, final DitaClass tag, final org.xml.sax.Attributes atts) {
        startElement(node, tag.localName, atts);
    }

    public void startElement(final Node node, final String tag, final org.xml.sax.Attributes atts) {
        setLocation(node);
        try {
            getContentHandler().startElement(NULL_NS_URI, tag, tag, atts);
        } catch (final SAXException e) {
            throw new ParseException(e);
        }
        tagStack.addFirst(tag);
    }

    public void endElement() {
        if (!tagStack.isEmpty()) {
            endElement(tagStack.removeFirst());
        }
    }

    public void endElement(final DitaClass tag) {
        endElement(tag.localName);
    }

    public void endElement(final String tag) {
        try {
            getContentHandler().endElement(NULL_NS_URI, tag, tag);
        } catch (final SAXException e) {
            throw new ParseException(e);
        }
    }

    public void characters(final char c) {
        try {
            getContentHandler().characters(new char[]{c}, 0, 1);
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
