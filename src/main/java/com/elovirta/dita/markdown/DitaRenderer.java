/*
 * Based on ToDitaSerializer (C) 2010-2011 Mathias Doenitz
 */
package com.elovirta.dita.markdown;

import com.elovirta.dita.markdown.renderer.*;
import com.vladsch.flexmark.Extension;
import com.vladsch.flexmark.IRender;
import com.vladsch.flexmark.ast.Document;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.util.collection.DynamicDefaultKey;
import com.vladsch.flexmark.util.dependency.DependencyHandler;
import com.vladsch.flexmark.util.dependency.FlatDependencyHandler;
import com.vladsch.flexmark.util.dependency.ResolvedDependencies;
import com.vladsch.flexmark.util.html.Attributes;
import com.vladsch.flexmark.util.html.Escaping;
import com.vladsch.flexmark.util.options.*;
import com.vladsch.flexmark.util.sequence.TagRange;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.*;

//import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.dita.dost.util.Constants.ATTRIBUTE_PREFIX_DITAARCHVERSION;
import static org.dita.dost.util.Constants.DITA_NAMESPACE;
//import static org.dita.dost.util.Constants.ELEMENT_NAME_DITA;
//import static org.dita.dost.util.XMLSerializer.EMPTY_ATTS;

public class DitaRenderer implements IRender {

