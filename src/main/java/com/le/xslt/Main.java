package com.le.xslt;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.le.xslt.view.MainView;

@SpringBootApplication(scanBasePackages={"com.le.xslt"})
@EnableScheduling
public class Main extends AbstractJavaFxApplicationSupport {

    public static void main(final String[] args) {
    	launch(Main.class, MainView.class, new XmlLogoScreen(),  args);
    }

}




