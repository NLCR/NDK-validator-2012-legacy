package com.logica.ndk.tm.utilities.io;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.logica.ndk.commons.utils.OsUtils;
import com.logica.ndk.commons.utils.OsUtils.OS;
import com.logica.ndk.commons.utils.cli.SysCommandExecutor;
import com.logica.ndk.tm.utilities.BusinessException;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * Helper for manipulating with directory and files permissions. <br>
 * This helper perform command <code>icacls</code> for Windows. More on <a
 * href="http://technet.microsoft.com/en-us/library/cc753525(v=ws.10).aspx">Microsoft TechNet</a>
 * 
 * @author ondrusekl
 */
public abstract class PermissionsHelper {

  private static final String ACL_PERMISSIONS_FILE_NAME = "aclPermissionsBackup";
  private final static transient Logger log = LoggerFactory.getLogger(PermissionsHelper.class);

  /**
   * Grant permmisions for defined user.
   * 
   * @param user
   *          user for granting permissions
   * @param path
   *          specified path to file or directory
   * @param replace
   *          if is true, then new permissions will be replaced, else added
   * @param recursive
   *          if is true, grant will perform on all inner files and subdirectories
   * @param permissions
   *          specified permissions from {@link WindowsPermissions} constants
   */
  public static void grant(String user, String path, boolean replace, boolean recursive, String[] permissions) {
    checkNotNull(user, "user must not be null");
    checkArgument(!user.isEmpty(), "user must not be empty");
    checkNotNull(path, "dirPath must not be null");
    checkArgument(!path.isEmpty(), "dirPath must not be empty");
    checkNotNull(permissions, "permissions must not be null");
    checkArgument(permissions.length > 0, "permissions must not be empty");

    checkMultiplePermissions(permissions);
    
    checkUser(user);

    log.info("grant on {} started", path);
    OS os = OsUtils.getOsType();
    switch (os) {
      case WINDOWS:
        try {
          SysCommandExecutor commandExecutor = new SysCommandExecutor();
          String command = new StringBuilder("icacls ")
              .append(path)
              .append(" /grant")
              .append(replace ? ":r " : " ")
              .append(user).append(":")
              //.append("(")
              .append(Joiner.on(",").join(permissions))
              //.append(")")
              .append(recursive ? " /t" : "")
              .toString();
          log.info("Performing command '{}' on {}", command, path);
          int result = commandExecutor.runCommand(command);
          if (result != 0) {
            throw new RuntimeException(format("Command: %s exited with code %d", command, result));
          }
          log.info("grant finished");
          break;
        }
        catch (Exception e) {
          throw new SystemException(format("Cannot grant access on %s for user %s", path, user), ErrorCodes.ACCESS_PROBLEM);
        }
      default:
        throw new SystemException(format("Cannot grant acces on OS %s", os), ErrorCodes.ACCESS_PROBLEM);
    }

  }

  public static void revoke(String user, String path, RemoveType type, boolean recursive) {
    checkNotNull(user, "user must not be null");
    checkArgument(!user.isEmpty(), "user must not be empty");
    checkNotNull(path, "dirPath must not be null");
    checkArgument(!path.isEmpty(), "dirPath must not be empty");
    
    checkUser(user);

    log.info("revoke on {} started", path);
    OS os = OsUtils.getOsType();
    switch (os) {
      case WINDOWS:
        try {
          SysCommandExecutor commandExecutor = new SysCommandExecutor();
          String command = new StringBuilder("icacls ")
              .append(path)
              .append(" /remove")
              .append(type != null ? (":" + type.getReal() + " ") : " ")
              .append(user)
              .append(recursive ? " /t" : "")
              .toString();
          log.info("Performing command '{}' on {}", command, path);
          int result = commandExecutor.runCommand(command);
          if (result != 0) {
            throw new RuntimeException(format("Command: %s exited with code %d", command, result));
          }
          log.info("revoke finished");
          break;
        }
        catch (Exception e) {
          throw new SystemException(format("Cannot revoke access on %s for user %s", path, user), ErrorCodes.ACCESS_PROBLEM);
        }
      default:
        throw new SystemException(format("Cannot revoke acces on OS %s", os), ErrorCodes.ACCESS_PROBLEM);
    }

  }

