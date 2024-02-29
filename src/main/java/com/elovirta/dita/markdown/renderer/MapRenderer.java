/*
 * Based on ToHtmlSerializer (C) 2010-2011 Mathias Doenitz
 */
package com.elovirta.dita.markdown.renderer;

import static com.elovirta.dita.markdown.renderer.Utils.buildAtts;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.XMLUtils.AttributesBuilder;

import com.elovirta.dita.markdown.*;
import com.elovirta.dita.utils.FragmentContentHandler;
import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.ext.anchorlink.AnchorLink;
import com.vladsch.flexmark.ext.attributes.AttributesNode;
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough;
import com.vladsch.flexmark.ext.gfm.strikethrough.Subscript;
import com.vladsch.flexmark.ext.superscript.Superscript;
import com.vladsch.flexmark.ext.tables.*;
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterBlock;
import com.vladsch.flexmark.util.ast.ContentNode;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.visitor.AstHandler;
import java.io.IOException;
import java.io.StringReader;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;
import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;
import org.dita.dost.util.DitaClass;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A renderer for a set of node types.
 */
public class MapRenderer extends AbstractRenderer {

  private static final Attributes MAP_ATTS = new AttributesBuilder()
    .add(ATTRIBUTE_NAME_CLASS, MAP_MAP.toString())
    .add(
      DITA_NAMESPACE,
      ATTRIBUTE_NAME_DITAARCHVERSION,
      ATTRIBUTE_PREFIX_DITAARCHVERSION + ":" + ATTRIBUTE_NAME_DITAARCHVERSION,
      "CDATA",
      "2.0"
    )
    //            .add(ATTRIBUTE_NAME_DOMAINS, "(topic hi-d) (topic ut-d) (topic indexing-d) (topic hazard-d) (topic abbrev-d) (topic pr-d) (topic sw-d) (topic ui-d)")
    .build();
  private static final Attributes TOPICREF_ATTS = buildAtts(MAP_TOPICREF);
  private static final Attributes TOPICHEAD_ATTS = buildAtts(MAPGROUP_D_TOPICHEAD);
  private static final Attributes TOPICMETA_ATTS = buildAtts(MAP_TOPICMETA);
  private static final Attributes NAVTITLE_ATTS = buildAtts(TOPIC_NAVTITLE);
  private static final Attributes RELTABLE_ATTS = new AttributesBuilder()
    .add(ATTRIBUTE_NAME_CLASS, MAP_RELTABLE.toString())
    .add("toc", "no")
    .build();
  private static final Attributes RELHEADER_ATTS = buildAtts(MAP_RELHEADER);
  private static final Attributes RELCOLSPEC_ATTS = new AttributesBuilder()
    .add(ATTRIBUTE_NAME_CLASS, MAP_RELCOLSPEC.toString())
    .add("toc", "no")
    .build();
  private static final Attributes RELROW_ATTS = buildAtts(MAP_RELROW);
  private static final Attributes RELCELL_ATTS = buildAtts(MAP_RELCELL);
  private static final Attributes KEYDEF_ATTS = buildAtts(MAPGROUP_D_KEYDEF);

  private final boolean idFromYaml;

  public MapRenderer(DataHolder options) {
    super(options);
    idFromYaml = DitaRenderer.ID_FROM_YAML.get(options);
  }

