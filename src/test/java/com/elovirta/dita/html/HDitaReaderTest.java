package com.elovirta.dita.html;

import com.elovirta.dita.utils.AbstractReaderTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xml.sax.XMLReader;

public class HDitaReaderTest extends AbstractReaderTest {

    private XMLReader r = new HDitaReader();

    @Override
    public XMLReader getReader() {
        return r;
    }

    @Override
    public String getSrc() {
        return "hdita/";
    }

    @Override
    public String getExp() {
        return "xdita/";
    }


    @ParameterizedTest
    @ValueSource(strings = {
            "codeblock.html",
            "comment.html",
            "conkeyref.html",
            "conref.html",
            "dl.html",
            "entity.html",
            "escape.html",
            "hdita.html",
            "html.html",
            "image-size.html",
            "image.html",
            "inline.html",
            "keyref.html",
            "keys.html",
            "linebreak.html",
            "link.html",
            "multiple_top_level.html",
            "ol.html",
            "quote.html",
            "short.html",
            "shortdesc.html",
            "table-width.html",
            "table.html",
            "ul.html",
//            "body_attributes.html",
//            "concept.html",
//            "header.html",
//            "header_attributes.html",
//            "multiple_top_level_specialized.html",
//            "pandoc_header.html",
//            "reference.html",
//            "task.html",
//            "taskOneStep.html",
//            "topic.html",
//            "yaml.html",
    })
    public void test(String file) throws Exception {
        run(file);
    }
}