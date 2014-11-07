/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logica.ndk.tm.validation.validator.parts;

import com.logica.ndk.tm.cdm.XMLHelper;
import com.logica.ndk.tm.utilities.em.ValidationViolation;
import com.logica.ndk.tm.utilities.validation.ValidationViolationsWrapper;
import com.logica.ndk.tm.validation.data.DataPackage;
import com.logica.ndk.tm.validation.utils.DPResourceWrapper;
import com.logica.ndk.tm.validation.validator.core.ResourceResolver;
import com.logica.ndk.tm.validation.validator.core.ValidationTypes;
import java.io.File;

/**
 *
 * @author brizat
 */
public class AltoValidator extends AbstractPartValidator {

    public AltoValidator(DataPackage dataToValidate, DPResourceWrapper validationProps) {
        super(dataToValidate, validationProps);
    }

    @Override
    public ValidationViolationsWrapper validate() {
        logger.info("Validating alto files started");
        File altoDir = dataToValidate.getAltoDir();
        ValidationViolationsWrapper result = new ValidationViolationsWrapper();
        String defaultMessage = ValidationTypes.ALTO_VALIDATION.getMessage();
        File[] altoFiles = altoDir.listFiles();

        for (File altoFile : altoFiles) {
            try {
                logger.info("Validating alto file: " + altoFile.getName());
                XMLHelper.validateXML(altoFile, validationProps.getAltoXsdLocation(), ResourceResolver.instance().getResolver());
            } catch (Exception e) {
                String message = defaultMessage.replace("{fileName}", altoFile.getName());
                logger.error(message + " " + e);
                result.add(new ValidationViolation(message, e.getMessage()));
            }
        }
        return result;
    }
}
