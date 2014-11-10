package com.logica.ndk.tm.utilities.alto;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.introspect.BasicClassIntrospector.GetterMethodFilter;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.jdom.Element;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.cdm.CDMMetsHelper;
import com.logica.ndk.tm.cdm.CDMSchema.CDMSchemaDir;
import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.alto.exception.InconsistentDataException;
import com.logica.ndk.tm.utilities.alto.exception.InvalidSourceFolderException;
import com.logica.ndk.tm.utilities.integration.wf.CreateImportsFromLTPImpl;
import com.logica.ndk.tm.utilities.jhove.MixHelper;
import com.logica.ndk.tm.utilities.transformation.jpeg2000.CreateImagesForPDFImpl;

/**
 * ALTO/PDF creator. Creates two-layer PDF containing the base images covered
 * with hidden OCR text for each page.
 * 
 * @author majdaf
 */
public class CreatePDFImpl extends AbstractUtility {
  private final static int OUTPUT_DPI = 72;
  float ALTO_SCALE_RATIO_V = 0.62f;//1.035f;  // Due to rounding or other magic there is a slight difference between the scale of IMG a ALTO
  int ALTO_SCALE_OFFSET_V = 15;//37;       // Same way there is a vertical offset

  private static final String XPATH_TO_FILEID = "/mets:fptr[starts-with(@FILEID, 'UC_')]/@FILEID";
  private static final String XPATH_TO_FILE_NAME = "/mets:mets/mets:fileSec/mets:fileGrp[@ID='UC_IMGGRP']/mets:file[@ID='{fileId}']/mets:FLocat/@xlink:href";
  private static final String XPATH_TO_PYHSICAL_MAP = "//mets:mets/mets:structMap[@TYPE='PHYSICAL']/mets:div/mets:div";

