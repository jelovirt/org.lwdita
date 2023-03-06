package com.elovirta.dita.markdown;

import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.xml.sax.ContentHandler;

import java.net.URI;

public interface MarkdownParser {
    void convert(BasedSequence sequence, URI input) throws ParseException;

    void setContentHandler(ContentHandler handler);
}
