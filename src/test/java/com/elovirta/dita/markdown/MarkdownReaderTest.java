package com.elovirta.dita.markdown;

import com.elovirta.dita.utils.AbstractReaderTest;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.XMLReader;

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
    public void testComplex() throws Exception {
        run("test.md");
    }

    @Test
    public void testHeader() throws Exception {
        run("header.md");
    }

//    @Test
//    public void testPandocHeader() throws Exception {
//        run("pandoc_header.md");
//    }

    @Test(expected = RuntimeException.class)
    public void testInvalidHeader() throws Exception {
        run("invalid_header.md");
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidSectionHeader() throws Exception {
        run("invalid_section_header.md");
    }

    @Test
    public void testHeaderAttributes() throws Exception {
        run("header_attributes.md");
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

//    @Test
//    public void testHtml() throws Exception {
//        run("html.md");
//    }

}