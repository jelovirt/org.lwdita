package com.elovirta.dita.markdown;

import com.elovirta.dita.markdown.MarkdownReader;
//import org.dita.dost.TestUtils;
import org.junit.Test;
import org.xml.sax.InputSource;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;

public class MarkdownReaderTest {

//    final File resourceDir = TestUtils.getResourceDir(MarkdownReaderTest.class);
//    private final File srcDir = new File(resourceDir, "src");


    @Test
    public void testParseURI() throws Exception {
        final Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        final MarkdownReader r = new MarkdownReader();
        final InputStream ri = getClass().getResourceAsStream("/test.md");
        try {
            final InputSource i = new InputSource(ri);
            t.transform(new SAXSource(r, i), new StreamResult(System.err));
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
//
//    @Test
//    public void testReferenceLink() throws Exception {
//        final Transformer t = TransformerFactory.newInstance().newTransformer();
//        t.setOutputProperty(OutputKeys.INDENT, "yes");
//        final MarkdownReader r = new MarkdownReader();
//        t.transform(new SAXSource(r, new InputSource(new File(srcDir, "reference.md").toURI().toString())), new StreamResult(System.err));
//    }

}