  public void execute(String cdmId, Boolean isPublic, String abstractionDir) throws InvalidSourceFolderException, InconsistentDataException {
    log.info("Creating ALTO/PDF for CDM ID: " + cdmId);
    log.info("Abstraction dir: " + abstractionDir);

    Preconditions.checkNotNull(cdmId);
    Preconditions.checkNotNull(abstractionDir);

    CDM cdm = new CDM();
    CDMMetsHelper h = new CDMMetsHelper();

    File imgDir = cdm.getImagesPDFDir(cdmId);
    if (!imgDir.exists()) {
      log.info("Images dir does not exist, creating images");
      new CreateImagesForPDFImpl().execute(cdmId);

    }
    File xmlDir = cdm.getAltoDir(cdmId);
    File mixDir = new File(cdm.getWorkspaceDir(cdmId).getAbsolutePath() + "/mix/postprocessingData");

    log.debug(imgDir.getAbsolutePath());
    if (!imgDir.exists() || !xmlDir.exists() || !mixDir.exists()) {
      throw new InvalidSourceFolderException("IMG, ALTO or MIX fodler does not exist in CDM for ID " + cdmId);
    }

    PDFHelper pdfHelper = new PDFHelper();
    File targetFile = new File(pdfHelper.getPdfTargetDir(cdmId, abstractionDir), "PDF_" + cdmId + ".pdf");

    //copy MODS to xml
    pdfHelper.copyMods(cdmId, abstractionDir);

    //create policy file
    pdfHelper.createPolicyFile(cdmId, isPublic, abstractionDir);

    // checks source folders and files in source folders
    int countOfImgFiles = checkFolderAndFiles(imgDir, "*.jpg");
    int countOfXMLFiles = checkFolderAndFiles(xmlDir, "*.xml");
    int countOfMIXFiles = checkFolderAndFiles(mixDir, "*.mix");

    log.debug("Number of IMG files = " + countOfImgFiles);
    log.debug("Number of XML files = " + countOfXMLFiles);
    log.debug("Number of MIX files = " + countOfMIXFiles);

//    if (countOfImgFiles != countOfXMLFiles || countOfImgFiles != countOfMIXFiles || countOfXMLFiles != countOfMIXFiles) {
    if (countOfImgFiles != countOfXMLFiles) {
      throw new InvalidSourceFolderException("XML folder must contain same amount of files as IMG folder");
    }

    List<org.dom4j.Node> physicalMapDivs = h.getNodesFromMets(XPATH_TO_PYHSICAL_MAP, cdm, cdmId);
    List<String> fileNames = new ArrayList<String>();

    for (org.dom4j.Node node : physicalMapDivs) {
      String fileId = h.getValueFormMets(node.getUniquePath() + XPATH_TO_FILEID, cdm, cdmId);

      String fileName = h.getValueFormMets(XPATH_TO_FILE_NAME.replace("{fileId}", fileId), cdm, cdmId);
      fileNames.add(fileName.substring((CDMSchemaDir.UC_DIR.getDirName() + "/").length(), fileName.length() - ".jp2".length()) + ".jpg");
    }

    //String[] imgFileNames = imgDir.list();  
//    String fileName;
//    String originaName;
//    for (int i=0; i<fileNames.size();i++){
//      fileName = FilenameUtils.removeExtension(FilenameUtils.removeExtension(StringUtils.substringAfter(fileNames.get(i), "_"))); //removes prefix and two extensions
//      originaName = h.getOldName(fileName, cdmId);
//      fileNames.set(i, originaName+".tif.jpg");
//    }
    String[] imgFileNames = fileNames.toArray(new String[0]);
    String[] xmlFileNames = xmlDir.list();
    String[] mixFileNames = mixDir.list();

    // checks the names of files if the same
//    filesNameCheck(imgFileNames, xmlFileNames, mixFileNames);

    try {
      // creates PDF document
      Document document = new Document();
      PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(targetFile));

      /* In memory of Zdenek Bazant - original test data - LOL :)
      document.addTitle("O delfínech");
      document.addAuthor("Frank Provost");
      document.addSubject("Rybí pokusy na delfínech.");
      document.addCreator("Logica Dolphin Ltd.");
      */

      document.addTitle(h.getDocumentTitle(cdm, cdmId));
      document.addAuthor(getCreator(cdmId));
      document.addSubject("");

      document.addCreator(TmConfig.instance().getString("meta.creator.code"));
      document.open();
      // For each page (image) do

      for (String imgFileName : imgFileNames) {

        // Prepare data for page
        //log.debug("Processing file " + imgFileName);
        String xmlFileName = getXMLName(imgFileName);
        String mixFileName = getMIXName(imgFileName, cdmId);

        // Open files
        File xmlFile = new File(xmlDir.getAbsolutePath() + "/" + xmlFileName);
        File mixFile = new File(mixDir.getAbsolutePath() + "/" + mixFileName);
        String ident = FilenameUtils.removeExtension(FilenameUtils.removeExtension(StringUtils.substringAfter(imgFileName, "_")));

        //check file with new name exist
        File imgFile = new File(imgDir.getAbsolutePath() + "/" + imgFileName);
        if (!imgFile.exists()) {
          log.info(String.format("File (%s) with new name not found! Try find with old name.", imgFile.getAbsolutePath()));
          imgFileName = h.getOldName(ident, cdmId);
          imgFile = new File(imgDir.getAbsolutePath() + "/" + imgFileName + ".jpg");
          log.info(String.format("File (%s) with old name", imgFile.getAbsolutePath()));
        }
        // Parses xml file
        //log.debug("Parsing XML");

        List<AltoWord> list = new AltoParser().parse(xmlFile);

        // Extract img metadat
        //log.debug("Retreiving img metadata");
        Image imgToInsert = Image.getInstance(imgFile.getAbsolutePath());
        int hDPI = MixHelper.getInstance(mixFile.getAbsolutePath()).getHorizontalDpi();
        int vDPI = MixHelper.getInstance(mixFile.getAbsolutePath()).getVerticalDpi();
        int xPx = MixHelper.getInstance(mixFile.getAbsolutePath()).getImageWidth();
        int yPx = MixHelper.getInstance(mixFile.getAbsolutePath()).getImageHeight();

        /*log.debug("hDPI = " + hDPI);
        log.debug("vDPI = " + vDPI);
        log.debug("xPx = " + xPx);
        log.debug("yPx = " + yPx);
        
        // Add PDF page
        log.debug("Adding page");*/
        //document.newPage();
        PdfContentByte cb = writer.getDirectContent();
        Rectangle pageSize = new Rectangle(scale(xPx, hDPI), scale(yPx, vDPI)); // size of the rectangle same as source
        document.setPageSize(pageSize);
        document.newPage();

        // Add image
        imgToInsert.setAbsolutePosition(0, 0);
        imgToInsert.scaleToFit(scale(xPx, hDPI), scale(yPx, vDPI));
        document.add(imgToInsert);

        // inserts a hidden layer of text into the pdf
        for (AltoWord word : list) {
          insertHiddenText(cb, PdfContentByte.TEXT_RENDER_MODE_INVISIBLE, word, hDPI, vDPI, yPx); // For visible use PdfContentByte.TEXT_RENDER_MODE_FILL
        }
      }
      document.close();
      log.info("PDF created: " + targetFile);

    }
    catch (JAXBException jaxbe) {
      log.error("Error during alto parsing occured.", jaxbe);
      throw new InconsistentDataException("Error during alto parsing occured");
    }
    catch (DocumentException de) {
      log.error("Error during pdf document processing.", de);
      throw new SystemException("Error during pdf document processing", ErrorCodes.PDF_DOCUMENT_ERROR);
    }
    catch (Exception ioe) {
      log.error("Error during processing of alto.", ioe);
      throw new SystemException("Error during processing of alto", ErrorCodes.PDF_ALTO_ERROR);
    }
  }

  private int checkFolderAndFiles(File f, String suffixes) throws InvalidSourceFolderException {
    // checks a source folder
    if (!f.exists() || !f.isDirectory()) {
      throw new InvalidSourceFolderException("Unable to read the folder " + f.getName());
    }
    final IOFileFilter wildCardFilter = new WildcardFileFilter(suffixes, IOCase.INSENSITIVE);
    final IOFileFilter dirFilter = FileFilterUtils.falseFileFilter();
    Collection<File> result = FileUtils.listFiles(f, wildCardFilter, dirFilter);

    return result.size();

  }

