package com.elovirta.dita.markdown;

import org.junit.Test;

public class MDitaReaderTest extends MarkdownReaderTest {

    @Override
    public MarkdownReader getReader() {
        return new MDitaReader();
    }

    @Override
    public String getPrefix() {
        return "lwdita/";
    }

    @Override
    @Test(expected = ParseException.class)
    public void testHeader() throws Exception {
        run("header.md");
    }
}