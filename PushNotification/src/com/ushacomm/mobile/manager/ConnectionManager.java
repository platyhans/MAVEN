package com.ushacomm.mobile.manager;

import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.util.logging.Level;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPoolMBean;

public class ConnectionManager {

	private final String POOL_NAME = "Mobile_PN_Pool"+System.currentTimeMillis();
	static java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(ConnectionManager.class.getName());
	private static ConnectionManager _instance =null;
	private HikariDataSource dataSource;
	private HikariPoolMBean poolProxy =null;
	public ConnectionManager() throws Exception{
		initialize();
	}

	public void initialize() throws Exception{
		System.out.println("initialising pool :"+POOL_NAME);
		HikariConfig config = new HikariConfig();
		config.setDriverClassName(PropertyManager.getInsance().getConfig("dbDriver"));
		config.setJdbcUrl(PropertyManager.getInsance().getConfig("dbURL"));
		config.setUsername(PropertyManager.getInsance().getConfig("dbUserId"));
		config.setPassword(PropertyManager.getInsance().getConfig("dbPassWord"));
		config.setMinimumIdle(3);
		config.setPoolName(POOL_NAME);
		config.setRegisterMbeans(true);
		dataSource = new HikariDataSource(config);
		
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		ObjectName poolName = new ObjectName("com.zaxxer.hikari:type=Pool ("+POOL_NAME+")");
		poolProxy = JMX.newMBeanProxy(mBeanServer, poolName, HikariPoolMBean.class);
		 
	}
	
	public static ConnectionManager getInstance() throws Exception{
		LOGGER.log(Level.INFO,"connection call  ...................._instance"+_instance);
		if(_instance ==null)
			_instance = new ConnectionManager();
		return _instance;
	}

	public Connection getConnection() throws Exception {
		Connection conn =null;
		try {
			conn = dataSource.getConnection();
			
			 System.out.println( "Pool Status =  (total= {"+poolProxy.getTotalConnections()+"}, "
					+ "inUse= {"+poolProxy.getActiveConnections()+"}, avail= {"+poolProxy.getIdleConnections()+"}, waiting= {"+poolProxy.getThreadsAwaitingConnection()+"})");		
			}catch (Exception e) {
				LOGGER.log(Level.SEVERE, e.getMessage(),e);		
				throw new Exception("Unable to get dB connection ");
		}
		LOGGER.log(Level.INFO, "conn :"+conn +" of pool :"+POOL_NAME);
 		return conn;

	}
	
	public void shutdown() {
		try{
			if (dataSource!=null) {
				dataSource.shutdown();
				LOGGER.log(Level.INFO,"Connectionpool :"+POOL_NAME +" has been shutdown successfully");
			}
			_instance =null;
		}catch(Exception e){
			LOGGER.log(Level.SEVERE, e.getMessage(),e);	
		}
	}
	
	
}
