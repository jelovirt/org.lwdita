/*
 * Based on ToDitaSerializer (C) 2010-2011 Mathias Doenitz
 */
package com.elovirta.dita.markdown;

import static org.dita.dost.util.Constants.ATTRIBUTE_PREFIX_DITAARCHVERSION;
import static org.dita.dost.util.Constants.DITA_NAMESPACE;

import com.elovirta.dita.markdown.renderer.*;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.*;
import com.vladsch.flexmark.util.sequence.LineAppendable;
import com.vladsch.flexmark.util.sequence.TagRange;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class DitaRenderer {

  /** Treat first paragraph as shortdesc. */
  public static final DataKey<Boolean> SHORTDESC_PARAGRAPH = new DataKey<>("SHORTDESC_PARAGRAPH", false);
  public static final DataKey<Boolean> TIGHT_LIST = new DataKey<>("TIGHT_LIST", true);
  /** Read topic ID from YAML header if available. */
  public static final DataKey<Boolean> ID_FROM_YAML = new DataKey<>("ID_FROM_YAML", false);
  /** Allow XML DITA in Markdown. */
  public static final DataKey<Boolean> RAW_DITA = new DataKey<>("RAW_DITA", true);

  /**
   * Parse as MDITA.
   *
   * @deprecated use {@link #MDITA_EXTENDED_PROFILE} instead.
   */
  @Deprecated
  public static final DataKey<Boolean> LW_DITA = new DataKey<>("LW_DITA", false);

  /** Parse as MDITA core profile. */
  public static final DataKey<Boolean> MDITA_CORE_PROFILE = new DataKey<>("MDITA_CORE_PROFILE", false);
  /** Parse as MDITA extended profile. */
  public static final DataKey<Boolean> MDITA_EXTENDED_PROFILE = new DataKey<>("MDITA_EXTENDED_PROFILE", false);
  /** Support concept, task, and reference specialization from heading class. */
  public static final DataKey<Boolean> SPECIALIZATION = new DataKey<>("SPECIALIZATION", false);
  /** Generate DITA concept output. */
  public static final DataKey<Boolean> SPECIALIZATION_CONCEPT = new DataKey<>("SPECIALIZATION_CONCEPT", false);
  /** Generate DITA task output. */
  public static final DataKey<Boolean> SPECIALIZATION_TASK = new DataKey<>("SPECIALIZATION_TASK", false);
  /** Generate DITA reference output. */
  public static final DataKey<Boolean> SPECIALIZATION_REFERENCE = new DataKey<>("SPECIALIZATION_REFERENCE", false);
  /** Fix missing root heading by reading title from either YAML heading or filename. */
  public static final DataKey<Boolean> WIKI = new DataKey<>("WIKI", false);
  /** Fix missing root heading by reading title from either YAML heading or filename. */
  public static final DataKey<Boolean> FIX_ROOT_HEADING = new DataKey<>("FIXUP_ROOT_HEADING", false);
  /** Generate DITA map output. */
  public static final DataKey<Boolean> MAP = new DataKey<>("MAP", false);
  //  public static final DataKey<String> SOFT_BREAK = new DataKey<>("SOFT_BREAK", "\n");
  //  public static final DataKey<String> HARD_BREAK = new DataKey<>("HARD_BREAK", "<br />\n");
  //  public static final NullableDataKey<String> STRONG_EMPHASIS_STYLE_HTML_OPEN = new NullableDataKey<>(
  //    "STRONG_EMPHASIS_STYLE_HTML_OPEN"
  //  );
  //  public static final NullableDataKey<String> STRONG_EMPHASIS_STYLE_HTML_CLOSE = new NullableDataKey<>(
  //    "STRONG_EMPHASIS_STYLE_HTML_CLOSE"
  //  );
  //  public static final NullableDataKey<String> EMPHASIS_STYLE_HTML_OPEN = new NullableDataKey<>(
  //    "EMPHASIS_STYLE_HTML_OPEN"
  //  );
  //  public static final NullableDataKey<String> EMPHASIS_STYLE_HTML_CLOSE = new NullableDataKey<>(
  //    "EMPHASIS_STYLE_HTML_CLOSE"
  //  );
  //  public static final NullableDataKey<String> CODE_STYLE_HTML_OPEN = new NullableDataKey<>("CODE_STYLE_HTML_OPEN");
  //  public static final NullableDataKey<String> CODE_STYLE_HTML_CLOSE = new NullableDataKey<>("CODE_STYLE_HTML_CLOSE");
  //  public static final NullableDataKey<String> INLINE_CODE_SPLICE_CLASS = new NullableDataKey<>(
  //    "INLINE_CODE_SPLICE_CLASS"
  //  );
  //  public static final DataKey<Boolean> PERCENT_ENCODE_URLS = SharedDataKeys.PERCENT_ENCODE_URLS;
  //  public static final DataKey<Integer> INDENT_SIZE = SharedDataKeys.INDENT_SIZE;
  //  public static final DataKey<Boolean> ESCAPE_HTML = new DataKey<>("ESCAPE_HTML", false);
  //  public static final DataKey<Boolean> ESCAPE_HTML_BLOCKS = new DataKey<>("ESCAPE_HTML_BLOCKS", ESCAPE_HTML);
  //  public static final DataKey<Boolean> ESCAPE_HTML_COMMENT_BLOCKS = new DataKey<>(
  //    "ESCAPE_HTML_COMMENT_BLOCKS",
  //    ESCAPE_HTML_BLOCKS
  //  );
  //  public static final DataKey<Boolean> ESCAPE_INLINE_HTML = new DataKey<>("ESCAPE_HTML_BLOCKS", ESCAPE_HTML);
  //  public static final DataKey<Boolean> ESCAPE_INLINE_HTML_COMMENTS = new DataKey<>(
  //    "ESCAPE_INLINE_HTML_COMMENTS",
  //    ESCAPE_INLINE_HTML
  //  );
  public static final DataKey<Boolean> SUPPRESS_HTML = new DataKey<>("SUPPRESS_HTML", false);
  public static final DataKey<Boolean> SUPPRESS_HTML_BLOCKS = new DataKey<>("SUPPRESS_HTML_BLOCKS", SUPPRESS_HTML);
  //  public static final DataKey<Boolean> SUPPRESS_HTML_COMMENT_BLOCKS = new DataKey<>(
  //    "SUPPRESS_HTML_COMMENT_BLOCKS",
  //    SUPPRESS_HTML_BLOCKS
  //  );
  //  public static final DataKey<Boolean> SUPPRESS_INLINE_HTML = new DataKey<>("SUPPRESS_INLINE_HTML", SUPPRESS_HTML);
  //  public static final DataKey<Boolean> SUPPRESS_INLINE_HTML_COMMENTS = new DataKey<>(
  //    "SUPPRESS_INLINE_HTML_COMMENTS",
  //    SUPPRESS_INLINE_HTML
  //  );
  //  public static final DataKey<Boolean> SOURCE_WRAP_HTML = new DataKey<>("SOURCE_WRAP_HTML", false);
  //  public static final DataKey<Boolean> SOURCE_WRAP_HTML_BLOCKS = new DataKey<>(
  //    "SOURCE_WRAP_HTML_BLOCKS",
  //    SOURCE_WRAP_HTML
  //  );
  public static final DataKey<Boolean> HEADER_ID_GENERATOR_RESOLVE_DUPES =
    SharedDataKeys.HEADER_ID_GENERATOR_RESOLVE_DUPES;
  public static final DataKey<String> HEADER_ID_GENERATOR_TO_DASH_CHARS =
    SharedDataKeys.HEADER_ID_GENERATOR_TO_DASH_CHARS;
  //  public static final DataKey<String> HEADER_ID_GENERATOR_NON_DASH_CHARS =
  //    SharedDataKeys.HEADER_ID_GENERATOR_NON_DASH_CHARS;
  public static final DataKey<Boolean> HEADER_ID_GENERATOR_NO_DUPED_DASHES =
    SharedDataKeys.HEADER_ID_GENERATOR_NO_DUPED_DASHES;
  //  public static final DataKey<Boolean> HEADER_ID_GENERATOR_NON_ASCII_TO_LOWERCASE =
  //    SharedDataKeys.HEADER_ID_GENERATOR_NON_ASCII_TO_LOWERCASE;
  //  public static final DataKey<Boolean> HEADER_ID_REF_TEXT_TRIM_LEADING_SPACES =
  //    SharedDataKeys.HEADER_ID_REF_TEXT_TRIM_LEADING_SPACES;
  //  public static final DataKey<Boolean> HEADER_ID_REF_TEXT_TRIM_TRAILING_SPACES =
  //    SharedDataKeys.HEADER_ID_REF_TEXT_TRIM_TRAILING_SPACES;
  //  public static final DataKey<Boolean> HEADER_ID_ADD_EMOJI_SHORTCUT = SharedDataKeys.HEADER_ID_ADD_EMOJI_SHORTCUT;
  //  public static final DataKey<Boolean> RENDER_HEADER_ID = SharedDataKeys.RENDER_HEADER_ID;
  //  public static final DataKey<Boolean> GENERATE_HEADER_ID = SharedDataKeys.GENERATE_HEADER_ID;
  public static final DataKey<Boolean> DO_NOT_RENDER_LINKS = SharedDataKeys.DO_NOT_RENDER_LINKS;
  public static final DataKey<String> FENCED_CODE_LANGUAGE_CLASS_PREFIX = new DataKey<>(
    "FENCED_CODE_LANGUAGE_CLASS_PREFIX",
    "language-"
  ); // prefix to add to unmapped info strings
  //  public static final DataKey<HashMap<String, String>> FENCED_CODE_LANGUAGE_CLASS_MAP = new DataKey<>(
  //    "FENCED_CODE_LANGUAGE_CLASS_MAP",
  //    HashMap::new
  //  ); // info to language class mapping
  public static final DataKey<String> FENCED_CODE_NO_LANGUAGE_CLASS = new DataKey<>(
    "FENCED_CODE_NO_LANGUAGE_CLASS",
    ""
  );
  //  public static final DataKey<String> FENCED_CODE_LANGUAGE_DELIMITERS = new DataKey<>(
  //    "FENCED_CODE_LANGUAGE_DELIMITERS",
  //    " \t"
  //  );
  //  public static final DataKey<String> SOURCE_POSITION_ATTRIBUTE = new DataKey<>("SOURCE_POSITION_ATTRIBUTE", "");
  //  public static final DataKey<Boolean> SOURCE_POSITION_PARAGRAPH_LINES = new DataKey<>(
  //    "SOURCE_POSITION_PARAGRAPH_LINES",
  //    false
  //  );
  //  public static final DataKey<String> TYPE = new DataKey<>("TYPE", "HTML");
  //  public static final DataKey<ArrayList<TagRange>> TAG_RANGES = new DataKey<>("TAG_RANGES", ArrayList::new);

  //  public static final DataKey<Boolean> RECHECK_UNDEFINED_REFERENCES = new DataKey<>(
  //    "RECHECK_UNDEFINED_REFERENCES",
  //    false
  //  );
  //  public static final DataKey<Boolean> OBFUSCATE_EMAIL = new DataKey<>("OBFUSCATE_EMAIL", false);
  //  public static final DataKey<Boolean> OBFUSCATE_EMAIL_RANDOM = new DataKey<>("OBFUSCATE_EMAIL_RANDOM", true);
  //  public static final DataKey<Boolean> HTML_BLOCK_OPEN_TAG_EOL = new DataKey<>("HTML_BLOCK_OPEN_TAG_EOL", true);
  //  public static final DataKey<Boolean> HTML_BLOCK_CLOSE_TAG_EOL = new DataKey<>("HTML_BLOCK_CLOSE_TAG_EOL", true);
  //  public static final DataKey<Boolean> UNESCAPE_HTML_ENTITIES = new DataKey<>("UNESCAPE_HTML_ENTITIES", true);
  //  public static final DataKey<String> AUTOLINK_WWW_PREFIX = new DataKey<>("AUTOLINK_WWW_PREFIX", "http://");

  // regex for suppressed link prefixes
  //  public static final DataKey<String> SUPPRESSED_LINKS = new DataKey<>("SUPPRESSED_LINKS", "javascript:.*");
  //  public static final DataKey<Boolean> NO_P_TAGS_USE_BR = new DataKey<>("NO_P_TAGS_USE_BR", false);
  //  public static final DataKey<Boolean> EMBEDDED_ATTRIBUTE_PROVIDER = new DataKey<>("EMBEDDED_ATTRIBUTE_PROVIDER", true);
  //
  //  public static final DataKey<Integer> FORMAT_FLAGS = new DataKey<>(
  //    "RENDERER_FORMAT_FLAGS",
  //    LineAppendable.F_TRIM_LEADING_WHITESPACE
  //  );
  //  public static final DataKey<Integer> MAX_TRAILING_BLANK_LINES = SharedDataKeys.RENDERER_MAX_TRAILING_BLANK_LINES;
  //  public static final DataKey<Integer> MAX_BLANK_LINES = SharedDataKeys.RENDERER_MAX_BLANK_LINES;

  private final DitaRendererOptions ditaOptions;
  private final DataHolder options;

  DitaRenderer(DataSet builder) {
    this.options =
      new MutableDataSet(builder)
        // Support legacy LW_DITA as an alias for MDITA_EXTENDED_PROFILE
        .set(MDITA_EXTENDED_PROFILE, LW_DITA.get(builder) || MDITA_EXTENDED_PROFILE.get(builder))
        .toImmutable();
    this.ditaOptions = new DitaRendererOptions(this.options);
  }

  public void render(Node node, ContentHandler out) {
    final SaxWriter saxWriter = new SaxWriter(out);
    MainNodeRenderer renderer = new MainNodeRenderer(options, saxWriter, node.getDocument());
    renderer.render(node);
  }

  private class MainNodeRenderer implements NodeRendererContext {

    private final Document document;
    private final Map<Class<? extends Node>, NodeRenderingHandler<? extends Node>> renderers;
    private final DataHolder options;
    private final DitaIdGenerator ditaIdGenerator;
    private final SaxWriter saxWriter;
    private Node renderingNode;
    private NodeRenderingHandler<? extends Node> renderingHandler;
    private int doNotRenderLinksNesting;

    MainNodeRenderer(DataHolder options, SaxWriter saxWriter, Document document) {
      this.saxWriter = saxWriter;
      this.renderingNode = null;
      this.doNotRenderLinksNesting = 0;
      this.options = new ScopedDataSet(options, document);
      this.document = document;
      this.renderers =
        DitaRenderer.MAP.get(options)
          ? new MapRenderer(this.getOptions()).getNodeRenderingHandlers()
          : new TopicRenderer(this.getOptions()).getNodeRenderingHandlers();
      this.doNotRenderLinksNesting = ditaOptions.doNotRenderLinksInDocument ? 0 : 1;
      this.ditaIdGenerator = new HeaderIdGenerator();
    }

    @Override
    public DataHolder getOptions() {
      return options;
    }

    @Override
    public DitaRendererOptions getDitaOptions() {
      return ditaOptions;
    }

    @Override
    public Document getDocument() {
      return document;
    }

    @Override
    public void render(Node node) {
      try {
        saxWriter.startDocument();
        saxWriter.getContentHandler().startDocument();
        saxWriter.getContentHandler().startPrefixMapping(ATTRIBUTE_PREFIX_DITAARCHVERSION, DITA_NAMESPACE);
        renderNode(node, this);
        saxWriter.close();
        saxWriter.getContentHandler().endPrefixMapping(ATTRIBUTE_PREFIX_DITAARCHVERSION);
        saxWriter.getContentHandler().endDocument();
      } catch (SAXException e) {
        throw new RuntimeException(e);
      }
    }

    private void renderNode(Node node, MainNodeRenderer subContext) {
      if (node instanceof Document) {
        // here we render multiple phases
        int oldDoNotRenderLinksNesting = subContext.getDoNotRenderLinksNesting();
        int documentDoNotRenderLinksNesting = getDitaOptions().doNotRenderLinksInDocument ? 1 : 0;
        this.ditaIdGenerator.generateIds(document);

        NodeRenderingHandler<? extends Node> nodeRenderer = renderers.get(node.getClass());
        if (nodeRenderer != null) {
          subContext.doNotRenderLinksNesting = documentDoNotRenderLinksNesting;
          NodeRenderingHandler<? extends Node> prevWrapper = subContext.renderingHandler;
          try {
            subContext.renderingNode = node;
            subContext.renderingHandler = nodeRenderer;
            nodeRenderer.render(node, subContext, subContext.saxWriter);
          } finally {
            subContext.renderingHandler = prevWrapper;
            subContext.renderingNode = null;
            subContext.doNotRenderLinksNesting = oldDoNotRenderLinksNesting;
          }
        }
      } else {
        NodeRenderingHandler<? extends Node> nodeRenderer = renderers.get(node.getClass());
        if (nodeRenderer != null) {
          Node oldNode = this.renderingNode;
          int oldDoNotRenderLinksNesting = subContext.doNotRenderLinksNesting;
          NodeRenderingHandler<? extends Node> prevWrapper = subContext.renderingHandler;
          try {
            subContext.renderingNode = node;
            subContext.renderingHandler = nodeRenderer;
            nodeRenderer.render(node, subContext, subContext.saxWriter);
          } finally {
            subContext.renderingNode = oldNode;
            subContext.doNotRenderLinksNesting = oldDoNotRenderLinksNesting;
            subContext.renderingHandler = prevWrapper;
          }
        } else {
          throw new RuntimeException("No renderer configured for " + node.getClass().getName());
        }
      }
    }

    @Override
    public void renderChildren(Node parent) {
      Node node = parent.getFirstChild();
      while (node != null) {
        Node next = node.getNext();
        renderNode(node, this);
        node = next;
      }
    }

    @Override
    public void renderChild(Node node) {
      renderNode(node, this);
    }

    protected int getDoNotRenderLinksNesting() {
      return doNotRenderLinksNesting;
    }
  }
}
