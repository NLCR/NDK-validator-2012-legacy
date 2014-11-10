package com.logica.ndk.tm.utilities.file;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.ImportFromLTPHelper;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.cdm.FormatMigrationHelper;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.process.ParamMap;
import com.logica.ndk.tm.process.ParamMapItem;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.OperationResult;
import com.logica.ndk.tm.utilities.OperationResult.State;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.exiff.ExifException;
import com.logica.ndk.tm.utilities.exiff.ExifService;
import com.logica.ndk.tm.utilities.jhove.JHoveHelper;
import com.logica.ndk.tm.utilities.jhove.JhoveService;
import com.logica.ndk.tm.utilities.jhove.JhoveService.OutputType;
import com.logica.ndk.tm.utilities.jhove.KduHelper;
import com.logica.ndk.tm.utilities.jhove.MixEnvBean;
import com.logica.ndk.tm.utilities.jhove.MixHelper;
import com.logica.ndk.tm.utilities.kakadu.KakaduException;
import com.logica.ndk.tm.utilities.kakadu.KakaduService;
import com.logica.ndk.tm.utilities.premis.PremisCsvHelper;
import com.logica.ndk.tm.utilities.premis.PremisCsvRecord;
import com.logica.ndk.tm.utilities.transformation.JhoveException;
import com.logica.ndk.tm.utilities.transformation.format.migration.FormatMigrationScan;
import com.logica.ndk.tm.utilities.transformation.format.migration.FormatMigrationScans;
import com.logica.ndk.tm.utilities.transformation.format.migration.FormatMigrationScansHelper;

import org.apache.commons.lang.StringUtils;

public class FileCharacterizationImpl extends AbstractUtility {

  private static String POST_PROC_DIR_NAME = "postprocessingData";
  private static String MIX_FILE_SUFFIX = ".mix";
  private static String XML_MIX_FILE_SUFFIX = ".xml.mix";
  private static final String OBJECT_IDENTIFIER_TYPE = TmConfig.instance().getString("utility.convertToJpeg2000.output.objIdentifierType");
  private static final String MC_CSV = CDMSchemaDir.MC_DIR.getDirName() + ".csv";
  private static final String ORIGINAL_DATA_CSV = CDMSchemaDir.ORIGINAL_DATA.getDirName() + ".csv";
  private static final String FLAT_DATA_CSV = CDMSchemaDir.FLAT_DATA_DIR.getDirName() + ".csv";
  private static final String MASTER_COPY_TIFF_DATA_CSV = CDMSchemaDir.MASTER_COPY_TIFF_DIR.getDirName() + ".csv";

  private static final String MIX_XSLT_PATH_FORMAT_MIGRATION = "com/logica/ndk/tm/utilities/jhove/jhoveXmlToMixXmlFormatMigration.xslt";
  private static final String MIX_XSLT_PATH_EXIF = "com/logica/ndk/tm/utilities/exif/exifXmlToMixXml.xslt";
  private static final String MIX_XSLT_PATH_JHOVE2 = "com/logica/ndk/tm/utilities/jhove/jhoveXmlToMixXml2.xslt";

  public static final String[] mixExt = { "mix" };

  private static final String FLAT_DATA_MIX_SUFFIX = ".xml.mix";
  final String[] cfgExts = TmConfig.instance().getStringArray("utility.fileChar.imgExtensions");
  // JHove for every image
  private OperationResult jhoveResult;
  FormatMigrationHelper migrationHelper = new FormatMigrationHelper();

