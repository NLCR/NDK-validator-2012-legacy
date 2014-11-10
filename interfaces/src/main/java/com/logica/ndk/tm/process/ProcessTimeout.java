package com.logica.ndk.tm.process;

import com.logica.ndk.tm.config.TmConfig;

/**
 * @author Rudolf Daco
 */
public interface ProcessTimeout {
  /**
   * Meno process variable na nastavenie timeout procesu. Tato variable obsahuje timeout procesu v milisekundach.
   */
  public final static String PARAMETER_NAME = "TM_PROC_TIMEOUT";
  /**
   * Default hodnota pre timeout procesu. Ak sa nenajde v config tak sa pouzije 10 dni = 864000000 ms.
   */
  public final static long DEFAULT_VALUE = TmConfig.instance().getLong("process.TM_PROC_TIMEOUT_DEFAULT", new Long(864000000));
}
