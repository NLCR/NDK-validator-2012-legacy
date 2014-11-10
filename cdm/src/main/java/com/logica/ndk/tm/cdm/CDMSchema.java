package com.logica.ndk.tm.cdm;

import static com.google.common.base.Preconditions.checkNotNull;

// Default CDMSchema implementation; hardcoded.
public class CDMSchema {

  private static final String CDM_DIR_PREFIX = "CDM_";

  public enum CDMSchemaDir {
    MC_DIR("MC_DIR", "masterCopy"),
    UC_DIR("UC_DIR", "userCopy"),
    TH_DIR("TH_DIR", "TH"),
    AMD_DIR("AMD_DIR", "amdSec"),
    ALTO_DIR("ALTO_DIR", "ALTO"),
    TXT_DIR("TXT_DIR", "TXT"),
    ARC_DIR("ARC_DIR", "ARC"),
    WARC_DIR("WARC_DIR", "WARC"),
    LOGS_DIR("LOGS_DIR", "LOGS"),
    WA_DATA_DIR("WA_DATA_DIR", "data"),
    MISC_DIR("MISC_DIR", "MISC"),
    RAW_DATA_DIR("RAW_DATA_DIR", "rawData"),
    POSTPROCESSING_DATA_DIR("POSTPROCESSING_DATA_DIR", "postprocessingData"),
    PREMIS_FILES_DIR("PREMIS_FILES_DIR", "premis"),
    FLAT_DATA_DIR("FLAT_DATA_DIR", "flatData"),
    SCANTAILOR_DIR("SCANTAILOR_DIR", "scanTailor"),
    PREVIEW_DIR("PREVIEW_DIR", "preview"),
    SIP1_DIR("SIP1_DIR", "SIP1"),
    SIP2_DIR("SIP2_DIR", "SIP2"),
    TRANSFORMATIONS_DIR("TRANSFORMATIONS_DIR", "transformations"),
    VALIDATION_DIR("VALIDATION_DIR", "validation"),
    SCANS_DIR("SCANS_DIR", "scans"),
    ALEPH_NOTIFICATION_DIR("ALEPH_NOTIFICATION_DIR", "alephNotification"),
    ALEPH_NOTIFICATION_RESPONSE_DIR("ALEPH_NOTIFICATION_DIR", "alephNotificationResponse"),
    WORKSPACE_DIR("WORKSPACE_DIR", CDMConstants.WORKSPACE_DIR_NAME),
    ORIGINAL_DATA("ORIGINAL_DATA", "originalData"),
    MIX_DIR("MIX_DIR", "mix"),
    IMAGES_PDF("IMAGES_PDF", "imagesForPDF"),    
    VALIDATION_FILE("VALIDATION_FILE", "validation.txt"),
    TIFFINFO_SUFFIX("TIFFINFO_SUFFIX", "_tiffinfo.txt"),
    LTP_MD_FILE("LTP_MD_FILE", "LTP_MD.xml"),
    ORDER_LOG_FILE("ORDER_LOG_FILE", "order.xml"),
    JPEG_TIFF_LOCATION_FILE("JPEG_TIFF_LOCATION_FILE", "jpeg-tif-location.txt"),
    SCANTAILOR_TEMP_OUT_DIR("SCANTAILOR_TEMP_OUT_DIR", "tempOut"),
    BACKUP_DIR("BACKUP_DIR", "backup"),
    MASTER_COPY_TIFF_DIR("MASTER_COPY_TIFF_DIR", "masterCopy_TIFF"),
    URN_DIR("URN_DIR", "urn"),
    URN_XML("URN_XML", "import.xml"),
    RESOLVER_RESPONSE("RESOLVER_RESPONSE", "resolver-response.txt"),
    RESULTS_DIR("RESULTS_DIR", "results"),
    LTP_LOG_DIR("LTP_LOG_DIR", "ltpLog"),
    OCR_DIR("OCR_DIR", "ocr"),
    HARD_LINKS_TO_CREATE("HARD_LINKS_TO_CREATE", "hard_links_to_create.xml"),
    VALIDATION_VERSION_FILE("VALIDATION_VERSION_FILE", "validation_version.txt"),
    IMPORT_K4_FINISH_OK("IMPORT_K4_FINISH_OK", "import_k4_ok");
    
    private final String dirName;
    private final String label;

    private CDMSchemaDir(final String label, final String dirName) {
      this.label = label;
      this.dirName = dirName;
    }

    public static CDMSchemaDir getCDMSchemaDirByLabel(final String dirLabel) {
      for (final CDMSchemaDir cdmSchemaDir : values()) {
        if (cdmSchemaDir.label.equals(dirLabel)) {
          return cdmSchemaDir;
        }
      }
      throw new IllegalArgumentException("CDMSchemaDir not definied for dirLabel: " + dirLabel);
    }

    public String getDirName() {
      return dirName;
    }

  }

  public String getDirName(final String dirLabel) {
    return CDMSchemaDir.getCDMSchemaDirByLabel(dirLabel).dirName;
  }

  public String getMasterCopyDirName() {
    return CDMSchemaDir.MC_DIR.dirName;
  }

  public String getUserCopyDirName() {
    return CDMSchemaDir.UC_DIR.dirName;
  }
  
  public String getImagesPDFDirName() {
    return CDMSchemaDir.IMAGES_PDF.dirName;
  }

  public String getValidationDirName(){
    return CDMSchemaDir.VALIDATION_DIR.dirName;
  }
  
  public String getValidationFileName(){
    return CDMSchemaDir.VALIDATION_FILE.dirName;
  }
  
  public String getUrnDirName(){
    return CDMSchemaDir.URN_DIR.dirName;
  }
  
  public String getUrnXmlName(){
    return CDMSchemaDir.URN_XML.dirName;
  }
  
