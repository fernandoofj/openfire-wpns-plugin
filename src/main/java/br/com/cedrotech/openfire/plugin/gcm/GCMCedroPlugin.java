package br.com.cedrotech.openfire.plugin.gcm;

import java.io.File;
import java.util.List;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;

import org.jivesoftware.util.JiveGlobals;

import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GCMCedroPlugin implements Plugin, PacketInterceptor {

    private static final Logger Log = LoggerFactory.getLogger(GCMCedroPlugin.class);

    private InterceptorManager interceptorManager;
    private GCMCedroDBHandler dbManager;

    public GCMCedroPlugin() {
        interceptorManager = InterceptorManager.getInstance();
        dbManager = new GCMCedroDBHandler();
    }
	
	public static String keystorePath() {
        return "./keystore.p12";
    }

    public void setPassword(String password) {
        JiveGlobals.setProperty("plugin.wpns.password", password);
    }

    public String getPassword() {
        return JiveGlobals.getProperty("plugin.wpns.password", "");
    }

    public void setBadge(String badge) {
        JiveGlobals.setProperty("plugin.wpns.badge", badge);
    }

    public int getBadge() {
        return Integer.parseInt(JiveGlobals.getProperty("plugin.wpns.badge", "1"));
    }

    public void setSound(String sound) {
        JiveGlobals.setProperty("plugin.wpns.sound", sound);
    }

    public String getSound() {
        return JiveGlobals.getProperty("plugin.wpns.sound", "default");
    }

    public void setProduction(String production) {
        JiveGlobals.setProperty("plugin.wpns.production", production);
    }

    public boolean getProduction() {
        return Boolean.parseBoolean(JiveGlobals.getProperty("plugin.wpns.badge", "false"));
    }

    public void initializePlugin(PluginManager pManager, File pluginDirectory) {
        interceptorManager.addInterceptor(this);

        IQHandler myHandler = new GCMCedroIQHandler();
        IQRouter iqRouter = XMPPServer.getInstance().getIQRouter();
        iqRouter.addHandler(myHandler);
    }

    public void destroyPlugin() {
        interceptorManager.removeInterceptor(this);
    }

    public void interceptPacket(Packet packet, Session session, boolean read, boolean processed) throws PacketRejectedException {

        if (isValidTargetPacket(packet, read, processed)) {
            Packet original = packet;

            if(original instanceof Message) {
                Message receivedMessage = (Message) original;

                if (receivedMessage.getType() == Message.Type.chat) {
                    JID targetJID = receivedMessage.getTo();

                    String user = targetJID.getNode();
                    String body = receivedMessage.getBody();
                    String payloadString = user + ": " + body;

                    String[] deviceToken = dbManager.getDeviceToken(targetJID);
                    if (deviceToken == null || deviceToken.length != 2) return;

                    new PushMessage(payloadString, getBadge(), getSound(), GCMCedroPlugin.keystorePath(), getPassword(), getProduction(), deviceToken).start();
                } else if (receivedMessage.getType() == Message.Type.groupchat) {
                    JID sourceJID = receivedMessage.getFrom();
                    JID targetJID = receivedMessage.getTo();

                    String user = sourceJID.getNode();
                    String body = receivedMessage.getBody();
                    String payloadString = user + ": " + body;
                    String roomName = targetJID.getNode();

                    List<String[]> deviceTokens = dbManager.getDeviceTokens(roomName);
                    if (deviceTokens == null || deviceTokens.isEmpty()) return;

                    new PushMessage(payloadString, getBadge(), getSound(), GCMCedroPlugin.keystorePath(), getPassword(), getProduction(), deviceTokens).start();
                }
            }
        }
    }

    private boolean isValidTargetPacket(Packet packet, boolean read, boolean processed) {
        return  !processed && read && packet instanceof Message;
    }
}
