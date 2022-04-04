package com.elovirta.dita.markdown;

import com.elovirta.dita.markdown.renderer.DelegatingNodeRendererFactory;
import com.elovirta.dita.markdown.renderer.NodeRenderer;
import com.elovirta.dita.markdown.renderer.NodeRendererFactory;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.dependency.Dependent;

import java.util.Set;
import java.util.function.Function;

/**
 * Factory for instantiating new node renderers with dependencies
 */
class DelegatingNodeRendererFactoryWrapper implements Function<DataHolder, NodeRenderer>, Dependent<DelegatingNodeRendererFactoryWrapper>, DelegatingNodeRendererFactory {
    private final NodeRendererFactory nodeRendererFactory;
    private Set<Class> myDelegates = null;

    public DelegatingNodeRendererFactoryWrapper(final NodeRendererFactory nodeRendererFactory) {
        this.nodeRendererFactory = nodeRendererFactory;
    }

    @Override
    public NodeRenderer apply(DataHolder options) {
        return nodeRendererFactory.apply(options);
    }

    public NodeRendererFactory getFactory() {
        return nodeRendererFactory;
    }

    @Override
    public Set<Class<? extends NodeRendererFactory>> getDelegates() {
        return nodeRendererFactory instanceof DelegatingNodeRendererFactory
                ? ((DelegatingNodeRendererFactory) nodeRendererFactory).getDelegates()
                : null;
    }

    @Override
    public final Set<? extends Class> getAfterDependents() {
        return null;
    }

    @Override
    public Set<? extends Class> getBeforeDependents() {
        return myDelegates;
    }

    @Override
    public final boolean affectsGlobalScope() {
        return false;
    }
}
