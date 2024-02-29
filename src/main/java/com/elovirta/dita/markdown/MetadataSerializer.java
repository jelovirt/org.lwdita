package com.elovirta.dita.markdown;

import com.elovirta.dita.markdown.renderer.NodeRendererContext;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterBlock;

public interface MetadataSerializer {
  void render(final YamlFrontMatterBlock node, final NodeRendererContext context, final SaxWriter out);
}
