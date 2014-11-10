package com.logica.ndk.tm.utilities.transformation.scantailor;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.cdm.PerThreadDocBuilderFactory;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.jhove.MixHelper;

public class CreateScantailorConfigImpl extends AbstractUtility {

//  private String agent;
//  private String agentVersion;
//  private static final String AGENT_ROLE = "machine";
//  private static final String FORMAT_DESIGNATION_NAME = "image/tiff";
//  private static final String FORMAT_REGISTRY_KEY = "fmt/353";
//  private static final String PRESERVATION_LEVEL_VALUE = "deleted";

  private static final String ST_PROJECT_EXTENSION = "scanTailor";

  public String execute(final String cdmId) {
    checkNotNull(cdmId, "cdmId must not be null");
    log.info("execute started");

//    agent = TmConfig.instance().getString("utility.scantailor.profile.agentName");
//    agentVersion = TmConfig.instance().getString("utility.scantailor.profile.agentVersion");

//    final String[] fileExts = TmConfig.instance().getStringArray("utility.fileChar.imgExtensions");
//    final boolean recursive = TmConfig.instance().getBoolean("utility.fileChar.recursive", false);

    final File scantailorConfigsDir = cdm.getScantailorConfigsDir(cdmId);
    if (!scantailorConfigsDir.exists()) {
      scantailorConfigsDir.mkdirs();
    }

//      final IOFileFilter wildCardFilter = new WildcardFileFilter(fileExts, IOCase.INSENSITIVE);
//      final IOFileFilter dirFilter = recursive ? FileFilterUtils.trueFileFilter() : FileFilterUtils.falseFileFilter();
//      final Collection<File> listFiles = FileUtils.listFiles(cdm.getFlatDataDir(cdmId), wildCardFilter, dirFilter);
    final Collection<File> listFiles = RunScantailorAbstract.getRelevantImages(cdmId, cdm.getFlatDataDir(cdmId), cdm);
    final Multimap<String, File> filesMutliMap = ArrayListMultimap.<String, File> create();

    for (final File file : listFiles) {
      final String scanId = file.getName().substring(0, file.getName().indexOf("_"));
      filesMutliMap.put(scanId, file);
    }
    try {
      generateFromMultiMap(filesMutliMap, cdmId, scantailorConfigsDir);
    }
    catch (SAXException e) {
      throw new SystemException("Exception during create XML document", ErrorCodes.XML_CREATION_FAILED);
    }
    log.trace("execute finished");
    return ResponseStatus.RESPONSE_OK;
  }

