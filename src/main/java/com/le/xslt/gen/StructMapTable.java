package com.le.xslt.gen;
import org.w3c.dom.*;

public class StructMapTable
{
    private XSLTGen xg;
    
    private int MColumns = 5;
    public String[][] maptable;
    public int curr_mrow = 0;

    private int NColumns = 2;
    public Node[][] nodetable;
    public int curr_nrow = 0;

    private int OColumns = 2;
    public Node[][] origtable;
    public int curr_orow = 0;

    public StructMapTable(XSLTGen xg, int No_Tags) {
        this.xg = xg;
        
        int Rows  = No_Tags + 1000;
        
        maptable  = new String[Rows][MColumns]; 
        nodetable = new Node[Rows][NColumns];
        origtable = new Node[Rows][OColumns];
    }

    /* 
    ** insert_rule:
    **     This function inserts a new rule specified by the input parameters,
    **     if there's no rule for xml_tag in Text maptable, and if the rule has
    **     not existed in the Struct maptable.
    */
    public void insert_rule(String xml_tag, String html_tag, 
			    String html_opentag, String html_closetag) {
        if ((xg.textmap.search_rule(xml_tag) == -1) && 
            (search_rule(xml_tag, html_tag) == -1)) {
            maptable[curr_mrow][0] = xml_tag;
            maptable[curr_mrow][1] = html_tag;
            maptable[curr_mrow][2] = html_opentag;
            maptable[curr_mrow][3] = html_closetag;
            maptable[curr_mrow][4] = "0";
            curr_mrow++;
        }
    }

    public void insert_rule(String xml_tag, String html_tag, 
			    String html_opentag, String html_closetag, 
			    String flag) {
        if ((xg.textmap.search_rule(xml_tag) == -1) && 
            (search_rule(xml_tag, html_tag) == -1)) {
            maptable[curr_mrow][0] = xml_tag;
            maptable[curr_mrow][1] = html_tag;
            maptable[curr_mrow][2] = html_opentag;
            maptable[curr_mrow][3] = html_closetag;
            maptable[curr_mrow][4] = flag;
            curr_mrow++;
        }
    }

    public void unset_rule(int index) {
    	if (maptable[index][4].compareTo("0") == 0)
    	    maptable[index][4] = "2";
    }

    public void updateTag(int rule, String before, String after) {
    	maptable[rule][2] = maptable[rule][2] + before;
    	maptable[rule][3] = after + maptable[rule][3];
    	maptable[rule][4] = "0";
    }

    /*
    ** replace_rule:
    **     This function replaces the html tag name and appends the html open
    **     tag and close tag for nodetable[index] using the input parameters.
    **     This function is used only when the html_node has exactly one 
    **     ELEMENT_NODE child.
    */
    public void replace_rule(int index, String xml_tag, String html_tag, 
                             String html_opentag, String html_closetag) {
        String xml, html;
        int replace, index_exist;
            
        /* 
        ** Check whether the rule for nodes in nodetable[index] exists in the
        ** maptable
        */
        xml = nodetable[index][0].getNodeName();
        if (nodetable[index][1] == null)
            html = "";
        else
            html = nodetable[index][1].getNodeName();
    
        replace = search_rule(xml, html);
    
    
        /* If the rule for nodetable[index] is not found, insert new rule */
        if (replace == -1) 
            insert_rule(xml_tag, html_tag, html_opentag, html_closetag);
            
        /* Else, replace the rule in maptable[replace] */
        else {
            /* 
            ** If the new html tag equals to "body" then do not replace it, i.e.
            ** simply return. 
            */
            if (html_tag.compareToIgnoreCase("body") == 0)
                return;
    
            /* 
            ** Replace the rule only when the html open and close tags do not
            ** start and end with the new open and close tags.
            */
            if ((maptable[replace][2].startsWith(html_opentag) == false) &&
                (maptable[replace][3].endsWith(html_closetag) == false)) {
                
                /* 
                ** Append the new open and close tags without changing the html 
                ** tag.
                */
                maptable[replace][2] = html_opentag + maptable[replace][2];
                maptable[replace][3] = maptable[replace][3] + html_closetag;
            }
        }
    }

