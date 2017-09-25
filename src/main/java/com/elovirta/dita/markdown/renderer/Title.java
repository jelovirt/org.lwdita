package com.elovirta.dita.markdown.renderer;

import com.vladsch.flexmark.ast.Heading;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.elovirta.dita.markdown.renderer.HeaderIdGenerator.generateId;

class Title {
    final String title;
    final String id;
    final Collection<String> classes;

    Title(final Heading node) {
        final String contents = node.getText().toString();
        classes = new ArrayList<>();
        final Pattern p = Pattern.compile("^(.+?)(?:\\{(.*?)\\})?$");
        final Matcher m = p.matcher(contents);
        if (m.matches()) {
            title = m.group(1);
            if (m.group(2) != null) {
                final Metadata metadata = Metadata.parse(m.group(2));
                classes.addAll(metadata.classes);
                id = metadata.id != null ? metadata.id : generateId(title.replaceAll("\\s+", " ").trim(), " -_", false);
            } else {
//                    id = getId(title);
                id = null;
            }
        } else {
            title = contents;
//                id = getId(contents);
            id = null;
        }
    }
}
