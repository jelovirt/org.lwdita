package com.elovirta.dita.markdown;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.elovirta.dita.markdown.renderer.Metadata;
import org.junit.jupiter.api.Test;

public class ToDitaSerializerTest {

  @Test
  public void testParseMetadata() {
    assertEqualsMetadata(new Metadata("foo", emptyList()), Metadata.parse("#foo"));
    assertEqualsMetadata(new Metadata("foo", emptyList()), Metadata.parse(" #foo "));
    assertEqualsMetadata(new Metadata("bar", emptyList()), Metadata.parse("#foo #bar"));
    assertEqualsMetadata(new Metadata(null, asList("foo")), Metadata.parse(".foo"));
    assertEqualsMetadata(new Metadata(null, asList("foo", "bar")), Metadata.parse(" .foo  .bar "));
    assertEqualsMetadata(new Metadata("baz", asList("foo", "bar")), Metadata.parse(".foo #baz .bar"));
  }

  private void assertEqualsMetadata(final Metadata exp, final Metadata act) {
    assertEquals(exp.id, act.id);
    assertEquals(exp.classes, act.classes);
  }
}
