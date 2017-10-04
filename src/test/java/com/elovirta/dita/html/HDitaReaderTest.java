package com.elovirta.dita.html;

import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.XMLReader;

public class HDitaReaderTest extends HtmlReaderTest {

    @Override
    public XMLReader getReader() {
        return new HDitaReader();
    }

    @Override
    public String getExp() {
        return "lwdita/";
    }

    @Test
    public void test() throws Exception {
        run("test.html");
    }

    @Test
    public void testHdita() throws Exception {
        run("hdita.html");
    }
    @Test
    public void testMultipleTopLevel() throws Exception {
        run("multiple_top_level.html");
    }
    @Ignore
    @Test
    public void testheader_attributes() throws Exception {
        run("header_attributes.html");
    }
    @Ignore()
    @Test
    public void testMultipleTopLevelSpecialized() throws Exception {
        run("multiple_top_level_specialized.html");
    }
    @Ignore
    @Test
    public void testheader() throws Exception {
        run("header.html");
    }
}