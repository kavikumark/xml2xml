package com.le.xslt.gen;
import org.w3c.dom.Node;

public class XSLTGen 
{
    public BuildDom xmldom, htmldom;
    public Node xml_root, html_root;

    public SubStringTable submap;    
    public TextMapTable textmap;
    public AttrMapTable attrmap;
    public StructMapTable structmap;

    public TextMatching textmatch;
    public StructMatching structmatch;
    public CheckSequence checkSeq;
    
    public SubStringRule textSub;
    
    public MoreXMLTags xmltags;
    
    private String generatedXslt;
    
    public XSLTGen(String xmlfile, String htmlfile) {
        xmldom = new BuildDom(xmlfile);
        int noXMLTags = xmldom.get_NoTags();

        htmldom = new BuildDom(htmlfile);

        xml_root  = xmldom.doc.getDocumentElement();
        html_root = htmldom.doc.getDocumentElement();

        submap      = new SubStringTable(this, noXMLTags);
        textmap     = new TextMapTable(noXMLTags);
        attrmap     = new AttrMapTable(noXMLTags);
        structmap   = new StructMapTable(this, noXMLTags);

        textmatch   = new TextMatching(this, xml_root, html_root);
        structmatch = new StructMatching(this, xml_root, html_root);
        checkSeq    = new CheckSequence(this, noXMLTags);
    
        textSub     = new SubStringRule(this, submap.substring, 
            submap.curr_srow);

        xmltags     = new MoreXMLTags(this, noXMLTags, xml_root);

        /* Remove this comment to print the matching rules. 
        textmap.print_table();
        System.out.println();
        attrmap.print_table();
        System.out.println(); 
        submap.print_table();
        System.out.println();
        structmap.print_table();
        System.out.println();
        checkSeq.print_seqtable();
        System.out.println();
        xmltags.print_table();
        System.out.println();
	*/

        ConstructXSLT myXSLT = new ConstructXSLT(this, xml_root, html_root);
        generatedXslt = myXSLT.getXsltContent();
        System.out.println(generatedXslt);
    }   

    public static void main(String[] argv) {
/*        if (argv.length != 2) {
            usage();
            System.exit(1);
        }
    
        XSLTGen myXSLTGen = new XSLTGen(argv[0], argv[1]);*/
    	String fname = "poem.xml";
    	
        XSLTGen myXSLTGen = new XSLTGen("E:\\workspace\\git\\mini-projects\\xml2xslt\\src\\main\\resources\\"+fname, "E:\\workspace\\git\\mini-projects\\xml2xslt\\src\\main\\resources\\"+fname);
    }

    private static void usage() {
        System.out.println("\nUsage: java XSLTGen source_file " + 
        	"destination_file\n");
    }

	public String getGeneratedXslt() {
		return generatedXslt;
	}

}       