  // TODO [rda] - optimalization - jhove can consume whole folder - do not
  // call it for each file separately - but it generatesw only one fle
  // 10 files in cycle = 35s, 10 files as one folder = 10s
  public String execute(final String cdmId, final String sourcePath, final String targetPath, final ParamMap parameters) throws FileCharacterizationException {
    log.info("FileCharacterization execute started. sourcepath:" + sourcePath + ", targetPath" + targetPath);
    log.debug("All files count in sourcePath: " + new File(sourcePath).listFiles().length);
    checkNotNull(sourcePath);
    final OperationResult result = new OperationResult();
    final File sourceDir = new File(sourcePath);
    File targetDir = null;
    if (!isEmpty(targetPath)) {
      targetDir = new File(targetPath);
    }
    else if (cdmId != null) {
      targetDir = new File(createWorkspacePath(cdmId, sourceDir.getName()));
    }
    else {
      log.error("targetPath and cdmId can't be null!");
      throw new FileCharacterizationException("targetPath and cdmId can't be null!");
    }
    if (sourceDir.exists() == false || sourceDir.isDirectory() == false) {
      log.error("sourceDir '" + sourcePath + "' doesn't exist!");
      throw new FileCharacterizationException("sourceDir '" + sourcePath + "' doesn't exist!");
    }
    int repeatCount = 0;
    do {
    	if (targetDir.exists() == false) {
    		if (targetDir.mkdirs() == false) {
    			log.error("error at creating target directory " + targetPath + " !");
    			throw new FileCharacterizationException("error at creating target directory " + targetPath + " !");
    		}
    	}
    	repeatCount++;
    } while (repeatCount < 3);

    final boolean cfgRecursive = TmConfig.instance().getBoolean("utility.fileChar.recursive", false);
    final IOFileFilter fileFilter = new WildcardFileFilter(cfgExts, IOCase.INSENSITIVE);
    IOFileFilter dirFilter = cfgRecursive ? FileFilterUtils.trueFileFilter() : FileFilterUtils.falseFileFilter();

    boolean formatMigration = migrationHelper.isFormatMigration(cdm.getCdmProperties(cdmId).getProperty("importType"));
    if (formatMigration) {
      log.info("Characterization for format migration.");
      dirFilter = FileFilterUtils.trueFileFilter();
    }

    final Collection<File> listFiles = FileUtils.listFiles(sourceDir, fileFilter, dirFilter);
    log.debug("Files to characterization count: " + listFiles.size());
    for (final File file : listFiles) {
      log.debug("File to characterize: " + file.getAbsolutePath());
      if (sourceDir.getName().equalsIgnoreCase(CDMSchemaDir.FLAT_DATA_DIR.getDirName()) && isFromLtpImport(file, cdmId)) {
        continue;
      }

      if (sourceDir.getName().equalsIgnoreCase(CDMSchemaDir.MC_DIR.getDirName()) && isFromLtpImport(file, cdmId)) {
        log.info("File %s is from ltp import, skiping", file.getAbsolutePath());
        continue;
      }
      //if (sourceDir.getName().equalsIgnoreCase(CDMSchemaDir.MASTER_COPY_TIFF_DIR.getDirName()) && isFromLtpImport(file, cdmId)) {
      //  log.info("File %s is from ltp import, skiping", file.getAbsolutePath());
      //  continue;
      //}
      if (sourceDir.getName().equalsIgnoreCase(CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName()) && isFromLtpImport(file, cdmId)) {
        log.info("File %s is from ltp import, skiping", file.getAbsolutePath());
        continue;
      }
      if (!file.getName().contains(".")) {
        log.error("Incorrect name of file - extension is missing: " + file.getName());
        result.setState(State.ERROR);
        result.getResultMessage().append("Incorrect name of file - extension is missing: " + file.getName());
        continue;
      }
      
      final String fileExt = file.getName().substring(file.getName().lastIndexOf(".") + 1);
      
      
      // Kakadu - only for JPEG200
      if (KakaduService.KAKADU_JPG2000_EXT.equalsIgnoreCase(fileExt) || KakaduService.KAKADU_JPG2000_CUSTOM_EXT.equals(fileExt)) {
        OperationResult kakaduResult;
        try {
          kakaduResult = new KakaduService().characterize(file, targetDir);
        }
        catch (final KakaduException e) {
          throw new FileCharacterizationException(e);
        }
        if (kakaduResult.getState().equals(State.ERROR)) {
          result.setState(State.ERROR);
          result.getResultMessage().append(kakaduResult.getResultMessage());
          continue;
        }
        result.getResultMessage().append(kakaduResult.getResultMessage());
      }

      try {
        if (sourceDir.getName().equals(CDMSchemaDir.ORIGINAL_DATA.getDirName()) && formatMigration) {
          jhoveResult = new JhoveService().characterize(file, targetDir, OutputType.XML_AND_MIX, MIX_XSLT_PATH_FORMAT_MIGRATION);
        }
        else if (sourceDir.getName().equals(CDMSchemaDir.ORIGINAL_DATA.getDirName()) && "K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))){
          if ("DJVU".equalsIgnoreCase(FilenameUtils.getExtension(file.getName()))) {
            jhoveResult = new ExifService().characterize(file, targetDir, OutputType.XML_AND_MIX, MIX_XSLT_PATH_EXIF);
            try {
				new MixHelper(new File(jhoveResult.getOutputFileName() + ".mix").getAbsolutePath()).updateExifMix(cdmId);
			} catch (DocumentException e) {
				e.printStackTrace();
			}
            
          } else {
            jhoveResult = new JhoveService().characterize(file, targetDir, OutputType.XML_AND_MIX, MIX_XSLT_PATH_JHOVE2);
            try {
				new MixHelper(new File(jhoveResult.getOutputFileName() + ".mix").getAbsolutePath()).updateJpegMix(cdmId);
			} catch (DocumentException e) {
				e.printStackTrace();
			}
          }
        } else if (sourceDir.getName().equals(CDMSchemaDir.MASTER_COPY_TIFF_DIR.getDirName()) && "K4".equals(cdm.getCdmProperties(cdmId).getProperty("importType"))) {
        	jhoveResult = new JhoveService().characterize(file, targetDir, OutputType.XML_AND_MIX, null);
        	try {
        		new MixHelper(new File(jhoveResult.getOutputFileName() + ".mix").getAbsolutePath()).updateTiffMix(cdmId);
        	} catch (DocumentException e) {
        		e.printStackTrace();
        	}
        	
        } else {
        	jhoveResult = new JhoveService().characterize(file, targetDir, OutputType.XML_AND_MIX, null);
        }
      }
      catch (final JhoveException e) {
    	  throw new FileCharacterizationException(e);
      }
      catch (ExifException e) {
    	  throw new FileCharacterizationException(e);
      } 
      catch (SAXException e) {
    	  throw new FileCharacterizationException(e);
	}
      if (jhoveResult.getState().equals(State.ERROR)) {
        result.setState(State.ERROR);
        result.getResultMessage().append(jhoveResult.getResultMessage());
        continue;
      }
      result.getResultMessage().append(jhoveResult.getResultMessage());
    }
    if (State.ERROR.equals(result.getState())) {
      log.error("Summary result of FileCharacterization: " + result.getState().toString() + " : " + result.getResultMessage().toString());
      throw new FileCharacterizationException("Summary result of FileCharacterization: " + result.getState().toString() + " : " + result.getResultMessage().toString());
    }

    validateMixsCreated(sourceDir, targetDir, cdmId);
    checkJHoveResultFile(result);

    try {    
      if (sourceDir.getName().equalsIgnoreCase(CDMSchemaDir.FLAT_DATA_DIR.getDirName())) {
        updateMixsForPS(cdmId);
      }
  
      if (sourceDir.getName().equalsIgnoreCase(CDMSchemaDir.MC_DIR.getDirName())) {
        updateMixForMC(cdmId);
      }
      
      /*if (sourceDir.getName().equalsIgnoreCase(CDMSchemaDir.MASTER_COPY_TIFF_DIR.getDirName())) {
        updateMixsForMcTiff(cdmId);
      }*/  
      
      if (formatMigration) {
        if ((sourceDir.getName().equalsIgnoreCase(CDMSchemaDir.FLAT_DATA_DIR.getDirName()) || sourceDir.getName().equalsIgnoreCase(CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName()))) {
          updateMixFormatMigration(cdmId, sourceDir);
        }
    
        if (sourceDir.getName().equalsIgnoreCase(CDMSchemaDir.ORIGINAL_DATA.getDirName())) {
          if(searchForItem(parameters, FormatMigrationHelper.SKIP_MIX_UPDATE_FORMAT_MIGRATION_PARAM_NAME) == null){
            updateMixFormatMigrationOrigData(cdmId);
          }
        }
      }
  
      log.info("execute finished");
      return result.getState().toString() + (result.getResultMessage().length() > 0 ? (" : " + result.getResultMessage().toString()) : "");
    } catch (Exception e) {
      log.error("Error while updating mix", e);
      throw new FileCharacterizationException("Error while updating mix", e);
    }
  }

