package com.elovirta.dita.markdown.renderer;

import com.elovirta.dita.markdown.SaxWriter;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.visitor.AstAction;
import com.vladsch.flexmark.util.visitor.AstHandler;

public class NodeRenderingHandler<N extends Node> extends AstHandler<N, NodeRenderingHandler.CustomNodeRenderer<N>> {

  public NodeRenderingHandler(Class<? extends N> aClass, CustomNodeRenderer<N> adapter) {
    super(aClass, adapter);
  }

  public void render(Node node, NodeRendererContext context, SaxWriter saxWriter) {
    //noinspection unchecked
    getAdapter().render((N) node, context, saxWriter);
  }

  public interface CustomNodeRenderer<N extends Node> extends AstAction<N> {
    void render(N node, NodeRendererContext context, SaxWriter html);
  }
}
