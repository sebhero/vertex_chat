package com.sebhero.tutorial;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Sebastian Börebäck on 2016-01-30.
 */
public class WebserverVerticle extends Verticle {

	@Override
	public void start() {

		final Pattern charUrlPattern = Pattern.compile("/chat/(\\w+)");
		final EventBus eventBus = vertx.eventBus();
		final Logger logger = container.logger();

		// 1' HTTP Server

		RouteMatcher httpRouterRouteMatcher = new RouteMatcher().get("/", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {

				request.response().sendFile("web/chat.html");

			}
			//".*\\.(css|js)$", new Handler<HttpServerRequest>
		}).get(".*\\.(css|js)$", new Handler<HttpServerRequest>() {
			@Override
			public void handle(HttpServerRequest request) {
				request.response().sendFile("web/" + new File(request.path()));
			}
		});

		vertx.createHttpServer().requestHandler(httpRouterRouteMatcher).listen(8080, "localhost");

		// 2' Websocket chat Server

		vertx.createHttpServer().websocketHandler(ws -> {
			final Matcher m = charUrlPattern.matcher(ws.path());
			if (!m.matches()) {
				ws.reject();
				return;
			}

			final String chatRoom = m.group(1);
			final String id = ws.textHandlerID();
			logger.info("Registering new connection with id: " + id +
					" for char-room");

			vertx.sharedData().getSet("chat.room." + chatRoom).add(id);

//				ws.closeHandler(new Handler<Void>() {
			ws.closeHandler(event -> {
				logger.info("Un-registering connection with id " + id +
						" for chat-room " + chatRoom);
				vertx.sharedData().getSet("chat.room." + chatRoom).remove(id);
			});

			ws.dataHandler(data -> {
				ObjectMapper om = new ObjectMapper();
				try {
					JsonNode rootNode = om.readTree(data.toString());
					((ObjectNode) rootNode).put("received", new Date().toString());
					String jsonOutput = om.writeValueAsString(rootNode);
					logger.info("json generated: " + jsonOutput);
					for (Object chatter : vertx.sharedData().getSet("chat.room." + chatRoom)) {
						eventBus.send((String) chatter, jsonOutput);
					}

				} catch (JsonProcessingException e) {
					e.printStackTrace();
					ws.reject();
				} catch (IOException e) {
					e.printStackTrace();
					ws.reject();
				}
			});

		}).listen(8090);
	}
}
