package com.logica.ndk.tm.validation.validator.parts;

import java.io.File;

import com.logica.ndk.tm.utilities.validation.ValidationViolationsWrapper;
import com.logica.ndk.tm.validation.data.DataPackage;
import com.logica.ndk.tm.validation.utils.DPResourceWrapper;
import com.logica.ndk.tm.validation.validator.core.MetsXSDValidator;

/**
 * @author Tomas Mriz (Logica)
 */
public class MetsValidator extends AbstractPartValidator {

    public MetsValidator (
        DataPackage dataToValidate,
        DPResourceWrapper validationProps
    ) {
        super(dataToValidate, validationProps);
    }

    @Override
    public ValidationViolationsWrapper validate() {
        logger.info("Mets validator is starting");
        ValidationViolationsWrapper result = new ValidationViolationsWrapper();
        File metsFile = dataToValidate.getMetsFile();

        if (metsFile.exists()) {
            MetsXSDValidator validator = new MetsXSDValidator(metsFile,
                    validationProps.getMetsXSDLocation(),
                    validationProps.getModsXSDLocation());
            ValidationViolationsWrapper xsdValidation = validator.validate();
            result.setViolationsList(xsdValidation.getViolationsList());
        }
        logger.info("Mets validator has end");

        return result;
    }
}
