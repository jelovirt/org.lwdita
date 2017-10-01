package com.elovirta.dita.html;

import com.elovirta.dita.utils.ClasspathURIResolver;
import nu.validator.htmlparser.common.DoctypeExpectation;
import nu.validator.htmlparser.common.Heuristics;
import nu.validator.htmlparser.sax.HtmlParser;
import org.xml.sax.*;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

/**
 * XMLReader implementation for HDITA.
 */
public class HDitaReader extends HtmlReader {

    public HDitaReader() {
        super("hdita2dita.xsl");
    }

}
