package com.elovirta.dita.utils;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

public class FragmentContentHandler extends XMLFilterImpl {

    /**
     * Ignore start document event
     */
    public void startDocument() throws SAXException {
        // ignore
    }

    /**
     * Ignore end document event.
     */
    public void endDocument() throws SAXException {
        // ignore
    }

}
