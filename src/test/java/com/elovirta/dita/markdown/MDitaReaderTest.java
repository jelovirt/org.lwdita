package com.elovirta.dita.markdown;

import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

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
        public final int line;
        public final int column;

        private Event(String type, int line, int column) {
            this.type = type;
            this.line = line;
            this.column = column;
        }
    }

    @Test
    public void testLocator() throws IOException, SAXException {
        final Deque<Event> exp = new ArrayDeque<>(Arrays.asList(
                new Event("startDocument", 0, 0),
                new Event("startPrefixMapping", 0, 0),

                //# Shortdesc 1:11
                new Event("startElement", 1, 0),
                new Event("startElement", 1, 0),
                new Event("characters", 1, 0),
                new Event("endElement", 1, 0),
                //Shortdesc. 3:10
                new Event("startElement", 3, 13),
                new Event("characters", 3, 13),
                new Event("endElement", 3, 13),
                new Event("startElement", 1, 0),
                //Paragraph. 5:14
                new Event("startElement", 5, 25),
                new Event("characters", 5, 25),
                new Event("endElement", 5, 25),
                new Event("endElement", 5, 25),
                new Event("endElement", 5, 25),

                new Event("endPrefixMapping", 5, 25),
                new Event("endDocument", 5, 25)
        ));
        final XMLReader r = getReader();
        r.setContentHandler(new ContentHandler() {
            Locator locator;

            private void assertEvent(String name) {
                final Event event = exp.pop();
                assertEquals(event.type, name);
                assertEquals(event.line, locator.getLineNumber());
                assertEquals(event.column, locator.getColumnNumber());
            }

            @Override
            public void setDocumentLocator(Locator locator) {
                this.locator = locator;
            }

            @Override
            public void startDocument() throws SAXException {
                assertEvent("startDocument");
            }

            @Override
            public void endDocument() throws SAXException {
                assertEvent("endDocument");
            }

            @Override
            public void startPrefixMapping(String prefix, String uri) throws SAXException {
                assertEvent("startPrefixMapping");
            }

            @Override
            public void endPrefixMapping(String prefix) throws SAXException {
                assertEvent("endPrefixMapping");
            }

            @Override
            public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
                assertEvent("startElement");
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                assertEvent("endElement");
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                assertEvent("characters");
            }

            @Override
            public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
                assertEvent("ignorableWhitespace");
            }

            @Override
            public void processingInstruction(String target, String data) throws SAXException {
                assertEvent("processingInstruction");
            }

            @Override
            public void skippedEntity(String name) throws SAXException {
                assertEvent("skippedEntity");
            }
        });
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(getSrc() + "shortdesc.md")) {
            InputSource input = new InputSource(in);
            r.parse(input);
        }
    }
}
