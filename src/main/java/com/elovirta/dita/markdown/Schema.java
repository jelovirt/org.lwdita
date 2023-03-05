package com.elovirta.dita.markdown;

import com.vladsch.flexmark.util.data.MutableDataSet;

import java.net.URI;
import java.util.Set;

public interface Schema {
    Set<URI> getUri();
    MutableDataSet getOptions();
}