//  private void filesNameCheck(String[] imgFileNames, String[] xmlFileNames, String[] mixFileNames) throws InconsistentDataException {
//    List<String> xmlNames = Arrays.asList(xmlFileNames);
//    List<String> mixNames = Arrays.asList(mixFileNames);
//    for (String imgFileName : imgFileNames) {
//      String xmlFileName = getXMLName(imgFileName);
//      String mixFileName = getMIXName(imgFileName);
//      if (!xmlNames.contains(xmlFileName)) {
//        throw new InconsistentDataException("ALTO/XML "+ xmlFileName + " not found for " + imgFileName);
//      }
//      if (!mixNames.contains(mixFileName)) {
//        throw new InconsistentDataException("MIX "+ mixFileName + "not found for " + imgFileName);
//      } 
//    }
//  }

  private void insertHiddenText(PdfContentByte cb, int renderingMode, AltoWord word, int hDPI, int vDPI, int yPx) throws DocumentException, IOException {
    cb.beginText();
    float fontSize = word.getFont().getSize();
    if (fontSize <= 0f) {
      fontSize = 0.1f;
    }

    //fontSize *= 4;

    cb.moveText(
        scale(word.getLeft(), hDPI),
        (scale(yPx - word.getTop(), vDPI)) - fontSize * ALTO_SCALE_RATIO_V
        //scale(word.getLeft(), hDPI),
        //scale(yPx - word.getTop(), vDPI)
        );
    cb.setFontAndSize(getFont(word.getFont().getFamily()), scale(fontSize, hDPI) * 4.5F); // FIXME majdaf - 5x hack

    cb.setTextRenderingMode(renderingMode);
    /*log.debug(word.getText());
    log.debug("FontSize: " + word.getFont().getSize());
    log.debug("FontFamily: " + word.getFont().getFamily());*/

    cb.showText(word.getText());
    cb.endText();
  }

  private String getXMLName(String imgName) {
//    String base = imgName.substring(0, imgName.lastIndexOf("."));
    String base = FilenameUtils.removeExtension(imgName);
    base = FilenameUtils.removeExtension(base);
    base = StringUtils.substringAfter(base, "_");
    return "ALTO_" + base + ".xml";
  }

  private String getMIXName(String imgName, String cdmId) {
    String base = FilenameUtils.removeExtension(imgName);
    base = FilenameUtils.removeExtension(base);
    String pageId = StringUtils.substringAfter(base, "_");
    CDMMetsHelper helper = new CDMMetsHelper();
    base = helper.getOldName(pageId, cdmId);
    return base + ".tif.xml.mix";
  }

  private float scale(int px, int dpi) {
    return scale((float) px, dpi);
  }

  private float scale(float px, int dpi) {
    return px / dpi * OUTPUT_DPI;
  }

  private BaseFont getFont(String fontName) throws DocumentException, IOException {
    String fontFamily;
    if (fontName == null) {
      fontName = BaseFont.TIMES_ROMAN;
    }
    if (fontName.contains("Courier")) {
      fontFamily = BaseFont.COURIER;
    }
    else {
      fontFamily = BaseFont.TIMES_ROMAN;
    }

    return BaseFont.createFont(fontFamily, BaseFont.CP1250, BaseFont.NOT_EMBEDDED);

  }

  private String getCreator(String cdmId) {
    //CDMMetsHelper helper = new CDMMetsHelper();
    //org.dom4j.Document doc = DocumentHelper.createDocument();

    SAXReader reader = new SAXReader();
    org.dom4j.Document metsDocument;
    try {
      metsDocument = reader.read(cdm.getMetsFile(cdmId));
      XPath xPath = DocumentHelper.createXPath("//dc:creator");
      xPath.setNamespaceURIs(ImmutableMap.<String, String> of("dc", "http://purl.org/dc/elements/1.1/"));
      Node node = xPath.selectSingleNode(metsDocument);
      return node.getText();

    }
    catch (Exception e) {
      return "";
    }
  }
}
