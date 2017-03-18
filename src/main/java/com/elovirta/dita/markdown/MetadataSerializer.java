package com.elovirta.dita.markdown;

import org.xml.sax.ContentHandler;

import java.util.Map;

public interface MetadataSerializer {

    void setContentHandler(final ContentHandler contentHandler);

    void write(final Map<String, Object> header);

}
