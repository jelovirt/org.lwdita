/*
 * Based on ToHtmlSerializer (C) 2010-2011 Mathias Doenitz
 */
package com.elovirta.dita.markdown.renderer;

import static com.elovirta.dita.markdown.MetadataSerializerImpl.buildAtts;
import static javax.xml.XMLConstants.XML_NS_URI;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.XMLUtils.AttributesBuilder;

import com.elovirta.dita.markdown.DitaRenderer;
import com.elovirta.dita.markdown.MetadataSerializerImpl;
import com.elovirta.dita.markdown.ParseException;
import com.elovirta.dita.markdown.SaxWriter;
import com.elovirta.dita.utils.FragmentContentHandler;
import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.ext.abbreviation.Abbreviation;
import com.vladsch.flexmark.ext.abbreviation.AbbreviationBlock;
import com.vladsch.flexmark.ext.admonition.AdmonitionBlock;
import com.vladsch.flexmark.ext.anchorlink.AnchorLink;
import com.vladsch.flexmark.ext.attributes.AttributesNode;
import com.vladsch.flexmark.ext.definition.DefinitionItem;
import com.vladsch.flexmark.ext.definition.DefinitionList;
import com.vladsch.flexmark.ext.definition.DefinitionTerm;
import com.vladsch.flexmark.ext.footnotes.Footnote;
import com.vladsch.flexmark.ext.footnotes.FootnoteBlock;
import com.vladsch.flexmark.ext.jekyll.tag.JekyllTag;
import com.vladsch.flexmark.ext.jekyll.tag.JekyllTagBlock;
import com.vladsch.flexmark.ext.tables.*;
import com.vladsch.flexmark.ext.typographic.TypographicQuotes;
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterBlock;
import com.vladsch.flexmark.util.ast.ContentNode;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.ReferenceNode;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
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
public class TopicRenderer extends AbstractRenderer {

  private static final String COLUMN_NAME_COL = "col";
  private static final String ATTRIBUTE_NAME_COLSPAN = "colspan";
  private static final Attributes TOPIC_ATTS = new AttributesBuilder()
    .add(ATTRIBUTE_NAME_CLASS, TOPIC_TOPIC.toString())
    .add(
      DITA_NAMESPACE,
      ATTRIBUTE_NAME_DITAARCHVERSION,
      ATTRIBUTE_PREFIX_DITAARCHVERSION + ":" + ATTRIBUTE_NAME_DITAARCHVERSION,
      "CDATA",
      "2.0"
    )
    //            .add(ATTRIBUTE_NAME_DOMAINS, "(topic hi-d) (topic ut-d) (topic indexing-d) (topic hazard-d) (topic abbrev-d) (topic pr-d) (topic sw-d) (topic ui-d)")
    .build();
  private static final Attributes BODY_ATTS = buildAtts(TOPIC_BODY);
  private static final Attributes NOTE_ATTS = buildAtts(TOPIC_NOTE);
  private static final Attributes FN_ATTS = buildAtts(TOPIC_FN);
  private static final Attributes LI_ATTS = buildAtts(TOPIC_LI);
  private static final Attributes P_ATTS = buildAtts(TOPIC_P);
  private static final Attributes DD_ATTS = buildAtts(TOPIC_DD);
  private static final Attributes CODEBLOCK_ATTS = buildAtts(PR_D_CODEBLOCK);
  private static final Attributes PRE_ATTS = buildAtts(TOPIC_PRE);
  private static final Attributes DT_ATTS = buildAtts(TOPIC_DT);
  private static final Attributes DEL_ATTS = new AttributesBuilder()
    .add(ATTRIBUTE_NAME_CLASS, TOPIC_PH.toString())
    .add("status", "deleted")
    .build();
  private static final Attributes SHORTDESC_ATTS = buildAtts(TOPIC_SHORTDESC);
  private static final Attributes PROLOG_ATTS = buildAtts(TOPIC_PROLOG);
  private static final Attributes BLOCKQUOTE_ATTS = buildAtts(TOPIC_LQ);
  private static final Attributes UL_ATTS = buildAtts(TOPIC_UL);
  private static final Attributes DL_ATTS = buildAtts(TOPIC_DL);
  private static final Attributes DLENTRY_ATTS = buildAtts(TOPIC_DLENTRY);
  private static final Attributes OL_ATTS = buildAtts(TOPIC_OL);
  private static final Attributes TABLE_ATTS = buildAtts(TOPIC_TABLE);
  private static final Attributes TGROUP_ATTS = buildAtts(TOPIC_TGROUP);
  private static final Attributes COLSPEC_ATTS = buildAtts(TOPIC_COLSPEC);
  private static final Attributes TBODY_ATTS = buildAtts(TOPIC_TBODY);
  private static final Attributes THEAD_ATTS = buildAtts(TOPIC_THEAD);
  private static final Attributes TR_ATTS = buildAtts(TOPIC_ROW);
  private static final Attributes SIMPLETABLE_ATTS = buildAtts(TOPIC_SIMPLETABLE);
  private static final Attributes STHEAD_ATTS = buildAtts(TOPIC_STHEAD);
  private static final Attributes STROW_ATTS = buildAtts(TOPIC_STROW);
  private static final Attributes STENTRY_ATTS = buildAtts(TOPIC_STENTRY);
  private static final Attributes XREF_ATTS = buildAtts(TOPIC_XREF);
  private static final Attributes ENTRY_ATTS = buildAtts(TOPIC_ENTRY);
  private static final Attributes FIG_ATTS = buildAtts(TOPIC_FIG);
  private static final Attributes REQUIRED_CLEANUP_ATTS = buildAtts(TOPIC_REQUIRED_CLEANUP);

