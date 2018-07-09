package com.le.xslt.gen;
import org.w3c.dom.Node;

public class CheckSequence
{
    private XSLTGen xg;
    
    public SequenceTable[] seqtable;
    public int curr_seqtable = 0;
    
    public static final int XML  = 0;
    public static final int HTML = 1;
    
    public static final int TEXT_TABLE   = 0;
    public static final int ATTR_TABLE   = 1;
    public static final int STRUCT_TABLE = 2;
    
    public CheckSequence(XSLTGen xg, int noXMLTags) {
        this.xg = xg;
        
        seqtable = new SequenceTable[noXMLTags];
        
        doCheckSequence();
    }
    
    private void doCheckSequence() {
        Node html_node;
        XTRACT xml_dtd, html_dtd;
        
        String[][] maptable = xg.structmap.get_mtable();
        int noRules = xg.structmap.curr_mrow;
        
        boolean checkRoot = false;
        boolean ROOT = false;   
        
        for (int i = 0; i < noRules; i++) {         
            if ((maptable[i][0].compareTo(xg.xml_root.getNodeName()) == 0) &&
                (checkRoot == true))
                ;
            else if (maptable[i][4].compareTo("3") != 0) {    
                /* 
                ** Search the actual html match in the nodetable since the rule
                ** stores the lowest child's name if each node from actual html
                ** to the rule html has only 1 child.
                */
                html_node = xg.structmap.search_htmlmatch(maptable[i][0]);

                if (maptable[i][0].compareTo(xg.xml_root.getNodeName()) == 0) 
                    ROOT = true;
  
                xml_dtd  = new ActualDTD(XML, ROOT, xg, maptable[i][0]);
                if (xml_dtd.checkRoot == true)
                    checkRoot = true;
            
                html_dtd = new ActualDTD(HTML, ROOT, xg, 
		    html_node.getNodeName());


/*              System.out.println(maptable[i][0] + " - " + 
                    html_node.getNodeName() + " : " + xml_dtd.dtd + " - " + 
                    html_dtd.dtd);
                System.out.println(xml_dtd.imp_dtd + " - " + html_dtd.imp_dtd);
*/

                String xml  = xml_dtd.imp_dtd,
                       html = html_dtd.imp_dtd,
                       xml_tag, html_tag;
        
                /* 
                ** Assume that if the xml dtd has '*' at the end, the html dtd
                ** also have '*' at the end.
                */
                if ((xml.endsWith("*") == true) && (html.endsWith("*") == true))
                {
                    xml  = getRepeatedTag(xml);
                    html = getRepeatedTag(html);
            
//                  System.out.println(xml + " - " + html);
                }

                /* 
                ** Assume that we only handle the case where the xml has only 1
                ** tag in the sequence, e.g. "a", or when it has '|' in it,
                ** e.g. "a | b | ...".
                */

                String[] xmls;
        
                if (xml.indexOf('|') != -1) {
                    xml = xml.replace('|', ':');
                    xmls = xml.split(":");
                }
                else {
                    xmls = new String[1];
                    xmls[0] = xml;
                }
        
        
                for (int j = 0; j < xmls.length; j++) {
                    xml = xmls[j];

                    /*
                    ** If the xml and html sequences have the same length, i.e.
                    ** they both have length of 1, then they match properly.
                    */
                    if (xml.length() == html.length())
                        ;
        
                    /* 
                    ** Else, when the html has length > 1, the structure mapping
                    ** needs to be modified. 
                    */
                    else if (xml.length() == 1) {              
                        xml_tag = (String) xml_dtd.symtable.get(xml.charAt(0) - 
                            xml_dtd.init_char);
                            
                        int html_length = findNumHtml(html_dtd, html);

                        if (html_length > 1 && html.indexOf("|") == -1) {
                            seqtable[curr_seqtable] = new SequenceTable(xml_tag,
                                html_length);
            
                            for (int k = 0; k < html.length(); k++) {
				if ((html.charAt(k) == '|') || 
				    (html.charAt(k) == '(') ||
				    (html.charAt(k) == ')') ||
				    (html.charAt(k) == '*'))
				    continue;

                                html_tag = (String) html_dtd.symtable.get(
				    html.charAt(k) - html_dtd.init_char);
                                
                                if ((html_tag.compareToIgnoreCase("br") == 0) ||
                                    (html_tag.compareToIgnoreCase("hr") == 0))
                                    continue;
                                    
                                /* 
                                ** If the html_tag is the same as html_node node
                                ** name, use the rule html specified in 
                                ** maptable[i][1] instead of the actual html.
                                */                  
                                if (html_tag.compareTo(html_node.getNodeName()) 
                                    == 0) {
                                    seqtable[curr_seqtable].addRule(
                                        STRUCT_TABLE, i);
                                    xg.structmap.unset_rule(i);      
                                }               
                                else {                                  
                                    int rule = -1;
                                    Node xml_node = null;
    
                                    /* 
                                    ** Find exact match of xml_tag -> html_tag 
                                    ** in Text, Attribute and Structure Map     
                                    ** Table.
                                    */      
                                    rule = xg.textmap.search_rule(xml_tag,
                                        html_tag);
                                    if (rule != -1) {
                                        seqtable[curr_seqtable].addRule(
                                            TEXT_TABLE, rule);
                                        xg.textmap.unset_rule(rule);
                                
                                        continue;
                                    }
                                    else 
                                        rule = xg.attrmap.search_XHrule(xml_tag,
                                            html_tag);
                    
                                    if (rule != -1) {
                                        seqtable[curr_seqtable].addRule(
                                            ATTR_TABLE, rule);
                                        xg.attrmap.unset_rule(rule);
                                
                                        continue;
                                    }
                                    else        
                                        rule = xg.structmap.search_rule(xml_tag,
                                            html_tag);
                    
                                    if (rule != -1) {
                                        seqtable[curr_seqtable].addRule(
                                            STRUCT_TABLE, rule);
                                        xg.structmap.unset_rule(rule);
                                    
                                        continue;
                                    }
                            
                                    /*
                                    ** If there is no exact match of xml_tag -> 
                                    ** html_tag in any of the rule map tables, 
                                    ** search the html node corresponding to 
                                    ** html_tag, in Text, Attr, and Struct node 
                                    ** table then get the matching xml node and 
                                    ** find the rule. For this research, it is 
                                    ** assumed that only one rule maps an xml to
                                    ** this html_tag.
                                    */                  
                                    else {
                                        xml_node = xg.textmap.search_xmlmatch(
                                            html_dtd.symnode[html.charAt(k) - 
                                            html_dtd.init_char]);
                                        
                                        if (xml_node != null) {
                                            rule = xg.textmap.search_rule(
                                                xml_node.getNodeName(), 
                                                html_tag);
                            
                                            seqtable[curr_seqtable].addRule(
                                                TEXT_TABLE, rule);
                                            xg.textmap.unset_rule(rule);
                                
                                            continue;
                                        }
                                        else
                                            xml_node = xg.attrmap.
                                                search_xmlmatch(
                                                html_dtd.symnode[html.charAt(k)-
                                                html_dtd.init_char]);
                                    
                                        if (xml_node != null) {
                                            rule = xg.attrmap.search_XHrule(
                                                xml_node.getNodeName(), 
                                                html_tag);
                            
                                            seqtable[curr_seqtable].addRule(
                                                ATTR_TABLE, rule);
                                            xg.attrmap.unset_rule(rule);
                                
                                            continue;
                                        }
                                        else
                                            xml_node = xg.structmap.
                                                search_xmlmatch(
                                                html_dtd.symnode[html.charAt(k)-
                                                html_dtd.init_char]);
                                    
                                        if (xml_node != null) {
                                            rule = xg.structmap.search_rule(
                                                xml_node.getNodeName(), 
                                                html_tag);
                            
                                            seqtable[curr_seqtable].addRule(
                                                STRUCT_TABLE, rule);
                                            xg.structmap.unset_rule(rule);
                                
                                            continue;
                                        }
                                        else
                                            xml_node = xg.submap.
                                                search_xmlmatch(
                                                html_dtd.symnode[html.charAt(k)-   
                                                html_dtd.init_char]);
        
                                        if (xml_node != null) {
                                            if (xml_node.getNodeType() ==
                                                Node.ATTRIBUTE_NODE) {
                                                rule = xg.attrmap.search_XHrule(
                                                    xml_node.getNodeName(), 
                                                    html_tag);
                            
                                                seqtable[curr_seqtable].addRule(
                                                    ATTR_TABLE, rule);
                                                xg.attrmap.unset_rule(rule);
                                            }
                                            else {
                                                rule = xg.textmap.search_rule(
                                                    xml_node.getNodeName(), 
                                                    html_tag);
                            
                                                seqtable[curr_seqtable].addRule(
                                                    TEXT_TABLE, rule);
                                                xg.textmap.unset_rule(rule);
                                            }
                        
                                            continue;
                                        }
                                    }       
                                }
                            }       
                
                            curr_seqtable++;
                        }
                    }
                }
            }
        }
    }
    
