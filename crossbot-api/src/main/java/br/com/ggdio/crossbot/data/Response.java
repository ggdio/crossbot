package br.com.ggdio.crossbot.data;

import java.util.List;

public abstract class Response {
	
	private final boolean markdown;
	private final List<Menu> menu;
	private final String text;
	
	private Long chatId;
	
	private int menuCols = 3;
	
	public Response(String text) {
		this(text, null, false);
	}
	
	public Response(String text, boolean markdown) {
		this(text, null, markdown);
	}
	
	public Response(String text, List<Menu> menu) {
		this(text, menu, false);
	}
	
	public Response(String text, List<Menu> menu, boolean markdown) {
		this.text = text;
		this.menu = menu;
		this.markdown = markdown;
	}
	
	public void setMenuCols(int menuCols) {
		this.menuCols = menuCols;
	}

	public boolean isMarkdownEnabled() {
		return markdown;
	}
	
	public List<Menu> getMenu() {
		return menu;
	}
	
	public String getText() {
		return text;
	}
	
	public int getMenuCols() {
		return menuCols;
	}
	
	public Long getChatId() {
		return chatId;
	}
	
	public void setChatId(Long chatId) {
		this.chatId = chatId;
	}

}
