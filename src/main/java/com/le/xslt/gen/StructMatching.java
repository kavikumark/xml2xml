package com.le.xslt.gen;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class StructMatching
{
    private XSLTGen xg;

    private Node xml_root, html_root, html_body;
 
    private int i;
    private NodeList childlist;
    
    private static final int ATTRIBUTE     = 1;
    private static final int TEXTorELEMENT = 2;    

    public StructMatching(XSLTGen xg, Node xml_root, Node html_root) {
        this.xg = xg;
    
        this.xml_root  = xml_root;
        this.html_root = html_root;

        /* Find the html "body" node. */
        childlist = html_root.getChildNodes();
        for (i = 0; i < childlist.getLength(); i++) {
            if (childlist.item(i).getNodeName().compareToIgnoreCase("body") 
                == 0) {
                html_body = childlist.item(i);
                break;
            }
        }
        
        if (html_body == null)
            html_body = html_root;
    
        doStructMatch(xml_root, html_body);

	if (xg.structmap.curr_nrow == 0) {
	    String xml_tag = xml_root.getNodeName(),
	    	   html_tag = html_body.getNodeName(),
                   html_opentag = xg.textmatch.buildOpenTag(html_body),
                   html_closetag = xg.textmatch.buildCloseTag(html_body);
            xg.structmap.insert_rule(xml_tag, html_tag, html_opentag, 
                html_closetag);     
            xg.structmap.insert_matchnode(xml_root, html_body);
	}
    }

    /* 
    ** doStructMatch:
    **     This function tries to match the nodes in the html document with
    **     those in the xml document based on the structure of the two trees,
    **     using bottom-up approach.
    */
    private void doStructMatch(Node xml_node, Node html_node) {
        int i, childCount;
        NodeList children;
    
        /* Do Structure Matching for all html node children (if any). */
        if (html_node.hasChildNodes()) {
            children = html_node.getChildNodes();
            childCount = children.getLength();
            for (i = 0; i < childCount; i++)
                doStructMatch(xml_node, children.item(i));
        }
    
        /* Find the matching xml node for the html node. */
        if (html_node.getNodeType() == Node.ELEMENT_NODE)
            doStructSearch(xml_node, html_node);
    }   

    /*
    ** doStructSearch:
    **     This function finds the match of html_node in the xml document
    **     starting with the xml_node.
    */
    private void doStructSearch(Node xml_node, Node html_node) {
        String xml_tag, html_tag, html_opentag, html_closetag;
        int i, j, xmlChildCount, xmlChildElement = 0, 
		  htmlChildCount, htmlChildElement = 0;
        NodeList xmlChildren, htmlChildren;
        int foundmatch = 1;
    
        /* 
        ** Check whether the node in the subtree of xml_node can match the 
        ** html_node.
        */
        if (xml_node.hasChildNodes()) {
            xmlChildren = xml_node.getChildNodes();
            xmlChildCount = xmlChildren.getLength();
            for (i = 0; i < xmlChildCount; i++) {
                if (xmlChildren.item(i).getNodeType() == Node.ELEMENT_NODE)
                    doStructSearch(xmlChildren.item(i), html_node);
            }
        }
    
        /* 
        ** Check whether the html_node matches the xml_node by examining whether
        ** each html_node child has a match in the subtree of the xml_node or
        ** does not have any match in the entire subtree.
        */
        if (html_node.hasChildNodes()) {
            htmlChildren = html_node.getChildNodes();
            htmlChildCount = htmlChildren.getLength();
    
            /* Count the number of html_node ELEMENT_NODE children. */
            htmlChildElement = 0;
            for (i = 0; i < htmlChildCount; i++) {
                if (htmlChildren.item(i).getNodeType() == Node.ELEMENT_NODE)
                    htmlChildElement++;
            }
    
            /* 
            ** Variable to count the number of html children that do not have
            ** any match in the entire xml document.
            */
            int check = 0;
    
            for (i = 0; i < htmlChildCount; i++) {
                
                /* 
                ** Special cases for <br/> and <hr/> where they will not have
                ** any xml match.
                */
                if ((htmlChildren.item(i).getNodeName().compareToIgnoreCase(
                    "br") == 0) ||
                    (htmlChildren.item(i).getNodeName().compareToIgnoreCase(
                    "hr") == 0))
                    foundmatch &= 1;
            
                /* Ignore empty html TEXT_NODE children. */
                else if ((htmlChildren.item(i).getNodeType() == Node.TEXT_NODE)
                    && (htmlChildren.item(i).getNodeValue().trim().compareTo("")
                        == 0)) 
                    foundmatch &= 1;
                
                /* 
                ** If the number of ELEMENT_NODE children is more than 1, and
                ** the current child being checked do not have any match in the
                ** entire xml document, treat them as valid child node. 
                */
                else if ((htmlChildElement > 1)
                    && (htmlChildren.item(i).getNodeType() == Node.ELEMENT_NODE)
                    && (xg.textmap.search_xmlmatch(htmlChildren.item(i))== null)
                    && (xg.attrmap.search_xmlmatch(htmlChildren.item(i))== null)
                    && (xg.submap.search_xmlmatch(htmlChildren.item(i)) == null)
                    && (xg.structmap.search_xmlmatch(htmlChildren.item(i)) == 
                        null)) {
                    foundmatch &= 1;
        
                    /* 
                    ** Increment the number of html_node children that do not
                    ** have any match in the entire xml document.
                    */
                    check++;
                }
                
                /* 
                ** For all other kinds of child nodes, try to find its match 
                ** using the rules / mappings found in Text Matching and 
                ** previous Structure Matching.
                */
                else {
                    int innermatch = -1;
        
                    /* Try to find a match in text maptable (EXACT match). */
                    innermatch = findMatch(TEXTorELEMENT, 
                        xg.textmap.get_ntable(), xg.textmap.curr_nrow, 
                        htmlChildren.item(i), xml_node);
        
                    /* 
                    ** If no match is found, try to find a match in struct
                    ** maptable.
                    */
                    if (innermatch == -1)
                        innermatch = findMatch(TEXTorELEMENT, 
                            xg.structmap.get_ntable(), xg.structmap.curr_nrow, 
                            htmlChildren.item(i), xml_node);
        
                    /*
                    ** If no match is found, try to find a match in attribute
                    ** maptable.
                    */
                    if (innermatch == -1) 
                        innermatch = findMatch(ATTRIBUTE, 
                            xg.attrmap.get_otable(), xg.attrmap.curr_orow,
                            htmlChildren.item(i), xml_node);
        
                    /*
                    ** If no match is found, try to find a match in substring 
                    ** table.
                    */
                    if (innermatch == -1)
                        innermatch = findMatchInSubstr(xg.submap.get_stable(), 
                            xg.submap.curr_srow, htmlChildren.item(i), 
                            xml_node);
        
                    /* 
                    ** If no matching is found in all mapping tables, check it
                    ** against the following conditions.
                    */
                    if (innermatch == -1) {
                        
                        /* 
                        ** If a html ELEMENT_NODE child, who has no children,
                        ** has no matches at all, ignore them. This is done to
                        ** handle <td/>, <th/>
                        */
                        if ((htmlChildren.item(i).getNodeType() == 
                             Node.ELEMENT_NODE) && 
                            (htmlChildren.item(i).hasChildNodes() == false)) {
                            innermatch = 1;
        
                            /* 
                            ** Increment the number of html_node children that 
                            ** do not have any match in the entire xml document.
                            */
                            check++;
                        }               
                    
                        /* 
                        ** If a html text node does not have a match, and number
                        ** of html_node ELEMENT_NODE children is non-zero, check
                        ** whether it is possible to append it to the closing 
                        ** tag of its previous ELEMENT_NODE sibling.
                        */
                        else if ((htmlChildren.item(i).getNodeType() == 
                                  Node.TEXT_NODE) && (htmlChildElement != 0)) {
                            if ((i > 0) && 
                                (htmlChildren.item(i-1).getNodeType() ==
                                 Node.ELEMENT_NODE)) {
                                Node xml = xg.textmap.search_xmlmatch(
                                htmlChildren.item(i-1));
            
                                if (xml == null) {
                                    xml = xg.submap.search_xmlmatch(
                                        htmlChildren.item(i-1));
            
                                    if ((xml != null) && (xml.getNodeType() ==
                                         Node.ATTRIBUTE_NODE))
                                        xml = null;
                                }
            
                                if (xml != null) {
                                    int replace = xg.textmap.search_rule(
                                        xml.getNodeName(), 
                                        htmlChildren.item(i-1).getNodeName());
                                    xg.textmap.replace_rule(replace,
                                        htmlChildren.item(i). getNodeValue());
                                }
                            }
            
                            innermatch = 1;
                        }
                        
                        /* For all other html children, set the as invalid. */
                        else
                            innermatch = 0;
                    }
            
                    /* 
                    ** AND the validity for a children (innermatch) with the 
                    ** validity of the html_node (foundmatch).
                    */
                    foundmatch &= innermatch;
                }
            }
    
            /* 
            ** If html_node is found to be valid, but all of its children do not
            ** have any match in the entire xml document, then this html_node 
            ** is a new inserted node in the html document and musti be set as
            ** invalid.
            */
            if ((foundmatch == 1) && (check == htmlChildElement)) 
                foundmatch = 0;

            /* If finally a valid mapping is found, add a new rule. */
            if (foundmatch == 1) {
                
                /* Get the tag names and build the opening and closing tags. */
                xml_tag = xml_node.getNodeName();
                html_tag = html_node.getNodeName();
                html_opentag = xg.textmatch.buildOpenTag(html_node);
                html_closetag = xg.textmatch.buildCloseTag(html_node);
        
                /* Check whether the new rule exists in the struct maptable. */
                int result = xg.structmap.search_supernode(xml_node, html_node);
        
//              System.out.println(result + " : " + xml_tag + " - " + html_tag);
        
                /* 
                ** If the result is -1, the rule does not exist in the maptable
                ** and hence insert the new rule.
                */
                if (result == -1) {
                    xg.structmap.insert_rule(xml_tag, html_tag, html_opentag, 
                        html_closetag);     
                    xg.structmap.insert_matchnode(xml_node, html_node);
                }
                
                /* 
                ** If the result is greater than 1, it means that we have to
                ** update the existing rule. Distinguish between html_node which
                ** have only 1 ELEMENT_NODE child and those with more than 1.
                */
                else if (result > -1) {
                    if (xml_node != xml_root) {
                        if (htmlChildElement == 1)
                            xg.structmap.replace_rule(result, xml_tag, html_tag,
                                html_opentag, html_closetag);
                        else if (htmlChildElement > 1)
                            xg.structmap.change_rule(result, xml_tag, html_tag,
                                html_opentag, html_closetag);
                  
                        xg.structmap.replace_matchnode(result, xml_node, 
                            html_node);
                    }
                }
            }
	}

        /* 
        ** Check whether the html_node matches the xml_node by examining whether
	** each xml_node child matches the html_node or does not have any match
        ** in the entire subtree.
        */
        if ((foundmatch == 0) && html_node.hasChildNodes() && 
	    (htmlChildElement == 0) && xml_node.hasChildNodes()) {
	    
	    /*
	    ** Only proceed if all xml nodes with the same name as xml_node have
	    ** the same structure by examining their dtd.
	    */

	    foundmatch = 1;

            xmlChildren = xml_node.getChildNodes();
            xmlChildCount = xmlChildren.getLength();

            /* Count the number of html_node ELEMENT_NODE children. */
            xmlChildElement = 0;
            for (i = 0; i < xmlChildCount; i++)
                if (xmlChildren.item(i).getNodeType() == Node.ELEMENT_NODE)
                    xmlChildElement++;
            
    
            /* 
            ** Variable to count the number of html children that do not have
            ** any match in the entire xml document.
            */
            int check = 0;

	    for (i = 0; i < xmlChildCount; i++) {
                /* Ignore empty xml TEXT_NODE children. */
                if ((xmlChildren.item(i).getNodeType() == Node.TEXT_NODE) &&
                    (xmlChildren.item(i).getNodeValue().trim().compareTo("")
                     == 0)) 
                    foundmatch &= 1;
                
                /* 
                ** If the number of ELEMENT_NODE children is more than 1, and
                ** the current child being checked do not have any match in the
                ** entire html document, treat them as valid child node. 
                */
                else if ((xmlChildElement > 1) 
                    && (xmlChildren.item(i).getNodeType() == Node.ELEMENT_NODE)
                    && (xg.textmap.search_htmlmatch(xmlChildren.item(i))== null)
                    && (xg.attrmap.search_htmlmatch(xmlChildren.item(i))== null)
                    && (xg.submap.search_htmlmatch(xmlChildren.item(i)) == null)
                    && (xg.structmap.search_htmlmatch(xmlChildren.item(i)) == 
                        null)) {
                    foundmatch &= 1;
        
                    /* 
                    ** Increment the number of xml_node children that do not
                    ** have any match in the entire xml document.
                    */
                    check++;
                }
		else {
		    int innermatch = -1;
		    Node html = null;

		    html = xg.textmap.search_htmlmatch(xmlChildren.item(i));

		    if (html == html_node)
			innermatch = 1;
		    else if (html != null)
			innermatch = 0;
/*		    
		    if (html == null) {
			html = xg.attrmap.search_htmlmatch(xmlChildren.item(i));

			if (html == html_node)
			    innermatch = 1;
			else if (html != null)
			    innermatch = 0;
		    }
*/
		    if (html == null) {
			html = xg.submap.search_htmlmatch(
			    xmlChildren.item(i));

			if (html == html_node)
			    innermatch = 1;
			else if (html != null)
			    innermatch = 0;
		    }
		
		    if (html == null) {
			html = xg.structmap.search_htmlmatch(
			    xmlChildren.item(i));

			if (html == html_node)
			    innermatch = 1;
			else if (html != null)
			    innermatch = 0;
		    }

		    if (html == null) 
			innermatch = 0;

		    foundmatch &= innermatch;
		}
	    }

	    if ((foundmatch == 1) && (check == xmlChildElement))
		foundmatch = 0;

            /* If finally a valid mapping is found, add a new rule. */
            if (foundmatch == 1) {
                
                /* Get the tag names and build the opening and closing tags. */
                xml_tag = xml_node.getNodeName();
                html_tag = html_node.getNodeName();
                html_opentag = xg.textmatch.buildOpenTag(html_node);
                html_closetag = xg.textmatch.buildCloseTag(html_node);
        
                /* Check whether the new rule exists in the struct maptable. */
                int result = xg.structmap.search_supernode(xml_node, html_node);
        
//              System.out.println(result + " : " + xml_tag + " - " + html_tag);
        
                /* 
                ** If the result is -1, the rule does not exist in the maptable
                ** and hence insert the new rule.
                */
                if (result == -1) {
//                    xg.structmap.insert_rule(xml_tag, html_tag, html_opentag, 
//                        html_closetag, "3");     
//                    xg.structmap.insert_matchnode(xml_node, html_node);
		    ; 
                }
	    }
	}     
    }
    
    /* 
    ** findMatch:
    **     This function checks whether 'html_child' has a match in the subtree
    **     of 'xml_node', using 'nodetable'.
    **     Return value:
    **         -1   : No match is found in 'nodetable'
    **          0   : It has a match but not in 'xml_node' subtree
    **      	1   : It has a match in 'xml_node' subtree
    */
    private int findMatch(int type, Node[][] nodetable, int size, 
                          Node html_child, Node xml_node) {
        Node xml;
        int innermatch = -1;
        
        for (int j = 0; j < size; j++) {
            Node html;
            
            if (type == TEXTorELEMENT)
                html = nodetable[j][1];
            else
                html = nodetable[j][2];
                
            if (html == html_child) {
                xml = nodetable[j][0];

                while ((xml != xml_node) && (xml != xml_root))
                    xml = xml.getParentNode(); 
        
                if (xml == xml_node) {
                    innermatch = 1;
                    break;
                }
                else 
                    innermatch = 0;
            }
        }
    
        return innermatch;
    }

    /* 
    ** findMatch:
    **     This function checks whether 'html_child' has a match in the subtree
    **     of 'xml_node', using 'substring' table.
    **     Return value:
    **         -1   : No match is found in 'substring' table
    **          0   : It has a match but not in 'xml_node' subtree
    **          1   : It has a match in 'xml_node' subtree
    */
    private int findMatchInSubstr(SubStringElement[] substring, int size,
                      Node html_child, Node xml_node) {
        Node xml;
        int innermatch = -1;
        
        for (int j = 0; j < size; j++) {
            if (substring[j].html == html_child) {
                for (int k = 0; k < substring[j].no_xml; k++) {
                    if (substring[j].xmls[k][0].getNodeType() ==
                        Node.ATTRIBUTE_NODE)
                        xml = substring[j].xmls[k][1];
                    else
                        xml = substring[j].xmls[k][0];
        
                    while ((xml != xml_node) && (xml != xml_root))
                        xml = xml.getParentNode();
                        
                    if (xml == xml_node) {
                        innermatch = 1;
                        break;
                    }
                    else
                        innermatch = 0;
                }
        
                if (innermatch == 1)
                    break;
            }
        }
        
        return innermatch;
    }                     
}
