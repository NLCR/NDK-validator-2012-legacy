/**
 * 
 */
package com.logica.ndk.tm.utilities.transformation.em;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.logica.ndk.tm.cdm.BibliographicHelper;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author kovalcikm
 */
public class GetBibliographicDataFromImportImpl extends AbstractUtility {

  CDMMetsHelper metsHelper = new CDMMetsHelper();

  private static final String TYPE_CODE_MONOGRAPHY = "BK";
  private static final String TYPE_CODE_PERIODICAL = "SE";
  private static final String ISBN_CODE = "isbn";
  private static final String ISSN_CODE = "issn";
  private static final String CCNB_CODE = "ccnb";
  private static final String BARCODE_CODE = "barCode";

  public BibliographicData execute(final String rootPath) {
    checkNotNull(rootPath);

    log.info("Utility GetBibliographicDataFromImport started. Package: " + rootPath);

    File rootDir = new File(rootPath);
    if (!rootDir.exists()) {
      throw new SystemException("Package directory does not exist: " + rootDir.getAbsolutePath(), ErrorCodes.FILE_NOT_FOUND);
    }

    WildcardFileFilter filter = new WildcardFileFilter("METS_*");
    List<File> metsFiles = (List<File>) FileUtils.listFiles(rootDir, filter, FileFilterUtils.falseFileFilter());
    if (metsFiles.size() != 1) {
      throw new SystemException("There must be exactly one METS file in the root folder: " + rootDir.getAbsolutePath(), ErrorCodes.WRONG_FILES_COUNT);
    }

    File metsFile = metsFiles.get(0);

    BibliographicData biblio = new BibliographicData();

    //Title
    String title = null;
    try {
      title = BibliographicHelper.getDocumentTitle(metsFile);
    }
    catch (Exception e) {
      log.warn("Title not found in METS: " + metsFile.getAbsolutePath());
    }

    //Author
    String author = BibliographicHelper.getDocumentAuthor(metsFile, null);

    //Language
    String language = null;
    try {
      language = BibliographicHelper.getDocumentLanguage(metsFile);
    }
    catch (Exception e) {
      log.warn("Language not found in METS: " + metsFile.getAbsolutePath());
    }

    //ISBN
    String issn = null;
    try {
      issn = BibliographicHelper.getIdentifierFromMods(metsFile, ISBN_CODE);
    }
    catch (Exception e) {
      log.info("ISBN not found in METS file: " + metsFile.getAbsolutePath());
    }

    //ISSN
    String isbn = null;
    try {
      isbn = BibliographicHelper.getIdentifierFromMods(metsFile, ISSN_CODE);
    }
    catch (Exception e) {
      log.info("ISSN not found in METS file: " + metsFile.getAbsolutePath());
    }

    //CCNB
    String ccnb = null;
    try {
      ccnb = BibliographicHelper.getIdentifierFromMods(metsFile, CCNB_CODE);
    }
    catch (Exception e) {
      log.info("CCNB not found in METS file: " + metsFile.getAbsolutePath());
    }

    //BARCODE
    String barcode = null;
    try {
      barcode = BibliographicHelper.getIdentifierFromMods(metsFile, BARCODE_CODE);
    }
    catch (Exception e) {
      log.info("CCNB not found in METS file: " + metsFile.getAbsolutePath());
    }

    String documentSigla = null;
    try {
      documentSigla = BibliographicHelper.getDocumentSigla(metsFile);
    }
    catch (Exception e) {
      log.info("Sigla not found in METS: " + metsFile.getAbsolutePath());
    }
    biblio.setSigla(documentSigla);
    if (documentSigla != null && !documentSigla.isEmpty()) {
      String libraryId = TmConfig.instance().getString("utility.urnNbn.registrarCodesMapping." + documentSigla.toLowerCase());
      if (libraryId.equalsIgnoreCase("nk")) {
        libraryId = "nkcr";
      }
      biblio.setLibraryId(libraryId);
    }
    String volumeDate = BibliographicHelper.getVolumeDate(metsFile);

    if (BibliographicHelper.isNumeric(volumeDate)) {
      biblio.setVolumeDate(volumeDate);
    }
    else {
      volumeDate = null;
      log.debug("Skipping volume date as it is not a number:" + volumeDate);
    }
    
    String pressmark = BibliographicHelper.getPressmark(metsFile);
    
    

    String volumeNumber = null;
    volumeNumber = BibliographicHelper.getVolumeNumber(metsFile);

    String type = null;
    String uuid = null;
    try {
      type = BibliographicHelper.getDocumentType(metsFile);
      if (CDMMetsHelper.DOCUMENT_TYPE_PERIODICAL.equals(type)) {
        biblio.setType(TYPE_CODE_PERIODICAL);
        uuid = BibliographicHelper.getIssueUuid(metsFile);
        if (uuid == null || uuid.isEmpty()) {
          uuid = BibliographicHelper.getSupplementUuid(metsFile);
        }
      }
      else {
        biblio.setType(TYPE_CODE_MONOGRAPHY);
        uuid = BibliographicHelper.getVolumeUuid(metsFile);
      }
    }
    catch (Exception e) {
      log.info("DOcument type not found in METS: " + metsFile.getAbsolutePath());
    }
    
    File mcDir = new File(rootDir, CDMSchemaDir.MC_DIR.getDirName());
    int scanCount = 0;
    if(mcDir.exists()) {
      scanCount = mcDir.listFiles().length;
    }    
    
    biblio.setTitle(title);
    biblio.setAuthor(author);
    biblio.setLanguage(language);
    biblio.setIsbn(isbn);
    biblio.setIsbn(issn);
    biblio.setCcnb(ccnb);
    biblio.setBarCode(barcode);
    biblio.setVolumeDate(volumeDate);
    biblio.setVolumeNumber(volumeNumber);
    biblio.setPageCount(BibliographicHelper.getImageCount(metsFile));
    biblio.setDateIssued(BibliographicHelper.getDateIssued(metsFile));
    biblio.setIssueNumber(BibliographicHelper.getIssueNumber(metsFile));
    biblio.setUuid(uuid);
    biblio.setPressmark(pressmark);

    return biblio;
  }

}
