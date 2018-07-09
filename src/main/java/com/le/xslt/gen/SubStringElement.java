package com.le.xslt.gen;
import org.w3c.dom.*;

public class SubStringElement
{
    private XSLTGen xg;
    
    Node       html;
    int        no_xml;
    Node[][]   xmls;
    int[]      strlen;
    boolean[]  family;
    boolean[]  avail;
    String     rule;
    Object[][] tags;
    Node[]     tagNode;
    int        noTags;

    public SubStringElement(XSLTGen xg, int rows, Node html, Node xml,
    			    boolean fam) {
        this.xg   = xg;
        this.html = html;
        no_xml    = 0;
        xmls      = new Node[rows][2];
        strlen	  = new int[rows];
        family 	  = new boolean[rows];
        avail     = new boolean[rows];        
        rule      = "";

        add_xml(xml, fam);
    }
    
    public SubStringElement(XSLTGen xg, int rows, Node html, Node xml, 
                            Node parent, boolean fam) {
        this.xg   = xg;
        this.html = html;
        no_xml    = 0;
        xmls      = new Node[rows][2];
        strlen	  = new int[rows];
        family    = new boolean[rows];
        avail     = new boolean[rows];
        rule      = "";

        add_xml(xml, parent, fam);
    }

    /* 
    ** add_xml:
    **     Add the pair (xml, null) if it does not exist.
    */
    public void add_xml(Node xml, boolean fam) {
        int xml_len, insert_index;

        if (search_xml(xml) == 0) {
            if (xml.getNodeType() == Node.ELEMENT_NODE)
                xml_len = xml.getFirstChild().getNodeValue().length();
            else
                xml_len = xml.getNodeValue().length();
                
            insertDescending(xml, null, xml_len, fam);
            no_xml++;
        }
    }

    /* 
    ** add_xml:
    **     Add the pair (xml, parent) if it does not exist. 
    */    
    public void add_xml(Node xml, Node parent, boolean fam) {
        int xml_len, insert_index;

        if (search_xml(xml, parent) == 0) {
            if (xml.getNodeType() == Node.ELEMENT_NODE)
                xml_len = xml.getFirstChild().getNodeValue().length();
            else
                xml_len = xml.getNodeValue().length();
            
            insertDescending(xml, parent, xml_len, fam);
            no_xml++;          
        }
    }
    
    /* 
    ** insertDescending:
    **     Sorted in descending order of string length. 
    */    
    private void insertDescending(Node xml, Node parent, int xml_len, 
    				  boolean fam) {
        int i;
        
        for (i = no_xml; (i > 0) && (xml_len > strlen[i - 1]); i--) {
            xmls[i][0] = xmls[i - 1][0];
            xmls[i][1] = xmls[i - 1][1];
            strlen[i]  = strlen[i - 1];
            family[i]  = family[i - 1];
            avail[i]   = avail[i - 1];
        }
        
        xmls[i][0] = xml;
        xmls[i][1] = parent;
        strlen[i]  = xml_len;
        family[i]  = fam;
        avail[i]   = true;
    }    
    
    public void set_family(int index) {
    	family[index] = true;
    }
    
    public void add_rule(String rule) {
        this.rule = rule;
    }
    
    public void add_tags(Object[][] tags, Node[] tagNode, int noTags) {
    	this.tags    = tags;
	this.tagNode = tagNode;
    	this.noTags  = noTags;
    }
    
    public Object[][] get_tags() {
    	return tags;
    }
    
    /* For element node only */
    public int searchSameContent(Node xml) {
    	if (xml.getNodeType() == Node.ELEMENT_NODE) {
    	    String tag   = xml.getNodeName();
    	
    	    while (xml.getNodeType() != Node.TEXT_NODE)
    		xml = xml.getFirstChild();
    					
    	    String value = xml.getNodeValue();
    		
    	    for (int i = 0; i < no_xml; i++) {
   		Node exist = xmls[i][0];
   		if ((exist.getNodeType() == Node.ELEMENT_NODE) &&
   		    (exist.getNodeName().compareTo(tag) == 0) &&
   		    (exist.getFirstChild().getNodeValue().compareTo(value) == 0)
   		    && (family[i] == true))
    		    return 1;
    	    }
    	}
    	
    	return 0;
    }

    public int search_xml(Node xml) {
        for (int i = 0; i < no_xml; i++) {
            if (xmls[i][0] == xml)
                return 1;
        }
    
        return 0;
    }

    private int search_xml(Node xml, Node parent) {
        for (int i = 0; i < no_xml; i++) {
            if ((xmls[i][0] == xml) && (xmls[i][1] == parent))
                return 1;
        }
    
        return 0;
    }
    
    public int findStartsWith(String html) {
        String xml;
        
        for (int i = 0; i < no_xml; i++) {
            if ((family[i] == true) && (avail[i] == true)) {
            	if (xmls[i][0].getNodeType() == Node.ELEMENT_NODE)
                    xml = xmls[i][0].getFirstChild().getNodeValue();
            	else
                    xml = xmls[i][0].getNodeValue();
            
            	xml = xg.textmatch.trimString(xml);

            	if (html.startsWith(xml)) {
            	    int subs = -1;
            		
            	    if (xmls[i][0].getNodeType() == Node.ATTRIBUTE_NODE)
            		subs = findSuitableAttrNode(i, xml);
            		
            	    if (subs != -1)
            		i = subs;
            			
            	    avail[i] = false;
                    return i;
                }
            }
        }
        
        return -1;
    }
    
