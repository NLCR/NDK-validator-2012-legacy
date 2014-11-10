package com.logica.ndk.tm.utilities.file;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.ImportFromLTPHelper;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;

/**
 * Utility renamed imported files from LTP.
 * 
 * @author brizat
 */
public class RenameImportedFilesFromLTPImpl extends AbstractUtility {

  private static String FIRST_BATCH_PREFIX = "1_";
  private static String METS_PREFIX = "METS_";

  private CDM cdm = new CDM();
  private HashMap<String, String> foldersToRename;
  private HashMap<String, String> renamePrefix;

  /**
   * @param cdmId
   * @return uuid from main mets
   */
  public String execute(String cdmId) {
    log.info("Utility RenameImportedFilesFromLTPImpl started");
    log.info(String.format("CdmId: %s", cdmId));

    initFoldersToRenameMap();
    initRenamePrefix(cdmId);

    Iterator<String> it = foldersToRename.keySet().iterator();

    File dataDir = cdm.getCdmDataDir(cdmId);

    while (it.hasNext()) {
      String dirName = it.next();
      File renamingDir = new File(dataDir.getAbsolutePath() + File.separator + dirName);
      log.info(String.format("Renaming files in dir: %s", dirName));
      if (!renamingDir.exists()) {
        log.info(String.format("Dir %s does not exist. Skipping", renamingDir.getAbsolutePath()));
        continue;
      }

      //Renaming data files in folders  

      File[] files = renamingDir.listFiles();
      for (File renamingFile : files) {
        String oldName = renamingFile.getName();        
        String onlyName = renamingFile.getName().substring(renamingFile.getName().indexOf("_")+1, renamingFile.getName().indexOf("."));
        String prefix=renamePrefix.get(onlyName)==null ? (renamePrefix.size()+1)+"_":renamePrefix.get(onlyName); 
        String targetName = oldName.replace(foldersToRename.get(dirName), prefix);

        if (targetName.contains("tif.tif")) {
          targetName = targetName.replace(".tif.tif", ".tif");
        }
        if (targetName.endsWith("jp2") && !targetName.endsWith(".tif.jp2")) {
          targetName = targetName.replace(".jp2", ".tif.jp2");
        }
        if (targetName.endsWith("txt") && !targetName.endsWith("tif.txt")) {
          targetName = targetName.replace("txt", "tif.txt");
        }
        if (targetName.endsWith("xml") && !targetName.endsWith("tif.xml")) {
          targetName = targetName.replace("xml", "tif.xml");
        }
        log.info(String.format("Renaming file %s to %s", oldName, targetName));
        if (oldName.equals(targetName)) {
          log.info(String.format("File (%s) is already renamed!)", renamingFile.getAbsolutePath()));
          continue;
        }
        File targetFile = new File(renamingDir, targetName);
        if (targetFile.exists()) {
          log.error(String.format("File with %s name exist!", targetFile.getAbsolutePath()));
          throw new BusinessException(String.format("File with %s name exist!", targetFile.getAbsolutePath()), ErrorCodes.IMPORT_LTP_RENAMIGN_FAILED);
        }
        renamingFile.renameTo(targetFile);
      }
    }

    //Rename mets
    File[] metsFiles = dataDir.listFiles(new FilenameFilter() {

      @Override
      public boolean accept(File file, String fileName) {
        return fileName.startsWith(METS_PREFIX);
      }
    });

    if (metsFiles.length != 1) {
      log.error(String.format("Wrong number (%d) of mets files, shoud by only one in folder!", metsFiles.length));
      throw new BusinessException(String.format("Wrong number (%d) of mets files, shoud by only one in folder!", metsFiles.length), ErrorCodes.IMPORT_LTP_MISSING_METS);
    }

    File metsFile = metsFiles[0];
    File targetMetsFile = new File(dataDir, METS_PREFIX + cdmId + ".xml");

    if (!targetMetsFile.getName().equals(metsFile.getName())) {

      if (targetMetsFile.exists()) {
        log.error(String.format("File with %s name is exist!", targetMetsFile.getAbsolutePath()));
        throw new BusinessException(String.format("File with %s name is exist!", targetMetsFile.getAbsolutePath()), ErrorCodes.IMPORT_LTP_RENAMIGN_FAILED);
      }

      metsFile.renameTo(targetMetsFile);
    }

    //Set importType to cdmProperties
    cdm.updateProperty(cdmId, "importType", ImportFromLTPHelper.IMPORT_TYPE);

    return ResponseStatus.RESPONSE_OK;
  }

  private void initFoldersToRenameMap() {
    foldersToRename = new HashMap<String, String>();
    foldersToRename.put(CDMSchemaDir.MC_DIR.getDirName(), "MC_");
    foldersToRename.put(CDMSchemaDir.UC_DIR.getDirName(), "UC_");
    foldersToRename.put(CDMSchemaDir.TXT_DIR.getDirName(), "TXT_");
    foldersToRename.put(CDMSchemaDir.ALTO_DIR.getDirName(), "ALTO_");
    //Also rename converted tiffs in pp and flatdata dirs
    foldersToRename.put(CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName(), "MC_");
    foldersToRename.put(CDMSchemaDir.FLAT_DATA_DIR.getDirName(), "MC_");
    //foldersToRename.put(CDMSchemaDir.AMD_DIR.getDirName(), "AMD_METS_");
  }

  private void initRenamePrefix(String cdmId)
  {
    try {
      renamePrefix = new HashMap<String, String>();
      File[] amdSecs = cdm.getAmdDir(cdmId).listFiles();
      Hashtable<Integer, String> dpiPrefix=new Hashtable<Integer, String>();
      for (int i = 0; i < amdSecs.length; i++) {
        String numerator = null;
        String denominator = null;
        org.w3c.dom.Document doc = XMLHelper.parseXML(amdSecs[i]);
        NodeList list = doc.getElementsByTagName("mix:xSamplingFrequency");
        for (int j = 0; j < list.getLength(); j++) {
          Element e = (Element) list.item(j);
          NodeList listNumerator = e.getElementsByTagName("mix:numerator");
          for (int k = 0; k < listNumerator.getLength(); k++) {
            if (numerator == null && listNumerator.item(k).getTextContent() != null || !listNumerator.item(k).getTextContent().isEmpty())
            {
              numerator = listNumerator.item(k).getTextContent();
              break;
            }
          }
          NodeList listDenominator = e.getElementsByTagName("mix:denominator");
          for (int k = 0; k < listDenominator.getLength(); k++) {
            if (denominator == null && listDenominator.item(k).getTextContent() != null || !listDenominator.item(k).getTextContent().isEmpty())
            {
              denominator = listDenominator.item(k).getTextContent();
              break;
            }
          }
          if (denominator != null && numerator != null)
            break;
        }
        int dpi=Integer.parseInt(numerator)/Integer.parseInt(denominator);
        String noAmdName = amdSecs[i].getName().replace("AMD_METS_", "");
        noAmdName=noAmdName.substring(0, (noAmdName.indexOf(".")));
        if(dpiPrefix.containsKey(dpi))
        {
          renamePrefix.put(noAmdName, dpiPrefix.get(dpi));
        }else
        {
          dpiPrefix.put(dpi,(dpiPrefix.size()+1)+"_");
          renamePrefix.put(noAmdName, dpiPrefix.get(dpi));
        }
      }
    }
    catch (Exception e) {
      log.error("Error with getting prefix: ",e);
    }
  }

  public static void main(String[] args) {
    new RenameImportedFilesFromLTPImpl().execute("bdd61540-4248-11e4-8cd0-00505682629d");
  }

}
