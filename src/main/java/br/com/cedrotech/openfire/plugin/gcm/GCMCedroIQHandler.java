package br.com.cedrotech.openfire.plugin.gcm;

import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.handler.IQHandler;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class GCMCedroIQHandler extends IQHandler {

	private static final Logger Log = LoggerFactory.getLogger(GCMCedroPlugin.class);

    private IQHandlerInfo info;

    private GCMCedroDBHandler dbManager;

    public GCMCedroIQHandler() {
        super("GCMCedro IQ Handler");
        info = new IQHandlerInfo("query","urn:xmpp:gcmcedro");
        dbManager = new GCMCedroDBHandler();
    }

    @Override
    public IQHandlerInfo getInfo() {
        return info;
    }

    @Override
    public IQ handleIQ(IQ packet) throws UnauthorizedException {
        IQ result = IQ.createResultIQ(packet);

        JID from = packet.getFrom();
        IQ.Type type = packet.getType();
		
        if (type.equals(IQ.Type.get)) {
		
			Log.info("IQ Get received: [JID] " + from.toBareJID());
		
            Element responseElement = DocumentHelper.createElement(QName.get("query", "urn:xmpp:gcmcedro"));
			
			String[] deviceTokens = dbManager.getDeviceToken(from);
            responseElement.addElement("phoneId").setText(deviceTokens[0]);
			responseElement.addElement("phoneUrl").setText(deviceTokens[1]);

            result.setChildElement(responseElement);
        } else if (type.equals(IQ.Type.set)) {
            Element receivedPacket = packet.getElement();

            String phoneId = receivedPacket.element("query").elementText("phoneId");
			String phoneUrl = receivedPacket.element("query").elementText("phoneUrl");
			
			Log.info("IQ Set received: [JID] " + from.toBareJID() + " [PhoneId] " + phoneId + " [PhoneUrl] " + phoneUrl);
			
            if (phoneId != null && phoneId.length() > 0 && phoneUrl != null && phoneUrl.length() > 0) {
			
				Log.debug("IQ Set received [not null data] : [JID] " + from.toBareJID() + " [PhoneId] " + phoneId + " [PhoneUrl] " + phoneUrl);
			
                if (dbManager.insertDeviceToken(from, phoneId, phoneUrl)) {
                    Element responseElement = DocumentHelper.createElement(QName.get("query", "urn:xmpp:gcmcedro"));
                    
                    String[] deviceTokens = dbManager.getDeviceToken(from);
					responseElement.addElement("phoneId").setText(deviceTokens[0]);
					responseElement.addElement("phoneUrl").setText(deviceTokens[1]);

                    result.setChildElement(responseElement);
                } else {
                    result.setChildElement(packet.getChildElement().createCopy());
                    result.setError(PacketError.Condition.internal_server_error);
                }
            } else {
                result.setChildElement(packet.getChildElement().createCopy());
                result.setError(PacketError.Condition.not_acceptable);
            }
        } else {
            result.setChildElement(packet.getChildElement().createCopy());
            result.setError(PacketError.Condition.not_acceptable);
        }

        return result;
    }
}
