/**
 * 
 */
package com.logica.ndk.tm.fileServer.service;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;



import org.apache.log4j.Logger;

import com.logica.ndk.tm.fileServer.service.inputChecker.CheckerThread;

/**
 * @author brizat
 */
public class Main {

	private static final Logger LOG = Logger.getLogger(Main.class);

	
  /**
   * @param args
   */

  private static CheckerThread cThread;
  
  private static void simpleLog(String msg){
	  try {
	    PrintWriter pw=new PrintWriter(new OutputStreamWriter(new FileOutputStream("c:\\buffer\\hardLinkservice-start.log", true),"utf-8"));
	    pw.println(new Date().toString()+": "+msg);
	    pw.close();
	  }
	  catch (Exception e){
		  
	  }
  }

  public static void start(String[] args) {  
	simpleLog("Starting");  
	LOG.info("Starting thread");
    cThread = new CheckerThread(args);
    cThread.run();
    LOG.info("Finished");
  }

  public static void stop(String[] args){
	simpleLog("Sttopping");  
	LOG.info("Stopping thread");
    cThread.stopThread();
  }  
  
  public static void main(String[] args){
	  start(args);
  }
}
