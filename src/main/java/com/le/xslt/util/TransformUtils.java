package com.le.xslt.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.le.xslt.ScreenData;
import com.le.xslt.gen.XSLTGen;

public class TransformUtils {

	public static List<String> getPaths(String xmlFilePath) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
	    File file = new File(xmlFilePath);
	    XPath xPath =  XPathFactory.newInstance().newXPath();
	    String expression = "//*[not(*)]";

	    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = builderFactory.newDocumentBuilder();
	    Document document = builder.parse(file);
	    document.getDocumentElement().normalize();

	    NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
	    List<String> paths = new ArrayList<>();
	    for(int i = 0 ; i < nodeList.getLength(); i++) {
	    	String xp = getXPath(nodeList.item(i));
	    	if(!paths.contains(xp)) {
	    		paths.add(xp);
	    		System.out.println(xp);
	    	}
	    }
	    return paths;
	}

	private static String getXPath(Node node) {
	    Node parent = node.getParentNode();
	    if (parent.getParentNode() == null) {
	        return node.getNodeName()+getAttributes(node);
	    }
	    return getXPath(parent) + "/" + node.getNodeName()+getAttributes(node);
	}
	
//	private static String getAttributes(Node node) {
//		List<String> alist = new ArrayList<>();
//		NamedNodeMap attributes = node.getAttributes();
//		if(attributes!=null) {
//			for(int i=0;i<attributes.getLength();i++) {
//				alist.add(attributes.item(i).getNodeName());
//			}
//		}
//		if(alist.size()>0) {
//			return alist.toString();
//		} else {
//			return "";
//		}
//	}
	
	public static String getXslt(String xmlFilePath,String finalXmlFilePath) {
		XSLTGen myXSLTGen = new XSLTGen(xmlFilePath,finalXmlFilePath);
		return myXSLTGen.getGeneratedXslt();
//		if(formats==null || formats.size()==0) {
//			
//		}
	}
	
	public static ScreenData process(ScreenData sd) {
		
		try {
			if(!sd.isModified()) {
				sd.setXml(readFile(sd.getInputFilePath()));
			}
			
			XPath xPath =  XPathFactory.newInstance().newXPath();
			String expression = "//*[not(*)]";

			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(sd.getXml())));
			document.getDocumentElement().normalize();
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
			
			List<String> paths = new ArrayList<>();
			Map<String,XmlNodeTag> upmap = new TreeMap<>();
			for(int i = 0 ; i < nodeList.getLength(); i++) {
				XmlNodeTag xp = getXNode(nodeList.item(i));
				upmap.put(xp.getTagPath(), xp);
				if(!paths.contains(xp.getTagPath())) {
					paths.add(xp.getTagPath());
				}
			}
			
			sd.getPaths().clear();
//			sd.getPaths().addAll(paths);
			
			XmlNodeTag tree = unflat(upmap.values());
			sd.setTree(tree);
			String xslt = Pojo2XsltGenerator.getXslt(sd);
			sd.setXsl(xslt);
			sd.getPaths().addAll(sd.getTree().getRoot().getAllNodePaths());
			
			Source xsltSource = new StreamSource(new StringReader(sd.getXsl()));
			Source xmlSource = new StreamSource(new StringReader(sd.getXml()));
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer(xsltSource);
			StringWriter sw = new StringWriter();
			transformer.transform(xmlSource, new StreamResult(sw));
			sd.setTransformedXml(sw.toString());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sd;
	}
	
	private static String readFile(String filePath) {
		StringBuilder stringBuffer = new StringBuilder();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(filePath));
			String text;
			while ((text = bufferedReader.readLine()) != null) {
				stringBuffer.append(text).append("\n");
			}

		} catch (IOException ex) {
		} finally {
			try {
				bufferedReader.close();
			} catch (IOException ex) {
			}
		}
		return stringBuffer.toString();
	}


	private static XmlNodeTag unflat(Collection<XmlNodeTag> tags) {
		Map<String,XmlNodeTag> tmap = new TreeMap<>();
		XmlNodeTag tree = null;
		for(XmlNodeTag tag : tags) {
			XmlNodeTag xnt = getTree(tag).getRoot();
//			xnt.compact();
			if(tree==null) {
				tree = xnt;
			} else {
				tree.merge(xnt);
			}
		}
//		System.out.println(tree.treeString());
//		tree.compact();
		System.out.println(tree.treeString());
//		tree = compact(tree);
//		System.out.println(compact(tree).treeString());
		return tree;
	}
	
	private static XmlNodeTag getTree(XmlNodeTag leaf) {
		XmlNodeTag child = new XmlNodeTag();
		child.setBody(leaf.getBody());
		child.setTagName(leaf.getTagName());
		child.setTagAttributes(leaf.getTagAttributes());
		child.setTagPath(leaf.getTagPath());
		child.setBody(leaf.getBody());
		if(leaf.getParent()!=null) {
			XmlNodeTag parent = getTree(leaf.getParent());
			parent.addChild(child);
			child.setParent(parent);
		}
		return child;
	}
	

	private static XmlNodeTag getXNode(Node node) {
	    Node parent = node.getParentNode();
	    if (parent.getParentNode() == null) {
	    	XmlNodeTag xnt = new XmlNodeTag();
	    	if(node.getNodeType()==Node.TEXT_NODE) {
	    		xnt.setBody("");
	    	}
	    	xnt.setTagName(node.getNodeName());
	    	xnt.setTagPath(node.getNodeName());
	    	xnt.setTagAttributes(getAttributes(node));
	        return xnt;
	    }
	    XmlNodeTag xnt = new XmlNodeTag();
	    if(node.getNodeType()==Node.TEXT_NODE) {
	    	xnt.setBody("");
	    }
    	xnt.setTagPath(node.getNodeName());
    	xnt.setTagAttributes(getAttributes(node));
    	xnt.setTagName(node.getNodeName());
    	XmlNodeTag pxpath = getXNode(parent);
    	xnt.setParent(pxpath);
    	pxpath.addChild(xnt);
    	xnt.setTagPath(pxpath.getTagPath()+"/"+node.getNodeName());
	    return xnt;
	}
	
	private static List<String> getAttributes(Node node) {
		List<String> alist = new ArrayList<>();
		NamedNodeMap attributes = node.getAttributes();
		if(attributes!=null) {
			for(int i=0;i<attributes.getLength();i++) {
				alist.add(attributes.item(i).getNodeName());
			}
		}
		return alist;
	}
	
	public static void main(String[] args) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException {
		String path = "E:\\workspace\\git\\mini-projects\\xml2xslt\\src\\main\\resources\\";
/*		String fname = "chatlog.xml";
		ScreenData sd = new ScreenData();
		sd.setInputFilePath(path+fname);
		sd = process(sd);
		List<String> allNodePaths = sd.getTree().getRoot().getAllNodePaths();
		System.out.println(allNodePaths);
*/
		System.out.println(getXslt(path+"input.xml", path+"output.xml"));
	}
}
