package com.elovirta.dita.markdown;

import static com.google.common.io.CharStreams.copy;
import static java.util.Arrays.asList;

import com.google.common.annotations.VisibleForTesting;
import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension;
import com.vladsch.flexmark.ext.admonition.AdmonitionExtension;
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
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.data.DataSet;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xml.sax.*;
import org.xml.sax.helpers.LocatorImpl;

/**
 * XMLReader implementation for Markdown.
 */
public class MarkdownReader implements XMLReader {

  private static final Pattern schemaPattern = Pattern.compile("^---[\r\n]+\\$schema: +(.+?)[\r\n]");
  private static final ServiceLoader<SchemaProvider> schemaLoader = ServiceLoader.load(SchemaProvider.class);

  public static final DataKey<Collection<String>> FORMATS = new DataKey<>("FORMATS", List.of("markdown", "md"));
  public static final DataKey<Boolean> PROCESSING_MODE = new DataKey<>("PROCESSING_MODE", false);

  /**
   * Supported features mapped to options.
   *
   * <dl>
   *     <dt><code>http://lwdita.org/sax/features/shortdesc-paragraph</code></dt>
   *     <dd>Treat first paragraph as shortdesc.</dd>
   *     <dt><code>http://lwdita.org/sax/features/id-from-yaml</code></dt>
   *     <dd>Read topic ID from YAML header if available.</dd>
   *     <dt><code>http://lwdita.org/sax/features/mdita</code></dt>
   *     <dd>Parse as MDITA.</dd>
   *     <dt><code>http://lwdita.org/sax/features/mdita-extended-profile</code></dt>
   *     <dd>Parse as MDITA extended profile.</dd>
   *     <dt><code>http://lwdita.org/sax/features/mdita-core-profile</code></dt>
   *     <dd>Parse as MDITA core profile.</dd>
   *     <dt><code>http://lwdita.org/sax/features/specialization</code></dt>
   *     <dd>Support concept, task, and reference specialization from heading class.</dd>
   *     <dt><code>http://lwdita.org/sax/features/specialization-concept</code></dt>
   *     <dd>Generate DITA concept output.</dd>
   *     <dt><code>http://lwdita.org/sax/features/specialization-task</code></dt>
   *     <dd>Generate DITA task output.</dd>
   *     <dt><code>http://lwdita.org/sax/features/specialization-reference</code></dt>
   *     <dd>Generate DITA reference output.</dd>
   *     <dt><code>http://lwdita.org/sax/features/fix-root-heading</code></dt>
   *     <dd>Fix missing root heading by reading title from either YAML heading or filename.</dd>
   *     <dt><code>http://lwdita.org/sax/features/map</code></dt>
   *     <dd>Generate DITA map output.</dd>
   * </dl>
   */
  private static final Map<String, DataKey<Boolean>> FEATURES;

  static {
    final Map<String, DataKey<Boolean>> features = new HashMap<>();
    features.put("http://lwdita.org/sax/features/shortdesc-paragraph", DitaRenderer.SHORTDESC_PARAGRAPH);
    features.put("http://lwdita.org/sax/features/tight-list", DitaRenderer.TIGHT_LIST);
    features.put("http://lwdita.org/sax/features/id-from-yaml", DitaRenderer.ID_FROM_YAML);
    features.put("http://lwdita.org/sax/features/mdita", DitaRenderer.MDITA_EXTENDED_PROFILE);
    features.put("http://lwdita.org/sax/features/mdita-extended-profile", DitaRenderer.MDITA_EXTENDED_PROFILE);
    features.put("http://lwdita.org/sax/features/mdita-core-profile", DitaRenderer.MDITA_CORE_PROFILE);
    features.put("http://lwdita.org/sax/features/specialization", DitaRenderer.SPECIALIZATION);
    features.put("http://lwdita.org/sax/features/specialization-concept", DitaRenderer.SPECIALIZATION_CONCEPT);
    features.put("http://lwdita.org/sax/features/specialization-task", DitaRenderer.SPECIALIZATION_TASK);
    features.put("http://lwdita.org/sax/features/specialization-reference", DitaRenderer.SPECIALIZATION_REFERENCE);
    features.put("http://lwdita.org/sax/features/fix-root-heading", DitaRenderer.FIX_ROOT_HEADING);
    features.put("http://lwdita.org/sax/features/map", DitaRenderer.MAP);
    FEATURES = Collections.unmodifiableMap(features);
  }

