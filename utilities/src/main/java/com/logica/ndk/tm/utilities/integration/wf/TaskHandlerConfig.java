package com.logica.ndk.tm.utilities.integration.wf;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.ProcessParams;
import com.logica.ndk.tm.utilities.integration.wf.exception.UnknownActivityException;

public class TaskHandlerConfig {
  private static final Logger LOG = LoggerFactory.getLogger(TaskHandlerConfig.class);
  private static final String PROPERTY_PROCESS_INPUT_PARAMS = "input-params";
  private static final String PROPERTY_PROCESS_OUTPUT_PARAMS = "output-params";
  private static final String PROPERTY_PROCESS_FINISH_SIGNAL = "finish-signal";
  private static final String PROPERTY_PROCESS_PACKAGE_TYPE = "package-type";
  private static final String PROPERTY_PROCESS_INSTANCE_LIMIT = "instance-limit";
  private static final String PROPERTY_PROCESS_COMMON = "common";
  private static final String PROPERTY_TYPE_PROCESS = "processes/process";
  private static final String PROPERTY_TYPE_TASK = "tasks/task";
  private static final String PROPERTY_TASK_PROCESS_MAPPING = "process-mapping";
  private static final String PROPERTY_TASK_TYPE_DEFINITION = "type-definition";
  private static final String PROPERTY_TASK_TIMES_DEFINITION = "times-definition";
  private static final String PROPERTY_PROCESS_TIMEOUT = "timeout";

  private static XMLConfiguration config = null;
  static {
    try {
      String configFileName = TmConfig.instance().getString("taskHandler.configFile");
      LOG.info("Loading config from file " + configFileName);
      config = new XMLConfiguration(new File(configFileName));
      config.setExpressionEngine(new XPathExpressionEngine());
      LOG.debug("Initial Task handler configuration:\n{}\n", ConfigurationUtils.toString(config));
    }
    catch (ConfigurationException ex) {
      throw new RuntimeException(ex);
    }
  }

  public static String getProperty(String key) {
    return config.getString(key);
  }

