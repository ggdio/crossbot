package br.com.ggdio.crossbot.data;

import java.util.List;

public class EditMessageResponse extends Response {

	public EditMessageResponse(String text) {
		super(text);
	}

	public EditMessageResponse(String text, boolean markdown) {
		super(text, markdown);
	}

	public EditMessageResponse(String text, List<Menu> menu) {
		super(text, menu);
	}

	public EditMessageResponse(String text, List<Menu> menu, boolean markdown) {
		super(text, menu, markdown);
	}

}