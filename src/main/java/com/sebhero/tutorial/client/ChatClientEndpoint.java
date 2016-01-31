package com.sebhero.tutorial.client;


import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

/**
 * Created by Sebastian Börebäck on 2016-01-31.
 */
@ClientEndpoint
public class ChatClientEndpoint {
	Session userSession = null;
	MessageHandler messageHandler;

	public ChatClientEndpoint(final URI endpointURI) {
		try {
			WebSocketContainer container = ContainerProvider.getWebSocketContainer();
			container.connectToServer(this, endpointURI);
		} catch (DeploymentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@OnOpen
	public void onOpen(final Session userSession) {
		this.userSession = userSession;
	}

	@OnClose
	public void onClose(final Session userSession, final CloseReason reason) {
		this.userSession = null;
	}

	@OnMessage
	public void onMessage(final String message) {
		if (messageHandler != null) {
			messageHandler.handleMessage(message);
		}
	}

	public void addMessageHandler(final MessageHandler msgHandler) {
		messageHandler = msgHandler;
	}

	public void sendMessage(final String message) {
		userSession.getAsyncRemote().sendText(message);
	}

	public static interface MessageHandler{
		public void handleMessage(String message);
	}
}
