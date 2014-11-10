package com.logica.ndk.tm.fileServer.service.pathResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.commons.utils.cli.SysCommandExecutor;
import com.logica.ndk.tm.fileServer.service.ServiceConfiguration;

public class SymbolicLinkResolver {

  private static final Logger LOG = LoggerFactory.getLogger(ServiceConfiguration.class);
  
  public static String resolveSymbolicLink(String link) throws SymbolicLinkResolverExcetion{
    
    String cygwinHome = ServiceConfiguration.instance().getString("cygwinHome");
    String commandTemplate = ServiceConfiguration.instance().getString("readLinkCmd");
    
    SysCommandExecutor cmdExecutor = new SysCommandExecutor();
    try {
      int commnadStatus = cmdExecutor.runCommand(cygwinHome +  commandTemplate.replace("${link}", link));
      
      if(commnadStatus == 0){
        String realLink = cmdExecutor.getCommandOutput();
        realLink = realLink.replace("\r\n", "");
        LOG.info("Link is symbolic! Real path: " + realLink);
        return realLink;
      }else if(commnadStatus == 1){
        LOG.info("Link is not symbolic!: " + link);
        return link;
      }      
    }
    catch (Exception e) {
      LOG.error("Error while resolving link", e);
      throw new SymbolicLinkResolverExcetion("Error while resolving link", e);
    }
    
    return link;
  }
  
}
