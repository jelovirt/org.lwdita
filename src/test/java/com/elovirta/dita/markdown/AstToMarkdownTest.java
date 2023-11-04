package com.elovirta.dita.markdown;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import com.elovirta.dita.utils.ClasspathURIResolver;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.Test;

public class AstToMarkdownTest {

  private byte[] run(final String input) throws Exception {
    final ByteArrayOutputStream o = new ByteArrayOutputStream();
    try (
      InputStream style = getClass().getResourceAsStream("/ast.xsl");
      InputStream ri = getClass().getResourceAsStream("/" + input)
    ) {
      final TransformerFactory tf = TransformerFactory.newInstance();
      tf.setURIResolver(new ClasspathURIResolver(tf.getURIResolver()));
      final Transformer t = tf.newTransformer(new StreamSource(style, "classpath:///ast.xsl"));
      t.transform(new StreamSource(ri), new StreamResult(o));
    }
    return o.toByteArray();
  }

  @Test
  public void testAst() throws Exception {
    final byte[] act = run("ast/ast.xml");
    final byte[] exp = read("markdown/ast.md");
    assertArrayEquals(exp, act);
  }

  private byte[] read(final String input) throws Exception {
    return Files.readAllBytes(Paths.get(getClass().getResource("/" + input).toURI()));
  }
}