    /*
    ** change_rule:
    **     This function changes the html tag name and html open and close tags
    **     for nodetable[index] using the input parameters.
    **     This function is used only when the html_node has more than one 
    **     ELEMENT_NODE children.
    */
    public void change_rule(int index, String xml_tag, String html_tag, 
                            String html_opentag, String html_closetag) {
        String xml, html;
        int replace;

        /* 
        ** Check whether the rule for nodes in nodetable[index] exists in the
        ** maptable
        */
        xml = nodetable[index][0].getNodeName();
        if (nodetable[index][1] == null)
            html = "";
        else
            html = nodetable[index][1].getNodeName();

        replace = search_rule(xml, html);
    
        /* If the rule for nodetable[index] is not found, insert new rule */
        if (replace == -1) 
            insert_rule(xml_tag, html_tag, html_opentag, html_closetag);
            
        /* Else, change the rule only if the new rule does not exist */
        else {
            int index_exist = search_rule(xml_tag, html_tag);
    
            /* 
            ** If the rule to be replaced is the same as the new rule,
            ** then do not change anything, simply return.
            */
            if (index_exist == replace) 
                return;             
    
            /* 
            ** If the new rule does not exist, insert the new html tag and its
            ** open and close tags.
            */                          
            else if (index_exist == -1) {
                maptable[replace][1] = new String(html_tag);
                maptable[replace][2] = new String(html_opentag);
                maptable[replace][3] = new String(html_closetag);
            }
            
            /* If the new rule exists, delete maptable[replace]. */
            else {
                for (; replace < curr_mrow-1; replace++) {
                    maptable[replace][0] = maptable[replace+1][0];
                    maptable[replace][1] = maptable[replace+1][1];
                    maptable[replace][2] = maptable[replace+1][2];
                    maptable[replace][3] = maptable[replace+1][3];
                }
                curr_mrow--;
            }
        }
    }

    /* 
    ** insert_matchnode:
    **     This function inserts new matching nodes specified by the input 
    **     parameters, if there's no rule for xml_tag in Text maptable, and if 
    **     the nodes has not existed in the Struct nodetable.
    */
    public void insert_matchnode(Node xml, Node html) {
        if ((xg.textmap.search_rule(xml.getNodeName()) == -1) && 
            (search_matchnode(xml, html) == 0)) {
            nodetable[curr_nrow][0] = xml;
            nodetable[curr_nrow][1] = html;
            curr_nrow++;
        }
    }

    /*
    ** replace matchnode:
    **     This function replaces the html node of nodetable[index] by the
    **     new html node from the input argument, if there exists a node with
    **     the same xml_node in the nodetable.
    **     If the index equals to -2, insert a new entry with xml_node from 
    **     input parameter and null as the html_node. This is to indicate that
    **     the html node associated with the xml node has already been assigned
    **     to different xml node.
    */
    public void replace_matchnode(int index, Node xml, Node html) {
        
        /* If there is an entry that needs to be replaced. */
        if (index != -2) {
        
            /* 
            ** If the new html node equals to "body" and the html node that is
            ** going to be replace is not null, then do not replace it, i.e.
            ** simply return. This only happens when the xml node is the 
            ** xml root. This is done so because we want the xml root to match
            ** the furthest element from the html root.
            */      
            if ((html.getNodeName().compareToIgnoreCase("body") == 0) && 
                (nodetable[index][1] != null))
                return;
    
            /* 
            ** If the html node to be replaced is not null, back up the row
            ** to orignode table.
            */
            if (nodetable[index][1] != null)
                insert_orignode(nodetable[index][0], nodetable[index][1]);
    
            nodetable[index][1] = html;
        }
        
        /* 
        ** If the index equals to -2, insert a new entry (xml, null) to 
        ** nodetable, which indicate that the html node associated with this xml
        ** has been assigned to other xml node.
        */
        else {
            if (xg.textmap.search_rule(xml.getNodeName()) == -1)
                insert_matchnode(xml, null);
        }
    }


    /* 
    ** insert_orignode:
    **     This function inserts the input parameters: xml and html, to 
    **     orignode, only if there is no entry with the same xml node in the 
    **     table.
    */
    public void insert_orignode(Node xml, Node html) {
        if (search_orignode(xml) == 0) {
            origtable[curr_orow][0] = xml;
            origtable[curr_orow][1] = html;
            curr_orow++;
        }
    }

    /* 
    ** get_mtable:
    **     Return the maptable which contains the rules.
    */
    public String[][] get_mtable() {
        return maptable;
    }

    /*
    ** get_ntable:
    **     Return the nodetable which contains the mappings between xml node
    **     and html node.
    */
    public Node[][] get_ntable() {
        return nodetable;
    }

    /*
    ** get_otable:
    **     Return the origtable which contains the mappings that have been
    **     removed from the nodetable.
    */
    public Node[][] get_otable() {
        return origtable;
    }

