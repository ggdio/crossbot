package br.com.ggdio.crossbot.data;

import java.util.List;

/**
 * Response object that sends a plain text message
 * 
 * @author Guilherme Dio
 *
 */
public class SendMessageResponse extends Response {
	
	public SendMessageResponse(String text) {
		super(text);
	}

	public SendMessageResponse(String text, boolean markdown) {
		super(text, markdown);
	}

	public SendMessageResponse(String text, List<Menu> menu) {
		super(text, menu);
	}

	public SendMessageResponse(String text, List<Menu> menu, boolean markdown) {
		super(text, menu, markdown);
	}

}