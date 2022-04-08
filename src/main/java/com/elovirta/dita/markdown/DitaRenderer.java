/*
 * Based on ToDitaSerializer (C) 2010-2011 Mathias Doenitz
 */
package com.elovirta.dita.markdown;

import com.elovirta.dita.markdown.renderer.*;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.*;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.Map;

import static org.dita.dost.util.Constants.ATTRIBUTE_PREFIX_DITAARCHVERSION;
import static org.dita.dost.util.Constants.DITA_NAMESPACE;

public class DitaRenderer {

    public static final DataKey<Boolean> SHORTDESC_PARAGRAPH = new DataKey<>("SHORTDESC_PARAGRAPH", false);
    public static final DataKey<Boolean> ID_FROM_YAML = new DataKey<>("ID_FROM_YAML", false);
    public static final DataKey<Boolean> LW_DITA = new DataKey<>("LW_DITA", false);
    public static final DataKey<String> SOFT_BREAK = new DataKey<>("SOFT_BREAK", "\n");
    public static final DataKey<String> HARD_BREAK = new DataKey<>("HARD_BREAK", "<br />\n");
    public static final DataKey<String> STRONG_EMPHASIS_STYLE_HTML_OPEN = new DataKey<>("STRONG_EMPHASIS_STYLE_HTML_OPEN", (String) null);
    public static final DataKey<String> STRONG_EMPHASIS_STYLE_HTML_CLOSE = new DataKey<>("STRONG_EMPHASIS_STYLE_HTML_CLOSE", (String) null);
    public static final DataKey<String> EMPHASIS_STYLE_HTML_OPEN = new DataKey<>("EMPHASIS_STYLE_HTML_OPEN", (String) null);
    public static final DataKey<String> EMPHASIS_STYLE_HTML_CLOSE = new DataKey<>("EMPHASIS_STYLE_HTML_CLOSE", (String) null);
    public static final DataKey<String> CODE_STYLE_HTML_OPEN = new DataKey<>("CODE_STYLE_HTML_OPEN", (String) null);
    public static final DataKey<String> CODE_STYLE_HTML_CLOSE = new DataKey<>("CODE_STYLE_HTML_CLOSE", (String) null);
    public static final DataKey<String> INLINE_CODE_SPLICE_CLASS = new DataKey<>("INLINE_CODE_SPLICE_CLASS", (String) null);
    public static final DataKey<Boolean> PERCENT_ENCODE_URLS = new DataKey<>("ESCAPE_HTML", false);
    public static final DataKey<Integer> INDENT_SIZE = new DataKey<>("INDENT", 0);
    public static final DataKey<Boolean> ESCAPE_HTML = new DataKey<>("ESCAPE_HTML", false);
    public static final DataKey<Boolean> ESCAPE_HTML_BLOCKS = new DataKey<>("ESCAPE_HTML_BLOCKS", ESCAPE_HTML::getFrom);
    public static final DataKey<Boolean> ESCAPE_HTML_COMMENT_BLOCKS = new DataKey<>("ESCAPE_HTML_COMMENT_BLOCKS", ESCAPE_HTML_BLOCKS::getFrom);
    public static final DataKey<Boolean> ESCAPE_INLINE_HTML = new DataKey<>("ESCAPE_HTML_BLOCKS", ESCAPE_HTML::getFrom);
    public static final DataKey<Boolean> ESCAPE_INLINE_HTML_COMMENTS = new DataKey<>("ESCAPE_INLINE_HTML_COMMENTS", ESCAPE_INLINE_HTML::getFrom);
    public static final DataKey<Boolean> SUPPRESS_HTML = new DataKey<>("SUPPRESS_HTML", false);
    public static final DataKey<Boolean> SUPPRESS_HTML_BLOCKS = new DataKey<>("SUPPRESS_HTML_BLOCKS", SUPPRESS_HTML::getFrom);
    public static final DataKey<Boolean> SUPPRESS_HTML_COMMENT_BLOCKS = new DataKey<>("SUPPRESS_HTML_COMMENT_BLOCKS", SUPPRESS_HTML_BLOCKS::getFrom);
    public static final DataKey<Boolean> SUPPRESS_INLINE_HTML = new DataKey<>("SUPPRESS_INLINE_HTML", SUPPRESS_HTML::getFrom);
    public static final DataKey<Boolean> SUPPRESS_INLINE_HTML_COMMENTS = new DataKey<>("SUPPRESS_INLINE_HTML_COMMENTS", SUPPRESS_INLINE_HTML::getFrom);
    public static final DataKey<Boolean> SOURCE_WRAP_HTML = new DataKey<>("SOURCE_WRAP_HTML", false);
    public static final DataKey<Boolean> SOURCE_WRAP_HTML_BLOCKS = new DataKey<>("SOURCE_WRAP_HTML_BLOCKS", SOURCE_WRAP_HTML::getFrom);
    public static final DataKey<Boolean> HEADER_ID_GENERATOR_RESOLVE_DUPES = new DataKey<>("HEADER_ID_GENERATOR_RESOLVE_DUPES", true);
    public static final DataKey<String> HEADER_ID_GENERATOR_TO_DASH_CHARS = new DataKey<>("HEADER_ID_GENERATOR_TO_DASH_CHARS", " -_");
    public static final DataKey<Boolean> HEADER_ID_GENERATOR_NO_DUPED_DASHES = new DataKey<>("HEADER_ID_GENERATOR_NO_DUPED_DASHES", false);
    public static final DataKey<Boolean> RENDER_HEADER_ID = new DataKey<>("RENDER_HEADER_ID", false);
    public static final DataKey<Boolean> GENERATE_HEADER_ID = new DataKey<>("GENERATE_HEADER_ID", true);
    public static final DataKey<Boolean> DO_NOT_RENDER_LINKS = new DataKey<>("DO_NOT_RENDER_LINKS", false);
    public static final DataKey<String> FENCED_CODE_LANGUAGE_CLASS_PREFIX = new DataKey<>("FENCED_CODE_LANGUAGE_CLASS_PREFIX", "language-");
    public static final DataKey<String> FENCED_CODE_NO_LANGUAGE_CLASS = new DataKey<>("FENCED_CODE_NO_LANGUAGE_CLASS", "");
    public static final DataKey<String> SOURCE_POSITION_ATTRIBUTE = new DataKey<>("SOURCE_POSITION_ATTRIBUTE", "");
    public static final DataKey<Boolean> SOURCE_POSITION_PARAGRAPH_LINES = new DataKey<>("SOURCE_POSITION_PARAGRAPH_LINES", false);

