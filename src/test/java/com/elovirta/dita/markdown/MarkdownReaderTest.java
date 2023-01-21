package com.elovirta.dita.markdown;

import com.elovirta.dita.utils.AbstractReaderTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MarkdownReaderTest extends AbstractReaderTest {

    @Override
    public XMLReader getReader() {
        return new MarkdownReader();
    }

    @Override
    public String getExp() {
        return "dita/";
    }

    @Override
    public String getSrc() {
        return "markdown/";
    }

    @Test
    public void testHeader() throws Exception {
        run("header.md");
    }

    @Disabled
    @Test
    public void testPandocHeader() throws Exception {
        run("pandoc_header.md");
    }

    @Test
    public void testInvalidHeader() {
        assertThrows(RuntimeException.class, () -> run("invalid_header.md"));
    }

    @Test
    public void testInvalidSectionHeader() {
        assertThrows(RuntimeException.class, () -> run("invalid_section_header.md"));
    }

    @Test
    public void testGitHubWiki() throws Exception {
        run("missing_root_header.md");
    }

    @Test
    public void testGitHubWikiWithYaml() throws Exception {
        run("missing_root_header_with_yaml.md");
    }

    @Test
    public void testHeaderAttributes() throws Exception {
        run("header_attributes.md");
    }

    @Test
    public void testBodyAttributes() throws Exception {
        run("body_attributes.md");
    }

    @Test
    public void testConcept() throws Exception {
        run("concept.md");
    }

    @Test
    public void testTask() throws Exception {
        run("task.md");
    }

    @Test
    public void testTaskOneStep() throws Exception {
        run("taskOneStep.md");
    }

    @Test
    public void testReference() throws Exception {
        run("reference.md");
    }

    @Test
    public void testImage() throws Exception {
        run("image.md");
    }

    @Test
    public void testImageSize() throws Exception {
        run("image-size.md");
    }

    @Test
    public void testLink() throws Exception {
        run("link.md");
    }

    @Test
    public void testUl() throws Exception {
        run("ul.md");
    }

    @Test
    public void testOl() throws Exception {
        run("ol.md");
    }

    @Test
    public void testInline() throws Exception {
        run("inline.md");
    }

    @Test
    public void testTable() throws Exception {
        run("table.md");
    }

    @Test
    public void testTableWidth() throws Exception {
        run("table-width.md");
    }

    @Test
    public void testQuote() throws Exception {
        run("quote.md");
    }

    @Test
    public void testEscape() throws Exception {
        run("escape.md");
    }

    @Test
    public void testDl() throws Exception {
        run("dl.md");
    }

    @Test
    public void testCodeblock() throws Exception {
        run("codeblock.md");
    }

    @Test
    public void testMultipleTopLevel() throws Exception {
        run("multiple_top_level.md");
    }

    @Test
    public void testMultipleTopLevelSpecialized() throws Exception {
        run("multiple_top_level_specialized.md");
    }

    @Test
    public void testNoBOM() throws Exception {
        run("testNoBOM.md");
    }

    @Test
    public void testBOM() throws Exception {
        run("testBOM.md");
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
    public void testShort() throws Exception {
        run("short.md");
    }

    @Test
    public void testShortdesc() throws Exception {
        run("shortdesc.md");
    }

    @Test
    public void testLinebreak() throws Exception {
        run("linebreak.md");
    }

    @Test
    public void testYaml() throws Exception {
        run("yaml.md");
    }

    @Test
    public void testKeys() throws Exception {
        run("keys.md");
    }

    @Test
    public void testEntity() throws Exception {
        run("entity.md");
    }

    @Test
    public void testComment() throws Exception {
        run("comment.md");
    }

    @Test
    public void testHtml() throws Exception {
        run("html.md");
    }

    @Test
    public void testUnsupportedHtml() throws Exception {
        run("unsupported_html.md");
    }

    @Test
    public void testConref() throws Exception {
        run("conref.md");
    }

    @Test
    public void testKeyref() throws Exception {
        run("keyref.md");
    }

    @Test
    public void testConkeyref() throws Exception {
        run("conkeyref.md");
    }

    @Test
    public void testFootnote() throws Exception {
        run("footnote.md");
    }

    @Test
    public void testJekyll() throws Exception {
        run("jekyll.md");
    }

    @Test
    public void testAbbreviation() throws Exception {
        run("abbreviation.md");
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
}