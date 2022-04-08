package com.elovirta.dita.utils;

import junit.framework.AssertionFailedError;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;

public abstract class AbstractReaderTest {

    private final DocumentBuilder db;

    public abstract XMLReader getReader();

    public String getExp() {
        return "";
    }

    public String getSrc() {
        return "";
    }

    public AbstractReaderTest() {
        try {
            final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            db = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public void run(final String input) throws Exception {
        run(getSrc() + input, getExp() + input.replaceAll("\\.(md|html)$", ".dita"));
    }

    void run(final String input, final String expFile) throws Exception {
        final Document act;
        try (final InputStream in = getClass().getResourceAsStream("/" + input)) {
            act = db.newDocument();
            final Transformer t = TransformerFactory.newInstance().newTransformer();
            final XMLReader r = getReader();
            final InputSource i = new InputSource(in);
            t.transform(new SAXSource(r, i), new DOMResult(act));
        }

        final Document exp;
//        System.err.println("/" + expFile);
        try (final InputStream in = getClass().getResourceAsStream("/" + expFile)) {
            exp = db.parse(in);
        }

        resetXMLUnit();
        try {
            XMLAssert.assertXMLEqual(clean(exp), clean(act));
        } catch (AssertionFailedError e) {
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(act), new StreamResult(System.out));
            throw e;
        }
    }

    private Document clean(Document doc) {
        final NodeList elems = doc.getElementsByTagName("*");
        for (int i = 0; i < elems.getLength(); i++) {
            final Element elem = (Element) elems.item(i);
            elem.removeAttribute("domains");
            elem.removeAttributeNS("http://dita.oasis-open.org/architecture/2005/", "DITAArchVersion");
        }
        doc.normalizeDocument();
        return doc;
    }

    void resetXMLUnit() {
        XMLUnit.setControlEntityResolver(null);
        XMLUnit.setTestEntityResolver(null);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalizeWhitespace(true);
        XMLUnit.setNormalize(true);
        XMLUnit.setIgnoreComments(true);
    }

}