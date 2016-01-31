package com.sebhero.tutorial.client.gui;

import com.sebhero.tutorial.client.ChatClientEndpoint;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by Sebastian Börebäck on 2016-01-31.
 */
public class ChatController implements Initializable {

	@FXML
	private MenuItem exitItem;

	@FXML
	private ChoiceBox<String> roomSelection;

	@FXML
	private Button connectButton;

	@FXML
	private TextField userNameTextfield;

	@FXML
	private TextField messageTextField;

	@FXML
	private Button chatButton;

	@FXML
	private MenuItem aboutMenuItem;

	@FXML
	private ListView<String> chatListView;

	private final ChatModel model = new ChatModel();

	private ChatClientEndpoint clientEndPoint;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		exitItem.setOnAction(e -> Platform.exit());
		roomSelection.setItems(FXCollections.observableArrayList("arduino", "java", "groovy", "scala"));
		roomSelection.getSelectionModel().select(0);
		model.userName.bindBidirectional(userNameTextfield.textProperty());
		model.roomName.bind(roomSelection.getSelectionModel().selectedItemProperty());
		model.readyToChat.bind(model.userName.isNotEmpty().and(roomSelection.selectionModelProperty().isNotNull()));
		chatButton.disableProperty().bind(model.readyToChat.not());
		messageTextField.textProperty().bindBidirectional(model.currentMessage);
		connectButton.disableProperty().bind(model.readyToChat.not());
		chatListView.setItems(model.chatHistory);
		messageTextField.setOnAction(event -> {
			handleSendMessage();
		});
		chatButton.setOnAction(evt -> {
			handleSendMessage();
		});

		roomSelection.setOnAction(e ->{
			System.out.println(roomSelection.getSelectionModel().selectedItemProperty().get());
			System.out.println(roomSelection.getSelectionModel().selectedItemProperty());
		});

		connectButton.setOnAction(event -> {
			try {
				clientEndPoint = new ChatClientEndpoint(new URI("ws://localhost:8090/chat/" + model.roomName.get()));
				clientEndPoint.addMessageHandler(responseString -> {
					Platform.runLater(() -> {
						model.chatHistory.add(jsonMessageToString(responseString, model.roomName.get()));
					});
				});
				model.connected.set(true);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}

		});

		aboutMenuItem.setOnAction(event -> {
			showDialog("Example websocket chat bot written in JavaFX.\n\n Please feel free to visit my blog at www.hascode.com for the full tutorial!\n\n2014 Micha Kops");
		});

	}

	private void handleSendMessage() {
		clientEndPoint.sendMessage(stringToJsonMessage(model.userName.get(), model.currentMessage.get()));
		model.currentMessage.set("");
		messageTextField.requestFocus();
	}

	private void showDialog(final String message) {
		Stage dialogStage = new Stage();
		dialogStage.initModality(Modality.WINDOW_MODAL);
		VBox box = new VBox();
		box.getChildren().addAll(new Label(message));
		box.setAlignment(Pos.CENTER);
		box.setPadding(new Insets(5));
		dialogStage.setScene(new Scene(box));
		dialogStage.show();
	}

	private static String stringToJsonMessage(final String user, final String message) {
		return Json.createObjectBuilder().add("sender", user).add("message", message).build().toString();
	}

	private static String jsonMessageToString(final String response, final String roomName) {
		JsonObject root = Json.createReader(new StringReader(response)).readObject();
		String message = root.getString("message");
		String sender = root.getString("sender");
		String received = root.getString("received");
		return String.format("%s@%s: %s [%s]", sender, roomName, message, received);
	}
}
