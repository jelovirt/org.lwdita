package com.elovirta.dita.markdown.renderer;

import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;

public interface DitaIdGenerator {
    DitaIdGenerator NULL = new DitaIdGenerator() {
        @Override
        public void generateIds(Document document) {

        }

        @Override
        public String getId(Node node) {
            return null;
        }
    };

    void generateIds(Document document);

    String getId(Node node);
}
