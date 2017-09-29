package com.elovirta.dita.utils;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.net.URI;

public class ClasspathURIResolver implements URIResolver {
    @Override
    public Source resolve(String href, String base) throws TransformerException {
        final URI file = URI.create(href);
        final InputStream inputStream = getClass().getResourceAsStream(file.getPath());
        return new StreamSource(inputStream, href);
    }
}
