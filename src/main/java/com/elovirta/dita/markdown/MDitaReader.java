package com.elovirta.dita.markdown;

import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.aside.AsideExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.ins.InsExtension;
import com.vladsch.flexmark.ext.jekyll.tag.JekyllTagExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.superscript.SuperscriptExtension;
import com.vladsch.flexmark.util.options.MutableDataSet;

import static java.util.Arrays.asList;

/**
 * XMLReader implementation for Markdown Lightweight DITA.
 */
public class MDitaReader extends MarkdownReader {

    public MDitaReader() {
        super(new MutableDataSet()
                .set(Parser.EXTENSIONS, asList(
                        AbbreviationExtension.create(),
                        AnchorLinkExtension.create(),
                        AsideExtension.create(),
                        FootnoteExtension.create(),
                        //                GfmIssuesExtension.create(),
                        //                GfmUsersExtension.create(),
                        //                TaskListExtension.create(),
                        InsExtension.create(),
                        JekyllTagExtension.create(),
                        //                JiraConverterExtension.create(),
                        //                StrikethroughSubscriptExtension.create(),
                        SuperscriptExtension.create(),
                        //                SubscriptExtension.create(),
                        TablesExtension.create(),
                        TypographicExtension.create(),
                        //                WikiLinkExtension.create(),
                        AutolinkExtension.create(),
                        YamlFrontMatterExtension.create(),
                        DefinitionExtension.create(),
                        StrikethroughExtension.create()))
                .set(DefinitionExtension.TILDE_MARKER, false)
                // for full GFM table compatibility add the following table extension options:
                .set(TablesExtension.COLUMN_SPANS, false)
                .set(TablesExtension.APPEND_MISSING_COLUMNS, true)
                .set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
                .set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true)
                .set(DitaRenderer.SHORTDESC_PARAGRAPH, true)
                .set(DitaRenderer.ID_FROM_YAML, true)
        );
//        options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
    }

}
