package com.logica.ndk.tm.utilities.integration.wf.task;

import java.util.Date;

import com.logica.ndk.tm.utilities.integration.wf.enumerator.DocumentLocality;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Enumerator;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Scanner;
import com.logica.ndk.tm.utilities.integration.wf.enumerator.Workplace;

/**
 * Represents a package task passed from WF to TM
 * 
 * @author majdaf
 */
public class PackageTask extends Task {

  String volumeNumber;
  String partNumber;
  String partName;
  String volumeDate;
  String dateIssued;
  String rdId;
  String issn;
  Scanner mainScanner;
  int minOCRRate;
  Scanner boardScanner;
  Enumerator type;
  Enumerator ocr;
  DocumentLocality documentLocality;
  Enumerator color;
  String author;
  int ocrRate;
  Enumerator docKeeper;
  String title;
  Date publishDT;
  String isbn;
  String barCode;
  String dpi;
  boolean destructiveDigitization;
  int pageCount;
  int scanCount;
  boolean scanAtPreparation;
  String ccnb;
  Enumerator project;
  String sigla;
  boolean documentDestroyed;
  Enumerator language;
  boolean publicDoc;
  Enumerator ocrFont;
  String publish;
  boolean split;
  String docNumber;
  String description;
  Workplace workplace;
  int controlFrequency;
  String urnnbn;
  Enumerator scanMode;
  int dimensionX;
  int dimensionY;
  Enumerator profilePP;
  Enumerator profileMC;
  Enumerator profileUC;
  Long nextScanId;
  String issueNumber;
  String pressmark;
  String physicalDescription;
  boolean scanCover;
  boolean scanBoard;
  boolean prepareCover;
  boolean prepareBoard;
  String batchNumber;
  int ocrLicenceUsed;
  boolean externalImage;
  String edition;
  Enumerator subtype;
  private Enumerator importType;
  String volumeUUID;
  String issueUUID;
  String recordIdentifier;
  String titleUUID;
  boolean processScan;
  boolean processPrepare;
  private String templateCode;
  String descriptionLevel;
  boolean pluginActivate;

   public boolean isPluginActivate() {
	return pluginActivate;
   }

	public void setPluginActivate(boolean pluginActivate) {
		this.pluginActivate = pluginActivate;
	}

  public String toString() {
    String result = String.valueOf(getId());
    if (getReservedBy() != null) {
      result += " by" + getReservedBy().getName();
    }
    if (getTitle() != null && getTitle().length() > 0) {
      result += " [" + getTitle() + "]";
    }
    return result;
  }

  public boolean isPublic() {
    return publicDoc;
  }

  public void setPublic(boolean publicDoc) {
    this.publicDoc = publicDoc;
  }

  public String getRdId() {
    return rdId;
  }

  public void setRdId(String rdId) {
    this.rdId = rdId;
  }

  public String getIssn() {
    return issn;
  }

  public void setIssn(String issn) {
    this.issn = issn;
  }

  public Enumerator getMainScanner() {
    return mainScanner;
  }

  public void setMainScanner(Scanner mainScanner) {
    this.mainScanner = mainScanner;
  }

  public int getMinOCRRate() {
    return minOCRRate;
  }

  public void setMinOCRRate(int minOCRRate) {
    this.minOCRRate = minOCRRate;
  }

  public Enumerator getBoardScanner() {
    return boardScanner;
  }

  public void setBoardScanner(Scanner boardScanner) {
    this.boardScanner = boardScanner;
  }

  public Enumerator getType() {
    return type;
  }

  public void setType(Enumerator type) {
    this.type = type;
  }

  public Enumerator getOcr() {
    return ocr;
  }

  public void setOcr(Enumerator ocr) {
    this.ocr = ocr;
  }

  public DocumentLocality getDocumentLocality() {
    return documentLocality;
  }

  public void setDocumentLocality(DocumentLocality documentLocality) {
    this.documentLocality = documentLocality;
  }

  public Enumerator getColor() {
    return color;
  }