  public static List<String> getParams() {
    List<String> result = new ArrayList<String>();
    result.add(ProcessParams.PARAM_NAME_EX_HANDLER_NAME);
    result.add(ProcessParams.PARAM_NAME_EX_HANDLER_EX_MESSAGE);
    result.add(ProcessParams.PARAM_NAME_TASK_ID);
    result.add(ProcessParams.PARAM_NAME_BAR_CODE);
    result.add(ProcessParams.PARAM_NAME_LIBRARY_ID);
    result.add(ProcessParams.PARAM_NAME_LOCAL_BASE);
    result.add(ProcessParams.PARAM_NAME_TITLE);
    result.add(ProcessParams.PARAM_NAME_RESERVED_INTERNAL_ID);
    result.add(ProcessParams.PARAM_NAME_PROGRESS);
    result.add(ProcessParams.PARAM_NAME_PACKAGE_TYPE);
    result.add(ProcessParams.PARAM_NAME_RD_ID);
    result.add(ProcessParams.PARAM_NAME_PATH_ID);
    result.add(ProcessParams.PARAM_NAME_MAIN_SCANNER);
    result.add(ProcessParams.PARAM_NAME_RESERVED_DT);
    result.add(ProcessParams.PARAM_NAME_MIN_OCR_RATE);
    result.add(ProcessParams.PARAM_NAME_BOARD_SCANNER);
    result.add(ProcessParams.PARAM_NAME_TYPE);
    result.add(ProcessParams.PARAM_NAME_OCR);
    result.add(ProcessParams.PARAM_NAME_DOCUMENT_LOCATION);
    result.add(ProcessParams.PARAM_NAME_COLOR);
    result.add(ProcessParams.PARAM_NAME_AUTHOR);
    result.add(ProcessParams.PARAM_NAME_OCR_RATE);
    result.add(ProcessParams.PARAM_NAME_DOC_KEEPER);
    result.add(ProcessParams.PARAM_NAME_PUBLISH_DT);
    result.add(ProcessParams.PARAM_NAME_ISBN);
    result.add(ProcessParams.PARAM_NAME_ISSN);
    result.add(ProcessParams.PARAM_NAME_DPI);
    result.add(ProcessParams.PARAM_NAME_DESTRUCTIVE_DIGITALIZATION);
    result.add(ProcessParams.PARAM_NAME_ACTIVITY);
    result.add(ProcessParams.PARAM_NAME_PAGE_COUNT);
    result.add(ProcessParams.PARAM_NAME_DESCRIPTION_LEVEL);
    result.add(ProcessParams.PARAM_NAME_RESERVED_BY);
    result.add(ProcessParams.PARAM_NAME_SCAN_AT_PREPARATION);
    result.add(ProcessParams.PARAM_NAME_CCNB);
    result.add(ProcessParams.PARAM_NAME_PROJECT);
    result.add(ProcessParams.PARAM_NAME_SIGLA);
    result.add(ProcessParams.PARAM_NAME_DOCUMENT_DESTROYED);
    result.add(ProcessParams.PARAM_NAME_LOCALITY);
    result.add(ProcessParams.PARAM_NAME_LANGUAGE);
    result.add(ProcessParams.PARAM_NAME_PUBLIC);
    result.add(ProcessParams.PARAM_NAME_COMMENT);
    result.add(ProcessParams.PARAM_NAME_OCR_FONT);
    result.add(ProcessParams.PARAM_NAME_UUID);
    result.add(ProcessParams.PARAM_NAME_CDM_ID);
    result.add(ProcessParams.PARAM_NAME_LOCAL_URN_STRING);
    result.add(ProcessParams.PARAM_NAME_SCAN_COUNT);
    result.add(ProcessParams.PARAM_NAME_PUBLISH);
    result.add(ProcessParams.PARAM_NAME_SPLIT);
    result.add(ProcessParams.PARAM_NAME_SOURCE_PACKAGE);
    result.add(ProcessParams.PARAM_NAME_URNNBN);
    result.add(ProcessParams.PARAM_NAME_DIMENSION_X);
    result.add(ProcessParams.PARAM_NAME_DIMENSION_Y);
    result.add(ProcessParams.PARAM_NAME_PROFILE_PP);
    result.add(ProcessParams.PARAM_NAME_PROFILE_UC);
    result.add(ProcessParams.PARAM_NAME_PROFILE_MC);
    result.add(ProcessParams.PARAM_NAME_SCANS);
    result.add(ProcessParams.PARAM_NAME_VOLUME_DATE);
    result.add(ProcessParams.PARAM_NAME_VOLUME_NUMBER);
    result.add(ProcessParams.PARAM_NAME_PART_NUMBER);
    result.add(ProcessParams.PARAM_NAME_PROCESS_EM);
    result.add(ProcessParams.PARAM_NAME_PROCESS_LTP);
    result.add(ProcessParams.PARAM_NAME_PROCESS_KRAMERIUS_NKCR);
    result.add(ProcessParams.PARAM_NAME_PROCESS_KRAMERIUS_MZK);
    result.add(ProcessParams.PARAM_NAME_IMPORT_TYPE);
    result.add(ProcessParams.PARAM_NAME_URL);
    result.add(ProcessParams.PARAM_NAME_DOC_NUMBER);
    result.add(ProcessParams.PARAM_NAME_OCR_LICENCE_USED);
    result.add(ProcessParams.PARAM_NAME_DATE_ISSUED);
    result.add(ProcessParams.PARAM_NAME_ISSUE_NUMBER);
    result.add(ProcessParams.PARAM_NAME_PROCESS_MANUAL);
    result.add(ProcessParams.PARAM_NAME_NOTE);
    result.add(ProcessParams.PARAM_NAME_TYPE_CODE);
    result.add(ProcessParams.PARAM_NAME_ISSUE_UUID);
    result.add(ProcessParams.PARAM_NAME_VOLUME_UUID);
    result.add(ProcessParams.PARAM_NAME_TITLE_UUID);
    result.add(ProcessParams.PARAM_NAME_RECORD_IDENTIFIER);

    return result;
  }

  private static HierarchicalConfiguration getConfigNode(String type, String nodeName) {
    String key = type + "[@name='" + nodeName + "']";
    LOG.debug("Selecting process node - " + key);
    try {
      HierarchicalConfiguration node = (HierarchicalConfiguration) config.configurationAt(key);
      return node;
    }
    catch (IllegalArgumentException e) {
      LOG.debug("No definition for key " + key);
      return null;
    }
  }

  public static List<String> getInputParams(String processId) {
    List<String> common = getProcessParams(PROPERTY_PROCESS_COMMON, PROPERTY_PROCESS_INPUT_PARAMS);
    List<String> process = getProcessParams(processId, PROPERTY_PROCESS_INPUT_PARAMS);
    Set<String> result = new HashSet<String>();
    result.addAll(common);
    result.addAll(process);
    return new ArrayList<String>(result);
  }

