package com.elovirta.dita.markdown;

import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.xml.sax.ContentHandler;

import java.net.URI;

/**
 * Interface for Markdown parser that produces SAX events for a DITA document.
 */
public interface MarkdownParser {
    /**
     * Parse a Markdown document.
     *
     * @param sequence Markdown document content.
     * @param input    Markdown document URI.
     * @throws ParseException if parsing failed
     */
    void convert(BasedSequence sequence, URI input) throws ParseException;

    /**
     * Set the content event handler.
     *
     * @param handler the new content handler
     */
    void setContentHandler(ContentHandler handler);
}
