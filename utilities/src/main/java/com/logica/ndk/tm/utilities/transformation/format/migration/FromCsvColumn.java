package com.logica.ndk.tm.utilities.transformation.format.migration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FromCsvColumn {
	
	String columnName();
	boolean mandatory() default false;
	String defaultValue() default "";
	
}
