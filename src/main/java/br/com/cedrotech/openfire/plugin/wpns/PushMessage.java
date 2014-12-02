package br.com.cedrotech.openfire.plugin.wpns;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PushMessage extends Thread {

    private static final Logger Log = LoggerFactory.getLogger(WpnsPlugin.class);

    private WpnsDBHandler dbManager;

    private String message;
    private int badge;
    private String sound;
    private Object keystore;
    private String password;
    private boolean production;
	private List<String[]> devices;

    public PushMessage(String message, int badge, String sound, Object keystore, String password, boolean production, String[] device) {
        this.message = message;
        this.badge = badge;
        this.sound = sound;
        this.keystore = keystore;
        this.password = password;
        this.production = production;
		this.devices = new ArrayList<String[]>();
		this.devices.add(device);

        dbManager = new WpnsDBHandler();
    }
	
	public PushMessage(String message, int badge, String sound, Object keystore, String password, boolean production, List<String[]> devices) {
        this.message = message;
        this.badge = badge;
        this.sound = sound;
        this.keystore = keystore;
        this.password = password;
        this.production = production;
        this.devices = devices;

        dbManager = new WpnsDBHandler();
    }

    public void run() {
        try {
        	
        	String toastMessage = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
                "<wp:Notification xmlns:wp=\"WPNotification\">"+
                	"<wp:Toast>"+
                		"<wp:Text1>%s</wp:Text1>"+
                		"<wp:Text2>%s</wp:Text2>"+
                	"</wp:Toast>"+
                "</wp:Notification>";
        	
        	for (String[] device : this.devices) {
				
        		String phoneId = device[0];
        		String phoneUrl = device[1];
        		
        		toastMessage = String.format(toastMessage, "Mensagem Recebida", message);
        		
        		byte[] notificationMessage = toastMessage.getBytes(Charset.forName("UTF-8"));
        		
        		URL obj = new URL(phoneUrl);
        		HttpURLConnection request = (HttpURLConnection) obj.openConnection();
        		request.setDoInput(true);
                request.setDoOutput(true);
        		request.setRequestMethod("POST");
        		request.setRequestProperty("Content-Type","text/xml");
        		request.setRequestProperty("Content-Length", Integer.toString(notificationMessage.length));
        		request.setRequestProperty("charset", "UTF-8");
        		request.setRequestProperty("X-WindowsPhone-Target", "toast");
        		request.setRequestProperty("X-NotificationClass", "2");
        	    request.setRequestProperty("X-MessageID", UUID.randomUUID().toString());
        	    
        	    OutputStream os = request.getOutputStream();
                os.write(notificationMessage);
                os.flush();
                
                int responseCode = request.getResponseCode();
                
                //200 = Ok
                if(responseCode == 200){
                	
                	Log.info("Push notification sent successfully to: " + phoneId);
                	
                } else {
                	
                	Log.error("Push notification response error [" + responseCode + "] to: " + phoneId);
                    dbManager.deleteDeviceToken(phoneId);

                }
                
                //Resposta
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
                
                String line;
                
                while ((line = reader.readLine()) != null) {
                	Log.info("Push notification response to " + phoneId + ": " + line);
                }
            
                reader.close();
                os.close();
                
			}
        	
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }
}
