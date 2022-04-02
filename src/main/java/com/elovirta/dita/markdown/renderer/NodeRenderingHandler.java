package com.elovirta.dita.markdown.renderer;

import com.elovirta.dita.markdown.CustomNodeRenderer;
import com.elovirta.dita.markdown.DitaWriter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeAdaptingVisitHandler;

public class NodeRenderingHandler<N extends Node> extends NodeAdaptingVisitHandler<N, CustomNodeRenderer<N>> implements CustomNodeRenderer<Node> {
    public NodeRenderingHandler(Class<? extends N> aClass, CustomNodeRenderer<N> adapter) {
        super(aClass, adapter);
    }

    @Override
    public void render(Node node, NodeRendererContext context, DitaWriter ditaWriter) {
        //noinspection unchecked
        myAdapter.render((N) node, context, ditaWriter);
    }
}
