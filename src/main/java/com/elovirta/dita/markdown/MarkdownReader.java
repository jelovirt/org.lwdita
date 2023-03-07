package com.elovirta.dita.markdown;

import com.google.common.annotations.VisibleForTesting;
import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.attributes.AttributesExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension;
import com.vladsch.flexmark.ext.ins.InsExtension;
import com.vladsch.flexmark.ext.jekyll.tag.JekyllTagExtension;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.xml.sax.*;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static org.apache.commons.io.IOUtils.copy;

/**
 * XMLReader implementation for Markdown.
 */
public class MarkdownReader implements XMLReader {

    private final MutableDataSet options;

    EntityResolver resolver;
    ContentHandler contentHandler;
    ErrorHandler errorHandler;

    /**
     * @see <a href="https://github.com/vsch/flexmark-java/wiki/Extensions">Extensions</a>
     */
    public MarkdownReader() {
        this(new MutableDataSet()
                .set(Parser.EXTENSIONS, asList(
                        AbbreviationExtension.create(),
                        AnchorLinkExtension.create(),
                        AttributesExtension.create(),
                        FootnoteExtension.create(),
                        InsExtension.create(),
                        JekyllTagExtension.create(),
                        SuperscriptExtension.create(),
                        TablesExtension.create(),
                        AutolinkExtension.create(),
                        YamlFrontMatterExtension.create(),
                        DefinitionExtension.create(),
                        StrikethroughSubscriptExtension.create()))
                .set(DefinitionExtension.TILDE_MARKER, false)
                .set(TablesExtension.COLUMN_SPANS, true)
                .set(TablesExtension.APPEND_MISSING_COLUMNS, false)
                .set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
                .set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true)
                .set(DitaRenderer.SPECIALIZATION, true)
        );
    }

    public MarkdownReader(final MutableDataSet options) {
        this.options = options;
    }

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return false;
    }

    /**
     * Set the value of a feature flag.
     *
     * <dl>
     *     <dt><code>http://lwdita.org/sax/features/shortdesc-paragraph</code></dt>
     *     <dd>Treat first paragraph as shortdesc.</dd>
     *     <dt><code>http://lwdita.org/sax/features/id-from-yaml</code></dt>
     *     <dd>Read topic ID from YAML header if available.</dd>
     *     <dt><code>http://lwdita.org/sax/features/mdita</code></dt>
     *     <dd>Parse as MDITA.</dd>
     *     <dt><code>http://lwdita.org/sax/features/specialization</code></dt>
     *     <dd>Support concept, task, and reference specialization from header class.</dd>
     * </dl>
     */
    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        switch (name) {
            case "http://lwdita.org/sax/features/shortdesc-paragraph":
                options.set(DitaRenderer.SHORTDESC_PARAGRAPH, value);
                break;
            case "http://lwdita.org/sax/features/id-from-yaml":
                options.set(DitaRenderer.ID_FROM_YAML, value);
                break;
            case "http://lwdita.org/sax/features/mdita":
                options.set(DitaRenderer.LW_DITA, value);
                break;
            case "http://lwdita.org/sax/features/specialization":
                options.set(DitaRenderer.SPECIALIZATION, value);
                break;
        }
    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return null;
    }

    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        // NOOP
    }

    @Override
    public void setEntityResolver(final EntityResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public EntityResolver getEntityResolver() {
        return resolver;
    }

    @Override
    public void setDTDHandler(final DTDHandler handler) {
        // NOOP
    }

    @Override
    public DTDHandler getDTDHandler() {
        return null;
    }

    @Override
    public void setContentHandler(final ContentHandler handler) {
        this.contentHandler = handler;
    }

    @Override
    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    @Override
    public void setErrorHandler(final ErrorHandler handler) {
        this.errorHandler = handler;
    }

    @Override
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @Override
    public void parse(final InputSource input) throws IOException, SAXException {
        final char[] markdownContent = getMarkdownContent(input);
        final URI schema = getSchema(markdownContent);
        final BasedSequence sequence = BasedSequence.of(CharBuffer.wrap(markdownContent));

        final MarkdownParser markdownParser = getParser(schema);
        markdownParser.setContentHandler(contentHandler);
        markdownParser.convert(sequence, Optional.ofNullable(input.getSystemId()).map(URI::create).orElse(null));
    }

    private static final ServiceLoader<SchemaProvider> schemaLoader = ServiceLoader.load(SchemaProvider.class);

    private MarkdownParser getParser(URI schema) {
        if (schema != null) {
            return schemaLoader.stream()
                    .filter(p -> p.get().isSupportedSchema(schema))
                    .findAny()
                    .map(s -> s.get().createMarkdownParser(schema))
                    .orElse(new BaseMarkdownParser(options));
        } else {
            return new BaseMarkdownParser(options);
        }
    }

    private static final Pattern schemaPattern = Pattern.compile("^---[\r\n]+\\$schema: +(.+?)[\r\n]");

    public URI getSchema(char[] data) {
        final Matcher matcher = schemaPattern.matcher(CharBuffer.wrap(data));
        if (matcher.find()) {
            final String value = matcher.group(1)
                    .replaceAll("^'(.+)'$", "$1")
                    .replaceAll("^\"(.+)\"$", "$1")
                    .trim();
            return URI.create(value);
        }
        return null;
    }

    @Override
    public void parse(final String systemId) throws IOException, SAXException {
        parse(new InputSource(systemId));
    }

    @VisibleForTesting
    char[] getMarkdownContent(final InputSource input) throws IOException {
        final CharArrayWriter out = new CharArrayWriter();
        if (input.getByteStream() != null) {
            final String encoding = input.getEncoding() != null ? input.getEncoding() : "UTF-8";
            try (BufferedInputStream is = "UTF-8".equalsIgnoreCase(encoding)
                    ? consumeBOM(input.getByteStream())
                    : new BufferedInputStream(input.getByteStream());
                 Reader in = new InputStreamReader(is, encoding)) {
                copy(in, out);
            }
        } else if (input.getCharacterStream() != null) {
            try (Reader in = input.getCharacterStream()) {
                copy(in, out);
            }
        } else if (input.getSystemId() != null) {
            final URL inUrl;
            try {
                inUrl = new URI(input.getSystemId()).toURL();
            } catch (final URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
            final String encoding = input.getEncoding() != null ? input.getEncoding() : "UTF-8";
            try (BufferedInputStream is = "UTF-8".equalsIgnoreCase(encoding)
                    ? consumeBOM(inUrl.openStream())
                    : new BufferedInputStream(inUrl.openStream());
                 Reader in = new InputStreamReader(is, encoding)) {
                copy(in, out);
            }
        }
        return out.toCharArray();
    }

    /**
     * Returns an input stream that skips the BOM if present.
     *
     * @param in the original input stream
     * @return An input stream without a possible BOM
     */
    private BufferedInputStream consumeBOM(final InputStream in) throws IOException {
        final BufferedInputStream bin = new BufferedInputStream(in);
        bin.mark(3);
        try {
            if (bin.read() != 0xEF || bin.read() != 0xBB || bin.read() != 0xBF) {
                bin.reset();
            }
        } catch (final IOException e) {
            bin.reset();
        }
        return bin;
    }
}
