package com.le.xslt.gen;
import org.w3c.dom.*;

public class TextMatching 
{
    private XSLTGen xg;
    
    private Node xml_root, html_root, html_body;

    private int j;
    private NodeList childlist;

    private static final int EXACT  = 1;
    private static final int SUBSTR = 2;

    public TextMatching(XSLTGen xg, Node xml_root, Node html_root) {
        this.xg = xg;
    
        this.xml_root  = xml_root;
        this.html_root = html_root;

        /* Find the html "body" node. */
        childlist = html_root.getChildNodes();
        for (j = 0; j < childlist.getLength(); j++) {
            if (childlist.item(j).getNodeName().compareToIgnoreCase("body") 
                == 0) {
                html_body = childlist.item(j);
                break;
            }
        }
        
        if (html_body == null)
            html_body = html_root;
    
        doTextMatch(EXACT, xml_root, html_body); 
        doTextMatch(SUBSTR, xml_root, html_body);
    }

    /*
    ** buildOpenTag:
    **     This functions create the opening tag for 'node' and takes into 
    **     account its attributes.
    */
    public String buildOpenTag(Node node) {
        if ((node == null) || (node.getNodeType() != Node.ELEMENT_NODE))
            return "";
            
        String s = new String("<" + node.getNodeName());
        int i, attrCount;
        NamedNodeMap attributes;
    
        if (node.hasAttributes()) {
            attributes = node.getAttributes();
            attrCount = attributes.getLength();
            for (i = 0; i < attrCount; i++) {
                s += (" " + attributes.item(i).getNodeName() + "=\"" +
                    attributes.item(i).getNodeValue() + "\"");
            }
        }

        s += ">";
    
        return s;
    }

    /* 
    ** buildCloseTag:
    **     This functions create the closing tag for 'node'.
    */
    public String buildCloseTag(Node node) {
        if ((node == null) || (node.getNodeType() != Node.ELEMENT_NODE))
            return "";
        else                    
            return (new String("</" + node.getNodeName() + ">"));
    }

    /*
    ** doTextMatch:
    **     This function tries to match the text in the xml document with those
    **     in the html document using top-down approach.
    */
    private void doTextMatch(int ExactOrSub, Node xml_node, Node html_node) {
        int i, childCount, attrCount;
        NodeList children;
        NamedNodeMap attributes;
    
        /* Find the matching html node for non-empty xml text nodes. */
        if ((xml_node.getNodeType() == Node.TEXT_NODE) &&
            (xml_node.getNodeValue().trim().compareTo("") != 0))
            doTextSearch(ExactOrSub, null, xml_node, html_node);
    
        /* Do Text Matching for all xml node children (if any). */
        if (xml_node.hasChildNodes()) {
            children = xml_node.getChildNodes();
            childCount = children.getLength();
            for (i = 0; i < childCount; i++) 
                doTextMatch(ExactOrSub, children.item(i), html_node);
        }
    
        /* Do Text Matching for all xml node attributes (if any). */
        if (xml_node.hasAttributes()) {
            attributes = xml_node.getAttributes();
            attrCount = attributes.getLength();
            for (i = 0; i < attrCount; i++) 
                doTextSearch(ExactOrSub, xml_node, attributes.item(i), 
                    html_node);
        }
    }

    /*
    ** doTextSearch:
    **     This function finds the match of xml_node in the html document 
    **     starting with html_node.
    **     If the xml_node is of type TEXT_NODE, the owner_node is null,
    **     if the xml_node is of type ATTRIBUTE_NODE, the owner_node contains
    **     xml node that owns the attribute xml_node.
    */
    private void doTextSearch(int ExactOrSub, Node owner_node, Node xml_node, 
                              Node html_node) {
        int i, childCount;
        NodeList children;
        
        /* 
        ** If the html_node is of type TEXT_NODE, then check whether it matches
        ** the text in xml_node.
        ** There are two types of match:
        **     EXACT match     : if the html text exactly matches the xml text.
        **     SUBString match : if the xml text is a substring of the html 
        **           text.
        */
        if (html_node.getNodeType() == Node.TEXT_NODE) {
            String h = html_node.getNodeValue().trim();
            String x = xml_node.getNodeValue().trim();
    
            if ((ExactOrSub == EXACT) && (x.compareTo(h) == 0))
                doTextSearchAlg(EXACT, owner_node, xml_node, html_node);
            else if ((ExactOrSub == SUBSTR) && (isSubstr(x, h) == 1))
                doTextSearchAlg(SUBSTR, owner_node, xml_node, html_node);
        }
    
        /* 
        ** Check whether the nodes in the subtree of html_node can match the
        ** xml_node.
        */
        if (html_node.hasChildNodes()) {
            children = html_node.getChildNodes();
            childCount = children.getLength();
            for (i = 0; i < childCount; i++)
                doTextSearch(ExactOrSub, owner_node, xml_node, 
                    children.item(i));
        }
    }

