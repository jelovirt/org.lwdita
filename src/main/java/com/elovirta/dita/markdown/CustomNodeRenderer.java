package com.elovirta.dita.markdown;

import com.elovirta.dita.markdown.renderer.NodeRendererContext;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.ast.NodeAdaptingVisitor;

public interface CustomNodeRenderer<N extends Node> extends NodeAdaptingVisitor<N> {
    void render(N node, NodeRendererContext context, DitaWriter html);
}
