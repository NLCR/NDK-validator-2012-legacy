/**
 * 
 */
package com.logica.ndk.tm.utilities.jhove;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.mule.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.utilities.ErrorCodes;

/**
 * @author kovalcikm
 */
public class KduHelper {

  HashMap<String, List<String>> propertiesMap;
  protected final transient Logger log = LoggerFactory.getLogger(getClass());

  public KduHelper(File kduFile) {

    readToMap(kduFile);
  }

  private void readToMap(File kduFile) {
    propertiesMap = new HashMap<String, List<String>>();
    BufferedReader br = null;
    try {
      try {
        br = new BufferedReader(new InputStreamReader(new FileInputStream(kduFile)));

        String key;
        List<String> value = new ArrayList<String>();
        String line = null;
        line = br.readLine();
        while (line != null) {
          if (line.split("=").length != 2) {
            line = br.readLine();
            continue;
          }
          key = line.split("=")[0];
          value = normalizeValue(line.split("=")[1]);
          propertiesMap.put(key, value);
          line = br.readLine();
        }
      }
      catch (Exception e) {
        throw new com.logica.ndk.tm.utilities.SystemException("Error while reading file: " + kduFile, ErrorCodes.ERROR_WHILE_READING_FILE);
      }
      finally {
        if (br != null) {
          br.close();
        }
      }
    }
    catch (IOException ioEx) {
      log.warn("BufferedReader closing failed.");
    }
  }

  private List<String> normalizeValue(String value) {
    ArrayList<String> valueList = new ArrayList<String>();
    int firstIndex = 0;
    int secondIndex = 1;
    if (!value.contains("{")) {
      Collections.addAll(valueList, value.split(","));
    }
    else {
      while (true) {
        firstIndex = value.indexOf("{");
        secondIndex = value.indexOf("}");
        valueList.add(value.substring(firstIndex + 1, secondIndex));
        value = value.substring(secondIndex + 1, value.length());
        if (value.length() == 0)
          break;
      }
    }
    return valueList;
  }

  public List<String> getProperty(String property) {
    try {
      return propertiesMap.get(property);
    }
    catch (Exception e) {
      return null;
    }
  }
}