  private static final Map<String, DitaClass> sections = new HashMap<>();

  static {
    sections.put(TOPIC_SECTION.localName, TOPIC_SECTION);
    sections.put(TOPIC_EXAMPLE.localName, TOPIC_EXAMPLE);
  }

  private final Map<String, String> abbreviations = new HashMap<>();
  private final MetadataSerializerImpl metadataSerializer;

  private final boolean shortdescParagraph;
  private final boolean idFromYaml;
  private final boolean tightList;

  //  private TableBlock currentTableNode;
  private int currentTableColumn;
  private boolean inSection = false;

  private final Set<String> footnotes = new HashSet<>();
  private String lastId;

  /**
   * Current header level.
   */
  private int headerLevel = 0;

  public TopicRenderer(DataHolder options) {
    super(options);
    shortdescParagraph = DitaRenderer.SHORTDESC_PARAGRAPH.get(options);
    idFromYaml = DitaRenderer.ID_FROM_YAML.get(options);
    tightList = DitaRenderer.TIGHT_LIST.get(options);
    metadataSerializer = new MetadataSerializerImpl(idFromYaml);
  }

  @Override
  public Map<Class<? extends Node>, NodeRenderingHandler<? extends Node>> getNodeRenderingHandlers() {
    final List<NodeRenderingHandler> res = new ArrayList<>(super.getNodeRenderingHandlers().values());
    if (mditaCoreProfile || mditaExtendedProfile) {
      res.add(
        new NodeRenderingHandler<>(
          TableBlock.class,
          (node, context, html) -> renderSimpleTableBlock(node, context, html)
        )
      );
      res.add(
        new NodeRenderingHandler<>(
          TableCaption.class,
          (node, context, html) -> renderSimpleTableCaption(node, context, html)
        )
      );
      res.add(
        new NodeRenderingHandler<>(TableBody.class, (node, context, html) -> renderSimpleTableBody(node, context, html))
      );
      res.add(
        new NodeRenderingHandler<>(TableHead.class, (node, context, html) -> renderSimpleTableHead(node, context, html))
      );
      res.add(
        new NodeRenderingHandler<>(TableRow.class, (node, context, html) -> renderSimpleTableRow(node, context, html))
      );
      res.add(
        new NodeRenderingHandler<>(TableCell.class, (node, context, html) -> renderSimpleTableCell(node, context, html))
      );
      res.add(
        new NodeRenderingHandler<>(
          TableSeparator.class,
          (node, context, html) -> renderSimpleTableSeparator(node, context, html)
        )
      );
    } else {
      res.add(new NodeRenderingHandler<>(TableBlock.class, (node, context, html) -> render(node, context, html)));
      res.add(new NodeRenderingHandler<>(TableCaption.class, (node, context, html) -> render(node, context, html)));
      res.add(new NodeRenderingHandler<>(TableBody.class, (node, context, html) -> render(node, context, html)));
      res.add(new NodeRenderingHandler<>(TableHead.class, (node, context, html) -> render(node, context, html)));
      res.add(new NodeRenderingHandler<>(TableRow.class, (node, context, html) -> render(node, context, html)));
      res.add(new NodeRenderingHandler<>(TableCell.class, (node, context, html) -> render(node, context, html)));
      res.add(new NodeRenderingHandler<>(TableSeparator.class, (node, context, html) -> render(node, context, html)));
    }
    if (!mditaCoreProfile) {
      res.add(
        new NodeRenderingHandler<>(
          AttributesNode.class,
          (node, context, html) -> {
            /* Ignore */
          }
        )
      );
      res.add(new NodeRenderingHandler<>(DefinitionList.class, (node, context, html) -> render(node, context, html)));
      res.add(new NodeRenderingHandler<>(DefinitionTerm.class, (node, context, html) -> render(node, context, html)));
      res.add(new NodeRenderingHandler<>(DefinitionItem.class, (node, context, html) -> render(node, context, html)));
      res.add(new NodeRenderingHandler<>(Footnote.class, (node, context, html) -> render(node, context, html)));
      res.add(new NodeRenderingHandler<>(FootnoteBlock.class, (node, context, html) -> render(node, context, html)));
    }
    if (!mditaCoreProfile && !mditaExtendedProfile) {
      res.add(new NodeRenderingHandler<>(Abbreviation.class, (node, context, html) -> render(node, context, html)));
      res.add(
        new NodeRenderingHandler<>(AbbreviationBlock.class, (node, context, html) -> render(node, context, html))
      );
      res.add(new NodeRenderingHandler<>(AdmonitionBlock.class, (node, context, html) -> render(node, context, html)));
    }
    res.add(new NodeRenderingHandler<>(AutoLink.class, (node, context, html) -> render(node, context, html)));
    res.add(
      new NodeRenderingHandler<>(YamlFrontMatterBlock.class, (node, context, html) -> render(node, context, html))
    );
    res.add(new NodeRenderingHandler<>(BlockQuote.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(BulletList.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(CodeBlock.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(Document.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(FencedCodeBlock.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(HardLineBreak.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(Heading.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(HtmlBlock.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(HtmlCommentBlock.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(HtmlInnerBlock.class, (node, context, html) -> render(node, context, html)));
    res.add(
      new NodeRenderingHandler<>(HtmlInnerBlockComment.class, (node, context, html) -> render(node, context, html))
    );
    res.add(new NodeRenderingHandler<>(HtmlInline.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(Image.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(ImageRef.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(IndentedCodeBlock.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(Link.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(LinkRef.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(BulletListItem.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(OrderedListItem.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(MailLink.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(OrderedList.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(Paragraph.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(Reference.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(SoftLineBreak.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(Text.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(ThematicBreak.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(JekyllTagBlock.class, (node, context, html) -> render(node, context, html)));
    res.add(new NodeRenderingHandler<>(JekyllTag.class, (node, context, html) -> render(node, context, html)));

    final HashMap map = new HashMap(super.getNodeRenderingHandlers());
    map.putAll(res.stream().collect(Collectors.toMap(handler -> handler.getNodeType(), handler -> handler)));
    return map;
  }

  // Visitor methods

  private void render(final Document node, final NodeRendererContext context, final SaxWriter html) {
    final boolean isCompound = hasMultipleTopLevelHeaders(node);
    if (isCompound) {
      final AttributesBuilder atts = new AttributesBuilder()
        .add(
          DITA_NAMESPACE,
          ATTRIBUTE_NAME_DITAARCHVERSION,
          ATTRIBUTE_PREFIX_DITAARCHVERSION + ":" + ATTRIBUTE_NAME_DITAARCHVERSION,
          "CDATA",
          "2.0"
        );
      if (mditaCoreProfile || mditaExtendedProfile) {
        atts.add(ATTRIBUTE_NAME_SPECIALIZATIONS, "(topic hi-d)(topic em-d)");
      } else {
        atts.add(
          ATTRIBUTE_NAME_SPECIALIZATIONS,
          "@props/audience @props/deliveryTarget @props/otherprops @props/platform @props/product"
        );
      }
      html.startElement(node, ELEMENT_NAME_DITA, atts.build());
    }
    context.renderChildren(node);
    if (isCompound) {
      html.endElement();
    }
  }

  private void render(final Abbreviation node, final NodeRendererContext context, final SaxWriter html) {
    html.characters(node.getChars().toString());
  }

  private void render(final AbbreviationBlock node, final NodeRendererContext context, final SaxWriter html) {
    // Ignore
  }

  private void render(final AdmonitionBlock node, final NodeRendererContext context, final SaxWriter html) {
    final String type = node.getInfo().toString();
    final AttributesBuilder atts = new AttributesBuilder(NOTE_ATTS);
    switch (type) {
      case "note":
      case "tip":
      case "fastpath":
      case "restriction":
      case "important":
      case "remember":
      case "attention":
      case "caution":
      case "notice":
      case "danger":
      case "warning":
      case "trouble":
        atts.add("type", type);
        break;
      default:
        atts.add("type", "other").add("othertype", type);
        break;
    }
    html.startElement(node, TOPIC_NOTE, atts.build());
    if (!node.getTitle().isEmpty()) {
      html.startElement(node, TOPIC_P, P_ATTS);
      html.characters(node.getTitle().toString());
      html.endElement();
    }
    context.renderChildren(node);
    html.endElement();
  }

  private void render(JekyllTagBlock node, final NodeRendererContext context, final SaxWriter html) {
    context.renderChildren(node);
  }

  private void render(JekyllTag node, final NodeRendererContext context, final SaxWriter html) {
    if (node.getTag().toString().equals("include")) {
      final AttributesBuilder atts = new AttributesBuilder(REQUIRED_CLEANUP_ATTS)
        .add(ATTRIBUTE_NAME_CONREF, node.getParameters().toString());
      html.startElement(node, TOPIC_REQUIRED_CLEANUP, atts.build());
      html.endElement();
    }
  }

  private void render(final AutoLink node, final NodeRendererContext context, final SaxWriter html) {
    if (node.getChars().charAt(0) == '<') {
      final AttributesBuilder atts = getLinkAttributes(node.getText().toString());

      html.startElement(node, TOPIC_XREF, getInlineAttributes(node, atts.build()));
      html.characters(node.getText().toString());
      html.endElement();
    } else {
      context.renderChildren(node);
    }
  }

  private void render(final Footnote node, final NodeRendererContext context, final SaxWriter html) {
    final String callout = node.getText().toString().trim();
    final String id = getId("fn_" + callout);
    if (footnotes.contains(id)) {
      final Attributes atts = new AttributesBuilder(XREF_ATTS)
        .add(ATTRIBUTE_NAME_TYPE, "fn")
        .add(ATTRIBUTE_NAME_HREF, String.format("#%s/%s", lastId, id))
        .build();
      html.startElement(node, TOPIC_XREF, atts);
      html.endElement();
    } else {
      footnotes.add(id);
      final Attributes atts = new AttributesBuilder(FN_ATTS).add("callout", callout).add(ATTRIBUTE_NAME_ID, id).build();
      html.startElement(node, TOPIC_FN, atts);
      Node child = node.getFootnoteBlock().getFirstChild();
      while (child != null) {
        context.renderChildren(child);
        child = child.getNext();
      }
      html.endElement();
    }
  }

  private void render(final FootnoteBlock node, final NodeRendererContext context, final SaxWriter html) {
    // Ignore
  }

  private void render(final BlockQuote node, final NodeRendererContext context, final SaxWriter html) {
    if (mditaCoreProfile || mditaExtendedProfile) {
      context.renderChildren(node);
    } else {
      printTag(node, context, html, TOPIC_LQ, getAttributesFromAttributesNode(node, BLOCKQUOTE_ATTS));
    }
  }

  private void render(final BulletList node, final NodeRendererContext context, final SaxWriter html) {
    printTag(node, context, html, TOPIC_UL, getAttributesFromAttributesNode(node, UL_ATTS));
  }

  private void render(final DefinitionList node, final NodeRendererContext context, final SaxWriter html) {
    html.startElement(node, TOPIC_DL, getAttributesFromAttributesNode(node, DL_ATTS));
    DitaClass previous = null;
    //        for (final Node child : node.getChildren()) {
    //            if (previous == null) {
    //                html.startElement(TOPIC_DLENTRY, DLENTRY_ATTS);
    //            }
    //            if (child instanceof DefinitionTermNode) {
    //                if (TOPIC_DD.equals(previous)) {
    //                    html.endElement(); // dlentry
    //                    html.startElement(TOPIC_DLENTRY, DLENTRY_ATTS);
    //                }
    //            }
    context.renderChildren(node);
    //            previous = (child instanceof DefinitionTermNode) ? TOPIC_DT : TOPIC_DD;
    //        }
    //        html.endElement(); // dlentry
    html.endElement(); // dl
  }

  private void render(final DefinitionTerm node, final NodeRendererContext context, final SaxWriter html) {
    if (node.getPrevious() == null || !(node.getPrevious() instanceof DefinitionTerm)) {
      html.startElement(node, TOPIC_DLENTRY, DLENTRY_ATTS);
    }
    //        printTag(node, context, html, TOPIC_DT, DT_ATTS);
    html.startElement(node, TOPIC_DT, DT_ATTS);
    Node child = node.getFirstChild();
    while (child != null) {
      Node next = child.getNext();
      context.renderChildren(child);
      child = next;
    }
    html.endElement();
  }

  private void render(final DefinitionItem node, final NodeRendererContext context, final SaxWriter html) {
    printTag(node, context, html, TOPIC_DD, DD_ATTS);
    if (node.getNext() == null || !(node.getNext() instanceof DefinitionItem)) {
      html.endElement();
    }
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
    if (!title.isEmpty()) {
      html.startElement(node, TOPIC_FIG, FIG_ATTS);
      html.startElement(node, TOPIC_TITLE, TITLE_ATTS);
      html.characters(title);
      html.endElement();
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
      html.endElement();
    } else {
      if (onlyImageChild) {
        atts.add("placement", "break");
      }
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
  }

  private void writeImage(
    ImageRef node,
    final String title,
    final String alt,
    final AttributesBuilder atts,
    final NodeRendererContext context,
    SaxWriter html
  ) {
    if (!title.isEmpty()) {
      html.startElement(node, TOPIC_FIG, FIG_ATTS);
      html.startElement(node, TOPIC_TITLE, TITLE_ATTS);
      html.characters(title);
      html.endElement();
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
      html.endElement(); // fig
    } else {
      if (onlyImageChild) {
        atts.add("placement", "break");
      }
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
  }

  private static <E> E containsSome(final Collection<E> col, final Collection<E> find) {
    for (final E c : col) {
      if (find.contains(c)) {
        return c;
      }
    }
    return null;
  }

  private void render(final Heading node, final NodeRendererContext context, final SaxWriter html) {
    final StringBuilder buf = new StringBuilder();
    node.getAstExtra(buf);
    Title header = null;
    if (!mditaCoreProfile) {
      if (node.getFirstChild() instanceof AnchorLink) {
        header = Title.getFromChildren(node.getFirstChild());
      } else {
        header = Title.getFromChildren(node);
      }
      header.id.ifPresent(node::setAnchorRefId);
    }

    if (inSection) {
      html.endElement(); // section or example
      inSection = false;
    }
    final DitaClass cls;
    final boolean isSection;
    if ((mditaCoreProfile || mditaExtendedProfile) && node.getLevel() == 2) {
      isSection = true;
      cls = TOPIC_SECTION;
    } else if (!mditaCoreProfile) {
      final String sectionClassName = containsSome(header.classes, sections.keySet());
      if (sectionClassName != null) {
        isSection = true;
        cls = sections.get(sectionClassName);
      } else {
        isSection = false;
        cls = null;
      }
    } else {
      isSection = false;
      cls = null;
    }
    if (isSection) {
      if (node.getLevel() <= headerLevel) {
        throw new ParseException(
          String.format(
            "Level %d section title must be higher level than parent topic title %d",
            node.getLevel(),
            headerLevel
          )
        );
      }
      final AttributesBuilder atts = new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, cls.toString());
      final String id = getSectionId(node, header);
      if (id != null) {
        atts.add(ATTRIBUTE_NAME_ID, id);
      }
      if (!mditaCoreProfile) {
        final Collection<String> classes = new ArrayList<>(header.classes);
        classes.removeAll(sections.keySet());
        if (!classes.isEmpty()) {
          atts.add("outputclass", String.join(" ", classes));
        }
      }
      html.startElement(node, cls, atts.build());
      inSection = true;
      html.startElement(node, TOPIC_TITLE, TITLE_ATTS);
      context.renderChildren(node);
      html.endElement(); // title
    } else {
      if (headerLevel > 0) {
        html.endElement(); // body
      }
      for (; node.getLevel() <= headerLevel; headerLevel--) {
        html.endElement(); // topic
      }
      headerLevel = node.getLevel();

      final AttributesBuilder atts = mditaCoreProfile || mditaExtendedProfile
        ? new AttributesBuilder(TOPIC_ATTS).add(ATTRIBUTE_NAME_SPECIALIZATIONS, "(topic hi-d)(topic em-d)")
        : new AttributesBuilder(TOPIC_ATTS)
          .add(
            ATTRIBUTE_NAME_SPECIALIZATIONS,
            "@props/audience @props/deliveryTarget @props/otherprops @props/platform @props/product"
          );

      final String id = getTopicId(node, header);
      if (id != null) {
        lastId = id;
        atts.add(ATTRIBUTE_NAME_ID, id);
      }
      if (!mditaCoreProfile) {
        if (!header.classes.isEmpty()) {
          atts.add(ATTRIBUTE_NAME_OUTPUTCLASS, String.join(" ", header.classes));
        }
        for (Entry<String, String> attr : header.attributes.entrySet()) {
          atts.add(attr.getKey(), attr.getValue());
        }
      }
      html.startElement(node, TOPIC_TOPIC, atts.build());
      html.startElement(node, TOPIC_TITLE, TITLE_ATTS);
      context.renderChildren(node);
      html.endElement(); // title
      if (shortdescParagraph && node.getNext() instanceof Paragraph) {
        html.startElement(node.getNext(), TOPIC_SHORTDESC, SHORTDESC_ATTS);
        context.renderChildren(node.getNext());
        html.endElement(); // shortdesc
      }
      if (node.getLevel() == 1) {
        final Node firstChild = node.getDocument().getFirstChild();
        if (firstChild instanceof YamlFrontMatterBlock) {
          html.startElement(firstChild, TOPIC_PROLOG, PROLOG_ATTS);
          metadataSerializer.render((YamlFrontMatterBlock) firstChild, context, html);
          html.endElement();
        }
      }
      html.startElement(node, TOPIC_BODY, BODY_ATTS);
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
        .flatMap(TopicRenderer::createHtmlToDita)
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
        .flatMap(TopicRenderer::createHtmlToDita)
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

  private void render(final ListItem node, final NodeRendererContext context, final SaxWriter html) {
    printTag(node, context, html, TOPIC_LI, LI_ATTS);
  }

  private void render(final MailLink node, final NodeRendererContext context, final SaxWriter html) {
    final AttributesBuilder atts = getLinkAttributes("mailto:" + node.getText());
    atts.add(ATTRIBUTE_NAME_FORMAT, "email");

    html.startElement(node, TOPIC_XREF, getInlineAttributes(node, atts.build()));
    context.renderChildren(node);
    html.endElement();
  }

  private void render(final OrderedList node, final NodeRendererContext context, final SaxWriter html) {
    printTag(node, context, html, TOPIC_OL, getAttributesFromAttributesNode(node, OL_ATTS));
  }

  private boolean onlyImageChild = false;

  /** Check if paragraph only contains a single attributes node. */
  private boolean isAttributesParagraph(final Node node) {
    if (node == null) {
      return false;
    }
    final Node firstChild = node.getFirstChild();
    return firstChild instanceof AttributesNode && firstChild.getNext() == null;
  }

  private void render(final Paragraph node, final NodeRendererContext context, final SaxWriter html) {
    if (isAttributesParagraph(node)) {
      // Attributes for previous block
    } else if (shortdescParagraph && !inSection && node.getPrevious() instanceof Heading) {
      // Pulled by Heading
    } else if (containsImage(node)) {
      onlyImageChild = true;
      context.renderChildren(node);
      onlyImageChild = false;
    } else {
      final Node parent = node.getParent();
      if (
        tightList &&
        parent.isOrDescendantOfType(BulletListItem.class, OrderedListItem.class) &&
        !parent.isOrDescendantOfType(BlockQuote.class) &&
        ((ListItem) parent).isTight()
      ) {
        context.renderChildren(node);
        return;
      }
      final Attributes atts;
      if (!mditaCoreProfile) {
        final Title header = Title.getFromChildren(node);
        final AttributesBuilder builder = new AttributesBuilder(P_ATTS);
        atts = readAttributes(header, builder).build();
      } else {
        atts = P_ATTS;
      }
      printTag(node, context, html, TOPIC_P, atts);
    }
  }

  /**
   * Contains only single image
   */
  private boolean containsImage(final ContentNode node) {
    final Node first = node.getFirstChild();
    if (first instanceof Image || first instanceof ImageRef) {
      return first.getNextAnyNot(AttributesNode.class) == null;
    }
    return false;
  }

  private void render(final TypographicQuotes node, final NodeRendererContext context, final SaxWriter html) {
    //        switch (node.getType()) {
    //            case DoubleAngle:
    //                html.characters('\u00AB');//&laquo;
    //                context.renderChildren(node);
    //                html.characters('\u00AB');//&laquo;
    //                break;
    //            case Double:
    //                html.characters('\u201C');//"&ldquo;"
    //                context.renderChildren(node);
    //                html.characters('\u201C');//"&ldquo;"
    //                break;
    //            case Single:
    html.characters('\u2018'); //"&lsquo;"
    context.renderChildren(node);
    html.characters('\u2018'); //"&lsquo;"
    //                break;
    //        }
  }

  private void render(final ReferenceNode node, final NodeRendererContext context, final SaxWriter html) {
    throw new RuntimeException();
  }

  private void render(final Reference node, final NodeRendererContext context, final SaxWriter html) {
    // Ignore
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
      final AttributesBuilder atts = new AttributesBuilder(XREF_ATTS).add(ATTRIBUTE_NAME_KEYREF, key);
      html.startElement(node, TOPIC_XREF, atts.build());
      if (!node.getText().toString().isEmpty()) {
        html.characters(node.getText().toString());
      }
      html.endElement();
    } else {
      final AttributesBuilder atts = getLinkAttributes(refNode.getUrl().toString());
      html.startElement(node, TOPIC_XREF, atts.build());
      if (!refNode.getTitle().toString().isEmpty()) {
        html.characters(refNode.getTitle().toString());
      } else {
        context.renderChildren(node);
      }
      html.endElement();
    }
  }

  private void render(final Link node, final NodeRendererContext context, final SaxWriter html) {
    final AttributesBuilder atts = getLinkAttributes(node.getUrl().toString());
    html.startElement(node, TOPIC_XREF, getInlineAttributes(node, atts.build()));
    context.renderChildren(node);
    html.endElement();
  }

  // OASIS Table

  private void render(final TableBody node, final NodeRendererContext context, final SaxWriter html) {
    printTag(node, context, html, TOPIC_TBODY, TBODY_ATTS);
  }

  private void render(final TableCaption node, final NodeRendererContext context, final SaxWriter html) {
    // Pull processed by TableBlock
  }

  private void render(final TableCell node, final NodeRendererContext context, final SaxWriter html) {
    final AttributesBuilder atts = new AttributesBuilder(ENTRY_ATTS);
    //        column.accept(this);
    if (node.getAlignment() != null) {
      atts.add(ATTRIBUTE_NAME_ALIGN, node.getAlignment().cellAlignment().name().toLowerCase());
    }
    if (node.getSpan() > 1) {
      atts.add(ATTRIBUTE_NAME_NAMEST, COLUMN_NAME_COL + Integer.toString(currentTableColumn + 1));
      atts.add(ATTRIBUTE_NAME_NAMEEND, COLUMN_NAME_COL + Integer.toString(currentTableColumn + node.getSpan()));
    }
    html.startElement(node, TOPIC_ENTRY, atts.build());
    context.renderChildren(node);
    html.endElement();

    currentTableColumn += node.getSpan();
  }

  private void render(final TableHead node, final NodeRendererContext context, final SaxWriter html) {
    printTag(node, context, html, TOPIC_THEAD, THEAD_ATTS);
  }

  private void render(final TableBlock node, final NodeRendererContext context, final SaxWriter html) {
    //    currentTableNode = node;
    final Attributes tableAtts;
    if (!mditaExtendedProfile && isAttributesParagraph(node.getNext())) {
      final Title header = Title.getFromChildren(node.getNext());
      final AttributesBuilder builder = new AttributesBuilder(TABLE_ATTS);
      tableAtts = readAttributes(header, builder).build();
    } else {
      tableAtts = TABLE_ATTS;
    }
    html.startElement(node, TOPIC_TABLE, tableAtts);
    final Node caption = node.getChildOfType(TableCaption.class);
    if (caption != null) {
      html.startElement(caption, TOPIC_TITLE, TITLE_ATTS);
      context.renderChildren(caption);
      html.endElement();
    }

    final int maxCols = findMaxCols(node);
    final Attributes atts = new AttributesBuilder(TGROUP_ATTS)
      .add(ATTRIBUTE_NAME_COLS, Integer.toString(maxCols))
      .build();
    html.startElement(node, TOPIC_TGROUP, atts);

    for (int i = 0; i < maxCols; i++) {
      final AttributesBuilder catts = new AttributesBuilder(COLSPEC_ATTS)
        .add(ATTRIBUTE_NAME_COLNAME, COLUMN_NAME_COL + (i + 1));
      html.startElement(node, TOPIC_COLSPEC, catts.build());
      html.endElement(); // colspec
    }

    context.renderChildren(node);
    html.endElement(); // tgroup
    html.endElement(); // table
    //    currentTableNode = null;
  }

  private int findMaxCols(TableBlock table) {
    int max = 0;
    for (Node body = table.getFirstChild(); body != null; body = body.getNext()) {
      if (body instanceof TableHead || body instanceof TableBody) {
        for (Node row = body.getFirstChild(); row != null; row = row.getNext()) {
          if (row instanceof TableRow) {
            int colCount = 0;
            for (Node col = row.getFirstChild(); col != null; col = col.getNext()) {
              if (col instanceof TableCell) {
                TableCell c = ((TableCell) col);
                colCount = colCount + c.getSpan();
              }
            }
            max = Math.max(max, colCount);
          }
        }
      }
    }
    return max;
  }

  private void render(TableSeparator node, NodeRendererContext context, SaxWriter html) {
    // Ignore
  }

  private void render(final TableRow node, final NodeRendererContext context, final SaxWriter html) {
    currentTableColumn = 0;
    printTag(node, context, html, TOPIC_ROW, TR_ATTS);
  }

  // Simple table

  private void renderSimpleTableBlock(final TableBlock node, final NodeRendererContext context, final SaxWriter html) {
    //    currentTableNode = node;
    final Attributes tableAtts;
    if (!mditaExtendedProfile && isAttributesParagraph(node.getNext())) {
      final Title header = Title.getFromChildren(node.getNext());
      final AttributesBuilder builder = new AttributesBuilder(SIMPLETABLE_ATTS);
      tableAtts = readAttributes(header, builder).build();
    } else {
      tableAtts = SIMPLETABLE_ATTS;
    }
    html.startElement(node, TOPIC_SIMPLETABLE, tableAtts);
    final Node caption = node.getChildOfType(TableCaption.class);
    if (caption != null) {
      html.startElement(caption, TOPIC_TITLE, TITLE_ATTS);
      context.renderChildren(caption);
      html.endElement();
    }

    context.renderChildren(node);
    html.endElement(); // table
    //    currentTableNode = null;
  }

  private void renderSimpleTableCaption(
    final TableCaption node,
    final NodeRendererContext context,
    final SaxWriter html
  ) {
    // Pull processed by TableBlock
  }

  private void renderSimpleTableHead(final TableHead node, final NodeRendererContext context, final SaxWriter html) {
    context.renderChildren(node);
  }

  private void renderSimpleTableBody(final TableBody node, final NodeRendererContext context, final SaxWriter html) {
    context.renderChildren(node);
  }

  private void renderSimpleTableSeparator(TableSeparator node, NodeRendererContext context, SaxWriter html) {
    // Ignore
  }

  private void renderSimpleTableRow(final TableRow node, final NodeRendererContext context, final SaxWriter html) {
    currentTableColumn = 0;
    if (node.getParent() instanceof TableHead) {
      printTag(node, context, html, TOPIC_STHEAD, STHEAD_ATTS);
    } else {
      printTag(node, context, html, TOPIC_STROW, STROW_ATTS);
    }
  }

  private void renderSimpleTableCell(final TableCell node, final NodeRendererContext context, final SaxWriter html) {
    final AttributesBuilder atts = new AttributesBuilder(STENTRY_ATTS);
    if (node.getSpan() > 1) {
      atts.add(ATTRIBUTE_NAME_COLSPAN, Integer.toString(node.getSpan()));
    }
    html.startElement(node, TOPIC_STENTRY, atts.build());
    if (isInline(node.getFirstChild())) {
      html.startElement(node, TOPIC_P, P_ATTS);
      context.renderChildren(node);
      html.endElement();
    } else {
      context.renderChildren(node);
    }
    html.endElement();

    currentTableColumn += node.getSpan();
  }

  private boolean isInline(Node node) {
    return node instanceof Text || node instanceof Emphasis || node instanceof StrongEmphasis;
  }

  // Code block

  private void render(final CodeBlock node, final NodeRendererContext context, final SaxWriter html) {
    final AttributesBuilder atts = new AttributesBuilder(mditaExtendedProfile ? PRE_ATTS : CODEBLOCK_ATTS)
      .add(XML_NS_URI, "space", "xml:space", "CDATA", "preserve");
    html.startElement(node, mditaExtendedProfile ? TOPIC_PRE : PR_D_CODEBLOCK, atts.build());
    String text = node.getChars().toString();
    if (text.endsWith("\n")) {
      text = text.substring(0, text.length() - 1);
    }
    html.characters(text);
    html.endElement();
  }

  private void render(final IndentedCodeBlock node, final NodeRendererContext context, final SaxWriter html) {
    final AttributesBuilder atts = new AttributesBuilder(mditaExtendedProfile ? PRE_ATTS : CODEBLOCK_ATTS)
      .add(XML_NS_URI, "space", "xml:space", "CDATA", "preserve");
    html.startElement(node, mditaExtendedProfile ? TOPIC_PRE : PR_D_CODEBLOCK, atts.build());
    // FIXME: For compatibility with HTML pre/code, should be removed
    if (mditaExtendedProfile) {
      html.startElement(node, HI_D_TT, TT_ATTS);
    }
    String text = node.getContentChars().toString();
    if (text.endsWith("\n")) {
      text = text.substring(0, text.length() - 1);
    }
    html.characters(text);
    if (mditaExtendedProfile) {
      html.endElement();
    }
    html.endElement();
  }

  private void render(final FencedCodeBlock node, final NodeRendererContext context, final SaxWriter html) {
    final AttributesBuilder atts = new AttributesBuilder(mditaExtendedProfile ? PRE_ATTS : CODEBLOCK_ATTS)
      .add(XML_NS_URI, "space", "xml:space", "CDATA", "preserve");

    BasedSequence info = node.getInfo();
    if (info.startsWith("{") && info.endsWith("}")) {
      final Metadata metadata = Metadata.parse(info.subSequence(1, info.length() - 1).toString());
      if (!metadata.classes.isEmpty()) {
        atts.add("outputclass", String.join(" ", metadata.classes));
      }
      if (metadata.id != null) {
        atts.add(ATTRIBUTE_NAME_ID, metadata.id);
      }
      for (Entry<String, String> entry : metadata.attrs.entrySet()) {
        atts.add(entry.getKey(), entry.getValue());
      }
    } else if (info.isNotNull() && !info.isBlank()) {
      int space = info.indexOf(' ');
      BasedSequence language;
      if (space == -1) {
        language = info;
      } else {
        language = info.subSequence(0, space);
      }
      atts.add("outputclass", /*context.getDitaOptions().languageClassPrefix +*/language.unescape());
    } else {
      String noLanguageClass = context.getDitaOptions().noLanguageClass.trim();
      if (!noLanguageClass.isEmpty()) {
        atts.add("outputclass", noLanguageClass);
      }
    }

    html.startElement(node, mditaExtendedProfile ? TOPIC_PRE : PR_D_CODEBLOCK, atts.build());
    // FIXME: For compatibility with HTML pre/code, should be removed
    if (mditaExtendedProfile) {
      html.startElement(node, HI_D_TT, TT_ATTS);
    }
    String text = node.getContentChars().normalizeEOL();
    if (text.endsWith("\n")) {
      text = text.substring(0, text.length() - 1);
    }
    html.characters(text);
    if (mditaExtendedProfile) {
      html.endElement();
    }
    html.endElement();
  }

  private void render(final Text node, final NodeRendererContext context, final SaxWriter html) {
    if (abbreviations.isEmpty()) {
      if (node.getParent() instanceof Code) {
        html.characters(node.getChars().toString());
      } else {
        html.characters(node.getChars().unescapeNoEntities());
      }
    } else {
      printWithAbbreviations(node.getChars().toString(), html);
    }
  }

  private void render(final ContentNode node, final NodeRendererContext context, final SaxWriter html) {
    context.renderChildren(node);
  }

  private void render(final SoftLineBreak node, final NodeRendererContext context, final SaxWriter html) {
    html.characters('\n');
  }

  private void render(final HardLineBreak node, final NodeRendererContext context, final SaxWriter html) {
    html.processingInstruction("linebreak", null);
  }

  private void render(final Node node, final NodeRendererContext context, final SaxWriter html) {
    throw new RuntimeException(
      "No renderer configured for " + node.getNodeName() + " = " + node.getClass().getCanonicalName()
    );
  }

  // helpers

  private Attributes getAttributesFromAttributesNode(Node node, Attributes base) {
    if (!mditaExtendedProfile && isAttributesParagraph(node.getNext())) {
      final Title header = Title.getFromChildren(node.getNext());
      final AttributesBuilder builder = new AttributesBuilder(base);
      return readAttributes(header, builder).build();
    } else {
      return base;
    }
  }

  @Override
  protected AttributesBuilder getLinkAttributes(final String href) {
    return getLinkAttributes(href, XREF_ATTS);
  }

  protected void printWithAbbreviations(String string, final SaxWriter html) {
    Map<Integer, Entry<String, String>> expansions = null;

    for (Entry<String, String> entry : abbreviations.entrySet()) {
      // first check, whether we have a legal match
      String abbr = entry.getKey();

      int ix = 0;
      while (true) {
        final int sx = string.indexOf(abbr, ix);
        if (sx == -1) break;

        // only allow whole word matches
        ix = sx + abbr.length();

        if (sx > 0 && Character.isLetterOrDigit(string.charAt(sx - 1))) continue;
        if (ix < string.length() && Character.isLetterOrDigit(string.charAt(ix))) {
          continue;
        }

        // ok, legal match so save an expansions "task" for all matches
        if (expansions == null) {
          expansions = new TreeMap<>();
        }
        expansions.put(sx, entry);
      }
    }

    if (expansions != null) {
      int ix = 0;
      for (Entry<Integer, Entry<String, String>> entry : expansions.entrySet()) {
        int sx = entry.getKey();
        final String abbr = entry.getValue().getKey();
        final String expansion = entry.getValue().getValue();

        html.characters(string.substring(ix, sx));
        final AttributesBuilder atts = new AttributesBuilder(PH_ATTS);
        if (expansion != null && !expansion.isEmpty()) {
          atts.add(ATTRIBUTE_NAME_OTHERPROPS, expansion);
        }
        html.startElement(null, TOPIC_PH, atts.build());
        html.characters(abbr);
        html.endElement();
        ix = sx + abbr.length();
      }
      html.characters(string.substring(ix));
    } else {
      html.characters(string);
    }
  }
}
