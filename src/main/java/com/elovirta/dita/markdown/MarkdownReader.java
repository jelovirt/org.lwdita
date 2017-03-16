package com.elovirta.dita.markdown;

import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.RootNode;
import org.xml.sax.*;
import org.yaml.snakeyaml.Yaml;

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
import java.util.Iterator;
import java.util.Map;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;

/**
 * XMLReader implementation for Markdown.
 */
public class MarkdownReader implements XMLReader {

    private final PegDownProcessor p;
    private final SAXTransformerFactory tf;
    private final Templates t;

    private EntityResolver resolver;
    private ContentHandler contentHandler;
    private ErrorHandler errorHandler;

    public MarkdownReader() {
        p = new PegDownProcessor(Extensions.ALL - Extensions.SMARTYPANTS);
        try {
            final URI style = getClass().getResource("/specialize.xsl").toURI();
            tf = (SAXTransformerFactory) TransformerFactory.newInstance();
            t = tf.newTemplates(new StreamSource(style.toString()));
        } catch (final URISyntaxException | TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
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
        final Map<String, Object> header = parserYaml(markdownContent);
        if (header != null) {
            markdownContent = consumeYaml(markdownContent);
        }
        final RootNode root = p.parseMarkdown(markdownContent);
        parseAST(root, header);
    }

    @Override
    public void parse(final String systemId) throws IOException, SAXException {
        parse(new InputSource(systemId));
    }

    private char[] getMarkdownContent(final InputSource input) throws IOException {
        final CharArrayWriter out = new CharArrayWriter();
        if (input.getByteStream() != null) {
            final String encoding = input.getEncoding() != null ? input.getEncoding() : "UTF-8";
            final InputStream is = "UTF-8".equalsIgnoreCase(encoding)
                    ? consumeBOM(input.getByteStream())
                    : input.getByteStream();
            final Reader in = new InputStreamReader(is, encoding);
            try {
                copy(in, out);
            } finally {
                closeQuietly(in);
                //closeQuietly(out);
            }
        } else if (input.getCharacterStream() != null) {
            final Reader in = input.getCharacterStream();
            try {
                copy(in, out);
            } finally {
                closeQuietly(in);
                //closeQuietly(out);
            }
        } else if (input.getSystemId() != null) {
            final URL inUrl;
            try {
                inUrl = new URI(input.getSystemId()).toURL();
            } catch (final URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
            final String encoding = input.getEncoding() != null ? input.getEncoding() : "UTF-8";
            final Reader in = new InputStreamReader(inUrl.openStream(), encoding);
            try {
                copy(in, out);
            } finally {
                closeQuietly(in);
                //closeQuietly(out);
            }
        }
        return out.toCharArray();
    }

    /**
     * Return char array after YAML document.
     */
    private char[] consumeYaml(char[] cs) {
        for (int i = 0, j = 0; i < cs.length; i++) {
            if (cs[i] == '\n') {
                if (j != 0 && isYamlDocumentStart(cs, j)) {
                    final char[] buf = new char[cs.length - i];
                    System.arraycopy(cs, i, buf, 0, buf.length);
                    return buf;
                }
                j = i + 1;
            }
        }
        return cs;
    }

    /**
     * Check if character array offset is the start of YAML document.
     */
    private boolean isYamlDocumentStart(char[] cs, int i) {
        return cs[i] == '-' && cs[i + 1] == '-' && cs[i + 2] == '-';
    }

    private Map<String, Object> parserYaml(char[] markdownContent) throws IOException {
        if (markdownContent.length > 3 && isYamlDocumentStart(markdownContent, 0)) {
            Yaml yaml = new Yaml();
            try (final CharArrayReader in = new CharArrayReader(markdownContent)) {
                final Iterator<Object> docs = yaml.loadAll(in).iterator();
                if (docs.hasNext()) {
                    final Object doc = docs.next();
                    if (doc instanceof Map) {
                        return (Map<String, Object>) doc;
                    } else {
                        System.err.println("Only top level maps supported, found " + doc.getClass().getName());
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns an input stream that skips the BOM if present.
     *
     * @param in the original input stream
     * @return An input stream without a possible BOM
     * @throws IOException
     */
    private InputStream consumeBOM(final InputStream in) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(in);
        bin.mark(3);
        try {
            final byte[] buf = new byte[3];
            bin.read(buf);
            if (buf[0] != (byte) 0xEF || buf[1] != (byte) 0xBB || buf[2] != (byte) 0xBF) {
                bin.reset();
            }
        } catch (final IOException e) {
            bin.reset();
        }
        return bin;
    }

    private void parseAST(final RootNode root, final Map<String, Object> header) throws SAXException {
        final TransformerHandler h;
        try {
            h = tf.newTransformerHandler(t);
        } catch (final TransformerConfigurationException e) {
            throw new SAXException(e);
        }
        h.setResult(new SAXResult(contentHandler));
        final ToDitaSerializer s = new ToDitaSerializer(h, header);
        s.toHtml(root);
    }

}
