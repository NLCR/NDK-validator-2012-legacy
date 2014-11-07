package com.logica.ndk.tm.validation.validator;

import java.util.*;

import com.logica.ndk.tm.utilities.em.ValidationViolation;
import com.logica.ndk.tm.utilities.validation.ValidationViolationsWrapper;
import com.logica.ndk.tm.validation.data.DataPackage;
import com.logica.ndk.tm.validation.printer.AbstractPrinter;
import com.logica.ndk.tm.validation.printer.FilePrinter;
import com.logica.ndk.tm.validation.utils.DPResourceWrapper;
import com.logica.ndk.tm.validation.validator.parts.*;

/**
 * @author Tomas Mriz (Logica)
 */
public class BaseValidator implements IValidator {

    private ArrayList<ValidationViolation> globalViolations;
    private ArrayList<AbstractPartValidator> validators;

    public BaseValidator() {
        this.globalViolations = new ArrayList<ValidationViolation>();
        this.validators = new ArrayList<AbstractPartValidator>();
    }

    @Override
    public ValidationStatus doValidation(DataPackage data, DPResourceWrapper validationProp) {

        initValidators(data, validationProp);
       
        for (AbstractPartValidator validator : validators) {
            ValidationViolationsWrapper violations = validator.validate();
            if (violations.getViolationsList() != null) {
                globalViolations.addAll(violations.getViolationsList());                
            }
        }
        
        printToFile(validationProp, data, globalViolations);
        if (globalViolations.isEmpty()) {
            return ValidationStatus.SUCCESS;
        }       
        
        return ValidationStatus.FAILED;
    }
    
    private void printToFile(DPResourceWrapper validationProp, DataPackage data, List<ValidationViolation> violations) {
        AbstractPrinter printer = new FilePrinter(validationProp.getOutputDir(), violations, validationProp.getOutputAppend(), data.getId());
        printer.printErrors();
    }   

    private void initValidators(DataPackage data,
            DPResourceWrapper validationProp) {
        validators.add(new StructureValidator(data, validationProp));
        validators.add(new MetsValidator(data, validationProp));
        validators.add(new MetadataValidator(data, validationProp));
        validators.add(new AltoValidator(data, validationProp));
    }

}

