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
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * Schema provider for:
 *
 * <dl>
 *   <dt>{@code urn:oasis:names:tc:dita:xsd:topic.xsd}</dt>
 *   <dt>{@code urn:oasis:names:tc:dita:xsd:topic.rng}</dt>
 *   <dd>Topic</dd>
 *   <dt>{@code urn:oasis:names:tc:dita:xsd:concept.xsd}</dt>
 *   <dt>{@code urn:oasis:names:tc:dita:xsd:concept.rng}</dt>
 *   <dd>Concept</dd>
 *   <dt>{@code urn:oasis:names:tc:dita:xsd:task.xsd}</dt>
 *   <dt>{@code urn:oasis:names:tc:dita:xsd:task.rng}</dt>
 *   <dd>Task</dd>
 *   <dt>{@code urn:oasis:names:tc:dita:xsd:reference.xsd}</dt>
 *   <dt>{@code urn:oasis:names:tc:dita:xsd:reference.rng}</dt>
 *   <dd>Reference</dd>
 * </ul>
 */
public class DefaultSchemaProvider implements SchemaProvider {

    private static final Map<URI, DataSet> SCHEMA_OPTIONS;

    static {
        final DataSet options = new MutableDataSet()
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
        final DataSet optionsConcept = new MutableDataSet(options)
                .set(DitaRenderer.SPECIALIZATION_CONCEPT, true)
                .toImmutable();
        final DataSet optionsTask = new MutableDataSet(options)
                .set(DitaRenderer.SPECIALIZATION_TASK, true)
                .toImmutable();
        final DataSet optionsReference = new MutableDataSet(options)
                .set(DitaRenderer.SPECIALIZATION_REFERENCE, true)
                .toImmutable();
        SCHEMA_OPTIONS = Map.of(
                URI.create("urn:oasis:names:tc:dita:xsd:topic.xsd"), options,
                URI.create("urn:oasis:names:tc:dita:xsd:topic.rng"), options,
                URI.create("urn:oasis:names:tc:dita:xsd:concept.xsd"), optionsConcept,
                URI.create("urn:oasis:names:tc:dita:xsd:concept.rng"), optionsConcept,
                URI.create("urn:oasis:names:tc:dita:xsd:task.xsd"), optionsTask,
                URI.create("urn:oasis:names:tc:dita:xsd:task.rng"), optionsTask,
                URI.create("urn:oasis:names:tc:dita:xsd:reference.xsd"), optionsReference,
                URI.create("urn:oasis:names:tc:dita:xsd:reference.rng"), optionsReference
        );
    }

    @Override
    public boolean isSupportedSchema(URI schema) {
        return SCHEMA_OPTIONS.containsKey(schema);
    }

    @Override
    public MarkdownParser createMarkdownParser(URI schema) {
        return new MarkdownParserImpl(SCHEMA_OPTIONS.get(schema));
    }
}
