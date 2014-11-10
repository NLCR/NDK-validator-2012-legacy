package com.logica.ndk.tm.utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import com.jcabi.aspects.RetryOnFailure;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.SystemException;

public class FileIOUtils {
	
	public static void createDirectory(File dir) { 
		if (!dir.isDirectory() && !dir.mkdirs()) {
			throw new SystemException("Cannot create directory: " + dir,
					ErrorCodes.CREATING_DIR_FAILED);
		}
	}
	
	public static void copyFile(File source, File dest, boolean overwrite) {	
		try {
			if (overwrite || !dest.exists()) {
				//FileUtils.copyFile(source, dest);
				retriedCopyFile(source, dest);
			}
		} catch (Exception e) {
  		throw new SystemException("Error in copying source: " + source + ", to dest: " + dest, 
  				ErrorCodes.COPY_FILES_FAILED);
  	}
	}
	
	public static void writeToFile(File target, String textToWrite, String description) {
    Writer output = null;
		try {
			output = new BufferedWriter(new FileWriter(target));
			output.write(textToWrite);
			output.close();
		} catch (Exception e) {
			throw new SystemException("Error while writing " + description + ". Exception: "+e, ErrorCodes.ERROR_WHILE_WRITING_FILE);
		} finally {
			try {
				if (output != null) {
					output.close();
				}
			} catch (IOException ex) {
				throw new SystemException("Error while closing " + description + ". ", ErrorCodes.ERROR_WHILE_WRITING_FILE);
			}
		}
	}

	public static ArrayList<String> readFilePerLineToList(File source, String description) {
		ArrayList<String> list = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			String line;
			reader = new BufferedReader(new FileReader(source));
			while ((line = reader.readLine()) != null) {
				list.add(line);
			} 
			return list;
		} catch (Exception e) {
				throw new SystemException("Error while reading " + description + ". ", ErrorCodes.ERROR_WHILE_READING_FILE);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					throw new SystemException("Error while closing " + description + ". ", ErrorCodes.ERROR_WHILE_READING_FILE);
				}
			}
		}
	}
	
	@RetryOnFailure(attempts = 3)
  private static void retriedCopyFile(File source, File destination) throws IOException {
      FileUtils.copyFile(source, destination);
  }
	
}
