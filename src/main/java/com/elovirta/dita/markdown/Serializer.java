package com.elovirta.dita.markdown;

import org.dita.dost.util.DitaClass;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.ArrayDeque;
import java.util.Deque;

import static javax.xml.XMLConstants.NULL_NS_URI;

class Serializer {

    final Deque<DitaClass> tagStack = new ArrayDeque<>();
    ContentHandler contentHandler;

    public void setContentHandler(final ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    // ContentHandler methods

    void startElement(final DitaClass tag, final Attributes atts) {
        try {
            contentHandler.startElement(NULL_NS_URI, tag.localName, tag.localName, atts);
        } catch (final SAXException e) {
            throw new ParseException(e);
        }
        tagStack.addFirst(tag);
    }

    void endElement() {
        if (!tagStack.isEmpty()) {
            endElement(tagStack.removeFirst());
        }
    }


    private void endElement(final DitaClass tag) {
        try {
            contentHandler.endElement(NULL_NS_URI, tag.localName, tag.localName);
        } catch (final SAXException e) {
            throw new ParseException(e);
        }
    }

    void characters(final char c) {
        try {
            contentHandler.characters(new char[]{c}, 0, 1);
        } catch (final SAXException e) {
            throw new ParseException(e);
        }
    }

    void characters(final String t) {
        final char[] cs = t.toCharArray();
        try {
            contentHandler.characters(cs, 0, cs.length);
        } catch (final SAXException e) {
            throw new ParseException(e);
        }
    }
}
