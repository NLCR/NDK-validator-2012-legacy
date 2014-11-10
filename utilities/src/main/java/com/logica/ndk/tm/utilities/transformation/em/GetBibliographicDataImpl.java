package com.logica.ndk.tm.utilities.transformation.em;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Strings;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author ondrusekl
 */
public class GetBibliographicDataImpl extends AbstractUtility {

  private final CDM cdm = new CDM();
  CDMMetsHelper metsHelper = new CDMMetsHelper();

  private static final String RECORD_IDENTIFIER_XPATH = "//mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/mods:mods/mods:recordInfo/mods:recordIdentifier";
  
  public static final String TYPE_CODE_MONOGRAPHY = "BK";
  public static final String TYPE_CODE_PERIODICAL = "SE";
  public static final String ISBN_CODE = "isbn";
  public static final String ISSN_CODE = "issn";
  public static final String CCNB_CODE = "ccnb";
  public static final String BARCODE_CODE = "barCode";

  public BibliographicData execute(final String cdmId) {
    checkNotNull(cdmId, "cdmId must not be null");

    log.info("Getting bibliographic data for cdm " + cdmId);

    BibliographicData biblio = new BibliographicData();

    try {
      
      String titleUuid = metsHelper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_TITLE, "uuid");
      if(!Strings.isNullOrEmpty(titleUuid)){
        log.info("title uuid: " + titleUuid);
        biblio.setTitleUUID(titleUuid);
      }
      String volumeUuid = metsHelper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_VOLUME, "uuid");
      if(!Strings.isNullOrEmpty(volumeUuid)){
        log.info("volume uuid: " + volumeUuid);
        biblio.setVolumeUUID(volumeUuid);
      }
      String issueUuid = metsHelper.getIdentifierFromMods(cdm, cdmId, CDMMetsHelper.DMDSEC_ID_MODS_ISSUE, "uuid");
      if(!Strings.isNullOrEmpty(issueUuid)){
        log.info("issue uuid: " + issueUuid);
        biblio.setIssueUUID(issueUuid);
      }
      String urnnbn = metsHelper.getIdentifierFromMods(cdm, cdmId, "urnnbn");
      if (!Strings.isNullOrEmpty(urnnbn)){
        biblio.setUrnnbn(urnnbn);
      }
      biblio.setTitle(metsHelper.getDocumentTitle(cdm, cdmId)); 
      biblio.setAuthor(metsHelper.getDocumentAuthor(cdm, cdmId, null));
      biblio.setLanguage(metsHelper.getDocumentLanguage(cdm, cdmId));
      biblio.setIsbn(metsHelper.getIdentifierFromMods(cdm, cdmId, ISBN_CODE));
      biblio.setIssn(metsHelper.getIssnIdentifierFromMods(cdm, cdmId));
      biblio.setCcnb(metsHelper.getIdentifierFromMods(cdm, cdmId, CCNB_CODE));
      biblio.setRecordIdentifier(metsHelper.getValueFormMets(RECORD_IDENTIFIER_XPATH, cdm, cdmId));
      String documentSigla = metsHelper.getDocumentSigla(cdm, cdmId);
      biblio.setSigla(documentSigla);
      if(documentSigla != null && !documentSigla.isEmpty()){
        String libraryId = TmConfig.instance().getString("utility.urnNbn.registrarCodesMapping." + documentSigla.toLowerCase());
        if(libraryId.equalsIgnoreCase("nk")){
          libraryId = "nkcr";
        }
        biblio.setLibraryId(libraryId);
      }
      //      biblio.setUrnnbn(metsHelper.get)
      String volumeDate = metsHelper.getVolumeDate(cdm, cdmId);
      
      if (isNumeric(volumeDate)) {
    	  biblio.setVolumeDate(volumeDate);
      } else {
    	  log.debug("Skipping volume date as it is not a number:" + volumeDate);
      }
      
      biblio.setVolumeNumber(metsHelper.getVolumeNumber(cdm, cdmId));
      biblio.setPageCount(metsHelper.getImageCount(cdm, cdmId));
      biblio.setDateIssued(metsHelper.getDateIssued(cdm, cdmId));
      biblio.setIssueNumber(metsHelper.getIssueNumber(cdm, cdmId));      
      String type = metsHelper.getDocumentType(cdmId);
      
      biblio.setBarCode(metsHelper.getIdentifierFromMods(cdm, cdmId, BARCODE_CODE));
      if (CDMMetsHelper.DOCUMENT_TYPE_PERIODICAL.equals(type)) {
        biblio.setType(TYPE_CODE_PERIODICAL);
      }
      else {
        biblio.setType(TYPE_CODE_MONOGRAPHY);
      }

      biblio.setPartNumber(metsHelper.getVolumeNumber(cdm, cdmId)); // This is not a bug. VolumeNumber and PartNumber are identical.
      biblio.setPartName(metsHelper.getPartName(cdm, cdmId));
    }
    catch (Exception e) {
        log.error("Error while retrieving bibliographic data from mets.", e);
      throw new SystemException("Error while retrieving bibliographic data from mets.",ErrorCodes.METS_GET_BIBLIO_FAILED);
    }
    log.info(biblio.toString());
    return biblio;

  }

  public static boolean isNumeric(String str)
  {
    try
    {
      double d = Short.parseShort(str);
    }
    catch (NumberFormatException nfe)
    {
      return false;
    }
    return true;
  }
  public static void main(String[] args) {
    new GetBibliographicDataImpl().execute("99f73da0-64f1-11e3-a484-001018b5eb5c");
  }
}
