/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logica.ndk.tm.utilities.validator.structures;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author brizat
 */
@XmlRootElement
public enum MandatoryEnum {
    MANDATORY, MANDATORY_IF_AVAILABLE, OPTIONAL
}
