package com.elovirta.dita.markdown;

import com.elovirta.dita.markdown.renderer.LinkResolverContext;
import com.vladsch.flexmark.util.dependency.Dependent;

import java.util.Set;
import java.util.function.Function;

public interface AttributeProviderFactory extends Function<LinkResolverContext, AttributeProvider>, Dependent<AttributeProviderFactory> {
    @Override
    Set<Class<? extends AttributeProviderFactory>> getAfterDependents();

    @Override
    Set<Class<? extends AttributeProviderFactory>> getBeforeDependents();

    @Override
    boolean affectsGlobalScope();

    @Override
    AttributeProvider apply(LinkResolverContext context);
}
