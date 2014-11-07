package com.logica.ndk.tm.validation.validator.parts;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.utilities.em.ValidationViolation;
import com.logica.ndk.tm.utilities.validation.ValidationViolationsWrapper;
import com.logica.ndk.tm.validation.data.DataPackage;
import com.logica.ndk.tm.validation.utils.DPResourceWrapper;
import com.logica.ndk.tm.validation.utils.MetsUtils;

import au.edu.apsr.mtk.base.*;
import com.logica.ndk.tm.validation.validator.core.ValidationTypes;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Tomas Mriz (Logica)
 */
public class StructureValidator extends AbstractPartValidator {

    public StructureValidator(
        DataPackage dataToValidate,
        DPResourceWrapper validationProps
    ) {
        super(dataToValidate, validationProps);
    }

    @Override
    public ValidationViolationsWrapper validate() {
        logger.info("Structure validator is starting");
        ValidationViolationsWrapper result = new ValidationViolationsWrapper();
        final File cdmDir = dataToValidate.getDPDir();

        logger.debug("Checking data directory");
        if (!cdmDir.exists() || !cdmDir.isDirectory()) {
            result.add(new ValidationViolation(ValidationTypes.STRUCTURE_VALIDATION.getMessage(),
                    "CDM directory does not exist: " +
                    dataToValidate.getDataDirectory()));
        }

        //mandatory directories exist
        logger.debug("Checking mandatory directories");
        for (final File mandatoryDir : getMandatoryDir()) {
            if (!mandatoryDir.exists()) {
                result.add(new ValidationViolation(ValidationTypes.STRUCTURE_VALIDATION.getMessage(),
                    "Mandatory directory does not exist: " + mandatoryDir));
            }
        }

        //mets file exists
        logger.debug("Checking mets file");
        if (!dataToValidate.getMetsFile().exists()) {
            result.add(new ValidationViolation(ValidationTypes.STRUCTURE_VALIDATION.getMessage(), "No main METS file"));
        } else {
            logger.info("Cross validation");
            if (validationProps.getCrossValidation()) {
                //cross validation
                List<ValidationViolation> violations = crossValidation(dataToValidate.getMetsFile());
                violations.addAll(result.getViolationsList());
                result.setViolationsList(violations);
            } else {
                logger.debug("Skipping cross validation");
            }
        }

        logger.info("Structure validator has end");
        return result;
    }

    private List<File> getMandatoryDir() {
        List<String> mandatoryDirsName = validationProps.getMandatoryDirs();
        List<File> mandatoryDirs = new ArrayList<File>();

        for(String name : mandatoryDirsName) {
            mandatoryDirs.add(new File(dataToValidate.getDPDir(), name));
        }

        return mandatoryDirs;
    }

    /**
     * Do cross validation between Mets file and directories
     *
     * @param metsFile
     * @return
     */
    private List<ValidationViolation> crossValidation(File metsFile) {
        logger.info("Cross validation started");
        List<ValidationViolation> violations =
                new ArrayList<ValidationViolation>();

        try {
            Map<String, List<String>> refs = getFileSecMap(metsFile);
            Map<String, String> refToDirsMapping =
                    MetsUtils.getDirToGroupIdMapping();

            Iterator it = refToDirsMapping.entrySet().iterator();
            File testingDir;

            //iterate over dir and check dir to ref, ref to dir
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                String key = (String) pairs.getKey();
                String value = (String) pairs.getValue();
                logger.info(value); 
                testingDir = new File(metsFile.getParentFile(),value);

                violations.addAll(
                        checkDirAgainstRef(refs.get(key), testingDir));

                it.remove(); // avoids a ConcurrentModificationException
            }
        } catch (Exception e) {
            violations.add(new ValidationViolation("Cross validation problem",
                "Cross validation failed. Error: " + e));
        }

        return violations;
    }

    /**
     * Check if files in directory are in ref list and if all files in ref list
     * are in directory
     *
     * @param refs - list of files names. Files could be with path.
     * @param baseDir - directory with files
     *
     * @return List<ValidationViolation> - different files
     */
    private List<ValidationViolation> checkDirAgainstRef(List<String> refs, File baseDir) {

        List<ValidationViolation> violations =
                new ArrayList<ValidationViolation>();
        if(!baseDir.exists()){
            violations.add(new ValidationViolation(ValidationTypes.CROSS_VALIDATION.getMessage(), "Dir " + baseDir.getName() + " does not exist!"));
            return violations;
        }
        
        Map<String, String> filesInDir = getFilesInDir(baseDir);

        //print all files witch are in Mets and not in directory
        for (String ref : refs) {
            String parsedRef = parseNameFromRef(ref);
            if (!filesInDir.containsKey(parsedRef)) {
                violations.add(
                    new ValidationViolation(ValidationTypes.CROSS_VALIDATION.getMessage(),
                            String.format("Missing file in directory. File: %s in Mets definition is"  +
                                          "missing in directory: %s", parsedRef,
                                          baseDir.getAbsolutePath())));
            } else {
                filesInDir.remove(parsedRef);
            }
        }

        //print all files witch are in directory and not in Mets
        Iterator it = filesInDir.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            violations.add(
                    new ValidationViolation(ValidationTypes.CROSS_VALIDATION.getMessage(),
                            String.format("Missing file in Mets file. File: %s from directory %s is" +
                                          "missing in Mets file", pairs.getKey(),
                                          baseDir.getAbsolutePath())));
            it.remove();
        }

        return violations;
    }

    private Map<String,String> getFilesInDir(File baseDir) {
        String[] files = baseDir.list();
        Map<String, String> filesInMap = new HashMap<String,String>();
        for (String file : files) {
            filesInMap.put(file, file);
        }

        return filesInMap;
    }

    /**
     * Parse string after last file separator
     *
     * @param ref - string to parse
     * @return
     */
    private String parseNameFromRef(String ref) {
        String[] path = ref.split("/");
        if (path.length == 0) {
            path = ref.split("\\\\");
        }

        return path[path.length - 1];
    }

    /**
     * Get mets references ordered against group
     *
     * @param metsFile
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws METSException
     */
    private Map<String,List<String>> getFileSecMap(File metsFile)
        throws IOException, SAXException, ParserConfigurationException, METSException {

        Document metsDocument = XMLHelper.parseXML(metsFile);
        METSWrapper mw = new METSWrapper(metsDocument);
        METS mets = mw.getMETSObject();

        return MetsUtils.getFileSecMap(mets);
    }

}