    public static final DataKey<Boolean> SHORTDESC_PARAGRAPH = new DataKey<Boolean>("SHORTDESC_PARAGRAPH", false);
    public static final DataKey<String> SOFT_BREAK = new DataKey<String>("SOFT_BREAK", "\n");
    public static final DataKey<String> HARD_BREAK = new DataKey<String>("HARD_BREAK", "<br />\n");
    public static final DataKey<String> STRONG_EMPHASIS_STYLE_HTML_OPEN = new DataKey<String>("STRONG_EMPHASIS_STYLE_HTML_OPEN", (String) null);
    public static final DataKey<String> STRONG_EMPHASIS_STYLE_HTML_CLOSE = new DataKey<String>("STRONG_EMPHASIS_STYLE_HTML_CLOSE", (String) null);
    public static final DataKey<String> EMPHASIS_STYLE_HTML_OPEN = new DataKey<String>("EMPHASIS_STYLE_HTML_OPEN", (String) null);
    public static final DataKey<String> EMPHASIS_STYLE_HTML_CLOSE = new DataKey<String>("EMPHASIS_STYLE_HTML_CLOSE", (String) null);
    public static final DataKey<String> CODE_STYLE_HTML_OPEN = new DataKey<String>("CODE_STYLE_HTML_OPEN", (String) null);
    public static final DataKey<String> CODE_STYLE_HTML_CLOSE = new DataKey<String>("CODE_STYLE_HTML_CLOSE", (String) null);
    public static final DataKey<String> INLINE_CODE_SPLICE_CLASS = new DataKey<String>("INLINE_CODE_SPLICE_CLASS", (String) null);
    public static final DataKey<Boolean> PERCENT_ENCODE_URLS = new DataKey<Boolean>("ESCAPE_HTML", false);
    public static final DataKey<Integer> INDENT_SIZE = new DataKey<Integer>("INDENT", 0);
    public static final DataKey<Boolean> ESCAPE_HTML = new DataKey<Boolean>("ESCAPE_HTML", false);
    public static final DataKey<Boolean> ESCAPE_HTML_BLOCKS = new DynamicDefaultKey<Boolean>("ESCAPE_HTML_BLOCKS", holder -> ESCAPE_HTML.getFrom(holder));
    public static final DataKey<Boolean> ESCAPE_HTML_COMMENT_BLOCKS = new DynamicDefaultKey<Boolean>("ESCAPE_HTML_COMMENT_BLOCKS", holder -> ESCAPE_HTML_BLOCKS.getFrom(holder));
    public static final DataKey<Boolean> ESCAPE_INLINE_HTML = new DynamicDefaultKey<Boolean>("ESCAPE_HTML_BLOCKS", holder -> ESCAPE_HTML.getFrom(holder));
    public static final DataKey<Boolean> ESCAPE_INLINE_HTML_COMMENTS = new DynamicDefaultKey<Boolean>("ESCAPE_INLINE_HTML_COMMENTS", holder -> ESCAPE_INLINE_HTML.getFrom(holder));
    public static final DataKey<Boolean> SUPPRESS_HTML = new DataKey<Boolean>("SUPPRESS_HTML", false);
    public static final DataKey<Boolean> SUPPRESS_HTML_BLOCKS = new DynamicDefaultKey<Boolean>("SUPPRESS_HTML_BLOCKS", holder -> SUPPRESS_HTML.getFrom(holder));
    public static final DataKey<Boolean> SUPPRESS_HTML_COMMENT_BLOCKS = new DynamicDefaultKey<Boolean>("SUPPRESS_HTML_COMMENT_BLOCKS", holder -> SUPPRESS_HTML_BLOCKS.getFrom(holder));
    public static final DataKey<Boolean> SUPPRESS_INLINE_HTML = new DynamicDefaultKey<Boolean>("SUPPRESS_INLINE_HTML", holder -> SUPPRESS_HTML.getFrom(holder));
    public static final DataKey<Boolean> SUPPRESS_INLINE_HTML_COMMENTS = new DynamicDefaultKey<Boolean>("SUPPRESS_INLINE_HTML_COMMENTS", holder -> SUPPRESS_INLINE_HTML.getFrom(holder));
    public static final DataKey<Boolean> SOURCE_WRAP_HTML = new DataKey<Boolean>("SOURCE_WRAP_HTML", false);
    public static final DataKey<Boolean> SOURCE_WRAP_HTML_BLOCKS = new DynamicDefaultKey<Boolean>("SOURCE_WRAP_HTML_BLOCKS", holder -> SOURCE_WRAP_HTML.getFrom(holder));
    //public static final DataKey<Boolean> SOURCE_WRAP_INLINE_HTML = new DynamicDefaultKey<>("SOURCE_WRAP_INLINE_HTML", SOURCE_WRAP_HTML::getFrom);
    public static final DataKey<Boolean> HEADER_ID_GENERATOR_RESOLVE_DUPES = new DataKey<Boolean>("HEADER_ID_GENERATOR_RESOLVE_DUPES", true);
    public static final DataKey<String> HEADER_ID_GENERATOR_TO_DASH_CHARS = new DataKey<String>("HEADER_ID_GENERATOR_TO_DASH_CHARS", " -_");
    public static final DataKey<Boolean> HEADER_ID_GENERATOR_NO_DUPED_DASHES = new DataKey<Boolean>("HEADER_ID_GENERATOR_NO_DUPED_DASHES", false);
    public static final DataKey<Boolean> RENDER_HEADER_ID = new DataKey<Boolean>("RENDER_HEADER_ID", false);
    public static final DataKey<Boolean> GENERATE_HEADER_ID = new DataKey<Boolean>("GENERATE_HEADER_ID", true);
    public static final DataKey<Boolean> DO_NOT_RENDER_LINKS = new DataKey<Boolean>("DO_NOT_RENDER_LINKS", false);
    public static final DataKey<String> FENCED_CODE_LANGUAGE_CLASS_PREFIX = new DataKey<String>("FENCED_CODE_LANGUAGE_CLASS_PREFIX", "language-");
    public static final DataKey<String> FENCED_CODE_NO_LANGUAGE_CLASS = new DataKey<String>("FENCED_CODE_NO_LANGUAGE_CLASS", "");
    public static final DataKey<String> SOURCE_POSITION_ATTRIBUTE = new DataKey<String>("SOURCE_POSITION_ATTRIBUTE", "");
    public static final DataKey<Boolean> SOURCE_POSITION_PARAGRAPH_LINES = new DataKey<Boolean>("SOURCE_POSITION_PARAGRAPH_LINES", false);
    public static final DataKey<String> TYPE = new DataKey<String>("TYPE", "HTML");
    public static final DataKey<ArrayList<TagRange>> TAG_RANGES = new DataKey<ArrayList<TagRange>>("TAG_RANGES", value -> new ArrayList<TagRange>());

