package com.elovirta.dita.utils;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

public class ClasspathURIResolver implements URIResolver {

  private final URIResolver uriResolver;

  public ClasspathURIResolver(URIResolver uriResolver) {
    this.uriResolver = uriResolver;
  }

  @Override
  public Source resolve(String href, String base) throws TransformerException {
    try {
      final URI res = new URI(base).resolve(href);
      final String resource = res.getScheme().equals("plugin")
        ? res.toString().substring(7).replace(':', '/')
        : res.getPath().substring(1);
      final InputStream in = this.getClass().getClassLoader().getResourceAsStream(resource);
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
