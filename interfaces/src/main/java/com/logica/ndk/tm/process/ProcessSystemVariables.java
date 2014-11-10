package com.logica.ndk.tm.process;

/**
 * List of variables in process context which are used for some system messaging.
 * 
 * @author Rudolf Daco
 */
public enum ProcessSystemVariables {
  SYS_001_EX_HANDLER_NAME, // name of work item which crashed
  SYS_002_EX_HANDLER_EX_MSG, // exception message when handler crashed
  SYS_003_EX_HANDLER_EX_CLASS, // class name of exception when handler crashed
  SYS_004_ABORT_INITIATOR, // source of process abort
  SYS_005_EX_HANDLER_ERROR_CODE, // error code of exception if exists
  SYS_006_EX_HANDLER_EX_MSG_LOCAL, // exception message when handler crashed - localized message
  SYS_007_EX_NODE_ID; // mule node id where utility was executed
}
