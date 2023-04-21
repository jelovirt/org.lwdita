package com.elovirta.dita.markdown.renderer;

import com.vladsch.flexmark.ext.anchorlink.AnchorLink;
import com.vladsch.flexmark.ext.attributes.AttributeNode;
import com.vladsch.flexmark.ext.attributes.AttributesNode;
import com.vladsch.flexmark.util.ast.Node;
import java.util.*;

class Title {

  final Collection<String> classes;
  final Map<String, String> attributes;
  final Optional<String> id;

  private Title(List<AttributesNode> attributesNodes) {
    classes = getClasses(attributesNodes);
    attributes = getAttributes(attributesNodes);
    id = getId(attributesNodes);
  }

  public static Title getFromNext(final Node node) {
    return new Title(getNextAttributesNodes(node));
  }

  public static Title getFromChildren(final Node node) {
    //        return new Title(getAttributesNodes(node));
    return new Title(getPreviousAttributesNodes(node));
  }

  private static List<AttributesNode> getNextAttributesNodes(Node current) {
    final List<AttributesNode> res = new ArrayList<>();
    Node node = current.getNext();
    while (node instanceof AttributesNode) {
      res.add((AttributesNode) node);
      node = node.getNext();
    }
    return res;
  }

  private static List<AttributesNode> getPreviousAttributesNodes(Node current) {
    final List<AttributesNode> res = new ArrayList<>();
    Node node = current.getLastChild();
    while (node != null) {
      if (node instanceof AttributesNode) {
        res.add((AttributesNode) node);
        //            } else if (node instanceof AnchorLink) {
        //                // Ignore
      } else {
        break;
      }
      node = node.getPrevious();
    }
    Collections.reverse(res);
    return res;
  }

  private static Map<String, String> getAttributes(List<AttributesNode> attributesNodes) {
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

  private static Optional<String> getId(List<AttributesNode> attributesNodes) {
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

  private static boolean isId(AttributeNode attributeNode) {
    return attributeNode.getName().toString().equals("#") || attributeNode.getName().toString().equals("id");
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

  private static boolean isClass(AttributeNode attributeNode) {
    return attributeNode.getName().toString().equals(".") || attributeNode.getName().toString().equals("class");
  }

  private static List<AttributesNode> getAttributesNodes(Node parent) {
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
