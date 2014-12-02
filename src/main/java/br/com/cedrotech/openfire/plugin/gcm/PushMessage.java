package br.com.cedrotech.openfire.plugin.gcm;

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

    private static final Logger Log = LoggerFactory.getLogger(GCMCedroPlugin.class);

    private GCMCedroDBHandler dbManager;

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

        dbManager = new GCMCedroDBHandler();
    }
	
	public PushMessage(String message, int badge, String sound, Object keystore, String password, boolean production, List<String[]> devices) {
        this.message = message;
        this.badge = badge;
        this.sound = sound;
        this.keystore = keystore;
        this.password = password;
        this.production = production;
        this.devices = devices;

        dbManager = new GCMCedroDBHandler();
    }

    public void run() {
        try {
        	
        	for (String[] device : this.devices) {
				
        		String phoneId = device[0];
        		String phoneUrl = device[1];
                
                Content content = new Content();
                
                content.addRegId(phoneUrl);
                content.createData("Mensagem Recebida", message);
                
                int responseCode = Notify.post(content);
                
                //200 = Ok
                if(responseCode == 200){
                	
                	Log.info("Push notification sent successfully to: " + phoneId);
                	
                } else {
                	
                	Log.error("Push notification response error [" + responseCode + "] to: " + phoneId);
                    dbManager.deleteDeviceToken(phoneId);

                }
                
			}
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }
}
