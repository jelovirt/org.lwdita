package com.elovirta.dita.markdown;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

public class SpecializeFilterTest {

  private final SAXParserFactory parserFactory = SAXParserFactory.newInstance();
  private final TransformerFactory transformerFactory = TransformerFactory.newInstance();
  private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

  private Transformer transformer;
  private DocumentBuilder documentBuilder;
  private SpecializeFilter filter;

  @BeforeEach
  public void setUp() throws ParserConfigurationException, SAXException, TransformerConfigurationException {
    documentBuilderFactory.setNamespaceAware(true);
    filter = new SpecializeFilter();
    filter.setParent(parserFactory.newSAXParser().getXMLReader());
    transformer = transformerFactory.newTransformer();
    documentBuilder = documentBuilderFactory.newDocumentBuilder();
  }

  @ParameterizedTest
  @ValueSource(
    strings = {
      "concept",
      "reference",
      "task",
      "task_cmd_with_info",
      "task_context",
      "task_context_with_two_p",
      "task_two_p_in_info",
      "task_inline_in_cmd",
    }
  )
  public void test(String name) throws Exception {
    try (
      InputStream srcIn = getClass().getResourceAsStream("/specialize/src/" + name + ".dita");
      InputStream expIn = getClass().getResourceAsStream("/specialize/exp/" + name + ".dita")
    ) {
      run_filter(srcIn, expIn);
    }
  }

  @ParameterizedTest
  @MethodSource("taskArgument")
  public void generatedTask(int context, int info, int inline) throws Exception {
    try (
      InputStream srcIn = generateSrc(context, Stream.of(Arguments.of(info, inline)));
      InputStream expIn = generateExp(context, Stream.of(Arguments.of(info, inline)))
    ) {
      run_filter(srcIn, expIn);
    }
  }

  @Test
  public void generatedTask_multipleSteps() throws Exception {
    try (InputStream srcIn = generateSrc(1, taskArgument()); InputStream expIn = generateExp(1, taskArgument())) {
      run_filter(srcIn, expIn);
    }
  }

  private void run_filter(InputStream srcIn, InputStream expIn) throws Exception {
    final Document act = documentBuilder.newDocument();
    transformer.transform(new SAXSource(filter, new InputSource(srcIn)), new DOMResult(act));

    final Document exp = documentBuilder.parse(new InputSource(expIn));
    final Diff diff = DiffBuilder
      .compare(act)
      .withTest(exp)
      .normalizeWhitespace()
      .ignoreWhitespace()
      .checkForIdentical()
      .build();
    if (diff.hasDifferences()) {
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.transform(new DOMSource(act), new StreamResult(System.out));
    }
    assertFalse(diff.hasDifferences());
  }

  private static Stream<Arguments> taskArgument() {
    final List<Arguments> buf = new ArrayList<>();
    for (int context = 0; context < 3; context++) {
      for (int info = 0; info < 3; info++) {
        for (int inline = 0; inline < 3; inline++) {
          buf.add(Arguments.of(context, info, inline));
        }
      }
    }
    return buf.stream();
  }

  private InputStream generateSrc(int context, Stream<Arguments> args) {
    final StringBuilder buf = new StringBuilder();
    buf.append(
      "<topic xmlns:ditaarch='http://dita.oasis-open.org/architecture/2005/' ditaarch:DITAArchVersion='2.0' specializations='@props/audience @props/deliveryTarget @props/otherprops @props/platform @props/product' class='- topic/topic ' id='task' outputclass='task'>"
    );
    buf.append("<title class='- topic/title '>Task</title>");
    buf.append("<body class='- topic/body '>");
    for (int i = 0; i < context; i++) {
      buf.append("<p class='- topic/p '>Context</p>");
    }
    buf.append("<ol class='- topic/ol '>");
    args.forEach(arg -> generateStepSrc(buf, (int) arg.get()[0], (int) arg.get()[1]));
    buf.append("</ol>");
    buf.append("</body>");
    buf.append("</topic>");

    return new ByteArrayInputStream(buf.toString().getBytes(StandardCharsets.UTF_8));
  }

  private void generateStepSrc(StringBuilder buf, int info, int inline) {
    buf.append("<li class='- topic/li '>");
    buf.append("<p class='- topic/p '>");
    if (inline > 0) {
      if (inline == 1) {
        buf.append("head");
      }
      buf.append("<b class='+ topic/ph hi-d/b '>");
    }
    buf.append("Command");
    if (inline > 0) {
      buf.append("</b>");
      if (inline == 2) {
        buf.append("tail");
      }
    }
    buf.append("</p>");
    for (int i = 0; i < info; i++) {
      buf.append("<p class='- topic/p '>Info.</p>");
    }
    buf.append("</li>");
  }

  private InputStream generateExp(int context, Stream<Arguments> args) {
    final StringBuilder buf = new StringBuilder();
    buf.append(
      "<task xmlns:ditaarch='http://dita.oasis-open.org/architecture/2005/' ditaarch:DITAArchVersion='2.0' specializations='@props/audience @props/deliveryTarget @props/otherprops @props/platform @props/product' class='- topic/topic task/task ' id='task'>"
    );
    buf.append("<title class='- topic/title '>Task</title>");
    buf.append("<taskbody class='- topic/body task/taskbody '>");
    if (context > 0) {
      buf.append("<context class='- topic/section task/context '>");
      for (int i = 0; i < context; i++) {
        buf.append("<p class='- topic/p '>Context</p>");
      }
      buf.append("</context>");
    }
    buf.append("<steps class='- topic/ol task/steps '>");
    args.forEach(arg -> generateStepExp(buf, (int) arg.get()[0], (int) arg.get()[1]));
    buf.append("</steps>");
    buf.append("</taskbody>");
    buf.append("</task>");

    return new ByteArrayInputStream(buf.toString().getBytes(StandardCharsets.UTF_8));
  }

  private void generateStepExp(StringBuilder buf, int info, int inline) {
    buf.append("<step class='- topic/li task/step '>");
    buf.append("<cmd class='- topic/ph task/cmd '>");
    if (inline > 0) {
      if (inline == 1) {
        buf.append("head");
      }
      buf.append("<b class='+ topic/ph hi-d/b '>");
    }
    buf.append("Command");
    if (inline > 0) {
      buf.append("</b>");
      if (inline == 2) {
        buf.append("tail");
      }
    }
    buf.append("</cmd>");
    if (info > 0) {
      buf.append("<info class='- topic/itemgroup task/info '>");
      for (int i = 0; i < info; i++) {
        buf.append("<p class='- topic/p '>Info.</p>");
      }
      buf.append("</info>");
    }
    buf.append("</step>");
  }
}
