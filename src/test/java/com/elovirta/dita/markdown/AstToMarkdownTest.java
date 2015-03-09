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
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URI;

import static org.junit.Assert.assertEquals;

public class AstToMarkdownTest {

    private String run(final String input) throws Exception {
        InputStream ri = null;
        try {
            final URI style = getClass().getResource("/ast.xsl").toURI();
            System.err.println(style.toString());
            final Transformer t = TransformerFactory.newInstance().newTransformer(new StreamSource(style.toString()));
            ri = getClass().getResourceAsStream("/" + input);
            final StringWriter o = new StringWriter();
            t.transform(new StreamSource(ri), new StreamResult(o));
            return o.toString();
        } finally {
            if (ri != null) {
                ri.close();
            }
        }
    }

    @Test
    public void testAst() throws Exception {
        final String act = run("ast.xml");
        final String exp = read("ast.md");
        assertEquals(exp, act);
    }

    private String read(final String input) throws IOException {
        final StringWriter o = new StringWriter();
        Reader i = null;
        try {
            i = new InputStreamReader(getClass().getResourceAsStream("/" + input));
            char[] buf = new char[1024];
            int len ;
            while ((len = i.read(buf)) != -1) {
                o.write(buf, 0, len);
            }
        } finally {
            if (i != null) {
                i.close();
            }
        }
        return o.toString();
    }

}
