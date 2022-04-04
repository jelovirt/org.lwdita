/*
 * Based on ToDitaSerializer (C) 2010-2011 Mathias Doenitz
 */
package com.elovirta.dita.markdown;

import com.elovirta.dita.markdown.renderer.*;
import com.vladsch.flexmark.html.Disposable;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.IRender;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.builder.Extension;
import com.vladsch.flexmark.util.data.*;
import com.vladsch.flexmark.util.dependency.DependencyHandler;
import com.vladsch.flexmark.util.dependency.ResolvedDependencies;
import com.vladsch.flexmark.util.sequence.TagRange;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.*;

import static org.dita.dost.util.Constants.ATTRIBUTE_PREFIX_DITAARCHVERSION;
import static org.dita.dost.util.Constants.DITA_NAMESPACE;

public class DitaRenderer implements IRender {

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
    //public static final DataKey<Boolean> SOURCE_WRAP_INLINE_HTML = new DataKey<>("SOURCE_WRAP_INLINE_HTML", SOURCE_WRAP_HTML::getFrom);
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
    public static final DataKey<String> TYPE = new DataKey<>("TYPE", "HTML");
    public static final DataKey<ArrayList<TagRange>> TAG_RANGES = new DataKey<>("TAG_RANGES", value -> new ArrayList<>());

    public static final DataKey<Boolean> RECHECK_UNDEFINED_REFERENCES = new DataKey<>("RECHECK_UNDEFINED_REFERENCES", false);
    public static final DataKey<Boolean> OBFUSCATE_EMAIL = new DataKey<>("OBFUSCATE_EMAIL", false);
    public static final DataKey<Boolean> OBFUSCATE_EMAIL_RANDOM = new DataKey<>("OBFUSCATE_EMAIL_RANDOM", true);
    public static final DataKey<Boolean> HTML_BLOCK_OPEN_TAG_EOL = new DataKey<>("HTML_BLOCK_OPEN_TAG_EOL", true);
    public static final DataKey<Boolean> HTML_BLOCK_CLOSE_TAG_EOL = new DataKey<>("HTML_BLOCK_CLOSE_TAG_EOL", true);
    public static final DataKey<Boolean> UNESCAPE_HTML_ENTITIES = new DataKey<>("UNESCAPE_HTML_ENTITIES", true);

    public static final DataKey<Integer> FORMAT_FLAGS = new DataKey<>("FORMAT_FLAGS", 0);
    public static final DataKey<Integer> MAX_TRAILING_BLANK_LINES = new DataKey<>("MAX_TRAILING_BLANK_LINES", 1);

    private final List<DelegatingNodeRendererFactoryWrapper> nodeRendererFactories;
    private final HeaderIdGeneratorFactory ditaIdGeneratorFactory;
    private final DitaRendererOptions ditaOptions;
    private final DataHolder options;
    private final Builder builder;
    private final Map<String, List<String>> metadata;

    DitaRenderer(Builder builder, Map<String, List<String>> metadata) {
//        final ContentHandler contentHandler, final Map<String, Object> documentMetadata

        this.builder = new Builder(builder); // take a copy to avoid after creation side effects
        this.options = new DataSet(builder);
        this.ditaOptions = new DitaRendererOptions(this.options);
        this.metadata = metadata;

        this.ditaIdGeneratorFactory = builder.ditaIdGeneratorFactory;

        // resolve renderer dependencies

        // Add as last. This means clients can override the rendering of core nodes if they want by default
        final CoreNodeRenderer.Factory nodeRendererFactory = new CoreNodeRenderer.Factory();
        final List<DelegatingNodeRendererFactoryWrapper> nodeRenderers = Collections.singletonList(new DelegatingNodeRendererFactoryWrapper(nodeRendererFactory));

        DitaRenderer.RendererDependencyHandler resolver = new DitaRenderer.RendererDependencyHandler();
        nodeRendererFactories = resolver.resolveDependencies(nodeRenderers).getNodeRendererFactories();
    }

    @Override
    public DataHolder getOptions() {
        return new DataSet(builder);
    }

