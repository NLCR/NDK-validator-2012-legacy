package com.logica.ndk.tm.utilities.validation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.MatchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

public class TiffinfoHelper {
  private static final String RESOLUTION_KEY = "Resolution";
  private static final String BITS_SAMPLE = "Bits/Sample";

  private final transient Logger log = LoggerFactory.getLogger(getClass());
  private static CDM cdm = new CDM();

  public static Properties getTiffinfoProp(String cdmId, String dirName, String imgName) {
    BufferedReader reader = null;
    Properties tiffInfo = new Properties();
    try {
      reader = new BufferedReader(new FileReader(cdm.getTiffinfoFile(cdmId, new File(new File(cdm.getCdmDataDir(cdmId), new File(dirName).getName()), imgName))));
      tiffInfo.load(reader);
    }
    catch (Exception e) {
      throw new SystemException("Error while reading tiffInfo file. ", e);
    }
    finally {
      if (reader != null) {
        try {
          reader.close();
        }
        catch (IOException e) {
          throw new SystemException("Error while closing tiffInfo file. ", ErrorCodes.ERROR_WHILE_READING_FILE);
        }
      }
    }
    return tiffInfo;
  }

  public static Integer getXResDPI(Properties tiffinfo) {
    MatchResult result = getResolution(tiffinfo);
    if (result == null) {
      throw new SystemException("Error while getting xDPI from tiffInfo file.", ErrorCodes.ERROR_WHILE_READING_FILE);
    }
    return Integer.valueOf(result.group(1));
  }

  public static Integer getYResDPI(Properties tiffinfo) {
    MatchResult result = getResolution(tiffinfo);
    if (result == null) {
      throw new SystemException("Error while getting yDPI from tiffInfo file.", ErrorCodes.ERROR_WHILE_READING_FILE);
    }
    return Integer.valueOf(result.group(2));
  }

  private static MatchResult getResolution(Properties tiffinfoProp) {
    String res = tiffinfoProp.getProperty(RESOLUTION_KEY).trim();
    Scanner sc = new Scanner(res);
    MatchResult result = null;
    sc.findInLine("\\s*(\\d+),\\s*(\\d+)");
    try {
      result = sc.match();
      if (result.groupCount() != 2) {
        result = null;
      }
    }
    catch (IllegalStateException e) {
      throw new SystemException("Error while getting DPI from tiffInfo file. ", ErrorCodes.ERROR_WHILE_READING_FILE);
    }
    return result;
  }

  public static String getBitsSample(Properties tiffinfoProp) {
    String res = tiffinfoProp.getProperty(BITS_SAMPLE).trim();
    return res;
  }

}
