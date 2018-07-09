package com.le.xslt.gen;
import org.w3c.dom.*;

public class TextMapTable
{
    private int MColumns = 5;
    public String[][] maptable;
    public int curr_mrow = 0;

    private int NColumns = 2;      
    public Node[][] nodetable;
    public int curr_nrow = 0;

    public TextMapTable(int No_Tags) {
        int Rows  = No_Tags + 1000;
    
        maptable  = new String[Rows][MColumns]; 
        nodetable = new Node[Rows][NColumns];
    }

    /* 
    ** insert_rule:
    **     This function inserts a new rule specified by the input parameters,
    **     if the rule has not existed in Text maptable.
    */
    public void insert_rule(String xml_tag, String html_tag, 
                            String html_opentag, String html_closetag, 
                            String flag) {
        if (search_rule(xml_tag, html_tag) == -1) {
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

    /*
    ** replace_rule:
    **     This function append the String 'text' in the close tag of rule
    **     'index'.
    */
    public void replace_rule(int index, String text) {
        if (maptable[index][3].endsWith(text) == false)
            maptable[index][3] = maptable[index][3] + text;
    }

    public void updateTag(int rule, String before, String after) {
    	maptable[rule][2] = maptable[rule][2] + before;
    	maptable[rule][3] = after + maptable[rule][3];
    	maptable[rule][4] = "0";
    }

    /* 
    ** insert_matchnode:
    **     This function inserts new matching nodes specified by the input 
    **     parameters, if the nodes has not existed in the Text nodetable.
    */
    public void insert_matchnode(Node xml, Node html) {
        if (search_matchnode(xml, html) == 0) {
            nodetable[curr_nrow][0] = xml;
            nodetable[curr_nrow][1] = html;
            curr_nrow++;
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
    ** print_table:
    **     Print the contents of the maptable, nodetable, and substring table 
    **     (optional).
    */
    public void print_table() {
        int i;
    
        /* To print the text map rules. */
        for (i = 0; i < curr_mrow; i++)
            System.out.println(maptable[i][4] + " : " + maptable[i][0] + " -> " 
                + maptable[i][1] + " :: " + maptable[i][2] + " ... " + 
                maptable[i][3]);
    
        /* To print the node match table. 
        System.out.println();
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
    ** search_hrule:
    **     This function searches the rule for html_tag in maptable. It returns 
    **     the location of the rule in the maptable, or -1 if it is not found.
    */
    public int search_hrule(String html_tag) {
        for (int i = 0; i < curr_mrow; i++)
            if (maptable[i][1].compareTo(html_tag) == 0)
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
    public Node search_htmlmatch(Node xml) {
        for (int i = 0; i < curr_nrow; i++)
            if (nodetable[i][0] == xml)
                return nodetable[i][1];

        return null;
    }    
}
