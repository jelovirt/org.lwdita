package com.elovirta.dita.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ClasspathURIResolverTest {

  private Source act;

  @AfterEach
  void cleanUp() throws IOException {
    if (act instanceof StreamSource) {
      final InputStream inputStream = ((StreamSource) act).getInputStream();
      if (inputStream != null) {
        inputStream.close();
      }
    }
  }

  @Test
  void resolve_plugin() throws TransformerException {
    final ClasspathURIResolver resolver = new ClasspathURIResolver(null);
    act = resolver.resolve("plugin:ast.xsl", "");
    assertNotNull(act);
  }

  @Test
  void resolve_plugin_notFound() throws TransformerException {
    AtomicBoolean useFallback = new AtomicBoolean(false);
    final ClasspathURIResolver resolver = new ClasspathURIResolver((href, base) -> {
      useFallback.set(true);
      return null;
    });
    act = resolver.resolve("plugin:missing", "");
    assertTrue(useFallback.get());
  }

  @Test
  void resolve_classpath() throws TransformerException {
    final ClasspathURIResolver resolver = new ClasspathURIResolver(null);
    act = resolver.resolve("classpath:/ast.xsl", "");
    assertNotNull(act);
  }

  @Test
  void resolve_classpath_notFound() throws TransformerException {
    AtomicBoolean useFallback = new AtomicBoolean(false);
    final ClasspathURIResolver resolver = new ClasspathURIResolver((href, base) -> {
      useFallback.set(true);
      return null;
    });
    act = resolver.resolve("classpath:/missing", "");
    assertTrue(useFallback.get());
  }
}
