package com.logica.ndk.tm.fileServer.service.links;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.commons.utils.cli.SysCommandExecutor;
import com.logica.ndk.tm.fileServer.service.ServiceConfiguration;
import com.logica.ndk.tm.fileServer.service.errorWriter.ErrorWriter;
import com.logica.ndk.tm.fileServer.service.input.InputFile;
import com.logica.ndk.tm.fileServer.service.input.LinkToCreate;
import com.logica.ndk.tm.fileServer.service.input.Loader;
import com.logica.ndk.tm.fileServer.service.pathResolver.SymbolicLinkResolver;
import com.logica.ndk.tm.fileServer.service.pathResolver.SymbolicLinkResolverExcetion;

/**
 * @author brizat
 *
 */
public class LinksCreator {

  private static final Logger LOG = LoggerFactory.getLogger(ServiceConfiguration.class);
  
  private InputFile xmlInput;
  private String linkToCdm;
  private Map<String, String> driversMapping;
  private File inFile;
  
  public LinksCreator(File inFile, Map<String, String> driversMapping) throws FileNotFoundException, JAXBException, URISyntaxException, SymbolicLinkResolverExcetion, LinksCreatorException, BadFileServerException {
    this.driversMapping = driversMapping;
    this.inFile = inFile;
    xmlInput = Loader.load(inFile.getAbsolutePath());
    linkToCdm = resolveLinkToCdm();
  }

  public Integer createLinks() throws LinksCreatorException{
    int numberOfCreatedLinks = 0;
    SysCommandExecutor cmdExecutor = new SysCommandExecutor();
    
    String cmdTemplate = ServiceConfiguration.instance().getString("command");
    
    List<LinkToCreate> links = xmlInput.getLinks();
    
    if(links == null){
      return 0;
    }
    for (LinkToCreate linkToCreate : links) {
      File target = new File(transformPath(linkToCreate.getTarget()));
      if(target.exists()){
        LOG.info("Target link exist! " + target.getAbsolutePath());
        continue;
      }
      
      String cmd = cmdTemplate.replace("${target}", target.getAbsolutePath()).replace("${source}", transformPath(linkToCreate.getSource()));
      int exitStatus = 0;
      try {
        exitStatus = cmdExecutor.runCommand(cmd);
        LOG.info("Exit status of command: " + exitStatus);
        if(exitStatus != 0){
          LOG.error("Exception while executing command!. Exit status: " + exitStatus + " Error: " + cmdExecutor.getCommandError());
          throw new LinksCreatorException("Exceotion while executing command!. Exit status: " + exitStatus + "Error: " + cmdExecutor.getCommandError());
        }
        numberOfCreatedLinks++;
      }
      catch (Exception e) {
        LOG.error("Exception while executing command " + cmd + "!.\n Exit status: " + exitStatus, e);
        throw new LinksCreatorException("Exceotion while executing command " + cmd + "!.\n Exit status: " + exitStatus + "Error: " + cmdExecutor.getCommandError(), e);
      }

    }
    
    return numberOfCreatedLinks;
  }
  
  private String resolveLinkToCdm() throws SymbolicLinkResolverExcetion, LinksCreatorException, BadFileServerException{
    String resolvedLink = SymbolicLinkResolver.resolveSymbolicLink(xmlInput.getRootLink());
    
    Set<Entry<String, String>> entrySet = driversMapping.entrySet();
    String resolvedLinkLow = resolvedLink.toLowerCase().replace("\\", "/");
    
    for (Entry<String, String> driverMapping : entrySet) {
      String driverMappinglow = driverMapping.getKey().toLowerCase();
      if(resolvedLinkLow.startsWith(driverMappinglow)){
        File cdm = new File(resolvedLinkLow.replace(driverMappinglow, driverMapping.getValue()));
        if(!cdm.exists()){
          LOG.error("Root path not exist on this file server: " + cdm.getAbsolutePath());
          throw new BadFileServerException("Root path not exist on this file server: " + cdm.getAbsolutePath());
          //ErrorWriter.writeErrorLog(new LinksCreatorException("Root path not exist on this file server: " + cdm.getAbsolutePath()), inFile);
        }
        return cdm.getAbsolutePath();
      }
    }
    
    LOG.error("Unable to resolve driver mapping for root cdm link: " + resolvedLink);
    throw new LinksCreatorException("Unable to resolve driver mapping for root cdm link: " + resolvedLink);
  }
  
  
  private String transformPath(String path){
    StringBuilder stringBuilder = new StringBuilder(linkToCdm);    
    if(!linkToCdm.endsWith(File.separator)){
      stringBuilder.append(File.separator);
    }
    return stringBuilder.append(path).toString();
  }

  public String getCdmPath(){
    return linkToCdm;
  }
  
}