    public static final DataKey<Boolean> RECHECK_UNDEFINED_REFERENCES = new DataKey<Boolean>("RECHECK_UNDEFINED_REFERENCES", false);
    public static final DataKey<Boolean> OBFUSCATE_EMAIL = new DataKey<Boolean>("OBFUSCATE_EMAIL", false);
    public static final DataKey<Boolean> OBFUSCATE_EMAIL_RANDOM = new DataKey<Boolean>("OBFUSCATE_EMAIL_RANDOM", true);
    public static final DataKey<Boolean> HTML_BLOCK_OPEN_TAG_EOL = new DataKey<Boolean>("HTML_BLOCK_OPEN_TAG_EOL", true);
    public static final DataKey<Boolean> HTML_BLOCK_CLOSE_TAG_EOL = new DataKey<Boolean>("HTML_BLOCK_CLOSE_TAG_EOL", true);
    public static final DataKey<Boolean> UNESCAPE_HTML_ENTITIES = new DataKey<Boolean>("UNESCAPE_HTML_ENTITIES", true);

    public static final DataKey<Integer> FORMAT_FLAGS = new DataKey<Integer>("FORMAT_FLAGS", 0);
    public static final DataKey<Integer> MAX_TRAILING_BLANK_LINES = new DataKey<Integer>("MAX_TRAILING_BLANK_LINES", 1);

    private final List<AttributeProviderFactory> attributeProviderFactories;
    private final List<DelegatingNodeRendererFactoryWrapper> nodeRendererFactories;
    private final List<LinkResolverFactory> linkResolverFactories;
    private final HeaderIdGeneratorFactory ditaIdGeneratorFactory;
    private final DitaRendererOptions ditaOptions;
    private final DataHolder options;
    private final Builder builder;
    private final Map<String, List<String>> metadata;

    DitaRenderer(Builder builder, Map<String, List<String>> metadata) {
//        final ContentHandler contentHandler, final Map<String, Object> documentMetadata
//        this.documentMetadata = documentMetadata;
//        setContentHandler(contentHandler);
//        metadataSerializer = new MetadataSerializerImpl();
//        metadataSerializer.setContentHandler(contentHandler);

        //////////////
        this.builder = new Builder(builder); // take a copy to avoid after creation side effects
        this.options = new DataSet(builder);
        this.ditaOptions = new DitaRendererOptions(this.options);
        this.metadata = metadata;

        this.ditaIdGeneratorFactory = builder.ditaIdGeneratorFactory;

        // resolve renderer dependencies
        final List<DelegatingNodeRendererFactoryWrapper> nodeRenderers = new ArrayList<DelegatingNodeRendererFactoryWrapper>(builder.nodeRendererFactories.size());

        for (int i = builder.nodeRendererFactories.size() - 1; i >= 0; i--) {
            final NodeRendererFactory nodeRendererFactory = builder.nodeRendererFactories.get(i);
            final Set<Class<? extends DelegatingNodeRendererFactoryWrapper>>[] myDelegates = new Set[] { null };

            nodeRenderers.add(new DelegatingNodeRendererFactoryWrapper(nodeRenderers, nodeRendererFactory));
        }

        // Add as last. This means clients can override the rendering of core nodes if they want by default
        final CoreNodeRenderer.Factory nodeRendererFactory = new CoreNodeRenderer.Factory();
        nodeRenderers.add(new DelegatingNodeRendererFactoryWrapper(nodeRenderers, nodeRendererFactory));

        DitaRenderer.RendererDependencyHandler resolver = new DitaRenderer.RendererDependencyHandler();
        nodeRendererFactories = resolver.resolveDependencies(nodeRenderers).getNodeRendererFactories();

        this.attributeProviderFactories = FlatDependencyHandler.computeDependencies(builder.attributeProviderFactories);
        this.linkResolverFactories = FlatDependencyHandler.computeDependencies(builder.linkResolverFactories);
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
//        renderer.flush(ditaOptions.maxTrailingBlankLines);
    }