  @Override
  public Map<Class<? extends Node>, NodeRenderingHandler<? extends Node>> getNodeRenderingHandlers() {
    final List<NodeRenderingHandler<? extends Node>> res = new ArrayList<>();
    res.add(new NodeRenderingHandler<>(TableBlock.class, this::renderSimpleTableBlock));
    res.add(new NodeRenderingHandler<>(TableBody.class, this::renderSimpleTableBody));
    res.add(new NodeRenderingHandler<>(TableHead.class, this::renderSimpleTableHead));
    res.add(new NodeRenderingHandler<>(TableRow.class, this::renderSimpleTableRow));
    res.add(new NodeRenderingHandler<>(TableCell.class, this::renderSimpleTableCell));
    res.add(new NodeRenderingHandler<>(TableSeparator.class, this::renderSimpleTableSeparator));

    res.add(new NodeRenderingHandler<>(YamlFrontMatterBlock.class, this::render));
    res.add(new NodeRenderingHandler<>(BulletList.class, this::render));
    res.add(new NodeRenderingHandler<>(Document.class, this::render));
    res.add(new NodeRenderingHandler<>(Heading.class, this::render));
    res.add(new NodeRenderingHandler<>(Image.class, this::render));
    res.add(new NodeRenderingHandler<>(ImageRef.class, this::render));
    res.add(new NodeRenderingHandler<>(Link.class, this::render));
    res.add(new NodeRenderingHandler<>(LinkRef.class, this::render));
    res.add(new NodeRenderingHandler<>(BulletListItem.class, this::render));
    res.add(new NodeRenderingHandler<>(OrderedListItem.class, this::render));
    res.add(new NodeRenderingHandler<>(MailLink.class, this::render));
    res.add(new NodeRenderingHandler<>(OrderedList.class, this::render));
    res.add(new NodeRenderingHandler<>(Paragraph.class, this::render));
    res.add(new NodeRenderingHandler<>(Reference.class, this::render));
    res.add(new NodeRenderingHandler<>(SoftLineBreak.class, this::render));
    res.add(new NodeRenderingHandler<>(Text.class, this::render));

    final Map<Class<? extends Node>, NodeRenderingHandler<? extends Node>> map = new HashMap<>(
      super.getNodeRenderingHandlers()
    );
    map.putAll(
      res
        .stream()
        .collect(
          Collectors.<NodeRenderingHandler<? extends Node>, Class<? extends Node>, NodeRenderingHandler<? extends Node>>toMap(
            AstHandler::getNodeType,
            Function.identity()
          )
        )
    );
    return map;
  }

  // Visitor methods

  private void render(final Document node, final NodeRendererContext context, final SaxWriter html) {
    final boolean isCompound = hasMultipleTopLevelHeaders(node);
    if (isCompound) {
      throw new ParseException(String.format("Map file cannot have multiple top level headers"));
    }
    final AttributesBuilder atts = mditaCoreProfile || mditaExtendedProfile
      ? new AttributesBuilder(MAP_ATTS).add(ATTRIBUTE_NAME_SPECIALIZATIONS, "")
      : new AttributesBuilder(MAP_ATTS)
        .add(
          ATTRIBUTE_NAME_SPECIALIZATIONS,
          "@props/audience @props/deliveryTarget @props/otherprops @props/platform @props/product"
        );

    String id = null;
    final Heading heading = (Heading) node.getChildOfType(Heading.class);
    if (heading != null) {
      final StringBuilder buf = new StringBuilder();
      node.getAstExtra(buf);
      Title header = null;
      if (!mditaCoreProfile) {
        if (node.getFirstChild() instanceof AnchorLink) {
          header = Title.getFromChildren(node.getFirstChild());
        } else {
          header = Title.getFromChildren(node);
        }
        header.id.ifPresent(heading::setAnchorRefId);
      }
      id = getTopicId(heading, header);
      if (!mditaCoreProfile) {
        if (!header.classes.isEmpty()) {
          atts.add(ATTRIBUTE_NAME_OUTPUTCLASS, String.join(" ", header.classes));
        }
        for (Entry<String, String> attr : header.attributes.entrySet()) {
          atts.add(attr.getKey(), attr.getValue());
        }
      }
    }
    if (id != null) {
      atts.add(ATTRIBUTE_NAME_ID, id);
    }
    html.startElement(node, MAP_MAP, atts.build());

    if (heading == null) {
      final Node firstChild = node.getFirstChild();
      if (firstChild instanceof YamlFrontMatterBlock) {
        html.startElement(firstChild, MAP_TOPICMETA, TOPICMETA_ATTS);
        metadataSerializer.render((YamlFrontMatterBlock) firstChild, context, html);
        html.endElement();
      }
    }

    context.renderChildren(node);
  }

