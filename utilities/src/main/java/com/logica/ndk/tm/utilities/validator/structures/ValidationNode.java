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
public class ValidationNode {
    
    private MandatoryEnum mandatory;
    private String name;
    private List<ValidationNode> childs;
    private List<Attribute> atributes;
    private Boolean nullable;
    private String evaluateIf;
    private String pattern;

    public ValidationNode() {
    }

    public ValidationNode(MandatoryEnum mandatory, String name, String fatherPath, List<ValidationNode> childs, List<Attribute> atributes) {
        this.mandatory = mandatory;
        this.name = name;
        this.childs = childs;
        this.atributes = atributes;
    }

    @XmlElement(name="attribute")
    public List<Attribute> getAtributes() {
        return atributes;
    }

    public void setAtributes(List<Attribute> atributes) {
        this.atributes = atributes;
    }
    
    @XmlElement(name="element")
    public List<ValidationNode> getChilds() {
        return childs;
    }

    public void setChilds(List<ValidationNode> childs) {
        this.childs = childs;
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
    
    @XmlAttribute    
    public boolean isNullable() {
      if(nullable == null){
        return false;
      }
      return nullable;
    }

    public void setNullable(boolean nullable) {
      this.nullable = nullable;
    }
    
    @XmlAttribute()
    public String getEvaluateIf() {
      return evaluateIf;
    }

    public void setEvaluateIf(String evaluateIf) {
      this.evaluateIf = evaluateIf;
    }

    @XmlAttribute()
    public String getPattern() {
      return pattern;
    }

    public void setPattern(String pattern) {
      this.pattern = pattern;
    }

    
    
}
