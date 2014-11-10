package com.logica.ndk.tm.validation;

import com.logica.ndk.tm.cdm.XMLHelper;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import com.logica.ndk.tm.validation.data.DataPackage;
import com.logica.ndk.tm.validation.utils.DPResourceWrapper;
import com.logica.ndk.tm.validation.validator.BaseValidator;
import com.logica.ndk.tm.validation.validator.IValidator;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tomas Mriz (Logica)
 */
public class DPValidator {

    private static String PROPERTIES_FILE_NAME = "DPValidator.properties";
    private static final transient Logger logger = LoggerFactory.getLogger(
            DPValidator.class);

    public static void main(String[] args) {
        logger.info("=== Validator is starting ===");
        DPResourceWrapper validatorProperties = null;
        String dataToValidate = null;

        try {
            validatorProperties = readPropertiesFile(args);
            dataToValidate = getDirToValidate(args);

            if (validatorProperties.getDirMode()) {
                multipleModValidation(validatorProperties, dataToValidate);
            } else {
                singleModValidation(validatorProperties, dataToValidate);
            }

        } catch (Exception e) {
            logger.error("Validation failed", e);
        }

        logger.info("=== Validator has end ===");
    }

    private static IValidator.ValidationStatus singleModValidation(
            DPResourceWrapper validatorProperties, String dataToValidate) {
        logger.info("======================================================================");
        IValidator validator = new BaseValidator();
        DataPackage packageToValidate = new DataPackage(dataToValidate);
        IValidator.ValidationStatus status = validator.doValidation(
                packageToValidate, validatorProperties);
        if (status == IValidator.ValidationStatus.FAILED) {
            logger.info("Validation failed - check output file for details");
        } else {
            logger.info("Validation was successful");
        }
        logger.info("======================================================================");

        return status;
    }

    private static void multipleModValidation(
            DPResourceWrapper validatorProperties, String dataToValidate) {
        logger.info("Multiple directory validation is ON");

        File basedDirectory = new File(dataToValidate);
        int errors = 0;

        if (basedDirectory.exists()) {
            String[] dirs = basedDirectory.list();
            logger.info(String.format("In directory is %s items to validate",
                    dirs.length));
            String processingDir = "";
            for (String dir : dirs) {
                processingDir = basedDirectory.getAbsolutePath() + File.separator + dir;
                logger.info("Processing directory: " + processingDir);
                IValidator.ValidationStatus status = singleModValidation(
                        validatorProperties, processingDir);
                if (status == IValidator.ValidationStatus.FAILED) {
                    errors++;
                }
            }
        } else {
            logger.error("Input directory does not exist");
        }

        logger.info("======================================================================");
        if (errors > 0) {
            logger.info(String.format("There are errors in directories (%s)",
                    errors));
        } else {
            logger.info("Validation was successful");
        }

        logger.info("Multiple validation has end");
    }

    private static DPResourceWrapper readPropertiesFile(String[] args)
            throws Exception {
        if (args.length > 0) {
            File propFile = new File(args[0]);
            InputStream fis;
            if (propFile.exists() && propFile.isFile()) {
                fis = new FileInputStream(propFile);
            } else {
                logger.info("Properties file not found");
                logger.info("Using defaults");
                fis = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME);
                //throw new Exception("Properties file not found");
            }
            Properties prop = new Properties();
            prop.load(fis);
            DPResourceWrapper dpResourceWrapper = new DPResourceWrapper(prop);
            return dpResourceWrapper;
        } else {
            logger.error("Properties file must be defined");
            throw new Exception("Properties file must be defined");
        }
    }

    private static String getDirToValidate(String[] args) throws Exception {
        if (args.length > 1) {
            return args[1];
        } else if(args.length > 0){
            if(new File(args[0]).isDirectory()){
                return args[0];
            }else{
                logger.error("Validation directory must be defined");
                throw new Exception("Validation directory must be defined");
            }
        }else{        
            logger.error("Validation directory must be defined");
            throw new Exception("Validation directory must be defined");
        }
    }
}
