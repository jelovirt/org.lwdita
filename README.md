# Markdown parser for DITA-OT

DITA-OT plug-in that adds a custom parser for Markdown to allow using Markdown as a source document format. The DITA Markdown files need to use a subset of Markdown constructs for compatibility with DITA content models.

## Requirements

DITA-OT 2.1 is required. Earlier versions of DITA-OT do not have the required extension points.

## Install

1.  Run plug-in installation command
    
```
dita -install https://github.com/jelovirt/dita-ot-markdown/releases/download/0.2.3/com.elovirta.dita.markdown_0.2.3.zip
```    

The `dita` command line tool requires not additional configuration; running DITA-OT using Ant requires adding plug-in contributed JAR files to `CLASSPATH` with e.g. `-lib plugins/com.elovirta.dita.markdown`.

## Usage

Markdown DITA topics can only be used by linking to them in map files.

```xml
<map>
  <topicref href="test.md" format="markdown"/>
</map>
```

The `format` attribute value must be set to `markdown` in order to recognize files as Markdown DITA; file extension is not used to recognize format.

## Syntax reference

Each header level will generate a topic title:

```markdown
# Topic title

## Nested topic title
```

```xml
<topic id="topic_title">
  <title>Topic title</title>
  <topic id="nested_topic_title">
    <title>Nested topic title</title>
  </topic>
</topic>
```
Pandoc [header_attributes](http://johnmacfarlane.net/pandoc/demo/example9/pandocs-markdown.html#extension-header_attributes) can be used to define `id` or `outputclass` attributes:
```markdown
# Topic title {#carrot .juice}
```

```xml
<topic id="carrot" outputclass="juice">
```
The outputclass values `section` and `example` have a special meaning. They are used to generate `section` and `example` element, respectively:
```markdown
# Topic title

## Section title {.section}

## Example title {.example}
```

```xml
<topic id="topic_title">
  <title>Topic title</title>
  <body>
    <section>
      <title>Section title</title>
    </section>
    <example>
      <title>Example title</title>
    </example>
  </body>
</topic>
```
Pandoc [pandoc_title_block](http://johnmacfarlane.net/pandoc/demo/example9/pandocs-markdown.html#extension-pandoc_title_block) extension can be used to group multiple level 1 headers under a common title:

```markdown
% Common title

# Topic title

# Second title
```

```xml
<topic id="common_title">
  <title>Common title</title>
  <topic id="topic_title">
    <title>Topic title</title>
  </topic>
  <topic id="second_title">
    <title>Second title</title>
  </topic>
</topic>
```

For a more extensive example, see [announcement blog entry](http://jelovirt.github.io/2015/02/06/dita-markdown.html).