    /**
     * Extension for {@link DitaRenderer}.
     * <p>
     * This should be implemented by all extensions that have DitaRenderer extension code.
     * <p>
     * Each extension will have its {@link DitaRenderer.DitaRendererExtension#extend(Builder, String)} method called.
     * and should call back on the builder argument to register all extension points
     */
    public interface DitaRendererExtension extends Extension {
        /**
         * This method is called first on all extensions so that they can adjust the options that must be
         * common to all extensions.
         *
         * @param options option set that will be used for the builder
         */
        void rendererOptions(MutableDataHolder options);

        /**
         * Called to give each extension to register extension points that it contains
         *
         * @param rendererBuilder builder to call back for extension point registration
         * @param rendererType    type of rendering being performed. For now "HTML", "JIRA" or "YOUTRACK"
         * @see Builder#attributeProviderFactory(AttributeProviderFactory)
         * @see Builder#nodeRendererFactory(NodeRendererFactory)
         * @see Builder#linkResolverFactory(LinkResolverFactory)
         * @see Builder#ditaIdGeneratorFactory(HeaderIdGeneratorFactory)
         */
        void extend(Builder rendererBuilder, String rendererType);
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
            List<DelegatingNodeRendererFactoryWrapper> blockPreProcessorFactories = new ArrayList<DelegatingNodeRendererFactoryWrapper>();
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

        private final List<PhasedNodeRenderer> phasedRenderers;
        private final LinkResolver[] myLinkResolvers;
        private final Set<RenderingPhase> renderingPhases;
        private final DataHolder options;
        private RenderingPhase phase;
        private final DitaIdGenerator ditaIdGenerator;
        private final HashMap<LinkType, HashMap<String, ResolvedLink>> resolvedLinkMap = new HashMap<LinkType, HashMap<String, ResolvedLink>>();
        private final AttributeProvider[] attributeProviders;

        MainNodeRenderer(DataHolder options, DitaWriter ditaWriter, Document document) {
            super(ditaWriter);
            this.options = new ScopedDataSet(options, document);
            this.document = document;
            this.renderers = new HashMap<>(32);
            this.renderingPhases = new HashSet<>(RenderingPhase.values().length);
            this.phasedRenderers = new ArrayList<>(nodeRendererFactories.size());
            this.myLinkResolvers = new LinkResolver[linkResolverFactories.size()];
            this.doNotRenderLinksNesting = ditaOptions.doNotRenderLinksInDocument ? 0 : 1;
            this.ditaIdGenerator = ditaIdGeneratorFactory != null ? ditaIdGeneratorFactory.create(this)
                    : (!(ditaOptions.renderHeaderId || ditaOptions.generateHeaderIds) ? DitaIdGenerator.NULL : new HeaderIdGenerator.Factory().create(this));

            ditaWriter.setContext(this);

            for (int i = nodeRendererFactories.size() - 1; i >= 0; i--) {
                NodeRendererFactory nodeRendererFactory = nodeRendererFactories.get(i);
                NodeRenderer nodeRenderer = nodeRendererFactory.create(this.getOptions());
                for (NodeRenderingHandler nodeType : nodeRenderer.getNodeRenderingHandlers()) {
                    // Overwrite existing renderer
                    NodeRenderingHandlerWrapper handlerWrapper = new NodeRenderingHandlerWrapper(nodeType, renderers.get(nodeType.getNodeType()));
                    renderers.put(nodeType.getNodeType(), handlerWrapper);
                }

                if (nodeRenderer instanceof PhasedNodeRenderer) {
                    this.renderingPhases.addAll(((PhasedNodeRenderer) nodeRenderer).getRenderingPhases());
                    this.phasedRenderers.add((PhasedNodeRenderer) nodeRenderer);
                }
            }

            for (int i = 0; i < linkResolverFactories.size(); i++) {
                myLinkResolvers[i] = linkResolverFactories.get(i).create(this);
            }

            this.attributeProviders = new AttributeProvider[attributeProviderFactories.size()];
            for (int i = 0; i < attributeProviderFactories.size(); i++) {
                attributeProviders[i] = attributeProviderFactories.get(i).create(this);
            }
        }

