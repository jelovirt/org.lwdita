package com.elovirta.dita.markdown;

import com.elovirta.dita.utils.AbstractReaderTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MDitaReaderTest extends AbstractReaderTest {

    private MarkdownReader r = new MDitaReader();

    @Override
    public MarkdownReader getReader() {
        return r;
    }

    @Override
    public String getExp() {
        return "xdita/";
    }

    @Override
    public String getSrc() {
        return "markdown/";
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "abbreviation.md",
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
            "header_attributes.md",
            "invalid_section_header.md",
            "html.md",
            "image.md",
            "inline.md",
            "jekyll.md",
            "keyref.md",
            "keys.md",
            "linebreak.md",
            "link.md",
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
//            "image-size.md",
//            "missing_root_header.md",
//            "missing_root_header_with_yaml.md",
//            "pandoc_header.md",
    })
    public void test(String file) throws Exception {
        run(file);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "header.md",
            "invalid_header.md",
            "invalid_header_third.md",
    })
    public void test_fail(String file) {
        assertThrows(ParseException.class, () -> run(file));
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
                        new Event("startElement", "shortdesc", 3, 1),
                        new Event("characters", "Shortdesc.", 3, 1),
                        new Event("endElement", "shortdesc", 3, 1),
                        new Event("startElement", "body", 1, 1),
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
                        new Event("startElement", "topic", 1, 1),
                        new Event("startElement", "title", 1, 1),
                        new Event("characters", "Task {.task}", 1, 1),
                        new Event("endElement", "title", 1, 1),
                        new Event("startElement", "shortdesc", 3, 1),
                        new Event("characters", "Context", 3, 1),
                        new Event("endElement", "shortdesc", 3, 1),
                        new Event("startElement", "body", 1, 1),
                        new Event("startElement", "ol", 5, 1),
                        new Event("startElement", "li", 5, 1),
                        new Event("startElement", "p", 5, 5),
                        new Event("characters", "Command", 5, 5),
                        new Event("endElement", "p", 5, 5),
                        new Event("startElement", "p", 7, 5),
                        new Event("characters", "Info.", 7, 5),
                        new Event("endElement", "p", 7, 5),
                        new Event("endElement", "li", 7, 5),
                        new Event("endElement", "ol", 7, 5),
                        new Event("endElement", "body", 7, 5),
                        new Event("endElement", "topic", 7, 5),
                        new Event("endDocument", 7, 5)
                ),
                "taskOneStep.md");
    }
}