  public static void saveToFile(String path, String aclFilePath, boolean recursive) {
    checkNotNull(path, "path must not be null");
    checkArgument(!path.isEmpty(), "path must not be empty");
    checkNotNull(aclFilePath, "aclFilePath must not be null");
    checkArgument(!aclFilePath.isEmpty(), "aclFilePath must not be empty");

    log.info("save to file {} on {} started", aclFilePath, path);

    OS os = OsUtils.getOsType();
    switch (os) {
      case WINDOWS:
        try {
          SysCommandExecutor commandExecutor = new SysCommandExecutor();
          String command = new StringBuilder("icacls ")
              .append(path)
              .append(" /save").append(" ")
              .append(aclFilePath)
              .append(recursive ? " /t" : "")
              .toString();
          log.info("Performing command '{}' on {}", command, path);
          int result = commandExecutor.runCommand(command);
          if (result != 0) {
            throw new RuntimeException(format("Command: %s exited with code %d", command, result));
          }
          log.info("save to file finished");
          break;
        }
        catch (Exception e) {
          throw new SystemException(format("Cannot save permissions on %s", path), ErrorCodes.PERMISSIONS_PROBLEM);
        }
      default:
        throw new SystemException(format("Cannot save permissions on OS %s", os), ErrorCodes.PERMISSIONS_PROBLEM);
    }
  }

  public static void save(String path) {
    checkNotNull(path, "path must not be null");
    checkArgument(!path.isEmpty(), "path must not be empty");

    String aclFilePath = path + File.separator + ACL_PERMISSIONS_FILE_NAME;
    saveToFile(path, aclFilePath, true);

  }

  public static void restoreFromFile(String path, String aclFilePath) {
    checkNotNull(path, "path must not be null");
    checkArgument(!path.isEmpty(), "path must not be empty");
    checkNotNull(aclFilePath, "aclFilePath must not be null");
    checkArgument(!aclFilePath.isEmpty(), "aclFilePath must not be empty");

    log.info("restore from file {} on {} started", aclFilePath, path);

    OS os = OsUtils.getOsType();
    switch (os) {
      case WINDOWS:
        try {
          SysCommandExecutor commandExecutor = new SysCommandExecutor();
          String parentPath = new File(path).getParent();
          String command = new StringBuilder("icacls ")
              .append(parentPath)
              .append(" /restore").append(" ")
              .append(aclFilePath)
              .toString();
          log.info("Performing command '{}' on {}", command, parentPath);
          int result = commandExecutor.runCommand(command);
          if (result != 0) {
            throw new RuntimeException(format("Command: %s exited with code %d", command, result));
          }
          log.info("restore from file finished");
          break;
        }
        catch (Exception e) {
          throw new SystemException(format("Cannot restore permissions on %s", path), ErrorCodes.PERMISSIONS_PROBLEM);
        }
      default:
        throw new SystemException(format("Cannot restore permissions on OS %s", os), ErrorCodes.PERMISSIONS_PROBLEM);
    }
  }

  public static void reset(String path, boolean recursive) {
    checkNotNull(path, "path must not be null");
    checkArgument(!path.isEmpty(), "path must not be empty");

    log.info("reset on {} started", path);

    OS os = OsUtils.getOsType();
    switch (os) {
      case WINDOWS:
        try {
          SysCommandExecutor commandExecutor = new SysCommandExecutor();
          String command = new StringBuilder("icacls ")
              .append(path)
              .append(" /reset")
              .append(recursive ? " /t" : "")
              .toString();
          log.info("Performing command '{}' on {}", command, path);
          int result = commandExecutor.runCommand(command);
          if (result != 0) {
            throw new RuntimeException(format("Command: %s exited with code %d", command, result));
          }
          log.info("reset finished");
          break;
        }
        catch (Exception e) {
          throw new SystemException(format("Cannot reset permissions on %s", path), ErrorCodes.PERMISSIONS_PROBLEM);
        }
      default:
        throw new SystemException(format("Cannot reset permissions on OS %s", os), ErrorCodes.PERMISSIONS_PROBLEM);
    }

  }

