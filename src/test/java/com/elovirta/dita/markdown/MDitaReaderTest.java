package com.elovirta.dita.markdown;

import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Arrays;

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

    @Test
    @Override
    public void testLocator() throws IOException, SAXException {
        testLocatorParsing(
                Arrays.asList(
                        new Event("startDocument", 1, 1),
                        new Event("startElement", "topic", 1, 1),
                        new Event("startElement", "title", 1, 1),
                        new Event("characters", "Shortdesc", 1, 1),
                        new Event("endElement", "title",1, 1),
                        new Event("startElement", "shortdesc", 3, 1),
                        new Event("characters", "Shortdesc.",3, 1),
                        new Event("endElement", "shortdesc", 3, 1),
                        new Event("startElement", "body", 1, 1),
                        new Event("startElement","p", 5, 1),
                        new Event("characters", "Paragraph.", 5, 1),
                        new Event("endElement", "p",5, 1),
                        new Event("endElement", "body", 5, 1),
                        new Event("endElement", "topic", 5, 1),
                        new Event("endDocument", 5, 1)
                ),
                "shortdesc.md");
    }

    @Test
    @Override
    public void taskOneStep() throws IOException, SAXException {
        testLocatorParsing(
                Arrays.asList(
                        new Event("startDocument", 1, 1),
                        new Event("startElement","topic", 1, 1),
                        new Event("startElement", "title", 1, 1),
                        new Event("characters", "Task {.task}",1, 1),
                        new Event("endElement", "title",1, 1),
                        new Event("startElement", "shortdesc", 3, 1),
                        new Event("characters", "Context",3, 1),
                        new Event("endElement", "shortdesc",3, 1),
                        new Event("startElement",  "body",1, 1),
                        new Event("startElement","ol", 5, 1),
                        new Event("startElement", "li",5, 1),
                        new Event("startElement", "p",5, 5),
                        new Event("characters", "Command",5, 5),
                        new Event("endElement", "p",5, 5),
                        new Event("startElement", "p",7, 5),
                        new Event("characters", "Info.",7, 5),
                        new Event("endElement", "p",7, 5),
                        new Event("endElement", "li",7, 5),
                        new Event("endElement", "ol", 7, 5),
                        new Event("endElement", "body",7, 5),
                        new Event("endElement", "topic",7, 5),
                        new Event("endDocument", 7, 5)
                ),
                "taskOneStep.md");
    }
}
