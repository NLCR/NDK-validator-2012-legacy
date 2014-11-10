package com.logica.ndk.commons.utils.id;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.config.TmConfig;

/**
 * @author ondrusekl
 */
public abstract class ISBNUtils {

  private final transient static Logger log = LoggerFactory.getLogger(ISBNUtils.class);
  private static String ISBN_PATTERN = generateRegExPattern((List)TmConfig.instance().getProperty("utility.ISBN.pattern"));

  public static final String normalize(String dirtyValue) {
    checkNotNull(dirtyValue, "dirtyValue must not be null");
    log.info("Normalize ISBN started");
    log.info("Dirty value: " + dirtyValue);
    //String normalizedValue = dirtyValue.replaceAll("[^0-9-$xX]", "");
    String normalizedValue = dirtyValue.substring(0, indexOfSpecialCharacter(dirtyValue));
    //String normalizedValue = dirtyValue.replaceAll("[^0-9-$xX]", "");
    normalizedValue = normalizedValue.replaceAll("\\s+","") ;
    log.info("Normalized value: " + normalizedValue);
    if (!dirtyValue.matches(".*" + normalizedValue + ".*")) {
      log.error(format("Possible bad ISBN normalize. original='%s' normalized='%s'", dirtyValue, normalizedValue));
      // TODO ondrusekl (25.4.2012): Nemela by se tady vyhodit BusinessException?
    }

    if(normalizedValue.length() > 17){
    	normalizedValue = normalizedValue.substring(0, 17);
    }
    	
    return normalizedValue;
  }
  
  private static int indexOfSpecialCharacter(String s){
    for(int i = 0; i < s.length(); i++){
      char ch = s.charAt(i);
      if(ch != '-' &&  ch != 'X' &&  ch != 'x' && (ch < '0' || ch > '9')){
        return i;
      }
    }
    return s.length();
  }

  public static final boolean validate(String isbn) {
    checkNotNull(isbn, "isbn must not be null");
    checkArgument(!isbn.isEmpty(), "isbn must not be empty");
    log.info("Validate ISBN started");
    
    //String normIsbn = normalize(isbn);
    
    Pattern pattern = Pattern.compile(ISBN_PATTERN);
    Matcher matcher = pattern.matcher(isbn);

    if (!matcher.matches()) {
      return false;
    }

    String cleanIsbn = isbn.replaceAll("[\\s-]", "").toLowerCase();

    if (cleanIsbn.length() != 10 && cleanIsbn.length() != 13) {
      log.error(format("ISBN %s has wrong length. Allowed lengths are 9 or 13 numbers", isbn));
    }

    boolean result = false;
    if (cleanIsbn.length() == 10) {
      int[] volumes = new int[] { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };
      int sum = 0;
      for (int i = 0; i < cleanIsbn.length(); i++) {
        char nextNumber = cleanIsbn.charAt(i);
        int number;
        if (i == cleanIsbn.length() - 1 && nextNumber == 'x') {
          number = 10;
        }
        else {
          number = new Integer(String.valueOf(nextNumber));
        }

        sum += (number * volumes[i]);
      }

      result = sum % 11 == 0;
      if (!result) {
        log.error("ISBN-10 " + isbn + " has wrong checksum. Sum of numbers must be in module 11");
      }
    }
    else if (cleanIsbn.length() == 13) {
      int[] volumes = new int[] { 1, 3, 1, 3, 1, 3, 1, 3, 1, 3, 1, 3, 1, };
      int sum = 0;
      for (int i = 0; i < cleanIsbn.length(); i++) {
        int number = new Integer(cleanIsbn.charAt(i)).intValue();
        sum += number * volumes[i];
      }

      result = sum % 10 == 0;
      if (!result) {
        log.error("ISBN-13 " + isbn + " has wrong checksum. Sum of numbers must be in module 10");
      }
    }

    return result;
  }

  private static String generateRegExPattern(List list){
    String result = "";
    for (int i = 0; i < list.size(); i++) {
      result += (String)list.get(i);
      if(i != list.size() - 1){
        result += ",";
      }
    }
    return result;
  }
  
  public static void main(String[] args) {
    ISBNUtils.normalize("80-7277-042-X");
  }
  
}
