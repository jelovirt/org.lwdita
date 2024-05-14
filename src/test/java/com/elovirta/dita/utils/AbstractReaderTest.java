package com.elovirta.dita.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.junit.jupiter.api.BeforeEach;
import org.opentest4j.AssertionFailedError;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.*;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.DefaultComparisonFormatter;
import org.xmlunit.diff.Diff;

public abstract class AbstractReaderTest {

  private final TransformerFactory transformerFactory = TransformerFactory.newInstance();
  private final DocumentBuilder db;

  protected XMLReader reader;

  @BeforeEach
  void setUpReader() {
    reader = getReader();
  }

  public abstract XMLReader getReader();

  public String getExp() {
    return "";
  }

  public String getSrc() {
    return "";
  }

  public AbstractReaderTest() {
    try {
      final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      builderFactory.setNamespaceAware(true);
      db = builderFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  public void run(final String input) throws Exception {
    run(getSrc() + input, getExp() + input.replaceAll("\\.(md|html)$", ".dita"));
  }

  protected void run(final String input, final String expFile) throws Exception {
    final Document act;
    try (final InputStream in = getClass().getResourceAsStream("/" + input)) {
      act = db.newDocument();
      final Transformer t = transformerFactory.newTransformer();
      final InputSource i = new InputSource(in);
      i.setSystemId(URI.create("classpath:/" + input).toString());
      t.transform(new SAXSource(reader, i), new DOMResult(act));
    }

    final Document exp;
    //        System.err.println("/" + expFile);
    try (final InputStream in = getClass().getResourceAsStream("/" + expFile)) {
      exp = db.parse(in);
    }

    try {
      final Diff diff = DiffBuilder
        .compare(clean(act))
        .withTest(clean(exp))
        .ignoreWhitespace()
        .ignoreComments()
        .normalizeWhitespace()
        .checkForIdentical()
        .build();
      if (diff.hasDifferences()) {
        System.err.println(diff.fullDescription(new DefaultComparisonFormatter()));
      }
      assertFalse(diff.hasDifferences());
    } catch (AssertionFailedError e) {
      //      transformerFactory.newTransformer().transform(new DOMSource(act), new StreamResult(System.out));
      throw e;
    }
  }

  private Document clean(Document doc) {
    final NodeList elems = doc.getElementsByTagName("*");
    for (int i = 0; i < elems.getLength(); i++) {
      final Element elem = (Element) elems.item(i);
      //            elem.removeAttribute("domains");
      //            elem.removeAttributeNS("http://dita.oasis-open.org/architecture/2005/", "DITAArchVersion");
    }
    doc.normalizeDocument();
    return doc;
  }

  protected static class Event {

    public final String type;
    public final String data;
    public final int line;
    public final int column;

    public Event(String type, int line, int column) {
      this(type, null, line, column);
    }

    public Event(String type, String data, int line, int column) {
      this.type = type;
      this.data = data;
      this.line = line;
      this.column = column;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Event event = (Event) o;

      if (line != event.line) return false;
      if (column != event.column) return false;
      if (!type.equals(event.type)) return false;
      return Objects.equals(data, event.data);
    }

    @Override
    public int hashCode() {
      int result = type.hashCode();
      result = 31 * result + (data != null ? data.hashCode() : 0);
      result = 31 * result + line;
      result = 31 * result + column;
      return result;
    }

    @Override
    public String toString() {
      return (
        "Event{" + "type='" + type + '\'' + ", data='" + data + '\'' + ", line=" + line + ", column=" + column + '}'
      );
    }
  }

  protected void testLocatorParsing(final List<Event> exp, final String file) throws IOException, SAXException {
    reader.setContentHandler(new LocatorContentHandler(new ArrayDeque<>(exp)));
    try (InputStream in = getClass().getClassLoader().getResourceAsStream(getSrc() + file)) {
      InputSource input = new InputSource(in);
      reader.parse(input);
    }
  }

  static class LocatorContentHandler implements ContentHandler {

    private final Deque<Event> exp;
    private Locator locator;

    private LocatorContentHandler(Deque<Event> exp) {
      this.exp = exp;
    }

    private void assertEvent(String name, String data) {
      assertEquals(exp.pop(), new Event(name, data, locator.getLineNumber(), locator.getColumnNumber()));
    }

    @Override
    public void setDocumentLocator(Locator locator) {
      this.locator = locator;
    }

    @Override
    public void startDocument() throws SAXException {
      assertEvent("startDocument", null);
    }

    @Override
    public void endDocument() throws SAXException {
      assertEvent("endDocument", null);
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
      //            assertEvent("startPrefixMapping", prefix);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
      //            assertEvent("endPrefixMapping", prefix);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
      assertEvent("startElement", localName);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
      assertEvent("endElement", localName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      assertEvent("characters", new String(ch, start, length));
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
      assertEvent("ignorableWhitespace", new String(ch, start, length));
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
      assertEvent("processingInstruction", target);
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
      assertEvent("skippedEntity", name);
    }
  }
}
