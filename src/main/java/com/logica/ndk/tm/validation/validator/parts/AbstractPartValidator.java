package com.logica.ndk.tm.validation.validator.parts;


import com.logica.ndk.tm.utilities.validation.ValidationViolationsWrapper;
import com.logica.ndk.tm.validation.data.DataPackage;
import com.logica.ndk.tm.validation.utils.DPResourceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tomas Mriz (Logica)
 */
public abstract class AbstractPartValidator {

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    protected DataPackage dataToValidate;
    protected DPResourceWrapper validationProps;

    public AbstractPartValidator(DataPackage dataToValidate, DPResourceWrapper validationProps) {
        this.dataToValidate = dataToValidate;
        this.validationProps = validationProps;
    }

     public abstract ValidationViolationsWrapper validate();
}
