package com.elovirta.dita.markdown;

import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;

import static junit.framework.TestCase.assertEquals;

public class MDitaReaderTest extends MarkdownReaderTest {

    @Override
    public MarkdownReader getReader() {
        return new MDitaReader();
    }

    @Override
    public String getExp() {
        return "xdita/";
    }

    @Override
    @Test(expected = ParseException.class)
    public void testHeader() throws Exception {
        run("header.md");
    }

    @Ignore
    @Test
    public void testImageSize() throws Exception {
//        run("image-size.md");
    }

    @Override
    @Ignore
    @Test
    public void testGitHubWiki() throws Exception {
//        run("missing_root_header.md");
    }

    @Override
    @Ignore
    @Test
    public void testGitHubWikiWithYaml() throws Exception {
//        run("missing_root_header_with_yaml.md");
    }

    private static class Event {
        public final String type;
        public final String data;
        public final int line;
        public final int column;

        private Event(String type, int line, int column) {
            this(type, null, line, column);
        }
        private Event(String type, String data, int line, int column) {
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
            return "Event{" +
                    "type='" + type + '\'' +
                    ", data='" + data + '\'' +
                    ", line=" + line +
                    ", column=" + column +
                    '}';
        }
    }

    @Test
    public void testLocator() throws IOException, SAXException {
        testLocatorParsing(
                Arrays.asList(
                        new Event("startDocument", 1, 1),
//                        new Event("startPrefixMapping", "ditaarch", 0, 0),

                        //# Shortdesc 1:11
                        new Event("startElement", "topic", 1, 1),
                        new Event("startElement", "title", 1, 1),
                        new Event("characters", "Shortdesc", 1, 1),
                        new Event("endElement", "title",1, 1),
                        //Shortdesc. 3:10
                        new Event("startElement", "shortdesc", 3, 1),
                        new Event("characters", "Shortdesc.",3, 1),
                        new Event("endElement", "shortdesc", 3, 1),
                        new Event("startElement", "body", 1, 1),
                        // Paragraph. 5:10
                        new Event("startElement","p", 5, 1),
                        new Event("characters", "Paragraph.", 5, 1),
                        new Event("endElement", "p",5, 1),
                        new Event("endElement", "body", 5, 1),
                        new Event("endElement", "topic", 5, 1),

//                        new Event("endPrefixMapping", "ditaarch", 5, 10),
                        new Event("endDocument", 5, 1)
                ),
                "shortdesc.md");
    }

    @Test
    public void taskOneStep() throws IOException, SAXException {
        testLocatorParsing(
                Arrays.asList(
                        new Event("startDocument", 1, 1),
//                        new Event("startPrefixMapping","ditaarch", 0, 0),

                        //# Task {.task} 1:14
                        new Event("startElement","topic", 1, 1),
                        new Event("startElement", "title", 1, 1),
                        new Event("characters", "Task {.task}",1, 1),
                        new Event("endElement", "title",1, 1),
                        // Context 3:8
                        new Event("startElement", "shortdesc", 3, 1),
                        new Event("characters", "Context",3, 1),
                        new Event("endElement", "shortdesc",3, 1),
                        new Event("startElement",  "body",1, 1),
                        // 1.  Command 5:11
                        new Event("startElement","ol", 5, 1),
                        new Event("startElement", "li",5, 1),
                        new Event("startElement", "p",5, 5),
                        new Event("characters", "Command",5, 5),
                        new Event("endElement", "p",5, 5),
                        //     Info. 7:10
                        new Event("startElement", "p",7, 5),
                        new Event("characters", "Info.",7, 5),
                        new Event("endElement", "p",7, 5),
                        new Event("endElement", "li",7, 5),
                        new Event("endElement", "ol", 7, 5),
                        new Event("endElement", "body",7, 5),
                        new Event("endElement", "topic",7, 5),

//                        new Event("endPrefixMapping","ditaarch", 7, 10),
                        new Event("endDocument", 7, 5)
                ),
                "taskOneStep.md");
    }

    private void testLocatorParsing(final List<Event> exp, final String file) throws IOException, SAXException {
        final XMLReader r = getReader();
        r.setContentHandler(new LocatorContentHandler(new ArrayDeque<>(exp)));
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(getSrc() + file)) {
            InputSource input = new InputSource(in);
            r.parse(input);
        }
    }

    private static class LocatorContentHandler implements ContentHandler {
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


    @Test
    public void test() throws ParserConfigurationException, SAXException, IOException {
        final XMLReader parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        parser.setContentHandler(new DefaultHandler() {
            private Locator locator;

            @Override
            public void setDocumentLocator(Locator locator) {
                this.locator = locator;
            }

            @Override
            public void startDocument() throws SAXException {
                System.out.printf("start document %d:%d%n", locator.getLineNumber(), locator.getColumnNumber());
            }

            @Override
            public void endDocument() throws SAXException {
                System.out.printf("end document %d:%d%n", locator.getLineNumber(), locator.getColumnNumber());
            }

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                System.out.printf("start %s %d:%d%n", qName, locator.getLineNumber(), locator.getColumnNumber());
            }
        });
        Reader input = new StringReader("<topic>\n  <title>Title</title>\n  <shortdesc>Desc</shortdesc>\n</topic>\n");
        parser.parse(new InputSource(input));
    }

}
