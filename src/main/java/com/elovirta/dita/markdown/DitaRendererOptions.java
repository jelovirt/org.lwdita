package com.elovirta.dita.markdown;

import com.vladsch.flexmark.util.data.DataHolder;

public class DitaRendererOptions {

  public final boolean doNotRenderLinksInDocument;
  public final String noLanguageClass;
  public final String languageClassPrefix;

  public DitaRendererOptions(DataHolder options) {
    doNotRenderLinksInDocument = DitaRenderer.DO_NOT_RENDER_LINKS.get(options);
    noLanguageClass = DitaRenderer.FENCED_CODE_NO_LANGUAGE_CLASS.get(options);
    languageClassPrefix = DitaRenderer.FENCED_CODE_LANGUAGE_CLASS_PREFIX.get(options);
  }
}
