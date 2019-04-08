package br.com.ggdio.crossbot.data;

import java.util.ArrayList;
import java.util.List;

import br.com.ggdio.crossbot.Controller;

public class Menu {

	private final String text;
	private final String callbackData;
	
	public Menu(String text, String callbackData) {
		this.text = text;
		this.callbackData = callbackData;
	}
	
	public String getText() {
		return text;
	}
	
	public String getCallbackData() {
		return callbackData;
	}
	
	public static Builder getBuilder(Controller controller) {
		return new Menu.Builder(controller);
	}
	
	public static class Builder {
		private final Controller controller;
		private final List<Menu> result = new ArrayList<>();
		
		public Builder(Controller controller) {
			this.controller = controller;
		}
		
		public Builder add(String text, String...callback) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < callback.length; i++) {
				String item = callback[i];
				sb.append(item);
				
				if(i < (callback.length-1)) sb.append("#");
				
			}
			result.add(new Menu(text, controller.getMethod() + ";" + sb.toString()));
			
			return this;
		}
		
		public List<Menu> build() {
			return result;
		}
	}
	
}