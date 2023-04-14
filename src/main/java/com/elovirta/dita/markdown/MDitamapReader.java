package com.elovirta.dita.markdown;

import static java.util.Arrays.asList;

import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.DataSet;
import com.vladsch.flexmark.util.data.MutableDataSet;

/**
 * XMLReader implementation for Markdown Lightweight DITA.
 */
public class MDitamapReader extends MarkdownReader {

  public static final DataSet CORE_PROFILE = new MutableDataSet()
    .set(Parser.EXTENSIONS, asList(AnchorLinkExtension.create(), YamlFrontMatterExtension.create()))
    .set(DitaRenderer.MAP, true)
    .set(DitaRenderer.ID_FROM_YAML, true)
    .set(DitaRenderer.MDITA_CORE_PROFILE, true)
    .toImmutable();

  public static final DataSet EXTENDED_PROFILE = new MutableDataSet()
    .set(Parser.EXTENSIONS, asList(AnchorLinkExtension.create(), YamlFrontMatterExtension.create()))
    .set(DitaRenderer.MAP, true)
    .set(DitaRenderer.ID_FROM_YAML, true)
    .set(DitaRenderer.LW_DITA, true)
    .set(DitaRenderer.MDITA_EXTENDED_PROFILE, true)
    .toImmutable();

  public MDitamapReader() {
    super(EXTENDED_PROFILE);
  }
}