  @RetryOnFailure(attempts = 3, types = SAXException.class)
  private void generateFromMultiMap(Multimap<String, File> filesMutliMap, String cdmId, File scantailorConfigsDir) throws SAXException
  {
    try {
      for (final String scanId : filesMutliMap.keySet()) {
        final File outFile = new File(scantailorConfigsDir, scanId + "." + ST_PROJECT_EXTENSION);
        log.info("Output scantailor file: " + outFile.getPath());
        if (!outFile.exists()) {
          log.info("Output scantailor file does not exists: " + outFile.getPath());

          final Document document = DocumentHelper.createDocument();
          //project definition
          final Element projectElement = document.addElement("project")
              //            .addAttribute("outputDirectory", cdm.getPostprocessingDataDir(cdmId).getAbsolutePath())
              .addAttribute("outputDirectory", cdm.getScantailorTempOutDir(cdmId).getAbsolutePath())
              .addAttribute("layoutDirection", "LTR");

          final Element directoriesElement = projectElement.addElement("directories");
          //files section
          final Element filesElement = projectElement.addElement("files");
          // images section
          final Element imagesElement = projectElement.addElement("images");

          directoriesElement.addElement("directory")
              //            .addAttribute("path", cdm.getFlatDataDir(cdmId).getAbsolutePath())          
              .addAttribute("path", cdm.getJpgTiffImagePath(cdmId).getAbsolutePath())
              .addAttribute("id", "1");
          int idNUmber = 1;
          for (final File file : filesMutliMap.get(scanId)) {
            log.debug("Processing file {}", file.getAbsolutePath());
            final String mixFilePath = new File(cdm.getWorkspaceDir(cdmId).getAbsolutePath() + "/mix/flatData/" + file.getName() + ".xml.mix").getAbsolutePath(); // FIMXE ondrusekl - /mix/flatData bud dynamicky z CDM nebo z nejake konfigurace
            final MixHelper mixHelper = new MixHelper(mixFilePath);
            filesElement.addElement("file")
                .addAttribute("dirId", "1")
                .addAttribute("id", Integer.toString(idNUmber))
                .addAttribute("name", file.getName());
            final Element imageElement = imagesElement.addElement("image")
                .addAttribute("subPages", "1")
                .addAttribute("fileImage", "0")
                .addAttribute("fileId", Integer.toString(idNUmber))
                .addAttribute("id", Integer.toString(idNUmber));
            imageElement.addElement("size")
                .addAttribute("width", String.valueOf(mixHelper.getImageWidth()))
                .addAttribute("height", String.valueOf(mixHelper.getImageHeight()));
            imageElement.addElement("dpi")
                .addAttribute("vertical", String.valueOf(mixHelper.getVerticalDpi()))
                .addAttribute("horizontal", String.valueOf(mixHelper.getHorizontalDpi()));

            log.debug("result xml = {}", document.asXML());

            // add transormation event
//        PremisCsvRecord record = new PremisCsvRecord(
//            new Date(),
//            getUtlilityName(),
//            getUtilityVersion(),
//            Operation.capture_digitalization,
//            CDMSchema.CDMSchemaDir.FLAT_DATA_DIR.getDirName(),
//            agent,
//            agentVersion,
//            AGENT_ROLE,
//            file,
//            OperationStatus.OK,
//            FORMAT_DESIGNATION_NAME,
//            FORMAT_REGISTRY_KEY,
//            PRESERVATION_LEVEL_VALUE);
//        cdm.addTransformationEvent(cdmId, record);
            idNUmber++;
          }
          final XMLWriter writer = new XMLWriter(new FileWriterWithEncoding(outFile, "UTF-8"), OutputFormat.createPrettyPrint());
          writer.write(document);
          writer.flush();
          writer.close();
        }
        DocumentBuilderFactory factory = PerThreadDocBuilderFactory.getDocumentBuilderFactory();
        try {
          org.w3c.dom.Document document = factory.newDocumentBuilder().parse(outFile);
        }
        catch (SAXException se) {
          FileUtils.deleteQuietly(outFile);
          log.debug("Deleting mix file and retrying ...", se);
          throw se;
        }
      }
    }
    catch (SAXException se) {
      throw new SAXException();
    }
    catch (final Exception e) {
      throw new SystemException("Exception during create XML doument", ErrorCodes.XML_CREATION_FAILED);
    }
  }
  // CONFIG FILE EXAMPLE

  //  <project outputDirectory="D:\temp\sc_in\out" layoutDirection="LTR">
  //  <directories>
  //    <directory path="D:/temp/sc_in" id="1"/>
  //  </directories>
  //  <files>
  //    <file dirId="1" id="2" name="fabia1.jpg"/>
  //    <file dirId="1" id="6" name="CCITT_1.TIF"/>
  //  </files>
  //  <images>
  //    <image subPages="2" fileImage="0" fileId="2" id="3">
  //      <size width="592" height="400"/>
  //      <dpi vertical="600" horizontal="600"/>
  //    </image>
  //    <image subPages="1" fileImage="0" fileId="6" id="7">
  //      <size width="1728" height="2376"/>
  //      <dpi vertical="200" horizontal="200"/>
  //    </image>
  //  </images>    
  //</project>

}
