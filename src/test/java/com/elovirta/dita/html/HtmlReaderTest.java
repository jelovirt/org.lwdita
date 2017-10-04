package com.elovirta.dita.html;

import com.elovirta.dita.utils.AbstractReaderTest;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.XMLReader;

public class HtmlReaderTest extends AbstractReaderTest {

    @Override
    public XMLReader getReader() {
        return new HtmlReader();
    }

    @Override
    public String getSrc() {
        return "html/";
    }

    @Override
    public String getExp() {
        return "dita/";
    }

    @Ignore("non-LwDITA assumes nested topics which are not supported")
    @Test
    public void test() throws Exception {
        run("test.html");
    }

    @Test
    public void testHdita() throws Exception {
        run("hdita.html");
    }

    @Test
    public void testInline() throws Exception {
        run("inline.html");
    }

    @Test
    public void testLinebreak() throws Exception {
        run("linebreak.html");
    }

    @Test
    public void testCodeblock() throws Exception {
        run("codeblock.html");
    }

    @Test
    public void testDl() throws Exception {
        run("dl.html");
    }

    @Test
    public void testcomment() throws Exception {
        run("comment.html");
    }
    @Test
    public void testconcept() throws Exception {
        run("concept.html");
    }
    @Test
    public void testentity() throws Exception {
        run("entity.html");
    }
    @Test
    public void testescape() throws Exception {
        run("escape.html");
    }
    @Test
    public void testheader() throws Exception {
        run("header.html");
    }
    @Test
    public void testheader_attributes() throws Exception {
        run("header_attributes.html");
    }
    @Test
    public void testhtml() throws Exception {
        run("html.html");
    }
    @Test
    public void testimage() throws Exception {
        run("image.html");
    }
    @Test
    public void testkeys() throws Exception {
        run("keys.html");
    }
    @Test
    public void testlink() throws Exception {
        run("link.html");
    }
    @Ignore
    @Test
    public void testMultipleTopLevel() throws Exception {
        run("multiple_top_level.html");
    }
    @Test
    public void testMultipleTopLevelSpecialized() throws Exception {
        run("multiple_top_level_specialized.html");
    }
    @Test
    public void testol() throws Exception {
        run("ol.html");
    }
//    @Test
//    public void testpandoc_header() throws Exception {
//        run("pandoc_header.html");
//    }
    @Test
    public void testquote() throws Exception {
        run("quote.html");
    }
    @Test
    public void testreference() throws Exception {
        run("reference.html");
    }
    @Test
    public void testshort() throws Exception {
        run("short.html");
    }
    @Test
    public void testshortdesc() throws Exception {
        run("shortdesc.html");
    }
    @Test
    public void testtable() throws Exception {
        run("table.html");
    }
    @Test
    public void testTask() throws Exception {
        run("task.html");
    }
    @Test
    public void testTaskOneStep() throws Exception {
        run("taskOneStep.html");
    }
//    @Test
//    public void testtopic() throws Exception {
//        run("topic.html");
//    }
    @Test
    public void testul() throws Exception {
        run("ul.html");
    }
    @Ignore
    @Test
    public void testyaml() throws Exception {
        run("yaml.html");
    }

}