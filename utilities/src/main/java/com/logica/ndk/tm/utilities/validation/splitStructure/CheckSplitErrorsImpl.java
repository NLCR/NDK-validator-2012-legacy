package com.logica.ndk.tm.utilities.validation.splitStructure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.google.common.base.Joiner;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.em.ValidationViolation;
import com.logica.ndk.tm.utilities.validation.AbstractCdmValidation;
import com.logica.ndk.tm.utilities.validation.ValidationViolationsWrapper;

public class CheckSplitErrorsImpl extends AbstractCdmValidation {

  public static void main(String[] args ){
    new CheckSplitErrorsImpl().validate(args[0], args[1], Boolean.parseBoolean(args[2]));
  }

  public CheckSplitErrorsImpl() {
    // TODO Auto-generated constructor stub
  }

  public void validate(String pathToCdmIds, String outputFile, Boolean writeToCdm) {
    File sourceFile = new File(pathToCdmIds);
    ValidationViolationsWrapper result = new ValidationViolationsWrapper();
    List<String> cdmIds;

    List<String> badMixCdmIds = new ArrayList<String>();
    List<String> pageCount = new ArrayList<String>();
    List<String> renameMapping = new ArrayList<String>();

    try {
      //cdmIds = FileUtils.readLines(sourceFile);
      cdmIds = retriedReadLines(sourceFile);
    }
    catch (IOException e) {
      log.error("Error while loading source cdmId to validate.", e);
      throw new SystemException("Error while loading source cdmId to validate.", e);
    }

    for (String cdmId : cdmIds) {
      if (validateMixFiles(cdmId, result)) {
        badMixCdmIds.add(cdmId);
      }

      if (validatePageCounts(cdmId, result)) {
        pageCount.add(cdmId);
      }

      if (checkRenameMappingCsv(cdmId, result)) {
        renameMapping.add(cdmId);
      }

      if (writeToCdm) {
        try {
          File outputDir = cdm.getValidationDir(cdmId);
          if(!outputDir.exists()){
            outputDir.mkdirs();
          }
          File outPutFile = new File(outputDir, "splitValidation.txt");
          
          
          if(!outPutFile.exists()){
            outPutFile.createNewFile();
          }
          
          //FileUtils.writeStringToFile(outPutFile, "Errors: \n" + Joiner.on("\n").join(result.getViolationsList()), false);
          retriedWriteStringToFile(outPutFile, "Errors: \n" + Joiner.on("\n").join(result.getViolationsList()), false);
          //FileUtils.writeStringToFile(outPutFile, "Errors: \n" + Joiner.on("\n").join(result.getViolationsList()));
        }
        catch (IOException e) {
          log.error("Error while writing output file for cdmId: " + cdmId, e);
        }
      }
    }
    
    File outFile = new File(outputFile);
    if(!outFile.exists()){
      try {
        outFile.createNewFile();
        String info = "Number of validate cmd: " + cdmIds.size();
        info += "\nMix missing: " + badMixCdmIds.size();
        info += "\nBad page count: " + pageCount.size();
        info += "\nRenameMapping bad: " + renameMapping.size();
        
        info += "\n\n";
        info += "Mix missing\n";
        
        info += Joiner.on("\n").join(badMixCdmIds);
        
        info += "\n\nPage count\n";        
        info += Joiner.on("\n").join(pageCount);
        
        info += "\n\nRename mapping\n";        
        info += Joiner.on("\n").join(renameMapping);
        
        //FileUtils.writeStringToFile(outFile, info, false);
        retriedWriteStringToFile(outFile, info, false);
      }
      catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
  }

  private boolean validatePageCounts(String cdmId, ValidationViolationsWrapper result) {
    String[] imagesExts = { "*.tiff", "*.tif", "*.jp2", "*.jpg", "*.jpeg", "*.txt", "*.xml" };
    IOFileFilter filterFileTypes = new WildcardFileFilter(imagesExts, IOCase.INSENSITIVE);

    List<File> mcFiles = (List<File>) FileUtils.listFiles(cdm.getMasterCopyDir(cdmId), filterFileTypes, FileFilterUtils.trueFileFilter());
    List<File> ucFiles = (List<File>) FileUtils.listFiles(cdm.getUserCopyDir(cdmId), filterFileTypes, FileFilterUtils.trueFileFilter());
    List<File> altoFiles = (List<File>) FileUtils.listFiles(cdm.getAltoDir(cdmId), filterFileTypes, FileFilterUtils.trueFileFilter());
    List<File> txtFiles = (List<File>) FileUtils.listFiles(cdm.getTxtDir(cdmId), filterFileTypes, FileFilterUtils.trueFileFilter());
    List<File> previewFiles = (List<File>) FileUtils.listFiles(cdm.getPreviewDir(cdmId), filterFileTypes, FileFilterUtils.trueFileFilter());
    List<File> thFiles = (List<File>) FileUtils.listFiles(cdm.getThumbnailDir(cdmId), filterFileTypes, FileFilterUtils.trueFileFilter());

    if (!(mcFiles.size() == ucFiles.size() && mcFiles.size() == altoFiles.size() && mcFiles.size() == txtFiles.size() && mcFiles.size() == previewFiles.size() && mcFiles.size() == thFiles.size())) {
      result.add(new ValidationViolation("Page count check", String.format("Master copy: %d, userCopy: %d, alto: %d, txt: %d, preview: %d, th: %d", mcFiles.size(), ucFiles.size(), altoFiles.size(), txtFiles.size(), previewFiles.size(), thFiles.size())));
      log.error(String.format("Master copy: %s, userCopy: %s, alto: %s, txt: %s, preview: %s, th: %s", mcFiles.size(), ucFiles.size(), altoFiles.size(), txtFiles.size(), previewFiles.size(), thFiles.size()));
      return true;
    }

    return false;
  }

  private boolean checkRenameMappingCsv(String cdmId, ValidationViolationsWrapper result) {
    File[] listFiles = cdm.getPostprocessingDataDir(cdmId).listFiles();
    boolean returnResult = false;
    for (File file : listFiles) {
      String pageName = file.getName().substring(0, file.getName().indexOf("."));

      File premisFile = new File(cdm.getPremisDir(cdmId), CDMMetsHelper.PREMIS_PREFIX + CDMSchemaDir.MC_DIR.getDirName() + "_" + pageName + ".xml");
      if (!premisFile.exists()) {
        result.add(new ValidationViolation("Rename mapping validation", "Missing premiss file: " + premisFile.getAbsolutePath()));
        returnResult = true;
      }

      premisFile = new File(cdm.getPremisDir(cdmId), CDMMetsHelper.PREMIS_PREFIX + CDMSchemaDir.ALTO_DIR.getDirName() + "_" + pageName + ".xml");
      if (!premisFile.exists()) {
        result.add(new ValidationViolation("Rename mapping validation", "Missing premiss file: " + premisFile.getAbsolutePath()));
        returnResult = true;
      }
    }
    return returnResult;
  }
  
  @RetryOnFailure(attempts = 3)
  private void retriedWriteStringToFile(File file, String string, Boolean... params) throws IOException {
    if(params.length > 0) {
      FileUtils.writeStringToFile(file, string, "UTF-8", params[0].booleanValue());
        
    } else {
      FileUtils.writeStringToFile(file, string, "UTF-8");
      
    }
  }
  
  @RetryOnFailure(attempts = 3)
  private List<String> retriedReadLines(File file) throws IOException {
      return FileUtils.readLines(file, "UTF-8");
  }

}