    private int findNumHtml(XTRACT html_dtd, String html) {
        int len = 0;
        String html_tag;
        
        for (int k = 0; k < html.length(); k++) {
	    if ((html.charAt(k) != '|') && (html.charAt(k) != '*') && 
		(html.charAt(k) != '(') && (html.charAt(k) != ')')) {
//		System.out.println(html.charAt(k));
                html_tag = (String) html_dtd.symtable.get(html.charAt(k) - 'a');
            
                if ((html_tag.compareToIgnoreCase("br") != 0) &&
                    (html_tag.compareToIgnoreCase("hr") != 0))
                    len++;
	    }
        }
        
        return len;
    }
    
    /*
    ** getRepeatedTag():
    **     This function removes the metacharacters from the dtd, and return
    **     the repeated tag inside ( )*.
    */
    private String getRepeatedTag(String dtd) {
        if (dtd.endsWith("*") == true)
            dtd = dtd.substring(0, dtd.length() - 1);
        
        if ((dtd.startsWith("(") == true) && (dtd.endsWith(")") == true))
            dtd = dtd.substring(1, dtd.length() - 1);
        
        return dtd;
    }       
    
    public void print_seqtable() {
        for (int i = 0; i < curr_seqtable; i++)     
            seqtable[i].print_seq();
    }
    
    public SequenceTable[] get_seqtable() {
        return seqtable;
    }
}
