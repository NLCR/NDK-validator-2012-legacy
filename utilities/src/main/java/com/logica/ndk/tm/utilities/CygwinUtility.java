package com.logica.ndk.tm.utilities;

import com.logica.ndk.tm.config.TmConfig;

public class CygwinUtility extends AbstractUtility {
  /**
   * @author kovalcikm
   */
  protected final String CYG_WIN_HOME = TmConfig.instance().getString("cygwinHome");
  protected final String BASH_PATH = "\\bin\\bash";

  
  protected String transformLocalPath(String path) {
    return "/cygdrive/" + path.replace(":\\", "/").replace("\\", "/");

  }

  protected boolean isLocalPath(String path) {
    return !(path.contains("\\\\") || path.contains("//"));
  }
  
  protected boolean isDosPath(String path) {
    return path.contains("\\");
  }
  
  protected String transformDosPathToPosix(String path) {
    return path.replace("\\", "/");
  }
  
	protected static String dosPathToPosix(String pathname) {
		if (pathname == null || pathname.isEmpty()) return pathname;
		String driveLetter = pathname.substring(0, 1);
		return pathname.replace('\\', '/').replace(driveLetter+":", "/cygdrive/"+driveLetter);	
	}

}
