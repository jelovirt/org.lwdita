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

}