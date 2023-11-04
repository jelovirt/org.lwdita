package com.elovirta.dita.utils;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
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
      URI res = URI.create(href);
      if (!res.isAbsolute()) {
        URI b = URI.create(base);
        if (Objects.equals(b.getScheme(), "plugin")) {
          String[] tokens = b.getSchemeSpecificPart().split(":", 2);
          res = URI.create("plugin:" + tokens[0] + ":" + URI.create(tokens[1]).resolve(href));
        } else {
          res = new URI(base).resolve(href);
        }
      }
      final String resource;
      if (Objects.equals(res.getScheme(), "plugin")) {
        resource = res.toString().substring(7).replace(':', '/');
      } else {
        resource = res.getPath().substring(1);
      }
      final InputStream in = this.getClass().getClassLoader().getResourceAsStream(resource);
      System.out.println("Resolved " + resource + ": " + in);
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
