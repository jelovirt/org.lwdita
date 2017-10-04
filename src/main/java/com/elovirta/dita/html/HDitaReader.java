package com.elovirta.dita.html;

/**
 * XMLReader implementation for HDITA.
 */
public class HDitaReader extends HtmlReader {

    public HDitaReader() {
        super("hdita2dita.xsl");
    }

}
