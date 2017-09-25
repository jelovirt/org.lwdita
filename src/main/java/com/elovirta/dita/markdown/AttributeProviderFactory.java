package com.elovirta.dita.markdown;

import com.elovirta.dita.markdown.renderer.NodeRendererContext;
import com.vladsch.flexmark.util.ComputableFactory;
import com.vladsch.flexmark.util.dependency.Dependent;

import java.util.Set;

public interface AttributeProviderFactory extends ComputableFactory<AttributeProvider, NodeRendererContext>, Dependent<AttributeProviderFactory> {
    @Override
    Set<Class<? extends AttributeProviderFactory>> getAfterDependents();
    
    @Override
    Set<Class<? extends AttributeProviderFactory>> getBeforeDependents();

    @Override
    boolean affectsGlobalScope();

    @Override
    AttributeProvider create(NodeRendererContext context);
}
