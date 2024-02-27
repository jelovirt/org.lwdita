package com.elovirta.dita.markdown.renderer;

import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_CLASS;

import org.dita.dost.util.DitaClass;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;

public class Utils {

  public static Attributes buildAtts(final DitaClass cls) {
    return new XMLUtils.AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, cls.toString()).build();
  }
}
