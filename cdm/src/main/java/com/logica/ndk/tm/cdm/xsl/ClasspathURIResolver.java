package com.logica.ndk.tm.cdm.xsl;

import java.io.InputStream;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom URIResolver pre najdenie zdroja v xsl elementoch xsl:import alebo xsl:include - najcastejsie dalsi xsl subor
 * (pozri aj javadoc URIResolver). V pripade, ze sa resource nenajde v classpath tak sa vrati null a transformer potom
 * pouzije svoj default postup pre ziskanie zdroja.
 * 
 * @author Rudolf Daco
 */
public class ClasspathURIResolver implements URIResolver {
  private static final Logger LOG = LoggerFactory.getLogger(ClasspathURIResolver.class);

  @Override
  public Source resolve(String href, String base) throws TransformerException {
    LOG.debug("ClasspathURIResolver input href: " + href + " base: " + base);
    Source source = null;
    InputStream inputStream = null;
    try {
      URL url = getClass().getClassLoader().getResource(href);
      if (url != null) {
        inputStream = url.openStream();
      }
      if (inputStream != null) {
        LOG.debug("ClasspathURIResolver find resource for href: " + href + " Resource URL: " + url);
        source = new StreamSource(url.toString());
      }
      else {
        LOG.debug("ClasspathURIResolver didn't find resource for href: " + href + ". Transformer will use default methods to find source for this href.");
        source = null;
      }
    }
    catch (Exception e) {
      LOG.warn("ClasspathURIResolver exception. Transformer will use default methods to find source for this href.", e);
    }
    finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        }
        catch (Exception e) {
          LOG.warn("ClasspathURIResolver exception. Transformer will use default methods to find source for this href.", e);
        }
      }
    }
    return source;
  }
}
