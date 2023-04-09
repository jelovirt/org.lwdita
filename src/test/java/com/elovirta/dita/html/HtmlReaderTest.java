package com.elovirta.dita.html;

import com.elovirta.dita.utils.AbstractReaderTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xml.sax.XMLReader;

public class HtmlReaderTest extends AbstractReaderTest {

  private XMLReader r = new HtmlReader();

  @Override
  public XMLReader getReader() {
    return r;
  }

  @Override
  public String getSrc() {
    return "html/";
  }

  @Override
  public String getExp() {
    return "dita/";
  }

  @ParameterizedTest
  @ValueSource(
    strings = {
      "body_attributes.html",
      "codeblock.html",
      "comment.html",
      "concept.html",
      "conkeyref.html",
      "conref.html",
      "dl.html",
      "entity.html",
      "escape.html",
      "hdita.html",
      "header.html",
      "header_attributes.html",
      "html.html",
      "image-size.html",
      "image.html",
      "inline.html",
      "inline_extended.html",
      "keyref.html",
      "keys.html",
      "linebreak.html",
      "link.html",
      "multiple_top_level_specialized.html",
      "ol.html",
      "quote.html",
      "reference.html",
      "short.html",
      "shortdesc.html",
      "table-width.html",
      "table.html",
      "task.html",
      "taskOneStep.html",
      "ul.html",
      //            "multiple_top_level.html",
      //            "pandoc_header.html",
      //            "topic.html",
      //            "yaml.html",
    }
  )
  public void test(String file) throws Exception {
    run(file);
  }
}