        @Override
        public Node getCurrentNode() {
            return renderingNode;
        }

        @Override
        public ResolvedLink resolveLink(LinkType linkType, CharSequence url, Boolean urlEncode) {
            return resolveLink(linkType, url, (Attributes) null, urlEncode);
        }

        @Override
        public ResolvedLink resolveLink(LinkType linkType, CharSequence url, Attributes attributes, Boolean urlEncode) {
            HashMap<String, ResolvedLink> resolvedLinks = resolvedLinkMap.get(linkType);
            if (resolvedLinks == null) {
                resolvedLinks = new HashMap<String, ResolvedLink>();
                resolvedLinkMap.put(linkType, resolvedLinks);
            }

            String urlSeq = url instanceof String ? (String) url : String.valueOf(url);
            ResolvedLink resolvedLink = resolvedLinks.get(urlSeq);
            if (resolvedLink == null) {
                resolvedLink = new ResolvedLink(linkType, urlSeq, attributes);

                if (!urlSeq.isEmpty()) {
                    Node currentNode = getCurrentNode();

                    for (LinkResolver linkResolver : myLinkResolvers) {
                        resolvedLink = linkResolver.resolveLink(currentNode, this, resolvedLink);
                        if (resolvedLink.getStatus() != LinkStatus.UNKNOWN) break;
                    }

                    if (urlEncode == null && ditaOptions.percentEncodeUrls || urlEncode != null && urlEncode) {
                        resolvedLink = resolvedLink.withUrl(Escaping.percentEncodeUrl(resolvedLink.getUrl()));
                    }
                }

                // put it in the map
                resolvedLinks.put(urlSeq, resolvedLink);
            }

            return resolvedLink;
        }

