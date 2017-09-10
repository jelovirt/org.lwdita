package com.elovirta.dita.markdown;

import com.elovirta.dita.markdown.renderer.NodeRendererContext;
import com.vladsch.flexmark.ast.Node;

public abstract class NodeRendererSubContext implements NodeRendererContext {
    final DitaWriter ditaWriter;
    Node renderingNode;
    NodeRenderingHandlerWrapper renderingHandlerWrapper;
    int doNotRenderLinksNesting;

    public NodeRendererSubContext(DitaWriter ditaWriter) {
        this.ditaWriter = ditaWriter;
        this.renderingNode = null;
        this.doNotRenderLinksNesting = 0;
    }

    public DitaWriter getDitaWriter() {
        return ditaWriter;
    }

    public void flush() {
        ditaWriter.line().flush();
    }

    public void flush(int maxBlankLines) {
        ditaWriter.line().flush(maxBlankLines);
    }

    protected int getDoNotRenderLinksNesting() {
        return doNotRenderLinksNesting;
    }

    public boolean isDoNotRenderLinks() {
        return doNotRenderLinksNesting != 0;
    }

    public void doNotRenderLinks(boolean doNotRenderLinks) {
        if (doNotRenderLinks) doNotRenderLinks();
        else doRenderLinks();
    }

    public void doNotRenderLinks() {
        this.doNotRenderLinksNesting++;
    }

    public void doRenderLinks() {
        if (this.doNotRenderLinksNesting == 0) throw new IllegalStateException("Not in do not render links context");
        this.doNotRenderLinksNesting--;
    }
}
