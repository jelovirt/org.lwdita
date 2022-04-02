package com.elovirta.dita.markdown;

import com.elovirta.dita.markdown.renderer.LinkResolverContext;
//import com.elovirta.dita.markdown.renderer.NodeRendererContext;
import com.vladsch.flexmark.util.dependency.Dependent;

import java.util.Set;
import java.util.function.Function;

public interface LinkResolverFactory extends Function<LinkResolverContext, LinkResolver>, Dependent<LinkResolverFactory> {
    @Override
    Set<Class<? extends LinkResolverFactory>> getAfterDependents();

    @Override
    Set<Class<? extends LinkResolverFactory>> getBeforeDependents();

    @Override
    boolean affectsGlobalScope();

    @Override
    LinkResolver apply(LinkResolverContext context);
}
