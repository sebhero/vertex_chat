package com.sebhero.tutorial.client.gui;/**
 * Created by Sebastian Börebäck on 2016-01-31.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class GuiChatClient extends Application {
	private static final String VIEW_GAME = "/template/chat.fxml";

	@Override
	public void start(Stage primaryStage) throws IOException {
		initGui(primaryStage);
	}

	private void initGui(Stage stage) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource(VIEW_GAME));
		Scene scene = new Scene(root);
		scene.setFill(Color.GRAY);
		stage.setScene(scene);
		stage.setTitle("Sebhero Websocket chat client");
		stage.show();
	}

	public static void main(String[] args) {
		Application.launch(args);
	}
}
