package com.elovirta.dita.markdown;

import static java.util.Arrays.asList;

import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.jekyll.tag.JekyllTagExtension;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.DataSet;
import com.vladsch.flexmark.util.data.MutableDataSet;

/**
 * XMLReader implementation for Markdown Lightweight DITA.
 */
public class MDitaReader extends MarkdownReader {

  public static final DataSet CORE_PROFILE = new MutableDataSet()
    .set(
      Parser.EXTENSIONS,
      asList(
        //                    AbbreviationExtension.create(),
        AnchorLinkExtension.create(),
        //                    FootnoteExtension.create(),
        //                    InsExtension.create(),
        JekyllTagExtension.create(),
        //                    SuperscriptExtension.create(),
        TablesExtension.create(),
        //                    AutolinkExtension.create(),
        YamlFrontMatterExtension.create()
        //                    DefinitionExtension.create(),
        //                    StrikethroughExtension.create()
      )
    )
    //            .set(DefinitionExtension.TILDE_MARKER, false)
    .set(TablesExtension.COLUMN_SPANS, true)
    .set(TablesExtension.APPEND_MISSING_COLUMNS, false)
    .set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
    .set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true)
    .set(DitaRenderer.SHORTDESC_PARAGRAPH, true)
    .set(DitaRenderer.TIGHT_LIST, false)
    .set(DitaRenderer.ID_FROM_YAML, true)
    .set(DitaRenderer.MDITA_CORE_PROFILE, true)
    .set(DitaRenderer.RAW_DITA, false)
    //            .set(DitaRenderer.SPECIALIZATION, false)
    .toImmutable();

  public static final DataSet EXTENDED_PROFILE = new MutableDataSet()
    .set(
      Parser.EXTENSIONS,
      asList(
        //                    AbbreviationExtension.create(),
        AnchorLinkExtension.create(),
        FootnoteExtension.create(),
        //                    InsExtension.create(),
        JekyllTagExtension.create(),
        SuperscriptExtension.create(),
        TablesExtension.create(),
        //                    AutolinkExtension.create(),
        YamlFrontMatterExtension.create(),
        DefinitionExtension.create()
        //                    StrikethroughExtension.create())
      )
    )
    .set(DefinitionExtension.TILDE_MARKER, false)
    .set(TablesExtension.COLUMN_SPANS, true)
    .set(TablesExtension.APPEND_MISSING_COLUMNS, false)
    .set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
    .set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true)
    .set(DitaRenderer.SHORTDESC_PARAGRAPH, true)
    .set(DitaRenderer.TIGHT_LIST, false)
    .set(DitaRenderer.ID_FROM_YAML, true)
    .set(DitaRenderer.LW_DITA, true)
    .set(DitaRenderer.MDITA_EXTENDED_PROFILE, true)
    .set(DitaRenderer.RAW_DITA, false)
    //            .set(DitaRenderer.SPECIALIZATION, false)
    .toImmutable();

  public MDitaReader() {
    super(EXTENDED_PROFILE);
  }
}