  private ParamMapItem searchForItem(ParamMap parametes, String paramName){
    if(parametes == null){
      return null;
    }
    for (ParamMapItem item : parametes.getItems()) {
      if(item.getName().equalsIgnoreCase(paramName)){
        return item;
      }
    }
    return null;
  }
  
  private void checkJHoveResultFile(OperationResult result) {
    if (jhoveResult != null) {
      final FileCharacterizationJHoveValidator validator = new FileCharacterizationJHoveValidator();
      final FileCharacterizationJHoveValidatorResultWrapper validatorResult = validator.containsJHoveResultErrors(new File(jhoveResult.getOutputFileName()));
      if (!validatorResult.isValid()) {
        result.setState(State.ERROR);
        for (String errorMessage : validatorResult.getMessages()) {
          result.getResultMessage().append(errorMessage);
          result.getResultMessage().append("    ");
        }
      }
    }
  }

  private boolean isEmpty(String s) {
    if (s == null) {
      return true;
    }
    if (s.length() == 0) {
      return true;
    }
    return false;
  }

  private void validateMixsCreated(File sourceDir, File targetDir, String cdmId) {
    log.info("Validation if each image has mix file started.");
    log.info(String.format("Source dir: %s, target dir: %s", sourceDir.getAbsolutePath(), targetDir.getAbsolutePath()));
    WildcardFileFilter filter = new WildcardFileFilter(cfgExts);
    List<File> imgFiles = (List<File>) FileUtils.listFiles(sourceDir, filter, FileFilterUtils.falseFileFilter());

    for (File imgFile : imgFiles) {
      if (sourceDir.getName().equalsIgnoreCase(CDMSchemaDir.FLAT_DATA_DIR.getDirName()) && isFromLtpImport(imgFile, cdmId)) {
        log.info("File is from ltp import skipping control.");
        continue;
      }
      File mixFile = new File(targetDir + File.separator + imgFile.getName() + XML_MIX_FILE_SUFFIX);
      if (!mixFile.exists()) {
        throw new SystemException("Mix file does not exist for: " + imgFile.getAbsolutePath(), ErrorCodes.FILE_NOT_FOUND);
      }
      else {
        if (mixFile.length() == 0) {
          throw new SystemException("Mix file has size 0, for: " + imgFile.getAbsolutePath(), ErrorCodes.FILE_NOT_FOUND);
        }
      }
    }
  }

