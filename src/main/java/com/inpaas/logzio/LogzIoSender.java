package com.inpaas.logzio;

import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import io.logz.logback.LogzioLogbackAppender;


public class LogzIoSender {
	
	private static final String LZIO_APPENDER_NAME = "LogzIo-Appender";
	
	public static synchronized void attach(Map<String, String> props) {
		
		final String logzioToken = props.get("token");
		final String logzioType = props.get("type");
		final String logzioUrl = props.get("url");
		
		if (logzioToken == null || logzioToken.length() == 0) 
			throw new RuntimeException("error.logzio.appender.tokenmissing");

		if (logzioType == null || logzioType.length() == 0) 
			throw new RuntimeException("error.logzio.appender.typemissing");

		if (logzioUrl == null || logzioUrl.length() == 0) 
			throw new RuntimeException("error.logzio.appender.urlmissing");
		
		// Obtain logger context
		final ch.qos.logback.classic.LoggerContext lc = 
				(ch.qos.logback.classic.LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
		
		final ch.qos.logback.classic.Logger rootLogger = 
				lc.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		
		final Appender<ILoggingEvent> defappender = (Appender<ILoggingEvent>) rootLogger.getAppender(LZIO_APPENDER_NAME);
		boolean attached = false;
		
		if (defappender == null) {
			attached = false;
			
		} else if (defappender instanceof LogzioLogbackAppender) {
			LogzioLogbackAppender llappender = (LogzioLogbackAppender) defappender;
			llappender.setToken(logzioToken);
			llappender.setLogzioType(logzioType);
			llappender.setLogzioUrl(logzioUrl);
			
			attached = true;
		} else {			
			dettachCommit(rootLogger, defappender);
			
			attached = false;
		}
		
		if (!attached) 
			attachCommit(rootLogger, logzioToken, logzioType, logzioUrl);
		
	}
	
	public static void dettach() {
		// Obtain logger context
		final ch.qos.logback.classic.LoggerContext lc = 
				(ch.qos.logback.classic.LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
		
		final ch.qos.logback.classic.Logger rootLogger = 
				lc.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		
		final Appender<ILoggingEvent> defappender = (Appender<ILoggingEvent>) rootLogger.getAppender(LZIO_APPENDER_NAME);
		if (defappender != null) dettachCommit(rootLogger, defappender);
		
	}
	
	private static Appender<ILoggingEvent> attachCommit(final ch.qos.logback.classic.Logger rootLogger, final String logzioToken, final String logzioType, final String logzioUrl) {
		final LogzioLogbackAppender llappender = new LogzioLogbackAppender();
	
		llappender.setName(LZIO_APPENDER_NAME);
		llappender.setToken(logzioToken);
		llappender.setLogzioType(logzioType);
		llappender.setLogzioUrl(logzioUrl);		
		llappender.setContext(rootLogger.getLoggerContext());
		
		final ThresholdFilter logFilter = new ThresholdFilter();
	    logFilter.setLevel(Level.WARN.levelStr);
	    logFilter.start();	    
	    llappender.addFilter(logFilter);				
	    
	    rootLogger.addAppender(llappender);	
	    llappender.start();
	    
	    return llappender;	    
	}
	
	private static boolean dettachCommit(final ch.qos.logback.classic.Logger rootLogger, final Appender<ILoggingEvent> defappender) {
		defappender.stop();
		
		return rootLogger.detachAppender(defappender);
	}

	
	
}
