package com.le.xslt.gen;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ConstructXSLT 
{
    private XSLTGen xg;
    private Node xml_root, html_root;
    
    private int asize;
    String[][] structChildren;
    int countStruct;
    StringBuffer output = new StringBuffer("");
    
    public ConstructXSLT(XSLTGen xg, Node xml_root, Node html_root) {
        this.xg = xg;
    
        this.xml_root  = xml_root;
        this.html_root = html_root;
        
        asize          = xg.xmldom.get_NoTags();
        structChildren = new String[asize][3];
        output = new StringBuffer("");
        generateXSLT();
    }

    private void generateXSLT() {
        int i;
    
        /* Print out XSLT header */
        
        output.append("<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999"
            + "/XSL/Transform\" version=\"2.0\">"+"\n");

	if (html_root.getNodeName().compareToIgnoreCase("html") != 0)
	    output.append("<xsl:output method=\"xml\" " +
	    	"encoding=\"utf-8\" indent=\"yes\"/>"+"\n");
    
        /* Generate structure matching rules. */
        String[][] structMapTable = xg.structmap.get_mtable();
        int noStructRules = xg.structmap.curr_mrow;
    
        /* 
        ** Start from the last rule, since the last rule will be the rule for
        ** xml_root.
        */
        for (i = noStructRules - 1; i >= 0; i--) {
            if ((structMapTable[i][4].compareTo("0") == 0) || 
		(structMapTable[i][4].compareTo("3") == 0)) {                 
                output.append("<xsl:template match=\"" +  
                    structMapTable[i][0] + "\">"+"\n");
    
                if (structMapTable[i][0].compareTo(xml_root.getNodeName()) == 0)
                {
                    output.append(xg.textmatch.buildOpenTag(html_root));
                    findRootChildren(html_root, structMapTable[i][1]);
                    output.append(xg.textmatch.buildCloseTag(html_root)+"\n");
               }
                else {
                    output.append(structMapTable[i][2]);          
                    findChildren(structMapTable[i][0], structMapTable[i][1],
			structMapTable[i][4]);
                    output.append(structMapTable[i][3]+"\n");
                }
            
                output.append("</xsl:template>"+"\n");
                output.append("\n");
            }
        }
        
        SequenceTable[] seqtable = xg.checkSeq.get_seqtable();
        int noSeqs = xg.checkSeq.curr_seqtable;
        
        for (i = 0; i < noSeqs; i++) {
            output.append("<xsl:template match=\"" + seqtable[i].xml +
                "\">"+"\n");
            
            generateSeqTableMaps(seqtable[i].xml, seqtable[i].html);
            
            output.append("</xsl:template>"+"\n");
            output.append("\n");
        }
    
        /* Generate text matching rules. */ 
        String[][] textMapTable = xg.textmap.get_mtable();
        int noTextRules = xg.textmap.curr_mrow;
    
        for (i = 0; i < noTextRules; i++) {
            if (textMapTable[i][4].compareTo("0") == 0) {
                output.append("<xsl:template match=\"" +  
                    textMapTable[i][0] + "\">"+"\n");
                output.append(textMapTable[i][2]);
    
                output.append("<xsl:value-of select=\".\"/>");
    
                output.append(textMapTable[i][3]+"\n");
                output.append("</xsl:template>"+"\n");
                output.append("\n");
            }
        }
        
        /* Generate rules for extra XML tags. */
        Object[][] extraTagTable = xg.xmltags.get_tagtable();
        int noTags = xg.xmltags.curr_trow;
        
        for (i = 0; i < noTags; i++) {
            if (extraTagTable[i][0].equals(new Integer(1)) == true) {
                output.append("<xsl:template match=\"" +
                    extraTagTable[i][1] + "\">"+"\n");
            
                findXMLChildren((String) extraTagTable[i][1]);
    
                output.append("</xsl:template>"+"\n");
                output.append("\n"+"");
            }
        }
                
        output.append("</xsl:stylesheet>"+"\n");
    }
    
    private void findRootChildren(Node html, String html_tag) {
        NodeList children;
        int childCount;
    
        if (html.getNodeName().compareTo(html_tag) == 0)
            findChildrenUsingNodes(xml_root, html, 1);
        else {      
            children   = html.getChildNodes();
            childCount = children.getLength();
            for (int j = 0; j < childCount; j++) {
                if (children.item(j).getNodeType() == Node.ELEMENT_NODE) {
                    output.append(xg.textmatch.buildOpenTag(
                        children.item(j)));
                        
                    if (children.item(j).getNodeName().compareTo(html_tag) == 0)
                        findChildrenUsingNodes(xml_root, children.item(j), 1);
                    else    
                        findRootChildren(children.item(j), html_tag);
    
                    output.append(xg.textmatch.buildCloseTag(
                        children.item(j))+"\n");
                }
                else if (children.item(j).getNodeType() == Node.TEXT_NODE) 
                    if (children.item(j).getNodeValue().trim().compareTo("") 
                        != 0)
                        output.append(children.item(j).getNodeValue());
            }
        }   
    }

    private void findChildren(String xml_tag, String html_tag, String flag) { 
        Node[][] structNode = xg.structmap.get_ntable();
        int noStructNodes = xg.structmap.curr_nrow;
    
        boolean found = findChildrenInTable(structNode, noStructNodes, xml_tag,
            html_tag, flag);
//	XTRACT seq = new MatchDTD(xg, xml_tag, html_tag, flag, structNode, 
//		     noStructNodes);
				
        if (found == false) {
            Node[][] origNode = xg.structmap.get_otable();
            int noOrigNodes = xg.structmap.curr_orow;
    
            findChildrenInTable(origNode, noOrigNodes, xml_tag, html_tag, flag);
//	    seq = new MatchDTD(xg, xml_tag, html_tag, flag, origNode,
//		  noOrigNodes);
        }

//      output.append("\n"+xml_tag + " - " + html_tag + " : " + seq.dtd);
    }

    private boolean findChildrenInTable(Node[][] nodeTable, int noNodes,
                                    String xml_tag, String html_tag, 
				    String flag) {
        int i, matchNo = 0;
 	boolean found = false;
    
        countStruct = 0;
    
        for (i = 0; i < noNodes; i++) {
            if ((nodeTable[i][0].getNodeName().compareTo(xml_tag) == 0) &&
                (nodeTable[i][1].getNodeName().compareTo(html_tag) == 0)) {
                found = true;
                matchNo++;
 
		if (flag.compareTo("3") == 0)
	            findChildrenUsingXML(nodeTable[i][0], nodeTable[i][1], 
                        matchNo);
		else 
		    findChildrenUsingNodes(nodeTable[i][0], nodeTable[i][1], 
			matchNo);
            }
        }

        return found;
    }

    private void findChildrenUsingXML(Node xml_node, Node html_node, 
				      int matchNo) {
	int j;

	int rule = xg.textmap.search_hrule(html_node.getNodeName());
	
	if (rule != -1) {
	    if (xg.textmap.maptable[rule][4].compareTo("1") != 0) {
	        if (matchNo == 1)
	            output.append("<xsl:value-of select=\"" + 
		        xg.textmap.maptable[rule][0] + "\"/>");
	    }
	    else
		rule = -1;
	}

	if (rule == -1) {
	    rule = xg.submap.search_sub(html_node.getNodeName());

	    if (rule != -1) {
		if (matchNo == 1)
                    xg.submap.substring[rule].printSubStringTags(
   		        xml_node.getNodeName());
	    }
	}	
    }

    private void findChildrenUsingNodes(Node xml_node, Node html_node, 
                                        int matchNo) {
        NodeList children;
        int j, k, l, childCount;
    
        Node xml;
        String tag;
    
        /* Get all mapping tables. */
        Node[][] textNode = xg.textmap.get_ntable();
        int noTextNodes = xg.textmap.curr_nrow;
    
        SubStringElement[] subString = xg.submap.get_stable();
        int noSubs = xg.submap.curr_srow;
        
        Node[][] structNode = xg.structmap.get_ntable();
        int noStructNodes = xg.structmap.curr_nrow;
        
        Node[][] attrNode = xg.attrmap.get_otable();
        int noAttrNodes = xg.attrmap.curr_orow;
    
        /* Find the matching node for each html_node children. */
        children = html_node.getChildNodes();
        childCount = children.getLength();

        for (j = 0; j < childCount; j++) {
            Node track, prev, start;
               
            xml = null;
                
            /* Try to find a match in TextMapTable */
            xml = searchXMLMatch(textNode, noTextNodes, children.item(j),
                xml_node, matchNo);
                                
            /* 
            ** If there is no match in TextMapTable, try to find a match in
            ** StructMapTable
            */
            if (xml == null)
                xml = searchXMLMatch(structNode, noStructNodes, 
                    children.item(j), xml_node, matchNo);
    
            /* 
                ** If there is no match in both TextMapTable and StructMapTable
            ** StructMapTable, try to find in AttrMapTable.
            */
            if (xml == null) {
                for (k = 0; k < noAttrNodes; k++) {
                    if (attrNode[k][2] == children.item(j)) {
                        track = attrNode[k][0];
        
                        if (track == xml_node) {
                            xml = track;
                            
                            printAttr(xml.getNodeName(), 
                               attrNode[k][1].getNodeName());
                            
                            break;
                        }
                        else {
                            prev = null;
                            start = track;
                            while ((track != xml_node) && (track != null)) {
                                prev  = track;
                                track = track.getParentNode();
                            }
            
                            if (track == xml_node) {                        
                                xml = prev;                 
                                printRule(xml, "", children.item(j), matchNo);
            
                                xg.xmltags.updateMoreTags(start, xml);
                                    
                                break;
                            }
                        }                   
                    }
                }
            }
        
            /* 
            ** If no match is found in Text, Struct or Attr maptable, try to 
            ** find a match in Substring table
            */
            if (xml == null) {
                for (k = 0; k < noSubs; k++) {
                    if (subString[k].html == children.item(j)) {
                        for (l = 0; l < subString[k].no_xml; l++) {
                            if (subString[k].family[l]) {
                                int go_else = 0;
                                    
                                if (subString[k].xmls[l][0].getNodeType() ==
                                    Node.ATTRIBUTE_NODE) {
                                    track = subString[k].xmls[l][1];
                                        
                                    if ((track == xml_node) ||
                                        ((track.getParentNode() == xml_node) &&
                                         (xml_node != xml_root))) {
                                        xml = track;
                                        printSubRule(xml_node.getNodeName(), 
                                            subString[k], matchNo);
                                        break;
                                    }
                                    else
                                        go_else = 1;
                                }
                                else {
                                    track = subString[k].xmls[l][0];
                                        
                                    if (track.getParentNode() == xml_node) {
                                        xml = track;
                                        printSubRule(xml_node.getNodeName(), 
                                            subString[k], matchNo);
                                        break;
                                    }
                                    else 
                                        go_else = 1;
                                }
                                    
                                if (go_else == 1) {
                                    prev  = null;
                                    start = track;
                                    while ((track != xml_node) && 
                                           (track != null)) {
                                        prev  = track;
                                        track = track.getParentNode();
                                    }
                                
                                    if (track == xml_node) {
                                        xml = prev;
                                        printRule(xml, "", children.item(j), 
					    matchNo);
                                                    
                                        xg.xmltags.updateMoreTags(start, xml);
                                            
                                        break;
                                    }
                                }
                            }
                        }
                        
                        break;
                    }
                }  
            }
    
            /* If there is no match in all tables, print out the node. */
            if (xml == null) {
                if (xml_node == xml_root) {
                    if ((children.item(j).getNodeType() == Node.TEXT_NODE) &&
                        (children.item(j).getNodeValue().trim().compareTo("") ==
                         0))
                        ;
                    else
                        output.append(children.item(j)+"\n");
                        
                    continue;
                }
                    
                if ((children.item(j).getNodeType() == Node.ELEMENT_NODE) &&  
                    (children.item(j).getNodeName().compareToIgnoreCase("br") 
                     != 0) && 
                    (children.item(j).getNodeName().compareToIgnoreCase("hr") 
                     != 0)) {
                    if ((children.item(j).hasChildNodes() == false) &&
                        (matchNo == 1))
                        output.append(children.item(j)+"\n");
                    else {
                        tag = children.item(j).getNodeName();
                
                        if ((matchNo == 1) || ((matchNo > 1) &&
                            (search_struct(structChildren, countStruct, 
                             "ELEMENT", tag, "") == 0) && 
                            (countStruct < asize))) {
                            structChildren[countStruct][0] = "ELEMENT";
                            structChildren[countStruct][1] = tag;
                            structChildren[countStruct][2] = "";
                            countStruct++;
    
                            output.append(xg.textmatch.
                                buildOpenTag(children.item(j)));
    
                            findChildrenUsingNodes(xml_node, children.item(j),
                                matchNo); 
    
                            output.append(xg.textmatch.
                                buildCloseTag(children.item(j))+"\n");
                        }
                    }
                }
    
                else if (children.item(j).getNodeType() == Node.TEXT_NODE) {    
    
                    /*
                    ** If a child of the html node is a non-empty text and
                    ** it does not match to any node in xml, print out the value
                    ** of the text node, provided that its previous sibling is
                    ** not of type ELEMENT_NODE, and it's in first iteration.
                    */
                    if ((matchNo == 1) && 
                        ((j == 0) || 
                         ((j > 0) && 
                          (children.item(j-1).getNodeType() != 
                           Node.ELEMENT_NODE))) &&
                        (xg.textmap.search_xmlmatch(children.item(j)) == null)
                        &&
                        (xg.attrmap.search_xmlmatch(children.item(j)) == null)
                        &&
                        (xg.submap.search_xmlmatch(children.item(j)) == null)
                        &&
                        (children.item(j).getNodeValue().trim().compareTo("") 
                        != 0))
                        output.append(children.item(j).getNodeValue());
            
                }
            }
        }
    }   
    
    private Node searchXMLMatch(Node[][] nodetable, int size, Node html_child,
                                Node xml_node, int matchNo) {
        Node xml = null, track, prev, start;
        
        for (int i = 0; i < size; i++) {
            if (nodetable[i][1] == html_child) {
                track = nodetable[i][0];
        
                if (track.getParentNode() == xml_node) {
                    xml = track;
                    printRule(xml, "", html_child, matchNo);
                    break;
                }
                else {
                    prev = null;
                    start = track;
                    while ((track != xml_node) && (track != null)) {
                        prev  = track;
                        track = track.getParentNode();
                    }
        
                    if (track == xml_node) {
                        xml = prev;
                        printRule(xml, "", html_child, matchNo);
        
                        xg.xmltags.updateMoreTags(start, xml);
        
                        break;
                    }
                }
            }
        }
        
        return xml;
    }   
    
    private void printRule(Node xml, String owner, Node html, int matchNo) {
    	if(xml==null) return;
	String tag = xml.getNodeName();

        if (tag.compareTo("#text") == 0) {
            if (matchNo == 1) {
                output.append(xg.textmatch.buildOpenTag(html));
                output.append("<xsl:value-of select=\"text()\"/>");
                output.append(xg.textmatch.buildCloseTag(html)+"\n");
            }
        }
        else {
            if ((search_struct(structChildren, countStruct, "ELEMENT", tag, 
                 owner) == 0) && (countStruct < asize)) {
                structChildren[countStruct][0] = "ELEMENT";
                structChildren[countStruct][1] = tag;
                structChildren[countStruct][2] = owner;
                countStruct++;

		int index;
		
		index = xg.textmap.search_rule(tag);
		if ((index != -1) && (xg.textSub.countSameSiblings(xml) == 1) &&
		    (xg.textmap.maptable[index][4].compareTo("1") != 0) &&
		    (xg.textmap.maptable[index][4].compareTo("2") != 0)) {
//		    output.append(xg.textmap.maptable[index][2]);
		    output.append("<xsl:apply-templates select=\"" + tag +
		 	"\"/>"+"\n");
//		    output.append("\n"+xg.textmap.maptable[index][3]);
		    
//		    xg.textmap.maptable[index][4] = "3";
		}
		else 
                    output.append("<xsl:apply-templates select=\"" + tag + 
                        "\"/>"+"\n");
            }
        }
    }    
    
    private void printAttr(String owner, String tag) {
        if ((search_struct(structChildren, countStruct, "ATTRIBUTE", tag, owner)
             == 0) && (countStruct < asize)) {
            structChildren[countStruct][0] = "ATTRIBUTE";
            structChildren[countStruct][1] = tag;
            structChildren[countStruct][2] = owner;
            countStruct++;
    
            int rule = xg.attrmap.search_rule(owner, tag);
                        
            output.append(xg.attrmap.maptable[rule][3]);
            output.append("<xsl:value-of select=\"@" + tag + "\"/>");
            output.append(xg.attrmap.maptable[rule][4]+"\n");
        }
    }
    
    private void printSubRule(String xml, SubStringElement substr, int matchNo)
    {
        Object[][] tags = substr.tags;
        int noTags      = substr.noTags;
        
        if ((matchNo == 1) && (substr.html.getNodeType() == Node.ELEMENT_NODE))
            output.append(xg.textmatch.buildOpenTag(substr.html)+"\n");
        
        for (int i = 0; i < noTags; i++) {
            if (((String) tags[i][0]).compareTo("EXTRA") == 0) {
                if ((matchNo == 1) || ((matchNo > 1) && 
                    (search_struct(structChildren, countStruct,"EXTRA",
                    (String) tags[i][1], "") == 0) && (countStruct < asize))) {
                    structChildren[countStruct][0] = "EXTRA";
                    structChildren[countStruct][1] = (String) tags[i][1];
                    structChildren[countStruct][2] = "";
                    countStruct++;

		    output.append("<xsl:text>");
                    output.append(tags[i][1]);
                    output.append("</xsl:text>"+"\n");
                }
            }
            else if ((((String) tags[i][0]).compareTo("TEXT") == 0) &&
                     (matchNo == 1))
                output.append("<xsl:value-of select=\"text()\"/> ");
            else if (((String) tags[i][0]).compareTo("ELEMENT") == 0) {
                int position   = ((Integer) tags[i][3]).intValue();
                String xml_tag = (String) tags[i][1];
                
                if (position != 0)
                    xml_tag = xml_tag + "[" + position + "]";
                    
                if ((search_struct(structChildren, countStruct, "ELEMENT", 
                     xml_tag, "") == 0) && (countStruct < asize)) {
                    structChildren[countStruct][0] = "ELEMENT";
                    structChildren[countStruct][1] = xml_tag;
                    structChildren[countStruct][2] = "";
                    countStruct++;
    
                    if (((String) tags[i][1]).compareTo(xml) == 0)
                        output.append("<xsl:value-of select=\"" + xml_tag +
                            "\"/>"); 
                    else
                        output.append("<xsl:value-of select=\"" + xml_tag
                            + "\"/>"); 
                }
            }   
            else if (((String) tags[i][0]).compareTo("ATTRIBUTE") == 0) {
                int position   = ((Integer) tags[i][3]).intValue();
                String xml_tag = (String) tags[i][2];
                
                if (position != 0)
                    xml_tag = xml_tag + "[" + position + "]";
                    
                if ((search_struct(structChildren, countStruct, "ELEMENT", 
                    (String) tags[i][1], xml_tag) == 0) && 
                    (countStruct < asize)) {
                    structChildren[countStruct][0] = "ELEMENT";
                    structChildren[countStruct][1] = (String) tags[i][1];
                    structChildren[countStruct][2] = xml_tag;
                    countStruct++;
    
                    if (((String) tags[i][2]).compareTo(xml) == 0)
                        output.append("<xsl:value-of select=\"@" + tags[i][1]
                            + "\"/>");  
                    else
                        output.append("<xsl:value-of select=\"" + xml_tag
                            + "/@" + tags[i][1] + "\"/>");
                }
            }
        }                               
        
        if ((matchNo == 1) && (substr.html.getNodeType() == Node.ELEMENT_NODE))
            output.append(xg.textmatch.buildCloseTag(substr.html)+"\n");
    }
    
    private void generateSeqTableMaps(String xml, int[][] html) {
        int i;
        
        for (i = 0; i < html.length; i++) {
            if (html[i][0] == xg.checkSeq.TEXT_TABLE) {
                output.append(xg.textmap.maptable[html[i][1]][2]);           

                if (xg.textmap.maptable[html[i][1]][4].compareTo("1") == 0) {
                    int matchAt = xg.submap.findSubStringTags(
                        xg.textmap.maptable[html[i][1]][1],
                        xg.textmap.maptable[html[i][1]][0]);
                    
                    if (matchAt != -1)  
                        xg.submap.substring[matchAt].printSubStringTags(xml);
                }
                else {                  
                    if (xg.textmap.maptable[html[i][1]][0].compareTo(xml) == 0)
                        output.append("<xsl:value-of select=\".\"/>");
                    else
                        output.append("<xsl:value-of select=\"" + 
                            xg.textmap.maptable[html[i][1]][0] + "\"/>");
                }
                    
                output.append(xg.textmap.maptable[html[i][1]][3]+"\n");
            }
            else if (html[i][0] == xg.checkSeq.ATTR_TABLE) {
                output.append(xg.attrmap.maptable[html[i][1]][3]);           

                if (xg.attrmap.maptable[html[i][1]][5].compareTo("1") == 0) {
                    int matchAt = xg.submap.findSubStringTags(
                        xg.attrmap.maptable[html[i][1]][2],
                        xg.attrmap.maptable[html[i][1]][0]);
                    
                    if (matchAt != -1)
                        xg.submap.substring[matchAt].printSubStringTags(xml);
                }
                else {                    
                    if (xg.attrmap.maptable[html[i][1]][0].compareTo(xml) == 0)
                        output.append("<xsl:value-of select=\"@" +
                            xg.attrmap.maptable[html[i][1]][1] + "\"/>");
                    else
                        output.append("<xsl:value-of select=\"" + 
                            xg.attrmap.maptable[html[i][1]][0] + "/@" +
                            xg.attrmap.maptable[html[i][1]][1] + "\"/>");
                }
                    
                output.append(xg.attrmap.maptable[html[i][1]][4]+"\n");
            }      
            else if (html[i][0] == xg.checkSeq.STRUCT_TABLE) {
                if (xg.structmap.maptable[html[i][1]][0].compareTo(
                    xml_root.getNodeName()) == 0) {
                    output.append(xg.textmatch.buildOpenTag(html_root));
                    findRootChildren(html_root, 
                        xg.structmap.maptable[html[i][1]][1]);
                    output.append(xg.textmatch.buildCloseTag(html_root)+"\n");
                }
                else {
                    output.append(xg.structmap.maptable[html[i][1]][2]);     
                    findChildren(xg.structmap.maptable[html[i][1]][0], 
                        xg.structmap.maptable[html[i][1]][1],
			xg.structmap.maptable[html[i][1]][4]);
                    output.append(xg.structmap.maptable[html[i][1]][3]+"\n");
                }
            }
        }
    }                                           

    private void findXMLChildren(String xml_tag) {
        int k;
        
        Node[][] attrNode = xg.attrmap.get_otable();
        int noAttrNodes = xg.attrmap.curr_orow;
    
        SubStringElement[] subString = xg.submap.get_stable();
        int noSubs = xg.submap.curr_srow;
        
        countStruct = 0;
	
	boolean goElse = true;

	int result = xg.attrmap.search_rule(xml_tag);      

	if ((result != -1) &&
            (xg.attrmap.maptable[result][5].compareTo("1") == 0)) {
            int matchAt = xg.submap.findSubStringTags(
                          xg.attrmap.maptable[result][2],
                          xg.attrmap.maptable[result][0]);
                    
            if (matchAt != -1) {
	        output.append(xg.attrmap.maptable[result][3]+"\n");
                xg.submap.substring[matchAt].printSubStringTags(xml_tag);
		output.append(xg.attrmap.maptable[result][4]+"\n");
		   
		goElse = false;
	    }
        }

	if (goElse == true) {
            for (k = 0; k < noAttrNodes; k++) {
                if (attrNode[k][0].getNodeName().compareTo(xml_tag) == 0)
                    printAttr(xml_tag, attrNode[k][1].getNodeName());
            }
        
            for (k = 0; k < noSubs; k++) {
                for (int l = 0; l < subString[k].no_xml; l++) {
                    if (subString[k].xmls[l][0].getNodeType() == 
                        Node.ATTRIBUTE_NODE) {
                        if (subString[k].xmls[l][1].getNodeName().
			    compareTo(xml_tag) == 0) 
                            printAttr(xml_tag, subString[k].xmls[l][0].
                                getNodeName()); 
                    }
		}
            }
        }
/*    
	int result = xg.textmap.search_rule(xml_tag);

	if ((result != -1) && 
    	    (xg.textmap.maptable[result][4].compareTo("1") == 0)) {
            int matchAt = xg.submap.findSubStringTags(
                          xg.textmap.maptable[result][1],
                          xg.textmap.maptable[result][0]);
                    
            if (matchAt != -1) {  
		output.append("\n"+xg.textmap.maptable[result][2]);
                xg.submap.substring[matchAt].printSubStringTags(xml_tag);
		output.append("\n"+xg.textmap.maptable[result][3]);
	    }
	}
*/
        Node[] extraNode = xg.xmltags.get_ntable();
        int noExtra = xg.xmltags.curr_nrow;
        
        NodeList children;
        int j, childCount, matchNo = 0; 
        String tag;
        
        for (k = 0; k < noExtra; k++) {
            if (extraNode[k].getNodeName().compareTo(xml_tag) == 0) {
                matchNo++;
                            
                children = extraNode[k].getChildNodes();
                childCount = children.getLength();
        
                /* 
                ** Count the number of non-empty TEXT_NODE children that have  
                ** html matches.
                */
                int textChild = 0;      
                for (j = 0; j < childCount; j++) {
                    if ((children.item(j).getNodeType() == Node.TEXT_NODE) &&
                        (children.item(j).getNodeValue().trim().compareTo("")
                         != 0) &&
                        ((xg.textmap.search_htmlmatch(children.item(j)) != null)
                        || (xg.structmap.search_htmlmatch(children.item(j)) != 
                        null) || 
                        (xg.submap.search_htmlmatch(children.item(j)) != null)))
                        textChild++;
                }
                
                /* 
                ** If the number of non-empty TEXT_NODE is greater than 1, and
                ** they have html matches, use <xsl:apply-templates/>.
                */
                if (textChild > 1) {
                    output.append("<xsl:apply-templates/>"+"\n");
                    break;
                }
                
                /* 
                ** Else, specify the children that needs to be printed out, 
                ** i.e. those who have matches, or are in the moreXMLTags.
                */
                else {    
                    if ((childCount == 1) && (textChild == 1))
                        output.append("<xsl:value-of select=\".\"/>");
                    else {
                        for (j = 0; j < childCount; j++) {
                            tag = children.item(j).getNodeName();
                    
                            if ((children.item(j).getNodeType() == 
                             Node.TEXT_NODE) &&
                            (children.item(j).getNodeValue().trim().
                                 compareTo("") == 0))
                                ;
                            else if ((xg.textmap.search_htmlmatch(
                                      children.item(j)) != null) ||
                                     (xg.structmap.search_htmlmatch(
                                      children.item(j)) != null) ||
                                     (xg.submap.search_htmlmatch(
                                      children.item(j)) != null) ||
                                     (xg.xmltags.search_outtag(tag) != -1)) 
                                printRule(children.item(j), "", null, matchNo);
                        }
                    }
                }
            }
        }
    }

    private int search_struct(String[][] array, int size, String type, 
                      String tag, String owner) {
        for (int i = 0; i < size; i++) {
            if ((array[i][0].compareTo(type) == 0) &&
                (array[i][1].compareTo(tag) == 0) && 
                (array[i][2].compareTo(owner) == 0))
                return 1;
        }
    
        return 0;
    }

	public String getXsltContent() {
		return output.toString();
	}

}