  public static List<String> getOutputParams(String processId) {
    List<String> common = getProcessParams(PROPERTY_PROCESS_COMMON, PROPERTY_PROCESS_OUTPUT_PARAMS);
    List<String> process = getProcessParams(processId, PROPERTY_PROCESS_OUTPUT_PARAMS);
    Set<String> result = new HashSet<String>();
    result.addAll(common);
    result.addAll(process);
    return new ArrayList<String>(result);
  }

  @SuppressWarnings("unchecked")
  private static List<String> getProcessParams(String processId, String paramSet) {
    List<String> result = new ArrayList<String>();
    Object list;
    HierarchicalConfiguration node;

    // Process specific params
    node = getConfigNode(PROPERTY_TYPE_PROCESS, processId);
    if (node != null) {
      list = node.getProperty(paramSet + "/param/@name");
      if (list != null) {
        if (list instanceof Collection) {
          for (Object param : (Collection<Object>) list) {
            result.add((String) param);
          }
        }
        else {
          result.add((String) list);
        }
      }
    }

    return result;
  }

  public static String getFinishSignal(String processId) {
    return getProcessProperty(PROPERTY_PROCESS_FINISH_SIGNAL, processId);
  }

  public static String getPackageType(String processId) {
    return getProcessProperty(PROPERTY_PROCESS_PACKAGE_TYPE, processId);
  }

  public static String getInstanceLimit(String processId) {
    return getProcessProperty(PROPERTY_PROCESS_INSTANCE_LIMIT, processId);
  }

  public static String getTimeout(String processId) {
    return getProcessProperty(PROPERTY_PROCESS_TIMEOUT, processId);
  }

  private static String getProcessProperty(String property, String processId) {
    HierarchicalConfiguration node = getConfigNode(PROPERTY_TYPE_PROCESS, processId);
    String result;
    if (node != null) {
      result = node.getString(property);
      if (result != null && !result.isEmpty()) {
        return result;
      }
    }

    // In case either whole process of finish signal is missing use common
    node = getConfigNode(PROPERTY_TYPE_PROCESS, PROPERTY_PROCESS_COMMON);
    result = node.getString(property);
    return result;
  }

  public static String getProcessIdByActivity(String code, String type) throws UnknownActivityException {
    LOG.debug("Getting process mapping for task activity " + code + " of type " + type);
    HierarchicalConfiguration node = getConfigNode(PROPERTY_TYPE_TASK, code);
    if (node == null) {
      throw new UnknownActivityException("Unknown activy code " + code + " of type " + type);
    }
    String key = PROPERTY_TASK_PROCESS_MAPPING;
    if (type != null) {
      key += "[@type='" + type + "']";
    }
    List<Object> list = (List<Object>) node.getList(key);
    if (list == null || list.size() == 0) {
      key = PROPERTY_TASK_PROCESS_MAPPING;
      list = (List<Object>) node.getList(key);
      if (list == null || list.size() == 0) {
        throw new UnknownActivityException("Unknown activy code " + code + " of type " + type);
      }
    }
    String processId = (String) list.get(0);
    LOG.debug(processId);
    return processId;
  }

  public static String getActivityTypeDefinition(String code) throws UnknownActivityException {
    HierarchicalConfiguration node = getConfigNode(PROPERTY_TYPE_TASK, code);
    if (node == null) {
      throw new UnknownActivityException("Unknown activy code " + code);
    }
    return node.getString(PROPERTY_TASK_TYPE_DEFINITION);
  }
  
  public static String getActivityTimesDefinition(String code) throws UnknownActivityException {
    HierarchicalConfiguration node = getConfigNode(PROPERTY_TYPE_TASK, code);
    if (node == null) {
      throw new UnknownActivityException("Unknown activy code " + code);
    }
    return node.getString(PROPERTY_TASK_TIMES_DEFINITION);
  }

  public static List<String> getIgnoredTasks() {
    String key = PROPERTY_TYPE_TASK + "[@ignored=\"true\"]";
    List<String> result = new ArrayList<String>();
    try {
      List<HierarchicalConfiguration> tasks = config.configurationsAt(key);
      for (HierarchicalConfiguration task : tasks) {
        result.add(task.getString("@name"));
      }
      return result;
    }
    catch (IllegalArgumentException e) {
      LOG.debug("No ignore definition");
      return null;
    }

  }
}
