package com.elovirta.dita.markdown;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class MDitaReaderTest extends MarkdownReaderTest {

    @Override
    MarkdownReader getReader() {
        return new MDitaReader();
    }

    @Override
    String getPrefix() {
        return "lwdita/";
    }

}