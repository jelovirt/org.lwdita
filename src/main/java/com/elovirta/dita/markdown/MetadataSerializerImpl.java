package com.elovirta.dita.markdown;

import com.elovirta.dita.markdown.renderer.NodeRenderer;
import com.elovirta.dita.markdown.renderer.NodeRendererContext;
import com.elovirta.dita.markdown.renderer.NodeRenderingHandler;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterBlock;
import org.dita.dost.util.DitaClass;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;

import java.util.*;
import java.util.stream.Collectors;

import static org.dita.dost.util.Constants.*;

public class MetadataSerializerImpl implements NodeRenderer {

    private static final Set<String> KNOWN = ImmutableSet.of(TOPIC_AUTHOR.localName, TOPIC_SOURCE.localName,
            TOPIC_PUBLISHER.localName, TOPIC_PERMISSIONS.localName, TOPIC_AUDIENCE.localName, TOPIC_CATEGORY.localName,
            TOPIC_RESOURCEID.localName, TOPIC_KEYWORD.localName);


    @Override
    public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        return new HashSet<>(Arrays.asList(
                new NodeRenderingHandler<YamlFrontMatterBlock>(YamlFrontMatterBlock.class, (node, context, html) -> render(node, context, html))

        ));
    }

    public static Attributes buildAtts(final DitaClass cls) {
        return new XMLUtils.AttributesBuilder()
                .add(ATTRIBUTE_NAME_CLASS, cls.toString())
                .build();
    }

    public void render(final YamlFrontMatterBlock node, final NodeRendererContext context, final DitaWriter html) {
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
            html.startElement(TOPIC_METADATA, buildAtts(TOPIC_METADATA));
            write(header, TOPIC_AUDIENCE, html);
            write(header, TOPIC_CATEGORY, html);
            if (header.containsKey(TOPIC_KEYWORD.localName)) {
                html.startElement(TOPIC_KEYWORDS, buildAtts(TOPIC_KEYWORDS));
                write(header, TOPIC_KEYWORD, html);
                html.endElement();
            }
            // prodinfo
            // othermeta
            html.endElement();
        }
        write(header, TOPIC_RESOURCEID, "appid", html);
        final List<String> keys = Sets.difference(header.keySet(), KNOWN).stream().sorted().collect(Collectors.toList());
        for (String key : keys) {
            for (String val : header.get(key)) {
                html.startElement(TOPIC_DATA.localName, new XMLUtils.AttributesBuilder()
                        .add(ATTRIBUTE_NAME_CLASS, TOPIC_DATA.toString())
                        .add(ATTRIBUTE_NAME_NAME, key)
                        .add(ATTRIBUTE_NAME_VALUE, val)
                        .build());
                html.endElement();
            }
        }
    }

    private void write(final Map<String, List<String>> header, final DitaClass elem, DitaWriter html) {
        if (header.containsKey(elem.localName)) {
            for (String v : header.get(elem.localName)) {
                html.startElement(elem, buildAtts(elem));
                if (v != null) {
                    html.characters(v.toString());
                }
                html.endElement();
            }
        }
    }

    private void write(final Map<String, List<String>> header, final DitaClass elem, final String attr, DitaWriter html) {
        if (header.containsKey(elem.localName)) {
            for (String v : header.get(elem.localName)) {
                html.startElement(elem, new XMLUtils.AttributesBuilder()
                        .add(ATTRIBUTE_NAME_CLASS, elem.toString())
                        .add(attr, v.toString())
                        .build());
                html.endElement();
            }
        }
    }
}
