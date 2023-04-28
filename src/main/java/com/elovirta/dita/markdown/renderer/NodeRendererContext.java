package com.elovirta.dita.markdown.renderer;

import com.elovirta.dita.markdown.DitaRendererOptions;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;

/**
 * The context for node rendering, including configuration and functionality for the node renderer to use.
 */
public interface NodeRendererContext {
  /**
   * Render the specified node and its children using the configured renderers. This should be used to render child
   * nodes; be careful not to pass the node that is being rendered, that would result in an endless loop.
   *
   * @param node the node to render
   */
  void render(Node node);

  /**
   * Render the children of the node, used by custom renderers
   *
   * @param parent node the children of which are to be rendered
   */
  void renderChildren(Node parent);

  /**
   * Render the node, used by custom renderers
   *
   * @param node node to be rendered
   */
  void renderChild(Node node);

  /**
   * Get the current rendering context {@link DataHolder}.
   * To get the document options you should use {@link #getDocument()} as the data holder.
   *
   * @return the current renderer options {@link DataHolder}
   */
  DataHolder getOptions();

  /**
   * @return the {@link DitaRendererOptions} for the context.
   */
  DitaRendererOptions getDitaOptions();

  /**
   * @return the {@link Document} node of the current context
   */
  Document getDocument();
}
