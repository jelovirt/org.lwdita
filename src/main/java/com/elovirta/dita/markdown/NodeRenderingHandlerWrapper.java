package com.elovirta.dita.markdown;

import com.elovirta.dita.markdown.renderer.NodeRenderingHandler;

class NodeRenderingHandlerWrapper {
    public final NodeRenderingHandler myRenderingHandler;
    public final NodeRenderingHandlerWrapper myPreviousRenderingHandler;

    public NodeRenderingHandlerWrapper(final NodeRenderingHandler renderingHandler, final NodeRenderingHandlerWrapper previousRenderingHandler) {
        myRenderingHandler = renderingHandler;
        myPreviousRenderingHandler = previousRenderingHandler;
    }
}