  private void render(final BulletList node, final NodeRendererContext context, final SaxWriter html) {
    context.renderChildren(node);
  }

  private void render(final Image node, final NodeRendererContext context, final SaxWriter html) {
    final AttributesBuilder atts = new AttributesBuilder(getInlineAttributes(node, IMAGE_ATTS))
      .add(ATTRIBUTE_NAME_HREF, node.getUrl().toString());
    writeImage(node, node.getTitle().toString(), null, atts, context, html);
  }

  private void writeImage(
    Image node,
    final String title,
    final String alt,
    final AttributesBuilder atts,
    final NodeRendererContext context,
    SaxWriter html
  ) {
    html.startElement(node, TOPIC_IMAGE, atts.build());
    if (node.hasChildren()) {
      html.startElement(node, TOPIC_ALT, ALT_ATTS);
      if (alt != null) {
        html.characters(alt);
      } else {
        context.renderChildren(node);
      }
      html.endElement();
    }
    html.endElement();
  }

  private void writeImage(
    ImageRef node,
    final String title,
    final String alt,
    final AttributesBuilder atts,
    final NodeRendererContext context,
    SaxWriter html
  ) {
    html.startElement(node, TOPIC_IMAGE, atts.build());
    if (node.hasChildren()) {
      html.startElement(node, TOPIC_ALT, ALT_ATTS);
      if (alt != null) {
        html.characters(alt);
      } else {
        context.renderChildren(node);
      }
      html.endElement();
    }
    html.endElement(); // image
  }

  private void render(final Heading node, final NodeRendererContext context, final SaxWriter html) {
    html.startElement(node, TOPIC_TITLE, TITLE_ATTS);
    context.renderChildren(node);
    html.endElement(); // title
    if (node.getLevel() == 1) {
      final Node firstChild = node.getDocument().getFirstChild();
      if (firstChild instanceof YamlFrontMatterBlock) {
        html.startElement(firstChild, MAP_TOPICMETA, TOPICMETA_ATTS);
        metadataSerializer.render((YamlFrontMatterBlock) firstChild, context, html);
        html.endElement();
      }
    }
  }

  private String getSectionId(Heading node, Title header) {
    if (header != null) {
      if (node.getAnchorRefId() != null) {
        return node.getAnchorRefId();
      } else {
        return null;
        //                return getId(header.title);
      }
    } else {
      if (node.getAnchorRefId() != null) {
        return node.getAnchorRefId();
      } else {
        return getId(node.getText().toString());
      }
    }
  }

  private String getTopicId(final Heading node, final Title header) {
    if (idFromYaml && node.getLevel() == 1 && node.getDocument().getChildOfType(YamlFrontMatterBlock.class) != null) {
      final AbstractYamlFrontMatterVisitor v = new AbstractYamlFrontMatterVisitor();
      v.visit(node.getDocument());
      final Map<String, List<String>> metadata = v.getData();
      final List<String> ids = metadata.get("id");
      if (ids != null && !ids.isEmpty()) {
        return ids.get(0);
      }
    }
    return getSectionId(node, header);
  }

  private void render(final YamlFrontMatterBlock node, final NodeRendererContext context, final SaxWriter html) {
    // YAML header is pulled by Heading renderer
  }

  private static String getId(final String contents) {
    return contents.toLowerCase().replaceAll("[^\\w]", "").trim().replaceAll("\\s+", "_");
  }

  /**
   * Render HTML block into DITA.
   */
  private void render(final HtmlBlock node, final NodeRendererContext context, final SaxWriter html) {
    final String text = node.getChars().toString();
    final FragmentContentHandler fragmentFilter = new FragmentContentHandler();
    fragmentFilter.setContentHandler(html);
    final TransformerHandler h;
    try {
      h = transformerFactorySupplier.get().newTransformerHandler(templatesSupplier.get());
    } catch (final TransformerConfigurationException e) {
      throw new RuntimeException(e);
    }
    h.getTransformer().setParameter("formats", String.join(",", formats));
    h.setResult(new SAXResult(fragmentFilter));
    final HtmlParser parser = new HtmlParser();
    parser.setContentHandler(h);
    try {
      html.setLocation(node);
      parser.parse(new InputSource(new StringReader(text)));
    } catch (IOException | SAXException e) {
      throw new ParseException("Failed to parse HTML: " + e.getMessage(), e);
    }
    html.setDocumentLocator();
  }

