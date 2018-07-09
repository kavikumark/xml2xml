package com.le.xslt.gen;
import org.w3c.dom.*;

public class AttrMapTable
{
    private int MColumns = 6;
    public String[][] maptable;
    public int curr_mrow = 0;

    private int OColumns = 3;
    public Node[][] ownertable;
    public int curr_orow = 0;

    public AttrMapTable(int No_Tags) {
        int Rows   = No_Tags + 5000;
    
        maptable   = new String[Rows][MColumns];
        ownertable = new Node[Rows][OColumns]; 
    }

    /* 
    ** insert_rule:
    **     This function inserts a new rule specified by the input parameters,
    **     if the rule has not existed in Attribute maptable.
    */
    public void insert_rule(String owner_name, String attr_tag, 
                            String html_tag, String html_opentag, 
                            String html_closetag, String flag) {
        if (search_rule(owner_name, attr_tag, html_tag) == -1) {
            maptable[curr_mrow][0] = owner_name;
            maptable[curr_mrow][1] = attr_tag;
            maptable[curr_mrow][2] = html_tag;
            maptable[curr_mrow][3] = html_opentag;
            maptable[curr_mrow][4] = html_closetag;
            maptable[curr_mrow][5] = flag;
            curr_mrow++;
        }
    }
    
    public void unset_rule(int index) {
    	if (maptable[index][5].compareTo("0") == 0)
    	    maptable[index][5] = "2";
    }
    
    public void updateTag(int rule, String before, String after) {
    	maptable[rule][3] = maptable[rule][3] + before;
    	maptable[rule][4] = after + maptable[rule][4];
    	maptable[rule][5] = "0";
    }

    /* 
    ** insert_matchnode:
    **     This function inserts new matching nodes specified by the input 
    **     parameters, if the nodes has not existed in the Attribute ownertable.
    */
    public void insert_matchnode(Node owner, Node attr, Node html) {
        if (search_matchnode(owner, attr, html) == 0) {
            ownertable[curr_orow][0] = owner;
            ownertable[curr_orow][1] = attr;
            ownertable[curr_orow][2] = html;
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
    ** get_otable:
    **     Return the ownertable which contains the mappings between xml node,
    **     attribute node and html node.
    */
    public Node[][] get_otable() {
        return ownertable;
    }

    /*
    ** print_table:
    **     Print the contents of the maptable, ownertable, and substring table 
    **     (optional).
    */
    public void print_table() {
        int i;

        /* To print the attribute map rules. */
        for (i = 0; i < curr_mrow; i++)
            System.out.println(maptable[i][5] + " : " + maptable[i][1] + " (" + 
                maptable[i][0] + ") -> " + maptable[i][2] + " :: " + 
                maptable[i][3] + " ... " + maptable[i][4]);

        /* To print the owner table. 
        System.out.println("");
        for (i = 0; i < curr_orow; i++)
            System.out.println(ownertable[i][0] + " : " + 
                ownertable[i][1].getNodeName() + " - " + 
                ownertable[i][2]);
        */
    }

    /*
    ** search_rule:
    **     This function searches the pair (xml_tag, attr_tag) in maptable and
    **     return the index of its location in the maptable, or -1 if it is not
    **     found.
    */
    public int search_rule(String owner_name, String attr_tag) {
        for (int i = 0; i < curr_mrow; i++)
            if ((maptable[i][0].compareTo(owner_name) == 0) &&
                (maptable[i][1].compareTo(attr_tag) == 0))
                return i;

        return -1;
    }

    /*
    ** search_rule:
    **     This function searches the pair (xml_tag, attr_tag, html_tag) in 
    **     maptable and return the index of its location in the maptable, or -1
    **     if it is not found.
    */
    public int search_rule(String owner_name, String attr_tag, 
                           String html_tag) {
        for (int i = 0; i < curr_mrow; i++)
            if ((maptable[i][0].compareTo(owner_name) == 0) &&
                (maptable[i][1].compareTo(attr_tag) == 0) &&
                (maptable[i][2].compareTo(html_tag) == 0))
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
    ** search_rule:
    **     This function searches the pair (xml_tag, html_tag) in maptable and
    **     return the index of its location in the maptable, or -1 if it is not
    **     found.
    */
    public int search_XHrule(String owner_name, String html_tag) {
        for (int i = 0; i < curr_mrow; i++)
            if ((maptable[i][0].compareTo(owner_name) == 0) &&
                (maptable[i][2].compareTo(html_tag) == 0))
                return i;

        return -1;
    }    
    
    /*
    ** search_matchnode:
    **     This function searches the pair (xml, attr, html) nodes in the 
    **     ownertable and return 1 if it is found, or 0 otherwise.
    */
    private int search_matchnode(Node owner, Node attr, Node html) {
        for (int i = 0; i < curr_orow; i++)
            if ((ownertable[i][0] == owner) && 
                (ownertable[i][1] == attr) &&
                (ownertable[i][2] == html))
                return 1;
    
        return 0;
    }

    /* 
    ** search_xmlmatch:
    **     This function searches the corresponding xml node of the input html
    **     node in the ownertable. If it is not found, it returns null.
    */
    public Node search_xmlmatch(Node html) {
        int index = search_match(html);

        if (index != -1)
            return ownertable[index][0];

        return null;
    }

    /* 
    ** search_match:
    **     This function searches the corresponding xml node of the input html
    **     node only in the ownertable. It returns the location of the match or
    **     -1 otherwise.
    */
    public int search_match(Node html) {
        for (int i = 0; i < curr_orow; i++)
            if (ownertable[i][2] == html)
                return i;

        return -1;
    }

    /* 
    ** search_htmlmatch:
    **     This function searches the corresponding html node of the input xml
    **     node in the ownertable. If it is not found, it returns null.
    */
    public Node search_htmlmatch(Node xml) {
        for (int i = 0; i < curr_orow; i++)
	    if (ownertable[i][0] == xml)
                return ownertable[i][2];

        return null;
    }
}
