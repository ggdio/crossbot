package br.com.ggdio.crossbot.script;

import javax.script.ScriptException;

import org.apache.commons.collections.map.HashedMap;

import br.com.ggdio.crossbot.Controller;
import br.com.ggdio.crossbot.ScriptControllerScope;
import br.com.ggdio.crossbot.data.Callback;
import br.com.ggdio.crossbot.data.Request;

public class ChatbotScriptEngine extends AbstractScriptEngine {

	@SuppressWarnings({ "serial", "unchecked" })
	public ChatbotScriptEngine(Controller controller, String engine, String path) {
		super(engine, path, new HashedMap() {{
			put("controller", new ScriptControllerScope(controller));
		}});
	}
	
	public void request(Request request) {
		try {
			invoke("request(request)", request);
			
		} catch (ScriptException e) {
			throw new RuntimeException(e);
			
		}
	}
	
	public void callback(Callback callback) {
		try {
			invoke("callback(callback)", callback);
			
		} catch (ScriptException e) {
			throw new RuntimeException(e);
			
		}
	}

	public String getMethod() {
		try {
			return (String) invoke("method(foo)", "bar");
			
		} catch (ScriptException e) {
			throw new RuntimeException(e);
			
		}
	}

}
