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
