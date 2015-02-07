# Markdown parser for DITA-OT

DITA-OT plug-in that adds a custom parser for Markdown to allow using Markdown as a source document format. The DITA Markdown files need to use a subset of Markdown constructs for compatibility with DITA content models.

## Requirements

DITA-OT 2.1 is required. Earlier versions of DITA-OT do not have the required extension points.

## Install

Follow normal DITA-OT plug-in installation procedure. The `dita` command line tool requires not additional configuration; running DITA-OT using Ant requires adding plug-in contributed JAR files to `CLASSPATH` with e.g. `-lib plugins/com.elovirta.dita.markdown`.
