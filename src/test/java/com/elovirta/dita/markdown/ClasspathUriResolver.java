package com.elovirta.dita.markdown;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class ClasspathUriResolver implements URIResolver {

    private final URIResolver uriResolver;

    public ClasspathUriResolver(URIResolver uriResolver) {
        this.uriResolver = uriResolver;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        try {
            final URI res = new URI(base).resolve(href);
            final InputStream in = this.getClass().getClassLoader().getResourceAsStream(res.getPath().substring(1));
            if (in != null) {
                return new StreamSource(in, res.toString());
            } else {
                return uriResolver.resolve(href, base);
            }
        } catch (URISyntaxException e) {
            throw new TransformerException(e);
        }
    }
}