  private final MutableDataSet options;

  EntityResolver resolver;
  ContentHandler contentHandler;
  ErrorHandler errorHandler;

  /**
   * @see <a href="https://github.com/vsch/flexmark-java/wiki/Extensions">Extensions</a>
   */
  public MarkdownReader() {
    this(
      new MutableDataSet()
        .set(
          Parser.EXTENSIONS,
          asList(
            AbbreviationExtension.create(),
            AdmonitionExtension.create(),
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
            StrikethroughSubscriptExtension.create()
          )
        )
        .set(DefinitionExtension.TILDE_MARKER, false)
        .set(TablesExtension.COLUMN_SPANS, true)
        .set(TablesExtension.APPEND_MISSING_COLUMNS, false)
        .set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
        .set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true)
        .set(DitaRenderer.SPECIALIZATION, true)
        .set(DitaRenderer.WIKI, true)
    );
  }

  public MarkdownReader(final DataSet options) {
    this.options = new MutableDataSet(options);
  }

  @Override
  public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
    switch (name) {
      case "http://xml.org/sax/features/namespaces":
        return true;
      case "http://xml.org/sax/features/namespace-prefixes":
        return false;
      default:
        final DataKey<Boolean> option = FEATURES.get(name);
        if (option != null) {
          return option.get(options);
        } else {
          throw new SAXNotRecognizedException("Unrecognized feature " + name);
        }
    }
  }

  /**
   * Set the value of a feature flag.
   *
   * @see #FEATURES
   */
  @Override
  public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
    switch (name) {
      case "http://xml.org/sax/features/namespaces":
        if (!value) {
          throw new SAXNotSupportedException("Unsupported value " + value + " for " + name);
        }
        break;
      case "http://xml.org/sax/features/namespace-prefixes":
        if (value) {
          throw new SAXNotSupportedException("Unsupported value " + value + " for " + name);
        }
        break;
      case "http://lwdita.org/sax/features/mdita-extended-profile":
        options.setAll(MDitaReader.EXTENDED_PROFILE);
        break;
      case "http://lwdita.org/sax/features/mdita-core-profile":
        options.setAll(MDitaReader.CORE_PROFILE);
        break;
      case "http://xml.org/sax/features/external-general-entities":
      case "http://xml.org/sax/features/external-parameter-entities":
      case "http://xml.org/sax/features/is-standalone":
      case "http://xml.org/sax/features/lexical-handler/parameter-entities":
      case "http://xml.org/sax/features/resolve-dtd-uris":
      case "http://xml.org/sax/features/string-interning":
      case "http://xml.org/sax/features/unicode-normalization-checking":
      case "http://xml.org/sax/features/use-attributes2":
      case "http://xml.org/sax/features/use-locator2":
      case "http://xml.org/sax/features/use-entity-resolver2":
      case "http://xml.org/sax/features/validation":
      case "http://xml.org/sax/features/xmlns-uris":
      case "http://xml.org/sax/features/xml-1.1":
        // Ignore. Should throw SAXNotSupportedException but Saxon doesn't catch the exception
        break;
      default:
        final DataKey<Boolean> option = FEATURES.get(name);
        if (option != null) {
          options.set(option, value);
        } else {
          throw new SAXNotRecognizedException("Unrecognized feature " + name);
        }
    }
  }

  @Override
  public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
    return null;
  }

  @Override
  public void setProperty(String name, Object value) throws SAXNotRecognizedException {
    switch (name) {
      case "https://dita-ot.org/property/formats":
        options.set(FORMATS, (Collection<String>) value);
        break;
      case "https://dita-ot.org/property/processing-mode":
        options.set(PROCESSING_MODE, "strict".equals(value));
        break;
      case "http://xml.org/sax/properties/declaration-handler":
      case "http://xml.org/sax/properties/document-xml-version":
      case "http://xml.org/sax/properties/dom-node":
      case "http://xml.org/sax/properties/lexical-handler":
      case "http://xml.org/sax/properties/xml-string":
        // Ignore. Should throw SAXNotSupportedException but Saxon doesn't catch the exception
        break;
      default:
        throw new SAXNotRecognizedException(String.format("Property %s not supported", name));
    }
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
    final Map.Entry<URI, Locator> schema = getSchema(markdownContent, input);
    final BasedSequence sequence = BasedSequence.of(CharBuffer.wrap(markdownContent));

    final MarkdownParser markdownParser = getParser(schema);
    markdownParser.setContentHandler(contentHandler);
    markdownParser.setErrorHandler(errorHandler);
    try {
      markdownParser.convert(sequence, Optional.ofNullable(input.getSystemId()).map(URI::create).orElse(null));
    } catch (ParseException e) {
      throw new SAXException("Failed to parse: " + e.getMessage(), e);
    }
  }

  @Override
  public void parse(final String systemId) throws IOException, SAXException {
    parse(new InputSource(systemId));
  }

  private MarkdownParser getParser(Map.Entry<URI, Locator> schema) throws SAXException {
    if (schema != null) {
      final URI value = schema.getKey();
      final Optional<MarkdownParser> markdownParser = schemaLoader
        .stream()
        .filter(p -> p.get().isSupportedSchema(value))
        .findAny()
        .map(s -> s.get().createMarkdownParser(value));
      if (markdownParser.isEmpty()) {
        if (errorHandler != null) {
          errorHandler.error(
            new SAXParseException(
              String.format("Markdown schema %s not recognized, using default Markdown parser", value),
              schema.getValue()
            )
          );
        }
      }
      return markdownParser.orElse(new MarkdownParserImpl(options.toImmutable()));
    } else {
      return new MarkdownParserImpl(options.toImmutable());
    }
  }

  @VisibleForTesting
  Map.Entry<URI, Locator> getSchema(char[] data, InputSource input) throws SAXParseException {
    final Matcher matcher = schemaPattern.matcher(CharBuffer.wrap(data));
    if (matcher.find() && matcher.group(1) != null) {
      final String value = matcher.group(1).replaceAll("^'(.+)'$", "$1").replaceAll("^\"(.+)\"$", "$1").trim();

      final LocatorImpl locator = new LocatorImpl();
      locator.setSystemId(input.getSystemId());
      locator.setPublicId(input.getPublicId());
      for (int i = 0, row = 1, col = 1, end = matcher.end(1); i < data.length; i++) {
        if (i == end) {
          locator.setLineNumber(row);
          locator.setColumnNumber(col);
          break;
        }
        if (data[i] == '\n') {
          row++;
          col = 1;
        } else if (data[i] == '\r') {
          // ignore
        } else {
          col++;
        }
      }

      try {
        final URI schema = new URI(value);
        return Map.entry(schema, locator);
      } catch (URISyntaxException e) {
        throw new SAXParseException(String.format("Failed to parse schema URI %s ", value), locator, e);
      }
    }
    return null;
  }

  @VisibleForTesting
  char[] getMarkdownContent(final InputSource input) throws IOException {
    final CharArrayWriter out = new CharArrayWriter();
    if (input.getByteStream() != null) {
      final String encoding = input.getEncoding() != null ? input.getEncoding() : "UTF-8";
      try (
        BufferedInputStream is = "UTF-8".equalsIgnoreCase(encoding)
          ? consumeBOM(input.getByteStream())
          : new BufferedInputStream(input.getByteStream());
        Reader in = new InputStreamReader(is, encoding)
      ) {
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
      try (
        BufferedInputStream is = "UTF-8".equalsIgnoreCase(encoding)
          ? consumeBOM(inUrl.openStream())
          : new BufferedInputStream(inUrl.openStream());
        Reader in = new InputStreamReader(is, encoding)
      ) {
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
