package com.elovirta.dita.markdown;

import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension;
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension;
import com.vladsch.flexmark.ext.attributes.AttributesExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension;
import com.vladsch.flexmark.ext.ins.InsExtension;
import com.vladsch.flexmark.ext.jekyll.tag.JekyllTagExtension;
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.DataSet;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.net.URI;

import static java.util.Arrays.asList;

public class TestSchemaProvider implements SchemaProvider {
    private static final URI SCHEMA = URI.create("urn:oasis:names:tc:dita:xsd:topic.xsd");
    private static final DataSet OPTIONS = new MutableDataSet()
            .set(Parser.EXTENSIONS, asList(
                    AbbreviationExtension.create(),
                    AnchorLinkExtension.create(),
                    AttributesExtension.create(),
                    FootnoteExtension.create(),
                    InsExtension.create(),
                    JekyllTagExtension.create(),
                    SuperscriptExtension.create(),
                    TablesExtension.create(),
                    AutolinkExtension.create(),
                    YamlFrontMatterExtension.create(),
                    DefinitionExtension.create(),
                    StrikethroughSubscriptExtension.create()))
            .set(DefinitionExtension.TILDE_MARKER, false)
            .set(TablesExtension.COLUMN_SPANS, true)
            .set(TablesExtension.APPEND_MISSING_COLUMNS, false)
            .set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
            .set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true)
            .set(DitaRenderer.SHORTDESC_PARAGRAPH, true)
            .toImmutable();

    @Override
    public boolean isSupportedSchema(URI schema) {
        return SCHEMA.equals(schema);
    }

    @Override
    public MarkdownParser createMarkdownParser(URI schema) {
        return new MarkdownParserImpl(OPTIONS);
    }
}
