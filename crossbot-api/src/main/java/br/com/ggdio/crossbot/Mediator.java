package br.com.ggdio.crossbot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContext;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.objects.Document;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

import br.com.ggdio.crossbot.data.Callback;
import br.com.ggdio.crossbot.data.Incoming;
import br.com.ggdio.crossbot.data.Request;
import br.com.ggdio.crossbot.utils.Semaphore;

class Mediator extends TelegramLongPollingBot {
	
	private static final Logger LOG = LoggerFactory.getLogger(Mediator.class);
	
	private static final Map<String, Mediator> mediators = new HashMap<>();
	
	private static String proxyHost;
	private static int proxyPort;
	
	private final Map<String, Controller> controllers = new HashMap<>();
	
	private final Map<Integer, Controller> expectedReplies = new ConcurrentHashMap<>();
	
	private final List<String> allowed = new ArrayList<>();
	
	private final ExecutorService executor;
	
	private final String botUsername;
	private final String botToken;
	
	private final Semaphore semaphore = new Semaphore(false);
	
	private Mediator(String botUsername, String botToken) {
		this(botUsername, botToken, ApiContext.getInstance(DefaultBotOptions.class), 10);
	}
	
	private Mediator(String botUsername, String botToken, DefaultBotOptions options, int threads) {
		super(options);
		
		this.executor = Executors.newFixedThreadPool(threads);
		
		this.botUsername = botUsername;
		this.botToken = botToken;
	}
	
	static final Mediator getInstance(String botUsername) {
		return mediators.get(botUsername);
	}
	
	static final Mediator getInstance(String botUsername, String botToken) {
		return Mediator.getInstance(botUsername, botToken, 10);
	}
	
	static final Mediator getInstance(String botUsername, String botToken, int threads) {
		Mediator instance = mediators.get(botUsername);
		if(instance == null) {
			instance = new Mediator(botUsername, botToken, resolveOptions(), threads);
			mediators.put(botUsername, instance);
		}
		
		return instance;
	}
	
	public void allow(String user) {
		allowed.add(user);
	}
	
	private static final DefaultBotOptions resolveOptions() {
		DefaultBotOptions options = ApiContext.getInstance(DefaultBotOptions.class);
		proxyHost = System.getProperty("http.proxyHost", null);
		proxyPort = Integer.parseInt(System.getProperty("http.proxyPort", "0"));
		System.out.println("http.proxyHost=" + proxyHost);
		System.out.println("http.proxyPort=" + proxyPort);
		if(proxyHost != null && proxyPort > 0) {
			System.out.println("Setting proxy...");
			RequestConfig rc = RequestConfig.custom()
				.setProxy(new HttpHost(proxyHost, proxyPort))
				.setSocketTimeout(10000)
				.setConnectTimeout(10000)
				.setConnectionRequestTimeout(10000)
				.build();
			
			options.setRequestConfig(rc);
		}
		
		return options;
	}
	
	public void expectReplyFrom(Integer userId, Controller controller) {
		expectedReplies.put(userId, controller);
	}
	
	@Override
	public String getBotUsername() {
		return botUsername;
	}
	
	@Override
	public String getBotToken() {
		return botToken;
	}
	
	void clearControllers() {
		ExecutorService executor = Executors.newFixedThreadPool(controllers.size());
		for (String k : controllers.keySet()) {
			executor.submit(() -> controllers.get(k).stop());
		}
		controllers.clear();
	}
	
	void registerController(Controller controller) {
		try {
			controller.setBotCommander(this);
			controllers.put(controller.getMethod(), controller);
			
		} catch(Exception e) {
			e.printStackTrace();
			
		}
	}

