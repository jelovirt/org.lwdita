package com.elovirta.dita.markdown;

import com.elovirta.dita.markdown.renderer.NodeRendererFactory;
import com.elovirta.dita.markdown.renderer.HeaderIdGeneratorFactory;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.builder.Extension;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.DataKey;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Builder for configuring an {@link DitaRenderer}. See methods for default configuration.
 */
class Builder extends MutableDataSet {
    List<AttributeProviderFactory> attributeProviderFactories = new ArrayList<AttributeProviderFactory>();
    List<NodeRendererFactory> nodeRendererFactories = new ArrayList<NodeRendererFactory>();
    List<LinkResolverFactory> linkResolverFactories = new ArrayList<LinkResolverFactory>();
    private final HashSet<DitaRenderer.DitaRendererExtension> loadedExtensions = new HashSet<DitaRenderer.DitaRendererExtension>();
    HeaderIdGeneratorFactory ditaIdGeneratorFactory = null;

    public Builder() {
        super();
    }

    public Builder(DataHolder options) {
        super(options);

        if (options.contains(Parser.EXTENSIONS)) {
            extensions(get(Parser.EXTENSIONS));
        }
    }

    public Builder(Builder other) {
        super(other);

        this.attributeProviderFactories.addAll(other.attributeProviderFactories);
        this.nodeRendererFactories.addAll(other.nodeRendererFactories);
        this.linkResolverFactories.addAll(other.linkResolverFactories);
        this.loadedExtensions.addAll(other.loadedExtensions);
        this.ditaIdGeneratorFactory = other.ditaIdGeneratorFactory;
    }

    public Builder(Builder other, DataHolder options) {
        super(other);

        List<Extension> extensions = new ArrayList<Extension>();
        HashSet<Class> extensionSet = new HashSet<Class>();
        for (Extension extension : get(Parser.EXTENSIONS)) {
            extensions.add(extension);
            extensionSet.add(extension.getClass());
        }

        if (options != null) {
            for (DataKey key : options.keySet()) {
                if (key == Parser.EXTENSIONS) {
                    for (Extension extension : options.get(Parser.EXTENSIONS)) {
                        if (!extensionSet.contains(extension.getClass())) {
                            extensions.add(extension);
                        }
                    }
                } else {
                    set(key, options.get(key));
                }
            }
        }

        set(Parser.EXTENSIONS, extensions);
        extensions(extensions);
    }

//    /**
//     * @return the configured {@link DitaRenderer}
//     */
//    public DitaRenderer build() {
//        return new DitaRenderer(this);
//    }

//    /**
//     * The HTML to use for rendering a softbreak, defaults to {@code "\n"} (meaning the rendered result doesn't have
//     * a line break).
//     * <p>
//     * Set it to {@code "<br>"} (or {@code "<br />"} to make them hard breaks.
//     * <p>
//     * Set it to {@code " "} to ignore line wrapping in the source.
//     *
//     * @param softBreak HTML for softbreak
//     * @return {@code this}
//     */
//    public Builder softBreak(String softBreak) {
//        this.set(SOFT_BREAK, softBreak);
//        return this;
//    }
//
//    /**
//     * The size of the indent to use for hierarchical elements, default 0, means no indent, also fastest rendering
//     *
//     * @param indentSize number of spaces per indent
//     * @return {@code this}
//     */
//    public Builder indentSize(int indentSize) {
//        this.set(INDENT_SIZE, indentSize);
//        return this;
//    }
//
//    /**
//     * Whether {@link HtmlInline} and {@link HtmlBlock} should be escaped, defaults to {@code false}.
//     * <p>
//     * Note that {@link HtmlInline} is only a tag itself, not the text between an opening tag and a closing tag. So
//     * markup in the text will be parsed as normal and is not affected by this option.
//     *
//     * @param escapeHtml true for escaping, false for preserving raw HTML
//     * @return {@code this}
//     */
//    public Builder escapeHtml(boolean escapeHtml) {
//        this.set(ESCAPE_HTML, escapeHtml);
//        return this;
//    }

//    /**
//     * Whether URLs of link or images should be percent-encoded, defaults to {@code false}.
//     * <p>
//     * If enabled, the following is done:
//     * <ul>
//     * <li>Existing percent-encoded parts are preserved (e.g. "%20" is kept as "%20")</li>
//     * <li>Reserved characters such as "/" are preserved, except for "[" and "]" (see encodeURI in JS)</li>
//     * <li>Unreserved characters such as "a" are preserved</li>
//     * <li>Other characters such umlauts are percent-encoded</li>
//     * </ul>
//     *
//     * @param percentEncodeUrls true to percent-encode, false for leaving as-is
//     * @return {@code this}
//     */
//    @SuppressWarnings("SameParameterValue")
//    public Builder percentEncodeUrls(boolean percentEncodeUrls) {
//        this.set(PERCENT_ENCODE_URLS, percentEncodeUrls);
//        return this;
//    }

