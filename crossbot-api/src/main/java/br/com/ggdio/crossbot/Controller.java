package br.com.ggdio.crossbot;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.api.methods.send.SendAudio;
import org.telegram.telegrambots.api.methods.send.SendDocument;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import br.com.ggdio.crossbot.data.Callback;
import br.com.ggdio.crossbot.data.EditMessageResponse;
import br.com.ggdio.crossbot.data.Incoming;
import br.com.ggdio.crossbot.data.Menu;
import br.com.ggdio.crossbot.data.Request;
import br.com.ggdio.crossbot.data.SendAudioResponse;
import br.com.ggdio.crossbot.data.SendDocumentResponse;
import br.com.ggdio.crossbot.data.SendMessageResponse;
import br.com.ggdio.crossbot.data.SendPhotoResponse;

public abstract class Controller {
	
	private Mediator bot;
	
	void setBotCommander(Mediator mediator) {
		this.bot = mediator;
	}
	
	protected void send(Incoming request, SendMessageResponse response) {
		if(bot == null) throw new IllegalStateException("Mediator not set on controller " + this.getClass().getName());
		
		try {
			SendMessage message = new SendMessage()
				.enableMarkdown(response.isMarkdownEnabled())
				.setText(response.getText());
			
			if(response.getMenu() != null && !response.getMenu().isEmpty()) {
				message.setReplyMarkup(buildMenu(response.getMenu(), response.getMenuCols()));
			}
			
			if(request != null) {
				message.setReplyToMessageId(request.getMessageId());
				message.setChatId(request.getChatId());
			} else {
				message.setChatId(response.getChatId());
			}
			
			bot.execute(message);
			
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	protected void send(Incoming request, EditMessageResponse response) {
		if(bot == null) throw new IllegalStateException("Mediator not set on controller " + this.getClass().getName());
		
		try {
			EditMessageText message = new EditMessageText()
				.enableMarkdown(response.isMarkdownEnabled())
				.setText(response.getText());
			
			if(response.getMenu() != null && !response.getMenu().isEmpty()) {
				message.setReplyMarkup((InlineKeyboardMarkup) buildMenu(response.getMenu(), response.getMenuCols()));
			}
			
			message.setMessageId(request.getMessageId());
			message.setChatId(request.getChatId());
			
			bot.execute(message);
			
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	protected void send(Incoming request, SendPhotoResponse response) {
		if(bot == null) throw new IllegalStateException("Mediator not set on controller " + this.getClass().getName());
		
		try {
			SendPhoto message = new SendPhoto();
			if(response.getFile() != null) {
				message.setNewPhoto(response.getFile());
			} else {
				message.setNewPhoto(response.getName(), response.getStream());
			}
			
			if(request != null) {
				message.setReplyToMessageId(request.getMessageId());
				message.setChatId(request.getChatId());
				
			} else {
				message.setChatId(response.getChatId());
				
			}
			
			bot.sendPhoto(message);
			
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	protected void send(Incoming request, SendDocumentResponse response) {
		if(bot == null) throw new IllegalStateException("Mediator not set on controller " + this.getClass().getName());
		
		try {
			SendDocument message = new SendDocument();
			if(response.getFile() != null) {
				message.setNewDocument(response.getFile());
			} else {
				message.setNewDocument(response.getName(), response.getStream());
			}
			
			if(request != null) {
				message.setReplyToMessageId(request.getMessageId());
				message.setChatId(request.getChatId());
				
			} else {
				message.setChatId(response.getChatId());
				
			}
			
			bot.sendDocument(message);
			
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	protected void send(Incoming request, SendAudioResponse response) {
		if(bot == null) throw new IllegalStateException("Mediator not set on controller " + this.getClass().getName());
		
		try {
			SendAudio message = new SendAudio();
			if(response.getFile() != null) {
				message.setNewAudio(response.getFile());
			} else {
				message.setNewAudio(response.getName(), response.getStream());
			}
			
			if(request != null) {
				message.setReplyToMessageId(request.getMessageId());
				message.setChatId(request.getChatId());
				
			} else {
				message.setChatId(response.getChatId());
				
			}
			
			bot.sendAudio(message);
			
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	protected Menu getMenuItem(Incoming request, String text, String...data) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < data.length; i++) {
			String item = data[i];
			sb.append(item);
			
			if(i < (data.length-1)) sb.append("#");
			
		}
		return new Menu(text, request.getMethod() + ";" + sb.toString());
	}
	
	public void expectReply(Incoming incoming) {
		bot.expectReplyFrom(incoming.getUserId(), this);
	}
	
	private ReplyKeyboard buildMenu(List<Menu> menu, int cols) {
		List<List<InlineKeyboardButton>> rows = new ArrayList<>();
		List<InlineKeyboardButton> row = null;
		
		int count = 0;
		for (Menu item : menu) {
			if(row == null) {
				row = new ArrayList<>();
			}
			
			row.add(buildMenuItem(item));
			
			if(++count == cols) {
				rows.add(row);
				row = null;
				count = 0;
			}
		}
		if(row != null) rows.add(row);
		
		InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
		markup.setKeyboard(rows);
		
		return markup;
	}
	
	private InlineKeyboardButton buildMenuItem(Menu item) {
		return new InlineKeyboardButton()
				.setText(item.getText())
				.setCallbackData(item.getCallbackData());
	}
	
	public boolean acceptPrivateMessage(Integer userId, String userName) {
		return true;
	}
	
	public boolean acceptIncoming(Integer userId, String userName) {
		return true;
	}
	
	public void stop() {
		
	}
	
	public abstract String getMethod();
	public abstract void request(Request request);
	public abstract void callback(Callback callback);

}