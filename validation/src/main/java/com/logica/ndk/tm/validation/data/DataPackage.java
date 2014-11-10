package com.logica.ndk.tm.validation.data;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tomas Mriz (Logica)
 */
public class DataPackage {

    private static final String AMD_SEC_DIR = "amdSec";
    private static final String ALTO_DIR = "ALTO";
    private static final transient Logger logger = LoggerFactory.getLogger(
        DataPackage.class);

    private String dataDirectory;

    public DataPackage(String dataDirectory) {

        this.dataDirectory = dataDirectory;
    }

    public File getDPDir() {
        return new File(dataDirectory);
    }

    public String getDataDirectory() {
        return dataDirectory;
    }

    public File getMetsFile() {
        File dir = getDPDir();
        String dirName = dir.getName();
        String metsName = "METS_" + dirName + ".xml";
        return new File(dir, metsName);
    }

    public File getAMDMetsFile() {
        return new File(getDPDir(), AMD_SEC_DIR);
    }

    public String getId() {
        return getDPDir().getName();
    }
    
    public File getAltoDir(){
        return new File(getDPDir(), ALTO_DIR);
    }
}
