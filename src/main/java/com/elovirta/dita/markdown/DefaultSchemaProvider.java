package com.elovirta.dita.markdown;

import static com.elovirta.dita.markdown.MDitaReader.CORE_PROFILE;
import static com.elovirta.dita.markdown.MDitaReader.EXTENDED_PROFILE;
import static java.util.Arrays.asList;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
      .set(
        Parser.EXTENSIONS,
        asList(
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
          StrikethroughSubscriptExtension.create()
        )
      )
      .set(DefinitionExtension.TILDE_MARKER, false)
      .set(TablesExtension.COLUMN_SPANS, true)
      .set(TablesExtension.APPEND_MISSING_COLUMNS, false)
      .set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
      .set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true)
      .set(DitaRenderer.SHORTDESC_PARAGRAPH, true)
      .set(DitaRenderer.FIX_ROOT_HEADING, true)
      .set(DitaRenderer.ID_FROM_YAML, true)
      .toImmutable();
    final DataSet optionsConcept = new MutableDataSet(options)
      .set(DitaRenderer.SPECIALIZATION_CONCEPT, true)
      .toImmutable();
    final DataSet optionsTask = new MutableDataSet(options).set(DitaRenderer.SPECIALIZATION_TASK, true).toImmutable();
    final DataSet optionsReference = new MutableDataSet(options)
      .set(DitaRenderer.SPECIALIZATION_REFERENCE, true)
      .toImmutable();
    final DataSet optionsMap = new MutableDataSet()
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
      .set(TablesExtension.COLUMN_SPANS, false)
      .set(TablesExtension.APPEND_MISSING_COLUMNS, false)
      .set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
      .set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true)
      //                .set(DitaRenderer.SHORTDESC_PARAGRAPH, true)
      .set(DitaRenderer.ID_FROM_YAML, true)
      .set(DitaRenderer.MAP, true)
      //            .set(DitaRenderer.SPECIALIZATION, false)
      .toImmutable();
    final Map<URI, DataSet> schemas = new HashMap<>();
    schemas.put(URI.create("urn:oasis:names:tc:dita:xsd:topic.xsd"), options);
    schemas.put(URI.create("urn:oasis:names:tc:dita:rng:topic.rng"), options);
    schemas.put(URI.create("urn:oasis:names:tc:dita:xsd:concept.xsd"), optionsConcept);
    schemas.put(URI.create("urn:oasis:names:tc:dita:rng:concept.rng"), optionsConcept);
    schemas.put(URI.create("urn:oasis:names:tc:dita:xsd:task.xsd"), optionsTask);
    schemas.put(URI.create("urn:oasis:names:tc:dita:rng:task.rng"), optionsTask);
    schemas.put(URI.create("urn:oasis:names:tc:dita:xsd:reference.xsd"), optionsReference);
    schemas.put(URI.create("urn:oasis:names:tc:dita:rng:reference.rng"), optionsReference);
    schemas.put(URI.create("urn:oasis:names:tc:dita:xsd:map.xsd"), optionsMap);
    schemas.put(URI.create("urn:oasis:names:tc:dita:rng:map.rng"), optionsMap);
    schemas.put(URI.create("urn:oasis:names:tc:mdita:xsd:topic.xsd"), EXTENDED_PROFILE);
    schemas.put(URI.create("urn:oasis:names:tc:mdita:rng:topic.rng"), EXTENDED_PROFILE);
    schemas.put(URI.create("urn:oasis:names:tc:mdita:extended:xsd:topic.xsd"), EXTENDED_PROFILE);
    schemas.put(URI.create("urn:oasis:names:tc:mdita:extended:rng:topic.rng"), EXTENDED_PROFILE);
    schemas.put(URI.create("urn:oasis:names:tc:mdita:core:xsd:topic.xsd"), CORE_PROFILE);
    schemas.put(URI.create("urn:oasis:names:tc:mdita:core:rng:topic.rng"), CORE_PROFILE);
    SCHEMA_OPTIONS = Collections.unmodifiableMap(schemas);
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
