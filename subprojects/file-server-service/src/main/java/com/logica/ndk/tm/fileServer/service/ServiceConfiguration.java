package com.logica.ndk.tm.fileServer.service;

import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Appender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.log4j.SimpleLayout;

public class ServiceConfiguration {

	public static final String LOG_PATTERN = "%d{yyyy.MM.dd HH:mm:ss,SSS} %5p [%t] %c - %m%n";

	private static final Logger LOG = Logger.getLogger(ServiceConfiguration.class);

	private static XMLConfiguration xmlConfig;

	public static synchronized Configuration instance(String cfgFile) {
		if ( xmlConfig==null) initInstance(cfgFile);
		return xmlConfig;
	}

	public static synchronized Configuration instance() {
		if ( xmlConfig==null) 
			throw new IllegalStateException("Configuration instance not initialized.") ;
		return xmlConfig;
	}
	
	

	private static void initInstance(String cfgFile) {
		try {
			if (cfgFile==null) {
				LOG.error("no configuration file name given - exiting");
				throw new NullPointerException("no config, no work - too dangerous to start with silly defaults");
			}
			xmlConfig = new XMLConfiguration(cfgFile);
			xmlConfig.clearErrorListeners();
			xmlConfig.setReloadingStrategy(new FileChangedReloadingStrategy());
			//LOG.info(ConfigurationUtils.toString(xmlConfig));
			LOG.info("Configuration initialized from:"+cfgFile);
			initLogging(cfgFile);
		} catch (ConfigurationException e) {
			LOG.error("Failed to load config", e);
		}
	}

	/*
	 <appender name="R" class="org.apache.log4j.DailyRollingFileAppender">
		<param name="datePattern" value="'-'yyyyMMdd" />
		<param name="File" value="c:/NDK/hardLinkService/app.log" />
		<param name="Threshold" value="ALL" />
		<layout class="org.apache.log4j.PatternLayout">
			<param name="ConversionPattern" value="%d{yyyy.MM.dd HH:mm:ss,SSS} %5p [%t] %c - %m%n" />
		</layout>
	</appender> 
	*/
	private static void initLogging(String cfgFileName) {
		try {
			File cfgFile = new File(cfgFileName);
			File baseDir = cfgFile.getParentFile();
			File logDir = new File(baseDir, "logs");
			File logFile = new File(logDir, cfgFile.getName() + ".log");
			if (!logDir.exists())
				logDir.mkdirs();

			DailyRollingFileAppender ap = new DailyRollingFileAppender();
			ap.setName("hardlinkservice-log");
			ap.setDatePattern("'-'yyyyMMdd");
			ap.setThreshold(Level.ALL);
			ap.setFile(logFile.getAbsolutePath(),true,false,0);
			ap.activateOptions();
			
			PatternLayout l=new PatternLayout(LOG_PATTERN);
			ap.setLayout(l);
			Logger.getRootLogger().addAppender(ap);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
