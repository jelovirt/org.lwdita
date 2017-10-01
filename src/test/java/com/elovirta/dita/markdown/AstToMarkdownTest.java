package com.elovirta.dita.markdown;

import org.junit.Test;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URI;

import static org.junit.Assert.assertEquals;

public class AstToMarkdownTest {

    private String run(final String input) throws Exception {
        InputStream ri = null;
        try (InputStream style = getClass().getResourceAsStream("/ast.xsl")) {
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            URIResolver classpathUriResolver = new ClasspathUriResolver(transformerFactory.getURIResolver());
            transformerFactory.setURIResolver(classpathUriResolver);
            final Transformer t = transformerFactory.newTransformer(new StreamSource(style, "/ast.xsl"));
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
        final String act = run("ast/ast.xml");
        final String exp = read("markdown/ast.md");
        assertEquals(exp, act);
    }

    private String read(final String input) throws IOException {
        final StringWriter o = new StringWriter();
        Reader i = null;
        try {
            i = new InputStreamReader(getClass().getResourceAsStream("/" + input));
            char[] buf = new char[1024];
            int len;
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
