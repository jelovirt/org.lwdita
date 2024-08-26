package com.elovirta.dita.markdown;

import com.elovirta.dita.markdown.renderer.HeaderIdGenerator;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ext.anchorlink.AnchorLink;
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterBlock;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataSet;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Base class for parsing Markdown to DITA.
 */
public class MarkdownParserImpl implements MarkdownParser {

  private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("com.elovirta.dita.messages");

  private final DataSet options;
  private ContentHandler contentHandler;
  private ErrorHandler errorHandler;

  public MarkdownParserImpl(DataSet options) {
    this.options = options;
  }

  @Override
  public void convert(BasedSequence sequence, URI input) throws SAXException {
    final Parser parser = Parser.builder(options).build();
    final Document root = parser.parse(sequence);
    try {
      final Document cleaned = preprocess(root, input);
      validate(cleaned);
      render(cleaned);
    } catch (ParseException e) {
      final Throwable cause = e.getCause();
      if (cause != null && cause instanceof SAXException) {
        throw (SAXException) cause;
      } else {
        throw new SAXException(e);
      }
    }
  }

  @Override
  public void setContentHandler(final ContentHandler handler) {
    this.contentHandler = handler;
  }

  @Override
  public void setErrorHandler(ErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
  }

  /**
   * Render AST to DITA events.
   *
   * @param root document AST
   */
  protected void render(final Document root) {
    ContentHandler res = contentHandler;
    final XMLFilterImpl cleanerFilter = new CleanerFilter();
    cleanerFilter.setContentHandler(res);
    res = cleanerFilter;
    if (DitaRenderer.SPECIALIZATION.get(options)) {
      final XMLFilterImpl specialize = new SpecializeFilter();
      specialize.setContentHandler(res);
      res = specialize;
    } else if (DitaRenderer.SPECIALIZATION_CONCEPT.get(options)) {
      final XMLFilterImpl specialize = new SpecializeFilter(SpecializeFilter.Type.CONCEPT);
      specialize.setContentHandler(res);
      res = specialize;
    } else if (DitaRenderer.SPECIALIZATION_TASK.get(options)) {
      final XMLFilterImpl specialize = new SpecializeFilter(SpecializeFilter.Type.TASK);
      specialize.setContentHandler(res);
      res = specialize;
    } else if (DitaRenderer.SPECIALIZATION_REFERENCE.get(options)) {
      final XMLFilterImpl specialize = new SpecializeFilter(SpecializeFilter.Type.REFERENCE);
      specialize.setContentHandler(res);
      res = specialize;
    }
    final DitaRenderer s = new DitaRenderer(options);
    s.render(root, res);
  }

  /**
   * Validate AST.
   *
   * @param root document AST
   * @throws ParseException if document is not valid
   */
  protected void validate(Document root) {
    final boolean mditaCoreProfile = DitaRenderer.MDITA_CORE_PROFILE.get(options);
    final boolean mditaExtendedProfile =
      DitaRenderer.MDITA_EXTENDED_PROFILE.get(options) || DitaRenderer.LW_DITA.get(options);

    int level = 0;
    Node node = root.getFirstChild();
    while (node != null) {
      if (node instanceof Heading) {
        Heading heading = (Heading) node;
        if ((mditaCoreProfile || mditaExtendedProfile) && heading.getLevel() > 2) {
          throw new ParseException(
            String.format(MESSAGES.getString("error.lwdita_invalid_level"), heading.getLevel(), heading.getText())
          );
        }
        if (heading.getLevel() > level + 1) {
          throw new ParseException(String.format(MESSAGES.getString("error.heading_skip"), level, heading.getLevel()));
        }
        level = heading.getLevel();
      }
      node = node.getNext();
    }
  }

