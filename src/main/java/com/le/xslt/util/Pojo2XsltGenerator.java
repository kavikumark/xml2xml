package com.le.xslt.util;

import java.util.StringTokenizer;

import com.le.xslt.ScreenData;
import com.le.xslt.controller.TagFormatRule;

public class Pojo2XsltGenerator {

	public static String getXslt(ScreenData sd) {
		XmlNodeTag tree = sd.getTree();
		StringBuffer result = new StringBuffer("");
//		result.append("<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"2.0\">\n");
		result.append("<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"\r\n" + 
				"    xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\r\n" + 
				"    xmlns:xtt=\"urn:com.workday/xtt\"\r\n" + 
				"    xmlns:etv=\"urn:com.workday/etv\"\r\n" + 
				"    xmlns:ws=\"urn:com.workday/workersync\"\r\n" + 
				"    xmlns:saxon=\"http://saxon.sf.net/\" extension-element-prefixes=\"saxon\"\r\n" + 
				"    exclude-result-prefixes=\"xs ws xtt saxon\"\r\n" + 
				"    version=\"3.0\">\n");
		result.append("<xsl:output method=\"xml\" encoding=\"utf-8\" indent=\"yes\"/>\n");
		result.append("");
		getTemplate(tree, result, sd);
		result.append("</xsl:stylesheet>\n");
		return result.toString();
	}

	private static void getTemplate(XmlNodeTag node, StringBuffer result, ScreenData sd) {
		result.append("<xsl:template match=\""+node.getTagName()+"\">\n");
		result.append("<").append(node.getTagName()).append(">\n");
		for(String att :node.getTagAttributes()) {
			result.append("<xsl:attribute name=\""+att+"\">");
			TagFormatRule rule = sd.getFormatRule(node.getTagPath(),att);
			String str = getRule(rule,"@"+att);
			result.append(str);
			result.append("</xsl:attribute>\n");
		}
		for(XmlNodeTag child:node.getChildren()) {
			result.append("<xsl:apply-templates select=\""+child.getTagName()+"\"/>\n");
		}
		if(node.getChildren().size()==0) {
			TagFormatRule rule = sd.getFormatRule(node.getTagPath(),null);
			String str = getRule(rule,".");
			result.append(str);
		}
		result.append("</").append(node.getTagName()).append(">\n");
		result.append("</xsl:template>\n");
		for(XmlNodeTag child:node.getChildren()) {
			getTemplate(child,result, sd);
		}
	}

	private static String getRule(TagFormatRule rule, String field) {
		StringBuffer result = new StringBuffer("");
		if(rule!=null
				&& rule.getTextField1().getText()!=null  
				&& rule.getTextField1().getText().trim().length()>0
				&& rule.getTextField2().getText()!=null  
				&& rule.getTextField2().getText().trim().length()>0
				) {
			if(rule.getTextField1().getText().equals("*")) {
				if(rule.getTextField2().getText().equalsIgnoreCase("lower")) {
					result.append("<xsl:value-of select=\"lower-case("+field+")\"/>\n");
				}
				if(rule.getTextField2().getText().equalsIgnoreCase("upper")) {
					result.append("<xsl:value-of select=\"upper-case("+field+")\"/>\n");
				}
			} else {
				String existingFormat = rule.getTextField1().getText();
				String newFormat = rule.getTextField2().getText();
				if(existingFormat.contains("dd") || existingFormat.contains("yy")) {
					String formattedString = getFormattedString(existingFormat,newFormat,field);
					result.append("<xsl:value-of select=\""+formattedString+"\"/>\n");
				}
			}
		} else {
			result.append("<xsl:value-of select=\""+field+"\"/>\n");
		}
		return result.toString();
	}
	
	private static String getFormattedString(String f1, String f2, String src) {
		StringTokenizer newTokens = new StringTokenizer(f2, "-/:,",true);
		StringBuffer result = new StringBuffer("concat(");
		while(newTokens.hasMoreTokens()) {
			String field = newTokens.nextToken();
			int fieldBegin = getIndex(f1, field, true);
			int fieldEnd = getIndex(f1, field, false);
			result.append("substring("+src+","+fieldBegin+","+fieldEnd+")");
			if(newTokens.hasMoreTokens()) {
				result.append(",'"+newTokens.nextToken()+"',");
			}
		}
		result.append(")");
			System.out.println(result.toString());
			return result.toString();
	}

	private static int getIndex(String f1, String field, boolean b) {
		if(b) return f1.indexOf(field) + 1;
		else return field.length();
	}
}