    /**
     * Add an attribute provider for adding/changing HTML attributes to the rendered tags.
     *
     * @param attributeProviderFactory the attribute provider factory to add
     * @return {@code this}
     */
    public Builder attributeProviderFactory(AttributeProviderFactory attributeProviderFactory) {
        this.attributeProviderFactories.add(attributeProviderFactory);
        return this;
    }

    /**
     * Add a factory for instantiating a node renderer (done when rendering). This allows to override the rendering
     * of node types or define rendering for custom node types.
     * <p>
     * If multiple node renderers for the same node type are created, the one from the factory that was added first
     * "wins". (This is how the rendering for core node types can be overridden; the default rendering comes last.)
     *
     * @param nodeRendererFactory the factory for creating a node renderer
     * @return {@code this}
     */
    public Builder nodeRendererFactory(NodeRendererFactory nodeRendererFactory) {
        this.nodeRendererFactories.add(nodeRendererFactory);
        return this;
    }

    /**
     * Add a factory for instantiating a node renderer (done when rendering). This allows to override the rendering
     * of node types or define rendering for custom node types.
     * <p>
     * If multiple node renderers for the same node type are created, the one from the factory that was added first
     * "wins". (This is how the rendering for core node types can be overridden; the default rendering comes last.)
     *
     * @param linkResolverFactory the factory for creating a node renderer
     * @return {@code this}
     */
    public Builder linkResolverFactory(LinkResolverFactory linkResolverFactory) {
        this.linkResolverFactories.add(linkResolverFactory);
        return this;
    }

    /**
     * Add a factory for generating the header id attribute from the header's text
     *
     * @param ditaIdGeneratorFactory the factory for generating header tag id attributes
     * @return {@code this}
     */
    public Builder ditaIdGeneratorFactory(HeaderIdGeneratorFactory ditaIdGeneratorFactory) {
        if (this.ditaIdGeneratorFactory != null) {
            throw new IllegalStateException("custom header id factory is already set to " + ditaIdGeneratorFactory.getClass().getName());
        }
        this.ditaIdGeneratorFactory = ditaIdGeneratorFactory;
        return this;
    }

    /**
     * @param extensions extensions to use on this HTML renderer
     * @return {@code this}
     */
    public Builder extensions(Iterable<? extends Extension> extensions) {
        // first give extensions a chance to modify options
        for (Extension extension : extensions) {
            if (extension instanceof DitaRenderer.DitaRendererExtension) {
                if (!loadedExtensions.contains(extension)) {
                    DitaRenderer.DitaRendererExtension ditaRendererExtension = (DitaRenderer.DitaRendererExtension) extension;
                    ditaRendererExtension.rendererOptions(this);
                }
            }
        }

        for (Extension extension : extensions) {
            if (extension instanceof DitaRenderer.DitaRendererExtension) {
                if (!loadedExtensions.contains(extension)) {
                    DitaRenderer.DitaRendererExtension ditaRendererExtension = (DitaRenderer.DitaRendererExtension) extension;
                    ditaRendererExtension.extend(this, this.get(DitaRenderer.TYPE));
                    loadedExtensions.add(ditaRendererExtension);
                }
            }
        }
        return this;
    }
}