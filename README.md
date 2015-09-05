Markdown DITA for DITA-OT
=========================

DITA-OT Markdown plug-in contains

-   a custom parser for Markdown to allow using
    Markdown DITA as a source document format,
-   and a transtype to generate Markdown from DITA source.

The Markdown DITA files need to use a subset of Markdown constructs for
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
    dita -install https://github.com/jelovirt/dita-ot-markdown/releases/download/1.1.0/com.elovirta.dita.markdown_1.1.0.zip
    ~~~~

The `dita` command line tool requires no additional configuration;
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

Support this project and others by [@jelovirt](https://github.com/jelovirt) via [Paypal](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=jarno%40elovirta%2ecom&lc=FI&item_name=Support%20Open%20Source%20work&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted).

License
-------

DITA-OT Markdown is licensed for use under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
