package com.logica.ndk.tm.cdm;

import javax.xml.parsers.DocumentBuilderFactory;

public class PerThreadDocBuilderFactory {
	private static final ThreadLocal<DocumentBuilderFactory> documentBuilderFactoryHolder = new ThreadLocal<DocumentBuilderFactory>() {
		@Override
		protected DocumentBuilderFactory initialValue() {
			return DocumentBuilderFactory.newInstance();
		}
	};
	
	public static DocumentBuilderFactory getDocumentBuilderFactory() {
		return documentBuilderFactoryHolder.get(); 
	}
}