  private static Stream<Entry<String, Entry<DitaClass, Attributes>>> createHtmlToDita(Entry<String, DitaClass> e) {
    final Entry<DitaClass, Attributes> value = new SimpleImmutableEntry<>(e.getValue(), buildAtts(e.getValue()));
    return Stream.of(
      new SimpleImmutableEntry<>("<" + e.getKey() + ">", value),
      new SimpleImmutableEntry<>("</" + e.getKey() + ">", value)
    );
  }

  private static final Map<String, Entry<DitaClass, Attributes>> htmlToDita;
  private static final Map<String, Entry<DitaClass, Attributes>> hditaToXdita;

  static {
    htmlToDita =
      Stream
        .of(
          new SimpleImmutableEntry<>("span", TOPIC_PH),
          new SimpleImmutableEntry<>("code", PR_D_CODEPH),
          new SimpleImmutableEntry<>("s", HI_D_LINE_THROUGH),
          new SimpleImmutableEntry<>("tt", HI_D_TT),
          new SimpleImmutableEntry<>("b", HI_D_B),
          new SimpleImmutableEntry<>("strong", HI_D_B),
          new SimpleImmutableEntry<>("i", HI_D_I),
          new SimpleImmutableEntry<>("em", HI_D_I),
          new SimpleImmutableEntry<>("sub", HI_D_SUB),
          new SimpleImmutableEntry<>("sup", HI_D_SUP),
          new SimpleImmutableEntry<>("u", HI_D_U)
        )
        .flatMap(MapRenderer::createHtmlToDita)
        .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue));
    hditaToXdita =
      Stream
        .<Entry<String, DitaClass>>of(
          new SimpleImmutableEntry<>("span", TOPIC_PH),
          new SimpleImmutableEntry<>("code", TOPIC_PH),
          new SimpleImmutableEntry<>("s", TOPIC_PH),
          new SimpleImmutableEntry<>("tt", HI_D_TT),
          new SimpleImmutableEntry<>("b", HI_D_B),
          new SimpleImmutableEntry<>("strong", HI_D_B),
          new SimpleImmutableEntry<>("i", HI_D_I),
          new SimpleImmutableEntry<>("em", HI_D_I),
          new SimpleImmutableEntry<>("sub", HI_D_SUB),
          new SimpleImmutableEntry<>("sup", HI_D_SUP),
          new SimpleImmutableEntry<>("u", HI_D_U)
        )
        .flatMap(MapRenderer::createHtmlToDita)
        .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue));
  }

  /**
   * Render inline HTML start or end tag. Start tags may contain attributes.
   */
  private void render(final HtmlInline node, final NodeRendererContext context, final SaxWriter html) {
    final String text = node.getChars().toString();
    final Entry<DitaClass, Attributes> entry = (mditaExtendedProfile ? hditaToXdita : htmlToDita).get(text);
    if (entry != null) {
      final DitaClass cls = entry.getKey();
      html.setLocation(node);
      if (text.startsWith("</")) {
        html.endElement(cls);
      } else {
        html.startElement(node, cls, entry.getValue());
      }
      return;
    }

    final TransformerHandler h;
    try {
      h = transformerFactorySupplier.get().newTransformerHandler(templatesSupplier.get());
    } catch (final TransformerConfigurationException e) {
      throw new RuntimeException(e);
    }
    h.getTransformer().setParameter("formats", String.join(",", formats));
    final HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALLOW);
    parser.setContentHandler(h);
    html.setLocation(node);
    if (text.startsWith("</")) {
      h.setResult(new SAXResult(new EndElementHandler(html)));
      final String data = text.replaceAll("/", "") + text;
      try (final StringReader in = new StringReader(data)) {
        parser.parse(new InputSource(in));
      } catch (IOException | SAXException e) {
        throw new ParseException("Failed to parse HTML: " + e.getMessage(), e);
      }
    } else {
      h.setResult(new SAXResult(new StartElementHandler(html)));
      try (final StringReader in = new StringReader(text)) {
        parser.parse(new InputSource(in));
      } catch (IOException | SAXException e) {
        throw new ParseException("Failed to parse HTML: " + e.getMessage(), e);
      }
    }
    html.setDocumentLocator();
  }

  private static final Class<? extends Node>[] INLINE = List
    .of(
      Text.class,
      TextBase.class,
      Code.class,
      Emphasis.class,
      StrongEmphasis.class,
      Superscript.class,
      Subscript.class,
      Strikethrough.class
    )
    .toArray(new Class[0]);

  private void render(final ListItem node, final NodeRendererContext context, final SaxWriter html) {
    final Paragraph paragraph = (Paragraph) node.getChildOfType(Paragraph.class);
    final Link link = paragraph != null ? (Link) paragraph.getChildOfType(Link.class) : null;
    final LinkRef linkRef = paragraph != null ? (LinkRef) paragraph.getChildOfType(LinkRef.class) : null;
    final DitaClass name;
    final AttributesBuilder atts;
    List<Node> navtitle = null;
    if (link != null || linkRef != null) {
      name = MAP_TOPICREF;
      atts = new AttributesBuilder(TOPICREF_ATTS);
      if (link != null) {
        atts.addAll(getLinkAttributes(link.getUrl().toString(), TOPICREF_ATTS).build());
        final String text = link.getText().toString();
        if (!text.isEmpty()) {
          navtitle = childList(link);
        }
      }
      if (linkRef != null) {
        final String text = linkRef.getText().toString();
        final String key = linkRef.getReference() != null ? linkRef.getReference().toString() : text;
        final Reference refNode = linkRef.getReferenceNode(linkRef.getDocument());
        if (refNode == null) { // "fake" reference link
          atts.add(ATTRIBUTE_NAME_KEYREF, key);
          if (!text.isBlank()) {
            navtitle = childList(linkRef);
          }
        } else {
          atts.addAll(getLinkAttributes(refNode.getUrl().toString(), TOPICREF_ATTS).build());
          atts.add(ATTRIBUTE_NAME_KEYREF, refNode.getReference().toString());
          if (!refNode.getTitle().toString().isEmpty()) {
            navtitle = childList(refNode);
          } else if (text != null && !text.isBlank()) {
            navtitle = childList(linkRef);
          }
        }
      }
    } else {
      if (mditaCoreProfile || mditaExtendedProfile) {
        name = MAP_TOPICREF;
        atts = new AttributesBuilder(TOPICREF_ATTS);
      } else {
        name = MAPGROUP_D_TOPICHEAD;
        atts = new AttributesBuilder(TOPICHEAD_ATTS);
      }
      navtitle = new ArrayList<>();
      Node child = paragraph.getFirstChild();
      while (child != null) {
        Node next = child.getNext();
        if (child.isOrDescendantOfType(INLINE)) {
          navtitle.add(child);
        } else if (child instanceof BulletList || child instanceof OrderedList) {
          break;
        }
        child = next;
      }
    }

    if (node instanceof OrderedListItem) {
      if (!mditaCoreProfile && !mditaExtendedProfile) {
        atts.add("collection-type", "sequence");
      }
    }

    html.startElement(node, name, getInlineAttributes(node, atts.build()));
    renderNavtitle(node, context, html, navtitle);
    Node child = node.getFirstChild();
    while (child != null) {
      Node next = child.getNext();
      if (child instanceof BulletList || child instanceof OrderedList) {
        context.renderChildren(child);
      }
      child = next;
    }
    html.endElement();
  }

  private void renderNavtitle(
    final Node node,
    final NodeRendererContext context,
    final SaxWriter html,
    final List<Node> navtitle
  ) {
    if (navtitle != null && !navtitle.isEmpty()) {
      html.startElement(node, MAP_TOPICMETA, TOPICMETA_ATTS);
      html.startElement(node, TOPIC_NAVTITLE, NAVTITLE_ATTS);
      for (Node child : navtitle) {
        context.renderChild(child);
      }
      html.endElement();
      html.endElement();
    }
  }

  private void render(final MailLink node, final NodeRendererContext context, final SaxWriter html) {
    final AttributesBuilder atts = getLinkAttributes("mailto:" + node.getText());
    atts.add(ATTRIBUTE_NAME_FORMAT, "email");

    html.startElement(node, TOPIC_XREF, getInlineAttributes(node, atts.build()));
    context.renderChildren(node);
    html.endElement();
  }

  private void render(final OrderedList node, final NodeRendererContext context, final SaxWriter html) {
    context.renderChildren(node);
  }

  private final boolean onlyImageChild = false;

  private boolean isAttributesParagraph(final Node node) {
    if (node == null) {
      return false;
    }
    final Node firstChild = node.getFirstChild();
    return firstChild instanceof AttributesNode && firstChild.getNext() == null;
  }

  private void render(final Reference node, final NodeRendererContext context, final SaxWriter html) {
    final Attributes atts = getLinkAttributes(node.getUrl().toString(), KEYDEF_ATTS)
      .add(ATTRIBUTE_NAME_KEYS, node.getReference().toString())
      .build();
    html.startElement(node, MAPGROUP_D_KEYDEF, atts);
    html.endElement();
  }

  private void render(final ImageRef node, final NodeRendererContext context, final SaxWriter html) {
    final String text = node.getText().toString();
    final String key = node.getReference() != null ? node.getReference().toString() : text;
    final Reference refNode = node.getReferenceNode(node.getDocument());
    if (refNode == null) { // "fake" reference image link
      final AttributesBuilder atts = new AttributesBuilder(IMAGE_ATTS).add(ATTRIBUTE_NAME_KEYREF, key);
      if (onlyImageChild) {
        atts.add("placement", "break");
      }
      html.startElement(node, TOPIC_IMAGE, getInlineAttributes(node, atts.build()));
      html.endElement();
    } else {
      final AttributesBuilder atts = new AttributesBuilder(getInlineAttributes(node, IMAGE_ATTS))
        .add(ATTRIBUTE_NAME_HREF, refNode.getUrl().toString());
      if (key != null) {
        atts.add(ATTRIBUTE_NAME_KEYREF, key);
      }
      writeImage(node, refNode.getTitle().toString(), text, atts, context, html);
    }
  }

  private void render(final RefNode node, final NodeRendererContext context, final SaxWriter html) {
    final String text = node.getText().toString();
    final String key = node.getReference() != null ? node.getReference().toString() : text;
    final Reference refNode = node.getReferenceNode(node.getDocument());
    if (refNode == null) { // "fake" reference link
      final AttributesBuilder atts = new AttributesBuilder(TOPICREF_ATTS).add(ATTRIBUTE_NAME_KEYREF, key);
      html.startElement(node, MAP_TOPICREF, atts.build());
      if (!node.getText().toString().isEmpty()) {
        renderNavtitle(node, context, html, childList(node));
        //        html.characters(node.getText().toString());
      }
      html.endElement();
    } else {
      final AttributesBuilder atts = getLinkAttributes(refNode.getUrl().toString());
      html.startElement(node, MAP_TOPICREF, atts.build());
      if (!refNode.getTitle().toString().isEmpty()) {
        //        html.characters(refNode.getTitle().toString());
        renderNavtitle(node, context, html, childList(refNode));
      } else {
        //        context.renderChildren(node);
        renderNavtitle(node, context, html, childList(node));
      }
      html.endElement();
    }
  }

  private void render(final Link node, final NodeRendererContext context, final SaxWriter html) {
    final String text = node.getText().toString();
    //        final String key = node.getReference() != null ? node.getReference().toString() : text;
    //        final ReferenceNode refNode = node.getReferenceNode(node.getDocument());
    //        if (refNode == null) { // "fake" reference link
    //            final AttributesBuilder atts = new AttributesBuilder(XREF_ATTS)
    //                    .add(ATTRIBUTE_NAME_KEYREF, key);
    //            html.startElement(TOPIC_XREF, atts.build());
    //            if (node.getReference() != null) {
    //                context.renderChildren(node);
    //            }
    //            html.endElement();
    //        } else {
    final AttributesBuilder atts = getLinkAttributes(node.getUrl().toString());
    html.startElement(node, MAP_TOPICREF, getInlineAttributes(node, atts.build()));
    //            if (!refNode.getTitle().toString().isEmpty()) {
    //                html.characters(refNode.toString());
    //            } else {
    //    context.renderChildren(node);
    renderNavtitle(node, context, html, childList(node));
    //            }
    html.endElement();
    //        }
  }

  private void renderSimpleTableBlock(final TableBlock node, final NodeRendererContext context, final SaxWriter html) {
    //        currentTableNode = node;
    final Attributes tableAtts;
    if (!mditaExtendedProfile && isAttributesParagraph(node.getNext())) {
      final Title header = Title.getFromChildren(node.getNext());
      final AttributesBuilder builder = new AttributesBuilder(RELTABLE_ATTS);
      tableAtts = readAttributes(header, builder).build();
    } else {
      tableAtts = RELTABLE_ATTS;
    }
    html.startElement(node, MAP_RELTABLE, tableAtts);

    context.renderChildren(node);
    //        html.endElement(); // tgroup
    html.endElement(); // table
    //        currentTableNode = null;
  }

  private void renderSimpleTableHead(final TableHead node, final NodeRendererContext context, final SaxWriter html) {
    //        printTag(node, context, html, TOPIC_STHEAD, STHEAD_ATTS);
    context.renderChildren(node);
  }

  private void renderSimpleTableBody(final TableBody node, final NodeRendererContext context, final SaxWriter html) {
    //        html.startElement(TOPIC_TBODY, TBODY_ATTS);
    context.renderChildren(node);
    //        html.endElement();
  }

  private void renderSimpleTableSeparator(TableSeparator node, NodeRendererContext context, SaxWriter html) {
    // Ignore
  }

  private void renderSimpleTableRow(final TableRow node, final NodeRendererContext context, final SaxWriter html) {
    if (node.getParent() instanceof TableHead) {
      printTag(node, context, html, MAP_RELHEADER, RELHEADER_ATTS);
    } else {
      printTag(node, context, html, MAP_RELROW, RELROW_ATTS);
    }
  }

  private void renderSimpleTableCell(final TableCell node, final NodeRendererContext context, final SaxWriter html) {
    final boolean isHeader = node.getParent().getParent().isOrDescendantOfType(TableHead.class);
    final AttributesBuilder atts = new AttributesBuilder(isHeader ? RELCOLSPEC_ATTS : RELCELL_ATTS);
    html.startElement(node, isHeader ? MAP_RELCOLSPEC : MAP_RELCELL, atts.build());
    context.renderChildren(node);
    html.endElement();
  }

  // Code block

  private void render(final Text node, final NodeRendererContext context, final SaxWriter html) {
    if (node.getParent() instanceof Code) {
      html.characters(node.getChars().toString());
    } else {
      html.characters(node.getChars().unescapeNoEntities());
    }
  }

  private void render(final ContentNode node, final NodeRendererContext context, final SaxWriter html) {
    context.renderChildren(node);
  }

  private void render(final SoftLineBreak node, final NodeRendererContext context, final SaxWriter html) {
    html.characters('\n');
  }

  private void render(final Node node, final NodeRendererContext context, final SaxWriter html) {
    throw new RuntimeException(
      "No renderer configured for " + node.getNodeName() + " = " + node.getClass().getCanonicalName()
    );
  }

  // helpers

  @Override
  protected AttributesBuilder getLinkAttributes(final String href) {
    return getLinkAttributes(href, TOPICREF_ATTS);
  }
}