    /*
    ** print_table:
    **     Print the contents of the maptable and nodetable (optional).
    */
    public void print_table() {
        int i;
    
        /* To print the struct map rules. */
        for (i = 0; i < curr_mrow; i++)
            System.out.println(maptable[i][4] + " : " + maptable[i][0] + " -> " 
                + maptable[i][1] + " :: " + maptable[i][2] + " ... " + 
                maptable[i][3]);
    
        /* To print the node match table. 
        System.out.println("");
        for (i = 0; i < curr_nrow; i++) 
            System.out.println(nodetable[i][0] + " - " + nodetable[i][1]);
        */
    }

    /*
    ** search_rule:
    **     This function searches the pair (xml_tag, html_tag) in maptable and
    **     return the index of its location in the maptable, or -1 if it is not
    **     found
    */
    public int search_rule(String xml_tag, String html_tag) {
        for (int i = 0; i < curr_mrow; i++) 
            if ((maptable[i][0].compareTo(xml_tag) == 0) &&
                (maptable[i][1].compareTo(html_tag) == 0))
                return i;
        
        return -1;
    }

    /*
    ** search_rule:
    **     This function searches the rule for xml_tag in maptable. It returns 
    **     the location of the rule in the maptable, or -1 if it is not found.
    */
    public int search_rule(String xml_tag) {
        for (int i = 0; i < curr_mrow; i++)
            if (maptable[i][0].compareTo(xml_tag) == 0)
                return i;
        
        return -1;
    }
    
    /*
    ** search_matchnode:
    **     This function searches the pair (xml, html) nodes in the nodetable
    **     and return 1 if it is found, or 0 otherwise.
    */
    private int search_matchnode(Node xml, Node html) {
        for (int i = 0; i < curr_nrow; i++)
            if ((nodetable[i][0] == xml) && (nodetable[i][1] == html))
                return 1;
     
        return 0;
    }
    
    /*
    ** search_orignode:
    **     This function searches the entry of 'xml' in the orignode table. 
    **     It returns 1 if the node is found, or 0 otherwise.
    */
    private int search_orignode(Node xml) {
        for (int i = 0; i < curr_orow; i++)
            if (origtable[i][0] == xml)
                return 1;
    
        return 0;
    }

    /* 
    ** search_xmlmatch:
    **     This function searches the corresponding xml node of the input html
    **     node in the nodetable. If it is not found, it returns null.
    */
    public Node search_xmlmatch(Node html) {
        for (int i = 0; i < curr_nrow; i++)
            if (nodetable[i][1] == html)
                return nodetable[i][0];
    
        return null;
    }   
    
    /* 
    ** search_htmlmatch:
    **     This function searches the corresponding html node of the input xml
    **     node name in the nodetable. If it is not found, it returns null.
    */
    public Node search_htmlmatch(String xml) {
        for (int i = 0; i < curr_nrow; i++)
            if (nodetable[i][0].getNodeName().compareTo(xml) == 0)
                return nodetable[i][1];
    
        return null;
    }       

    /* 
    ** search_htmlmatch:
    **     This function searches the corresponding html node of the input xml
    **     node name in the nodetable. If it is not found, it returns null.
    */
    public Node search_htmlmatch(Node xml) {
        for (int i = 0; i < curr_nrow; i++)
            if (nodetable[i][0] == xml)
                return nodetable[i][1];
    
        return null;
    }       
    
    /*
    ** search_supernode:
    **     This function searches the pair (xml, html) in the nodetable, and it
    **     returns some numbers depend on the characteristic of the nodes it
    **     found:
    **     -3    : If it found a pair in the nodetable that exactly matches
    **         (xml, html)
    **     -2    : If it found a pair with same html node but the xml node is 
    **         the ancestor of the input xml node
    **     -1    : If it cannot found any match
    **     other : If it found a pair with same xml node but different html node
    **         and if the search_xmlmatch(html) returns either the input xml
    **         or null
    */
    public int search_supernode(Node xml, Node html) {
        Node test;
    
        for (int i = 0; i < curr_nrow; i++) {
            if ((xml == nodetable[i][0]) && (html == nodetable[i][1]))
                return -3;
            else if ((xml == nodetable[i][0]) && (html != nodetable[i][1])) {
                test = search_xmlmatch(html);
    
                if ((test == xml) || (test == null))
                    return i;
            }
            else if ((xml != nodetable[i][0]) && (html == nodetable[i][1])) {
                test = nodetable[i][0];
    
                while ((test != xml) && (test != null))
                    test = test.getParentNode();
    
                if (test == xml)
                    return -2;
            }
        }
    
        return -1;
    }
}