  public void setColor(Enumerator colour) {
    this.color = colour;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public int getOcrRate() {
    return ocrRate;
  }

  public void setOcrRate(int ocrRate) {
    this.ocrRate = ocrRate;
  }

  public Enumerator getDocKeeper() {
    return docKeeper;
  }

  public void setDocKeeper(Enumerator docKeeper) {
    this.docKeeper = docKeeper;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Date getPublishDT() {
    return publishDT;
  }

  public void setPublishDT(Date publishDT) {
    this.publishDT = publishDT;
  }

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public String getBarCode() {
    return barCode;
  }

  public void setBarCode(String barCode) {
    this.barCode = barCode;
  }

  public String getDpi() {
    return dpi;
  }

  public void setDpi(String dpi) {
    this.dpi = dpi;
  }

  public boolean isDestructiveDigitization() {
    return destructiveDigitization;
  }

  public void setDestructiveDigitization(boolean destructiveDigitization) {
    this.destructiveDigitization = destructiveDigitization;
  }

  public int getPageCount() {
    return pageCount;
  }

  public void setPageCount(int pageCount) {
    this.pageCount = pageCount;
  }

  public boolean isScanAtPreparation() {
    return scanAtPreparation;
  }

  public void setScanAtPreparation(boolean scanAtPreparation) {
    this.scanAtPreparation = scanAtPreparation;
  }

  public String getCcnb() {
    return ccnb;
  }

  public void setCcnb(String ccnb) {
    this.ccnb = ccnb;
  }

  public Enumerator getProject() {
    return project;
  }

  public void setProject(Enumerator project) {
    this.project = project;
  }

  public String getSigla() {
    return sigla;
  }

  public void setSigla(String sigla) {
    this.sigla = sigla;
  }

  public boolean isDocumentDestroyed() {
    return documentDestroyed;
  }

  public void setDocumentDestroyed(boolean documentDestroyed) {
    this.documentDestroyed = documentDestroyed;
  }

  public Enumerator getLanguage() {
    return language;
  }

  public void setLanguage(Enumerator language) {
    this.language = language;
  }

  public boolean isPublicDoc() {
    return publicDoc;
  }

  public void setPublicDoc(boolean publicDoc) {
    this.publicDoc = publicDoc;
  }

  public Enumerator getOcrFont() {
    return ocrFont;
  }

  public void setOcrFont(Enumerator ocrFont) {
    this.ocrFont = ocrFont;
  }

  public int getScanCount() {
    return scanCount;
  }

  public void setScanCount(int scanCount) {
    this.scanCount = scanCount;
  }

  public Boolean getError() {
    return error;
  }

  public String getPublish() {
    return publish;
  }

  public void setPublish(String publish) {
    this.publish = publish;
  }

  public boolean isSplit() {
    return split;
  }

  public void setSplit(boolean split) {
    this.split = split;
  }

  public String getDocNumber() {
    return docNumber;
  }

  public void setDocNumber(String docNumber) {
    this.docNumber = docNumber;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Workplace getWorkplace() {
    return workplace;
  }

  public void setWorkplace(Workplace workplace) {
    this.workplace = workplace;
  }

  public int getControlFrequency() {
    return controlFrequency;
  }

  public void setControlFrequency(int controlFrequency) {
    this.controlFrequency = controlFrequency;
  }

  public String getUrnnbn() {
    return urnnbn;
  }

  public void setUrnnbn(String urnnbn) {
    this.urnnbn = urnnbn;
  }

  public Enumerator getScanMode() {
    return scanMode;
  }

  public void setScanMode(Enumerator scanMode) {
    this.scanMode = scanMode;
  }

  public int getDimensionX() {
    return dimensionX;
  }

  public void setDimensionX(int dimensionX) {
    this.dimensionX = dimensionX;
  }

  public int getDimensionY() {
    return dimensionY;
  }

  public void setDimensionY(int dimensionY) {
    this.dimensionY = dimensionY;
  }

  public Enumerator getProfilePP() {
    return profilePP;
  }

  public void setProfilePP(Enumerator profilePP) {
    this.profilePP = profilePP;
  }

  public Enumerator getProfileMC() {
    return profileMC;
  }

  public void setProfileMC(Enumerator profileMC) {
    this.profileMC = profileMC;
  }

  public Enumerator getProfileUC() {
    return profileUC;
  }

  public void setProfileUC(Enumerator profileUC) {
    this.profileUC = profileUC;
  }

  public Long getNextScanId() {
    return nextScanId;
  }

  public void setNextScanId(Long nextScanId) {
    this.nextScanId = nextScanId;
  }

  public String getDateIssued() {
    return dateIssued;
  }

  public void setDateIssued(String dateIssued) {
    this.dateIssued = dateIssued;
  }

  public String getPartNumber() {
    return partNumber;
  }

  public void setPartNumber(String partNumber) {
    this.partNumber = partNumber;
  }

  public String getVolumeDate() {
    return volumeDate;
  }

  public void setVolumeDate(String volumeDate) {
    this.volumeDate = volumeDate;
  }

  public String getVolumeNumber() {
    return volumeNumber;
  }

  public void setVolumeNumber(String volumeNumber) {
    this.volumeNumber = volumeNumber;
  }

  public String getIssueNumber() {
    return issueNumber;
  }

  public void setIssueNumber(String issueNumber) {
    this.issueNumber = issueNumber;
  }

  public String getPressmark() {
    return pressmark;
  }

  public void setPressmark(String pressmark) {
    this.pressmark = pressmark;
  }

  public String getPhysicalDescription() {
    return physicalDescription;
  }

  public void setPhysicalDescription(String physicalDescription) {
    this.physicalDescription = physicalDescription;
  }

  public boolean isScanCover() {
    return scanCover;
  }

  public void setScanCover(boolean scanCover) {
    this.scanCover = scanCover;
  }

  public boolean isScanBoard() {
    return scanBoard;
  }

  public void setScanBoard(boolean scanBoard) {
    this.scanBoard = scanBoard;
  }

  public boolean isPrepareCover() {
    return prepareCover;
  }

  public void setPrepareCover(boolean prepareCover) {
    this.prepareCover = prepareCover;
  }

  public boolean isPrepareBoard() {
    return prepareBoard;
  }

  public void setPrepareBoard(boolean prepareBoard) {
    this.prepareBoard = prepareBoard;
  }

  public String getBatchNumber() {
    return batchNumber;
  }

  public void setBatchNumber(String batchNumber) {
    this.batchNumber = batchNumber;
  }

  public int getOcrLicenceUsed() {
    return ocrLicenceUsed;
  }

  public void setOcrLicenceUsed(int ocrLicenceUsed) {
    this.ocrLicenceUsed = ocrLicenceUsed;
  }

  public boolean isExternalImage() {
    return externalImage;
  }

  public void setExternalImage(boolean externalImage) {
    this.externalImage = externalImage;
  }

  public String getPartName() {
    return partName;
  }

  public void setPartName(String partName) {
    this.partName = partName;
  }

  public String getEdition() {
    return edition;
  }

  public void setEdition(String edition) {
    this.edition = edition;
  }

  public Enumerator getSubtype() {
    return subtype;
  }

  public void setSubtype(Enumerator subtype) {
    this.subtype = subtype;
  }
  

//  @Override
//  public Enumerator getImportType() {
//    Task sourcePackageObject = getSourcePackageObject();
//    if(sourcePackageObject != null){
//      if(sourcePackageObject instanceof IDTask){
//        return ((IDTask)sourcePackageObject).getImportType();
//      }
//    }
//    return null;
//  }

  public Enumerator getImportType() {
    return importType;
  }

  public void setImportType(Enumerator importType) {
    this.importType = importType;
  }

  public String getTitleUUID() {
    return titleUUID;
  }

  public void setTitleUUID(String titleUUID) {
    this.titleUUID = titleUUID;
  }

  public String getVolumeUUID() {
    return volumeUUID;
  }

  public void setVolumeUUID(String volumeUUID) {
    this.volumeUUID = volumeUUID;
  }

  public String getIssueUUID() {
    return issueUUID;
  }

  public void setIssueUUID(String issueUUID) {
    this.issueUUID = issueUUID;
  }

  public String getRecordIdentifier() {
    return recordIdentifier;
  }

  public void setRecordIdentifier(String recordIdentifier) {
    this.recordIdentifier = recordIdentifier;
  }

  public boolean isProcessScan() {
    return processScan;
  }

  public void setProcessScan(boolean processScan) {
    this.processScan = processScan;
  }

  public boolean isProcessPrepare() {
    return processPrepare;
  }

  public void setProcessPrepare(boolean processPrepare) {
    this.processPrepare = processPrepare;
  }
  
  public String getTemplateCode() {
    return templateCode;
  }

  public void setTemplateCode(String templateCode) {
    this.templateCode = templateCode;
  }

  public String getDescriptionLevel() {
    return descriptionLevel;
  }

  public void setDescriptionLevel(String descriptionLevel) {
    this.descriptionLevel = descriptionLevel;
  }
  
  
}
