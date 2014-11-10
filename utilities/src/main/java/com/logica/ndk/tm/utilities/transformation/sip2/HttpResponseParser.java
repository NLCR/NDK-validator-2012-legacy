package com.logica.ndk.tm.utilities.transformation.sip2;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponseParser {

  protected final transient Logger log = LoggerFactory.getLogger(getClass());
  
  private Map<String, String> map;

  public HttpResponseParser(Map<String, String> map) {
    this.map = map;
  }

  public HttpResponseParser(String json) {
    parseJsonString(json);
  }

  public void parseJsonString(String json) {
    map = new HashMap<String, String>();
    String[] split = json.replace("{", "").replace("}", "").split(",");

    for (String pair : split) {
      String[] splitPair = pair.split("\":\"");
      if (splitPair.length != 2) {
          log.warn("Cannot parse text to map: " + json);
//        throw new HtppResponseParserException("Cannot parse text to map: " + json);
      }
      else {
        map.put(splitPair[0].replace("\"", ""), splitPair[1].replace("\"", ""));
      }
    }
  }

  public String getValue(String type) {
    return map.get(type);
  }

  public Map<String, String> getMap() {
    return map;
  }
//  public static void main(String[] args) {
//   args new HttpResponseParser(new HashMap<String, String>()).
//  }
}
