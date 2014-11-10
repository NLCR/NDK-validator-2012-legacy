package com.logica.ndk.tm.fileServer.service.inputChecker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.fileServer.service.ServiceConfiguration;

public class CheckerThread implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(ServiceConfiguration.class);
  private volatile boolean stopThread = false;

  private String configFile=null;
  
  public CheckerThread(String[] args){
	  super();
	  if (args.length>0){
		  configFile=args[0];
	  }
  }
  
  @Override
  public void run() {
    while (!stopThread) {
      LOG.info("Running control");
      int secondsToWait = ServiceConfiguration.instance(configFile).getInt("sleepInSeconds");

      try {
        Thread.sleep(secondsToWait * 1000);
      }
      catch (InterruptedException e) {
        LOG.info("Interrupted!", e);
      }
      
      
      //Loading every time, because changes can be made
      //Input folders
      String[] inputDirsPaths = ServiceConfiguration.instance(configFile).getStringArray("inputDirs");

      List<File> inputDirs = new ArrayList<File>();

      for (String inputFilePath : inputDirsPaths) {
        File inputDir = new File(inputFilePath);
        if(inputDir.exists()){
          inputDirs.add(inputDir);
        }else{
          LOG.info("Input dir loader from config not exist! Skiping this one: " + inputFilePath);
        }        
      }
      
      //Drivers mapping
      String[] driversMappingArray = ServiceConfiguration.instance(configFile).getStringArray("driversMapping");

      Map<String, String> driversMapping = new HashMap<String, String>();

      for (String driversMappingPair : driversMappingArray) {
        String[] split = driversMappingPair.split("=");
        if(split.length == 2){
          driversMapping.put(split[0], split[1]);
        }else{
          LOG.error("Bad folder mapping format! Skiping this one: " + driversMappingPair);
        }
      }      
      
      Checker checker = new Checker(inputDirs, driversMapping);
      checker.check();
    }
  }

  public synchronized void stopThread(){
    stopThread = true;
  }
  
}
