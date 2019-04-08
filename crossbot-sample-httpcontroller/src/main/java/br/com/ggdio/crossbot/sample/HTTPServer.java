package br.com.ggdio.crossbot.sample;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import spark.Spark;

public class HTTPServer extends Spark {
	
	public static void main(String[] args) throws InterruptedException {
		new HTTPServer();
	}

	@SuppressWarnings("unchecked")
	public HTTPServer() throws InterruptedException {
		port(8090);
		
		post("/chatbot", (request, response) -> {
			String body = request.body();
			System.out.println(body);
			Map<String, String> requestData = (Map<String, String>) JSONUtils.fromJSON(body);
			String type = requestData.get("type");
			
			response.header("Content-Type", "application/json");
			
			switch(type) {
				case "request": {
					return request(requestData);
				}
				case "callback": {
					return callback(requestData);
				}
			}
			
			return "{\"text\": \"Invalid Entry\"}";
		});
		
		System.out.println("HTTP Server Started !");
		new ReentrantLock().lock();
	}
	
	/**
	 * Handles the INITIAL interaction
	 * @param requestData - Mapped request data
	 * @return JSON
	 */
	private String request(Map<String, String> requestData) {
		return "{"
				+ "\"text\": \"Hello World from HTTP Server !\","
				+ "\"menu\": ["
					+ "{\"text\": \"Item A\", \"data\": \"item-a\"},"
					+ "{\"text\": \"Item B\", \"data\": \"item-b\"},"
					+ "{\"text\": \"Item C\", \"data\": \"item-c\"},"
					+ "{\"text\": \"Item D\", \"data\": \"item-d\"}"
				+ "]"
		  + "}";
	}
	
	/**
	 * Handles a MENU interaction
	 * @param requestData - Mapped callback data
	 * @return JSON
	 */
	private String callback(Map<String, String> requestData) {
		String[] args = requestData.get("data").split(";")[1].split("#");
		String item = args[0];
		
		return "{\"type\": \"edit\", \"text\": \"HTTP Server - You Selected "+item.toUpperCase()+"\"}";
	}

}