package com.logica.ndk.tm.validation.validator;

import java.util.Properties;

import com.logica.ndk.tm.validation.data.DataPackage;
import com.logica.ndk.tm.validation.utils.DPResourceWrapper;

/**
 * @author Tomas Mriz (Logica)
 */
public interface IValidator {

    public ValidationStatus doValidation(DataPackage data,
            DPResourceWrapper validationProp);

    public enum ValidationStatus {
        SUCCESS,
        FAILED
    }
}
