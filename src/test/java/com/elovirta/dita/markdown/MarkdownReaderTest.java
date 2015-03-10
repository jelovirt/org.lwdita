package com.elovirta.dita.markdown;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;

public class MarkdownReaderTest {

    private void run(final String input) throws Exception {
        final Transformer t = TransformerFactory.newInstance().newTransformer();
        final MarkdownReader r = new MarkdownReader();
        final InputStream ri = getClass().getResourceAsStream("/" + input);
        try {
            final InputSource i = new InputSource(ri);
            t.transform(new SAXSource(r, i), new SAXResult(new DefaultHandler()));
        } finally {
            ri.close();
        }
    }

    @Test
    public void testComplex() throws Exception {
        run("test.md");
    }

    @Test
    public void testHeader() throws Exception {
        run("header.md");
    }

    @Test
    public void testPandocHeader() throws Exception {
        run("pandoc_header.md");
    }

    @Test(expected=ParseException.class)
    public void testInvalidHeader() throws Exception {
        run("invalid_header.md");
    }

    @Test(expected=ParseException.class)
    public void testInvalidSectionHeader() throws Exception {
        run("invalid_section_header.md");
    }

    @Test
    public void testHeaderAttributes() throws Exception {
        run("header_attributes.md");
    }

    @Test
    public void testConcept() throws Exception {
        run("concept.md");
    }

    @Test
    public void testTask() throws Exception {
        run("task.md");
    }

    @Test
    public void testReference() throws Exception {
        run("reference.md");
    }

}