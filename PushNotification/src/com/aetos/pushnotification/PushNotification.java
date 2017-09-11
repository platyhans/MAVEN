/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aetos.pushnotification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import com.ushacomm.mobile.manager.ConnectionManager;
import com.ushacomm.mobile.manager.PropertyManager;

/**
 *
 * @author Ushacomm
 */
public class PushNotification {
	
	java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ConnectionManager.class.getName());
	
	public static enum NOTIFT_TO_TYPE{
		NOTIFY_ALL(1),
		NOTIFY_USER(2),
		NOTIFY_ACCOUNT(3);
		
		int notifType;
		private NOTIFT_TO_TYPE(int notifType) {
	        this.notifType = notifType;
	    }
	}
	private enum DEVICETYPE{
		ANDROID,IOS
	}
	/**
	 * 
	 * @param configFilePath - PushNotification configuration file (PNConfig.cfg) path including  file name .
	 * @throws Exception if file could not loaded
	 */
	public PushNotification(String configFilePath) throws Exception {		
		System.out.println("initialisation call ....................");
		PropertyManager.getInsance().initialise(configFilePath);
 	}
	
    /**
     * @param args the command line arguments
     * @throws Exception 
     */
    public static void main(String[] args) throws  Exception{
    	PushNotification notification1=null,notification = null;
    	try{
	    	notification  = new PushNotification(System.getProperty("user.dir")+System.getProperty("file.separator")+"config"+System.getProperty("file.separator")+"PNConfig.cfg");
	    	BaseResponse response =notification.sendNotification("CUST_56",NOTIFT_TO_TYPE.NOTIFY_USER, "General", "test", "testmsg");
	        System.out.print("return:");
	        System.out.println("isSuccess ="+response.getResponseId());
	        System.out.println("msg ="+response.getResponseMsg());
    	}finally{
    		notification.freeAllResources();
    	}
    	try{
	        notification1  = new PushNotification(System.getProperty("user.dir")+System.getProperty("file.separator")+"config"+System.getProperty("file.separator")+"PNConfig.cfg");
	        BaseResponse response =notification1.sendNotification(null,NOTIFT_TO_TYPE.NOTIFY_ALL, "General", "test", "testmsgAcc");
	        System.out.print("return:");
	        System.out.println("isSuccess ="+response.getResponseId());
	        System.out.println("msg ="+response.getResponseMsg());
    	}finally{
    		notification1.freeAllResources();
    	}
    }
    
    /**
     * function sends the notification 
     * @param notifyTo - this is contains value of NOTIFT_TO_TYPE , in case of NOTIFT_TO_TYPE.NOTIFY_ALL is will be null
     * @param key - enum of type NOTIFT_TO_TYPE
     * @param notifType - can be NORMAL or any notifType
     * @param title - notification title
     * @param msg - notification message
     * @return BaseResponse which has responseId(int) and responseMsg(String)
     */
    public BaseResponse sendNotification( String notifyTo, NOTIFT_TO_TYPE key, String notifType, String title, String msg) {
    	return sendNotification(notifyTo,key,notifType,title,msg,null);
    }
    
    /**
     * function sends the notification 
     * @param notifyTo - this is contains value of NOTIFT_TO_TYPE , in case of NOTIFT_TO_TYPE.NOTIFY_ALL is will be null
     * @param key - enum of type NOTIFT_TO_TYPE
     * @param notifType - can be NORMAL or any notifType
     * @param title - notification title
     * @param msg - notification message
     * @param deviceId - device id to which notification is to be send,  this is being used only for payment notification
     * @return BaseResponse which has responseId(int) and responseMsg(String)
     */
    public BaseResponse sendNotification( String notifyTo, NOTIFT_TO_TYPE key, String notifType, String title, String msg,String deviceId) {
        BaseResponse srvRes = new BaseResponse();
        try {
        // get list of notication id for the user
        List<String> androidDevices = deviceId==null?getDeviceList(notifyTo, key, DEVICETYPE.ANDROID):getDeviceListBydid(deviceId ,DEVICETYPE.ANDROID);
        List<String> iosDevices = deviceId==null?getDeviceList(notifyTo, key, DEVICETYPE.IOS):getDeviceListBydid(deviceId,DEVICETYPE.IOS);
        if (iosDevices.size() < 1 && androidDevices.size() < 1) {
        	srvRes.setResponseId(-1);
            srvRes.setResponseMsg("No device is listed for the user");
            return srvRes;
        }
        
     // Insert notification details to ateos_notification table
        if (!insertIntoNotificationTable(notifyTo, key,notifType, title, msg)) {
            throw new Exception("System is unavailable , please try later");
        }

        //sending notification android devices
//        srvRes.setResponseMsg("Notification sent to ");
        if(androidDevices.size() > 0) {
            GCMBroadcast gcmBroadcast = new GCMBroadcast();
            try {
                gcmBroadcast.processRequest(androidDevices, title, msg);
                srvRes.setResponseId(1);
                srvRes.setResponseMsg("Notification sent to android devices ");
            } catch (Exception ex) {
            	LOGGER.log(Level.SEVERE,"Error sending PN to Google :"+ex.getMessage(),ex);
                ex.printStackTrace();
                srvRes.setResponseId(-1);
                srvRes.setResponseMsg("Error sending PN to Google");
            }
        }
        
        //sending notification ios devices
        if(iosDevices.size() > 0) {
            APNBroadcast apnBroadcast = new APNBroadcast();
            try {
                apnBroadcast.sendNotification(iosDevices, title, msg);
                if(srvRes.getResponseId()==1)
                    srvRes.setResponseMsg(srvRes.getResponseMsg()+", iOS devices ");
                else{
                	srvRes.setResponseId(1);
                    srvRes.setResponseMsg("Notification sent to iOS devices ");
                }
            } catch (Exception ex) {
            	LOGGER.log(Level.SEVERE,"Error sending PN to Apple : "+ex.getMessage(),ex);
                ex.printStackTrace();
                if(srvRes.getResponseId()!=1){
                    srvRes.setResponseMsg(srvRes.getResponseMsg()+" "+ex.getMessage());
                    srvRes.setResponseId(-1);
                }
            }
        }
        } catch (Exception e) {
        	LOGGER.log(Level.SEVERE, e.getMessage(),e);
            srvRes.setResponseId(-1);
            srvRes.setResponseMsg(e.getMessage());
            return srvRes;
        }
        return srvRes;
    }
    
    
    public void freeAllResources(){
    	try {
			ConnectionManager.getInstance().shutdown();
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE,e.getMessage(),e);
		}
    }

	private boolean insertIntoNotificationTable( String notifyTo, NOTIFT_TO_TYPE key,String notifType, String title, String msg) throws Exception{
        boolean isInserted = false;
        PreparedStatement statement = null;
        Connection conn =null;
        String query = null;
        
        try {

            if (key.notifType ==NOTIFT_TO_TYPE.NOTIFY_ALL.notifType) {
            	notifyTo ="[ALL]";
                query = PropertyManager.getInsance().getConfig("insertAllNotifQuery");
            } else {
            	String keyStr = (key.notifType==NOTIFT_TO_TYPE.NOTIFY_ACCOUNT.notifType)?"BILLABLEACCOUNT":"USERID";
                query = PropertyManager.getInsance().getConfig("insertNotifQuery")+" "+ keyStr+" = ?";
            }
            conn =ConnectionManager.getInstance().getConnection();
            statement = conn.prepareStatement(query);
            System.out.println("inside insertIntoNotificationTable: insert QUERY ="+query);
             
             statement.setString(1, notifType);
             statement.setString(2, title);
             statement.setString(3, msg);
             statement.setString(4, notifyTo);
             System.out.println("inside insertIntoNotificationTable: insert QUERYValue ="+notifyTo+"/"+notifType+"/"+title+"/"+msg);
            int retVal = statement.executeUpdate();
             System.out.println("inside insertIntoNotificationTable:after insert retVal =" + retVal);
            if (retVal > 0) {
                isInserted = true;
            }

        } catch (Exception ex) {
        	LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
            isInserted = false;

        } finally {
            try{
            if(statement!=null)
                statement.close();
            if(conn!=null)
                conn.close();
            }catch(Exception ex){
                ex.printStackTrace();
            }


        }
        return isInserted;
    }
    
    private LinkedList<String> getDeviceList(String notifyTo, NOTIFT_TO_TYPE key, DEVICETYPE deviceType) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        String query = null;
        Connection conn =null;
        
        
        LinkedList<String> devices = new LinkedList<String>();
        try {
        	
        	if (key.notifType == NOTIFT_TO_TYPE.NOTIFY_ALL.notifType) {
                query = PropertyManager.getInsance().getConfig("getAllDeviceListQuery");
            } else {
            	String keyStr = (key.notifType==NOTIFT_TO_TYPE.NOTIFY_ACCOUNT.notifType)?"BILLABLEACCOUNT":"USERID";
                query  = PropertyManager.getInsance().getConfig("getDeviceListQuery")+"(" +PropertyManager.getInsance().getConfig("selectUserid")+" "+ keyStr +" = ?" +")";
            }
             conn = ConnectionManager.getInstance().getConnection();
             System.out.println("inside getDeviceList: QUERY ="+query);
             statement = conn.prepareStatement(query);
            statement.setString(1, deviceType.toString()); 
            if(!(key.notifType == NOTIFT_TO_TYPE.NOTIFY_ALL.notifType))
                statement.setString(2, notifyTo); 
            System.out.println("inside getDeviceList: QUERYValue ="+deviceType+"/"+notifyTo);
            rs = statement.executeQuery();
            while (rs.next()) {
                devices.add(rs.getString("NOTIFICATIONID"));
            }
        } catch (Exception ex) {
        	LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
        } finally {
            try{
            if(statement!=null)
                statement.close();
            if(rs!=null)
                rs.close();
            if(conn!=null)
                conn.close();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        return devices;
    }
    
    private LinkedList<String> getDeviceListBydid(String deviceId,DEVICETYPE deviceType) {
    	PreparedStatement statement = null;
        ResultSet rs = null;
        String query = null;
        Connection conn =null;
        LinkedList<String> devices = new LinkedList<String>();
        try {
        	
             query = PropertyManager.getInsance().getConfig("getDeviceByDid");
             conn = ConnectionManager.getInstance().getConnection();
             System.out.println("inside getDeviceList: QUERY ="+query);
             statement = conn.prepareStatement(query);
            statement.setString(1, deviceType.toString());
            statement.setString(2, deviceId); 
            System.out.println("inside getDeviceList: QUERYValue ="+deviceType+"/"+deviceId);
            rs = statement.executeQuery();
            while (rs.next()) {
                devices.add(rs.getString("NOTIFICATIONID"));
            }
        } catch (Exception ex) {
        	LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
        } finally {
            try{
            if(statement!=null)
                statement.close();
            if(rs!=null)
                rs.close();
            if(conn!=null)
                conn.close();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
        return devices;
	}
}
