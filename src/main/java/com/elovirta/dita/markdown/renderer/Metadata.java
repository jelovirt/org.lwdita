package com.elovirta.dita.markdown.renderer;

import java.util.ArrayList;
import java.util.List;

public class Metadata {
    public final String id;
    public final List<String> classes;

    public Metadata(final String id, final List<String> classes) {
        this.id = id;
        this.classes = classes;
    }

    public static Metadata parse(final String contents) {
        final String c = contents.trim();
        final List<String> classes = new ArrayList<>();
        String fragment = null;
        for (final String t : c.split("\\s+")) {
            if (t.startsWith("#")) {
                fragment = t.substring(1);
            } else if (t.startsWith(".")) {
                classes.add(t.substring(1));
            }
        }
        final String id = fragment != null ? fragment : null;
        return new Metadata(id, classes);
    }
}
