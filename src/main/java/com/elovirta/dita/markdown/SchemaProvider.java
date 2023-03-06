package com.elovirta.dita.markdown;

import java.net.URI;

public interface SchemaProvider {
    boolean isSupportedSchema(URI schema);

    MarkdownParser createMarkdownParser(URI schema);
}
