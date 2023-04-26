package com.elovirta.dita.markdown.renderer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class MetadataTest {

  @Test
  void test_classes_single() {
    final Metadata act = Metadata.parse(".class");
    assertEquals(null, act.id);
    assertEquals(List.of("class"), act.classes);
  }

  @Test
  void test_classes_multiple() {
    final Metadata act = Metadata.parse(".one .two .one");
    assertEquals(null, act.id);
    assertEquals(List.of("one", "two", "one"), act.classes);
  }

  @Test
  void test_id() {
    final Metadata act = Metadata.parse("#id");
    assertEquals("id", act.id);
    assertEquals(Collections.emptyList(), act.classes);
  }

  @Test
  void test_id_multiple() {
    final Metadata act = Metadata.parse("#one #two");
    assertEquals("two", act.id);
    assertEquals(Collections.emptyList(), act.classes);
  }

  @Test
  void test_mixed() {
    final Metadata act = Metadata.parse(".class #id");
    assertEquals("id", act.id);
    assertEquals(List.of("class"), act.classes);
  }
}
