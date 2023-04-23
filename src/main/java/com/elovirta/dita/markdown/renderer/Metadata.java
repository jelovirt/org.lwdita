package com.elovirta.dita.markdown.renderer;

import java.util.*;

public class Metadata {

  public final String id;
  public final List<String> classes;
  private final Map<String, String> attrs;

  public Metadata(final String id, final List<String> classes, final Map<String, String> attrs) {
    this.id = id;
    this.classes = classes;
    this.attrs = attrs;
  }

  public static Metadata parse(final String contents) {
    final String c = contents.trim();
    List<String> classes = null;
    Map<String, String> attrs = null;
    String fragment = null;
    for (final String t : c.split("\\s+")) {
      if (t.startsWith("#")) {
        fragment = t.substring(1);
      } else if (t.startsWith(".")) {
        if (classes == null) {
          classes = new ArrayList<>();
        }
        classes.add(t.substring(1));
      } else if (t.contains("=")) {
        if (attrs == null) {
          attrs = new HashMap<>();
        }
        final String[] tokens = t.split("=", 2);
        attrs.put(tokens[0], tokens[1]);
      }
    }
    final String id = fragment;
    return new Metadata(
      id,
      Objects.requireNonNullElse(classes, Collections.emptyList()),
      Objects.requireNonNullElse(attrs, Collections.emptyMap())
    );
  }
}
