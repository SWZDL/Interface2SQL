package cn.shengwenzeng.instanceinterfacesqlfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Interface2SQLApplication extends Application {
	@Override
	public void start(Stage stage) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(Interface2SQLApplication.class.getResource("InstanceInterfaceSQLFX-view.fxml"));
		Scene scene = new Scene(fxmlLoader.load(), 320, 240);
		stage.setWidth(1000);
		stage.setHeight(500);
		stage.setTitle("InstanceInterfaceSQLFX");
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch();
	}
}