  public String getResolverResponseFileName(){
    return CDMSchemaDir.RESOLVER_RESPONSE.dirName;
  }
    
  public String getOrderLogFileName(){
    return CDMSchemaDir.ORDER_LOG_FILE.dirName;
  }

  public String getTiffinfoFileName(String imageName){
  	checkNotNull(imageName, "imageName argument must not be null");
    return imageName + CDMSchemaDir.TIFFINFO_SUFFIX.dirName;
  }

  public String getJpegTiffLocationFileName(){
    return CDMSchemaDir.JPEG_TIFF_LOCATION_FILE.dirName;
  }

  public String getLtpMdFileName(){
    return CDMSchemaDir.LTP_MD_FILE.dirName;
  }
  
  public String getThumbnailDirName() {
    return CDMSchemaDir.TH_DIR.dirName;
  }

  public String getAmdDirName() {
    return CDMSchemaDir.AMD_DIR.dirName;
  }

  public String getAltoDirName() {
    return CDMSchemaDir.ALTO_DIR.dirName;
  }

  public String getTxtDirName() {
    return CDMSchemaDir.TXT_DIR.dirName;
  }

  public String getLogsDirName() {
    return CDMSchemaDir.LOGS_DIR.dirName;
  }
  
  public String getWaDataDirName() {
    return CDMSchemaDir.WA_DATA_DIR.dirName;
  }

  public String getArcDirName() {
    return CDMSchemaDir.ARC_DIR.dirName;
  }

  public String getWarcDirName() {
    return CDMSchemaDir.WARC_DIR.dirName;
  }

  public String getMiscDirName() {
    return CDMSchemaDir.MISC_DIR.dirName;
  }

  public String getRawDataDirName() {
    return CDMSchemaDir.RAW_DATA_DIR.dirName;
  }

  public String getPostprocessingDataDirName() {
    return CDMSchemaDir.POSTPROCESSING_DATA_DIR.dirName;
  }

  public String getPremisDirName() {
    return CDMSchemaDir.PREMIS_FILES_DIR.dirName;
  }

  public String getTransformationsDirName() {
    return CDMSchemaDir.TRANSFORMATIONS_DIR.dirName;
  }

  public String getScansDirName() {
    return CDMSchemaDir.SCANS_DIR.dirName;
  }
  
  public String getAlephNotificationDirName() {
    return CDMSchemaDir.ALEPH_NOTIFICATION_DIR.dirName;
  }
  
  public String getAlephNotificationResponseDirName() {
    return CDMSchemaDir.ALEPH_NOTIFICATION_RESPONSE_DIR.dirName;
  }

  public String getFlatDataDirName() {
    return CDMSchemaDir.FLAT_DATA_DIR.dirName;
  }

  public String getScantailorConfigsDirName() {
    return CDMSchemaDir.SCANTAILOR_DIR.dirName;
  }
  
  public String getOcrDirName() {
    return CDMSchemaDir.OCR_DIR.dirName;
  }
  
  public String getScantailorTempOutDirName() {
    return CDMSchemaDir.SCANTAILOR_TEMP_OUT_DIR.dirName;
  }

  public String getBackupDirName() {
    return CDMSchemaDir.BACKUP_DIR.dirName;
  }

  public String getResultsDirName() {
    return CDMSchemaDir.RESULTS_DIR.dirName;
  }
  
  public String getSIP1DirName() {
    return CDMSchemaDir.SIP1_DIR.dirName;
  }

  public String getSIP2DirName() {
    return CDMSchemaDir.SIP2_DIR.dirName;
  }

  public String getPreviewDirName() {
    return CDMSchemaDir.PREVIEW_DIR.dirName;
  }
  
  public String getOriginalDataDirName() {
    return CDMSchemaDir.ORIGINAL_DATA.dirName;
  }
  
  public String getMixDirName() {
    return CDMSchemaDir.MIX_DIR.dirName;
  }
  
  public String getLtpLogDirName() {
    return CDMSchemaDir.LTP_LOG_DIR.dirName;
  }
  
  public String getMasterCopyTiffDirName() {
    return CDMSchemaDir.MASTER_COPY_TIFF_DIR.dirName;
  }


  public String getMetsFileName(final String cdmId) {
    return "METS_" + CDMEncodeUtils.encodeForFilename(cdmId) + ".xml";
  }
  
  public String getValidationFileName(final String cdmId) {
    return "validation.txt";
  }

  public String getMD5FileName(final String cdmId) {
    return "MD5_" + CDMEncodeUtils.encodeForFilename(cdmId) + ".md5";
  }

  public String getScansCsvFileName(final String cdmId) {
    return "scans.csv";
  }

  public String getAlephFileName(final String cdmId) {
    return "Aleph_" + CDMEncodeUtils.encodeForFilename(cdmId) + ".xml";
  }

  public String getEmConfigFileName(final String cdmId) {
    checkNotNull(cdmId, "cdmId must not be null");
    return "EM_" + CDMEncodeUtils.encodeForFilename(cdmId) + ".csv";
  }
  
  public String getFlatToPPMappingFileName() {
    return "mapping" + ".csv";
  }

  public String getOcrFilesListName(final String cdmId) {
    checkNotNull(cdmId, "cdmId must not be null");
    return CDMEncodeUtils.encodeForFilename(cdmId) + ".ocrList";
  }
  
  public String getCdmDirPrefix() {
    return CDM_DIR_PREFIX;
  }
  
  public String getHardLinksToCreateFileName(){
    return CDMSchemaDir.HARD_LINKS_TO_CREATE.dirName;
  }
  
  public String getValidationVersionFileName(){
    return CDMSchemaDir.VALIDATION_VERSION_FILE.dirName;
  }
  
}
