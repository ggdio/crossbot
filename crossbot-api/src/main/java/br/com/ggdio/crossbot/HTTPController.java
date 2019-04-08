package br.com.ggdio.crossbot;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.client.urlconnection.HttpURLConnectionFactory;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;

import br.com.ggdio.crossbot.data.Callback;
import br.com.ggdio.crossbot.data.EditMessageResponse;
import br.com.ggdio.crossbot.data.Incoming;
import br.com.ggdio.crossbot.data.Menu;
import br.com.ggdio.crossbot.data.Request;
import br.com.ggdio.crossbot.data.Response;
import br.com.ggdio.crossbot.data.SendMessageResponse;
import br.com.ggdio.crossbot.utils.JSONUtils;
import br.com.ggdio.crossbot.utils.SSLUtilities;

/**
 * HTTP Client Wrapper
 * 
 * @author Guilherme Dio
 *
 */
class HTTPController extends Controller {
	
	private final String endpoint;
	private final Client client;
	
	private final String method;

	public HTTPController(String endpoint, String method) {
		this.endpoint = endpoint;
		this.client = initClient();
		
		this.method = method;
	}

	@Override
	public String getMethod() {
		return this.method;
	}

	@Override
	public void request(Request request) {
		handle(request);
	}

	@Override
	public void callback(Callback callback) {
		handle(callback);
	}
	
	private void handle(Incoming incoming) {
		incoming.setAttachment(null);
		
		WebResource webResource = client.resource(this.endpoint);
		Builder builder = webResource.accept("application/json");
		ClientResponse response = builder.post(ClientResponse.class, JSONUtils.toJSON(new IncomingDTO(incoming)));
		
		if (response.getStatus() != 200) {
			send(incoming, new SendMessageResponse("Failed - HTTP error code : *" + response.getStatus() + "*", true));
		}
		
		String output = response.getEntity(String.class);
		
		if(output == null || "".equals(output) || "{}".equals(output.replaceAll("\\s", ""))) {
			return;
		}
		
		ResponseDTO dto = JSONUtils.fromJSON(ResponseDTO.class, output);
		Response botResponse = dto.unwrap(method);
		
		if(botResponse instanceof SendMessageResponse) {
			send(incoming, (SendMessageResponse) botResponse);
			
		} else {
			send(incoming, (EditMessageResponse) botResponse);
			
		}
	}
	
	private Client initClient() {
		URLConnectionClientHandler clientHandler = getClientHandler();
		ClientConfig config = new DefaultClientConfig();
		config.getProperties().put("jersey.config.client.connectTimeout", 20000);
		config.getProperties().put("jersey.config.client.readTimeout", 20000);
		config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
				new com.sun.jersey.client.urlconnection.HTTPSProperties(getHostnameVerifier(), getSSLContext()));
		Client client = new Client(clientHandler, config);
		SSLUtilities.trustAllHostnames();
		SSLUtilities.trustAllHttpsCertificates();
		return client;
	}
	
	private URLConnectionClientHandler getClientHandler() {
		return new URLConnectionClientHandler(new HttpURLConnectionFactory() {
			@Override
			public HttpURLConnection getHttpURLConnection(URL url) throws IOException {
				return (HttpURLConnection) url.openConnection();
			}
		});
	}
	
	private HostnameVerifier getHostnameVerifier() {
		return new HostnameVerifier() {

			@Override
			public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
				return true;
			}
		};
	}
	
	private SSLContext getSSLContext() {
		javax.net.ssl.TrustManager x509 = new javax.net.ssl.X509TrustManager() {

			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
					throws java.security.cert.CertificateException {
				return;
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
					throws java.security.cert.CertificateException {
				return;
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};
		SSLContext ctx = null;
		try {
			ctx = SSLContext.getInstance("SSL");
			ctx.init(null, new javax.net.ssl.TrustManager[] { x509 }, null);
		} catch (java.security.GeneralSecurityException ex) {
		}
		return ctx;
	}
	
	public static class ResponseDTO { //writeonly
		private String type = "message";
		
		private boolean markdown = true;
		private List<MenuDTO> menu;
		private  String text;
		private Long chatId;
		private int menuCols = 3;
		
		public Response unwrap(String method) {
			Response response = null;
			List<Menu> defMenu = null;
			
			if(menu != null) {
				defMenu = new ArrayList<>();
				for (MenuDTO dto : menu) {
					dto.setData(method + ";" + dto.getData());
					defMenu.add(dto.unwrap());
				}
			}
			
			if("message".equals(type)) {
				response = new SendMessageResponse(text, defMenu, markdown);
				
			} else {
				response = new EditMessageResponse(text, defMenu, markdown);
				
			}
			
			response.setMenuCols(menuCols);
			response.setChatId(chatId);
			
			return response;
		}
		
		public void setType(String type) {
			this.type = type;
		}
		
		public void setMarkdown(boolean markdown) {
			this.markdown = markdown;
		}
		
		public void setMenu(List<MenuDTO> menu) {
			this.menu = menu;
		}
		
		public void setText(String text) {
			this.text = text;
		}
		
		public void setChatId(Long chatId) {
			this.chatId = chatId;
		}
		
		public void setMenuCols(int menuCols) {
			this.menuCols = menuCols;
		}
		
	}
	
	public static class MenuDTO { //writeonly
		private String text;
		private String data;
		
		public Menu unwrap() {
			return new Menu(text, data);
		}
		
		public void setText(String text) {
			this.text = text;
		}
		
		public void setData(String data) {
			this.data = data;
		}
		
		public String getData() {
			return data;
		}
		
	}
	
	public static class IncomingDTO { //readonly
		
		private String type;
		private String method;
		private Long chatId;
		private Integer messageId;
		private String data;
		private Integer userId;
		private String userName;
		private String firstName;
		private String lastName;
		private boolean privateChat;
		
		public IncomingDTO() {
			
		}
		
		public IncomingDTO(Incoming incoming) {
			this.type = "request";
			if(incoming instanceof Callback) {
				this.type = "callback";
			}
			
			this.method = incoming.getMethod();
			this.chatId = incoming.getChatId();
			this.messageId = incoming.getMessageId();
			this.data = incoming.getData();
			this.userId = incoming.getUserId();
			this.userName = incoming.getUserName();
			this.firstName = incoming.getFirstName();
			this.lastName = incoming.getLastName();
			this.privateChat = incoming.isPrivateChat();
		}
		
		public String getType() {
			return type;
		}

		public String getMethod() {
			return method;
		}

		public Long getChatId() {
			return chatId;
		}

		public Integer getMessageId() {
			return messageId;
		}

		public String getData() {
			return data;
		}

		public Integer getUserId() {
			return userId;
		}

		public String getUserName() {
			return userName;
		}

		public String getFirstName() {
			return firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public boolean isPrivateChat() {
			return privateChat;
		}
		
	}

}
