package com.elovirta.dita.html;

import com.vladsch.flexmark.util.data.DataSet;

/**
 * XMLReader implementation for HDITA.
 */
public class HDitaReader extends HtmlReader {

  public HDitaReader() {
    super(new DataSet(), "hdita2dita.xsl");
  }
}
