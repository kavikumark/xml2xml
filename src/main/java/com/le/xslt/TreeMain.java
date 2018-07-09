package com.le.xslt;

import java.util.List;

import com.le.xslt.util.TransformUtils;
import com.le.xslt.util.XmlNodeTag;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.stage.Stage;

public class TreeMain extends Application {
	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) {
		
		String path = "E:\\workspace\\git\\mini-projects\\xml2xslt\\src\\main\\resources\\";
		String fname = "chatlog.xml";
		ScreenData sd = new ScreenData();
		sd.setInputFilePath(path+fname);
		sd = TransformUtils.process(sd);
		List<String> allNodePaths = sd.getTree().getRoot().getAllNodePaths();
		System.out.println(allNodePaths);

		
		final Scene scene = new Scene(new Group(), 200, 400);
		Group sceneRoot = (Group) scene.getRoot();
		
		XmlNodeTag tree = sd.getTree();
		TreeItem<String> root = new TreeItem<>(tree.getRoot().getTagName());
		root.setExpanded(true);
		TreeTableView<String> treeTableView = new TreeTableView<>(root);
		addChildren(tree.getRoot(), root,treeTableView);

		TreeTableColumn<String, String> column = new TreeTableColumn<>("Tag");
		column.setPrefWidth(150);
		column.setCellValueFactory((CellDataFeatures<String, String> p) -> new ReadOnlyStringWrapper(p.getValue().getValue()));
		treeTableView.getColumns().add(column);
		
		for(int i=0;i<4;i++) {
			TreeTableColumn<String, String> col = new TreeTableColumn<>("Attribute "+(i+1));
			col.setPrefWidth(150);
			col.setCellValueFactory((CellDataFeatures<String, String> p) -> new ReadOnlyStringWrapper(""+Math.random()));
			treeTableView.getColumns().add(col);
		}
		

		sceneRoot.getChildren().add(treeTableView);
		stage.setScene(scene);
		stage.show();
	}
	
	private void addChildren(XmlNodeTag node,TreeItem treeItem, TreeTableView<String> treeTableView) {
		treeItem.setExpanded(true);
		for(XmlNodeTag childNode:node.getChildren()) {
			TreeItem<String> childTreeItem = new TreeItem<>(childNode.getTagName());
			treeItem.getChildren().add(childTreeItem);
			addChildren(childNode, childTreeItem,treeTableView);
		}
	}
}