    /* 
    ** doTextSearchAlg:
    **     This function creates the rule for the mapping between xml_node and
    **     html_node, and store the matching nodes in the nodetable.
    */
    private void doTextSearchAlg(int ExactOrSub, Node owner_node, Node xml_node,
                                 Node html_node) {
        String xml_tag, html_tag, html_opentag, html_closetag;
    
        int i, childCount, childElement;
    
        Node next_html, check_html, html_final;
        NodeList children;
    
        /* 
        ** Variable for indicating whether or not the resulting rule needs to
        ** be added to the rule table.
        ** If special_xml = 1, it does not need to be added.
        */
        int special_xml = 0;
    
        /* 
        ** If the xml_node is of type TEXT_NODE, then get its parent's name as
        ** the tag.
        */
        if (xml_node.getNodeType() == Node.TEXT_NODE) {
            childElement = 0;
            children     = xml_node.getParentNode().getChildNodes();
            childCount   = children.getLength();
            for (i = 0; i < childCount; i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE)
                    childElement++;
            }
            
            /* 
            ** If the xml text node has non-zero ELEMENT_NODE siblings, set
            ** the special_xml variable to 1. 
            */ 
            if (childElement != 0)
                special_xml = 1;
    
            xml_tag = xml_node.getParentNode().getNodeName();
        }
        
        /* If the xml_node is an ATTRIBUTE_NODE, get its name as the tag. */
        else
            xml_tag = xml_node.getNodeName();
    
        /* 
        ** next_html variable is used to check whether the next sibling is a
        ** <br/>.
        */
        next_html = html_node.getNextSibling();
    
        /* 
        ** If the next_html node is a <br/> or <hr/>, map the xml_node to the 
        ** <br/> or <hr/> in the rule, but leave the html_node as the mapping in
        ** the nodetable.
        */
        if ((next_html != null) && 
            (next_html.getNodeType() == Node.ELEMENT_NODE) && 
            ((next_html.getNodeName().compareToIgnoreCase("br") == 0) ||
             (next_html.getNodeName().compareToIgnoreCase("hr") == 0))) {
            html_tag      = html_node.getNodeName();
            html_opentag  = "";
            html_closetag = "<" + next_html.getNodeName() + "/>";
    
            html_final = html_node;
        }   
        
        /* 
        ** If the next_html node is not a <br/> or <hr/>, then build the html 
        ** tag name, open and close tags while considering whether or not the 
        ** html_node is the only child.
        */
        else {
            check_html = html_node.getParentNode();
    
            /* Count the number of ELEMENT_NODE siblings of html_node. */
            childElement = 0;
            children     = check_html.getChildNodes();
            childCount   = children.getLength();
            for (i = 0; i < childCount; i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE)
                    childElement++;
            }
    
            /* 
            ** If the number of ELEMENT_NODE siblings is not zero, then use
            ** html_node name as the html_tag, which in this case will be
            ** "#text" since the html_node is a TEXT_NODE, and set the open
            ** close tags to empty strings.
            */
            if (childElement != 0) {
                html_tag      = html_node.getNodeName(); // html_tag = "#text"
                html_opentag  = new String("");
                html_closetag = new String("");
                html_final    = html_node;
            }
    
