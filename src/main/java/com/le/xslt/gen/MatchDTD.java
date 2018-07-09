package com.le.xslt.gen;
import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MatchDTD extends XTRACT 
{
    private String xml_tag;
    private String flag;

    private Node[][] nodeTable;
    private int noNodes;
    public boolean found;

    public MatchDTD(XSLTGen xg, String xml_tag, String html_tag, String flag, 
	Node[][] nodeTable, int noNodes) {

	super(xg.checkSeq.XML, true, xg, html_tag);

	this.xml_tag   = xml_tag;
	this.flag      = flag;
	this.nodeTable = nodeTable;
	this.noNodes   = noNodes;
	found          = false;

	findAllSequences();
	new_sym        = symtable.size();
//	print_seq(sequence);

	generalize();
//	print_seq(sg);     

	factorSubsets();
//	print_seq(sf);
//      print_seq(symtable);

	dtd = mdl();
    }

    public MatchDTD(ArrayList seq, ArrayList sym) {
	super(seq, sym);
    }

    public void findAllSequences() {
        String seq = "";
        
        for (int i = 0; i < noNodes; i++) {
            if ((nodeTable[i][0].getNodeName().compareTo(xml_tag) == 0) &&
                (nodeTable[i][1].getNodeName().compareTo(elemName) == 0)) {
                found = true;

		dtdElem = nodeTable[i][1].getNodeName();

		if (flag.compareTo("0") == 0)
	            seq = findChildrenUsingNodes(nodeTable[i][0], 
			  nodeTable[i][1]);
		else if (flag.compareTo("3") == 0)
		    seq = findChildrenUsingXML(nodeTable[i][0], 
			  nodeTable[i][1]);

		if (seq.compareTo("") != 0) 
                    if (sequence.indexOf(seq) == -1)
                        sequence.add(seq);   
	    }
	}
    }

    private String findChildrenUsingNodes(Node xml_node, Node html_node) {
        NodeList children;
        int i, j, k, childCount;

	String sequence = "", tag;
	Node xml;

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

        for (i = 0; i < childCount; i++) {
            Node track, prev;
               
            xml = null;
                
            /* Try to find a match in TextMapTable */
            for (j = 0; j < noTextNodes; j++) {
                if (textNode[j][1] == children.item(i)) {
                    track = textNode[j][0];
		    tag   = "";
        
                    if (track.getParentNode() == xml_node) {
                        xml = track;
			tag = xml.getNodeName();
			sequence = sequence + getSymbol(tag, xml, "ELEMENT");
                        break;
                    }
                    else {
                        prev = null;

                        while ((track != xml_node) && (track != null)) {
                            prev  = track;
                            track = track.getParentNode();

			    if (tag.compareTo("") == 0)
				tag = prev.getNodeName();
			    else
				tag = prev.getNodeName() + "/" + tag;
                        }
        
                        if (track == xml_node) {
			    xml = prev;
			    sequence = sequence + getSymbol(tag, xml, 
				       "ELEMENT");
                            break;
			}
                    }
                }
	    }
                                
            /* 
            ** If there is no match in TextMapTable, try to find a match in
            ** StructMapTable
            */
            if (xml == null) {
                for (j = 0; j < noStructNodes; j++) {
                    if (structNode[j][1] == children.item(i)) {
                        track = structNode[j][0];
			tag   = "";
        
                        if (track.getParentNode() == xml_node) {
                            xml = track;
		    	    tag = xml.getNodeName();
			    sequence = sequence + getSymbol(tag, xml, 
				       "ELEMENT");
                            break;
                        }
                        else {
                            prev = null;

                            while ((track != xml_node) && (track != null)) {
                                prev  = track;
                                track = track.getParentNode();

			        if (tag.compareTo("") == 0)
				    tag = prev.getNodeName();
			        else
				    tag = prev.getNodeName() + "/" + tag;
                            }
        
                            if (track == xml_node) {
			        xml = prev;
			        sequence = sequence + getSymbol(tag, xml, 
					   "ELEMENT");
                                break;
			    }
			}
                    }
                }
	    }
    
            /* 
            ** If there is no match in both TextMapTable and StructMapTable,
            ** try to find in AttrMapTable.
            */
            if (xml == null) {
                for (j = 0; j < noAttrNodes; j++) {
                    if (attrNode[j][2] == children.item(i)) {
                        track = attrNode[j][0];
			tag   = "";
        
                        if (track == xml_node) {
                            xml = track;
                            tag = "@" + attrNode[j][1].getNodeName();
			    sequence = sequence + getSymbol(tag, xml, 
				       "ATTRIBUTE");
                            break;
                        }
                        else {
                            prev = null;

                            while ((track != xml_node) && (track != null)) {
                                prev  = track;
                                track = track.getParentNode();

				if (tag.compareTo("") == 0)
				    tag = prev.getNodeName() + "/@" + 
					  attrNode[j][1].getNodeName();
				else
				    tag = prev.getNodeName() + "/" + tag;
                            }
            
                            if (track == xml_node) {                        
                                xml = prev;                 
           			sequence = sequence + getSymbol(tag, xml, 
					   "ATTRIBUTE"); 
                                    
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
                for (j = 0; j < noSubs; j++) {
                    if (subString[j].html == children.item(i)) 
			sequence = sequence + getSubStringTags(j, xml_node); 
                }  
            }
    
            /* If there is no match in all tables, print out the node. */
            if (xml == null) {
                if (children.item(j).getNodeType() == Node.ELEMENT_NODE) 
		    sequence = sequence + getSymbol(children.item(i).
			       getNodeName(), children.item(i), "HTML");
                else if (children.item(j).getNodeType() == Node.TEXT_NODE)   
//		    sequence = sequence + getSymbol(children.item(i).
//			       getNodeValue(), children.item(i), "EXTRA");  
		    ;
            }
        }

	return sequence;
    }  

    private String getSubStringTags(int index, Node xml_node) {
        Object[][] tags = xg.submap.substring[index].tags;
	Node[] tagNode  = xg.submap.substring[index].tagNode;
	int noTags      = xg.submap.substring[index].noTags;
	int k, position;
	String sequence = "", tag = "";
	Node xml, track, prev;

	for (k = 0; k < noTags; k++) {
	    if (((String) tags[k][0]).compareTo("EXTRA") == 0)
//		sequence = sequence + getSymbol((String) tags[k][1], null, 
//		    "EXTRA");
		;
	    else if (((String) tags[k][0]).compareTo("TEXT") == 0)
		sequence = sequence + getSymbol((String) tags[k][1], null, 
		    "TEXT");
	    else if (((String) tags[k][0]).compareTo("ELEMENT") == 0) {
               	track    = tagNode[k];
		position = ((Integer) tags[k][3]).intValue();
      
               	if (track.getParentNode() == xml_node) {
               	    xml = track;
		    tag = xml.getNodeName();

		    if (position != 0)
			tag = tag + "[" + position + "]";

		    sequence = sequence + getSymbol(tag, xml, "ELEMENT");
               	}
               	else {
              	    prev = null;

               	    while ((track != xml_node) && (track != null)) {
                        prev  = track;
                        track = track.getParentNode();

			if (tag.compareTo("") == 0) {
			    tag = prev.getNodeName();

			    if (position != 0)
			        tag = tag + "[" + position + "]";
			}
			else
			    tag = prev.getNodeName() + "/" + tag;
               	    }
        
                    if (track == xml_node) {
		       	xml = prev;
		      	sequence = sequence + getSymbol(tag, xml, "ELEMENT");
	    	    }
	        }
	    }
	    else if (((String) tags[k][0]).compareTo("ATTRIBUTE") == 0) {
		track = tagNode[k];
		position = ((Integer) tags[k][3]).intValue();

               	if (track == xml_node) {
                    xml = track;
                    tag = "@" + tags[k][1];
		    sequence = sequence + getSymbol(tag, xml, "ATTRIBUTE");
                }
                else {
                    prev = null;

                    while ((track != xml_node) && (track != null)) {
                        prev  = track;
                        track = track.getParentNode();

			if (tag.compareTo("") == 0) {
			    if (position != 0) 
			    	tag = prev.getNodeName() + "[" + position + 
				      "]/@" + tags[k][1];
			    else
				tag = prev.getNodeName() + "/@" + tags[k][1];
			}
			else 
			    tag = prev.getNodeName() + "/" + tag;
                    }
            
                    if (track == xml_node) {                
                      	xml = prev;                 
           		sequence = sequence + getSymbol(tag, xml, "ATTRIBUTE"); 
               	    }
              	}          
	    }
	}

	return sequence;
    } 

    private String findChildrenUsingXML(Node xml_node, Node html_node) {
	int i, rule = -1;
	String sequence = "", tag;
	Node xml = null, track, prev;

        Node[][] textNode = xg.textmap.get_ntable();
        int noTextNodes = xg.textmap.curr_nrow;
    
        SubStringElement[] subString = xg.submap.get_stable();
        int noSubs = xg.submap.curr_srow;
        
        Node[][] structNode = xg.structmap.get_ntable();
        int noStructNodes = xg.structmap.curr_nrow;
        
        Node[][] attrNode = xg.attrmap.get_otable();
        int noAttrNodes = xg.attrmap.curr_orow;

	for (i = 0; i < noTextNodes; i++) {
            if (textNode[i][1] == html_node) {
		track = textNode[i][0];
		tag   = "";

		if (track.getParentNode() == xml_node) {
		    rule = xg.textmap.search_rule(track.getNodeName(),
			   html_node.getNodeName());
	
		    if (rule != -1) {
	    		if (xg.textmap.maptable[rule][4].compareTo("1") != 0) {
			    xml = track;
			    tag = xml.getNodeName();
			    sequence = sequence + getSymbol(tag, xml, 
				       "ELEMENT");
	    		}
	    		else
			    rule = -1;
		    }
		}
	    }
	}

	if (rule == -1) {
	    for (i = 0; i < noStructNodes; i++) {
		if (structNode[i][1] == html_node) {
		    track = structNode[i][0];
		    tag   = "";

		    if (track.getParentNode() == xml_node) {
		        rule = xg.structmap.search_rule(track.getNodeName(),
			       html_node.getNodeName());
	
		    	if (rule != -1) {
	    		    if (xg.structmap.maptable[rule][4].compareTo("1") 
				!= 0) {
			        xml = track;
			        tag = xml.getNodeName();
			        sequence = sequence + getSymbol(tag, xml, 
				           "ELEMENT");
	    		    }
	    		    else
			        rule = -1;
			}
		    }
		}
	    }
	}

	if (rule == -1) {
	    for (i = 0; i < noAttrNodes; i++) {
		if (attrNode[i][2] == html_node) {
		    track = attrNode[i][0];
		    tag   = "";

		    if (track == xml_node) {
			rule = xg.attrmap.search_rule(track.getNodeName(),
			       attrNode[i][1].getNodeName(), 
			       html_node.getNodeName());

			if (rule != -1) {
			    if (xg.attrmap.maptable[rule][5].compareTo("1")
				!= 0) {
				xml = track;
				tag = attrNode[i][1].getNodeName();
				sequence = sequence + getSymbol(tag, xml,
					   "ATTRIBUTE");
			    }
			    else
				rule = -1;
			}
		    }
		}
	    }
	}

	if (rule == -1) {
	    for (i = 0; i < noSubs; i++) {
		if (subString[i].html == html_node) 
		    sequence = sequence + getSubStringTags(i, xml_node);
	    }
	}

	return sequence;
    }
} 
