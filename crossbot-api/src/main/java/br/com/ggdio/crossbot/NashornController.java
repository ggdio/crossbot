package br.com.ggdio.crossbot;

import br.com.ggdio.crossbot.data.Callback;
import br.com.ggdio.crossbot.data.Request;
import br.com.ggdio.crossbot.script.ChatbotScriptEngine;

/**
 * Nashorn Wrapper
 * 
 * @author Guilherme Dio
 *
 */
class NashornController extends Controller {
	
	private static final String ENGINE_NAME = "nashorn";

	private final ChatbotScriptEngine engine;

	NashornController(String scriptPath) {
		this.engine = new ChatbotScriptEngine(this, ENGINE_NAME, scriptPath);
	}

	@Override
	public String getMethod() {
		return engine.getMethod();
	}

	@Override
	public void request(Request request) {
		engine.request(request);
	}

	@Override
	public void callback(Callback callback) {
		engine.callback(callback);
	}

}