        @Override
        public String getNodeId(Node node) {
            String id = ditaIdGenerator.getId(node);
            if (attributeProviderFactories.size() != 0) {

                Attributes attributes = new Attributes();
                if (id != null) attributes.replaceValue("id", id);

                for (AttributeProvider attributeProvider : attributeProviders) {
                    attributeProvider.setAttributes(this.renderingNode, AttributablePart.ID, attributes);
                }
                id = attributes.getValue("id");
            }
            return id;
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
        public RenderingPhase getRenderingPhase() {
            return phase;
        }

        @Override
        public String encodeUrl(CharSequence url) {
            if (ditaOptions.percentEncodeUrls) {
                return Escaping.percentEncodeUrl(url);
            } else {
                return url instanceof String ? (String) url : String.valueOf(url);
            }
        }

        @Override
        public Attributes extendRenderingNodeAttributes(AttributablePart part, Attributes attributes) {
            Attributes attr = attributes != null ? attributes : new Attributes();
            for (AttributeProvider attributeProvider : attributeProviders) {
                attributeProvider.setAttributes(this.renderingNode, part, attr);
            }
            return attr;
        }

        @Override
        public void render(Node node) {
            try {
//            clean(astRoot);
//            final boolean isCompound = hasMultipleTopLevelHeaders(astRoot);
                ditaWriter.contentHandler.startDocument();
                ditaWriter.contentHandler.startPrefixMapping(ATTRIBUTE_PREFIX_DITAARCHVERSION, DITA_NAMESPACE);
//            if (isCompound) {
//                contentHandler.startElement(NULL_NS_URI, ELEMENT_NAME_DITA, ELEMENT_NAME_DITA, EMPTY_ATTS);
//            }
                try {
                    renderNode(node, this);
                    ditaWriter.close();
                } catch (final ParseException e) {
                    //e.printStackTrace();
                    throw new SAXException("Failed to parse Markdown: " + e.getMessage(), e);
                }
//            if (isCompound) {
//                contentHandler.endElement(NULL_NS_URI, ELEMENT_NAME_DITA, ELEMENT_NAME_DITA);
//            }
                ditaWriter.contentHandler.endPrefixMapping(ATTRIBUTE_PREFIX_DITAARCHVERSION);
                ditaWriter.contentHandler.endDocument();
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void delegateRender() {
            renderByPreviousHandler(this);
        }

        void renderByPreviousHandler(NodeRendererSubContext subContext) {
            if (subContext.renderingNode != null) {
                NodeRenderingHandlerWrapper nodeRenderer = subContext.renderingHandlerWrapper.myPreviousRenderingHandler;
                if (nodeRenderer != null) {
                    Node oldNode = subContext.renderingNode;
                    int oldDoNotRenderLinksNesting = subContext.doNotRenderLinksNesting;
                    NodeRenderingHandlerWrapper prevWrapper = subContext.renderingHandlerWrapper;
                    try {
                        subContext.renderingHandlerWrapper = nodeRenderer;
                        nodeRenderer.myRenderingHandler.render(oldNode, subContext, subContext.ditaWriter);
                    } finally {
                        subContext.renderingNode = oldNode;
                        subContext.doNotRenderLinksNesting = oldDoNotRenderLinksNesting;
                        subContext.renderingHandlerWrapper = prevWrapper;
                    }
                }
            } else {
                throw new IllegalStateException("renderingByPreviousHandler called outside node rendering code");
            }
        }

        @Override
        public NodeRendererContext getSubContext(Appendable out, boolean inheritIndent) {
            throw new UnsupportedOperationException();
//            DitaWriter ditaWriter = new DitaWriter(getDitaWriter(), out, inheritIndent);
//            ditaWriter.setContext(this);
//            //noinspection ReturnOfInnerClass
//            return new DitaRenderer.MainNodeRenderer.SubNodeRenderer(this, ditaWriter, false);
        }

        @Override
        public NodeRendererContext getDelegatedSubContext(final Appendable out, final boolean inheritIndent) {
            throw new UnsupportedOperationException();
//            DitaWriter ditaWriter = new DitaWriter(getDitaWriter(), out, inheritIndent);
//            ditaWriter.setContext(this);
//            //noinspection ReturnOfInnerClass
//            return new DitaRenderer.MainNodeRenderer.SubNodeRenderer(this, ditaWriter, true);
        }

        void renderNode(Node node, NodeRendererSubContext subContext) {
            if (node instanceof Document) {
                // here we render multiple phases
                int oldDoNotRenderLinksNesting = subContext.getDoNotRenderLinksNesting();
                int documentDoNotRenderLinksNesting = getDitaOptions().doNotRenderLinksInDocument ? 1 : 0;
                this.ditaIdGenerator.generateIds(document);

                for (RenderingPhase phase : RenderingPhase.values()) {
                    if (phase != RenderingPhase.BODY && !renderingPhases.contains(phase)) { continue; }
                    this.phase = phase;
                    // here we render multiple phases

                    // go through all renderers that want this phase
                    for (PhasedNodeRenderer phasedRenderer : phasedRenderers) {
                        if (phasedRenderer.getRenderingPhases().contains(phase)) {
                            subContext.doNotRenderLinksNesting = documentDoNotRenderLinksNesting;
                            subContext.renderingNode = node;
                            phasedRenderer.renderDocument(subContext, subContext.ditaWriter, (Document) node, phase);
                            subContext.renderingNode = null;
                            subContext.doNotRenderLinksNesting = oldDoNotRenderLinksNesting;
                        }
                    }

                    if (getRenderingPhase() == RenderingPhase.BODY) {
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

        @SuppressWarnings("WeakerAccess")
        private class SubNodeRenderer extends NodeRendererSubContext implements NodeRendererContext {
            private final DitaRenderer.MainNodeRenderer myMainNodeRenderer;

            public SubNodeRenderer(DitaRenderer.MainNodeRenderer mainNodeRenderer, DitaWriter ditaWriter, final boolean inheritCurrentHandler) {
                super(ditaWriter);
                myMainNodeRenderer = mainNodeRenderer;
                doNotRenderLinksNesting = mainNodeRenderer.getDitaOptions().doNotRenderLinksInDocument ? 1 : 0;
                if (inheritCurrentHandler) {
                    renderingNode = mainNodeRenderer.renderingNode;
                    renderingHandlerWrapper = mainNodeRenderer.renderingHandlerWrapper;
                }
            }

            @Override
            public String getNodeId(Node node) {return myMainNodeRenderer.getNodeId(node);}

            @Override
            public DataHolder getOptions() {return myMainNodeRenderer.getOptions();}

            @Override
            public DitaRendererOptions getDitaOptions() {return myMainNodeRenderer.getDitaOptions();}

            @Override
            public Document getDocument() {return myMainNodeRenderer.getDocument();}

            @Override
            public RenderingPhase getRenderingPhase() {return myMainNodeRenderer.getRenderingPhase();}

            @Override
            public String encodeUrl(CharSequence url) {return myMainNodeRenderer.encodeUrl(url);}

            @Override
            public Attributes extendRenderingNodeAttributes(AttributablePart part, Attributes attributes) {
                return myMainNodeRenderer.extendRenderingNodeAttributes(
                        part,
                        attributes
                );
            }

            @Override
            public void render(Node node) {
                myMainNodeRenderer.renderNode(node, this);
            }

            @Override
            public void delegateRender() {
                myMainNodeRenderer.renderByPreviousHandler(this);
            }

            @Override
            public Node getCurrentNode() {
                return myMainNodeRenderer.getCurrentNode();
            }

            @Override
            public ResolvedLink resolveLink(LinkType linkType, CharSequence url, Boolean urlEncode) {
                return myMainNodeRenderer.resolveLink(linkType, url, urlEncode);
            }

            @Override
            public ResolvedLink resolveLink(LinkType linkType, CharSequence url, Attributes attributes, Boolean urlEncode) {
                return myMainNodeRenderer.resolveLink(linkType, url, attributes, urlEncode);
            }

            @Override
            public NodeRendererContext getSubContext(Appendable out, boolean inheritIndent) {
                throw new UnsupportedOperationException();
//                DitaWriter ditaWriter = new DitaWriter(this.ditaWriter, out, inheritIndent);
//                ditaWriter.setContext(this);
//                //noinspection ReturnOfInnerClass
//                return new DitaRenderer.MainNodeRenderer.SubNodeRenderer(myMainNodeRenderer, ditaWriter, false);
            }

            @Override
            public NodeRendererContext getDelegatedSubContext(final Appendable out, final boolean inheritIndent) {
                throw new UnsupportedOperationException();
//                DitaWriter ditaWriter = new DitaWriter(this.ditaWriter, out, inheritIndent);
//                ditaWriter.setContext(this);
//                //noinspection ReturnOfInnerClass
//                return new DitaRenderer.MainNodeRenderer.SubNodeRenderer(myMainNodeRenderer, ditaWriter, true);
            }

            @Override
            public void renderChildren(Node parent) {
                myMainNodeRenderer.renderChildrenNode(parent, this);
            }

            @Override
            public DitaWriter getDitaWriter() { return ditaWriter; }

            protected int getDoNotRenderLinksNesting() {return super.getDoNotRenderLinksNesting();}

            @Override
            public boolean isDoNotRenderLinks() {return super.isDoNotRenderLinks();}

            @Override
            public void doNotRenderLinks(boolean doNotRenderLinks) {super.doNotRenderLinks(doNotRenderLinks);}

            @Override
            public void doNotRenderLinks() {super.doNotRenderLinks();}

            @Override
            public void doRenderLinks() {super.doRenderLinks();}
        }
    }


}