  /**
   * Preprocess AST before validation and rendition.
   * <p>
   * If document doesn't start with H1, generate H1 from YAML metadata or file name.
   */
  protected Document preprocess(Document root, URI input) throws SAXException {
    if (isWiki(root) && !DitaRenderer.MAP.get(options)) {
      final Map<String, String> header = parseYamlHeader(root);
      if (DitaRenderer.WIKI.get(options)) {
        generateRootHeading(root, header.get("id"), header.getOrDefault("title", getTextFromFile(input)));
      } else if (DitaRenderer.FIX_ROOT_HEADING.get(options)) {
        if (errorHandler != null) {
          errorHandler.warning(
            new SAXParseException(MESSAGES.getString("error.missing_title"), null, input.toString(), 1, 1)
          );
        }
        generateRootHeading(root, header.get("id"), header.getOrDefault("title", getTextFromFile(input)));
      } else if (MarkdownReader.PROCESSING_MODE.get(options)) {
        if (errorHandler != null) {
          errorHandler.error(
            new SAXParseException(MESSAGES.getString("error.missing_title"), null, input.toString(), 1, 1)
          );
        }
      } else {
        if (errorHandler != null) {
          errorHandler.warning(
            new SAXParseException(MESSAGES.getString("error.missing_title"), null, input.toString(), 1, 1)
          );
        }
        String id = header.get("id");
        if (id == null) {
          id =
            HeaderIdGenerator.generateId(
              getTextFromFile(input),
              DitaRenderer.HEADER_ID_GENERATOR_TO_DASH_CHARS.get(root),
              DitaRenderer.HEADER_ID_GENERATOR_NO_DUPED_DASHES.get(root)
            );
        }
        generateRootHeading(root, id, header.get("title"));
      }
    }
    return root;
  }

  private static String getId(final String contents) {
    return contents.toLowerCase().replaceAll("[^\\w\\s]", "").trim().replaceAll("\\s+", "-");
  }

  private Map<String, String> parseYamlHeader(Document root) {
    Map<String, String> res = new HashMap<>();
    final YamlFrontMatterBlock yaml = root.getFirstChild() instanceof YamlFrontMatterBlock
      ? (YamlFrontMatterBlock) root.getFirstChild()
      : null;
    if (yaml != null) {
      final AbstractYamlFrontMatterVisitor v = new AbstractYamlFrontMatterVisitor();
      v.visit(root);
      final Map<String, List<String>> metadata = v.getData();
      final List<String> ids = metadata.get("id");
      if (ids != null && !ids.isEmpty()) {
        res.put("id", ids.get(0));
      }
      final List<String> titles = metadata.get("title");
      if (titles != null && !titles.isEmpty()) {
        String title = titles.get(0);
        if (
          (title.charAt(0) == '\'' && title.charAt(title.length() - 1) == '\'') ||
          (title.charAt(0) == '"' && title.charAt(title.length() - 1) == '"')
        ) {
          title = title.substring(1, title.length() - 1);
        }
        res.put("title", title);
      }
    }
    return res;
  }

  private void generateRootHeading(Document root, String id, String title) {
    final Heading heading = new Heading();
    if (id != null) {
      heading.setAnchorRefId(id);
    }
    heading.setLevel(1);
    if (id == null) {
      final AnchorLink anchorLink = new AnchorLink();
      anchorLink.appendChild(new Text(title != null ? title : ""));
      heading.appendChild(anchorLink);
    } else {
      heading.appendChild(new Text(title != null ? title : ""));
    }
    root.prependChild(heading);
  }

  /**
   * Check if document doesn't start with H1, excluding optional YAML header.
   */
  private static boolean isWiki(Document root) {
    Node firstChild = root.getFirstChild();
    if (firstChild == null) {
      return false;
    }
    if (firstChild instanceof YamlFrontMatterBlock) {
      firstChild = firstChild.getNext();
    }
    return !(firstChild instanceof Heading && ((Heading) firstChild).getLevel() == 1);
  }

  /**
   * Filename to title.
   */
  private String getTextFromFile(URI file) {
    final String path = file.getPath();
    final String name = path.substring(path.lastIndexOf("/") + 1);
    final String title = name.lastIndexOf(".") != -1 ? name.substring(0, name.lastIndexOf(".")) : name;
    return title.replace('_', ' ').replace('-', ' ');
  }
}
