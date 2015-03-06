Markdown DITA for DITA-OT 
=========================

DITA-OT Markdown plug-in contains

-   a custom parser for Markdown to allow using
    Markdown as a source document format,
-   and a transtype to generate Markdown from DITA source.

The DITA Markdown files need to use a subset of Markdown constructs for
compatibility with DITA content models.

Requirements
------------

DITA-OT 2.1 is required. Earlier versions of DITA-OT do not have the
required extension points.

Build
-----

1.  Run Gradle distribution task

    ~~~~ {.sh}
    ./gradlew dist
    ~~~~

Distribution ZIP file is generated under `build/distributions`.

Install
-------

1.  Run plug-in installation command

    ~~~~ {.sh}
    dita -install https://github.com/jelovirt/dita-ot-markdown/releases/download/0.5.2/com.elovirta.dita.markdown_0.5.2.zip
    ~~~~

The `dita` command line tool requires not additional configuration;
running DITA-OT using Ant requires adding plug-in contributed JAR files
to `CLASSPATH` with e.g. `-lib plugins/com.elovirta.dita.markdown`.

Usage
-----

Markdown DITA topics can only be used by linking to them in map files.

~~~~ {.xml}
<map>
  <topicref href="test.md" format="markdown"/>
</map>
~~~~

The `format` attribute value must be set to `markdown` in order to
recognize files as Markdown DITA; file extension is not used to
recognize format.

To publish Markdown DITA files, use `markdown` transtype.

See [Syntax reference](https://github.com/jelovirt/dita-ot-markdown/wiki/Syntax-reference) for XML and Markdown DITA correspondence.

Donating
--------

Support this project and others by [@jelovirt](https://github.com/jelovirt) via Gratipay.

[![Support via Gratipay](https://cdn.rawgit.com/gratipay/gratipay-badge/2.3.0/dist/gratipay.png)](https://gratipay.com/jelovirt/)

License
-------

The DITA Open Toolkit is licensed for use under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
