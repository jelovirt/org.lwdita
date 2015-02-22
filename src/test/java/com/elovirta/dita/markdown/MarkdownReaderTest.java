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

    @Test
    public void testComplex() throws Exception {
        final Transformer t = TransformerFactory.newInstance().newTransformer();
        final MarkdownReader r = new MarkdownReader();
        final InputStream ri = getClass().getResourceAsStream("/test.md");
        try {
            final InputSource i = new InputSource(ri);
            t.transform(new SAXSource(r, i), new SAXResult(new DefaultHandler()));
        } finally {
            ri.close();
        }
    }

    @Test
    public void testHeader() throws Exception {
        final Transformer t = TransformerFactory.newInstance().newTransformer();
        final MarkdownReader r = new MarkdownReader();
        final InputStream ri = getClass().getResourceAsStream("/header.md");
        try {
            final InputSource i = new InputSource(ri);
            t.transform(new SAXSource(r, i), new SAXResult(new DefaultHandler()));
        } finally {
            ri.close();
        }
    }

    @Test
    public void testPandocHeader() throws Exception {
        final Transformer t = TransformerFactory.newInstance().newTransformer();
        final MarkdownReader r = new MarkdownReader();
        final InputStream ri = getClass().getResourceAsStream("/pandoc_header.md");
        try {
            final InputSource i = new InputSource(ri);
            t.transform(new SAXSource(r, i), new SAXResult(new DefaultHandler()));
        } finally {
            ri.close();
        }
    }


    @Test(expected=ParseException.class)
    public void testInvalidHeader() throws Exception {
        final Transformer t = TransformerFactory.newInstance().newTransformer();
        final MarkdownReader r = new MarkdownReader();
        final InputStream ri = getClass().getResourceAsStream("/invalid_header.md");
        try {
            final InputSource i = new InputSource(ri);
            t.transform(new SAXSource(r, i), new SAXResult(new DefaultHandler()));
        } finally {
            ri.close();
        }
    }

    @Test(expected=ParseException.class)
    public void testInvalidSectionHeader() throws Exception {
        final Transformer t = TransformerFactory.newInstance().newTransformer();
        final MarkdownReader r = new MarkdownReader();
        final InputStream ri = getClass().getResourceAsStream("/invalid_section_header.md");
        try {
            final InputSource i = new InputSource(ri);
            t.transform(new SAXSource(r, i), new SAXResult(new DefaultHandler()));
        } finally {
            ri.close();
        }
    }

    @Test
    public void testHeaderAttributes() throws Exception {
        final Transformer t = TransformerFactory.newInstance().newTransformer();
        final MarkdownReader r = new MarkdownReader();
        final InputStream ri = getClass().getResourceAsStream("/header_attributes.md");
        try {
            final InputSource i = new InputSource(ri);
            t.transform(new SAXSource(r, i), new SAXResult(new DefaultHandler()));
        } finally {
            ri.close();
        }
    }

//    @Test
//    public void testParseInputSource() throws Exception {
//        final MarkdownReader r = new MarkdownReader();
//        r.setContentHandler(new DefaultHandler());
//        r.parse(new File(srcDir, "test.md").toURI().toString());
//    }

}