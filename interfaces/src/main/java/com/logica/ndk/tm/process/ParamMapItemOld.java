package com.logica.ndk.tm.process;

import java.io.Serializable;

/**
 * Items for ParamMap class.
 * 
 * @author Rudolf Daco
 */
@Deprecated
public class ParamMapItemOld implements Serializable {
	private static final long serialVersionUID = 4102763164339764696L;
	private String name;
	private String value;

	public ParamMapItemOld() {
	}

	public ParamMapItemOld(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