  public void updateMixFormatMigrationOrigData(String cdmId) {
    log.info("updateMixFormatMigrationOrigData method started.");
    // UPDATE original data
    String origDataMixDirPath = cdm.getMixDir(cdmId).getAbsolutePath() + File.separator + CDMSchemaDir.ORIGINAL_DATA.getDirName();
    File origDataMixDir = new File(origDataMixDirPath);
    Collection<File> mixOrigData = FileUtils.listFiles(origDataMixDir, mixExt, false);
    MixEnvBean bean = MixHelper.loadEvnMixFileOriginalData(cdmId);
    for (File mixFile : mixOrigData) {
      updateMixFormatMigration(mixFile, bean,cdmId);
    }
  }
  
  public void updateMixFormatMigration(File mixFile, MixEnvBean envBean,String cdmId){
    MixHelper mixHelper = new MixHelper(mixFile.getAbsolutePath());
    JHoveHelper jhoveHelper;
    File jHoveFile = new File(mixFile.getParent() + File.separator + FilenameUtils.getBaseName(mixFile.getAbsolutePath()));
    try {
      jhoveHelper = JHoveHelper.getInstance(jHoveFile.getAbsolutePath());
    }
    catch (DocumentException e) {
      throw new com.logica.ndk.tm.utilities.SystemException("Error while reading Jhove xml.", e);
    }
    mixHelper.setFormatDesignation(jhoveHelper.getMimeType(), jhoveHelper.getVersion());
    mixHelper.addDenominator(1);
    if (envBean != null) {
      mixHelper.updatePropertiesFromEnvDocument(envBean,cdmId);
    }
    mixHelper.addNormalOrientation();
    mixHelper.writeToFile(mixHelper.getMix(), mixFile.getAbsolutePath());
  }

  public void updateMixFormatMigration(String cdmId, File forDirectory) {
    log.info("updateMixFormatMigration method started.");
    // UPDATE data
    File mixDir = new File(cdm.getMixDir(cdmId) + File.separator + forDirectory.getName());
    Collection<File> mixFiles = FileUtils.listFiles(mixDir, mixExt, false);
    MixHelper mixHelper;
    JHoveHelper jhoveHelper;
    File jHoveFile;
    File imgFile;
    
    List<String> formatMigrationScanNumbers = new LinkedList<String>();
    FormatMigrationScans formatMigrationScans = null;
    
    try {
      formatMigrationScans = FormatMigrationScansHelper.load(new File(cdm.getWorkspaceDir(cdmId), FormatMigrationScansHelper.FILE_NAME));
    }
    catch (Exception e) {
      log.error("Error while loading " + FormatMigrationScansHelper.FILE_NAME + " but processing continue", e);
    }
    if(formatMigrationScans != null){
      for (FormatMigrationScan formatMigrationScan : formatMigrationScans.getScans()) {
        formatMigrationScanNumbers.add(formatMigrationScan.getScanNumber().toString());
      }
    }else{
      formatMigrationScanNumbers.add("1");
    }
    
    for (File file : mixFiles) {
      // Ignore non-migration files
      if (!migrationHelper.isVirtualScanFile(cdmId, file)) {
        continue;
      }
      if(isFromScan(formatMigrationScanNumbers, file.getName())) {
        jHoveFile = new File(mixDir + File.separator + FilenameUtils.getBaseName(file.getAbsolutePath()));
        mixHelper = new MixHelper(file.getAbsolutePath());
        try {
          jhoveHelper = JHoveHelper.getInstance(jHoveFile.getAbsolutePath());
        }
        catch (DocumentException e) {
          throw new com.logica.ndk.tm.utilities.SystemException("Error while reading Jhove xml.", e);
        }
        mixHelper.setFormatDesignation(jhoveHelper.getMimeType(), jhoveHelper.getVersion());
  
        //ChangeHistory
        final File originalDataCsv = new File(cdm.getTransformationsDir(cdmId) + File.separator + ORIGINAL_DATA_CSV);
        List<PremisCsvRecord> records = PremisCsvHelper.getRecords(originalDataCsv, cdm, cdmId);
        PremisCsvRecord foundRecord = null;
  
        for (PremisCsvRecord record : records) {
          if (FilenameUtils.getBaseName(jHoveFile.getName()).contains(FilenameUtils.getBaseName(record.getFile().getName()))) {
            foundRecord = record;
            break;
          }
        }
        File originalDataFile = null;
        if (foundRecord != null) {
          originalDataFile = foundRecord.getFile();
        }
        imgFile = new File(forDirectory.getAbsolutePath() + File.separator + FilenameUtils.removeExtension((jHoveFile.getName())));
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        if (originalDataFile != null) {
          mixHelper.setChangeHistory(df.format(new Date(imgFile.lastModified())), originalDataFile.getAbsolutePath());
        }
        mixHelper.normalizeBitsPerSample(cdmId);
        mixHelper.removeReferenceBlackWhite();
        mixHelper.removePrimaryChromaticities();
        mixHelper.removeWhitePoint();
        mixHelper.writeToFile(mixHelper.getMix(), file.getAbsolutePath());
        log.info("Update mix for format migration finished.");
      }
    }
  }

