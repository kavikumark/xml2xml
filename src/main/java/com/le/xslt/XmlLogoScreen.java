package com.le.xslt;

import javafx.scene.Parent;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class XmlLogoScreen extends SplashScreen{

	private static String DEFAULT_IMAGE = "/icons/xml.gif";

	/**
	 * Override this to create your own splash pane parent node.
	 *
	 * @return A standard image
	 */
	public Parent getParent() {
		final ImageView imageView = new ImageView(getClass().getResource(getImagePath()).toExternalForm());
		final ProgressBar splashProgressBar = new ProgressBar();
		splashProgressBar.setPrefWidth(imageView.getImage().getWidth());
		final VBox vbox = new VBox();
		vbox.getChildren().addAll(imageView, splashProgressBar);
		return vbox;
	}

	/**
	 * Customize if the splash screen should be visible at all.
	 *
	 * @return true by default
	 */
	public boolean visible() {
		return true;
	}

	/**
	 * Use your own splash image instead of the default one.
	 *
	 * @return "/icons/emrilogo.png"
	 */
	public String getImagePath() {
		return DEFAULT_IMAGE;
	}

	
}
