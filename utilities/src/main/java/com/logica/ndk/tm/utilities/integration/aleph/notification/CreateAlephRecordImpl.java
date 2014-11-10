package com.logica.ndk.tm.utilities.integration.aleph.notification;

import au.edu.apsr.mtk.base.METSException;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMException;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.integration.aleph.notification.request.*;
import com.logica.ndk.tm.utilities.urnnbn.K4Helper;

import org.apache.commons.io.FileUtils;
import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * Utilita na vytvorenie Aleph notification request pre dane CDM. Kazde CDM pocas spracovania by malo pouzit tuto
 * utilitu. Vytvoreny request sa kopiruje do spolocneho centralneho adresara ktory potom sleduje utilita
 * NotifyAlephImpl. Utilita NotifyAlephImpl pouziva pocas spracovania koncept presunu suborov medzi pracovnymi sub-
 * adresarmi a preto aj tato utilta presuva subor do sub-adresa incoming v centralnom adresari.
 * 
 * @author Rudolf Daco
 */
public class CreateAlephRecordImpl extends AbstractUtility {
  private static final String CDM_PROPERTY_REFERENCEDCDMS = "referencedCdms";
  private final static String NKCR = "nkcr";
  private final String URN_NBN_CODE = "urnnbn";
  private final String UUID_CODE = "uuid";
  private CDMMetsHelper metsHelper = new CDMMetsHelper();

  public String execute(String cdmId, String docNumber, String locality) throws SystemException {
    log.info("cdmId parameter: " + cdmId);
    try {
      generateAlephNotificationRequest(cdmId, docNumber, locality);
      return ResponseStatus.RESPONSE_OK;
    } catch (Exception e) {
      log.error(e.getMessage(),e);
      throw new SystemException(e);
    }
  }

  /**
   * Vytvorime XML request pre tento CDM pricom sa hladaju aj referencovane CDM ktore sa do tohto XML zahrnu. Ak
   * referencovane CDM nie su tak ide o monografiu a XML bude mat iba jeden child zaznam ktory zodpoveda mastru.
   *
   * @param cdmId
   * @throws METSException 
   * @throws ParserConfigurationException 
   * @throws IOException 
   * @throws SAXException 
   * @throws CDMException 
   */
  private void generateAlephNotificationRequest(String cdmId, String docNumber, String locality) throws CDMException, SAXException, IOException, ParserConfigurationException, METSException {
    URNNBNNotification notification = new URNNBNNotification();
    notification.setBatchNumber(getBatchNumber());
    Records records = new Records();
    Record record = new Record();
    record.setDocNumber(docNumber);
    record.setBarCode(getBarCode(cdmId));


    boolean multipartMonograph = isMultipartMonograph(cdmId);
    record.setUrl(getUrl(cdmId, locality, false, multipartMonograph));
    record.setUuid(getMainUuid(cdmId, multipartMonograph));

    // digitalRecords for CDMs
    DigitalRecords digitalRecords = new DigitalRecords();
    List<String> referencedCdms = getReferencedCdms(cdmId);
    if (referencedCdms == null || referencedCdms.size() == 0) { // If package has no children, PROBABLY it is Monograph
      String documentType = metsHelper.getDocumentType(cdmId);
      boolean issue = false;
      if (documentType.equalsIgnoreCase(CDMMetsHelper.DOCUMENT_TYPE_PERIODICAL)) { // Handle periodicals with only one issue
        issue = true;
      }
      // generate only 1 digitalRecord for this master CDM
      digitalRecords.getDigitalRecord().add(getDigitalRecord(cdmId, issue, locality));
    }
    else {
      // generate digitalRecords for every referenced CDM
      for (String referencedCdm : referencedCdms) {
        digitalRecords.getDigitalRecord().add(getDigitalRecord(referencedCdm, true, locality));
      }
    }
    record.setDigitalRecords(digitalRecords);
    records.getRecord().add(record);
    notification.setRecords(records);
    File notificationFile = writeToFile(notification, cdmId, locality);
    copyToNotifyDir(notificationFile);
  }

  /**
   * Prekopirujeme vytvoreny XML do adresata ktory sleduje job na vytvaranie a posielanie XML requestov do Aleph.
   *
   * @param file
   */
  private void copyToNotifyDir(File file) {
    File outDir = AlephNotificationHelper.getRequestIncomingDir();
    try {
      //FileUtils.copyFileToDirectory(file, outDir);
      retriedCopyFileToDirectory(file, outDir);
    }
    catch (IOException e) {
      log.error(format("Error copy aleph notification file %s to directory %s", file, outDir), e);
      throw new SystemException(format("Error copy aleph notification file %s to directory %s", file, outDir), e);
    }
  }

