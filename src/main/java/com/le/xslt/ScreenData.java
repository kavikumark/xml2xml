package com.le.xslt;

import java.util.ArrayList;
import java.util.List;

import com.le.xslt.controller.TagFormatRule;
import com.le.xslt.util.XmlNodeTag;

public class ScreenData {
	private String inputFilePath;
	private List<String> paths = new ArrayList<>();
	private String xml;
	private String xsl;
	private XmlNodeTag tree;
	private List<TagFormatRule> rules;
	private boolean isModified;
	private String transformedXml;

	public String getInputFilePath() {
		return inputFilePath;
	}
	public void setInputFilePath(String inputFilePath) {
		this.inputFilePath = inputFilePath;
	}
	public List<String> getPaths() {
		return paths;
	}
	public void setPaths(List<String> paths) {
		this.paths = paths;
	}
	public String getXml() {
		return xml;
	}
	public void setXml(String xml) {
		this.xml = xml;
	}
	public String getXsl() {
		return xsl;
	}
	public void setXsl(String xsl) {
		this.xsl = xsl;
	}
	public XmlNodeTag getTree() {
		return tree;
	}
	public void setTree(XmlNodeTag tree) {
		this.tree = tree;
	}
	public boolean isModified() {
		return isModified;
	}

	public void setModified(boolean isModified) {
		this.isModified = isModified;
	}
	public List<TagFormatRule> getRules() {
		return rules;
	}
	public void setRules(List<TagFormatRule> rules) {
		this.rules = rules;
	}
	public TagFormatRule getFormatRule(String tagPath, String att) {
		if(rules!=null) {
			for(TagFormatRule rule:rules) {
				if(rule.getTagPath().getValue().equals(tagPath) && (att==null || rule.getAttributes().getValue().equals(att))) {
					return rule;
				}
			}
		}
		return null;
	}
	public String getTransformedXml() {
		return transformedXml;
	}
	public void setTransformedXml(String transformedXml) {
		this.transformedXml = transformedXml;
	}
}
