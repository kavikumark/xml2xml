package com.le.xslt.gen;
import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
   
public class BuildDom 
{
    public Document doc;
    public static int No_Tags = 0;

    public static void main(String[] argv) {
        BuildDom myBuildDom = new BuildDom(argv[0]);
        myBuildDom.display();
    }

    public BuildDom(String filename) {
        doc = parseXmlFile(filename, false);
        countElementNodes(doc.getDocumentElement());
    }
    
    // Parses an XML file and returns a DOM document.
    private Document parseXmlFile(String filename, boolean validating) {
        try {
            // Create a builder factory
            DocumentBuilderFactory factory = 
                DocumentBuilderFactory.newInstance();
            factory.setValidating(validating);
    
            // Create the builder and parse the file
            Document doc = 
                factory.newDocumentBuilder().parse(new File(filename));
            return doc;
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        
        return null;
    }

    private void countElementNodes(Node node) {
        int i, childCount;
        NodeList children;

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            No_Tags++;

            if (node.hasChildNodes()) {
                children = node.getChildNodes();
                childCount = children.getLength();
                for (i = 0; i < childCount; i++)
                    countElementNodes(children.item(i));
            }
        }
    }   

    private void display() {
        displayRecursive(doc.getDocumentElement(), 0);
    }

    private void displayRecursive(Node node, int depth) {
        int i, childCount, attrCount;
        NodeList children;
        NamedNodeMap attributes;

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            spacer(depth);
            System.out.println("Element: " + node.getNodeName());
    
            if (node.hasAttributes()) {
                attributes = node.getAttributes();
                attrCount = attributes.getLength();
                for (i = 0; i < attrCount; i++)
                    displayRecursive(attributes.item(i), depth + 1);
            }

            if (node.hasChildNodes()) {
                children = node.getChildNodes();
                childCount = children.getLength();
                for (i = 0; i < childCount; i++)
                    displayRecursive(children.item(i), depth + 1);
            }
        }
        else if (node.getNodeType() == Node.TEXT_NODE) {
            spacer(depth);  
            String s = node.getNodeValue();
            System.out.println("Text: " + s);
        }
        else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            spacer(depth);
            System.out.println("Attr: " + node.getNodeName() + " - Value: " +
                node.getNodeValue());
        }
        else {
            spacer(depth);
            System.out.println(node.getNodeName() + ": " + node.getNodeValue());
        }
    }

    private void spacer(int spaces) {
        int i;

        for (i = 0; i < spaces; i++) {
            System.out.print("    ");
        }
    }

    public static int get_NoTags() {
        return No_Tags;
    }
}
