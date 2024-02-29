/*
 * Based on ToHtmlSerializer (C) 2010-2011 Mathias Doenitz
 */
package com.elovirta.dita.markdown.renderer;

import static com.elovirta.dita.markdown.MarkdownReader.FORMATS;
import static com.elovirta.dita.markdown.renderer.Utils.buildAtts;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.toURI;
import static org.dita.dost.util.XMLUtils.AttributesBuilder;

import com.elovirta.dita.markdown.DitaRenderer;
import com.elovirta.dita.markdown.MetadataSerializer;
import com.elovirta.dita.markdown.MetadataSerializerImpl;
import com.elovirta.dita.markdown.SaxWriter;
import com.elovirta.dita.utils.ClasspathURIResolver;
import com.google.common.base.Suppliers;
import com.google.common.io.Files;
import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.ext.anchorlink.AnchorLink;
import com.vladsch.flexmark.ext.attributes.AttributesNode;
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough;
import com.vladsch.flexmark.ext.gfm.strikethrough.Subscript;
import com.vladsch.flexmark.ext.superscript.Superscript;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import com.vladsch.flexmark.util.visitor.AstHandler;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;
import org.dita.dost.util.DitaClass;
import org.dita.dost.util.XMLUtils;
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

  protected final boolean mditaExtendedProfile;
  protected final boolean mditaCoreProfile;
  protected final Supplier<SAXTransformerFactory> transformerFactorySupplier;
  protected final Supplier<Templates> templatesSupplier;
  protected final Collection<String> formats;
  protected final MetadataSerializer metadataSerializer;

  public AbstractRenderer(DataHolder options) {
    final boolean idFromYaml = DitaRenderer.ID_FROM_YAML.get(options);
    metadataSerializer = new MetadataSerializerImpl(idFromYaml);

    mditaExtendedProfile = DitaRenderer.MDITA_EXTENDED_PROFILE.get(options);
    mditaCoreProfile = DitaRenderer.MDITA_CORE_PROFILE.get(options);
    transformerFactorySupplier =
      Suppliers.memoize(() -> {
        final SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
        tf.setURIResolver(new ClasspathURIResolver(tf.getURIResolver()));
        return tf;
      })::get;
    templatesSupplier =
      Suppliers.memoize(() -> {
        final SAXTransformerFactory tf = transformerFactorySupplier.get();
        final String stylesheet = (mditaCoreProfile || mditaExtendedProfile)
          ? "/hdita2dita.xsl"
          : "/hdita2dita-markdown.xsl";
        try (InputStream in = getClass().getResourceAsStream(stylesheet)) {
          return tf.newTemplates(new StreamSource(in, "classpath://" + stylesheet));
        } catch (IOException | TransformerConfigurationException e) {
          throw new RuntimeException(e);
        }
      })::get;
    formats = FORMATS.get(options);
  }

  /**
   * @return the mapping of nodes this renderer handles to rendering function
   */
  protected Map<Class<? extends Node>, NodeRenderingHandler<? extends Node>> getNodeRenderingHandlers() {
    final List<NodeRenderingHandler<? extends Node>> res = new ArrayList<>();
    res.add(new NodeRenderingHandler<>(AnchorLink.class, this::render));
    res.add(new NodeRenderingHandler<>(Code.class, this::render));
    res.add(new NodeRenderingHandler<>(TextBase.class, this::render));
    res.add(new NodeRenderingHandler<>(Emphasis.class, this::render));
    res.add(new NodeRenderingHandler<>(StrongEmphasis.class, this::render));
    res.add(new NodeRenderingHandler<>(HtmlEntity.class, this::render));
    res.add(new NodeRenderingHandler<>(HtmlInlineComment.class, this::render));
    if (!mditaCoreProfile) {
      res.add(new NodeRenderingHandler<>(Superscript.class, this::render));
      res.add(new NodeRenderingHandler<>(Subscript.class, this::render));
    }
    if (!mditaCoreProfile && !mditaExtendedProfile) {
      res.add(new NodeRenderingHandler<>(Strikethrough.class, this::render));
    }

    return res.stream().collect(Collectors.toMap(AstHandler::getNodeType, Function.identity()));
  }

  protected void render(AnchorLink node, final NodeRendererContext context, final SaxWriter html) {
    context.renderChildren(node);
  }

  private void render(final Code node, final NodeRendererContext context, final SaxWriter html) {
    if (mditaExtendedProfile) {
      printTag(node, context, html, HI_D_TT, TT_ATTS);
    } else {
      printTag(node, context, html, PR_D_CODEPH, getInlineAttributes(node, CODEPH_ATTS));
    }
  }

  protected void render(final TextBase node, final NodeRendererContext context, final SaxWriter html) {
    context.renderChildren(node);
  }

  protected void render(final Emphasis node, final NodeRendererContext context, final SaxWriter html) {
    printTag(node, context, html, HI_D_I, getInlineAttributes(node, I_ATTS));
  }

  protected void render(final StrongEmphasis node, final NodeRendererContext context, final SaxWriter html) {
    printTag(node, context, html, HI_D_B, getInlineAttributes(node, B_ATTS));
  }

  protected void render(final Superscript node, final NodeRendererContext context, final SaxWriter html) {
    printTag(node, context, html, HI_D_SUP, getInlineAttributes(node, SUP_ATTS));
  }

  protected void render(final Subscript node, final NodeRendererContext context, final SaxWriter html) {
    printTag(node, context, html, HI_D_SUB, getInlineAttributes(node, SUB_ATTS));
  }

  private void render(final Strikethrough node, final NodeRendererContext context, final SaxWriter html) {
    if (mditaExtendedProfile) {
      printTag(node, context, html, TOPIC_PH, PH_ATTS);
    } else {
      printTag(node, context, html, HI_D_LINE_THROUGH, getInlineAttributes(node, LINE_THROUGH_ATTS));
    }
  }

  /**
   * Map HTML entity to Unicode character.
   */
  private void render(final HtmlEntity node, final NodeRendererContext context, final SaxWriter html) {
    final BasedSequence chars = node.getChars();
    final String name = chars.subSequence(1, chars.length() - 1).toString().toLowerCase();
    final String val = Entities.ENTITIES.getProperty(name);
    if (val != null) {
      html.characters(val);
    }
  }

  private void render(final HtmlInlineComment node, final NodeRendererContext context, final SaxWriter html) {
    // Ignore
  }

  protected List<Node> childList(Node astRoot) {
    return StreamSupport.stream(astRoot.getChildren().spliterator(), false).collect(Collectors.toList());
  }

  protected boolean hasMultipleTopLevelHeaders(Document astRoot) {
    final long count = StreamSupport
      .stream(astRoot.getChildren().spliterator(), false)
      .filter(n -> (n instanceof Heading) && (((Heading) n).getLevel() == 1))
      .count();
    return count > 1;
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
          default:
            if (formats.contains(ext)) {
              format = ext;
            } else {
              format = !ext.isEmpty() ? ext : "html";
            }
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

  protected Attributes getInlineAttributes(Node node, Attributes base) {
    if (!mditaCoreProfile && !mditaExtendedProfile) {
      if (node.getChildOfType(AttributesNode.class) != null) {
        final Title header = Title.getFromChildren(node);
        final AttributesBuilder builder = new AttributesBuilder(base);
        return readAttributes(header, builder).build();
      } else if (node.getNext() instanceof AttributesNode) {
        final Title header = Title.getFromNext(node);
        final AttributesBuilder builder = new AttributesBuilder(base);
        return readAttributes(header, builder).build();
      }
    }
    return base;
  }

  protected AttributesBuilder readAttributes(Title header, AttributesBuilder builder) {
    if (!header.classes.isEmpty()) {
      builder.add(ATTRIBUTE_NAME_OUTPUTCLASS, String.join(" ", header.classes));
    }
    for (Map.Entry<String, String> attr : header.attributes.entrySet()) {
      builder.add(attr.getKey(), attr.getValue());
    }
    header.id.ifPresent(id -> builder.add(ATTRIBUTE_NAME_ID, id));
    return builder;
  }
}
