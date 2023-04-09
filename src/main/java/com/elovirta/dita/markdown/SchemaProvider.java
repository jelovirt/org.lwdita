package com.elovirta.dita.markdown;

import java.net.URI;

/**
 * Markdown schema provider for parsers.
 */
public interface SchemaProvider {
  /**
   * Test whether schema is supported by this provider.
   *
   * @param schema Markdown schema
   */
  boolean isSupportedSchema(URI schema);

  /**
   * Create Markdown parser for schema.
   *
   * @param schema Markdown schema
   * @return parser for schema
   */
  MarkdownParser createMarkdownParser(URI schema);
}
