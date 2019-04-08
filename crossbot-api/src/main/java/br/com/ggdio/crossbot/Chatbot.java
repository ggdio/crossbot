package br.com.ggdio.crossbot;

import java.util.List;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.generics.BotSession;

/**
 * Chatbot builder
 * 
 * @author Guilherme Dio
 *
 */
public class Chatbot {
	
	private static TelegramBotsApi api;
	
	private final Mediator mediator;

	private BotSession session;
	
	Chatbot(Mediator mediator) {
		this.mediator = mediator;
	}
	
	public String getBotName() {
		return mediator.getBotUsername();
	}

	public static Chatbot newBot(String id, String token) {
		if(Chatbot.api == null) {
			ApiContextInitializer.init();
			Chatbot.api = new TelegramBotsApi();
		}
		
		return new Chatbot(Mediator.getInstance(id, token));
	}
	
	public Chatbot allowUsers(List<String> users) {
		for (String user : users) {
			mediator.allow(user);
		}
		return this;
	}
	
	public Chatbot withController(Controller controller) {
		mediator.registerController(controller);
		return this;
	}
	
	public Chatbot withNashornController(String path) {
		mediator.registerController(new NashornController(path));
		return this;
	}
	
	public Chatbot withHTTPController(String endpoint, String method) {
		mediator.registerController(new HTTPController(endpoint, method));
		return this;
	}
	
	public void changeToken(String token) {
		session.setToken(token);
	}
	
	public boolean isRunning() {
		return session != null && session.isRunning();
	}
	
	public void start() {
		mediator.start();
		
		if(session != null && !session.isRunning()) {
			session.start();
			return;
		}
		
		try {
			session = Chatbot.api.registerBot(mediator);
			
		} catch (TelegramApiRequestException e) {
			throw new RuntimeException(e);
			
		}
	}
	
	public void clearMediator() {
		mediator.clearControllers();
	}
	
	public void stop() {
		mediator.halt();
		
		if(session != null) 
			session.stop();
	}
	
}