  private static void checkMultiplePermissions(String[] permissions) {
    if (permissions.length > 1) {
      for (String permission : permissions) {
        if (WindowsPermissions.FULL_ACCESS.equalsIgnoreCase(permission)
            || WindowsPermissions.MODIFY_ACCESS.equalsIgnoreCase(permission)
            || WindowsPermissions.READ_AND_EXECUTE_ACCESS.equalsIgnoreCase(permission)
            || WindowsPermissions.READ_ONLY_ACCESS.equalsIgnoreCase(permission)
            || WindowsPermissions.WRITE_ONLY_ACCESS.equalsIgnoreCase(permission)) {
          throw new IllegalArgumentException("Permissions [F, M, RX, R, W] cannot be used in multiple mode");
        }
      }
    }
  }
  
  private static void checkUser(String user) {	  
		  SysCommandExecutor commandExecutor = new SysCommandExecutor();
	      String command = new StringBuilder("net user ").
	        append("\"").
	      	append(user).
	      	append("\"").
	      	append(" /DOMAIN").toString();
	      
	      log.info("Performing command '{}' on", command);
	      int result = -1;
		  try {
			result = commandExecutor.runCommand(command);
		  } catch (Exception e) {
				throw new SystemException("Cannot check existence of the user", ErrorCodes.EXISTENCE_CHECK_FAILED);
		  }
	      if (result != 0) {
	    	  log.info("The user doesn't exist: " +user);  
	    	  throw new BusinessException("Can't grant access. The user doesn't exist.", ErrorCodes.PERMISSION_HELPER_USER_NOT_EXIST);
	      }
	      log.info("user exists");	  
	  
  }

  public static class WindowsPermissions {

    public final static String FULL_ACCESS = "F";
    public final static String MODIFY_ACCESS = "M";
    public final static String READ_AND_EXECUTE_ACCESS = "RX";
    public final static String READ_ONLY_ACCESS = "R";
    public final static String WRITE_ONLY_ACCESS = "W";
    public final static String DELETE = "D";
    public final static String READ_CONTROL = "RC";
    public final static String WRITE_DAC = "WDAC";
    public final static String WRITE_OWNER = "WO";
    public final static String SYNCHRONIZE = "S";
    public final static String ACCESS_SYSTEM_SECURITY = "AS";
    public final static String MAXIMUM_ALLOWED = "MA";
    public final static String GENERIC_READ = "GR";
    public final static String GENERIC_WRITE = "GW";
    public final static String GENERIC_EXECUTE = "GE";
    public final static String GENERIC_ALL = "GA";
    public final static String READ_DATA_OR_LIST_DIRECTORY = "RD";
    public final static String WRITE_DATA_OR_ADD_FILE = "WD";
    public final static String APPEND_DATA_OR_ADD_SUBDIRECTORY = "AD";
    public final static String READ_EXTENDED_ATTRIBUTES = "REA";
    public final static String WRITE_EXTENDED_ATTRIBUTES = "WEA";
    public final static String EXECUTE_OR_TRAVERSE = "X";
    public final static String DELETE_CHILD = "DC";
    public final static String READ_ATTRIBUTES = "RA";
    public final static String WRITE_ATTRIBUTES = "WA";

  }

  public static enum RemoveType {
    GRANTED("g"),
    DENIED("d");

    private final String real;

    private RemoveType(String real) {
      this.real = real;
    }

    public String getReal() {
      return real;
    }
  }

}
