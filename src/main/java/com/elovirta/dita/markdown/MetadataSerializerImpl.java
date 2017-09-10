package com.elovirta.dita.markdown;

import org.dita.dost.util.DitaClass;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.dita.dost.util.Constants.*;

public class MetadataSerializerImpl extends SaxSerializer implements MetadataSerializer {

    public static Attributes buildAtts(final DitaClass cls) {
        return new XMLUtils.AttributesBuilder()
                .add(ATTRIBUTE_NAME_CLASS, cls.toString())
                .build();
    }

    @Override
    public void write(final Map<String, Object> header) {
        write(header, TOPIC_AUTHOR);
        write(header, TOPIC_SOURCE);
        write(header, TOPIC_PUBLISHER);
        // copyright
        // critdates
        write(header, TOPIC_PERMISSIONS, "view");
        if (header.containsKey(TOPIC_AUDIENCE.localName) || header.containsKey(TOPIC_CATEGORY.localName)
                || header.containsKey(TOPIC_KEYWORD.localName)) {
            startElement(TOPIC_METADATA, buildAtts(TOPIC_METADATA));
            write(header, TOPIC_AUDIENCE);
            write(header, TOPIC_CATEGORY);
            if (header.containsKey(TOPIC_KEYWORD.localName)) {
                startElement(TOPIC_KEYWORDS, buildAtts(TOPIC_KEYWORDS));
                write(header, TOPIC_KEYWORD);
                endElement();
            }
            // prodinfo
            // othermeta
            endElement();
        }
        write(header, TOPIC_RESOURCEID, "appid");
    }

    private void write(final Map<String, Object> header, final DitaClass elem) {
        if (header.containsKey(elem.localName)) {
            Object value = header.get(elem.localName);
            final List<Object> values = value instanceof List ? (List) value : Collections.singletonList(value);
            for (Object v : values) {
                startElement(elem, buildAtts(elem));
                if (value != null) {
                    characters(v.toString());
                }
                endElement();
            }
        }
    }

    private void write(final Map<String, Object> header, final DitaClass elem, final String attr) {
        if (header.containsKey(elem.localName)) {
            final Object value = header.get(elem.localName);
            final List<Object> values = value instanceof List ? (List) value : Collections.singletonList(value);
            for (Object v : values) {
                startElement(elem, new XMLUtils.AttributesBuilder()
                        .add(ATTRIBUTE_NAME_CLASS, elem.toString())
                        .add(attr, v.toString())
                        .build());
                endElement();
            }
        }
    }
}
