package com.sebhero.tutorial.client.console;


import com.sebhero.tutorial.client.ChatClientEndpoint;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;


/**
 * Created by Sebastian Börebäck on 2016-01-31.
 */
public class ConsoleChatClient {
	public static void main(String[] args) throws URISyntaxException {
//		Console console = System.console();
		Scanner console = new Scanner(System.in);
		try {

			System.out.print("Please enter your user name: ");
			final String userName = "sebcon";
//			final String userName = console.nextLine();
			System.out.println("Enter chat-room name: ");
			final String roomName = "arduino";
//			final String roomName = console.nextLine();
			System.out.println("connecting to chat-room " + roomName);
//			new URI("ws://localhost:8080/sebhero/chat/" + roomName)
			final ChatClientEndpoint clientEndPoint = new ChatClientEndpoint(
					new URI("ws://localhost:8090/chat/" + roomName)
			);

			clientEndPoint.addMessageHandler(responseString -> {
				System.out.println(jsonMessageToString(responseString, roomName));
			});
			System.out.println("connected to server");

			while (true) {
				String message = console.nextLine();
				clientEndPoint.sendMessage(stringToJsonMessage(userName, message));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}


	}

	private static String stringToJsonMessage(String user, String message) {

		return Json.createObjectBuilder()
				.add("sender", user)
				.add("message", message)
				.build().toString();

	}

	private static String jsonMessageToString(String response, String roomName) {
		JsonObject root = Json.createReader(new StringReader(response)).readObject();
		String message = root.getString("message");
		String sender = root.getString("sender");
		String received = root.getString("received");
		return String.format("%s@%s: %s [%s]", sender, roomName, message, received);
	}
}
