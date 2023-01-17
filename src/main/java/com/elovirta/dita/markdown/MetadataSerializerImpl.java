package com.elovirta.dita.markdown;

import com.elovirta.dita.markdown.renderer.NodeRendererContext;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterBlock;
import org.dita.dost.util.DitaClass;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.dita.dost.util.Constants.*;

public class MetadataSerializerImpl {

    private final Set<String> knownKeys;

    public MetadataSerializerImpl(Boolean idFromYaml) {
        final ImmutableSet.Builder<String> keys = ImmutableSet.<String>builder()
                .add(TOPIC_AUTHOR.localName, TOPIC_SOURCE.localName,
                        TOPIC_PUBLISHER.localName, TOPIC_PERMISSIONS.localName, TOPIC_AUDIENCE.localName,
                        TOPIC_CATEGORY.localName, TOPIC_RESOURCEID.localName, TOPIC_KEYWORD.localName);
        if (idFromYaml) {
            keys.add(ATTRIBUTE_NAME_ID);
        }
        knownKeys = keys.build();
    }

    public static Attributes buildAtts(final DitaClass cls) {
        return new XMLUtils.AttributesBuilder()
                .add(ATTRIBUTE_NAME_CLASS, cls.toString())
                .build();
    }

    public void render(final YamlFrontMatterBlock node, final NodeRendererContext context, final SaxWriter html) {
        final AbstractYamlFrontMatterVisitor v = new AbstractYamlFrontMatterVisitor();
        v.visit(node);
        final Map<String, List<String>> header = v.getData();

        write(header, TOPIC_AUTHOR, html);
        write(header, TOPIC_SOURCE, html);
        write(header, TOPIC_PUBLISHER, html);
        // copyright
        // critdates
        write(header, TOPIC_PERMISSIONS, "view", html);
        if (header.containsKey(TOPIC_AUDIENCE.localName) || header.containsKey(TOPIC_CATEGORY.localName)
                || header.containsKey(TOPIC_KEYWORD.localName)) {
            html.startElement(node, TOPIC_METADATA, buildAtts(TOPIC_METADATA));
            write(header, TOPIC_AUDIENCE, ATTRIBUTE_NAME_AUDIENCE, html);
            write(header, TOPIC_CATEGORY, html);
            if (header.containsKey(TOPIC_KEYWORD.localName)) {
                html.startElement(node, TOPIC_KEYWORDS, buildAtts(TOPIC_KEYWORDS));
                write(header, TOPIC_KEYWORD, html);
                html.endElement();
            }
            // prodinfo
            // othermeta
            html.endElement();
        }
        write(header, TOPIC_RESOURCEID, "appid", html);
        final List<String> keys = Sets.difference(header.keySet(), knownKeys).stream().sorted().collect(Collectors.toList());
        for (String key : keys) {
            for (String val : header.get(key)) {
                html.startElement(node, TOPIC_DATA.localName, new XMLUtils.AttributesBuilder()
                        .add(ATTRIBUTE_NAME_CLASS, TOPIC_DATA.toString())
                        .add(ATTRIBUTE_NAME_NAME, key)
                        .add(ATTRIBUTE_NAME_VALUE, val)
                        .build());
                html.endElement();
            }
        }
    }

    private void write(final Map<String, List<String>> header, final DitaClass elem, SaxWriter html) {
        if (header.containsKey(elem.localName)) {
            for (String v : header.get(elem.localName)) {
                html.startElement(null, elem, buildAtts(elem));
                if (v != null) {
                    html.characters(v);
                }
                html.endElement();
            }
        }
    }

    private void write(final Map<String, List<String>> header, final DitaClass elem, final String attr, SaxWriter html) {
        if (header.containsKey(elem.localName)) {
            for (String v : header.get(elem.localName)) {
                html.startElement(null, elem, new XMLUtils.AttributesBuilder()
                        .add(ATTRIBUTE_NAME_CLASS, elem.toString())
                        .add(attr, v)
                        .build());
                html.endElement();
            }
        }
    }
}
