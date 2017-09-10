package com.elovirta.dita.markdown;

import com.elovirta.dita.markdown.DitaRenderer.Metadata;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class ToDitaSerializerTest {

    @Test
    public void testParseMetadata() {
        assertEqualsMetadata(new Metadata("foo", Collections.EMPTY_LIST), Metadata.parse("#foo"));
        assertEqualsMetadata(new Metadata("foo", Collections.EMPTY_LIST), Metadata.parse(" #foo "));
        assertEqualsMetadata(new Metadata("bar", Collections.EMPTY_LIST), Metadata.parse("#foo #bar"));
        assertEqualsMetadata(new Metadata(null, Arrays.asList("foo")), Metadata.parse(".foo"));
        assertEqualsMetadata(new Metadata(null, Arrays.asList("foo", "bar")), Metadata.parse(" .foo  .bar "));
        assertEqualsMetadata(new Metadata("baz", Arrays.asList("foo", "bar")), Metadata.parse(".foo #baz .bar"));
    }

    private void assertEqualsMetadata(final Metadata exp, final Metadata act) {
        assertEquals(exp.id, act.id);
        assertEquals(exp.classes, act.classes);
    }

}