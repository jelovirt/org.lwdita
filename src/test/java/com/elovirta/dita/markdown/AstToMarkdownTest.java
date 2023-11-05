package com.elovirta.dita.markdown;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.elovirta.dita.utils.ClasspathURIResolver;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class AstToMarkdownTest {

  @ParameterizedTest
  @ValueSource(
    strings = {
      "abbreviation",
      "admonition",
      "ast",
      "body_attributes",
      "codeblock",
      "comment",
      "concept",
      "conkeyref",
      "conref",
      "dl",
      "entity",
      "escape",
      "footnote",
      "hdita",
      "header",
      "header_attributes",
      "html",
      "image-size",
      "image",
      "inline",
      "inline_extended",
      "jekyll",
      "keyref",
      "keys",
      "linebreak",
      "link",
      "missing_root_header",
      "missing_root_header_with_yaml",
      "multiple_top_level",
      "multiple_top_level_specialized",
      "note",
      "ol",
      "pandoc_header",
      "quote",
      "reference",
      "short",
      "shortdesc",
      "table-block",
      "table-width",
      "table",
      "task",
      "taskOneStep",
      "taskTight",
      "testBOM",
      "testNoBOM",
      "topic",
      "ul",
      "unsupported_html",
      "yaml",
    }
  )
  public void testAst(String name) throws Exception {
    final byte[] act = run("output/ast/" + name + ".xml");
    final byte[] exp = read("output/markdown/" + name + ".md");
    assertEquals(new String(exp, StandardCharsets.UTF_8), new String(act, StandardCharsets.UTF_8));
  }

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

  private byte[] read(final String input) throws Exception {
    return Files.readAllBytes(Paths.get(getClass().getResource("/" + input).toURI()));
  }
}
