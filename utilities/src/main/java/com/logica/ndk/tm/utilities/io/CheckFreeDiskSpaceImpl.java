package com.logica.ndk.tm.utilities.io;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.logica.ndk.tm.config.TmConfig;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;
import com.logica.ndk.tm.utilities.commandline.CmdLineAdvancedImpl;;

/**
 * Check free disk space for process running. If given required minimal space plus size of 
 * given directory plus DEFAULT_MIN_FREE_SPACE_MB is bigger than actual free disk space 
 * exception is raised.
 *  	 
 * @author Petr Palous
 *
 */
public class CheckFreeDiskSpaceImpl extends CmdLineAdvancedImpl {
	private static final String DEFAULT_MIN_FREE_SPACE_MB = TmConfig.instance().getString("utility.freeDiskSpace.defaultMinFreeSpaceMB");
	private static final long MB_SIZE = 1024*1024;
	private static final long DEFAULT_MIN_FREE_SPACE = Long.parseLong(DEFAULT_MIN_FREE_SPACE_MB)*MB_SIZE;
	
	public void execute(String cdmId, String requiredMinFreeSpaceMB, String cdmReferenceDir, String growCoef) {
		long spaceNeeded;
		File referenceDir;
		checkNotNull(cdmId, "cdmId must not be null");
		if (requiredMinFreeSpaceMB == null || requiredMinFreeSpaceMB.isEmpty()) {
			requiredMinFreeSpaceMB = "0";
		}
		long requiredMinFreeSpace = Long.parseLong(requiredMinFreeSpaceMB)*MB_SIZE + DEFAULT_MIN_FREE_SPACE;
		if (cdmReferenceDir == null || cdmReferenceDir.isEmpty()) {
			spaceNeeded = requiredMinFreeSpace;
			referenceDir = cdm.getCdmLinkDir(cdmId);
		} else {
			if (growCoef == null || growCoef.isEmpty()) {
				growCoef = "1";
			}
			referenceDir = new File(cdmReferenceDir);
			log.debug("Size of directory {} * coef {} calculation started.", cdmReferenceDir, growCoef);
			long calcSpace4dir = FileUtils.sizeOfDirectory(referenceDir)*Integer.parseInt(growCoef);
			log.debug("calculation result: {}", calcSpace4dir);
//			spaceNeeded = calcSpace4dir > requiredMinFreeSpace ? calcSpace4dir : requiredMinFreeSpace;
			spaceNeeded = calcSpace4dir + requiredMinFreeSpace;
		}		
		execute("utility.freeDiskSpace", referenceDir.getAbsolutePath(), null);
		// remove all non printable ASCII chars and parse string as long
		long usableSpace = Long.parseLong(scriptOutput.replaceAll("[^\\x20-\\x7e]",""));
		log.info("Directory: {}; usableSpace: {}", referenceDir, usableSpace);
		if (usableSpace < spaceNeeded) {
			log.info("spaceNeeded: {} is bigger than free disk space.", spaceNeeded);
			throw new SystemException("Insufficient free disk space for processing.", ErrorCodes.INSUFFICIENT_DISK_SPACE);
		}
	}
	
}
