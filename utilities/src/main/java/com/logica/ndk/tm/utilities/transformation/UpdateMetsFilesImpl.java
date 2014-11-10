package com.logica.ndk.tm.utilities.transformation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.w3c.dom.Document;

import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.jhove.FileDateCreatedStrategyFactory;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvHelper;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord;
import com.logica.ndk.tm.utilities.transformation.em.EmCsvRecord.EmPageType;

/**
 * Aktualizuje sa fileSec a structMap na zaklade dat v CDM, pricom je potrebny EM csv file kvoly structMap.
 * 
 * @author Rudolf Daco
 */
public class UpdateMetsFilesImpl extends AbstractUtility {
  private CDMMetsHelper metsHelper = new CDMMetsHelper();

  private static final String ALOWED_POSTFIXES = "*.xml";
  private static final String AMD_METS_FILE_PREFIX = "AMD_METS_";

  public String execute(String cdmId) {
    checkNotNull(cdmId, "cdmId must not be null");
    log.info("UpdateMetsFiles started.");
    try {
      File metsFile = cdm.getMetsFile(cdmId);
      // check EM csvm
      File emFile = cdm.getEmConfigFile(cdmId);
      if (emFile == null || emFile.exists() == false) {
        log.warn(emFile.getName() + " file not found skipping update.");
        return ResponseStatus.RESPONSE_WARNINGS;
      }

      //update is not made for scans which are "forDeletion"
      List<EmCsvRecord> emRecords = EmCsvHelper.getRecords(EmCsvHelper.getCsvReader(emFile.getAbsolutePath()));
      for (Iterator<EmCsvRecord> iter = emRecords.iterator(); iter.hasNext();) {
        EmCsvRecord record = iter.next();
        if (record.getPageType().equals(EmPageType.forDeletion)) {
          iter.remove();
        }
      }

      // Update amdSec files
      final String[] allowedPostfixes = { ALOWED_POSTFIXES };
      final IOFileFilter wildCardFilter = new WildcardFileFilter(allowedPostfixes, IOCase.INSENSITIVE);
      Collection<File> amdSecMets = FileUtils.listFiles(cdm.getAmdDir(cdmId), wildCardFilter, FileFilterUtils.trueFileFilter());
      List<EmCsvRecord> emRecord = new ArrayList<EmCsvRecord>();
      log.info("Updating amdSec files. Files: " + amdSecMets);
      int counter = 1;
      String metsFileId;
      for (File mets : amdSecMets) {
        //ziskanie ID z nazvu METS suboru
        metsFileId = (String) FilenameUtils.removeExtension(mets.getName()).subSequence(AMD_METS_FILE_PREFIX.length(), FilenameUtils.removeExtension(mets.getName()).length());

        emRecord.clear();
        for (EmCsvRecord record : emRecords) {
          if (metsFileId.equals(record.getPageId())) {
            log.info("EM csv record for amdSec file found. amdSec will be updated: " + mets.getName());
            emRecord.add(record);
            metsHelper.removeFileSec(mets);
            if (metsHelper.addFileGroups(mets, cdm, cdmId, counter, FileDateCreatedStrategyFactory.getDateCreatedStrategy(cdmId))) {
              counter++;
            }
            metsHelper.removeStructs(mets, null);
            metsHelper.addPhysicalStructMap(mets, cdm, cdmId, emRecord, false);
          }
        }
        metsHelper.prettyPrint(mets);
      }

      //Update main METS

      // update fileSec
      metsHelper.removeFileSec(metsFile);
      metsHelper.addFileGroups(metsFile, cdm, cdmId, 1);
      // update structMap
      metsHelper.removeStructs(metsFile, null);
      try {
        metsHelper.addPhysicalStructMap(metsFile, cdm, cdmId, emRecords, true);
        metsHelper.addLogicalStructMap(metsFile, cdm, cdmId);
      }
      catch (Exception e) {
        log.error("Physical struct map and logical map not created. Ex: " , e);
        throw new SystemException("Physical struct map and logical map not created. Exception:", e, ErrorCodes.UPDATE_METS_FAILED);
      }

      // format
      metsHelper.prettyPrint(metsFile);
      
      //check mets - exist, valid
      try {
        Document metsDocument = XMLHelper.parseXML(metsFile);
      }
      catch (Exception e) {
       log.error("Mets is not valid or not exist",e);
       throw e;
      }
      
      
    }

    catch (Exception e) {
      log.error("Error at UpdateMetsFilesImpl. cdmId: " + cdmId, e);
      throw new SystemException("Error at UpdateMetsFilesImpl. cdmId: " + cdmId, ErrorCodes.UPDATE_METS_FAILED);
    }
    log.info("UpdateMetsFiles finished.");
    return ResponseStatus.RESPONSE_OK;
  }
  public static void main(String[] args) {
    new UpdateMetsFilesImpl().execute("7f66a6a0-dbee-11e3-b110-005056827e51");
  }
}
