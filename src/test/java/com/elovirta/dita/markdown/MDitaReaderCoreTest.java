package com.elovirta.dita.markdown;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.elovirta.dita.utils.AbstractReaderTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;

public class MDitaReaderCoreTest extends AbstractReaderTest {

  private MarkdownReader r = new MarkdownReader(MDitaReader.CORE_PROFILE);

  @Override
  public MarkdownReader getReader() {
    return r;
  }

  @Override
  public String getExp() {
    return "xdita/";
  }

  @Override
  public String getSrc() {
    return "markdown/";
  }

  @ParameterizedTest
  @ValueSource(
    strings = {
      "codeblock_core.md",
      "comment.md",
      "conkeyref.md",
      "conref.md",
      "entity.md",
      "escape.md",
      "html.md",
      "image.md",
      "inline_core.md",
      "keyref.md",
      "keys.md",
      "linebreak.md",
      "link.md",
      "multiple_top_level.md",
      "note.md",
      "ol.md",
      "quote.md",
      "short.md",
      "shortdesc.md",
      "table-width.md",
      "table.md",
      "testBOM.md",
      "testNoBOM.md",
      "ul.md",
      "yaml.md",
    }
  )
  public void test(String file) throws Exception {
    run(file);
  }

  @ParameterizedTest
  @ValueSource(
    strings = {
      "unsupported_html.md",
      "inline_extended.md",
      "jekyll.md",
      "footnote.md",
      "dl.md",
      "abbreviation.md",
      "body_attributes.md",
    }
  )
  public void test_unsupported(String file) {
    assertThrows(AssertionFailedError.class, () -> run(file));
  }

  @ParameterizedTest
  @ValueSource(strings = { "header.md", "invalid_header.md", "invalid_header_third.md" })
  public void test_fail(String file) {
    assertThrows(ParseException.class, () -> run(file));
  }
}
