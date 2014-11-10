package com.logica.ndk.tm.utilities.io;

import java.io.File;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;
import java.io.IOException;

/**
 * Implementation of {@link CopyTo} WS interface.
 * 
 * @author palousp
 */
public class MoveToImpl extends AbstractUtility {
	
	/**
	 * Move all files from source directory and subdirectories to respective directories in destination directory 
	 * @param sourceDirName source directory pathname
	 * @param destDirName 	destination directory pathname 
	 * @param pattern string (regexp) used to filter files
         * @return 
	 * @throws SystemException
	 */
        @RetryOnFailure
	public String moveDir(String sourceDirName, String destDirName, String pattern) throws SystemException {
		Pattern pat=null;
		if (pattern!=null && !pattern.isEmpty()) pat=Pattern.compile(pattern.toUpperCase());
            
		// File (or directory) to be moved
		File sourceDir = new File(sourceDirName);

		// Destination directory
		File destDir = new File(destDirName);

		log.debug("Moving files from {} to {}", sourceDir.getAbsolutePath(),destDir.getAbsolutePath());
                
		if (sourceDir.isDirectory()) {
			if(destDir.exists()){
                            try{
                                FileUtils.deleteDirectory(destDir);
                            }catch(IOException ex){
                               throw new SystemException(String.format("Can't delete target dir: %s",destDir.getAbsolutePath()), ex, ErrorCodes.CREATING_DIR_FAILED); 
                            }
                        }
                        
			log.debug("destinatino directory not existing, creating: {}", destDir.getAbsolutePath());
			if (!destDir.mkdirs())
			    throw new SystemException(String.format("Can't create target dir: %s",destDir.getAbsolutePath()), ErrorCodes.CREATING_DIR_FAILED);
			
			String[] children = sourceDir.list();
			log.debug("moving {} files", children.length);
			for (int i = 0; i < children.length; i++) {
				File f = new File(sourceDir + File.separator + children[i]);
				if (f.isFile()) {
					// Move file to new directory
					boolean suitableFile=true;
					if (pat!=null && pat.matcher(f.getName().toUpperCase()).matches()!=true ) suitableFile=false;
					if (suitableFile){
						boolean success = f.renameTo(new File(destDir, children[i]));
						if (!success) {
							throw new SystemException(String.format("Cannot move file from %s to %s", sourceDirName, destDirName), ErrorCodes.MOVING_FILE_FAILED);
						}
					} else {
						log.info("File {} skipped - pattern ({}) match not true",f.getAbsolutePath(), pattern);
					}
				} else {
					moveDir(sourceDir + File.separator + children[i],
							destDir + File.separator + children[i], pattern);
				}
			}   
			//delete source directory
			log.info("Going to delete source directory: "+sourceDir.getPath());
			//FileUtils.deleteQuietly(sourceDir);
			retriedDeleteQuietly(sourceDir);
		} else {
//			throw new SystemException(String.format("Source is not a directory: {}, will do nothing.",sourceDir.getAbsolutePath()));
			log.warn("Source is not a directory: {}, will do nothing.",sourceDir.getAbsolutePath());
		}
		return ResponseStatus.RESPONSE_OK;
	}
 
/*	
	public String moveDirNoRecurse(String sourceDirName, String destDirName, String pattern) throws SystemException {
		Pattern pat=null;
		if (pattern!=null) pat=Pattern.compile(pattern.toUpperCase());
            
		// File (or directory) to be moved
		File sourceDir = new File(sourceDirName);

		// Destination directory
		File destDir = new File(destDirName);            

		log.debug("Moving files from {} to {}", sourceDir.getAbsolutePath(),destDir.getAbsolutePath());
        
		if (sourceDir.isDirectory()) {
			if (!destDir.exists()) { // If targetLocation does not exist, it will be created.
				log.debug("destinatino directory not existing, creating: {}", destDir.getAbsolutePath());
				if (!destDir.mkdirs())
					throw new SystemException(String.format("Can't create target dir: %s",destDir.getAbsolutePath()));
			}
		} else {
			throw new SystemException(String.format("Source is not a directory: {}, will do nothing.",sourceDir.getAbsolutePath()));
		}

		for (File child: sourceDir.listFiles()){
			if (child.isFile()){
				// Move file to new directory
				boolean suitableFile=true;
				if (pat!=null && pat.matcher(child.getName().toUpperCase()).matches()!=true ) suitableFile=false;
				if (suitableFile){
					File newName=new File(destDir, child.getName());
					boolean success = child.renameTo(newName);
					if (!success) {
						throw new SystemException(String.format("Cannot move file from %s to %s", child.getAbsolutePath(), newName));
					}
				} else {
					log.info("File {} skipped - pattern ({}) match not true",child.getAbsolutePath(), pattern);
				}                      
			}
		}
		return ResponseStatus.RESPONSE_OK;
	}
*/
	
	@RetryOnFailure(attempts = 3)
  private void retriedDeleteQuietly(File target) {
      FileUtils.deleteQuietly(target);
  }
	
}