	public void onUpdateReceived(Update update) {
		this.semaphore.proceed();
		
		Incoming incoming = null;
		
		Message message = null;
		User from = null;
		String data = null;
		
		if (update.hasCallbackQuery()) {
			message = update.getCallbackQuery().getMessage();
			if(message == null) return;
			
			from = update.getCallbackQuery().getFrom();
			
			data = update.getCallbackQuery().getData();
			if(data == null) return;
			
			incoming = new Callback();
			incoming.setLocale(from.getLanguageCode());
			incoming.setMethod(data.split(";")[0]);
			
			
		} else {
			message = update.getMessage();
			if(message == null) return;
			
			from = message.getFrom();
			
			incoming = new Request();
			incoming.setLocale(from.getLanguageCode());
			
			if(message.isReply()) {
				if(!expectedReplies.containsKey(from.getId())) {
					LOG.warn("Unexpected reply from User-ID {}", from.getId());
					return;
				}
				
				data = expectedReplies.remove(from.getId()).getMethod();
				
				if(message.hasDocument()) {
					Document document = message.getDocument();
					GetFile getFile = new GetFile();
					getFile.setFileId(document.getFileId());
					try {
						File file = downloadTelegramFile(execute(getFile));
						incoming.setAttachment(file);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				message = message.getReplyToMessage();
				
			} else {
				data = message.getText();
				
			}
			
			if(data == null) return;
			
			if(data.endsWith("@".concat(getBotUsername()))) {
				int index = data.indexOf("@");
				incoming.setMethod(data.substring(0, index));
				
			} else {
				incoming.setMethod(data);
				
			}
		}
		
		Controller controller = controllers.get(incoming.getMethod());
		if(controller == null) {
			LOG.warn("Unknown controller {}", incoming.getMethod());
			return;
		}
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("Incoming request at controller {}", incoming.getMethod());
		}
		
		incoming.setChatId(message.getChatId());
		incoming.setMessageId(message.getMessageId());
		incoming.setData(data);
		
		incoming.setUserId(from.getId());
		incoming.setUserName(from.getUserName());
		incoming.setFirstName(from.getFirstName());
		incoming.setLastName(from.getLastName());
		
		if(!allowed.isEmpty()) {
			if(incoming.getUserName() == null) return;
			if(!allowed.contains(incoming.getUserName())) return;
		}
		
		incoming.setPrivateChat(message.getChat().isUserChat());
		
		if(incoming.isPrivateChat() && !controller.acceptPrivateMessage(incoming.getUserId(), incoming.getUserName())) {
			LOG.warn("User [id={}, name={}] is trying to communicate with BOT in private.", incoming.getUserId(), incoming.getPersonName());
			return;
		}
		
		if(!controller.acceptIncoming(incoming.getUserId(), incoming.getUserName())) {
			LOG.warn("User [id={}, name={}] is trying to communicate with a forbidden method [id={}] for it's ID.", incoming.getUserId(), incoming.getPersonName(), incoming.getMethod());
			return;
		}
		
		if(incoming instanceof Request) {
			final Request request = (Request) incoming;
			executor.submit(() -> controller.request(request));
		} else {
			final Callback callback = (Callback) incoming;
			executor.submit(() -> controller.callback(callback));
		}

	}
	
	@SuppressWarnings("deprecation")
	public final java.io.File downloadTelegramFile(org.telegram.telegrambots.api.objects.File file) throws TelegramApiException {
        String url = file.getFileUrl(getBotToken());
        String tempFileName = file.getFileId();
        try {
        	 File output = File.createTempFile(tempFileName, ".tmp");
        	 URL remote = new URL(url);
        	 HttpURLConnection connection = null;
        	 if(proxyHost != null && proxyPort > 0) {
        		 Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        		 connection = (HttpURLConnection) remote.openConnection(proxy);
        		 
        	 } else {
        		 connection = (HttpURLConnection) remote.openConnection();
        		 
        	 }
        	 connection.connect();
        	 
        	 InputStream is = connection.getInputStream();
        	 FileUtils.copyInputStreamToFile(is, output);
        	 IOUtils.closeQuietly(is);
             
        	 try {
        		 connection.disconnect();
        	 } catch(Throwable t) {}
        	 
             return output;
             
        } catch (MalformedURLException e) {
            throw new TelegramApiException("Wrong url for file: " + url);
            
        } catch (IOException e) {
            throw new TelegramApiRequestException("Error downloading the file", e);
            
        }
    }
	
	public void start() {
		this.semaphore.setGreenSignal();
	}
	
	public void halt() {
		this.semaphore.setRedSignal();
	}
	
}