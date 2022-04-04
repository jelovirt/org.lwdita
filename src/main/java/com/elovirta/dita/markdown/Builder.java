package com.elovirta.dita.markdown;

import com.elovirta.dita.markdown.renderer.HeaderIdGeneratorFactory;
import com.elovirta.dita.markdown.renderer.NodeRendererFactory;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Builder for configuring an {@link DitaRenderer}. See methods for default configuration.
 */
class Builder extends MutableDataSet {
    HeaderIdGeneratorFactory ditaIdGeneratorFactory = null;

    public Builder(DataHolder options) {
        super(options);
    }

    public Builder(Builder other) {
        super(other);
        this.ditaIdGeneratorFactory = other.ditaIdGeneratorFactory;
    }
}
