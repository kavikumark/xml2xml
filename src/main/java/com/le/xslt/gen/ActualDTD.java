package com.le.xslt.gen;
import org.w3c.dom.*;
import java.util.regex.*;

public class ActualDTD extends XTRACT 
{
    public ActualDTD(int column, boolean ROOT, XSLTGen xg, String elemName) {
	super(column, ROOT, xg, elemName);

	findAllSequences();
	new_sym = symtable.size();

	generalize();
	factorSubsets();

	dtd = mdl();
	imp_dtd = getImportantPart();
    }

    public void findAllSequences() {
        Node[][] nodetable = xg.structmap.get_ntable();
        int noNodes = xg.structmap.curr_nrow;

        Node node;
        String seq;
        
        for (int i = 0; i < noNodes; i++) {
            if (nodetable[i][column].getNodeName().compareTo(elemName) == 0) {
                if (nodetable[i][0] == xg.xml_root)
                    node = nodetable[i][column];
                else {
                    node = nodetable[i][column].getParentNode();
                    
                    if (node == xg.xml_root)
                        checkRoot = true;
                }
                
                dtdElem = node.getNodeName();
                    
                seq = generateSequence(node);
                    
                if (sequence.indexOf(seq) == -1)
                    sequence.add(seq);             
            }
        }
    }

    private String generateSequence(Node node) {
        NodeList children;
        int childCount;
        String sequence = "";
        
        children = node.getChildNodes();
        childCount = children.getLength();
        for (int i = 0; i < childCount; i++)
            if ((children.item(i).getNodeType() == Node.ELEMENT_NODE) ||
                ((children.item(i).getNodeType() == Node.TEXT_NODE) &&
                 (children.item(i).getNodeValue().trim().compareTo("") != 0)))
                sequence = sequence + getSymbol(children.item(i).getNodeName(),
                    children.item(i));
        
        return sequence;
    }
}
