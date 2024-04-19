package com.elovirta.dita.markdown;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.elovirta.dita.utils.ClasspathURIResolver;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

public class DitaToAstTest {

  private final TransformerFactory transformerFactory;

  public DitaToAstTest() {
    transformerFactory = TransformerFactory.newInstance();
    transformerFactory.setURIResolver(new ClasspathURIResolver(transformerFactory.getURIResolver()));
  }

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
      "html_unsupported",
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
      "yaml",
    }
  )
  public void testAst(String name) throws Exception {
    final Document act = run("dita/" + name + ".dita");
    final Document exp = read("output/ast/" + name + ".xml");
    try {
      final Diff diff = DiffBuilder
        .compare(clean(act))
        .withTest(clean(exp))
        .normalizeWhitespace()
        .ignoreWhitespace()
        .ignoreComments()
        .checkForIdentical()
        .build();
      assertFalse(diff.hasDifferences());
    } catch (AssertionFailedError e) {
      transformerFactory.newTransformer().transform(new DOMSource(exp), new StreamResult(System.out));
      System.out.println();
      transformerFactory.newTransformer().transform(new DOMSource(act), new StreamResult(System.out));
      throw e;
    }
  }

  private Document run(final String input) throws Exception {
    final Document output = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    try (
      InputStream style = getClass().getResourceAsStream("/dita2ast.xsl");
      InputStream ri = getClass().getResourceAsStream("/" + input)
    ) {
      final Transformer t = transformerFactory.newTransformer(new StreamSource(style, "classpath:///dita2ast.xsl"));
      t.transform(new StreamSource(ri), new DOMResult(output));
    }
    return output;
  }

  private Document clean(Document doc) {
    final List<Node> roots = XMLUtils.toList(doc.getChildNodes());
    for (Node node : roots) {
      if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
        doc.removeChild(node);
      }
    }
    //    final NodeList elems = doc.getElementsByTagName("*");
    //    for (int i = 0; i < elems.getLength(); i++) {
    //      final Element elem = (Element) elems.item(i);
    //            elem.removeAttribute("domains");
    //            elem.removeAttributeNS("http://dita.oasis-open.org/architecture/2005/", "DITAArchVersion");
    //    }
    doc.normalizeDocument();
    return doc;
  }

  private Document read(final String input) throws IOException {
    try (InputStream in = getClass().getResourceAsStream("/" + input)) {
      return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
    } catch (ParserConfigurationException | SAXException e) {
      throw new RuntimeException(e);
    }
  }
}
