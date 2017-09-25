package com.elovirta.dita.markdown;

import com.vladsch.flexmark.util.Utils;
import com.vladsch.flexmark.util.options.DataHolder;

public class DitaRendererOptions {
    public final String softBreak;
    public final boolean isSoftBreakAllSpaces;
    public final String hardBreak;
    public final String strongEmphasisStyleHtmlOpen;
    public final String strongEmphasisStyleHtmlClose;
    public final String emphasisStyleHtmlOpen;
    public final String emphasisStyleHtmlClose;
    public final String codeStyleHtmlOpen;
    public final String codeStyleHtmlClose;
    public final boolean escapeHtmlBlocks;
    public final boolean escapeHtmlCommentBlocks;
    public final boolean escapeInlineHtml;
    public final boolean escapeInlineHtmlComments;
    public final boolean percentEncodeUrls;
    public final int indentSize;
    public final boolean suppressHtmlBlocks;
    public final boolean suppressHtmlCommentBlocks;
    public final boolean suppressInlineHtml;
    public final boolean suppressInlineHtmlComments;
    public final boolean doNotRenderLinksInDocument;
    public final boolean renderHeaderId;
    public final boolean generateHeaderIds;
    public final String languageClassPrefix;
    public final String noLanguageClass;
    public final String sourcePositionAttribute;
    public final String inlineCodeSpliceClass;
    public final boolean sourcePositionParagraphLines;
    public final boolean sourceWrapHtmlBlocks;
    public final int formatFlags;
    public final int maxTrailingBlankLines;
    public final boolean htmlBlockOpenTagEol;
    public final boolean htmlBlockCloseTagEol;
    public final boolean unescapeHtmlEntities;

    public DitaRendererOptions(DataHolder options) {
        softBreak = DitaRenderer.SOFT_BREAK.getFrom(options);
        isSoftBreakAllSpaces = Utils.isWhiteSpaceNoEOL(softBreak);
        hardBreak = DitaRenderer.HARD_BREAK.getFrom(options);
        strongEmphasisStyleHtmlOpen = DitaRenderer.STRONG_EMPHASIS_STYLE_HTML_OPEN.getFrom(options);
        strongEmphasisStyleHtmlClose = DitaRenderer.STRONG_EMPHASIS_STYLE_HTML_CLOSE.getFrom(options);
        emphasisStyleHtmlOpen = DitaRenderer.EMPHASIS_STYLE_HTML_OPEN.getFrom(options);
        emphasisStyleHtmlClose = DitaRenderer.EMPHASIS_STYLE_HTML_CLOSE.getFrom(options);
        codeStyleHtmlOpen = DitaRenderer.CODE_STYLE_HTML_OPEN.getFrom(options);
        codeStyleHtmlClose = DitaRenderer.CODE_STYLE_HTML_CLOSE.getFrom(options);
        escapeHtmlBlocks = DitaRenderer.ESCAPE_HTML_BLOCKS.getFrom(options);
        escapeHtmlCommentBlocks = DitaRenderer.ESCAPE_HTML_COMMENT_BLOCKS.getFrom(options);
        escapeInlineHtml = DitaRenderer.ESCAPE_INLINE_HTML.getFrom(options);
        escapeInlineHtmlComments = DitaRenderer.ESCAPE_INLINE_HTML_COMMENTS.getFrom(options);
        percentEncodeUrls = DitaRenderer.PERCENT_ENCODE_URLS.getFrom(options);
        indentSize = DitaRenderer.INDENT_SIZE.getFrom(options);
        suppressHtmlBlocks = DitaRenderer.SUPPRESS_HTML_BLOCKS.getFrom(options);
        suppressHtmlCommentBlocks = DitaRenderer.SUPPRESS_HTML_COMMENT_BLOCKS.getFrom(options);
        suppressInlineHtml = DitaRenderer.SUPPRESS_INLINE_HTML.getFrom(options);
        suppressInlineHtmlComments = DitaRenderer.SUPPRESS_INLINE_HTML_COMMENTS.getFrom(options);
        doNotRenderLinksInDocument = DitaRenderer.DO_NOT_RENDER_LINKS.getFrom(options);
        renderHeaderId = DitaRenderer.RENDER_HEADER_ID.getFrom(options);
        generateHeaderIds = DitaRenderer.GENERATE_HEADER_ID.getFrom(options);
        languageClassPrefix = DitaRenderer.FENCED_CODE_LANGUAGE_CLASS_PREFIX.getFrom(options);
        noLanguageClass = DitaRenderer.FENCED_CODE_NO_LANGUAGE_CLASS.getFrom(options);
        sourcePositionAttribute = DitaRenderer.SOURCE_POSITION_ATTRIBUTE.getFrom(options);
        sourcePositionParagraphLines = !sourcePositionAttribute.isEmpty() && DitaRenderer.SOURCE_POSITION_PARAGRAPH_LINES.getFrom(options);
        sourceWrapHtmlBlocks = !sourcePositionAttribute.isEmpty() && DitaRenderer.SOURCE_WRAP_HTML_BLOCKS.getFrom(options);
        formatFlags = DitaRenderer.FORMAT_FLAGS.getFrom(options);
        maxTrailingBlankLines = DitaRenderer.MAX_TRAILING_BLANK_LINES.getFrom(options);
        htmlBlockOpenTagEol = DitaRenderer.HTML_BLOCK_OPEN_TAG_EOL.getFrom(options);
        htmlBlockCloseTagEol = DitaRenderer.HTML_BLOCK_CLOSE_TAG_EOL.getFrom(options);
        unescapeHtmlEntities = DitaRenderer.UNESCAPE_HTML_ENTITIES.getFrom(options);
        inlineCodeSpliceClass = DitaRenderer.INLINE_CODE_SPLICE_CLASS.getFrom(options);
    }
}
