/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aetos.pushnotification;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;
import com.ushacomm.mobile.manager.PropertyManager;

/**
 *
 * @author Ushacomm
 */
public class APNBroadcast {
	java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(APNBroadcast.class.getName());
    public void sendNotification(List<String> devices,String title,String msg) throws Exception {
       String path;
       ApnsService service = null;
       try{
       path =PropertyManager.getInsance().getConfig("p12CertFile");
//            path1 = URLDecoder.decode(Thread.currentThread().getContextClassLoader().getResource("com/aetos/pushnotification/"+DBConnection.iosp12cert).getPath(),"UTF-8");
//       path = System.getProperty("user.dir")+System.getProperty("file.separator")+"config"+System.getProperty("file.separator")+DBConnection.iosp12cert;
       LOGGER.log(Level.INFO,"p12 file path ="+path);
       String isProd = PropertyManager.getInsance().getConfig("isProduction");
              
        if (isProd!=null &&isProd.equalsIgnoreCase("true")) {
         service=    APNS.newService().withCert(path, "eBill").withProductionDestination().build();
        }        
        else
            service=    APNS.newService().withCert(path, "eBill").withSandboxDestination().build();
        service.start();
         String payload = APNS.newPayload().alertBody(msg).build();
         LOGGER.log(Level.INFO,"sending notification");
            Collection<? extends ApnsNotification> apnsNotification = service.push(devices, payload);
            LOGGER.log(Level.INFO," notification sent.." );
       }catch(Exception e){
    	   LOGGER.log(Level.SEVERE,"Error sending PN to Google :"+e.getMessage(),e);
       }finally{
    	   if(service!=null){
    		   service.stop();
    	   }
       }
    }
    public static void main (String[] args) throws Exception{
        APNBroadcast ab =new APNBroadcast();
        ArrayList<String> list =new ArrayList<String>();
        list.add("6bd8a2182301c0a2a9b6ba8a0fc405d931158924d4697b8614a33eebf188fa54");
        try {
            ab.sendNotification(list, "title", "msg");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
    }
}
