package com.elovirta.dita.markdown;

import com.elovirta.dita.markdown.renderer.HeaderIdGeneratorFactory;
import com.elovirta.dita.markdown.renderer.NodeRendererFactory;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.builder.Extension;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Builder for configuring an {@link DitaRenderer}. See methods for default configuration.
 */
class Builder extends MutableDataSet {
    List<AttributeProviderFactory> attributeProviderFactories = new ArrayList<>();
    List<NodeRendererFactory> nodeRendererFactories = new ArrayList<>();
    List<LinkResolverFactory> linkResolverFactories = new ArrayList<>();
    private final HashSet<DitaRenderer.DitaRendererExtension> loadedExtensions = new HashSet<>();
    HeaderIdGeneratorFactory ditaIdGeneratorFactory = null;

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