package com.elovirta.dita.markdown;

import com.elovirta.dita.utils.AbstractReaderTest;
import com.elovirta.dita.utils.TestErrorHandler;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xml.sax.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

public class MarkdownReaderTest extends AbstractReaderTest {

    private XMLReader r = new MarkdownReader();

    @Override
    public XMLReader getReader() {
        return r;
    }

    @Override
    public String getExp() {
        return "dita/";
    }

    @Override
    public String getSrc() {
        return "markdown/";
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "abbreviation.md",
            "admonition.md",
            "body_attributes.md",
            "codeblock.md",
            "comment.md",
            "concept.md",
            "conkeyref.md",
            "conref.md",
            "dl.md",
            "entity.md",
            "escape.md",
            "footnote.md",
            "header.md",
            "header_attributes.md",
            "html.md",
            "image-size.md",
            "image.md",
            "inline.md",
            "jekyll.md",
            "keyref.md",
            "keys.md",
            "linebreak.md",
            "link.md",
            "missing_root_header.md",
            "missing_root_header_with_yaml.md",
            "multiple_top_level.md",
            "multiple_top_level_specialized.md",
            "ol.md",
            "quote.md",
            "reference.md",
            "short.md",
            "shortdesc.md",
            "table-width.md",
            "table.md",
            "task.md",
            "taskOneStep.md",
            "testBOM.md",
            "testNoBOM.md",
            "ul.md",
            "unsupported_html.md",
            "yaml.md",
//            "pandoc_header.md",
            "schema/concept.md",
            "schema/example.md",
            "schema/reference.md",
            "schema/task.md",
            "schema/topic.md",
    })
    public void test(String file) throws Exception {
        run(file);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "missing_root_header.md",
            "missing_root_header_with_yaml.md",
    })
    public void test_missingHeader(String file) throws Exception {
        reader = new MarkdownReader(new MutableDataSet()
                .set(Parser.EXTENSIONS, singletonList(YamlFrontMatterExtension.create()))
                .set(DitaRenderer.FIX_ROOT_HEADING, true));
        final TestErrorHandler errorHandler = new TestErrorHandler();
        reader.setErrorHandler(errorHandler);

        run(file);

        assertEquals(1, errorHandler.warnings.size());
        final SAXParseException e = errorHandler.warnings.get(0);
        assertEquals("Document content doesn't start with heading", e.getMessage());
        assertEquals("classpath:/markdown/" + file, e.getSystemId());
        assertEquals(null, e.getPublicId());
        assertEquals(1, e.getLineNumber());
        assertEquals(1, e.getColumnNumber());
    }

    @Test
    public void test_schemaParseFailure() throws Exception {
        final XMLReader reader = getReader();
        final TestErrorHandler errorHandler = new TestErrorHandler();
        reader.setErrorHandler(errorHandler);

        try (final InputStream in = getClass().getResourceAsStream("/markdown/schema/unrecognized.md")) {
            final InputSource input = new InputSource(in);
            input.setSystemId("classpath:///schema/unrecognized.md");
            reader.parse(input);
        }

        assertEquals(1, errorHandler.errors.size());
        final SAXParseException act = errorHandler.errors.get(0);
        assertEquals(null, act.getPublicId());
        assertEquals("classpath:///schema/unrecognized.md", act.getSystemId());
        assertEquals(2, act.getLineNumber());
        assertEquals(56, act.getColumnNumber());
    }

    @Test
    public void test_schemaParseFailure_withoutErrorHandler() throws Exception {
        final XMLReader reader = getReader();

        try (final InputStream in = getClass().getResourceAsStream("/" + getSrc() + "schema/unrecognized.md")) {
            final InputSource input = new InputSource(in);
            input.setSystemId("classpath:///schema/unrecognized.md");
            reader.parse(input);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "invalid_header.md",
    })
    public void test_fail(String file) {
        assertThrows(RuntimeException.class, () -> run(file));
    }

    @Test
    public void getMarkdownContent_url() throws Exception {
        final String input = getSrc() + "testBOM.md";
        final URL in = getClass().getResource("/" + input);
        final InputSource i = new InputSource(in.toString());
        final char[] content = new MarkdownReader().getMarkdownContent(i);
        assertEquals('W', content[0]);
    }

    @Test
    public void testLocator() throws IOException, SAXException {
        testLocatorParsing(
                Arrays.asList(
                        new Event("startDocument", 1, 1),
                        new Event("startElement", "topic", 1, 1),
                        new Event("startElement", "title", 1, 1),
                        new Event("characters", "Shortdesc", 1, 1),
                        new Event("endElement", "title", 1, 1),
                        new Event("startElement", "body", 1, 1),
                        new Event("startElement", "p", 3, 1),
                        new Event("characters", "Shortdesc.", 3, 1),
                        new Event("endElement", "p", 3, 1),
                        new Event("startElement", "p", 5, 1),
                        new Event("characters", "Paragraph.", 5, 1),
                        new Event("endElement", "p", 5, 1),
                        new Event("endElement", "body", 5, 1),
                        new Event("endElement", "topic", 5, 1),
                        new Event("endDocument", 5, 1)
                ),
                "shortdesc.md");
    }

    @Test
    public void taskOneStep() throws IOException, SAXException {
        testLocatorParsing(
                Arrays.asList(
                        new Event("startDocument", 1, 1),
                        new Event("startElement", "task", 1, 1),
                        new Event("startElement", "title", 1, 1),
                        new Event("characters", "Task", 1, 1),
                        new Event("endElement", "title", 1, 1),
                        new Event("startElement", "taskbody", 1, 1),
                        new Event("startElement", "context", 3, 1),
                        new Event("startElement", "p", 3, 1),
                        new Event("characters", "Context", 3, 1),
                        new Event("endElement", "p", 3, 1),
                        new Event("endElement", "context", 5, 1),
                        new Event("startElement", "steps", 5, 1),
                        new Event("startElement", "step", 5, 1),
                        new Event("startElement", "cmd", 5, 5),
                        new Event("characters", "Command", 5, 5),
                        new Event("endElement", "cmd", 5, 5),
                        new Event("startElement", "info", 7, 5),
                        new Event("startElement", "p", 7, 5),
                        new Event("characters", "Info.", 7, 5),
                        new Event("endElement", "p", 7, 5),
                        new Event("endElement", "info", 7, 5),
                        new Event("endElement", "step", 7, 5),
                        new Event("endElement", "steps", 7, 5),
                        new Event("endElement", "taskbody", 7, 5),
                        new Event("endElement", "task", 7, 5),
                        new Event("endDocument", 7, 5)
                ),
                "taskOneStep.md");
    }

    @Test
    public void testHtmlLocator() throws IOException, SAXException {
        testLocatorParsing(
                Arrays.asList(
                        new Event("startDocument", 1, 1),
                        new Event("startElement", "topic", 1, 1),
                        new Event("startElement", "title", 1, 1),
                        new Event("characters", "HTML Block", 1, 1),
                        new Event("endElement", "title", 1, 1),
                        new Event("startElement", "body", 1, 1),

                        new Event("startElement", "p", 3, 1),
                        new Event("characters", "Plain ", 3, 1),
                        new Event("startElement", "b", 3, 7),
                        new Event("startElement", "i", 3, 10),
                        new Event("characters", "paragraph", 3, 10),
                        new Event("endElement", "i", 3, 22),
                        new Event("endElement", "b", 3, 26),
                        new Event("characters", ".", 3, 26),
                        new Event("endElement", "p", 3, 26),

                        new Event("startElement", "p", 5, 1),
                        new Event("characters", "HTML paragraph.", 5, 1),
                        new Event("endElement", "p", 5, 1),

                        new Event("startElement", "p", 7, 1),
                        new Event("startElement", "video", 7, 1),
                        new Event("endElement", "video", 7, 54),
                        new Event("endElement", "p", 7, 54),

                        new Event("endElement", "body", 7, 54),
                        new Event("endElement", "topic", 7, 54),
                        new Event("endDocument", 7, 54)
                ),
                "html.md");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "---\n$schema: urn:test\n",
            "---\r\n$schema: urn:test\r\n",
            "---\n$schema:  urn:test  \n",
            "---\n$schema: \"urn:test\"\n",
            "---\n$schema: 'urn:test'\n",
    })
    public void getSchema(String data) throws SAXParseException {
        final MarkdownReader markdownReader = new MarkdownReader();
        final InputSource input = new InputSource("file:/foo/bar.md");

        final Map.Entry<URI, Locator> act = markdownReader.getSchema(data.toCharArray(), input);

        assertEquals(URI.create("urn:test"), act.getKey());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "---\n$schema: urn: test\n",
            "---\r\n$schema: urn: test\r\n",
    })
    public void getSchema_failure(String data) {
        final InputSource input = new InputSource("file:/foo/bar.md");

        try {
            ((MarkdownReader) reader).getSchema(data.toCharArray(), input);
            fail();
        } catch (SAXParseException e) {
            assertEquals(null, e.getPublicId());
            assertEquals("file:/foo/bar.md", e.getSystemId());
            assertEquals(2, e.getLineNumber());
            assertEquals(19, e.getColumnNumber());
        }
    }
}