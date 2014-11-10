package com.logica.ndk.tm.repair.ocrValidationErrors;

import com.logica.ndk.tm.cdm.CDM;
import com.logica.ndk.tm.utilities.FileIOUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: krchnacekm
 */
class CDMUtilImpl implements CDMUtil {

    private String cdmIdsPath;
    private CDM cdm = new CDM();

    public CDMUtilImpl(final String cdmIdsPath) {
        if (cdmIdsPath != null && new File(cdmIdsPath).exists()) {
            this.cdmIdsPath = cdmIdsPath;
        } else {
            throw new IllegalArgumentException(String.format("Configuration file %s not exists", cdmIdsPath));
        }
    }

    public Map<String, File> getCDMPaths() {
        final Map<String, File> result = new HashMap<String, File>();

        final File cdmIdsFile = new File(cdmIdsPath);
        if(cdmIdsFile.exists()) {
            final List<String> cdmIds = FileIOUtils.readFilePerLineToList(cdmIdsFile, "List of cdm ids");
            for (String cdmId : cdmIds) {
                result.put(cdmId, cdm.getCdmDir(cdmId));
            }
        } else {
            throw new IllegalStateException(String.format("File %s does not exist.", cdmIdsPath));
        }

        return result;
    }

}
