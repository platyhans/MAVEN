/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aetos.pushnotification;


/**
 *
 * @author krishna.kant
 */
public class BaseResponse {
    
    private int responseId;
	private String responseMsg;
 
	 

	public String getResponseMsg() {
		return responseMsg;
	}

	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}
 
	public int getResponseId() {
		return responseId;
	}

	public void setResponseId(int responseId) {
		this.responseId = responseId;
	}
    
}
