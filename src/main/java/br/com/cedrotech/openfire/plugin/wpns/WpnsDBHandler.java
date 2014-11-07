package br.com.cedrotech.openfire.plugin.wpns;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.ArrayList;

import org.jivesoftware.database.DbConnectionManager;

import org.xmpp.packet.JID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WpnsDBHandler {

    private static final Logger Log = LoggerFactory.getLogger(WpnsPlugin.class);

    private static final String LOAD_TOKEN = "SELECT phoneAppID, phoneUrl FROM ofWPNS WHERE JID=?";
    private static final String INSERT_TOKEN = "INSERT INTO ofWPNS wp (wp.JID, wp.phoneAppID, wp.phoneUrl) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE wp.phoneAppID = ?, wp.phoneUrl = ?";
    private static final String DELETE_TOKEN = "DELETE FROM ofWPNS WHERE phoneAppID = ?";
    private static final String LOAD_TOKENS = "SELECT phoneAppID, phoneUrl FROM ofWPNS LEFT JOIN ofMucMember ON ofWPNS.JID = ofMucMember.jid LEFT JOIN ofMucRoom ON ofMucMember.roomID = ofMucRoom.roomID WHERE ofMucRoom.name = ?";

    public boolean insertDeviceToken(JID targetJID, String phoneId, String phoneUrl) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean isCompleted = false;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(INSERT_TOKEN);
            pstmt.setString(1, targetJID.toBareJID());
            pstmt.setString(2, phoneId);
            pstmt.setString(3, phoneUrl);
            pstmt.executeUpdate();
            pstmt.close();

            isCompleted = true;
        } catch (SQLException sqle) {
            Log.error(sqle.getMessage(), sqle);
            isCompleted = false;
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
        return isCompleted;
    }

    public boolean deleteDeviceToken(String phoneAppID) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean isCompleted = false;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(DELETE_TOKEN);
            pstmt.setString(1, phoneAppID);
            pstmt.executeUpdate();
            pstmt.close();

            isCompleted = true;
        } catch (SQLException sqle) {
            Log.error(sqle.getMessage(), sqle);
            isCompleted = false;
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
        return isCompleted;
    }

    public String[] getDeviceToken(JID targetJID) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String[] returnToken = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(LOAD_TOKEN);
            pstmt.setString(1, targetJID.toBareJID());
            rs = pstmt.executeQuery();
            if (rs.next()) {
            	String phoneId = rs.getString(1);
				String phoneUrl = rs.getString(2);
				
				String [] token = { phoneId, phoneUrl };
				
				returnToken = token;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException sqle) {
            Log.error(sqle.getMessage(), sqle);
            returnToken = null;
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
        return returnToken;
    }

    public List<String[]> getDeviceTokens(String roomName) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        List<String[]> returnToken = new ArrayList<String[]>();
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(LOAD_TOKENS);
            pstmt.setString(1, roomName);
            rs = pstmt.executeQuery();
            while (rs.next()) {
            	String phoneId = rs.getString(1);
				String phoneUrl = rs.getString(2);
				
				String [] token = { phoneId, phoneUrl };
				
				returnToken.add(token);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException sqle) {
            Log.error(sqle.getMessage(), sqle);
        } finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
        
        return returnToken;
    }
}
