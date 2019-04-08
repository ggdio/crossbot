package br.com.ggdio.crossbot.utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;

/**
 * JSON utilitary
 * 
 * @author Guilherme Dio
 *
 */
public class JSONUtils {

	public static final ObjectMapper mapper = new ObjectMapper(new AllowCommentsJsonFactory());
	
	public static final <T> void registerSerializer(Class<? extends T> type, JsonSerializer<T> serializer) {
		SimpleModule module = new SimpleModule();
		try {
			module.addSerializer(type, serializer);
			
		} catch (Exception e) {
			throw new RuntimeException("Could'nt register serializer '"+ serializer.getClass() +"'", e);
			
		}
		mapper.registerModule(module);
	}
	
	public static final <T> void registerDeserializer(Class<T> type, JsonDeserializer<? extends T> deserializer) {
		SimpleModule module = new SimpleModule();
		try {
			module.addDeserializer(type, deserializer);
			
		} catch (Exception e) {
			throw new RuntimeException("Could'nt register serializer '"+ deserializer.getClass() +"'", e);
			
		}
		mapper.registerModule(module);
	}
	
	/**
	 * Parse object to JSON
	 * @param data - Object to be parsed
	 * 
	 * @return JSON as String
	 */
	public static final String toJSON(Object data) {
		try {
			return mapper.writeValueAsString(data);
			
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
			
		}
	}
	
	/**
	 * Shortcut method for {@link JSONUtils#fromJSON(Class, String)}
	 * @param json
	 * @return
	 */
	public static final Object fromJSON(String json) {
		return fromJSON(Object.class, json);
	}
	
	/**
	 * Parse string JSON to java object
	 * @param def - The object class definition
	 * @param json - The string json
	 * 
	 * @return Java Object
	 */
	public static final <T> T fromJSON(Class<T> def, String json) {
		try {
			return (T) mapper.readValue(json, def);
			
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
			
		}
	}
	
	/**
	 * Parse string JSON to java collection
	 * @param def - DTO type
	 * @param collType - Java type
	 * @param json - String
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static final <T> List<T> fromJSON(Class<T> def, CollectionType collType, String json) {
		try {
			return (List<T>) mapper.readValue(json, collType);
			
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
			
		}
	}

	public static <T> T treeToValue(JsonNode node, Class<T> type) throws JsonProcessingException {
		return mapper.treeToValue(node, type);
	}
	
	/**
	 * Searches for an object value in a json
	 * 
	 * @param keyPath
	 * @param root
	 * @return
	 */
	public static Object resolveValue(String keyPath, Object root) {
		if(keyPath == null || root == null) {
			return null;
		}
		
    	String[] splittedPath = keyPath.split("\\.");
    	
		if(splittedPath.length > 1) {
    		String rootKey = splittedPath[0];
    		String popKeyPos = keyPath.substring(rootKey.length() + 1);
    		
    		if(rootKey.indexOf("[@") > 0) {
    			Object qObject = findObjectOnArray(keyPath, root);
    			return resolveValue(popKeyPos, qObject);
    		} 
    		
    		Object refObject = null;
    		if (root instanceof Map) {
    			refObject =  ((Map<?, ?>)root).get(rootKey);
    		} else if (root instanceof List) {
    			refObject = ((List<?>)root).get(Integer.valueOf(rootKey));
    		}
    		
    		
    		if(refObject instanceof Map || refObject instanceof List) {
				return resolveValue(popKeyPos, refObject);
				
    		} else {//dotted path found not matching object type
    			return null;
    			
    		}
    	}
		
		
    	//No dotted path 
		if(keyPath.indexOf("[@") > 0) {
			return findObjectOnArray(keyPath, root);
		} 
		
    	if (root instanceof Map) {
			return ((Map<?, ?>)root).get(keyPath);
			
		} else if (root instanceof List) {
			return ((List<?>)root).get(Integer.valueOf(keyPath));
			
		} 

    	return null;//not found
	}
	
	private static Object findObjectOnArray(String keyPath, Object root) {
		String[] queryElements = keyPath.split("\\[\\@|\\]|\\=\\'|\\'");
		List<?> array = (List<?>) ((Map<?, ?>) root).get(queryElements[0]);
		for(Object obj : array) {
			if(obj instanceof Map) {
				Map<?, ?> theMap = (Map<?, ?>) obj;
				Object theAttribute = theMap.get(queryElements[1]);
				if(theAttribute != null && theAttribute.equals(queryElements[2])){
					return theMap;
				}
			}
		}
		return null;
	}
	
	private static class AllowCommentsJsonFactory extends JsonFactory {
		
		private static final long serialVersionUID = 6364474439353234335L;
		
		@Override
		public JsonParser createParser(String value) throws IOException, JsonParseException {
			return this.createJsonParser(value);
		}

		@Override
	    public JsonParser createJsonParser(String value) throws IOException, JsonParseException {
			JsonParser p = super.createParser(value);
			p.enable(JsonParser.Feature.ALLOW_COMMENTS);
			return p;
	    }
	}
	
}