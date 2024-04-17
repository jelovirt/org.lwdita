package com.elovirta.dita.markdown.renderer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Add namespace mapping to predefined element name prefixes.
 */
final class NamespaceFilter extends XMLFilterImpl {

  public NamespaceFilter(XMLReader parent) {
    super(parent);
  }

  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    if (qName.startsWith("m:")) {
      final String tag = qName.substring(2);
      if (qName.equals("m:math")) {
        super.startPrefixMapping("m", "http://www.w3.org/1998/Math/MathML");
      }
      super.startElement(uri, tag, qName, atts);
    } else if (qName.startsWith("svg:")) {
      final String tag = qName.substring(4);
      if (qName.equals("svg:svg")) {
        super.startPrefixMapping("svg", "http://www.w3.org/2000/svg");
      }
      super.startElement(uri, tag, qName, atts);
    } else {
      super.startElement(uri, localName, qName, atts);
    }
  }

  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (qName.startsWith("m:")) {
      final String tag = qName.substring(2);
      super.endElement(uri, tag, qName);
      if (qName.equals("m:math")) {
        super.endPrefixMapping("m");
      }
    } else if (qName.startsWith("svg:")) {
      final String tag = qName.substring(2);
      super.endElement(uri, tag, qName);
      if (qName.equals("svg:svg")) {
        super.endPrefixMapping("svg");
      }
    } else {
      super.endElement(uri, localName, qName);
    }
  }
}
