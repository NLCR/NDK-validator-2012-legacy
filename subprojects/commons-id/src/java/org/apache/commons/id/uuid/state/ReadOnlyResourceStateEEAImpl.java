package org.apache.commons.id.uuid.state;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Extends ReadOnlyResourceStateImpl to read config flie as file with absolute path not only as classpath resource.
 * 
 * @author Rudolf Daco
 */
public class ReadOnlyResourceStateEEAImpl extends ReadOnlyResourceStateImpl {
  private static final long serialVersionUID = -5052471641682313782L;

  public void load() throws Exception {
    // Get the resource name
    String resourceName = System.getProperty(CONFIG_FILENAME_KEY);
    if (resourceName == null) {
      throw new IllegalStateException("No value set for system property: "
          + CONFIG_FILENAME_KEY);
    }

    // Load the resource from classpath
    InputStream in = null;
    try {
      in = ClassLoader.getSystemResourceAsStream(resourceName);
      if (in == null) {
        // try to load at absolute file path
        in = new FileInputStream(resourceName);
      }
      //Do the XML parsing
      parse(in);
    }
    finally {
      if (in != null) {
        try {
          in.close();
        }
        catch (IOException ioe) {
          //Nothing to do at this point.
        }
      }
    }
  }
}
