package com.logica.ndk.tm.utilities.urnnbn;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;
import org.springframework.web.client.HttpClientErrorException;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.JAXBContextPool;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.process.UrnNbnSource;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Implementation of {@link UpdateUrnNbn} WS interface.
 * 
 * @author majdaf
 */
public class UpdateUrnNbnImpl extends UrnNbnClient {

  public void assign(String registrarCode, final String cdmId) throws CDMException, DocumentException {
    assignChecks(registrarCode, cdmId);

    log.info("UpdateUrnNbn started");

    File importXmlFile = cdm.getUrnXml(cdmId);
    if (!importXmlFile.exists()) {
      throw new SystemException(importXmlFile.getPath() + " does not exist.", ErrorCodes.ERROR_WHILE_READING_FILE);
    }

    Import importedDocument = null;
    try {
      JAXBContext jaxbContext = JAXBContextPool.getContext(Import.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      importedDocument = (Import) jaxbUnmarshaller.unmarshal(importXmlFile);
    }
    catch (JAXBException e1) {
      throw new SystemException("Error while unmarshalling " + importXmlFile.getPath(), ErrorCodes.JAXB_UNMARSHALL_ERROR);
    }
    
    String urnNbn = importedDocument.getDigitalDocument().getUrnNbn().getValue();
    checkNotNull(urnNbn);

    // TODO Temporary import hack - will be removed once handled properly by WF
    String importType = cdm.getCdmProperties(cdmId).getProperty("importType");
    if (importType != null && importType.length() > 0) {
      return;
    }

    // Only update if urn from DB
    String urnNbnSource = cdm.getCdmProperties(cdmId).getProperty(URN_NBN_SOURCE_CODE);
    log.debug("URN Source: " + urnNbnSource);
    if (urnNbnSource == null || !UrnNbnSource.DB.value().equals(urnNbnSource)) {
      log.debug("URN source not DB " + urnNbnSource);
      return;
    }

    log.info("update started for URN:NBN " + urnNbn);
    log.debug("Document to be imported " + importedDocument);
    String resolverUrnNbn = null;
    try {
      resolverUrnNbn = assignUrnNbn(registrarCode, cdmId);
    }
    catch (IOException e) {
      int statusCode;
      try {
        statusCode = urlConn.getResponseCode();
      }
      catch (IOException e2) {
        log.warn("Unable to retrieve error response code from connection.");
        throw new SystemException("Unable to retrieve error response code from connection.", ErrorCodes.URNNBN_RETRIEVING_ERROR);
      }
      String message = streamToString(urlConn.getErrorStream());

      log.info("Exception in Resolver request. Status code: " + statusCode + ", Message: " + message);
      try {
        //FileUtils.write(cdm.getResolverResponseFile(cdmId), "UpdateUrnNbn resolver response: \n", true);
        retriedWrite(cdm.getResolverResponseFile(cdmId), "UpdateUrnNbn resolver response: \n", true);
        //FileUtils.write(cdm.getResolverResponseFile(cdmId), "Status code: " + statusCode + "\n", true);
        retriedWrite(cdm.getResolverResponseFile(cdmId), "Status code: " + statusCode + "\n", true);
        //FileUtils.write(cdm.getResolverResponseFile(cdmId), "Message: " + message + "\n", true);
        retriedWrite(cdm.getResolverResponseFile(cdmId), "Message: " + message + "\n", true);
      }
      catch (IOException ex) {
        log.warn("Unable to log resolver response.", ErrorCodes.ERROR_WHILE_WRITING_FILE);
      }

      int duplicityCode = TmConfig.instance().getInt("utility.urnNbn.duplicityCode");
      try {
        statusCode = urlConn.getResponseCode();
      }
      catch (IOException e2) {
        log.warn("Unable to retrieve error response code from connection.");
        throw new SystemException("Unable to retrieve error response code from connection.", ErrorCodes.URNNBN_RETRIEVING_ERROR);
      }
      if (statusCode == duplicityCode) {
        log.info("URNNBN already assigned and resolver notified");
        return;
      }

      else {
        log.error("Update URNNBN failed. Message: " + urlConn.getErrorStream(), e);
        throw new SystemException("Update URNNBN failed", ErrorCodes.URNNBN_UPDATE_FAILED);
      }
    }
    if (!urnNbn.equals(resolverUrnNbn)) {
      log.error("URN:NBN resolver returned different URN:NBN on update");
      log.error("UrnNbn: " + urnNbn + ", returned: " + resolverUrnNbn);
      throw new BusinessException("URN:NBN resolver returned different URN:NBN on update", ErrorCodes.UPDATE_URNNBN_DIFFERENT_ON_UPDATE);
    }
    return;
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedWrite(File file, CharSequence data, Boolean... params) throws IOException {
    if (params.length > 0) {
      FileUtils.write(file, data, "UTF-8", params[0].booleanValue());
    } else {
      FileUtils.write(file, data, "UTF-8");
    }
  }

}
