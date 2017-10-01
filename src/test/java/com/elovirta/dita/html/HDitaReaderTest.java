package com.elovirta.dita.html;

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

}