/*
 * Based on ToHtmlSerializer (C) 2010-2011 Mathias Doenitz
 */
package com.elovirta.dita.markdown.renderer;

import static com.elovirta.dita.markdown.MetadataSerializerImpl.buildAtts;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.toURI;
import static org.dita.dost.util.XMLUtils.AttributesBuilder;

import com.elovirta.dita.markdown.SaxWriter;
import com.google.common.io.Files;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import java.net.URI;
import java.util.Map;
import java.util.stream.StreamSupport;
import org.dita.dost.util.DitaClass;
import org.xml.sax.Attributes;

/**
 * A renderer for a set of node types.
 */
public abstract class AbstractRenderer {

  protected static final Attributes I_ATTS = buildAtts(HI_D_I);
  protected static final Attributes B_ATTS = buildAtts(HI_D_B);
  protected static final Attributes CODEPH_ATTS = buildAtts(PR_D_CODEPH);
  protected static final Attributes LINE_THROUGH_ATTS = buildAtts(HI_D_LINE_THROUGH);
  protected static final Attributes SUP_ATTS = buildAtts(HI_D_SUP);
  protected static final Attributes SUB_ATTS = buildAtts(HI_D_SUB);
  protected static final Attributes TT_ATTS = buildAtts(HI_D_TT);
  protected static final Attributes TITLE_ATTS = buildAtts(TOPIC_TITLE);
  protected static final Attributes IMAGE_ATTS = buildAtts(TOPIC_IMAGE);
  protected static final Attributes ALT_ATTS = buildAtts(TOPIC_ALT);
  protected static final Attributes PH_ATTS = buildAtts(TOPIC_PH);

  /**
   * @return the mapping of nodes this renderer handles to rendering function
   */
  abstract Map<Class<? extends Node>, NodeRenderingHandler<? extends Node>> getNodeRenderingHandlers();

  protected boolean hasMultipleTopLevelHeaders(Document astRoot) {
    final long count = StreamSupport
      .stream(astRoot.getChildren().spliterator(), false)
      .filter(n -> (n instanceof Heading) && (((Heading) n).getLevel() == 1))
      .count();
    return count > 1;
  }

  protected boolean hasChildren(final Node node) {
    return node.hasChildren();
  }

  protected void printTag(
    Text node,
    NodeRendererContext context,
    SaxWriter html,
    final DitaClass tag,
    final Attributes atts
  ) {
    html.startElement(node, tag, atts);
    html.characters(node.getChars().toString());
    html.endElement();
  }

  protected void printTag(
    Node node,
    NodeRendererContext context,
    SaxWriter html,
    final DitaClass tag,
    final Attributes atts
  ) {
    html.startElement(node, tag, atts);
    context.renderChildren(node);
    html.endElement();
  }

  abstract AttributesBuilder getLinkAttributes(final String href);

  protected AttributesBuilder getLinkAttributes(final String href, Attributes baseAttrs) {
    final AttributesBuilder atts = new AttributesBuilder(baseAttrs).add(ATTRIBUTE_NAME_HREF, href);
    if (href.startsWith("#")) {
      atts.add(ATTRIBUTE_NAME_FORMAT, "markdown");
    } else {
      final URI uri = toURI(href);
      String format = null;
      if (uri.getPath() != null) {
        final String ext = Files.getFileExtension(uri.getPath()).toLowerCase();
        switch (ext) {
          case ATTR_FORMAT_VALUE_DITA:
          case "xml":
            format = null;
            break;
          // Markdown is converted to DITA
          case "md":
          case "markdown":
            format = "markdown";
            break;
          default:
            format = !ext.isEmpty() ? ext : "html";
            break;
        }
      }
      if (uri.getScheme() != null && uri.getScheme().equals("mailto")) {
        atts.add(ATTRIBUTE_NAME_FORMAT, "email");
      }
      if (format != null) {
        atts.add(ATTRIBUTE_NAME_FORMAT, format);
      }

      if (
        uri != null && (uri.isAbsolute() || !uri.isAbsolute() && uri.getPath() != null && uri.getPath().startsWith("/"))
      ) {
        atts.add(ATTRIBUTE_NAME_SCOPE, ATTR_SCOPE_VALUE_EXTERNAL);
      }
    }
    return atts;
  }

  protected String normalize(final String string) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < string.length(); i++) {
      char c = string.charAt(i);
      switch (c) {
        case ' ':
        case '\n':
        case '\t':
          continue;
      }
      sb.append(Character.toLowerCase(c));
    }
    return sb.toString();
  }
}
