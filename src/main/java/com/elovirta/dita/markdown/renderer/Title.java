package com.elovirta.dita.markdown.renderer;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ext.attributes.AttributeNode;
import com.vladsch.flexmark.ext.attributes.AttributesNode;
import com.vladsch.flexmark.util.ast.Node;

import java.util.*;

class Title {
//    final String title;
    final Collection<String> classes;
//    final Map<String, String> attributes;

    Title(final Heading node) {
//        final StringBuilder contents = new StringBuilder();
//        getText(node, contents);
//        title = contents.toString();
        final List<AttributesNode> attributesNodes = getAttributesNodes(node);
        classes = getClasses(attributesNodes);
        getId(attributesNodes).ifPresent(node::setAnchorRefId);
    }

    private Optional<String> getId(List<AttributesNode> attributesNodes) {
        for (AttributesNode attributesNode : attributesNodes) {
            for (Node child : attributesNode.getChildren()) {
                if (child instanceof AttributeNode) {
                    final AttributeNode attributeNode = (AttributeNode) child;
                    if (attributeNode.getName().equals("#")) {
                        return Optional.of(attributeNode.getValue().toString());
                    }
                }
            }
        }
        return Optional.empty();
    }

    private List<String> getClasses(List<AttributesNode> attributesNodes) {
        final List<String> res = new ArrayList<>();
        for (AttributesNode attributesNode : attributesNodes) {
            for (Node child : attributesNode.getChildren()) {
                if (child instanceof AttributeNode) {
                    final AttributeNode attributeNode = (AttributeNode) child;
                    if (attributeNode.getName().equals(".")) {
                        res.add(attributeNode.getValue().toString());
                    }
                }
            }
        }
        return res;
    }

    private List<AttributesNode> getAttributesNodes(Node parent) {
        final List<AttributesNode> res = new ArrayList<>();
        for (Node child : parent.getChildren()) {
            if (child instanceof AttributesNode) {
                res.add((AttributesNode) child);
            } else {
                res.addAll(getAttributesNodes(child));
            }
        }
        return res;
    }

//    private void getText(Node parent, StringBuilder buf) {
//        final List<AttributesNode> res = new ArrayList<>();
//        for (Node child : parent.getChildren()) {
//            if (child instanceof Text) {
//                buf.append(child.getChars());
//            } else {
//                getText(child, buf);
//            }
//        }
//    }
}
