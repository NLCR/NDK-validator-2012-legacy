package com.logica.ndk.tm.validation.printer;

import java.util.ArrayList;
import java.util.List;

import com.logica.ndk.tm.utilities.em.ValidationViolation;

/**
 * @author Tomas Mriz (Logica)
 */
public abstract class AbstractPrinter {

    protected List<ValidationViolation> violations;

    protected AbstractPrinter(List<ValidationViolation> violations) {
        this.violations = violations;
    }

    public abstract void printErrors();
}
