package com.le.xslt.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.le.xslt.ScreenData;
import com.le.xslt.util.TransformUtils;
import com.le.xslt.util.XmlNodeTag;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

@FXMLController
public class MainController {

	@FXML
	private TextArea xmlText;
	
	@FXML
	private TextArea xslText;

	@FXML
	private TextArea transformedXML;
	
	@FXML
	private AnchorPane anchorPane;
	
	@FXML
	private AnchorPane dropDownAnchor;
	
	private String previousDirectory;
	
	private ScreenData sd;

	@FXML
	void close(ActionEvent event) {
		Stage stage = (Stage) xmlText.getScene().getWindow();
		stage.close();
	}
	
	@FXML
	void tree(ActionEvent event) {
//		XmlTreeViewUtil.show(this,stage,event);
	}
	
	@FXML
	void openXmlFile(ActionEvent event) {
		Node source = (Node) event.getSource();
		Window theStage = source.getScene().getWindow();
		FileChooser fileChooser = new FileChooser();
		if(previousDirectory==null) {
			previousDirectory = System.getProperty("user.dir");
		}
		if(previousDirectory!=null) {
			fileChooser.setInitialDirectory(new File(previousDirectory));
		}

		// Set extension filter
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
		fileChooser.getExtensionFilters().add(extFilter);

		// Show save file dialog
		File file = fileChooser.showOpenDialog(theStage);

		
		if (file != null) {
			sd = new ScreenData();
			sd.setInputFilePath(file.getAbsolutePath());
			previousDirectory = file.getParent();
			sd = TransformUtils.process(sd);
			sd.setRules(createRules(sd));
			xmlText.setText(sd.getXml());
			xslText.setText(sd.getXsl());
			transformedXML.setText(sd.getTransformedXml());
		}
	}

	
	private List<TagFormatRule> createRules(ScreenData sd) {
		List<String> paths = sd.getPaths();
		List<TagFormatRule> changingFieldsList = new ArrayList<>();
		float layouty = 30.0f;
		float layoutx = 10.0f;
		dropDownAnchor.getChildren().clear();
		
		Label label = new Label();
		label.setLayoutY(2);
		label.setLayoutX(10);
		label.setText("Rules");
		label.setStyle("-fx-background-color: slateblue; -fx-text-fill: white;");
		dropDownAnchor.getChildren().add(label);
		
		for (final String path : paths) {
			XmlNodeTag pathNode = sd.getTree().getRoot().getNodeByTagPath(path);
			final ObservableList<String> dataList = FXCollections.observableArrayList(path);
			final ObservableList<String> attList = FXCollections.observableArrayList(pathNode.getTagAttributes());
			if(pathNode.isLeaf()) {
				attList.add("<<BODY CONTENT>>");
			}

			final  ComboBox<String> tagPath = new ComboBox<>(dataList);
			tagPath.setValue(path);
			dropDownAnchor.getChildren().add(tagPath);

			final  ComboBox<String> attributes = new ComboBox<>(attList);
			if(pathNode.getTagAttributes().size()>0) {
				attributes.setValue(pathNode.getTagAttributes().get(0));
			}
			dropDownAnchor.getChildren().add(attributes);
			
			final  TextField textField1 = new TextField();
			dropDownAnchor.getChildren().add(textField1);
			
			final TextField textField2 = new TextField();
			dropDownAnchor.getChildren().add(textField2);
			
			tagPath.setLayoutY(layouty);
			tagPath.setMaxWidth(100);
			tagPath.setPrefWidth(100);

			attributes.setLayoutY(layouty);
			attributes.setMaxWidth(100);
			attributes.setPrefWidth(100);
			
			textField1.setLayoutY(layouty);
			textField2.setLayoutY(layouty);
			
			tagPath.setLayoutX(layoutx);
			attributes.setLayoutX(layoutx + 130);
			textField1.setLayoutX(layoutx + 240);
			textField2.setLayoutX(layoutx + 420);
			
			layouty  += 30;
			changingFieldsList.add(new TagFormatRule(tagPath, textField1, textField2,attributes));
		}
		
		final Button button = new Button();
		button.setText("Generate XLS");
		button.setLayoutY(layouty + 10);
		button.setLayoutX(layoutx + 200);
		dropDownAnchor.getChildren().add(button);
		dropDownAnchor.layout();
		
		button.setOnAction(e->generateXsl(e));
		
		return changingFieldsList;
	}
	
	
	
	
	@FXML
	void generateXsl(ActionEvent event) {
		sd.setModified(true);
		sd.setXml(xmlText.getText());
		System.out.println(sd.getRules().get(0).getTextField2().getText());
		sd = TransformUtils.process(sd);
		
		xslText.setText(sd.getXsl());
		
	}
}
