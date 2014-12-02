package br.com.cedrotech.openfire.plugin.gcm;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.Gson;


public class Notify {
	
	static String gcmURL = "https://android.googleapis.com/gcm/send";
	static String gcmAuthToken = "AIzaSyAixnjwuIzu79F1Fk3WCZzZSVUKwDD3c1Q"; //mypush - fernando
	
	public static int post(Content content){
		 
        try{
 
	        // 1. URL
	        URL url = new URL(gcmURL);
	 
	        // 2. Open connection
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	 
	        // 3. Specify POST method
	        conn.setRequestMethod("POST");
	 
	        // 4. Set the headers
	        conn.setRequestProperty("Content-Type", "application/json");
	        conn.setRequestProperty("Authorization", "key=" + gcmAuthToken);
	 
	        conn.setDoOutput(true);
 
            // 5. Add JSON data into POST request body 
 
            // 5.2 Get connection output stream
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            
            Gson mGson = new Gson();
            
            String objJson = mGson.toJson(content);
            
            wr.writeBytes(objJson);
 
            // 5.4 Send the request
            wr.flush();
 
            // 5.5 close
            wr.close();
 
            // 6. Get the response
            int responseCode = conn.getResponseCode();
            
            System.out.println("responseCode: " + responseCode);
            
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
 
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            
            in.close();
 
            // 7. Print result
            System.out.println(response.toString());
            
            return responseCode;
 
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
		return 0;
    }
}