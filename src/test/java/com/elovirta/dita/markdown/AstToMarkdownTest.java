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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
    Files.write(
      Paths.get(
        "/Users/jelovirt/Work/github.com/jelovirt/org.lwdita/src/test/resources/out/" +
        input.substring(4, input.indexOf(".")) +
        ".md"
      ),
      o.toByteArray()
    );
    return o.toByteArray();
  }

  @ParameterizedTest
  @ValueSource(
    strings = {
      "ast",
      "codeblock",
      "dita2abbreviation",
      "dita2admonition",
      "dita2ast",
      "dita2body_attributes",
      "dita2codeblock",
      "dita2comment",
      "dita2concept",
      "dita2conkeyref",
      "dita2conref",
      "dita2dl",
      "dita2entity",
      "dita2escape",
      "dita2footnote",
      "dita2hdita",
      "dita2header",
      "dita2header_attributes",
      "dita2html",
      "dita2image-size",
      "dita2image",
      "dita2inline",
      "dita2inline_extended",
      "dita2jekyll",
      "dita2keyref",
      "dita2keys",
      "dita2linebreak",
      "dita2link",
      "dita2missing_root_header",
      "dita2missing_root_header_with_yaml",
      "dita2multiple_top_level",
      "dita2multiple_top_level_specialized",
      "dita2note",
      "dita2ol",
      "dita2pandoc_header",
      "dita2quote",
      "dita2reference",
      "dita2short",
      "dita2shortdesc",
      "dita2table-width",
      "dita2table",
      "dita2task",
      "dita2taskOneStep",
      "dita2taskTight",
      "dita2testBOM",
      "dita2testNoBOM",
      "dita2topic",
      "dita2ul",
      "dita2unsupported_html",
      "dita2yaml",
      "dl",
      "escape",
      "header",
      "header_attributes",
      "image",
      "inline",
      "link",
      "ol",
      "pandoc_header",
      "quote",
      "table",
      "ul",
    }
  )
  public void testAst(String name) throws Exception {
    final byte[] act = run("ast/" + name + ".xml");
    final byte[] exp = read("out/" + name + ".md");
    assertArrayEquals(exp, act);
  }

  private byte[] read(final String input) throws Exception {
    return Files.readAllBytes(Paths.get(getClass().getResource("/" + input).toURI()));
  }
}
