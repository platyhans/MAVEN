/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ushacomm.mobile.manager;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Properties;

/**
 *
 * @author Ushacomm
 */
public class PropertyManager {

    private Properties configProp =new Properties();
    
    private String filePath = null;
    
    private static  PropertyManager _instance=null;
    
    
     private void loadPropertyFromFile() throws Exception
     {
         try {
        	 Charset utf8 = Charset.forName("UTF-8");
            System.out.println("config file path ="+filePath);
            Reader reader = new InputStreamReader(new FileInputStream(filePath),utf8);
            configProp.load(reader);
         } catch (Exception e) {
                throw new Exception("Could Not Load Property File : caused by :"+e.getMessage());
         }
     }
   
    public static PropertyManager getInsance() throws Exception{
        if (_instance == null) {
            _instance =new PropertyManager();
        }else{
        	if(_instance.filePath ==null)
        			throw new Exception("Configuration file path not set yet.");
        }
        return _instance;
    }
    
    public void initialise(String filePath) throws Exception{
    	this.filePath = filePath;
    	_instance.loadPropertyFromFile();
    }
    
    public String getConfig(String configLabel) {
        return configProp.getProperty("" + configLabel);
    }
     public static void main(String args[]) throws Exception{
    	 PropertyManager.getInsance().initialise(System.getProperty("user.dir")+System.getProperty("file.separator")+"config"+System.getProperty("file.separator")+"PNConfig.cfg");
         System.out.println(""+PropertyManager.getInsance().getConfig("p12Cert"));
     }
}
 