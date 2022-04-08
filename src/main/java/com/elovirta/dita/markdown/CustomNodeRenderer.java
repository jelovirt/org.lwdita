package com.elovirta.dita.markdown;

import com.elovirta.dita.markdown.renderer.NodeRendererContext;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeAdaptingVisitor;

public interface CustomNodeRenderer<N extends Node> extends NodeAdaptingVisitor<N> {
    void render(N node, NodeRendererContext context, SaxWriter html);
}
