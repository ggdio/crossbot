package br.com.ggdio.crossbot;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

import br.com.ggdio.crossbot.data.Callback;
import br.com.ggdio.crossbot.data.EditMessageResponse;
import br.com.ggdio.crossbot.data.Incoming;
import br.com.ggdio.crossbot.data.Menu;
import br.com.ggdio.crossbot.data.SendDocumentResponse;
import br.com.ggdio.crossbot.data.SendMessageResponse;
import br.com.ggdio.crossbot.data.SendPhotoResponse;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

/**
 * Scope for script engine chatbot controller
 * 
 * @author Guilherme Dio
 *
 */
@SuppressWarnings("restriction")
public class ScriptControllerScope {

	private final Controller controller;

	public ScriptControllerScope(Controller controller) {
		this.controller = controller;
		
	}
	
	public String[] parseCallback(Callback callback) {
		return callback.getArgs();
	}
	
	@SuppressWarnings("unchecked")
	public void text(Incoming incoming, ScriptObjectMirror object) {
		Map<String, Object> args = (Map<String, Object>) convert(object);
		controller.send(incoming, new SendMessageResponse(
				getText(args), 
				getMenu(incoming, args), 
				true
			));
	}
	
	@SuppressWarnings("unchecked")
	public void edit(Incoming incoming, ScriptObjectMirror object) {
		Map<String, Object> args = (Map<String, Object>) convert(object);
		controller.send(incoming, new EditMessageResponse(
				getText(args), 
				getMenu(incoming, args), 
				true
			));
	}
	
	@SuppressWarnings("unchecked")
	public void photo(Incoming incoming, ScriptObjectMirror object) {
		Map<String, Object> args = (Map<String, Object>) convert(object);
		controller.send(incoming, new SendPhotoResponse(new File(getFile(args))));
	}
	
	@SuppressWarnings("unchecked")
	public void document(Incoming incoming, ScriptObjectMirror object) {
		Map<String, Object> args = (Map<String, Object>) convert(object);
		controller.send(incoming, new SendDocumentResponse(new File(getFile(args))));
	}
	
	private String getText(Map<String, Object> args) {
		return get("text", args);
	}
	
	private String getFile(Map<String, Object> args) {
		return get("file", args);
	}
	
	private String get(String field, Map<String, Object> args) {
		String text = MapUtils.getString(args, field);
		if(text == null) throw new IllegalArgumentException("Missing '"+field+"' field.");
		
		return text;
	}
	
	private Object convert(Object scriptObj) {
	    if (scriptObj instanceof ScriptObjectMirror) {
	        ScriptObjectMirror scriptObjectMirror = (ScriptObjectMirror) scriptObj;
	        if (scriptObjectMirror.isArray()) {
	            List<Object> list = new ArrayList<>();
	            for (Map.Entry<String, Object> entry : scriptObjectMirror.entrySet()) {
	                list.add(convert(entry.getValue()));
	            }
	            return list;
	        } else {
	            Map<String, Object> map = new HashMap<>();
	            for (Map.Entry<String, Object> entry : scriptObjectMirror.entrySet()) {
	                map.put(entry.getKey(), convert(entry.getValue()));
	            }
	            return map;
	        }
	    } else {
	        return scriptObj;
	    }
	}
	
	@SuppressWarnings("unchecked")
	private List<Menu> getMenu(Incoming incoming, Map<String, Object> args) {
		Object rawMenu = args.get("menu");
		if(rawMenu == null) return null;
		
		List<Menu> menu = new ArrayList<>();
		List<Map<String, String>> items = (List<Map<String, String>>) rawMenu;
		for (Map<String, String> item : items) {
			menu.add(controller.getMenuItem(incoming, item.get("text"), item.get("data")));
		}
		
		return menu;
	}

}