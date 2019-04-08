package br.com.ggdio.crossbot.data;

public class Callback extends Incoming {
	
	public Callback() {
		
	}
	
	public String[] getArgs() {
		return getData().split(";")[1].split("#");
	}
	
}