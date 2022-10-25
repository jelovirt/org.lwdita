package com.elovirta.dita.markdown;

import com.elovirta.dita.utils.ClasspathURIResolver;
import com.google.common.annotations.VisibleForTesting;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension;
import com.vladsch.flexmark.ext.anchorlink.AnchorLink;
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
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import com.vladsch.flexmark.util.sequence.BasedSequenceImpl;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.URLUtils;
import org.xml.sax.*;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.CharBuffer;

import static java.util.Arrays.asList;
import static org.apache.commons.io.IOUtils.copy;

/**
 * XMLReader implementation for Markdown.
 */
public class MarkdownReader implements XMLReader {

    final Parser p;
    private SAXTransformerFactory tf;
    private Templates t;
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
        );
        try (InputStream style = getClass().getResourceAsStream("/specialize.xsl")) {
            tf = (SAXTransformerFactory) TransformerFactory.newInstance();
            tf.setURIResolver(new ClasspathURIResolver(tf.getURIResolver()));
            t = tf.newTemplates(new StreamSource(style, "classpath:///specialize.xsl"));
        } catch (final IOException | TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public MarkdownReader(final MutableDataSet options) {
        this.options = options;
        this.p = Parser.builder(options).build();
    }

    @Override
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return false;
    }

    @Override
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        // NOOP
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
        char[] markdownContent = getMarkdownContent(input);
        final BasedSequence sequence = BasedSequenceImpl.of(CharBuffer.wrap(markdownContent));

        try {
            final Document root = p.parse(sequence);
            final Document cleaned = clean(root, input);
            parseAST(cleaned);
        } catch (ParseException e) {
            throw new SAXException("Failed to parse Markdown: " + e.getMessage(), e);
        }
    }

    private Document clean(Document root, InputSource input) {
        final boolean lwDita = DitaRenderer.LW_DITA.getFrom(options);
        if (!lwDita) {
            final boolean isWiki = !(root.getFirstChild() != null
                    && root.getFirstChild() instanceof Heading
                    && ((Heading) root.getFirstChild()).getLevel() == 1);
            if (isWiki) {
                final String title = getTextFromFile(input.getSystemId());
                final Heading heading = new Heading();
//                heading.setAnchorRefId(getIdFromFile(input.getSystemId()));
                heading.setLevel(1);
                final AnchorLink anchorLink = new AnchorLink();
                anchorLink.appendChild(new Text(title));
                heading.appendChild(anchorLink);
                root.prependChild(heading);
            }
        }
        return root;
    }

    private String getTextFromFile(String file) {
        final String path = URI.create(file).getPath();
        final String name = path.substring(path.lastIndexOf("/") + 1);
        final String title = name.lastIndexOf(".") != -1
                ? name.substring(0, name.lastIndexOf("."))
                : name;
        return title.replace('_', ' ').replace('-', ' ');
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
            try (InputStream is = "UTF-8".equalsIgnoreCase(encoding)
                    ? consumeBOM(input.getByteStream())
                    : input.getByteStream();
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
            try (InputStream is = "UTF-8".equalsIgnoreCase(encoding)
                    ? consumeBOM(inUrl.openStream())
                    : inUrl.openStream();
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
    private InputStream consumeBOM(final InputStream in) throws IOException {
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

    private void parseAST(final Document root) throws SAXException {
        ContentHandler res = contentHandler;
        if (t != null) {
            final TransformerHandler h;
            try {
                h = tf.newTransformerHandler(t);
            } catch (final TransformerConfigurationException e) {
                throw new SAXException(e);
            }
            h.setResult(new SAXResult(res));
            res = h;
        }
        final DitaRenderer s = new DitaRenderer(options);
        s.render(root, res);
    }

}