            /* 
            ** If the number of ELEMENT_NODE sibling is zero, then get the 
            ** highest possible html node in the html_document where each node 
            ** in the subtree will only have one child. The stop node for
            ** checking will be the html "body" node, since it is not possible
            ** for xml nodes to match html node above html "body".
            ** The tags will be appended each time the xml node is matched to
            ** higher level html node.
            */
            else {
                html_tag     = check_html.getNodeName();
                html_opentag = buildOpenTag(check_html);
                html_closetag= buildCloseTag(check_html);
                html_final   = check_html;
    
                if (check_html != html_body) {
                    check_html = check_html.getParentNode();
    
                    while (check_html != html_body) {
                        if (check_html.hasChildNodes()) {
    
                            /* Count the number of ELEMENT_NODE children. */
                            childElement = 0;
                            children     = check_html.getChildNodes();
                            childCount   = children.getLength();
                            for (i = 0; i < childCount; i++) {
                                if (children.item(i).getNodeType() == 
                                    Node.ELEMENT_NODE)
                                    childElement++;
                            }
    
                            /* 
                            ** If the number of ELEMENT_NODE child is only 1,   
                            ** append the new open and close tags to the 
                            ** previous one, and set the html_final to the  
                            ** new node.
                            */
                            if (childElement == 1) {
                                html_opentag = buildOpenTag(check_html) +
                                    html_opentag;
                                html_closetag = html_closetag + 
                                    buildCloseTag(check_html);
    
                                html_final = check_html;
                                check_html = check_html.getParentNode();
                            }
    
                            /* 
                            ** Once the number of ELEMENT_NODE child is not 1,
                            ** stop checking.
                            */
                            else
                                break;
                        }
                        else
                            break;
                    }
                }
            }
        }
    
        /* 
        ** the new rule and matching nodes to text maptable and 
        ** nodetable, respectively.
        */
        if (xml_node.getNodeType() == Node.TEXT_NODE) {
            Node add_xml;
    
            /* 
            ** In the case when it is not needed to add the specified rule,
            ** add the xml text node and its html match to nodetable.
            */
            if (special_xml == 1)
                add_xml = xml_node;
            else 
                add_xml = xml_node.getParentNode();
    
            if (ExactOrSub == EXACT) {
                xg.textmap.insert_matchnode(add_xml, html_final); 
                
                if (special_xml != 1)
                    xg.textmap.insert_rule(xml_tag, html_tag, html_opentag, 
                        html_closetag, "0");
            }
            else if (ExactOrSub == SUBSTR) {
                if (xg.submap.insert_sub(add_xml, html_final)) {
                    if (special_xml != 1)
                        xg.textmap.insert_rule(xml_tag, html_tag, html_opentag,
                            html_closetag, "1");
                }
            }
        }
    
        /* 
        ** Insert the new rule and matching nodes to attribute maptable and
        ** nodetable, respectively.
        */
        else {    
            if (ExactOrSub == EXACT) {
                xg.attrmap.insert_rule(owner_node.getNodeName(), xml_tag, 
                    html_tag, html_opentag, html_closetag, "0");         
                xg.attrmap.insert_matchnode(owner_node, xml_node, html_final);
            }
            else if (ExactOrSub == SUBSTR) {
                if (xg.submap.insert_sub(owner_node, xml_node, html_final))
                    xg.attrmap.insert_rule(owner_node.getNodeName(), xml_tag,
                        html_tag, html_opentag, html_closetag, "1");
            }
        }
    }

    /* 
    ** isSubstr:
    **     This function checks whether string s1 is a substring of string s2.
    **     s1 is a substring of s2, if the character before and after the
    **     occurrence of s1 in s2 are non letters and non digits.
    */
    private int isSubstr(String s1, String s2) {
        int len1, len2, i;

        /* 
        ** Trim the two strings to be compared, so that there are no spaces
        ** at the beginning of each new line.
        */
        s1   = trimString(s1);
        s2   = trimString(s2);

        len1 = s1.length();
        len2 = s2.length();
    
        for (i = 0; (i < len2) && ((i + len1) <= len2); i++) {
            if (s2.substring(i, i + len1).compareTo(s1) == 0) { 
                if ((i > 0) && ((i + len1) < len2)) {           
                    if (((Character.isLetterOrDigit(s2.charAt(i - 1)) == false) 
                         || ((Character.isLetterOrDigit(s2.charAt(i - 1)) ==    
                              true) && (Character.isLetterOrDigit(s2.charAt(i))
                              == false)))
                        && ((Character.isLetterOrDigit(s2.charAt(i + len1)) == 
                             false) || ((Character.isLetterOrDigit(s2.charAt(
                             i + len1)) == true) && (Character.isLetterOrDigit(
                             s2.charAt(i + len1 - 1)) == false))))
                        return 1;
                }
                else if ((i == 0) && (len1 < len2)) {
                    if ((Character.isLetterOrDigit(s2.charAt(len1)) == false) ||
                        ((Character.isLetterOrDigit(s2.charAt(len1)) == true) &&
                         (Character.isLetterOrDigit(s2.charAt(len1 - 1)) == 
                          false)))
                        return 1;
                }
                else if ((i > 0) && ((i + len1) == len2)) {
                    if ((Character.isLetterOrDigit(s2.charAt(i - 1)) == false)
                        || ((Character.isLetterOrDigit(s2.charAt(i - 1)) == 
                             true) && (Character.isLetterOrDigit(s2.charAt(i))
                             == false)))
                        return 1;
                }
            }
        }

        return 0;
    }

    /*
    ** trimString:
    **     This function removes the spaces at the beginning of each new line in
    **     the string s.
    */
    public String trimString(String s) {
        int len, i, newline = 0;
        String s_trim = "";

        s   = s.trim();
        len = s.length();

        for (i = 0; i < len; i++) {
            if (s.charAt(i) == '\n') {
                newline = 1;
                s_trim = s_trim + s.charAt(i);
            }
            else if ((s.charAt(i) == ' ') && (newline == 1))
                ;
            else if ((s.charAt(i) == ' ') && (newline == 0))
                s_trim = s_trim + s.charAt(i);
            else {
                newline = 0;
                s_trim = s_trim + s.charAt(i);
            }
        }

        return s_trim;
    }
}
