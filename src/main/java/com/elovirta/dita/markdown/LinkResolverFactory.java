package com.elovirta.dita.markdown;

import com.elovirta.dita.markdown.renderer.NodeRendererContext;
import com.vladsch.flexmark.util.ComputableFactory;
import com.vladsch.flexmark.util.dependency.Dependent;

import java.util.Set;

public interface LinkResolverFactory extends ComputableFactory<LinkResolver, NodeRendererContext>, Dependent<LinkResolverFactory> {
    @Override
    Set<Class<? extends LinkResolverFactory>> getAfterDependents();
    
    @Override
    Set<Class<? extends LinkResolverFactory>> getBeforeDependents();

    @Override
    boolean affectsGlobalScope();

    @Override
    LinkResolver create(NodeRendererContext context);
}