    @Override
    public void render(Node node, Appendable output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String render(Node node) {
        StringBuilder sb = new StringBuilder();
        render(node, sb);
        return sb.toString();
    }

    @Override
    public DitaRenderer withOptions(DataHolder options) {
//        return options == null ? this : new DitaRenderer(new Builder(builder, options));
        return null;
    }

    public void render(Node node, ContentHandler out) {
        final DitaWriter ditaWriter = new DitaWriter(out);
        MainNodeRenderer renderer = new MainNodeRenderer(options,
                ditaWriter,
                node.getDocument());
        renderer.render(node);
    }

    public static class RendererDependencyStage {
        private final List<DelegatingNodeRendererFactoryWrapper> dependents;

        public RendererDependencyStage(List<DelegatingNodeRendererFactoryWrapper> dependents) {
            this.dependents = dependents;
        }
    }

    public static class RendererDependencies extends ResolvedDependencies<DitaRenderer.RendererDependencyStage> {
        private final List<DelegatingNodeRendererFactoryWrapper> nodeRendererFactories;

        public RendererDependencies(List<DitaRenderer.RendererDependencyStage> dependentStages) {
            super(dependentStages);
            List<DelegatingNodeRendererFactoryWrapper> blockPreProcessorFactories = new ArrayList<>();
            for (DitaRenderer.RendererDependencyStage stage : dependentStages) {
                blockPreProcessorFactories.addAll(stage.dependents);
            }
            this.nodeRendererFactories = blockPreProcessorFactories;
        }

        public List<DelegatingNodeRendererFactoryWrapper> getNodeRendererFactories() {
            return nodeRendererFactories;
        }
    }

    private static class RendererDependencyHandler extends DependencyHandler<DelegatingNodeRendererFactoryWrapper, DitaRenderer.RendererDependencyStage, DitaRenderer.RendererDependencies> {
        @Override
        protected Class getDependentClass(DelegatingNodeRendererFactoryWrapper dependent) {
            return dependent.getFactory().getClass();
        }

        @Override
        protected DitaRenderer.RendererDependencies createResolvedDependencies(List<DitaRenderer.RendererDependencyStage> stages) {
            return new DitaRenderer.RendererDependencies(stages);
        }

        @Override
        protected DitaRenderer.RendererDependencyStage createStage(List<DelegatingNodeRendererFactoryWrapper> dependents) {
            return new DitaRenderer.RendererDependencyStage(dependents);
        }
    }


    private class MainNodeRenderer extends NodeRendererSubContext implements NodeRendererContext {
        private final Document document;
        private final Map<Class<?>, NodeRenderingHandlerWrapper> renderers;

        private final DataHolder options;
        private final DitaIdGenerator ditaIdGenerator;

        MainNodeRenderer(DataHolder options, DitaWriter ditaWriter, Document document) {
            super(ditaWriter);
            this.options = new ScopedDataSet(options, document);
            this.document = document;
            this.renderers = new HashMap<>(32);
            this.doNotRenderLinksNesting = ditaOptions.doNotRenderLinksInDocument ? 0 : 1;
            this.ditaIdGenerator = ditaIdGeneratorFactory != null
                    ? ditaIdGeneratorFactory.create(this)
                    : (!(ditaOptions.renderHeaderId || ditaOptions.generateHeaderIds)
                    ? DitaIdGenerator.NULL
                    : new HeaderIdGenerator.Factory().create(this));

            ditaWriter.setContext(this);

            for (int i = nodeRendererFactories.size() - 1; i >= 0; i--) {
                NodeRendererFactory nodeRendererFactory = nodeRendererFactories.get(i);
                NodeRenderer nodeRenderer = nodeRendererFactory.apply(this.getOptions());
                for (NodeRenderingHandler nodeType : nodeRenderer.getNodeRenderingHandlers()) {
                    NodeRenderingHandlerWrapper handlerWrapper = new NodeRenderingHandlerWrapper(nodeType, renderers.get(nodeType.getNodeType()));
                    renderers.put(nodeType.getNodeType(), handlerWrapper);
                }
            }
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
                ditaWriter.contentHandler.startDocument();
                ditaWriter.contentHandler.startPrefixMapping(ATTRIBUTE_PREFIX_DITAARCHVERSION, DITA_NAMESPACE);
                renderNode(node, this);
                ditaWriter.close();
                ditaWriter.contentHandler.endPrefixMapping(ATTRIBUTE_PREFIX_DITAARCHVERSION);
                ditaWriter.contentHandler.endDocument();
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
        }

        void renderNode(Node node, NodeRendererSubContext subContext) {
            if (node instanceof Document) {
                // here we render multiple phases
                int oldDoNotRenderLinksNesting = subContext.getDoNotRenderLinksNesting();
                int documentDoNotRenderLinksNesting = getDitaOptions().doNotRenderLinksInDocument ? 1 : 0;
                this.ditaIdGenerator.generateIds(document);

                NodeRenderingHandlerWrapper nodeRenderer = renderers.get(node.getClass());
                if (nodeRenderer != null) {
                    subContext.doNotRenderLinksNesting = documentDoNotRenderLinksNesting;
                    NodeRenderingHandlerWrapper prevWrapper = subContext.renderingHandlerWrapper;
                    try {
                        subContext.renderingNode = node;
                        subContext.renderingHandlerWrapper = nodeRenderer;
                        nodeRenderer.myRenderingHandler.render(node, subContext, subContext.ditaWriter);
                    } finally {
                        subContext.renderingHandlerWrapper = prevWrapper;
                        subContext.renderingNode = null;
                        subContext.doNotRenderLinksNesting = oldDoNotRenderLinksNesting;
                    }
                }
            } else {
                NodeRenderingHandlerWrapper nodeRenderer = renderers.get(node.getClass());
                if (nodeRenderer != null) {
                    Node oldNode = this.renderingNode;
                    int oldDoNotRenderLinksNesting = subContext.doNotRenderLinksNesting;
                    NodeRenderingHandlerWrapper prevWrapper = subContext.renderingHandlerWrapper;
                    try {
                        subContext.renderingNode = node;
                        subContext.renderingHandlerWrapper = nodeRenderer;
                        nodeRenderer.myRenderingHandler.render(node, subContext, subContext.ditaWriter);
                    } finally {
                        subContext.renderingNode = oldNode;
                        subContext.doNotRenderLinksNesting = oldDoNotRenderLinksNesting;
                        subContext.renderingHandlerWrapper = prevWrapper;
                    }
                } else {
                    throw new RuntimeException("No renderer configured for " + node.getClass().getName());
                }
            }
        }

        public void renderChildren(Node parent) {
            renderChildrenNode(parent, this);
        }

        @SuppressWarnings("WeakerAccess")
        protected void renderChildrenNode(Node parent, NodeRendererSubContext subContext) {
            Node node = parent.getFirstChild();
            while (node != null) {
                Node next = node.getNext();
                renderNode(node, subContext);
                node = next;
            }
        }
    }
}
