package com.elovirta.dita.html;

import com.elovirta.dita.markdown.AbstractReaderTest;
import org.junit.Test;
import org.xml.sax.XMLReader;

public class HtmlReaderTest extends AbstractReaderTest {

    @Override
    public XMLReader getReader() {
        return new HtmlReader();
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