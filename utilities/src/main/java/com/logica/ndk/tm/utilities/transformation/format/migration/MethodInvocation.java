package com.logica.ndk.tm.utilities.transformation.format.migration;

import java.lang.reflect.Method;

public class MethodInvocation {
	public String columnName;
	public Method invocationMethod;
	public boolean mandatory;
	public String defaultValue;

	public MethodInvocation(String columnName, Method invocationMethod,	boolean mandatory, String defaultValue) {
		this.columnName = columnName;
		this.invocationMethod = invocationMethod;
		this.mandatory = mandatory;
		this.defaultValue = defaultValue;
	}
	
	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public Method getInvocationMethod() {
		return invocationMethod;
	}

	public void setInvocationMethod(Method invocationMethod) {
		this.invocationMethod = invocationMethod;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }
	
	
}