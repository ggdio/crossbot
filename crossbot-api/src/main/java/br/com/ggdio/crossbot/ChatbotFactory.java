package br.com.ggdio.crossbot;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.ggdio.crossbot.utils.JSONUtils;

public class ChatbotFactory {
	
	private static final Logger LOG = LoggerFactory.getLogger(ChatbotFactory.class);

	public static Chatbot getFromClasspathConfig() throws Exception {
		String file = System.getProperty("config");
		String json = FileUtils.readFileToString(new File(file), Charset.defaultCharset());
		return getFromJSON(json);
	}

	@SuppressWarnings("unchecked")
	public static Chatbot getFromJSON(String json) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SecurityException, IllegalArgumentException, InvocationTargetException {
		Map<String, Object> data = (Map<String, Object>) JSONUtils.fromJSON(json);
		
		String tenant = MapUtils.getString(data, "tenant");
		String bot = MapUtils.getString(data, "bot");
		String token = MapUtils.getString(data, "token");
		List<Object> controllers = (List<Object>) data.get("controllers");
		
		LOG.info("Setting up BOT [id={}]", bot);
		
		Chatbot chatbot = Chatbot.newBot(bot, token);
		
		if(data.containsKey("allow")) {
			chatbot.allowUsers((List<String>) data.get("allow"));
		}
		
		for (Object def : controllers) {
			LOG.info("Initializing controller={}", def);
			if(def instanceof String) {
				chatbot.withController(buildController(tenant, null, (String) def));
				
			} else {
				Map<String, Object> item = (Map<String, Object>) def;
				String type = MapUtils.getString(item, "type");
				
				if("http".equals(type)) {
					String endpoint = MapUtils.getString(item, "endpoint");
					String method = MapUtils.getString(item, "method");
					
					chatbot.withHTTPController(endpoint, method);
					
				} else if("nashorn".equals(type)) {
					String path = MapUtils.getString(item, "path");
					if(path.startsWith("classpath://")) {
						path = ChatbotFactory.class.getResource(path.substring(12)).getFile();
						
					} else if(path.startsWith("url://")) {
						throw new IllegalArgumentException("URLClassLoader not yet supported. Put your libs under classpath manually.");
						
					}
					
					chatbot.withNashornController(path);
					
				} else if("java".equals(type)) {
					String clazz = MapUtils.getString(item, "class");
					
					chatbot.withController(buildController(tenant, item, clazz));
					
				} else {
					LOG.error("Unknown controller type [def={}, args={}]", type, item);
					throw new RuntimeException("Unknown controller type '"+type+"'");
					
				}
				
			}
		}
		return chatbot;
	}

	@SuppressWarnings("unchecked")
	private static Controller buildController(String tenant, Map<String, Object> args, String clazz) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Class<Controller> clazzDef = (Class<Controller>) Class.forName(clazz);
		if(args == null) args = new HashMap<>();
		
		try { // 1st TENANT + MAP
			return clazzDef.getConstructor(String.class, Map.class).newInstance(tenant, args);
			
		} catch (NoSuchMethodException e) {
			try { // 2nd TENANT
				return clazzDef.getConstructor(String.class).newInstance(tenant);
				
			} catch (NoSuchMethodException e1) {
				try { //3rd DEFAULT
					return clazzDef.getConstructor().newInstance();
					
				} catch (NoSuchMethodException e2) {
					throw new IllegalStateException("The controller '"+clazz+"' lacks constructors from pattern(tenant VS tenant + args VS default)");
					
				} 
				
				
			}
		}
	}

}
