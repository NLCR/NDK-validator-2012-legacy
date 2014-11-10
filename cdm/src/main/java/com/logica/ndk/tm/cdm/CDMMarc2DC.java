package com.logica.ndk.tm.cdm;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.apache.xerces.util.DOMUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.logica.ndk.commons.utils.id.ISBNUtils;
import com.logica.ndk.commons.utils.id.ISSNUtils;
import com.logica.ndk.tm.cdm.xsl.ClasspathURIResolver;

public class CDMMarc2DC {
	
	private static final String XSL_OAIMARC_TO_MARC21 = "xsl/OAIMARC2MARC21slim-NDK.xsl";
	private static final String XSL_MARC21_TO_DC = "xsl/MARC21slim2OAIDC.xsl";
	private static final Logger LOG = LoggerFactory.getLogger(CDMMarc2DC.class);
	private static final String DC_ISBN = "URN:ISBN:";
	
	public static Document transformAlephMarcToDC(File inputFile) throws IOException, TransformerException, SAXException, ParserConfigurationException {
		
		checkNotNull(inputFile);
	    InputStream input = null;
	    CDMMetsHelper cdmMetsHelper = new CDMMetsHelper();
	    ByteArrayOutputStream marc21Out = null;
	    ByteArrayInputStream marc21In = null;
	    ByteArrayOutputStream dcOut = null;
	    ByteArrayInputStream dcIn = null;
	    InputStream styleA2M = null;
	    InputStream styleM2M = null;
	    try {
	      input = new XMLHelper.Input(inputFile);
	      // transform from aleph/oai-marc to marc21
	      marc21Out = new ByteArrayOutputStream();
	      styleA2M = new XMLHelper.Input(XSL_OAIMARC_TO_MARC21);
	      XMLHelper.transformXML(input, marc21Out, styleA2M);
	      // transform from marc21 to mods
	      marc21In = new ByteArrayInputStream(marc21Out.toByteArray());
	      if (LOG.isDebugEnabled()) {
	        LOG.debug("MARC21:\n" + new String(marc21Out.toByteArray(), "UTF-8") + "\n\n");
	      }
	      dcOut = new ByteArrayOutputStream();
	      styleM2M = new XMLHelper.Input(XSL_MARC21_TO_DC);
	      // pouzijeme custom URIResolver pre spavne najdenie xsl ktore su included v hlavom xsl. Default URIResolver nevie najst tieto xls na classpath 
	      XMLHelper.transformXML(marc21In, dcOut, styleM2M, new ClasspathURIResolver());
	      // parse and patch DC
	      dcIn = new ByteArrayInputStream(dcOut.toByteArray());
	      final Document dcDoc = XMLHelper.parseXML(dcIn);
	      
	   // Normalize ISSN
        Element issnIdentifier = DOMUtil.getFirstChildElementNS(dcDoc.getDocumentElement(), "http://purl.org/dc/elements/1.1/", "identifier");
        while (issnIdentifier != null) {
          if (issnIdentifier.getTextContent() != null && issnIdentifier.getTextContent().startsWith("issn:")) {
            String dirtyValue = issnIdentifier.getTextContent();
            String normalizedValue = ISSNUtils.normalize(dirtyValue);
            if (!dirtyValue.matches(normalizedValue)) {
              if (normalizedValue != null && !"".equals(normalizedValue)) {
                LOG.info(format("ISSN normalized. original='%s' normalized='%s'", dirtyValue, normalizedValue));
                if (ISSNUtils.validate(normalizedValue)) {
                  LOG.info("ISSN is valid: " + normalizedValue);
                  issnIdentifier.setTextContent(normalizedValue);
                }
                else {
                  LOG.info("ISSN is not valid: " + normalizedValue + "going to remove it.");
                  DOMUtil.getParent(issnIdentifier).removeChild(issnIdentifier);
                }
              }
              else {
                LOG.info(format("ISSN empty after normalization. Going to remove it. original='%s' normalized='%s'", dirtyValue, normalizedValue));
                DOMUtil.getParent(issnIdentifier).removeChild(issnIdentifier);
              }
            }
            
          }
          issnIdentifier = DOMUtil.getNextSiblingElementNS(issnIdentifier, "http://www.loc.gov/mods/v3", "identifier");
        }       
	      
	      
	      //FIXME should be in the transformation
	      //pacth the the DC document
	      
	      // Normalize ISBN
	      Element isbnIdentifier = DOMUtil.getFirstChildElementNS(dcDoc.getDocumentElement(), "http://purl.org/dc/elements/1.1/", "identifier");
	      while (isbnIdentifier != null) {
	        if (isbnIdentifier.getTextContent() != null && isbnIdentifier.getTextContent().startsWith("URN:ISBN")) {
	          String dirtyValue = isbnIdentifier.getTextContent();
	          String normalizedValue = ISBNUtils.normalize(dirtyValue);
	          if (!dirtyValue.matches(normalizedValue)) {
	            if (normalizedValue != null && !"".equals(normalizedValue)) {
	              LOG.info(format("ISBN normalized. original='%s' normalized='%s'", dirtyValue, normalizedValue));
	              isbnIdentifier.setTextContent(normalizedValue);  
	            }
	            else {
	              LOG.info(format("ISBN empty after normalization. Going to remove it. original='%s' normalized='%s'", dirtyValue, normalizedValue));
	              DOMUtil.getParent(isbnIdentifier).removeChild(isbnIdentifier);
	            }
	          }
	          
	        }
	        isbnIdentifier = DOMUtil.getNextSiblingElementNS(isbnIdentifier, "http://www.loc.gov/mods/v3", "identifier");
	      }	      
	      
	      return dcDoc;
	    } finally {
	        IOUtils.closeQuietly(marc21Out);
	        IOUtils.closeQuietly(marc21In);
	        IOUtils.closeQuietly(dcOut);
	        IOUtils.closeQuietly(dcIn);
	        IOUtils.closeQuietly(styleA2M);
	        IOUtils.closeQuietly(styleM2M);
	      }
		
	}
}
	



