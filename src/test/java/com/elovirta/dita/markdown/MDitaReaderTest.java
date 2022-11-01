package com.elovirta.dita.markdown;

import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

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
}
