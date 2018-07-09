package com.le.xslt.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlNodeTag {
	private String tagName;
	private List<String> tagAttributes = new ArrayList<>();
	private String body;
	private List<XmlNodeTag> children = new ArrayList<>();
	private XmlNodeTag parent;
	private String tagPath;
	public String getTagName() {
		return tagName;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	public List<String> getTagAttributes() {
		return tagAttributes;
	}
	public void setTagAttributes(List<String> tagAttributes) {
		this.tagAttributes = tagAttributes;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public List<XmlNodeTag> getChildren() {
		return children;
	}
	public void setChildren(List<XmlNodeTag> children) {
		this.children = children;
	}
	public void addAttribute(String att) {
		tagAttributes.add(att);
	}
	public void addChild(XmlNodeTag child) {
//		child.setParent(this);
		children.add(child);
	}
	public XmlNodeTag getParent() {
		return parent;
	}
	public void setParent(XmlNodeTag parent) {
		this.parent = parent;
	}
	public String getPath() {
		if(parent==null) {
			return tagName;
		} else {
			return parent.getTagName()+"/"+tagName;
		}
	}
	public XmlNodeTag getRoot() {
		if(parent==null) {
			return this;
		} else {
			return parent.getRoot();
		}
	}
	public boolean isLeaf() {
		return getChildren().size()==0;
	}
	public boolean isRoot() {
		return getParent()==null;
	}
//	public String getParentTagPath() {
//		return parent.getTagPath();
//	}
//	
//	public XmlNodeTag getByParentTagPath(String parentTagPath) {
//		if(getParentTagPath().equals(parentTagPath)) {
//			return this;
//		} else {
//			return parent.getByParentTagPath(parentTagPath);
//		}
//	}
	
	public XmlNodeTag getNodeByTagPath(String tagPath) {
		XmlNodeTag result = null;
		if(getTagPath().equals(tagPath)) {
			result = this;
		} else {
			for(XmlNodeTag child:getChildren()) {
				XmlNodeTag childResult = child.getNodeByTagPath(tagPath);
				if(childResult!=null) {
					result = childResult;
				}
			}
		}
		return result;
	}
	
	public List<String> getAllNodePaths() {
		List<String> result = new ArrayList<>();
		result.add(getTagPath());
		for(XmlNodeTag child:children) {
			result.addAll(child.getAllNodePaths());
		}
		return result;
	}
	
	public XmlNodeTag getTree() {
		if(parent!=null) {
			XmlNodeTag child = new XmlNodeTag();
			child.setTagName(tagName);
			child.setBody(body);
			child.setParent(parent);
			child.setTagAttributes(tagAttributes);
			child.setTagPath(tagPath);
//			parent.getChildren().clear();
			parent.addChild(child);
			parent.getTree();
		}
		return getRoot();
	}
	
	public XmlNodeTag compact() {
		if(parent!=null) {
		Map<String,XmlNodeTag> treeNode = new HashMap<>();
		for(XmlNodeTag child : parent.getChildren()) {
//			if(!treeNode.containsKey(child.getTagPath())) {
				treeNode.put(child.getTagPath(), child);
//			}
		}
		parent.getChildren().clear();
		parent.getChildren().addAll(treeNode.values());
			parent.compact();
		}
		return getRoot();
	}
	public String getTagPath() {
		return tagPath;
	}
	public void setTagPath(String tagPath) {
		this.tagPath = tagPath;
	}
	@Override
	public String toString() {
		return "\nXmlNodeTag [tagName=" + tagName + ", tagAttributes=" + tagAttributes + ", body=" + body + ", children="
				+ children.size() + ", parent=" + parent + ", tagPath=" + tagPath + "]";
	}
	public String treeString() {
		String treeString = getTagName()+""+getTagAttributes().toString();
		if(getChildren().size()!=0) {
			for(XmlNodeTag child:getChildren()) {
				treeString+="\n\t"+child.treeString();
			}
		}
		return treeString;
	}
	public void merge(XmlNodeTag other) {
		for(XmlNodeTag otherChild:other.getChildren()) {
			boolean childExists = false;
			XmlNodeTag existingChild = null;
			for(XmlNodeTag child:getChildren()) {
				if(otherChild.getTagPath().equals(child.getTagPath())) {
					childExists = true;
					existingChild = child;
				}
			}
			if(!childExists) {
				addChild(otherChild);
			} else {
				existingChild.merge(otherChild);
			}
		}
	}
}
