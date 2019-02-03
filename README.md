Lightweight DITA for DITA-OT
============================

The DITA-OT LwDITA plug-in supersedes the previously released [Markdown plug-in for DITA-OT](https://github.com/jelovirt/dita-ot-markdown) and adds additional features to support Lightweight DITA. 

**Note:** The LwDITA plug-in is included in DITA Open Toolkit 3.0 and newer.

It contains:

-   a custom parser for Markdown and HTML to allow using
    MDITA and HDITA as a source document format,
-   and a transtype to generate Markdown from DITA source.

The Markdown DITA files need to use a subset of Markdown constructs for
compatibility with DITA content models.


Usage
-----

### Using LwDITA files as input

Markdown DITA or HTML DITA topics can only be used by linking to them in map files.

~~~~ {.xml}
<map>
  <topicref href="test.md" format="mdita"/>
  <topicref href="test.html" format="hdita"/>
</map>
~~~~

The `format` attribute value must be set to `mdita` or `hdita` in order to
recognize files as Markdown DITA or HTML DITA, respectively; the file extension is not used to
recognize format.

See the [syntax reference](https://github.com/jelovirt/org.lwdita/wiki/Syntax-reference) for XML and Markdown DITA correspondence.

### Generating Markdown output

The DITA-OT LwDITA plug-in extends the DITA Open Toolkit with additional output formats _(transformation types)_ that can be used to publish DITA content as Markdown.

* To publish Markdown DITA files, use the `markdown` transtype.

* To generate [GitHub Flavored Markdown](https://help.github.com/categories/writing-on-github/) files, use the `markdown_github` transtype.

* To publish GitHub Flavored Markdown and generate a  `SUMMARY.md` table of contents file for publication via [GitBook](https://www.gitbook.com), use the `markdown_gitbook` transtype.

Requirements
------------

DITA-OT 2.2 is required. Earlier versions of DITA-OT do not have the
required extension points.

Install
-------

1.  Run the plug-in installation command:

    ~~~~ {.sh}
    $ dita --install org.lwdita
    ~~~~

    On DITA-OT version 3.0 and older:

    ~~~~ {.sh}
    $ dita --install https://github.com/jelovirt/org.lwdita/releases/download/2.2.0/org.lwdita-2.2.0.zip
    ~~~~

The `dita` command line tool requires no additional configuration;
running DITA-OT using Ant requires adding plug-in contributed JAR files
to the `CLASSPATH` with e.g. `-lib plugins/org.lwdita`.

Build
-----

To build the DITA-OT Markdown plug-in from source:

 
1.  Run the Gradle distribution task to generate the plug-in distribution package:

    ~~~~ {.sh}
    ./gradlew dist
    ~~~~

    The distribution ZIP file is generated under `build/distributions`.

Donating
--------

Support this project and others by [@jelovirt](https://github.com/jelovirt) via [Paypal](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=jarno%40elovirta%2ecom&lc=FI&item_name=Support%20Open%20Source%20work&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted).

License
-------

DITA-OT LwDITA is licensed for use under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