  private boolean isFromScan(List<String> scansNumbers, String fileName){
    for (String scanNumber : scansNumbers) {
      if(fileName.startsWith(scanNumber + "_")){
        return true;
      }
    }
    return false;
  }
  
  public void updateMixForMC(String cdmId) {
    log.info("Update mix for masterCopy started.");
    String[] LTPmigrationTypes = TmConfig.instance().getStringArray("ltp.format-migration.types");
    String processType = cdm.getCdmProperties(cdmId).getProperty("processType");
    for (int i = 0; i < LTPmigrationTypes.length; i++) {
      if (LTPmigrationTypes[i].equals(processType)) {
        log.info("Format migration process. Skipping update for masterCopy mix.");
        return;
      }
    }

    String version = TmConfig.instance().getString("utility.convertToJpeg2000.output.version");
    String colorSpace = TmConfig.instance().getString("utility.convertToJpeg2000.output.colorMode");
    String compressionScheme = TmConfig.instance().getString("utility.convertToJpeg2000.output.compressionScheme");
    String objIdentifierType = TmConfig.instance().getString("utility.convertToJpeg2000.output.objIdentifierType");
    File mixDirMC = new File(cdm.getMixDir(cdmId) + File.separator + cdm.getMasterCopyDir(cdmId).getName());
    String[] mixExt = { "mix" };
    Collection<File> mixFilesMC = FileUtils.listFiles(mixDirMC, mixExt, false);
    MixHelper mixHelper;
    JHoveHelper jhoveHelper;
    File jHoveFile;
    File mcFile;
    for (File file : mixFilesMC) {
      String imageFileName = FilenameUtils.getBaseName(FilenameUtils.getBaseName(file.getName()));
      if (!FilenameUtils.getExtension(imageFileName).equals("jp2")) {
        continue;
      }
      if (!isFromLtpImport(file, cdmId)) {
        jHoveFile = new File(mixDirMC + File.separator + FilenameUtils.getBaseName(file.getAbsolutePath()));
        mixHelper = new MixHelper(file.getAbsolutePath());
        try {
          jhoveHelper = JHoveHelper.getInstance(jHoveFile.getAbsolutePath());
        }
        catch (DocumentException e) {
          throw new com.logica.ndk.tm.utilities.SystemException("Error while reading Jhove xml.", e);
        }

        mixHelper.setFormatDesignation(jhoveHelper.getMimeType(), version);
        mixHelper.setPhotometricInterpretation(colorSpace);

        //ChangeHistory
        final File mcCsv = new File(cdm.getTransformationsDir(cdmId) + File.separator + MC_CSV);
        List<PremisCsvRecord> records = PremisCsvHelper.getRecords(mcCsv, cdm, cdmId);
        PremisCsvRecord foundRecord = null;

        for (PremisCsvRecord record : records) {
          if (FilenameUtils.getBaseName(jHoveFile.getName()).contains(FilenameUtils.getBaseName(record.getFile().getName()))) {
            foundRecord = record;
            break;
          }
        }
        CDMMetsHelper metsHelper = new CDMMetsHelper();
        String flatFileName = null;
        File flatFile = null;
        File PPfile = null;
        if (foundRecord != null) {
          flatFileName = metsHelper.getFlatFileForPPFile(cdmId, FilenameUtils.getBaseName(foundRecord.getFile().getName()));
          flatFile = new File(cdm.getFlatDataDir(cdmId) + File.separator + flatFileName + ".tif");
          PPfile = foundRecord.getFile();
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        mcFile = new File(cdm.getMasterCopyDir(cdmId).getAbsolutePath() + File.separator + FilenameUtils.removeExtension((jHoveFile.getName())));
        if ((flatFile != null) && (flatFile.exists())) {
          mixHelper.setChangeHistory(df.format(new Date(mcFile.lastModified())), flatFile.getAbsolutePath());
        }
        else {
          if ((PPfile != null) && PPfile.exists()) {
            mixHelper.setChangeHistory(df.format(new Date(mcFile.lastModified())), PPfile.getAbsolutePath());
          }
        }

        if (mixHelper.getCompressionValue().equals("Unknown")) {
          mixHelper.setCompression(compressionScheme);
        }
        mixHelper.normalizeBitsPerSample(cdmId);
        if (foundRecord != null) {
          mixHelper.setObjectInformation(objIdentifierType, foundRecord.getId());
        }

        File kduFile = new File(mixDirMC + File.separator + FilenameUtils.getBaseName(jHoveFile.getName()) + ".kdu");
        if (kduFile.exists()) {
          KduHelper kduHelper = new KduHelper(kduFile);

          //SpecialFormatCharacteristics
          //CodecCompliance
          String agentName = TmConfig.instance().getString("utility.convertToJpeg2000.profile.agentName", null);
          String agentVersion = TmConfig.instance().getString("utility.convertToJpeg2000.profile.agentVersion", null);
          String codeStreamProfile = kduHelper.getProperty("Sprofile").get(0);
          String codeStreamProfileCode = "P" + codeStreamProfile.substring(codeStreamProfile.length() - 1);
          String codeStreamClass;

          if (kduHelper.getProperty("Cclass") == null) {
            mixHelper.setCodecCompliance(agentName, agentVersion, codeStreamProfileCode, "C2"); //TODO
          }
          else {
            codeStreamClass = kduHelper.getProperty("Cclass").get(0);
            String codeStreamClassCode = "C" + codeStreamProfile.substring(codeStreamClass.length() - 1);
            mixHelper.setCodecCompliance(agentName, agentVersion, codeStreamProfileCode, codeStreamClassCode);
          }

          //EncodingOptions
          List<String> sTiles = kduHelper.getProperty("Stiles");
          Integer tileWidth = null;
          Integer tileHeight = null;
          if (sTiles != null) {
            tileWidth = Integer.parseInt(sTiles.get(0).split(",")[0]);
            tileHeight = Integer.parseInt(sTiles.get(0).split(",")[1]);
          }
          Integer qualityLayers = null;
          Integer resolutionLevels = null;
          try {
            qualityLayers = Integer.parseInt(kduHelper.getProperty("Clayers").get(0));
            resolutionLevels = Integer.parseInt(kduHelper.getProperty("Clevels").get(0));
          }
          catch (Exception e) {
            log.warn("Property in kdu file not found. Set as empty");
          }
          mixHelper.setEncodingOptions(tileWidth, tileHeight, qualityLayers, resolutionLevels);
        }
        mixHelper.writeToFile(mixHelper.getMix(), file.getAbsolutePath());
        log.info("Update mix for masterCopy finished.");
      }
      else {
        log.info("File is from lpt import, update not needed.");
      }

    }
  }

  private String createWorkspacePath(String cdmId, String dirName) {
    return cdm.getMixDir(cdmId).getAbsolutePath() + File.separator + dirName;
  }

  public void updateMixsForPS(String cdmId) {
    log.info("Updating mix files started");
    String objIdentifierType = TmConfig.instance().getString("utility.convertToJpeg2000.output.objIdentifierType");
    String postProcMixDirPath = cdm.getMixDir(cdmId).getAbsolutePath() + File.separator + CDMSchemaDir.FLAT_DATA_DIR.getDirName();
    File postProcMixDir = new File(postProcMixDirPath);
    if (postProcMixDir.exists()) {
      String[] mixFiles = postProcMixDir.list(new FilenameFilter() {

        @Override
        public boolean accept(File arg0, String arg1) {
          return arg1.endsWith(MIX_FILE_SUFFIX);
        }
      });
      for (String mixFile : mixFiles) {
        // Dont update PS mix for format migration files
        if (migrationHelper.isFormatMigration(cdm.getCdmProperties(cdmId).getProperty("importType")) && migrationHelper.isVirtualScanFile(cdmId, new File(mixFile))) {
          continue;
        }
        log.info("Updating mix file: " + mixFile);
        if (!isFromLtpImport(new File(postProcMixDirPath + File.separator + mixFile), cdmId)) {
          MixHelper helper = new MixHelper(postProcMixDirPath + File.separator + mixFile);
          try {
            PremisCsvHelper premisHelper = new PremisCsvHelper();
            final File mcCsv = new File(cdm.getTransformationsDir(cdmId) + File.separator + FLAT_DATA_CSV);
            final List<PremisCsvRecord> records = premisHelper.getRecords(mcCsv, cdm, cdmId);
            PremisCsvRecord foundRecord = null;
            for (PremisCsvRecord record : records) {
              if (mixFile.substring(0, mixFile.length() - FLAT_DATA_MIX_SUFFIX.length()).contains(FilenameUtils.getBaseName(record.getFile().getName()))) {
                foundRecord = record;
                break;
              }
            }

            if (foundRecord != null) {
              helper.setObjectInformation(objIdentifierType, foundRecord.getId());
            }

            String imgName = mixFile.substring(0, mixFile.lastIndexOf(FLAT_DATA_MIX_SUFFIX));
            log.debug("ImageName from mixFile: " + imgName);
            helper.updateDPIfromTiffinfo(cdmId, imgName);
            helper.normalizeBitsPerSample(cdmId);

            MixEnvBean bean = MixHelper.loadEvnMixFile(mixFile, cdmId);
            if (bean == null) {
              break;
            }
            helper.updatePropertiesFromEnvDocument(bean,cdmId);
          }
          catch (Exception e) {
            log.error("Error while updating mix file from environment-info.xml. " + e.getMessage());
            throw new SystemException("Error while updating mix file from environment-info.xml. ", e);
          }
        }
      }
    }
    log.info("Updating mix files finished");
  }
  
  public void updateMixSetObjectIdentifierValue(String cdmId, String dirName){
    log.info("Updating mix files started");
    String mcMixDirPath = cdm.getMixDir(cdmId).getAbsolutePath() + File.separator + dirName;
    File mcMixDir = new File(mcMixDirPath);
    if (mcMixDir.exists()) {
     
      List<PremisCsvRecord> records = null;
      String[] mixFiles = mcMixDir.list(new FilenameFilter() {

        @Override
        public boolean accept(File arg0, String arg1) {
          return arg1.endsWith(XML_MIX_FILE_SUFFIX);
        }
      });
      for (String mixFile : mixFiles) {
        log.info("Updating mix file: " + mixFile);
        final File mcCsv = new File(cdm.getTransformationsDir(cdmId) + File.separator + dirName + ".csv");
        if(!mcCsv.exists()){
            throw new SystemException("Could not find csv file, " + mcCsv.getAbsolutePath());
        }
        
        if(records == null){
            records = PremisCsvHelper.getRecords(mcCsv, cdm, cdmId);
        }
        
        try {
          
          PremisCsvRecord foundRecord = null;
          for (PremisCsvRecord record : records) {
            if (mixFile.substring(0, mixFile.length() - XML_MIX_FILE_SUFFIX.length()).contains(FilenameUtils.getBaseName(record.getFile().getName()))) {
              foundRecord = record;
              break;
            }
          }

          MixHelper mixHelper = new MixHelper(mcMixDirPath + File.separator + mixFile);
          if (foundRecord != null) {
            String recordId;
            if(dirName.equalsIgnoreCase(CDMSchemaDir.FLAT_DATA_DIR.getDirName())){
                recordId = foundRecord.getId().replace("PS", "FLAT");
            }else if(dirName.equalsIgnoreCase(CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName())){
                recordId = foundRecord.getId().replace("PS", "PP");
            }else{
                recordId = foundRecord.getId();
            }
            mixHelper.setObjectInformation(OBJECT_IDENTIFIER_TYPE, recordId);
            mixHelper.writeToFile(mixHelper.getMix(), mcMixDirPath + File.separator + mixFile);
          }

          
        }
        catch (Exception e) {
          throw new SystemException("Error while updating mix", e);
                  
        }
      }
    }
  }
  
  /*public void updateMixsForMcTiff(String cdmId) {
    log.info("Updating mix files MC_TIFF started");
    String objIdentifierType = TmConfig.instance().getString("utility.ConvertDjVuToTiff.output.objIdentifierType");
    String masterCopyTiffMixDirPath = cdm.getMixDir(cdmId).getAbsolutePath() + File.separator + CDMSchemaDir.MASTER_COPY_TIFF_DIR.getDirName();
    File masterCopyTiffMixDir = new File(masterCopyTiffMixDirPath);
    if (masterCopyTiffMixDir.exists()) {
      String[] mixFiles = masterCopyTiffMixDir.list(new FilenameFilter() {

        @Override
        public boolean accept(File arg0, String arg1) {
          return arg1.endsWith(MIX_FILE_SUFFIX);
        }
      });
      for (String mixFile : mixFiles) {
        // Dont update MC_TIFF mix for format migration files
        if (migrationHelper.isFormatMigration(cdm.getCdmProperties(cdmId).getProperty("importType")) && migrationHelper.isVirtualScanFile(cdmId, new File(mixFile))) {
          continue;
        }
        log.info("Updating mix file: " + mixFile);
        if (!isFromLtpImport(new File(masterCopyTiffMixDirPath + File.separator + mixFile), cdmId)) {
          MixHelper helper = new MixHelper(masterCopyTiffMixDirPath + File.separator + mixFile);
          try {
            PremisCsvHelper premisHelper = new PremisCsvHelper();
            final File mcTiffCsv = new File(cdm.getTransformationsDir(cdmId) + File.separator + MASTER_COPY_TIFF_DATA_CSV);
            final List<PremisCsvRecord> records = premisHelper.getRecords(mcTiffCsv, cdm, cdmId);
            PremisCsvRecord foundRecord = null;
            for (PremisCsvRecord record : records) {
              if (mixFile.substring(0, mixFile.length() - FLAT_DATA_MIX_SUFFIX.length()).contains(FilenameUtils.getBaseName(record.getFile().getName()))) {
                foundRecord = record;
                break;
              }
            }

            if (foundRecord != null) {
              helper.setObjectInformation(objIdentifierType, foundRecord.getId());
            }

            //String imgName = mixFile.substring(0, mixFile.lastIndexOf(FLAT_DATA_MIX_SUFFIX));
            //log.debug("ImageName from mixFile: " + imgName);
            //helper.updateDPIfromTiffinfo(cdmId, imgName);
            //helper.normalizeBitsPerSample();

            MixEnvBean bean = MixHelper.loadEvnMixFile(mixFile, cdmId);
            if (bean == null) {
              break;
            }
            helper.updatePropertiesFromEnvDocument(bean);
            
            MixHelper mixHelper = new MixHelper(mixFile.getAbsolutePath());
            mixHelper.writeToFile(mixHelper.getMix(), mixFile.getAbsolutePath());
          }
          catch (Exception e) {
            log.error("Error while updating mix file from environment-info.xml. " + e.getMessage());
            throw new SystemException("Error while updating mix file from environment-info.xml. ", e);
          }
        }
      }
    }
    log.info("Updating mix files finished");
  }*/
  
  public static void main(String[] args) throws JhoveException{
    CDM cdm = new CDM();
    //new FileCharacterizationImpl().updateMixFormatMigration("a0d64a10-212d-11e3-94f4-00505682629d", cdm.getFlatDataDir("a0d64a10-212d-11e3-94f4-00505682629d"));
    //new FileCharacterizationImpl().updateMixsForMcTiff("2b8211a0-ea4e-11e3-bf25-00505682629d");
    //new FileCharacterizationImpl().updateMixForMC("2b8211a0-ea4e-11e3-bf25-00505682629d");
    //new FileCharacterizationImpl().execute("47932f80-1723-11e4-b553-00505682629d", "C:\\Users\\dominiks\\AppData\\Local\\Temp\\cdm\\CDM_47932f80-1723-11e4-b553-00505682629d\\data\\originalData", "C:\\Users\\dominiks\\AppData\\Local\\Temp\\cdm\\CDM_47932f80-1723-11e4-b553-00505682629d\\data\\.workspace\\mix\\originalData", null);
    //FileCharacterizationImpl fch = new FileCharacterizationImpl();
    new JhoveService().transformXmlToMix(new File(args[0]), new File(args[0]), new File(args[1]), false, new OperationResult(), MIX_XSLT_PATH_JHOVE2);
    //fch.updateMixSetObjectIdentifierValue("3cde6c80-38c5-11e4-8e8e-00505682629d", CDMSchemaDir.ORIGINAL_DATA.getDirName());
    //fch.updateMixSetObjectIdentifierValue("3cde6c80-38c5-11e4-8e8e-00505682629d", CDMSchemaDir.MC_DIR.getDirName());
    //fch.updateMixSetObjectIdentifierValue("3cde6c80-38c5-11e4-8e8e-00505682629d", CDMSchemaDir.POSTPROCESSING_DATA_DIR.getDirName());

//new FileCharacterizationImpl().execute("47932f80-1723-11e4-b553-00505682629d", "C:\\Users\\dominiks\\AppData\\Local\\Temp\\cdm\\CDM_47932f80-1723-11e4-b553-00505682629d\\data\\.workspace\\masterCopy_TIFF", "C:\\Users\\dominiks\\AppData\\Local\\Temp\\cdm\\CDM_47932f80-1723-11e4-b553-00505682629d\\data\\.workspace\\mix\\masterCopy_TIFF", null);
  }
  
}
