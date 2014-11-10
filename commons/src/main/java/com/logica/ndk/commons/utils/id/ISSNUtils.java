/**
 * 
 */
package com.logica.ndk.commons.utils.id;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.config.TmConfig;

/**
 * @author kovalcikm
 */
public class ISSNUtils {

  private final transient static Logger log = LoggerFactory.getLogger(ISSNUtils.class);

  private static String ISSN_PATTERN = TmConfig.instance().getString("utility.ISSN.pattern");

  public static final String normalize(String dirtyValue) {
    checkNotNull(dirtyValue, "dirtyValue must not be null");
    log.info("Normalize ISSN started");
    log.info("Dirty value: " + dirtyValue);
    String normalizedValue = dirtyValue.replaceAll("[^0-9-$xX]", "");

    if (!normalizedValue.contains("-") && normalizedValue.length() == 8) { // "-" is missing but number of digits is OK
      normalizedValue = normalizedValue.substring(0, 4) + "-" + normalizedValue.substring(4, normalizedValue.length());
    }

    log.info("Normalized value: " + normalizedValue);
    if (!dirtyValue.matches(".*" + normalizedValue + ".*")) {
      log.error(format("Possible bad ISSN normalize. original='%s' normalized='%s'", dirtyValue, normalizedValue));
    }
    return normalizedValue;
  }

  public static final boolean validate(String issn) {
    checkNotNull(issn, "isbn must not be null");
    checkArgument(!issn.isEmpty(), "isbn must not be empty");
    log.info("Validate ISSN started");

    Pattern pattern = Pattern.compile(ISSN_PATTERN);
    Matcher matcher = pattern.matcher(issn);

    if (!matcher.matches()) {
      return false;
    }

    String cleanIssn = issn.replaceAll("[\\s-]", "").toLowerCase();

    if (cleanIssn.length() != 8) {
      log.error(format("ISSN %s has wrong length. Allowed lengths are 8 characters", issn));
    }

    int sum = 0;
    int value;
    char[] issnAsChars = cleanIssn.toCharArray();
    if (cleanIssn.length() == 8) {
      for (int i = 0; i <= 6; i++) {
        try
        {
          value = Character.getNumericValue(issnAsChars[i]) * (8 - i);
        }
        catch (NumberFormatException nfe)
        {
          return false;
        }
        sum += value;
      }
    }

    int lastDigit = 11 - (sum % 11);
    if (lastDigit == 10) {
      if (issnAsChars[7] == 'x') {
        return true;
      }
      else {
        log.error("ISSN " + issn + " is wrong. Last value has to be: X or x");
        return false;
      }
    }
    else {
      if (lastDigit == Character.getNumericValue(issnAsChars[7])) {
        return true;
      }
    }
    log.error("ISSN " + issn + " is wrong. Last value has to be: 11 - (sum(position*value) mod 11) = " + lastDigit);
    return false;
  }
}
