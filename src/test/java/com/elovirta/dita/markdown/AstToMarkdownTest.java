package com.elovirta.dita.markdown;

import com.elovirta.dita.utils.ClasspathURIResolver;
import org.junit.Test;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

import static org.junit.Assert.assertEquals;

public class AstToMarkdownTest {

    private String run(final String input) throws Exception {
        final StringWriter o = new StringWriter();
        try (InputStream style = getClass().getResourceAsStream("/ast.xsl");
             InputStream ri = getClass().getResourceAsStream("/" + input);) {
            final TransformerFactory tf = TransformerFactory.newInstance();
            tf.setURIResolver(new ClasspathURIResolver(tf.getURIResolver()));
            final Transformer t = tf.newTransformer(new StreamSource(style, "classpath:///ast.xsl"));
            t.transform(new StreamSource(ri), new StreamResult(o));
        }
        return o.toString();
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