    public static final DataKey<Boolean> HTML_BLOCK_OPEN_TAG_EOL = new DataKey<>("HTML_BLOCK_OPEN_TAG_EOL", true);
    public static final DataKey<Boolean> HTML_BLOCK_CLOSE_TAG_EOL = new DataKey<>("HTML_BLOCK_CLOSE_TAG_EOL", true);
    public static final DataKey<Boolean> UNESCAPE_HTML_ENTITIES = new DataKey<>("UNESCAPE_HTML_ENTITIES", true);

    public static final DataKey<Integer> FORMAT_FLAGS = new DataKey<>("FORMAT_FLAGS", 0);
    public static final DataKey<Integer> MAX_TRAILING_BLANK_LINES = new DataKey<>("MAX_TRAILING_BLANK_LINES", 1);

    private final DitaRendererOptions ditaOptions;
    private final DataHolder options;

    DitaRenderer(MutableDataSet builder) {
        this.options = new DataSet(builder);
        this.ditaOptions = new DitaRendererOptions(this.options);
    }

    public void render(Node node, ContentHandler out) {
        final SaxWriter saxWriter = new SaxWriter(out);
        MainNodeRenderer renderer = new MainNodeRenderer(options,
                saxWriter,
                node.getDocument());
        renderer.render(node);
    }

    private class MainNodeRenderer implements NodeRendererContext {
        private final Document document;
        private final Map<Class<? extends Node>, NodeRenderingHandler<? extends Node>> renderers;
        private final DataHolder options;
        private final DitaIdGenerator ditaIdGenerator;
        private final SaxWriter saxWriter;
        private Node renderingNode;
        private NodeRenderingHandler renderingHandler;
        private int doNotRenderLinksNesting;

        MainNodeRenderer(DataHolder options, SaxWriter saxWriter, Document document) {
            this.saxWriter = saxWriter;
            this.renderingNode = null;
            this.doNotRenderLinksNesting = 0;
            this.options = new ScopedDataSet(options, document);
            this.document = document;
            this.renderers = new CoreNodeRenderer(this.getOptions()).getNodeRenderingHandlers();
            this.doNotRenderLinksNesting = ditaOptions.doNotRenderLinksInDocument ? 0 : 1;
            this.ditaIdGenerator = new HeaderIdGenerator();

            saxWriter.setContext(this);
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
                saxWriter.contentHandler.startDocument();
                saxWriter.contentHandler.startPrefixMapping(ATTRIBUTE_PREFIX_DITAARCHVERSION, DITA_NAMESPACE);
                renderNode(node, this);
                saxWriter.close();
                saxWriter.contentHandler.endPrefixMapping(ATTRIBUTE_PREFIX_DITAARCHVERSION);
                saxWriter.contentHandler.endDocument();
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

                NodeRenderingHandler nodeRenderer = renderers.get(node.getClass());
                if (nodeRenderer != null) {
                    subContext.doNotRenderLinksNesting = documentDoNotRenderLinksNesting;
                    NodeRenderingHandler prevWrapper = subContext.renderingHandler;
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
                NodeRenderingHandler nodeRenderer = renderers.get(node.getClass());
                if (nodeRenderer != null) {
                    Node oldNode = this.renderingNode;
                    int oldDoNotRenderLinksNesting = subContext.doNotRenderLinksNesting;
                    NodeRenderingHandler prevWrapper = subContext.renderingHandler;
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

        protected int getDoNotRenderLinksNesting() {
            return doNotRenderLinksNesting;
        }
    }
}
