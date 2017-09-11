/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aetos.pushnotification;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.ushacomm.mobile.manager.PropertyManager;

public class GCMBroadcast {

//"AIzaSyCiOY-WVR7gtKaxTokaQbBTo9GjHsP8-hY";//"AIzaSyALdh0CH49HxaUzNS684M0erQuAD_sDOjI";

 /*   public GCMBroadcast() {
        super();
//        set the deviceList
    }*/

	java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(GCMBroadcast.class.getName());

	protected void processRequest(List<String> devices, String title, String msg) throws Exception {
		String SENDER_ID = PropertyManager.getInsance().getConfig("androidSenderId");

		List<List<String>> batchList = new ArrayList<List<String>>();
		int batchSize = 999;
		for (int i = 0; i < devices.size(); i = i + batchSize) {
			batchList.add(new ArrayList<String>(devices.subList(i, Math.min(devices.size(), i + batchSize))));
		}

		for (List<String> deviceList : batchList) {
			Message.Builder messageBuilder = new Message.Builder();
			messageBuilder.addData("msg", msg);
			messageBuilder.addData("title", title);
			Message message = messageBuilder.build();

			MulticastResult multicastResult;
			multicastResult = new Sender(SENDER_ID).send(message, deviceList, 5);
			List<Result> results = multicastResult.getResults();
			// analyze the results
			String canonicalIds = "", errorIds = "";
			int successCount = 0;
			for (int i = 0; i < deviceList.size(); i++) {
				String regId = deviceList.get(i);
				Result result = results.get(i);
				String messageId = result.getMessageId();
				if (messageId != null) {

					String canonicalRegId = result.getCanonicalRegistrationId();
					if (canonicalRegId != null) {
						// same device has more than on registration id: update
						// it
						// LOGGER.log(Level.INFO,"canonicalRegId " +
						// canonicalRegId);
						canonicalIds += canonicalRegId + ", ";
					} else {
						/*
						 * LOGGER.log(Level.INFO,
						 * "Succesfully sent message to device: " + regId +
						 * "; messageId = " + messageId);
						 */
						successCount++;
					}
				} else {
					String error = result.getErrorCodeName();
					// LOGGER.log(Level.INFO,"Error sending message to " + regId
					// + ": " + error);
					errorIds += regId + " : Caused by" + error + "\n";
				}
			}
			LOGGER.log(Level.INFO, "Android Notification :Notification sent to " + successCount + " devices" +"out of "+deviceList.size());
			if (canonicalIds.length() > 0)
				LOGGER.log(Level.INFO, "Android Notification :canonical Reg Ids " + canonicalIds);
			if (errorIds.length() > 0)
				LOGGER.log(Level.INFO, "Android Notification :erronious Reg Ids " + errorIds);
		}

	}

}
