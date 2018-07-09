package com.le.xslt.controller;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class TagFormatRule {
	private final ComboBox<String> tagPath;
	private final ComboBox<String> attributes;
	private final TextField textField1;
	private final TextField textField2;

	public TagFormatRule(ComboBox<String> tagPath, TextField textField1, TextField textField2,ComboBox<String> attributes) {
		super();
		this.tagPath = tagPath;
		this.textField1 = textField1;
		this.textField2 = textField2;
		this.attributes = attributes;
	}

	public TextField getTextField2() {
		return textField2;
	}

	public ComboBox<String> getAttributes() {
		return attributes;
	}

	public ComboBox<String> getTagPath() {
		return tagPath;
	}

	public TextField getTextField1() {
		return textField1;
	}
}