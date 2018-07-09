package com.le.xslt.gen;
import org.w3c.dom.Node;

public class SubStringTable
{
    private XSLTGen xg;
    
    public SubStringElement[] substring;
    public int curr_srow = 0;

    private int Rows;

    public SubStringTable(XSLTGen xg, int No_Tags) {
        this.xg = xg;
        Rows = No_Tags + 50;

        substring  = new SubStringElement[Rows];
    }

    /* 
    ** insert_sub:
    **     This function inserts the pair (xml, html) under the substring table.
    */
    public boolean insert_sub(Node xml, Node html) {
        boolean add = false;
        
        if ((xg.textmap.search_xmlmatch(html) == null) &&
            (xg.attrmap.search_xmlmatch(html) == null)) {
            add = true;       
            int index = search_sub(html);
                 	
            if (search_htmlsub(xml) == -1) {            
            	if (index == -1) {
                    substring[curr_srow] = new SubStringElement(xg, Rows, html, 
                    	xml, true);
                    curr_srow++;
            	}
            	else
            	    if (substring[index].searchSameContent(xml) == 0)
                	substring[index].add_xml(xml, true);
            }
            else {
            	if (index == -1) {
            	    substring[curr_srow] = new SubStringElement(xg, Rows, html,
            		xml, false);
            	    curr_srow++;
            	}
            	else
            	    substring[index].add_xml(xml, false);
            }
        }
    
        return add;
    }

    /* 
    ** insert_sub:
    **     This function inserts the pair (xml, html) under the substring table.
    */
    public boolean insert_sub(Node owner, Node xml, Node html) {
        boolean add = false;
        
        if ((xg.textmap.search_xmlmatch(html) == null) &&
            (xg.attrmap.search_xmlmatch(html) == null)) {
           	add = true;
                    
           	int index = search_sub(html);

           	if (index == -1) {
           	    substring[curr_srow] = new SubStringElement(xg, Rows, html,
                   	xml, owner, false);
               	    curr_srow++;
           	}
           	else
               	    substring[index].add_xml(xml, owner, false);
        }
    
        return add;
    }

    /*
    ** get_stable:
    **     Return the substring table which contains an array of the mappings
    **     between a html node and its corresponding xml nodes that build the
    **     string in the html node.
    */
    public SubStringElement[] get_stable() {
        return substring;
    }

    /*
    ** print_table:
    **     Print the contents of the maptable, nodetable, and substring table 
    **     (optional).
    */
    public void print_table() {
        int i;

        /* To print the sub match table */
        for (i = 0; i < curr_srow; i++) {
            System.out.println(substring[i].html);
            for (int j = 0; j < substring[i].no_xml; j++) {
            	System.out.print("\t" + substring[i].family[j] + " : ");
                if (substring[i].xmls[j][0].getNodeType() ==
                    Node.ATTRIBUTE_NODE)
                    System.out.print(substring[i].xmls[j][0].getNodeName());
                else
                    System.out.print(substring[i].xmls[j][0]);
            
                System.out.println(" ** " + substring[i].xmls[j][1]);
            }
            System.out.println();
        }
    }

    /* 
    ** search_sub:
    **     This function searches the entry for html node in substring table and
    **     return the index if it is found, or 0 otherwise.
    */
    private int search_sub(Node html) {
        for (int i = 0; i < curr_srow; i++) {
            if (substring[i].html == html)
                return i;
        }
    
        return -1;
    }

    /* 
    ** search_sub:
    **     This function searches the entry for html node in substring table and
    **     return the index if it is found, or 0 otherwise.
    */
    public int search_sub(String html) {
        for (int i = 0; i < curr_srow; i++) {
            if (substring[i].html.getNodeName().compareTo(html) == 0)
                return i;
        }
    
        return -1;
    }

    private int search_htmlsub(Node xml) {
        for (int i = 0; i < curr_srow; i++) {
            if (substring[i].search_xml(xml) == 1) 
                return i;
        }
    
        return -1;
    }

    /* 
    ** search_xmlmatch:
    **     This function searches the corresponding xml node of the input html
    **     node in the substring table. If it is not found, it returns null.
    */
    public Node search_xmlmatch(Node html) {
        int index = search_sub(html);

        if (index != -1) 
            return substring[index].xmls[0][0];

        return null;
    }

    /* 
    ** search_htmlmatch:
    **     This function searches the corresponding html node of the input xml
    **     node name in substring table. If it is not found, it returns null.
    */
    public Node search_htmlmatch(Node xml) {
        int index = search_htmlsub(xml);

        if (index != -1) 
            return substring[index].html;

        return null;
    }
    
    public int findSubStringTags(String html, String xml) {
    	int i, matchAt = -1;
    	Object[][] tags = null;
    	
    	/* Find the first substring element that matches the rule xml, html. */
    	for (i = 0; i < curr_srow; i++) {
    	    if ((substring[i].html.getNodeName().compareTo(html) == 0) && 
    		(substring[i].checkTags(xml))) {
    		tags    = substring[i].tags;
    		matchAt = i;
    		break;
    	    }
    	}

	/* 
	** Assume that the rest substring element that matches the rule xml
	** and html has the same "tags".
	*/   
/*
	boolean valid = true;
			
    	for (i = matchAt + 1; i < curr_srow; i++) {
 	    if ((substring[i].html.getNodeName().compareTo(html) == 0) &&
    		(substring[i].checkTags(xml))) {
    		if (substring[i].tags != tags)
    		    valid = false;
    	    }
    	}
    	
    	if (valid == false)
    	    System.out.println("Different Tags");
*/
    	
    	return matchAt;
    }
}
