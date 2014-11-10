/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.logica.ndk.tm.utilities.validator.structures;

import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author brizat
 */
@XmlRootElement
public class Attribute {
    
        
    private MandatoryEnum mandatory;
    private List<String> posibleValues;
    private String name;
    private String pattern;

    public Attribute() {
    }

    public Attribute(MandatoryEnum mandatory, List<String> posibleValues, String name) {
        this.mandatory = mandatory;
        this.posibleValues = posibleValues;
        this.name = name;
    }

    public Attribute(MandatoryEnum mandatory, String name) {
        this.mandatory = mandatory;
        this.name = name;
    
    }
    @XmlAttribute
    public MandatoryEnum getMandatory() {
        return mandatory;
    }

    public void setMandatory(MandatoryEnum mandatory) {
        this.mandatory = mandatory;
    }
    
    @XmlAttribute
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @XmlElement(name="possibleValue")
    public List<String> getPosibleValues() {
        return posibleValues;
    }

    public void setPosibleValues(List<String> posibleValues) {
        this.posibleValues = posibleValues;
    }
    
    @XmlElement(name="pattern")
    public String getPattern() {
      return pattern;
    }

    public void setPattern(String pattern) {
      this.pattern = pattern;
    }
    
    
}
