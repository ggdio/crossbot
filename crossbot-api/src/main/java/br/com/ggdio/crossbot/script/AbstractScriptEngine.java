package br.com.ggdio.crossbot.script;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.LoggerFactory;

/**
 * Abstract Engine for Script Invocation
 * 
 * @author Guilherme Dio
 *
 */
public abstract class AbstractScriptEngine {

	// cached
	private static final ScriptEngineManager manager = new ScriptEngineManager();
	private static final Map<String, ScriptEngine> engines = new HashMap<>();
	private static final Map<String, String[]> funcArgs = new HashMap<>();
	
	// prototype
	private final String engineName;
	private final String namespace;
	private final ScriptEngine engine;
	
	private static final String OBJECT = "instance";
	private static final Pattern PATTERN = Pattern.compile("^.*\\((.*)\\)$");
	
	public AbstractScriptEngine(String engine, String path) {
		this(engine, path, null);
	}

	public AbstractScriptEngine(String engine, String path, Map<String, Object> bindings) {
		if(engine == null) engine = "nashorn";
		
		this.engineName = engine;
		this.namespace = engine.concat(":").concat(path);
		this.engine = getEngine(engine, path, bindings);
		
	}

	private ScriptEngine getEngine(String engineName, String path, Map<String, Object> bindings) {
		ScriptEngine engine = engines.get(namespace);
		if(engine == null) {
			engine = manager.getEngineByName(engineName);
			engines.put(namespace, engine);
		}
		
		ScriptContext context = engine.getContext();
		
		try {
			Bindings b = getBindings(engine, path);
			for (String key : bindings.keySet()) {
				b.put(key, bindings.get(key));
			}
			context.setAttribute(OBJECT, engine.eval(read(path), b), ScriptContext.ENGINE_SCOPE);
			
		} catch(Exception e) {
			throw new IllegalStateException("Malformed external JVM script. Could not eval.", e);
			
		}
		
		return engine;
	}
	
	protected Bindings getBindings(ScriptEngine scriptEngine, String scriptPath) {
		Bindings bindings = scriptEngine.createBindings();
		bindings.put("logger", LoggerFactory.getLogger(engineName + "-" + new File(scriptPath).getName().replaceAll("\\.", "-")));
		
		return bindings;
	}
	
	private final String read(String path) throws IOException {
		try(RandomAccessFile aFile = new RandomAccessFile(path, "r"); FileChannel inChannel = aFile.getChannel();) {
			long fileSize = inChannel.size();
			ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
			inChannel.read(buffer);
			buffer.flip();
			
			StringBuilder script = new StringBuilder();
			for (int i = 0; i < fileSize; i++) {
				script.append((char) buffer.get());
			}
			
			return script.toString();
		}
	}
	
	protected Object invoke(String function, Object...args) throws ScriptException {
		String funcNamespace = namespace.concat(":").concat(function);
		
		String[] names = funcArgs.get(funcNamespace);
		
		if(names == null) {
			Matcher matcher = PATTERN.matcher(function);
			if(!matcher.matches()) throw new IllegalArgumentException("Unknown nashorn method invocation [function=" + function + ", args=" + args + "]");
			
			String params = matcher.group(1);
			names = params.split(",");
			
			funcArgs.put(funcNamespace, names);
			
		}
		
		if(names.length != args.length) throw new IllegalArgumentException("Wrong number of args for nashorn function invocation [function=" + function + ", args=" + args + "]");
		
		for (int i = 0; i < args.length; i++) {
			engine.put(names[i], args[i]);
		}
		
		return engine.eval(OBJECT.concat(".").concat(function));
	}
	
	protected void bindGlobal(String name, Object value) {
		engine.getContext().setAttribute(name, value, ScriptContext.GLOBAL_SCOPE);
	}
	
}
