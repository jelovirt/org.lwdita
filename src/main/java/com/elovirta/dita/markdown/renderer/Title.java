package com.elovirta.dita.markdown.renderer;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ext.anchorlink.AnchorLink;
import com.vladsch.flexmark.ext.attributes.AttributeNode;
import com.vladsch.flexmark.ext.attributes.AttributesNode;
import com.vladsch.flexmark.util.ast.Node;

import java.util.*;

class Title {
//    final String title;
    final Collection<String> classes;
    final Map<String, String> attributes;
    final Optional<String> id;

    Title(final Node node) {
//        final StringBuilder contents = new StringBuilder();
//        getText(node, contents);
//        title = contents.toString();
        final List<AttributesNode> attributesNodes = getAttributesNodes(node);
        classes = getClasses(attributesNodes);
        attributes = getAttributes(attributesNodes);
        id = getId(attributesNodes);
//        if (node instanceof Heading) {
//            final Heading heading = (Heading) node;
//            getId(attributesNodes).ifPresent(heading::setAnchorRefId);
//        }
    }

    private Map<String, String> getAttributes(List<AttributesNode> attributesNodes) {
        final Map<String, String> res = new HashMap<>();
        for (AttributesNode attributesNode : attributesNodes) {
            for (Node child : attributesNode.getChildren()) {
                if (child instanceof AttributeNode) {
                    final AttributeNode attributeNode = (AttributeNode) child;
                    if (!isClass(attributeNode) && !isId(attributeNode)) {
                        res.put(attributeNode.getName().toString(), attributeNode.getValue().toString());
                    }
                }
            }
        }
        return res;
    }

    private Optional<String> getId(List<AttributesNode> attributesNodes) {
        for (AttributesNode attributesNode : attributesNodes) {
            for (Node child : attributesNode.getChildren()) {
                if (child instanceof AttributeNode) {
                    final AttributeNode attributeNode = (AttributeNode) child;
                    if (isId(attributeNode)) {
                        return Optional.of(attributeNode.getValue().toString());
                    }
                }
            }
        }
        return Optional.empty();
    }

    private boolean isId(AttributeNode attributeNode) {
        return attributeNode.getName().equals("#") || attributeNode.getName().equals("id");
    }

    private List<String> getClasses(List<AttributesNode> attributesNodes) {
        final List<String> res = new ArrayList<>();
        for (AttributesNode attributesNode : attributesNodes) {
            for (Node child : attributesNode.getChildren()) {
                if (child instanceof AttributeNode) {
                    final AttributeNode attributeNode = (AttributeNode) child;
                    if (isClass(attributeNode)) {
                        res.add(attributeNode.getValue().toString());
                    }
                }
            }
        }
        return res;
    }

    private boolean isClass(AttributeNode attributeNode) {
        return attributeNode.getName().equals(".") || attributeNode.getName().equals("class");
    }

    private List<AttributesNode> getAttributesNodes(Node parent) {
        final List<AttributesNode> res = new ArrayList<>();
        for (Node child : parent.getChildren()) {
            if (child instanceof AttributesNode) {
                res.add((AttributesNode) child);
            } else if (child instanceof AnchorLink) {
                res.addAll(getAttributesNodes(child));
            } else {
//                res.addAll(getAttributesNodes(child));
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
