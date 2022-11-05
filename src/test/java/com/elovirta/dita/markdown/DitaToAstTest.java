package com.elovirta.dita.markdown;

import com.elovirta.dita.utils.ClasspathURIResolver;
import junit.framework.AssertionFailedError;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;

public class DitaToAstTest {

    @Test
    public void testAst() throws Exception {
        final Document act = run("dita/ast.dita");
        final Document exp = read("ast/dita2ast.xml");
        resetXMLUnit();
        try {
            XMLAssert.assertXMLEqual(clean(exp), clean(act));
        } catch (AssertionFailedError e) {
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(act), new StreamResult(System.out));
            throw e;
        }
    }

    private Document run(final String input) throws Exception {
        final Document output = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        try (InputStream style = getClass().getResourceAsStream("/dita2ast.xsl");
             InputStream ri = getClass().getResourceAsStream("/" + input);) {
            final TransformerFactory tf = TransformerFactory.newInstance();
            tf.setURIResolver(new ClasspathURIResolver(tf.getURIResolver()));
            final Transformer t = tf.newTransformer(new StreamSource(style, "classpath:///dita2ast.xsl"));
            t.transform(new StreamSource(ri), new DOMResult(output));
        }
        return output;
    }

    private Document clean(Document doc) {
        final NodeList elems = doc.getElementsByTagName("*");
        for (int i = 0; i < elems.getLength(); i++) {
            final Element elem = (Element) elems.item(i);
//            elem.removeAttribute("domains");
//            elem.removeAttributeNS("http://dita.oasis-open.org/architecture/2005/", "DITAArchVersion");
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

    private Document read(final String input) throws IOException {
        try (InputStream in = getClass().getResourceAsStream("/" + input)) {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

}
