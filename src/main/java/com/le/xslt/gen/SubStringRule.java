package com.le.xslt.gen;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SubStringRule
{
    XSLTGen xg;
    SubStringElement[] substring;
    int size;

    public SubStringRule(XSLTGen xg, SubStringElement[] substring, int size) {
        this.xg        = xg;
        this.substring = substring;
        this.size      = size;
        
        checkRealMatch();
        generateRule();
    }
    
    private void checkRealMatch() {
        int i, j, k;
        Node html_root = xg.html_root,
             xml_root = xg.xml_root;
        
        for (i = 0; i < size; i++) {
            if (substring[i].no_xml == 1)
            substring[i].set_family(0);
            else {
                Node[][] structNode = xg.structmap.get_ntable();
                int structSize = xg.structmap.curr_nrow;
                    
                for (k = 0; k < structSize; k++) {
                    if ((k > 0) && (structNode[k][0] == xml_root))
                        ;
                    else if (isInSubtree(substring[i].html, structNode[k][1], 
                         html_root)) {
                        for (j = 0; j < substring[i].no_xml; j++) {
                            Node xml;
                        
                            if (substring[i].xmls[j][0].getNodeType() ==
                                Node.ATTRIBUTE_NODE)
                                xml = substring[i].xmls[j][1];
                            else
                                xml = substring[i].xmls[j][0];
                        
                            if (isInSubtree(xml, structNode[k][0], xml_root))
                                substring[i].set_family(j);
                        }
                                
                        break;
                    }
                }
            }
        }
    }
    
    private boolean isInSubtree(Node child, Node tree, Node root) {
        while ((child != tree) && (child != root))
            child = child.getParentNode();
        
        if (child == tree)
            return true;
        else
            return false;
    }
            
    private void generateRule() {
        int i, j, noTags;
        String html, rule, extra;
        Object[][] tags;
	Node[]     tagNode;
    
        for (i = 0; i < size; i++) {
            html  = getHtmlString(substring[i].html);
            rule  = "";
            extra = "";
            
            /* tags (type, xml_node, parent_node) */
            tags    = new Object[html.length()][4];
	    tagNode = new Node[html.length()];
            noTags  = 0;    
/*            
            System.out.println(html);
            System.out.println();
*/           
            int index, lenMatch;
            
            while (html.compareTo("") != 0) {
                index = substring[i].findStartsWith(html);
                
                if (index == -1) {
                    extra = extra + html.charAt(0);
                    rule  = rule + html.charAt(0);
                    html  = html.substring(1);
                }
                else {
                    if (extra.compareTo("") != 0) {
                        tags[noTags][0] = new String("EXTRA");
                        tags[noTags][1] = new String(extra);
                        tags[noTags][2] = new String("");
                        tags[noTags][3] = new Integer(0);
			tagNode[noTags] = null;
                        noTags++;
                        
                        extra = "";
                    }
                    
                    Node xml = substring[i].xmls[index][0];
                    int position;
                    
                    if (xml.getNodeType() == Node.ELEMENT_NODE) {
                        tags[noTags][0] = new String("ELEMENT");
                        tags[noTags][1] = new String(xml.getNodeName());
                        tags[noTags][2] = new String("");
                        
                        position = getPosition(xml);
                        tags[noTags][3] = new Integer(position);

			tagNode[noTags] = xml;
                        
                        noTags++;
                        
                        rule = rule + "<xsl:value-of select=\"" + 
                            xml.getNodeName();
                        if (position != 0)
                            rule = rule + "[" + position + "]";
                        rule = rule + "\"/>";
                        
                        lenMatch = xg.textmatch.trimString(
                            xml.getFirstChild().getNodeValue()).length();
                    }
                    else if (xml.getNodeType() == Node.TEXT_NODE) {
                        tags[noTags][0] = new String("TEXT");
                        tags[noTags][1] = new String(xml.getNodeValue());
                        tags[noTags][2] = new String("");
                        tags[noTags][3] = new Integer(getPosition(xml));	
			tagNode[noTags] = xml;
                        noTags++;
                                                        
                        lenMatch = xg.textmatch.trimString(
                            xml.getNodeValue()).length();
                    }
                    else {
                        tags[noTags][0] = new String("ATTRIBUTE");
                        tags[noTags][1] = new String(xml.getNodeName());
                        tags[noTags][2] = new String(substring[i].
                            xmls[index][1].getNodeName());
                        tags[noTags][3] = new Integer(getPosition(
                            substring[i].xmls[index][1]));
			tagNode[noTags] = substring[i].xmls[index][1];
                        noTags++;
                                                
                        rule = rule + "<xsl:value-of select=\"@" +
                            xml.getNodeName() + "\"/>";
                        
                        lenMatch = xg.textmatch.trimString(
                            xml.getNodeValue()).length();
                    }       
                    
                    html = html.substring(lenMatch);
                }
            }
            
            extra = extra.trim();
            if (extra.compareTo("") != 0) {
                tags[noTags][0] = new String("EXTRA");
                tags[noTags][1] = new String(extra);
                tags[noTags][2] = new String("");
                tags[noTags][3] = new Integer(0);
		tagNode[noTags] = null;
                noTags++;
            }               
            
            rule = xg.textmatch.trimString(rule);
            substring[i].add_rule(rule);           
            substring[i].add_tags(tags, tagNode, noTags);
            
/*            
            System.out.println(rule);
            
            for (int k = 0; k < noTags; k++)
                System.out.println(tags[k][0] + " : " + tags[k][1] + " * " +
                    tags[k][2] + " " + tags[k][3]);     

            System.out.println();  
            System.out.println();           
*/
        }
    }
    
    private int getPosition(Node xml) {
        Node parent;
        NodeList children;
        int count, position, childCount;

	count = countSameSiblings(xml);

        if (count > 1) {             	   
            position   = 1;

            String tag = xml.getNodeName();

            parent     = xml.getParentNode();
            children   = parent.getChildNodes();
            childCount = children.getLength();            
            for (int i = 0; i < childCount; i++) {
                if (children.item(i) == xml)
                    return position;
                else
                    if (children.item(i).getNodeName().compareTo(tag) == 0)
                        position++;
            }
        }
        
        return 0;
    }   
    
    public int countSameSiblings(Node xml) {
    	String tag = xml.getNodeName();
    	
    	Node parent;
    	NodeList children;
    	int i, count = 0, childCount;
    	
        parent     = xml.getParentNode();
        children   = parent.getChildNodes();
        childCount = children.getLength();
        for (i = 0; i < childCount; i++)
            if (children.item(i).getNodeName().compareTo(tag) == 0)
                count++;
        
        return count;
    }    	
    
    private String getHtmlString(Node html) {
        while (html.getNodeType() != Node.TEXT_NODE)
            html = html.getFirstChild();

        return xg.textmatch.trimString(html.getNodeValue());
    }
}
