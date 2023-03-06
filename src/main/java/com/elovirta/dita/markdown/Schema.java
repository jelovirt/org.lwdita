package com.elovirta.dita.markdown;

import java.net.URI;
import java.util.Set;

public interface Schema {
    Set<URI> getScheme();

    MarkdownParser createMarkdownParser();
}