  /**
   * Zapiseme vytvoreny XML do suboru.
   *
   * @param notification
   * @param cdmId
   * @return
   */
  private File writeToFile(URNNBNNotification notification, String cdmId, String locality) {
    File outDir = new CDM().getAlephNotificationDir(cdmId);
    if (!outDir.exists()) {
      if (outDir.mkdirs() == false) {
        log.error("Error at creating directory " + outDir);
        throw new SystemException("Error at creating directory " + outDir);
      }
    }
    String prefix;
    if(locality.equalsIgnoreCase(NKCR)){
      prefix = AlephNotificationHelper.NKCR_FILE_PREFIX;
    }else{
      prefix = AlephNotificationHelper.MZK_FILE_PREFIX;
    }
    File targetFile = new File(outDir, prefix + "_" + cdmId + ".xml");
    AlephNotificationHelper.writeToFile(notification, targetFile);
    return targetFile;
  }

  private DigitalRecord getDigitalRecord(String cdmId, boolean issue, String locality) {
    DigitalRecord digitalRecord = new DigitalRecord();
    digitalRecord.setUrnnbn(getUrnnbn(cdmId));
    if (issue) {
      digitalRecord.setUuid(getIssueUuid(cdmId));
    }
    else {
      digitalRecord.setUuid(getMainUuid(cdmId, false));
    }
    digitalRecord.setUrl(getUrl(cdmId, locality, issue, false));
    return digitalRecord;
  }

  private String getUrnnbn(String cdmId) {
    checkNotNull(cdmId, "cdmId must not be null");
    try {
    	return new CDMMetsHelper().getIdentifierFromMods(new CDM(), cdmId, URN_NBN_CODE);
    }
    catch (CDMException e) {
      log.error("Error at getUrnnbn from METS file.", e);
      throw new AlephNotificationException("Error at getUrnnbn from METS file.", e, ErrorCodes.CREATE_ALEPH_RECORD_GET_URNNBN_FROM_METS);
    }
    catch (DocumentException e) {
      log.error("Error at getUrnnbn from METS file.", e);
      throw new AlephNotificationException("Error at getUrnnbn from METS file.", e, ErrorCodes.CREATE_ALEPH_RECORD_GET_URNNBN_FROM_METS);
    }
  }

  /**
   * Ziskame CDM id referencovanych CDM.
   *
   * @param cdmId
   * @return
   */
  private List<String> getReferencedCdms(String cdmId) {
    List<String> list = new ArrayList<String>();
    Properties cdmProperties = new CDM().getCdmProperties(cdmId);
    String referencedCdms = cdmProperties.getProperty(CDM_PROPERTY_REFERENCEDCDMS);
    if (referencedCdms != null) {
      String[] referencedCdmList = referencedCdms.split(",");
      for (int i = 0; i < referencedCdmList.length; i++) {
        list.add(referencedCdmList[i]);
      }
    }
    return list;
  }

  private String getUrl(String cdmId, String locality, boolean issue, boolean multipartMonography) {
    String localityTemp = locality;
    if (locality.equalsIgnoreCase(NKCR)) {
      localityTemp = "nk";
    }

    return K4Helper.getK4Url(issue ? getIssueUuid(cdmId) : getMainUuid(cdmId, multipartMonography), localityTemp);
  }

  private String getIssueUuid(String cdmId) {
    //TODO - now we always generating uuid for issues and using it as cdmId;
    return cdmId;
    /*try {
      return new CDMMetsHelper().getModsNodeValue(xPathExpression, new CDM(), cdmId);
    }
    catch (CDMException e) {
      log.error("Error at getUrnnbn from METS file.", e);
      throw new AlephNotificationException("Error at getUuid from METS file.", e, ErrorCodes.CREATE_ALEPH_RECORD_GET_UUID_FROM_METS);
    }
    catch (DocumentException e) {
      log.error("Error at getUrnnbn from METS file.", e);
      throw new AlephNotificationException("Error at getUuid from METS file.", e, ErrorCodes.CREATE_ALEPH_RECORD_GET_UUID_FROM_METS);
    }*/
  }

  private String getMainUuid(String cdmId, boolean multipartMonography) {
    try {
       	if (multipartMonography) {
            final String result = new CDMMetsHelper().getIdentifierFromModsTitle(cdm, cdmId, UUID_CODE);
            return result;
    	} else {
            final String result = new CDMMetsHelper().getIdentifierFromMods(cdm, cdmId, UUID_CODE);
            return result;
    	}
    }
    catch (CDMException e) {
      log.error("Error at getUrnnbn from METS file.", e);
      throw new AlephNotificationException("Error at getUuid from METS file.", e, ErrorCodes.CREATE_ALEPH_RECORD_GET_UUID_FROM_METS);
    }
    catch (DocumentException e) {
      log.error("Error at getUrnnbn from METS file.", e);
      throw new AlephNotificationException("Error at getUuid from METS file.", e, ErrorCodes.CREATE_ALEPH_RECORD_GET_UUID_FROM_METS);
    }
  }

  private String getBarCode(String cdmId) {
      return cdm.getCdmProperties(cdmId).getProperty("barCode");
  }

  private String getBatchNumber() {
    return "empty";
  }

  private boolean isMultipartMonograph(String cdmId) {
    return new CDMMetsHelper().isMultiPartMonograph(cdmId);
  }
  
}
