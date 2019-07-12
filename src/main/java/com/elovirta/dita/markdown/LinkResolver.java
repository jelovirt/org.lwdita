package com.elovirta.dita.markdown;

import com.elovirta.dita.markdown.renderer.NodeRendererContext;
import com.elovirta.dita.markdown.renderer.ResolvedLink;
import com.vladsch.flexmark.util.ast.Node;

public interface LinkResolver {
    ResolvedLink resolveLink(Node node, NodeRendererContext context, ResolvedLink link);
}
