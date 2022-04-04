package com.elovirta.dita.markdown.renderer;

import com.elovirta.dita.markdown.DitaRendererOptions;
import com.elovirta.dita.markdown.DitaWriter;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.html.Attributes;

/**
 * The context for node rendering, including configuration and functionality for the node renderer to use.
 */
public interface NodeRendererContext extends LinkResolverContext {

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
     * Increment the do not render links in this context. This value will persist for the duration of the current node's render() method
     * and will be restored upon return. Effectively it will persist for the rendering of the children of this node.
     *
     * @see #doNotRenderLinks()
     * @see #doRenderLinks()
     */
    void doNotRenderLinks();

    /**
     * Decrement the do not render links in this context. This value will persist for the duration of the current node's render() method
     * and will be restored upon return. Effectively it will persist for the rendering of the children of this node.
     *
     * @throws IllegalStateException if the current doNotRender links value is 0.
     * @see #doNotRenderLinks()
     * @see #doRenderLinks()
     */
    void doRenderLinks();

    /**
     * @return current rendering phase
     */
    RenderingPhase getRenderingPhase();

    /**
     * Get the current rendering context {@link DataHolder}. These are the options passed or set on the {@link DitaRenderer#builder()} or passed to {@link DitaRenderer#builder(DataHolder)}.
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

    /**
     * @return the current node being rendered
     */
    Node getCurrentNode();

    /**
     * Resolve link for rendering. Link Resolvers are going to be called until one returns ResolvedLink with getStatus() != LinkStatus.Unknown
     * <p>
     * A resolver can replace the url but not change the status letting downstream resolvers handle the rest.
     * This is useful when a resolver does partial processing like macro expansion but does not know how to handle the rest.
     * <p>
     * Core processing will simply pass the link as is. It is up to extension LinkResolvers and AttributeProviders to make sense of the link and applicable attributes based on status.
     *
     * @param linkType   type of link being rendered. Core defined links are Link, Image. Extensions can define their own
     * @param url        link url text
     * @param attributes link attributes
     * @param urlEncode  whether the link should be url encoded, if null then the value of {@link DitaRenderer#PERCENT_ENCODE_URLS} will be used to determine whether the resolved URL is to be encoded.
     * @return resolved link url for this link and its resolved status
     */
    ResolvedLink resolveLink(LinkType linkType, CharSequence url, Attributes attributes, Boolean urlEncode);
}
