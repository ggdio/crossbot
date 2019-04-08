package br.com.ggdio.crossbot.data;

import java.io.File;
import java.io.InputStream;

/**
 * Response object that sends a document
 * 
 * @author Guilherme Dio
 *
 */
public class SendPhotoResponse extends Response {
	
	private final File file;
	
	private final String name;
	private final InputStream stream;

	public SendPhotoResponse(File file) {
		super(null, null, false);
		
		this.file = file;
		this.name = null;
		this.stream = null;
	}
	
	public SendPhotoResponse(String name, InputStream stream) {
		super(null, null, false);
		
		this.file = null;
		this.name = name;
		this.stream = stream;
	}
	
	public File getFile() {
		return file;
	}
	
	public String getName() {
		return name;
	}
	
	public InputStream getStream() {
		return stream;
	}

}