    private int findSuitableAttrNode(int index, String xmlVal) {
        int i;
        boolean found_owner;
        Node xml    = xmls[index][0],
             parent = xmls[index][1];
        String xml_tag = xml.getNodeName();
        
        found_owner = hasParentInList(parent);
        
        if (found_owner)
            return -1;
        else {
            int newAttr = -1;
            
            for (i = 0; i < no_xml; i++) {
                if (i != index) {
                    if ((family[i] == true) && (avail[i] == true) &&
                        (xmls[i][0].getNodeType() == Node.ATTRIBUTE_NODE)) {
                        if ((xmls[i][0].getNodeName().compareTo(xml_tag) == 0)
			    &&
                            (xmls[i][0].getNodeValue().compareTo(xmlVal) == 0))
                           if (hasParentInList(xmls[i][1]))
                               newAttr = i;
                    }
                }
            }
            
            return newAttr;
        }
    }
                        
    private boolean hasParentInList(Node parent) {
        boolean found_owner = false;
            
        /* Find ELEMENT_NODE that has the same name as parent of index i. */
    	for (int i = 0; i < no_xml; i++) {
    	    if ((family[i] == true) && 
    	        (xmls[i][0].getNodeType() == Node.ELEMENT_NODE) &&
    	        (xmls[i][0] == parent))
    	        found_owner = true;
        }
        
        return found_owner;
    }    
    
    public int countNonExtraTags() {
    	int numExtra = 0, numNonExtra = 0;
    	
    	for (int i = 0; i < noTags; i++) {
    	    if (((String) tags[i][0]).compareTo("EXTRA") == 0)
    	        numExtra++;
    	    else
    	  	numNonExtra++;
    	}
    	
    	if (numExtra != 0)
    	    return numNonExtra;
    	else 
    	    return -1;
    }
    
    public int findNonExtraPos() {
    	int i;
    	
    	for (i = 0; i < noTags; i++) {
    	    if (((String) tags[i][0]).compareTo("EXTRA") != 0)
    	 	return i;
    	}
    	
    	return -1;
    }
    
    public boolean checkTags(String xml) {
    	int i;

    	for (i = 0; i < noTags; i++) {
    	    if (((((String) tags[i][0]).compareTo("ELEMENT") == 0) && 
    		 (((String) tags[i][1]).compareTo(xml) != 0)) ||
		((((String) tags[i][0]).compareTo("ATTRIBUTE") == 0) &&
    		 (((String) tags[i][2]).compareTo(xml) != 0)))
    		return false;
    	}
    	
    	return true;
    }
    
    public void printSubStringTags(String xml) {
    	for (int i = 0; i < noTags; i++) {
    	    if (((String) tags[i][0]).compareTo("EXTRA") == 0)
		System.out.print(tags[i][1]);
    	    else if (((String) tags[i][0]).compareTo("TEXT") == 0)
    		System.out.print("<xsl:value-of select=\"text()\"/>");
    	    else if (((String) tags[i][0]).compareTo("ELEMENT") == 0) {
    		int position   = ((Integer) tags[i][3]).intValue();
    		String xml_tag = (String) tags[i][1];
    			
    		if (position != 0)
    		    xml_tag = xml_tag + "[" + position + "]";
    			
   		if (((String) tags[i][1]).compareTo(xml) == 0)
                    System.out.print("<xsl:value-of select=\".\"/>"); 
               	else
                    System.out.print("<xsl:value-of select=\"" + xml_tag +
           		"\"/>"); 
            }  	
       	    else if (((String) tags[i][0]).compareTo("ATTRIBUTE") == 0) {
    		int position   = ((Integer) tags[i][3]).intValue();
    		String xml_tag = (String) tags[i][2];
    			
    		if (position != 0)
    		    xml_tag = xml_tag + "[" + position + "]";
    				
    		if (((String) tags[i][2]).compareTo(xml) == 0)    
                    System.out.print("<xsl:value-of select=\"@" + tags[i][1] +
                	"\"/>");  
                else
                    System.out.print("<xsl:value-of select=\"" + xml_tag +
                	"/@" + tags[i][1] + "\"/>");
            }
        }	            			 	
    }   
    
    public void updateTags(String type, String xml, int noBefore, int noAfter) {
    	int i, j, k, pos;
    	Object[][] newTags = tags;
    	
    	for (i = 0; i < noTags; i++) {
    	    if ((((String) tags[i][0]).compareTo(type) == 0) &&
    		(((String) tags[i][1]).compareTo(xml) == 0)) {
    		pos = i;
    			
    		for (j = 0, k = 0; j < (pos - noBefore); j++, k++)
    		    newTags[j] = tags[k];
    				
    		k = pos;
    		newTags[j++] = tags[k++];
    			
    		for (k = k + noAfter; k < noTags; j++, k++)
    		    newTags[j] = tags[k];
    			
    		noTags = noTags - noBefore - noAfter;
    		tags   = newTags;
    			
    		break;
    	    }
    	}
    }	 	
}
