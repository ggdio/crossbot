package br.com.ggdio.crossbot.data;

import java.io.File;

public abstract class Incoming {

	private String method;
	
	private Long chatId;
	private Integer messageId;
	
	private String data;
	
	private Integer userId;
	private String userName;
	
	private String firstName;
	private String lastName;
	
	private String locale;
	
	private File attachment;
	
	private boolean privateChat;

	public String getMethod() {
		return method;
	}

	public Long getChatId() {
		return chatId;
	}

	public Integer getMessageId() {
		return messageId;
	}

	public String getData() {
		return data;
	}

	public Integer getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}
	
	public String getLocale() {
		return locale;
	}

	public String getPersonName() {
		String fullName = getFirstName();
		String lastName = getLastName();
		if(lastName != null && !lastName.isEmpty()) {
			fullName = fullName.concat(" ").concat(lastName);
		}
		
		return fullName;
	}
	
	public void setAttachment(File attachment) {
		this.attachment = attachment;
	}
	
	public boolean isPrivateChat() {
		return privateChat;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public void setChatId(Long chatId) {
		this.chatId = chatId;
	}

	public void setMessageId(Integer messageId) {
		this.messageId = messageId;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public void setLocale(String locale) {
		this.locale = locale;
	}
	
	public boolean hasAttachment() {
		return getAttachment() != null;
	}
	
	public File getAttachment() {
		return attachment;
	}
	
	public void setPrivateChat(boolean privateChat) {
		this.privateChat = privateChat;
	}

}