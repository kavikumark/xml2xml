package com.le.xslt.gen;
import org.w3c.dom.*;

public class MoreXMLTags
{
    private XSLTGen xg;
    private Node xml_root;
    
    public Object[][] tagtable;
    public int curr_trow = 0;
    
    public Node[] nodetable;
    public int curr_nrow = 0;

    public MoreXMLTags(XSLTGen xg, int No_Tags, Node xml_root) {
        this.xg  = xg;
        this.xml_root = xml_root;
        
        tagtable  = new Object[No_Tags][2];
        nodetable = new Node[No_Tags];
        
        searchXMLTag(xml_root);
    }
    
    /* 
    ** searchXMLTag:
    **     This function searches the XML Tags that has not had a rule from both
    **     Text Matching and Structure Matching.
    */
    private void searchXMLTag(Node xml_node) {
        /* Check the tag of ELEMENT_NODE only. */
        if (xml_node.getNodeType() == Node.ELEMENT_NODE) {
            String xml_tag = xml_node.getNodeName();
            
            if ((xg.textmap.search_rule(xml_tag) == -1) &&
                (xg.structmap.search_rule(xml_tag) == -1)) {
                insert_tag(xml_tag);
                insert_node(xml_node);
            }
        }
        
        /* Continue the search to xml_node's children (if any). */    
        NodeList children;
        int childCount;
        
        if (xml_node.hasChildNodes()) {
            children = xml_node.getChildNodes();
            childCount = children.getLength();
            for (int i = 0; i < childCount; i++)        
            searchXMLTag(children.item(i));
        }
    }
    
    /* 
    ** insert_tag:
    **     This function inserts a new tag, if the tag has not existed in 
    **     tagtable.
    */
    private void insert_tag(String xml_tag) {
        if (search_tag(xml_tag) == -1) {
            tagtable[curr_trow][0] = new Integer(0);
            tagtable[curr_trow][1] = new String(xml_tag);
            curr_trow++;
        }
    }

    public void set_print(String xml_tag) {
        int setIndex = search_tag(xml_tag);
    
        if (setIndex != -1)
            tagtable[setIndex][0] = new Integer(1);
    }

    public void updateMoreTags(Node start, Node finish) {
        while ((start != finish) && (start != null)) {
            set_print(start.getNodeName());
            start = start.getParentNode();
        }
    
        if (start == finish)
            set_print(start.getNodeName());
    }

    /* 
    ** insert_node:
    **     This function inserts a new node, if the node has not existed in 
    **     nodetable.
    */    
    private void insert_node(Node xml) {
        if (search_node(xml) == 0)
            nodetable[curr_nrow++] = xml;
    }
    
    /*
    ** search_tag:
    **     This function searches xml_tag in tagtable. It returns 1 if it is
    **     found, or 0 otherwise.
    */
    private int search_tag(String xml_tag) {
        for (int i = 0; i < curr_trow; i++)
            if (((String) tagtable[i][1]).compareTo(xml_tag) == 0)
                return i;
        
        return -1;
    }

    /*
    ** search_outtag:
    **     This function searches xml_tag in tagtable. It returns 1 if it is
    **     found, or 0 otherwise.
    */
    public int search_outtag(String xml_tag) {
        for (int i = 0; i < curr_trow; i++)
            if ((((String) tagtable[i][1]).compareTo(xml_tag) == 0) &&
                (tagtable[i][0].equals(new Integer(1)) == true))
                return i;
        
        return -1;
    }

    /*
    ** search_node:
    **     This function searches xml in nodetable. It returns 1 if it is
    **     found, or 0 otherwise.
    */
    public int search_node(Node xml) {
        for (int i = 0; i < curr_nrow; i++)
            if (nodetable[i] == xml)
                return 1;
        
        return 0;
    }
        
    /* 
    ** get_tagtable:
    **     Returns tagtable which contains all XML tags that has not had a rule
    **     from both Text Matching and Structure Matching.
    */
    public Object[][] get_tagtable() {
        return tagtable;
    }
    
    /* 
    ** get_ntable:
    **     Returns nodetable which contains all XML nodes that has not had a
    **     rule from both Text Matching and Structure Matching.
    */
    public Node[] get_ntable() {
        return nodetable;
    }
    
    /*
    ** print_table:
    **     Print the contents of the tagtable and nodetable (optional).
    */
    public void print_table() {
        int i;
        
        /* To print the xml tags. */
        for (i = 0; i < curr_trow; i++)
            System.out.println(tagtable[i][1]);
    
        /* To print the node table. 
        System.out.println("");
        for (i = 0; i < curr_nrow; i++) 
            System.out.println(nodetable[i]);
        */      
    }
}
