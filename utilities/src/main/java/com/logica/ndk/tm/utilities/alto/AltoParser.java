package com.logica.ndk.tm.utilities.alto;

import gov.loc.standards.alto.v2.Alto;
import gov.loc.standards.alto.v2.Alto.Styles.TextStyle;
import gov.loc.standards.alto.v2.BlockType;
import gov.loc.standards.alto.v2.ComposedBlockType;
import gov.loc.standards.alto.v2.PageSpaceType;
import gov.loc.standards.alto.v2.StringType;
import gov.loc.standards.alto.v2.TextBlockType;
import gov.loc.standards.alto.v2.TextBlockType.TextLine;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.logica.ndk.tm.cdm.JAXBContextPool;

/**
 * Alto/XML parser
 * 
 * @author skorepam
 */
public class AltoParser {

  private final transient Logger log = LoggerFactory.getLogger(getClass());
//  private static AltoParser instance;
  
//  public static Unmarshaller jaxbUnmarshaller;

  private List<AltoWord> result = Lists.newArrayList();
  /*
  protected AltoParser() {
    super();  
  }
  
  public static AltoParser instance(){
    if(instance == null){
      instance = new AltoParser();
    }
    return instance;
  }
*/
  /**
   * Parses a single ALTO file and returns the contained words as list of AltoWord objects.
   * 
   * @param f
   *          - Input file
   * @return List of words with their properties
   * @throws JAXBException
   */
  public List<AltoWord> parse(File f) throws JAXBException {
    result = Lists.newArrayList();
 
   	JAXBContext jaxbContext = JAXBContextPool.getContext(Alto.class);    	    
   	Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    
    Alto alto = (Alto) jaxbUnmarshaller.unmarshal(f);
    Alto.Layout.Page page = alto.getLayout().getPage().get(0); // uz tady je alokovan printSpace - PageSpaceType
    PageSpaceType pageSpaceType = page.getPrintSpace();
    
    if (pageSpaceType == null){
      return result;
    }
    
    for (BlockType blockType : pageSpaceType.getTextBlockOrIllustrationOrGraphicalElement()) {
      if (blockType instanceof TextBlockType) {
        result.addAll(processTextBlockType((TextBlockType) blockType));
      }
      else if (blockType instanceof ComposedBlockType) {
        processComposedtBlockType((ComposedBlockType) blockType);
      }
    }

    return result;
  }

  private void processComposedtBlockType(ComposedBlockType composedBlock) {

    log.debug("Procesing composed block type");
    for (BlockType blockType : composedBlock.getTextBlockOrIllustrationOrGraphicalElement()) {
      if (blockType instanceof TextBlockType) {
        result.addAll(processTextBlockType((TextBlockType) blockType));
      }
    }

  }

  private List<AltoWord> processTextBlockType(TextBlockType textBlock) {

    log.debug("Processing text block type");
    Font font = getFont(textBlock.getSTYLEREFS(), new Font());
    List<AltoWord> result = Lists.newArrayListWithExpectedSize(textBlock.getTextLine().size());
    for (TextLine textLine : textBlock.getTextLine()) {
      font = getFont(textBlock.getSTYLEREFS(), font);
      for (Object stringOrSp : textLine.getStringAndSP()) {
        if (stringOrSp instanceof StringType) {
          StringType stringType = (StringType) stringOrSp;
          AltoWord altoWord = new AltoWord();
          altoWord.setFont(getFont(stringType.getSTYLEREFS(), font));
          altoWord.setHeight(stringType.getHEIGHT());
          altoWord.setLeft(stringType.getHPOS());
          altoWord.setText(stringType.getCONTENT());
          altoWord.setTop(stringType.getVPOS());
          altoWord.setWidth(stringType.getWIDTH());

          result.add(altoWord);
        }
      }
    }

    return result;
  }

  private Font getFont(List<Object> refs, Font old) {
    for (Object ro : refs) {
      if (ro instanceof TextStyle) {
        Font font = new Font();
        TextStyle ref = (TextStyle) ro;
        font.setSize(ref.getFONTSIZE());
        font.setFamily(ref.getFONTFAMILY());
        if (ref.getFONTSTYLE() != null && ref.getFONTSTYLE().size() > 0) {
          font.setStyle(ref.getFONTSTYLE().get(0)); // TODO must findout how it wokrs
        }
        if (ref.getFONTWIDTH() != null) {
          font.setWidth(ref.getFONTWIDTH().name());
        }
        
        return font;
      }
    }
    return old;
  } 
  
}
