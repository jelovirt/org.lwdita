package com.elovirta.dita.markdown;

import com.elovirta.dita.utils.AbstractReaderTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class MDitamapReaderTest extends AbstractReaderTest {

  private MarkdownReader r = new MDitamapReader();

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
    return "mdita/";
  }

  @ParameterizedTest
  @ValueSource(
    strings = {
      "map/map.md",
      "map/map_ol.md",
      "map/map_title.md",
      "map/map_topichead.md",
      "map/map_without_title.md",
      "map/map_yaml.md",
    }
  )
  public void test_map(String file) throws Exception {
    run(file);
  }
}
